package minimatch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MagicItem extends ParseItem {

	private final Pattern pattern;

	public MagicItem(String source, Options options) {
		super(source);
		this.pattern = Pattern.compile(source);
	}

	@Override
	public boolean match(String input, Options options) {
		Matcher m = pattern.matcher(input);
		return m.matches();
	}

}
