/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Genuitec LLC
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
package minimatch.isaacs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import minimatch.AbstractMinimatchTest;
import minimatch.Minimatch;
import minimatch.Options;
import minimatch.SysErrDebugger;
import minimatch.TestCase;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Translation of https://github.com/isaacs/minimatch/blob/master/test/patterns.js
 * 
 * @author Piotr Tomiak <piotr@genuitec.com>
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class Patterns extends AbstractMinimatchTest {

	protected static List<String> files = new ArrayList<String>();

	public static List<String> lst(String... items) {
		return Arrays.asList(items);
	}
	
	/* Tests */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> patterns() {
		files.clear();
		files.addAll(Arrays.asList(new String[] { "a", "b",
				"c", "d", "abc", "abd", "abe", "bb", "bcd", "ca", "cb", "dd",
				"de", "bdir/", "bdir/cfile" }));
		return Arrays.asList(new Object[][] {
				/* http://www.bashcookbook.com/bashinfo/source/bash-1.14.7/tests/glob-test */
				//expansion is not yet supported, so some tests are not present
				{new Test("a*", lst("a", "abc", "abd", "abe"))},
				
				//escaped star should not match
				{new Test("\\*", lst())}, 
				{new Test("\\**", lst())},
				{new Test("\\*\\*", lst())},
				
				{new Test("b*/", lst("bdir/"))},
				{new Test("c*",  lst("c", "ca", "cb"))},
				{new Test("**",  files)},
				
				/* legendary larry crashes bashes - doesn't match anything but should not blow up */
				{new Test("/^root:/{s/^[^:]*:[^:]*:([^:]*).*$/\\1/", lst())},
				{new Test("/^root:/{s/^[^:]*:[^:]*:([^:]*).*$/\u0001/", lst())},
				
				/* Character classes */
				{new Test("[a-c]b*", 	lst("abc", "abd", "abe", "bb", "cb"))},
				{new Test("[a-y]*[^c]", lst("abd", "abe", "bb", "bcd", "bdir/", "ca", "cb", "dd", "de"))},
				{new AddFiles("a-b", "aXb")},
				{new Test("a[X-]b", 	lst("a-b", "aXb"))},
				  
				{new AddFiles(".x", ".y")},
				{new Test("[^a-c]*", 	lst("d", "dd", "de"))},
				{new AddFiles("a*b/", "a*b/ooo")},
				{new Test("a\\*b/*", lst("a*b/ooo"))},
				{new Test("a\\*?/*", lst("a*b/ooo"))},
				{new Test("*\\\\!*", lst(), lst("echo !7"))},
				{new Test("*\\!*", lst("echo !7"), lst("echo !7"))},
				{new Test("*.\\*", lst("r.*"), lst("r.*"))},
				{new Test("a[b]c", lst("abc"))},
				//{new Test("a[\\b]c", lst("abc"))}, => Java RexExp does not tolerate "\b"
				{new Test("a?c", lst("abc"))},
				{new Test("a\\*c", lst(), lst("abc"))},
				{new Test("", lst(""), lst(""))},
				
				/* http://www.opensource.apple.com/source/bash/bash-23/bash/tests/glob-test */
				{new AddFiles("man/", "man/man1/", "man/man1/bash.1") },
				{new Test("*/man*/bash.*", lst("man/man1/bash.1"))},
				{new Test("man/man1/bash.1", lst("man/man1/bash.1"))},
				{new Test("a***c", lst("abc"), lst("abc"))},
				{new Test("a*****?c", lst("abc"), lst("abc"))},
				{new Test("?*****??", lst("abc"), lst("abc"))},
				{new Test("*****??", lst("abc"), lst("abc"))},
				{new Test("?*****?c", lst("abc"), lst("abc"))},
				{new Test("?***?****c", lst("abc"), lst("abc"))},
				{new Test("?***?****?", lst("abc"), lst("abc"))},
				{new Test("?***?****", lst("abc"), lst("abc"))},
				{new Test("*******c", lst("abc"), lst("abc"))},
				{new Test("*******?", lst("abc"), lst("abc"))},
				{new Test("a*cd**?**??k", lst("abcdecdhjk"), lst("abcdecdhjk"))},
				{new Test("a**?**cd**?**??k", lst("abcdecdhjk"), lst("abcdecdhjk"))},
				{new Test("a**?**cd**?**??k***", lst("abcdecdhjk"), lst("abcdecdhjk"))},
				{new Test("a**?**cd**?**??***k", lst("abcdecdhjk"), lst("abcdecdhjk"))},
				{new Test("a**?**cd**?**??***k**", lst("abcdecdhjk"), lst("abcdecdhjk"))},
				{new Test("a****c**?**??*****", lst("abcdecdhjk"), lst("abcdecdhjk"))},
				{new Test("[-abc]", lst("-"), lst("-"))},
				{new Test("[abc-]", lst("-"), lst("-"))},
				{new Test("\\", lst("\\"), lst("\\"))},
				{new Test("[\\\\]", lst("\\"), lst("\\"))},
				// RegExp "[[]" is not allowed in Java
				// {new Test("[[]", lst("["), lst("["))},
				// Cover this case with 2 special tests
				{new Test("[[]", lst("[[]"), lst("[[]"))},
				{new Test("[\\[]", lst("["), lst("["))},
				{new Test("[", lst("["), lst("["))},
				{new Test("[*", lst("[abc"), lst("[abc"))},	
				
				/* a right bracket shall lose its special meaning and
				   represent itself in a bracket expression if it occurs
				   first in the list.  -- POSIX.2 2.8.3.2 */
				{new Test("[]]", lst("]"), lst("]"))},
				{new Test("[]-]", lst("]"), lst("]"))},
				{new Test("[a-z]", lst("p"), lst("p"))},
				{new Test("??**********?****?", lst(), lst("abc"))},
				{new Test("??**********?****c", lst(), lst("abc"))},
				{new Test("?************c****?****", lst(), lst("abc"))},
				{new Test("*c*?**", lst(), lst("abc"))},
				{new Test("a*****c*?**", lst(), lst("abc"))},
				{new Test("a********???*******", lst(), lst("abc"))},
				{new Test("[]", lst(), lst("a"))},
				{new Test("[abc", lst(), lst("["))},
			
				/* nocase tests */
				{new Test("XYZ", lst("xYz"), lst("xYz", "ABC", "IjK"), new Options().setNocase(true))},
				{new Test("ab*", lst("ABC"), lst("xYz", "ABC", "IjK"), new Options().setNocase(true))},
				{new Test("[ia]?[ck]", lst("ABC", "IjK"), lst("xYz", "ABC", "IjK"), new Options().setNocase(true))},

				// [ pattern, [matches], MM opts, files, TAP opts]
				/* onestar/twostar */
				{new Test("{/*,*}", lst(), lst("/asdf/asdf/asdf"))},
				/* XXX - implement braces expansion
				{new Test("{/?,*}", lst("/a", "bb"), lst("/a", "/b/b", "/a/b/c", "bb"))},
				*/

				// dots should not match unless requested",
				{new Test("**", lst("a/b"), lst("a/b", "a/.d", ".a/.d"))},

				// .. and . can only match patterns starting with .,
				// even when options.dot is set.
				{new SetFiles("a/./b", "a/../b", "a/c/b", "a/.d/b")},
				{new Test("a/*/b", lst("a/c/b", "a/.d/b"), new Options().setDot(true))},
				{new Test("a/.*/b", lst("a/./b", "a/../b", "a/.d/b"), new Options().setDot(true))},
				{new Test("a/*/b", lst("a/c/b"), new Options().setDot(false))},
				{new Test("a/.*/b", lst("a/./b", "a/../b", "a/.d/b"), new Options().setDot(true))},

				// this also tests that changing the options needs
				// to change the cache key, even if the pattern is
				// the same!
				{new Test("**", lst("a/b", "a/.d", ".a/.d"), lst(".a/.d", "a/.d", "a/b"), new Options().setDot(true))},

				/* paren sets cannot contain slashes */
				{new Test("*(a/b)", lst(), lst("a/b"))},

				// brace sets trump all else.
				//
				// invalid glob pattern.  fails on bash4 and bsdglob.
				// however, in this implementation, it"s easier just
				// to do the intuitive thing, and let brace-expansion
				// actually come before parsing any extglob patterns,
				// like the documentation seems to say.
				//
				// XXX: if anyone complains about this, either fix it
				// or tell them to grow up and stop complaining.
				//
				// bash/bsdglob says this:
				// {new Test("*(a|{b),c)}", lst("*(a|{b),c)}"), lst("a", "ab", "ac", "ad"]]
				// but we do this instead:
				/* XXX - implement brace expansion
				{new Test("*(a|{b),c)}", lst("a", "ab", "ac"), lst("a", "ab", "ac", "ad"))},
				*/

				// test partial parsing in the presence of comment/negation chars
				{new Test("[!a*", lst("[!ab"), lst("[!ab", "[ab"))},
				{new Test("[#a*", lst("[#ab"), lst("[#ab", "[ab"))},

				// like: {a,b|c\\,d\\\|e} except it"s unclosed, so it has to be escaped.
				{new Test("+(a|*\\|c\\\\|d\\\\\\|e\\\\\\\\|f\\\\\\\\\\|g",
						lst("+(a|b\\|c\\\\|d\\\\|e\\\\\\\\|f\\\\\\\\|g"),
						lst("+(a|b\\|c\\\\|d\\\\|e\\\\\\\\|f\\\\\\\\|g", "a", "b\\c"))},

				// crazy nested {,,} and *(||) tests.
				/* XXX - implement brace expansion
				{new SetFiles(
						"a", "b", "c", "d", "ab", "ac", "ad", "bc", "cb", "bc,d",
						"c,db", "c,d", "d)", "(b|c", "*(b|c", "b|c", "b|cc", "cb|c",
						"x(a|b|c)", "x(a|c)", "(a|b|c)", "(a|c)")},
				{new Test("*(a|{b,c})", lst("a", "b", "c", "ab", "ac"))},
				{new Test("{a,*(b|c,d)}", lst("a", "(b|c", "*(b|c", "d)"))},
				// a
				// *(b|c)
				// *(b|d)
				{new Test("{a,*(b|{c,d})}", lst("a", "b", "bc", "cb", "c", "d"))},
				{new Test("*(a|{b|c,c})", lst("a", "b", "c", "ab", "ac", "bc", "cb"))},
				*/
						
				// test various flag settings.
				/* XXX - implement brace expansion
				{new Test("*(a|{b|c,c})", lst("x(a|b|c)", "x(a|c)", "(a|b|c)", "(a|c)"), new Options().setNoext(true))},
				*/
				{new Test("a?b", lst("x/y/acb", "acb/"), lst("x/y/acb", "acb/", "acb/d/e", "x/y/acb/d"), new Options().setMatchBase(true))},
				{new Test("#*", lst("#a", "#b"), lst("#a", "#b", "c#d"), new Options().setNocomment(true))},

				// begin channelling Boole and deMorgan...
				// negation tests
				{new SetFiles("d", "e", "!ab", "!abc", "a!b", "\\!a")},

				// anything that is NOT a* matches.
				{new Test("!a*", lst("\\!a", "d", "e", "!ab", "!abc"))},

				// anything that IS !a* matches.
				{new Test("!a*", lst("!ab", "!abc"), new Options().setNonegate(true))},

				// anything that IS a* matches
				{new Test("!!a*", lst("a!b"))},

				// anything that is NOT !a* matches
				{new Test("!\\!a*", lst("a!b", "d", "e", "\\!a"))},

				// negation nestled within a pattern
				{new SetFiles(
						"foo.js",
						"foo.bar",
						"foo.js.js",
						"blar.js",
						"foo.",
						"boo.js.boo"
					)
				},
				// last one is tricky! * matches foo, . matches ., and "js.js" != "js"
				// copy bash 4.3 behavior on this.
				{new Test("*.!(js)", lst("foo.bar", "foo.", "boo.js.boo", "foo.js.js"))},

				/* https://github.com/isaacs/minimatch/issues/5 */
				{new SetFiles(
						"a/b/.x/c", "a/b/.x/c/d", "a/b/.x/c/d/e", "a/b/.x", "a/b/.x/",
						"a/.x/b", ".x", ".x/", ".x/a", ".x/a/b", "a/.x/b/.x/c", ".x/.x"
					)
				},
				{new Test("**/.x/**", lst(
				      ".x/", ".x/a", ".x/a/b", "a/.x/b", "a/b/.x/", "a/b/.x/c",
				      "a/b/.x/c/d", "a/b/.x/c/d/e"))},

				/* https://github.com/isaacs/minimatch/issues/59 */
				{new Test("[z-a]", lst())},
				{new Test("a/[2015-03-10T00:23:08.647Z]/z", lst())},
				{new Test("[a-0][a-\u0100]", lst())},
				  
		});
	}
	
	public Patterns(ITestCase testCase) {
		super(testCase);
	}
	
	private static class Test extends AbstractTestCase {

		private static int nextId = 1;
		
		protected final String pattern;
		protected final Set<String> expectedOutput;
		protected final Options options;
		protected final List<String> files;
		private final int id;

		public Test(String pattern, List<String> expectedOutput) {
			this(pattern, expectedOutput, Patterns.files, null);
		}
		
		public Test(String pattern, List<String> expectedOutput, List<String> files) {
			this(pattern, expectedOutput, files, null);
		}
		
		public Test(String pattern, List<String> expectedOutput, Options options) {
			this(pattern, expectedOutput, Patterns.files, options);
		}
		
		public Test(String pattern, List<String> expectedOutput, List<String> files, Options options) {
			this.pattern = pattern;
			this.expectedOutput = new HashSet<String>(expectedOutput);
			if (options == null) {
				options = new Options();
			}
			this.options = options.setDebugger(SysErrDebugger.INSTANCE);
			this.files = files;
			this.id = nextId++;
			testCase = new TestCase(toString());
		}
		
		@Override
		protected void internalRun() {
			Minimatch mm = new Minimatch(pattern, options);
			Set<String> matched = new HashSet<String>();
			for (String file: files) {
				if (mm.match(file)) {
					matched.add(file);
				}
			}
			Assert.assertEquals("Pattern matching failed", expectedOutput, matched);
		}
		
		@Override
		public String toString() {
			return MessageFormat.format("Pattern {0}{1}: {2}", 
					id, options != null ? options : "", pattern);
		}
		
	}
		
	private static class AddFiles implements ITestCase {
		
		private String[] fileNames;
		
		public AddFiles(String... fileNames) {
			this.fileNames = fileNames;
		}
		
		@Override
		public void run() {
			files.addAll(Arrays.asList(fileNames));
		}
		
		@Override
		public String toString() {
			return "Add files: " + Arrays.toString(fileNames);
		}
	}
	
	private static class SetFiles implements ITestCase {
		
		private String[] fileNames;
		
		public SetFiles(String... fileNames) {
			this.fileNames = fileNames;
		}
		
		@Override
		public void run() {
			files.clear();
			files.addAll(Arrays.asList(fileNames));
		}
		
		@Override
		public String toString() {
			return "Set files: " + Arrays.toString(fileNames);
		}
	}
	
}
