/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Angelo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package minimatch;

import static minimatch.internal.StringUtils.matches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import minimatch.internal.StringUtils;
import minimatch.internal.adapters.DefaultPathAdapter;
import minimatch.internal.parser.GlobStar;
import minimatch.internal.parser.LiteralItem;
import minimatch.internal.parser.MagicItem;
import minimatch.internal.parser.ParseContext;
import minimatch.internal.parser.ParseItem;
import minimatch.internal.parser.ParseResult;
import minimatch.internal.parser.PatternListItem;

/**
 * Port of Node.js' https://github.com/isaacs/minimatch to Java.
 *
 */
public class Minimatch {

	private static final GlobStar GLOBSTAR = new GlobStar();

	// any single thing other than /
	// don't need to escape / when using new RegExp()
	private final String qmark = "[^/]"

	// * => any number of characters
			,
			star = qmark + "*?";

	private final List<Character> reSpecials = StringUtils
			.asList("().*{}+?[]^$\\!".toCharArray());

	private static final Pattern hasBraces = Pattern.compile("\\{.*\\}");
	protected static final String slashSplit = "/+";

	protected String pattern;
	protected final Options options;
	protected boolean comment;
	protected boolean empty;
	protected boolean negate;

	private List<List<ParseItem>> set;

	public Minimatch(String pattern) {
		this(pattern, null);
	}

	public Minimatch(String pattern, Options options) {
		this.pattern = pattern.trim();
		this.options = getOptions(options);

		if (this.options.isAllowWindowsPaths()) {
			pattern = StringUtils.replacePath(pattern);
		}

		// this.regexp = null
		this.negate = false;
		this.comment = false;
		this.empty = false;

		// make the set of regexps etc.
		this.make();
	}

	private void make() {
		// don't do it more than once.
		// if (this._made) {
		// return;
		// }
		String pattern = this.pattern;
		Options options = this.options;

		// empty patterns and comments match nothing.
		if (!options.isNocomment() && pattern.charAt(0) == '#') {
			this.comment = true;
			return;
		}

		if (pattern == null) {
			this.empty = true;
			return;
		}

		// step 1: figure out negation, etc.
		this.parseNegate();

		// step 2: expand braces
		String[] set = /* this.globSet = */this.braceExpand(pattern, options);

		// if (options.debug) {
		// this.debug = console.error
		// }
		// this.debug(this.pattern, set)

		// step 3: now we have a set, so turn each one into a series
		// of path-portion
		// matching patterns.
		// These will be regexps, except in the case of "**", which is
		// set to the GLOBSTAR object for globstar behavior,
		// and will not contain any / characters
		// set /* = this.globParts */ = set.map(function(s) {
		// return s.split(slashSplit)
		// })
		String[][] globParts = globParts(set);
		if (options.isDebug()) {
			this.debug(this.pattern, set);
		}

		// glob --> regexps
		List<List<ParseItem>> results = globToRegExps(globParts);
		
		// filter out everything that didn't compile properly.
		/*
		 * set = set.filter(function (s) { return s.indexOf(false) === -1 })
		 */
		if (options.isDebug()) {
			this.debug(this.pattern, set);
		}
		this.set = results;

	}

	private String[][] globParts(String[] set) {
		String[][] parts = new String[set.length][];
		for (int i = 0; i < set.length; i++) {
			parts[i] = set[i].split(slashSplit);
		}
		return parts;
	}

	private List<List<ParseItem>> globToRegExps(String[][] globParts) {
		String[] s = null;
		List<List<ParseItem>> parts = new ArrayList<List<ParseItem>>();
		List<ParseItem> p = null;
		for (int i = 0; i < globParts.length; i++) {
			s = globParts[i];
			p = new ArrayList<ParseItem>();
			parts.add(p);
			for (int j = 0; j < s.length; j++) {
				p.add(parse(s[j], false).getItem());
			}
		}
		return parts;
	}

