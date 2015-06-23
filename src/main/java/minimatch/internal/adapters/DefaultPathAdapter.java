/**
 * Copyright (c) 2015 Angelo ZERR
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package minimatch.internal.adapters;

import java.util.ArrayList;
import java.util.List;

import minimatch.PathAdapter;

/**
 * Default {@link PathAdapter} implementation with String.
 *
 */
public class DefaultPathAdapter implements PathAdapter<List<String>> {

	public static final PathAdapter<List<String>> INSTANCE = new DefaultPathAdapter();

	@Override
	public int getLength(List<String> file) {
		return file.size();
	}

	@Override
	public String getPathName(List<String> file, int i) {
		return file.get(i);
	}

	@Override
	public List<String> createPath(String filename) {
		List<String> file = new ArrayList<String>();
		file.add(filename);
		return file;
	}

	@Override
	public List<String> subPath(List<String> file, int fr) {
		return file.subList(fr, file.size());
	}
}
