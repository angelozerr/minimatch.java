import minimatch.Minimatch;
import minimatch.Options;
import minimatch.SysErrDebugger;

public class MinimatchTest {

	public static void main(String[] args) {

		Options options = new Options();
		options.setDebugger(SysErrDebugger.INSTANCE);

		boolean result = Minimatch.minimatch("bar.foo", "*.foo", options);
		System.err.println(result);
	}
}