	// parse a component of the expanded set.
	// At this point, no pattern may contain "/" in it
	// so we're going to return a 2d array, where each entry is the
	// full
	// pattern, split on '/', and then turned into a regular
	// expression.
	// A regexp is made at the end which joins each array with an
	// escaped /, and another full one which joins each regexp with
	// |.
	//
	// Following the lead of Bash 4.1, note that "**" only has
	// special meaning
	// when it is the *only* thing in a path portion. Otherwise, any
	// series
	// of * is equivalent to a single *. Globstar behavior is
	// enabled by
	// default, and can be disabled by setting options.noglobstar.
	private ParseResult parse(String pattern, boolean isSub) {
		Options options = this.options;

		// shortcuts
		if (!options.isNoglobstar() && "**".equals(pattern)) {
			return new ParseResult(GLOBSTAR, false);
		}
		if (StringUtils.isEmpty(pattern)) {
			return new ParseResult(ParseItem.Empty, false);
		}

		ParseContext ctx = new ParseContext();
		ctx.re = "";
		ctx.hasMagic = options.isNocase();

		boolean escaping = false;
		// ? => one single character
		Stack<PatternListItem> patternListStack = new Stack<PatternListItem>();
		char plType;

		boolean inClass = false;
		int reClassStart = -1;
		int classStart = -1;
		// . and .. never match anything that doesn't start with .,
		// even when options.dot is set.
		String patternStart = pattern.charAt(0) == '.' ? "" // anything
		// not (start or / followed by . or .. followed by / or end)
				: options.isDot() ? "(?!(?:^|\\/)\\.{1,2}(?:$|\\/))"
						: "(?!\\.)";

		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (options.isDebug()) {
				this.debug("%s\t%s %s %j", pattern, i, ctx.re, c);
			}
			// skip over any that are escaped.
			if (escaping && reSpecials.contains(c)) {
				ctx.re += '\\' + c;
				escaping = false;
				continue;
			}

			switch (c) {
			case '/':
				// completely not allowed, even escaped.
				// Should already be path-split by now.
				// return false;
				return null;

			case '\\':
				clearStateChar(ctx);
				escaping = true;
				continue;
				// the various stateChar values
				// for the "extglob" stuff.
			case '?':
			case '*':
			case '+':
			case '@':
			case '!':
				if (options.isDebug()) {
					this.debug("%s\t%s %s %j <-- stateChar", pattern, i,
							ctx.re, c);
				}

				// all of those are literals inside a class, except that
				// the glob [!a] means [^a] in regexp
				if (inClass) {
					if (options.isDebug()) {
						this.debug("  in class");
					}
					if (c == '!' && i == classStart + 1) {
						c = '^';
					}
					ctx.re += c;
					continue;
				}

				// if we already have a stateChar, then it means
				// that there was something like ** or +? in there.
				// Handle the stateChar, then proceed with this one.
				if (options.isDebug()) {
					this.debug("call clearStateChar %j", ctx.stateChar);
				}
				clearStateChar(ctx);
				ctx.stateChar = c;
				// if extglob is disabled, then +(asdf|foo) isn't a thing.
				// just clear the statechar *now*, rather than even diving
				// into
				// the patternList stuff.
				if (options.isNoext()) {
					clearStateChar(ctx);
				}
				continue;

			case '(':
				if (inClass) {
					ctx.re += "(";
					continue;
				}

				if (ctx.stateChar == null) {
					ctx.re += "\\(";
					continue;
				}

				plType = ctx.stateChar;
				patternListStack.push(new PatternListItem(plType, i - 1, ctx.re
						.length()));

				// negation is (?:(?!js)[^/]*)
				ctx.re += ctx.stateChar == '!' ? "(?:(?!" : "(?:";
				if (options.isDebug()) {
					this.debug("plType %j %j", ctx.stateChar, ctx.re);
				}
				ctx.stateChar = null; // false;
				continue;

			case ')':
				if (inClass || patternListStack.size() == 0) {
					ctx.re += "\\)";
					continue;
				}

				clearStateChar(ctx);
				ctx.hasMagic = true;
				ctx.re += ")";
				plType = patternListStack.pop().type;
				// negation is (?:(?!js)[^/]*)
				// The others are (?:<pattern>)<type>
				switch (plType) {
				case '!':
					ctx.re += "[^/]*?)";
					break;
				case '?':
				case '+':
				case '*':
					ctx.re += plType;
					break;
				case '@':
					break; // the default anyway
				}
				continue;

			case '|':
				if (inClass || patternListStack.size() == 0 || escaping) {
					ctx.re += "\\|";
					escaping = false;
					continue;
				}

				clearStateChar(ctx);
				ctx.re += '|';
				continue;

				// these are mostly the same in regexp and glob
			case '[':
				// swallow any state-tracking char before the [
				clearStateChar(ctx);

				if (inClass) {
					ctx.re += '\\' + c;
					continue;
				}

				inClass = true;
				classStart = i;
				reClassStart = ctx.re.length();
				ctx.re += c;
				continue;

			case ']':
				// a right bracket shall lose its special
				// meaning and represent itself in
				// a bracket expression if it occurs
				// first in the list. -- POSIX.2 2.8.3.2
				if (i == classStart + 1 || !inClass) {
					ctx.re += "\\" + c;
					escaping = false;
					continue;
				}

				// handle the case where we left a class open.
				// "[z-a]" is valid, equivalent to "\[z-a\]"
				if (inClass) {
					// split where the last [ was, make sure we don't have
					// an invalid re. if so, re-walk the contents of the
					// would-be class to re-translate any characters that
					// were passed through as-is
					// TODO: It would probably be faster to determine this
					// without a try/catch and a new RegExp, but it's tricky
					// to do safely. For now, this is safe and works.
					String cs = pattern.substring(classStart + 1, i);
					try {
						// RegExp('[' + cs + ']');
						Pattern.compile("[" + cs + "]");
					} catch (Throwable e) {
						// not a valid class!
						ParseResult sp = this.parse(cs, true);
						ctx.re = ctx.re.substring(0, reClassStart) + "\\["
								+ sp.getItem() + "\\]";
						ctx.hasMagic = ctx.hasMagic || sp.isB();
						inClass = false;
						continue;
					}
				}

				// finish up the class.
				ctx.hasMagic = true;
				inClass = false;
				ctx.re += c;
				continue;

			default:
				// swallow any state char that wasn't consumed
				clearStateChar(ctx);

				if (escaping) {
					// no need
					escaping = false;
				} else if (reSpecials.contains(c) && !(c == '^' && inClass)) {
					ctx.re += "\\";
				}

				ctx.re += c;

			} // switch
		} // for

