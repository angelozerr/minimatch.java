package minimatch;

import java.text.MessageFormat;

public class SysErrDebugger implements Debugger {

	public static final Debugger INSTANCE = new SysErrDebugger();

	private SysErrDebugger() {

	}
	
	@Override
	public void debug(String pattern, Object... arguments) {
		String message = MessageFormat.format(pattern, arguments);
		System.err.println(message);
	}
}
