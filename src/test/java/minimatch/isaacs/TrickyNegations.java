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
import java.util.Collection;
import java.util.List;

import minimatch.AbstractMinimatchTest;
import minimatch.Minimatch;
import minimatch.TestCase;
import minimatch.AbstractMinimatchTest.AbstractTestCase;
import minimatch.AbstractMinimatchTest.ITestCase;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Translation of https://github.com/isaacs/minimatch/blob/master/test/tricky-negations.js
 * 
 * @author Piotr Tomiak <piotr@genuitec.com>
 */
@RunWith(Parameterized.class)
@SuppressWarnings("nls")
public class TrickyNegations extends AbstractMinimatchTest {


	private static final List<Object[]> TESTS = new ArrayList<Object[]>();
	
	static {
		addTests("bar.min.js",
				new Case("*.!(js|css)", true),
				new Case("!*.+(js|css)", false),
				new Case("*.+(js|css)", true)
		);
		
		addTests("a-integration-test.js",
				new Case("*.!(j)", true),
				new Case("!(*-integration-test.js)", false),
				new Case("*-!(integration-)test.js", true),
				new Case("*-!(integration)-test.js", false),
				new Case("*!(-integration)-test.js", true),
				new Case("*!(-integration-)test.js", true),
				new Case("*!(integration)-test.js", true),
				new Case("*!(integration-test).js", true),
				new Case("*-!(integration-test).js", true),
				new Case("*-!(integration-test.js)", true),
				new Case("*-!(integra)tion-test.js", false),
				new Case("*-integr!(ation)-test.js", false),
				new Case("*-integr!(ation-t)est.js", false),
				new Case("*-i!(ntegration-)test.js", false),
				new Case("*i!(ntegration-)test.js", true),
				new Case("*te!(gration-te)st.js", true),
				new Case("*-!(integration)?test.js", false),
				new Case("*?!(integration)?test.js", true)
		);

		addTests("foo-integration-test.js",
				new Case("foo-integration-test.js", true),
				new Case("!(*-integration-test.js)", false)
		);

		addTests("foo.jszzz.js",
				new Case("*.!(js).js", true)
		);

		addTests("asd.jss",
				new Case("*.!(js)", true)
		);

		addTests("asd.jss.xyz",
				new Case("*.!(js).!(xy)", true)
		);

		addTests("asd.jss.xy",
				new Case("*.!(js).!(xy)", false)
		);

		addTests("asd.js.xyz",
				new Case("*.!(js).!(xy)", false)
		);

		addTests("asd.js.xy",
				new Case("*.!(js).!(xy)", false)
		);

		addTests("asd.sjs.zxy",
				new Case("*.!(js).!(xy)", true)
		);

		addTests("asd..xyz",
				new Case("*.!(js).!(xy)", true)
		);

		addTests("asd..xy",
				new Case("*.!(js).!(xy)", false),
				new Case("*.!(js|x).!(xy)", false)
		);

		addTests("foo.js.js",
				new Case("*.!(js)", true)
		);

		addTests("testjson.json",
				new Case("*(*.json|!(*.js))", true),
				new Case("+(*.json|!(*.js))", true),
				new Case("@(*.json|!(*.js))", true),
				new Case("?(*.json|!(*.js))", true)
		);

		addTests("foojs.js",
				new Case("*(*.json|!(*.js))", false), // XXX bash 4.3 disagrees!
				new Case("+(*.json|!(*.js))", false), // XXX bash 4.3 disagrees!
				new Case("@(*.json|!(*.js))", false),
				new Case("?(*.json|!(*.js))", false)
		);

		addTests("other.bar",
				new Case("*(*.json|!(*.js))", true),
				new Case("+(*.json|!(*.js))", true),
				new Case("@(*.json|!(*.js))", true),
				new Case("?(*.json|!(*.js))", true)
		);
		
		
	}

	
	private static void addTests(String file, Case... cases) {
		for (Case c: cases) {
			TESTS.add(new Object[] {new Test(file, c)});
		}
	}
	
	/* Tests */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> tests() {
		return TESTS;
	}
	
	public TrickyNegations(ITestCase testCase) {
		super(testCase);
	}

	private static class Test extends AbstractTestCase {

		private static int nextId = 1;
		
		private String file;
		private Case c;
		private int id;
		
		public Test(String file, Case c) {
			this.c = c;
			this.file = file;
			this.id = nextId++;
			this.testCase = c.testCase;
		}
		
		@Override
		public void internalRun() {
			Assert.assertEquals("Pattern matching failed", c.shouldPass, 
					new Minimatch(c.pattern).match(file));
		}
		
		@Override
		public String toString() {
			return MessageFormat.format("{0}. File: {1}, pattern: {2}", id, file, c.pattern);
		}
		
	}
	
	private static class Case {
		public final String pattern;
		public final boolean shouldPass;
		public final TestCase testCase;
		
		public Case(String pattern, boolean shouldPass) {
			this.pattern = pattern;
			this.shouldPass = shouldPass;
			this.testCase = new TestCase("Pattern: " +pattern);
		}
	}
	
}