		// handle the case where we left a class open.
		// "[abc" is valid, equivalent to "\[abc"
		if (inClass) {
			// split where the last [ was, and escape it
			// this is a huge pita. We now have to re-walk
			// the contents of the would-be class to re-translate
			// any characters that were passed through as-is
			String cs = pattern.substring(classStart + 1);
			ParseResult sp = this.parse(cs, true);
			ctx.re = ctx.re.substring(0, reClassStart) + "\\[" + sp.getItem();
			ctx.hasMagic = ctx.hasMagic || sp.isB();
		}

		// handle the case where we had a +( thing at the *end*
		// of the pattern.
		// each pattern list stack adds 3 chars, and we need to go through
		// and escape any | chars that were passed through as-is for the regexp.
		// Go through and escape them, taking care not to double-escape any
		// | chars that were already escaped.
		while (!patternListStack.isEmpty()) {
			PatternListItem pl = patternListStack.pop();
			String tail = ""; // re.slice(pl.reStart + 3);
			// maybe some even number of \, then maybe 1 \, followed by a |
			/*
			 * tail = tail.replace(/((?:\\{2})*)(\\?)\|/g, function (_, $1, $2)
			 * { if (!$2) { // the | isn't already escaped, so escape it. $2 =
			 * '\\'; }
			 * 
			 * // need to escape all those slashes *again*, without escaping the
			 * // one that we need for escaping the | character. As it works
			 * out, // escaping an even number of slashes can be done by simply
			 * repeating // it exactly after itself. That's why this trick
			 * works. // // I am sorry that you have to see this. return $1 + $1
			 * + $2 + '|'; })
			 */

			if (options.isDebug()) {
				this.debug("tail=%j\n   %s", tail, tail);
			}
			String t = "*".equals(pl.type) ? star : "?".equals(pl.type) ? qmark
					: "\\" + pl.type;

			ctx.hasMagic = true;
			// ctx.re = ctx.re.slice(0, pl.reStart) + t + "\\(" + tail;
		}

