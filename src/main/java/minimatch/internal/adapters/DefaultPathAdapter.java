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
