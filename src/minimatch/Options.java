package minimatch;

public class Options {

	public static final Options DEFAULT = new Options();

	private boolean allowWindowsPaths;
	private boolean nocomment;
	private boolean nonegate;
	private boolean nobrace;
	private boolean noglobstar;
	private boolean nocase;
	private boolean dot;
	private boolean noext;
	private boolean matchBase;
	private boolean flipNegate;

	private Debugger debugger;

	public boolean isAllowWindowsPaths() {
		return allowWindowsPaths;
	}

	public void setAllowWindowsPaths(boolean allowWindowsPaths) {
		this.allowWindowsPaths = allowWindowsPaths;
	}

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

	public boolean isMatchBase() {
		return matchBase;
	}

	public void setMatchBase(boolean matchBase) {
		this.matchBase = matchBase;
	}

	public boolean isFlipNegate() {
		return flipNegate;
	}

	public void setFlipNegate(boolean flipNegate) {
		this.flipNegate = flipNegate;
	}

	public Debugger getDebugger() {
		return debugger;
	}

	public void setDebugger(Debugger debugger) {
		this.debugger = debugger;
	}

}