		// handle trailing things that only matter at the very end.
		clearStateChar(ctx);
		if (escaping) {
			// trailing \\
			ctx.re += "\\\\";
		}

		// only need to apply the nodot start if the re starts with
		// something that could conceivably capture a dot
		boolean addPatternStart = false;
		switch (ctx.re.charAt(0)) {
		case '.':
		case '[':
		case '(':
			addPatternStart = true;
		}

		// if the re is not "" at this point, then we need to make sure
		// it doesn't match against an empty path part.
		// Otherwise a/* will match a/, which it should not.
		if (!StringUtils.isEmpty(ctx.re) && ctx.hasMagic)
			ctx.re = "(?=.)" + ctx.re;

		if (addPatternStart) {
			ctx.re = patternStart + ctx.re;
		}
		// parsing just a piece of a larger pattern.
		if (isSub) {
			return new ParseResult(new LiteralItem(ctx.re), ctx.hasMagic);
		}

		// skip the regexp for non-magical patterns
		// unescape anything in it, though, so that it'll be
		// an exact match against a file etc.
		if (!ctx.hasMagic) {
			return new ParseResult(new LiteralItem(
					StringUtils.globUnescape(pattern)), false);
		}
		// var flags = options.nocase ? 'i' : ''
		// var regExp = new RegExp('^' + re + '$', flags)

		// regExp._glob = pattern
		// regExp._src = re

