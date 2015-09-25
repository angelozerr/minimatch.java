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
import java.util.regex.Matcher;
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
	private final String QMARK = "[^/]";

	// * => any number of characters
	private final String STAR = QMARK + "*?";

	// ** when dots are allowed.  Anything goes, except .. and .
	// not (^ or / followed by one or two dots followed by $ or /),
	// followed by anything, any number of times.
	private final String TWO_STAR_DOT = "(?:(?!(?:\\/|^)(?:\\.{1,2})($|\\/)).)*?";

	// not a ^ or / followed by a dot,
	// followed by anything, any number of times.
	private final String TWO_STAR_NO_DOT = "(?:(?!(?:\\/|^)\\.).)*?'";
	
	private final List<Character> reSpecials = StringUtils
			.asList("().*{}+?[]^$\\!".toCharArray());

	private static final Pattern hasBraces = Pattern.compile("\\{.*\\}");
	protected static final Pattern slashSplit = Pattern.compile("/+");

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
		if (!options.isNocomment() && !pattern.isEmpty() && pattern.charAt(0) == '#') {
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
		String[] set = /* this.globSet = */this.braceExpand(this.pattern, this.options);

		this.debug("%s %s", this.pattern, Arrays.toString(set)); //$NON-NLS-1$

		// step 3: now we have a set, so turn each one into a series
		// of path-portion
		// matching patterns.
		// These will be regexps, except in the case of "**", which is
		// set to the GLOBSTAR object for globstar behavior,
		// and will not contain any / characters
		String[][] globParts = globParts(set);
		this.debug("%s %s", this.pattern, toString(globParts));

		// glob --> regexps
		List<List<ParseItem>> results = globToRegExps(globParts);
		
		// filter out everything that didn't compile properly.
		/*
		 * set = set.filter(function (s) { return s.indexOf(false) === -1 })
		 */
		this.debug("%s %s", this.pattern, results);
		
		this.set = results;
	}
	
	private String toString(String[][] globParts) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (String[] arr: globParts) {
			sb.append(Arrays.toString(arr));
			sb.append(", ");
		}
		if (sb.length() > 1) {
			sb.setLength(sb.length() - 2);
		}
		sb.append(']');
		return sb.toString();
	}

	private String[][] globParts(String[] set) {
		String[][] parts = new String[set.length][];
		for (int i = 0; i < set.length; i++) {
			parts[i] = slashSplit.split(set[i], Integer.MAX_VALUE);
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
		Stack<PatternListItem> negativeListStack = new Stack<PatternListItem>();
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
			this.debug("%s\t%s %s \"%c\"", pattern, i, ctx.re, c);
			
			// skip over any that are escaped.
			if (escaping && reSpecials.contains(c)) {
				ctx.re += "\\" + c;
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
				this.debug("%s\t%s %s \"%c\" <-- stateChar", pattern, i,
						ctx.re, c);

				// all of those are literals inside a class, except that
				// the glob [!a] means [^a] in regexp
				if (inClass) {
					this.debug("  in class");
					if (c == '!' && i == classStart + 1) {
						c = '^';
					}
					ctx.re += c;
					continue;
				}

				// if we already have a stateChar, then it means
				// that there was something like ** or +? in there.
				// Handle the stateChar, then proceed with this one.
				this.debug("call clearStateChar \"%c\"", ctx.stateChar);
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
				ctx.re += ctx.stateChar == '!' ? "(?:(?!(?:" : "(?:";
				this.debug("plType \"%c\" \"%s\"", ctx.stateChar, ctx.re);
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
				PatternListItem pl = patternListStack.pop();
				plType = pl.type;
				// negation is (?:(?!js)[^/]*)
				// The others are (?:<pattern>)<type>
				switch (plType) {
				case '!':
					negativeListStack.push(pl);
					ctx.re += ")[^/]*?)";
					pl.reEnd = ctx.re.length();
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
						Pattern.compile("[" + cs + "]");
					} catch (Throwable e) {
						// not a valid class!
						ParseResult sp = this.parse(cs, true);
						ctx.re = ctx.re.substring(0, reClassStart) + "\\["
								+ sp.getItem().getSource() + "\\]";
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
			ctx.re = ctx.re.substring(0, reClassStart) + "\\[" + sp.getItem().getSource();
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
			String tail = ctx.re.substring(pl.reStart + 3);
			// maybe some even number of \, then maybe 1 \, followed by a |
			Pattern p = Pattern.compile("((?:\\\\{2})*)(\\\\?)\\|");
			
			//Java 1.6 lacks functional programming stuff, so replace manually 
			Matcher m = p.matcher(tail);
			StringBuilder sb = new StringBuilder();
			int lastEnd = 0;
			while (m.find()) {
				String g1 = m.group(1);
				String g2 = m.group(2);
				if (g2 == null || g2.isEmpty()) {
			        // the | isn't already escaped, so escape it.
					g2 = "\\";
				}
		        // need to escape all those slashes *again*, without escaping the
		        // one that we need for escaping the | character.  As it works out,
		        // escaping an even number of slashes can be done by simply repeating
		        // it exactly after itself.  That's why this trick works.
		        //
		        // I am sorry that you have to see this.
				sb.append(tail.substring(lastEnd, m.start()));
				sb.append(g1 + g1 + g2 + "|");
				lastEnd = m.end();
			}
			sb.append(tail.substring(lastEnd));

			tail = sb.toString();
			
			this.debug("tail=%s\n   %s", tail, tail);
			String t = "*".equals(pl.type) ? STAR : "?".equals(pl.type) ? QMARK
					: "\\" + pl.type;

			ctx.hasMagic = true;
			ctx.re = ctx.re.substring(0, pl.reStart) + t + "\\(" + tail;
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
		
		// Hack to work around lack of negative lookbehind in JS
		// A pattern like: *.!(x).!(y|z) needs to ensure that a name
		// like 'a.xyz.yz' doesn't match.  So, the first negative
		// lookahead, has to look ALL the way ahead, to the end of
		// the pattern.
		while(!negativeListStack.isEmpty()) {
			PatternListItem nl = negativeListStack.pop();

		    String nlBefore = ctx.re.substring(0, nl.reStart);
		    String nlFirst = ctx.re.substring(nl.reStart, nl.reEnd - 8);
		    String nlLast = ctx.re.substring(nl.reEnd - 8, nl.reEnd);
		    String nlAfter = ctx.re.substring(nl.reEnd);

		    nlLast += nlAfter;

		    // Handle nested stuff like *(*.js|!(*.json)), where open parens
		    // mean that we should *not* include the ) in the bit that is considered
		    // "after" the negated section.
		    int openParensBefore = nlBefore.split("\\(").length - 1;
		    String cleanAfter = nlAfter;
		    for (int i = 0; i < openParensBefore; i++) {
		    	cleanAfter = cleanAfter.replaceAll("\\)[+*?]?", "");
		    }
		    nlAfter = cleanAfter;

		    String dollar = "";
		    if (nlAfter.isEmpty() && !isSub) {
		    	dollar = "$";
		    }
		    String newRe = nlBefore + nlFirst + nlAfter + dollar + nlLast;
		    ctx.re = newRe;
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

		return new ParseResult(new MagicItem(ctx.re, options), false);
	}

	protected void debug(String pattern, Object... arguments) {
		if (this.options.isDebug()) {
			this.options.getDebugger().debug(pattern, arguments);
		}
	}

	private void clearStateChar(ParseContext ctx) {
		if (ctx.stateChar != null) {
			// we had some state-tracking character
			// that wasn't consumed by this pass.
			switch (ctx.stateChar) {
			case '*':
				ctx.re += STAR;
				ctx.hasMagic = true;
				break;
			case '?':
				ctx.re += QMARK;
				ctx.hasMagic = true;
				break;
			default:
				ctx.re += "\\" + ctx.stateChar;
				break;
			}
			debug("clearStateChar \"%c\" \"%s\"", ctx.stateChar, ctx.re);
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
		//XXX - implement brace expansion
		return new String[] {pattern};
	}

	private boolean match(List<String> f, boolean partial) {

//		if ("/".equals(input) && partial)
//			return true;
		
		Options options = this.options;
		
		// just ONE of the pattern sets in this.set needs to match
		// in order for it to be valid. If negating, then just one
		// match means that we have failed.
		// Either way, return on the first hit.

		List<List<ParseItem>> set = this.set;
		this.debug("%s %s %s", this.pattern, "set", set);
		
		// Find the basename of the path by looking for the last non-empty
		// segment
		String filename = null;
		int i;
		for (i = f.size() - 1; i >= 0; i--) {
			filename = f.get(i);
			// if (filename) break;
			if (!StringUtils.isEmpty(filename))
				break;
		}

		for (i = 0; i < set.size(); i++) {
			List<ParseItem> pattern = set.get(i);
			List<String> file = f;
			if (options.isMatchBase() && pattern.size() == 1) {
				file = new ArrayList<String>();
				file.add(filename);
			}
			boolean hit = this.matchOne(file, pattern, partial);
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
	private <T> boolean matchOne(List<String> file,
			List<ParseItem> pattern, boolean partial) {
		Options options = this.options;

		this.debug("matchOne\n\tOptions: %s\n\tfile: %s\n\tpattern: %s", options, file, pattern);
		
		this.debug("matchOne %s %s", file.size(), pattern.size());

		int fi = 0, pi = 0, fl = file.size(), pl = pattern.size();
		for (; (fi < fl) && (pi < pl); fi++, pi++) {
			this.debug("matchOne loop");
			ParseItem p = pattern.get(pi);
			String f = file.get(fi);

			this.debug("%s %s %s", pattern, p, f);

			// should be impossible.
			// some invalid regexp stuff in the set.
			if (p == null) {
				return false;
			}

			if (p instanceof GlobStar) {
				this.debug("GLOBSTAR [%s, %s, %s]", pattern, p, f);
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
					this.debug("** at the end");
					// a ** at the end will just swallow the rest.
					// We have found a match.
					// however, it will not swallow /.x, unless
					// options.dot is set.
					// . and .. are *never* matched by **, for explosively
					// exponential reasons.
					for (; fi < fl; fi++) {
						String fileitem = file.get(fi);
						if (fileitem.equals(".")
								|| fileitem.equals("..")
								|| (!options.isDot() && fileitem.length() > 0 && fileitem.charAt(0) == '.'))
							return false;
					}
					return true;
				}

				// ok, let's see if we can swallow whatever we can.
				while (fr < fl) {
					String swallowee = file.get(fr);

					this.debug("\nglobstar while %s %s %s %s %s", file, fr, pattern, pr,
							swallowee);
					// XXX remove this slice. Just pass the start index.
					if (this.matchOne(file.subList(fr, file.size()),
							pattern.subList(pr, pattern.size()), partial)) {
						this.debug("globstar found match! %s %s %s", fr, fl,
								swallowee);
						// found a match.
						return true;
					} else {
						// can't swallow "." or ".." ever.
						// can only swallow ".foo" when explicitly asked.
						if (swallowee.equals(".")
								|| swallowee.equals("..")
								|| (!options.isDot() && swallowee.charAt(0) == '.')) {
							this.debug("dot detected! %s %s %s %s", file, fr, pattern,
									pr);
							break;
						}

						// ** swallows a segment, and continue.
						this.debug("globstar swallow a segment, and continue");
						fr++;
					}
				}

				// no match was found.
				// However, in partial mode, we can't say this is necessarily
				// over.
				// If there's more *pattern* left, then
				if (partial) {
					// ran out of file
					this.debug("\n>>> no match, partial? %s %s %s %s", file, fr,
							pattern, pr);
					if (fr == fl)
						return true;
				}
				return false;
			}

			// something other than **
			// non-magic patterns just have to match exactly
			// patterns with magic have been turned into regexps.
			if (!p.match(f, options)) {
			    this.debug("pattern match %s %s false", p, f);
				return false;
			}
		    this.debug("pattern match %s %s true", p, f);

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
					&& (file.get(fi).equals(""));
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
		return match(input, DefaultPathAdapter.INSTANCE, partial);
	}

	public <T> boolean match(T f, PathAdapter<T> adapter) {
		return match(f, adapter, false);
	}

	public <T> boolean match(T input, PathAdapter<T> adapter, boolean partial) {
		this.debug("match %s %s", input, this.pattern);
		
		// short-circuit in the case of busted things.
		// comments, etc.
		if (this.comment)
			return false;
		
		List<String> file = adapter.toArray(input, this.options);
		
		if (this.empty)
			return file.isEmpty();
		
		this.debug("%s %s %s", this.pattern, "split", file);
		return match(file, partial);
	}

}
