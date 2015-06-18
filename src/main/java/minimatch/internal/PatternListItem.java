package minimatch.internal;

public class PatternListItem {

	public final char type;
	public final int start;
	public final int reStart;

	public PatternListItem(char type, int start, int reStart) {
		this.type = type;
		this.start = start;
		this.reStart = reStart;
	}
}
