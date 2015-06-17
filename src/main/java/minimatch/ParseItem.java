package minimatch;

public abstract class ParseItem {

	public static final ParseItem Empty = new LiteralItem("");

	private final String source;

	public ParseItem(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public abstract boolean match(String f, Options options);

}
