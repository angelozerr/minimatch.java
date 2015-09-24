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
package minimatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to ease with locating tested pattern in the source code. It should be
 * instantiated in the test case constructor and set as cause of exception if 
 * test fails.
 * 
 * @author Piotr Tomiak <piotr@genuitec.com>
 */
public class TestCase extends Throwable {

	private static final long serialVersionUID = 1L;

	public TestCase(String message) {
		super(message);
		List<StackTraceElement> list = new ArrayList<StackTraceElement>();
		for (StackTraceElement el: getStackTrace()) {
			if (!el.getMethodName().equals("<init>")) { //$NON-NLS-1$
				list.add(el);
			}
		}
		setStackTrace(list.toArray(new StackTraceElement[0]));
	}
	
	public void setAsCauseOf(Throwable t) {
		while (t.getCause() != null) {
			t = t.getCause();
		}
		t.initCause(this);
	}
	
}