		return new ParseResult(new MagicItem(ctx.re, options), false);
	}

	protected void debug(String pattern, Object... arguments) {
		this.options.getDebugger().debug(pattern, arguments);
	}

	private void clearStateChar(ParseContext ctx) {
		if (ctx.stateChar != null) {
			// we had some state-tracking character
			// that wasn't consumed by this pass.
			switch (ctx.stateChar) {
			case '*':
				ctx.re += star;
				ctx.hasMagic = true;
				break;
			case '?':
				ctx.re += qmark;
				ctx.hasMagic = true;
				break;
			default:
				ctx.re += "\\" + ctx.stateChar;
				break;
			}
			ctx.stateChar = null;
		}
	}

	private void parseNegate() {
		String pattern = this.pattern;
		boolean negate = false;
		Options options = this.options;
		int negateOffset = 0;

		if (options.isNonegate()) {
			return;
		}

		for (int i = 0, l = pattern.length(); i < l && pattern.charAt(i) == '!'; i++) {
			negate = !negate;
			negateOffset++;
		}

		if (negateOffset > 0) {
			this.pattern = pattern.substring(negateOffset);
		}
		this.negate = negate;
	}

	private String[] braceExpand(String pattern, Options options) {
		if (options.isNobrace() || !matches(hasBraces, pattern)) {
			// shortcut. no need to expand.
			return new String[] { pattern };
		}
		return expand(pattern);
	}

	private String[] expand(String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> boolean match(T f, PathAdapter<T> adapter) {
		return match(f, adapter, false);
	}

	public <T> boolean match(T f, PathAdapter<T> adapter, boolean partial) {
		Options options = this.options;
		if (options.isDebug()) {
			this.debug(this.pattern, "split", f);
		}
		// just ONE of the pattern sets in this.set needs to match
		// in order for it to be valid. If negating, then just one
		// match means that we have failed.
		// Either way, return on the first hit.

		List<List<ParseItem>> set = this.set;
		if (options.isDebug()) {
			this.debug(this.pattern, "set", set);
		}
		// Find the basename of the path by looking for the last non-empty
		// segment
		String filename = null;
		int i;
		for (i = adapter.getLength(f) - 1; i >= 0; i--) {
			filename = adapter.getPathName(f, i);
			// if (filename) break;
			if (!StringUtils.isEmpty(filename))
				break;
		}

		for (i = 0; i < set.size(); i++) {
			List<ParseItem> pattern = set.get(i);
			T file = f;
			if (options.isMatchBase() && pattern.size() == 1) {
				file = adapter.createPath(filename);
			}
			boolean hit = this.matchOne(file, adapter, pattern, partial);
			if (hit) {
				if (options.isFlipNegate())
					return true;
				return !this.negate;
			}
		}

		// didn't get any hits. this is success if it's a negative
		// pattern, failure otherwise.
		if (options.isFlipNegate())
			return false;
		return this.negate;
	}

	protected static Options getOptions(Options options) {
		return options == null ? Options.DEFAULT : options;
	}

	// set partial to true to test if, for example,
	// "/a/b" matches the start of "/*/b/*/d"
	// Partial means, if you run out of file before you run
	// out of pattern, then that's fine, as long as all
	// the parts match.
	private <T> boolean matchOne(T file, PathAdapter<T> adapter,
			List<ParseItem> pattern, boolean partial) {
		Options options = this.options;

		if (options.isDebug()) {
			// //this.debug('matchOne',
			// // { 'this': this, file: file, pattern: pattern })
			this.debug("matchOne", adapter.getLength(file), pattern.size());
		}

		int fi = 0, pi = 0, fl = adapter.getLength(file), pl = pattern.size();
		for (; (fi < fl) && (pi < pl); fi++, pi++) {
			if (options.isDebug()) {
				this.debug("matchOne loop");
			}
			ParseItem p = pattern.get(pi);
			String f = adapter.getPathName(file, fi);

			// this.debug(pattern, p, f);

			// should be impossible.
			// some invalid regexp stuff in the set.
			if (p == null) {
				return false;
			}

			if (p instanceof GlobStar) {
				if (options.isDebug()) {
					this.debug("GLOBSTAR", pattern, p, f);
				}
				// "**"
				// a/**/b/**/c would match the following:
				// a/b/x/y/z/c
				// a/x/y/z/b/c
				// a/b/x/b/x/c
				// a/b/c
				// To do this, take the rest of the pattern after
				// the **, and see if it would match the file remainder.
				// If so, return success.
				// If not, the ** "swallows" a segment, and try again.
				// This is recursively awful.
				//
				// a/**/b/**/c matching a/b/x/y/z/c
				// - a matches a
				// - doublestar
				// - matchOne(b/x/y/z/c, b/**/c)
				// - b matches b
				// - doublestar
				// - matchOne(x/y/z/c, c) -> no
				// - matchOne(y/z/c, c) -> no
				// - matchOne(z/c, c) -> no
				// - matchOne(c, c) yes, hit
				int fr = fi;
				int pr = pi + 1;
				if (pr == pl) {
					if (options.isDebug()) {
						this.debug("** at the end");
					}
					// a ** at the end will just swallow the rest.
					// We have found a match.
					// however, it will not swallow /.x, unless
					// options.dot is set.
					// . and .. are *never* matched by **, for explosively
					// exponential reasons.
					for (; fi < fl; fi++) {
						String fileitem = adapter.getPathName(file, fi);
						if (fileitem.equals(".")
								|| fileitem.equals("..")
								|| (!options.isDot() && fileitem.charAt(0) == '.'))
							return false;
					}
					return true;
				}

				// ok, let's see if we can swallow whatever we can.
				while (fr < fl) {
					String swallowee = adapter.getPathName(file, fr);

					if (options.isDebug()) {
						this.debug("\nglobstar while", file, fr, pattern, pr,
								swallowee);
					}
					// XXX remove this slice. Just pass the start index.
					if (this.matchOne(adapter.subPath(file, fr), adapter,
							pattern.subList(pr, pattern.size()), partial)) {
						if (options.isDebug()) {
							this.debug("globstar found match!", fr, fl,
									swallowee);
						}
						// found a match.
						return true;
					} else {
						// can't swallow "." or ".." ever.
						// can only swallow ".foo" when explicitly asked.
						if (swallowee.equals(".")
								|| swallowee.equals("..")
								|| (!options.isDot() && swallowee.charAt(0) == '.')) {
							if (options.isDebug()) {
								this.debug("dot detected!", file, fr, pattern,
										pr);
							}
							break;
						}

						// ** swallows a segment, and continue.
						if (options.isDebug()) {
							this.debug("globstar swallow a segment, and continue");
						}
						fr++;
					}
				}

				// no match was found.
				// However, in partial mode, we can't say this is necessarily
				// over.
				// If there's more *pattern* left, then
				if (partial) {
					// ran out of file
					if (options.isDebug()) {
						this.debug("\n>>> no match, partial?", file, fr,
								pattern, pr);
					}
					if (fr == fl)
						return true;
				}
				return false;
			}

			// something other than **
			// non-magic patterns just have to match exactly
			// patterns with magic have been turned into regexps.
			if (!p.match(f, options)) {
				return false;
			}

		}
		// Note: ending in / means that we'll get a final ""
		// at the end of the pattern. This can only match a
		// corresponding "" at the end of the file.
		// If the file ends in /, then it can only match a
		// a pattern that ends in /, unless the pattern just
		// doesn't have any more for it. But, a/b/ should *not*
		// match "a/b/*", even though "" matches against the
		// [^/]*? pattern, except in partial mode, where it might
		// simply not be reached yet.
		// However, a/b/ should still satisfy a/*

		// now either we fell off the end of the pattern, or we're done.
		if (fi == fl && pi == pl) {
			// ran out of pattern and filename at the same time.
			// an exact hit!
			return true;
		} else if (fi == fl) {
			// ran out of file, but still had pattern left.
			// this is ok if we're doing the match as part of
			// a glob fs traversal.
			return partial;
		} else if (pi == pl) {
			// ran out of pattern, still have file left.
			// this is only acceptable if we're on the very last
			// empty segment of a file with a trailing slash.
			// a/* should match a/b/
			boolean emptyFileEnd = (fi == fl - 1)
					&& (adapter.getPathName(file, fi).equals(""));
			return emptyFileEnd;
		}

		// should be unreachable.
		throw new IllegalStateException("wtf?");
	}

	public static boolean minimatch(String p, String pattern) {
		return minimatch(p, pattern, null);
	}

	public static boolean minimatch(String p, String pattern, Options options) {
		options = getOptions(options);
		if (options == null) {
			options = Options.DEFAULT;
		}
		// shortcut: comments match nothing.
		if (!options.isNocomment() && pattern.charAt(0) == '#') {
			return false;
		}
		// "" only matches ""
		if (StringUtils.isEmpty(pattern.trim())) {
			return "".equals(p);
		}

		return new Minimatch(pattern, options).match(p);
	}

	public boolean match(String p) {
		return match(p, false);
	}

	public boolean match(String input, boolean partial) {
		if (options.isDebug()) {
			this.debug("match", input, this.pattern);
		}
		// short-circuit in the case of busted things.
		// comments, etc.
		if (this.comment)
			return false;
		if (this.empty)
			return StringUtils.isEmpty(input);

		if ("/".equals(input) && partial)
			return true;

		Options options = this.options;

		// windows: need to use /, not \
		if (options.isAllowWindowsPaths()) {
			input = StringUtils.replacePath(input);
		}

		// treat the test path as a set of pathparts.
		List<String> f = Arrays.asList(input.split(slashSplit));
		return match(f, DefaultPathAdapter.INSTANCE, partial);
	}

}
