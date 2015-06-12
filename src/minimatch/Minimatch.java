package minimatch;

import static minimatch.StringUtils.matches;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
	private static final String slashSplit = "/+";

	private String pattern;
	private final Options options;
	private boolean comment;
	private boolean empty;
	private boolean negate;
	private String[] globSet;

	public Minimatch(String pattern, Options options) {
		this.pattern = pattern.trim();
		this.options = getOptions(options);

		// windows support: need to use /, not \
		// if (path.sep !== '/') {
		// pattern = pattern.split(path.sep).join('/')
		// }

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
		String[] set = this.globSet = this.braceExpand(pattern, options);

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

		// this.debug(this.pattern, set)

		// glob --> regexps
		List<ParseResult> result = globToRegExps(globParts);
		System.err.println(result);

	}

	private String[][] globParts(String[] set) {
		String[][] parts = new String[set.length][];
		for (int i = 0; i < set.length; i++) {
			parts[i] = set[i].split(slashSplit);
		}
		return parts;
	}

	private List<ParseResult> globToRegExps(String[][] globParts) {
		String[] part = null;
		List<ParseResult> parts = new ArrayList<ParseResult>();
		for (int i = 0; i < globParts.length; i++) {
			part = globParts[i];
			for (int j = 0; j < part.length; j++) {
				parts.add(parse(part[j], false));
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

		ParseContext context = new ParseContext();
		context.re = "";
		context.hasMagic = options.isNocase();

		boolean escaping = false;
		// ? => one single character
		Object[] patternListStack = null;
		Object plType;

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
				this.debug("%s\t%s %s %j", pattern, i, context.re, c);
			}
			// skip over any that are escaped.
			if (escaping && reSpecials.contains(c)) {
				context.re += '\\' + c;
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
				clearStateChar(context);
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
            		 this.debug("%s\t%s %s %j <-- stateChar", pattern, i, context.re, c);
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
                  context.re += c;
                  continue;
                }

                // if we already have a stateChar, then it means
                // that there was something like ** or +? in there.
                // Handle the stateChar, then proceed with this one.
                if (options.isDebug()) {
                this.debug("call clearStateChar %j", context.stateChar);
                }
                clearStateChar(context);
                context.stateChar = c;
                // if extglob is disabled, then +(asdf|foo) isn't a thing.
                // just clear the statechar *now*, rather than even diving
                // into
                // the patternList stuff.
                if (options.isNoext()) {
                  clearStateChar(context);
                }
                continue;

              case '(':
                if (inClass) {
                  context.re += "(";
                  continue;
                }

                if (context.stateChar != null) {
                	context.re += "\\(";
                  continue;
                }

                plType = context.stateChar;
                patternListStack.push({
                  type : plType,
                  start : i - 1,
                  reStart : re.length
                });
                // negation is (?:(?!js)[^/]*)
                context.re += context.stateChar == '!' ? "(?:(?!" : "(?:";
                if (options.isDebug()) {
                this.debug("plType %j %j", context.stateChar, context.re);
                }
                context.stateChar = null; //false;
                continue;

              case ')':
                if (inClass || !patternListStack.length) {
                	context.re += "\\)";
                  continue;
                }

                clearStateChar(context);
                context.hasMagic = true;
                context.re += ")";
                plType = patternListStack.pop().type;
                // negation is (?:(?!js)[^/]*)
                // The others are (?:<pattern>)<type>
                switch (plType) {
                case '!':
                  re += '[^/]*?)'
                  break
                case '?':
                case '+':
                case '*':
                  re += plType
                  break
                case '@':
                  break // the default anyway
                }
                continue

              case '|':
                if (inClass || !patternListStack.length || escaping) {
                  re += '\\|'
                  escaping = false
                  continue
                }

                clearStateChar()
                re += '|'
                continue

                // these are mostly the same in regexp and glob
              case '[':
                // swallow any state-tracking char before the [
                clearStateChar()

                if (inClass) {
                  re += '\\' + c
                  continue
                }

                inClass = true
                classStart = i
                reClassStart = re.length
                re += c
                continue

              case ']':
                // a right bracket shall lose its special
                // meaning and represent itself in
                // a bracket expression if it occurs
                // first in the list. -- POSIX.2 2.8.3.2
                if (i == classStart + 1 || !inClass) {
                  re += "\\" + c;
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
                    //RegExp('[' + cs + ']');
                	  Pattern.compile("[" + cs + "]");
                  } catch (Throwable e) {
                    // not a valid class!
                    var sp = this.parse(cs, SUBPARSE);
                    re = re.substr(0, reClassStart) + '\\[' + sp[0]
                        + '\\]'
                    hasMagic = hasMagic || sp[1];
                    inClass = false;
                    continue;
                  }
                }

                // finish up the class.
                hasMagic = true;
                inClass = false;
context.re += c;
                continue;

              default:
                // swallow any state char that wasn't consumed
                clearStateChar(context);

                if (escaping) {
                  // no need
                  escaping = false;
                } else if (reSpecials[c] && !(c == '^' && inClass)) {
                  re += "\\";
                }

                re += c;

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
			// String sp = this.parse(cs, true);
			// re = re.substring(0, reClassStart) + "\\[" + sp[0];
			// hasMagic = hasMagic || sp[1];
		}

		return null;
	}

	private void debug(String pattern, Object... arguments) {
		this.options.getDebugger().debug(pattern, arguments);
	}

	private void clearStateChar(ParseContext context) {
		if (context.stateChar != null) {
			// we had some state-tracking character
			// that wasn't consumed by this pass.
			switch (context.stateChar) {
			case '*':
				context.re += star;
				context.hasMagic = true;
				break;
			case '?':
				context.re += qmark;
				context.hasMagic = true;
				break;
			default:
				context.re += "\\" + context.stateChar;
				break;
			}
			context.stateChar = null;
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

	// private Object braceExpand() {
	// return braceExpand(null, null);
	// }

	private String[] braceExpand(String pattern, Options options) {
		if (options.isNobrace() || !matches(hasBraces, pattern)) {
			// shortcut. no need to expand.
			return new String[] { pattern };
		}
		return expand(pattern);
	}

	private String[] expand(String pattern2) {
		// TODO Auto-generated method stub
		return null;
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
		return false;
	}

	private static Options getOptions(Options options) {
		return options == null ? Options.DEFAULT : options;
	}
}
