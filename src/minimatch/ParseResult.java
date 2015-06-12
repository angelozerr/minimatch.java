package minimatch;

public class ParseResult {

	private final ParseItem item;
	private final boolean b;

	public ParseResult(ParseItem item, boolean b) {
		this.item = item;
		this.b = b;
	}

	public ParseItem getItem() {
		return item;
	}

	public boolean isB() {
		return b;
	}
}
