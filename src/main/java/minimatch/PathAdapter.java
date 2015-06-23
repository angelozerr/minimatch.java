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
