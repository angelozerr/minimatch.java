package minimatch;

public class Options {

	public static final Options DEFAULT = new Options();

	private boolean nocomment;
	private boolean nonegate;
	private boolean nobrace;
	private boolean noglobstar;
	private boolean nocase;
	private boolean dot;
	private boolean noext;

	private Debugger debugger;

	public boolean isNocomment() {
		return nocomment;
	}

	public void setNocomment(boolean nocomment) {
		this.nocomment = nocomment;
	}

	public boolean isNonegate() {
		return nonegate;
	}

	public void setNonegate(boolean nonegate) {
		this.nonegate = nonegate;
	}

	public boolean isNobrace() {
		return nobrace;
	}

	public void setNobrace(boolean nobrace) {
		this.nobrace = nobrace;
	}

	public boolean isNoglobstar() {
		return noglobstar;
	}

	public void setNoglobstar(boolean noglobstar) {
		this.noglobstar = noglobstar;
	}

	public boolean isNocase() {
		return nocase;
	}

	public void setNocase(boolean nocase) {
		this.nocase = nocase;
	}

	public boolean isDot() {
		return dot;
	}

	public void setDot(boolean dot) {
		this.dot = dot;
	}

	public boolean isNoext() {
		return noext;
	}

	public void setNoext(boolean noext) {
		this.noext = noext;
	}

	public boolean isDebug() {
		return debugger != null;
	}

	public Debugger getDebugger() {
		return debugger;
	}

	public void setDebugger(Debugger debugger) {
		this.debugger = debugger;
	}

}
