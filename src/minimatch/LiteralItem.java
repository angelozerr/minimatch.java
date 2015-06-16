package minimatch;


public class LiteralItem extends ParseItem {
	public LiteralItem(String source) {
		super(source);
	}

	@Override
	public boolean match(String input, Options options) {
		return options.isNocase() ? input.equalsIgnoreCase(getSource()) : input
				.equals(getSource());
	}
}
