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
package minimatch;

/**
 * Path adapter API.
 * 
 * @param <T>
 */
public interface PathAdapter<T> {

	/**
	 * Returns the length of the segment of the given path.
	 * 
	 * @param path
	 * @return the length of the segment of the given path.
	 */
	int getLength(T path);

	/**
	 * Returns the path name of the given index of the given paths.
	 * 
	 * @param path
	 * @param i
	 * @return the path name of the given index of the given paths.
	 */
	String getPathName(T path, int i);

	/**
	 * Create path with the given path name.
	 * 
	 * @param pathName
	 *            path name.
	 * @return path with the given path name.
	 */
	T createPath(String pathName);

	/**
	 * Returns a sub path of the given paths at the given index.
	 * 
	 * @param path
	 * @param i
	 * @return a sub path of the given paths at the given index.
	 */
	T subPath(T path, int i);

}
