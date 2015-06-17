import minimatch.Minimatch;
import minimatch.Options;
import minimatch.SysErrDebugger;

import org.junit.Assert;
import org.junit.Test;

public class MinimatchTest {

//	public static void main(String[] args) {
//
//		Options options = new Options();
//		options.setDebugger(SysErrDebugger.INSTANCE);
//
//		boolean result = Minimatch.minimatch("bar.foo", "*.foo", options);
//		System.err.println(result);
//	}

	@Test
	public void test() {
		// Given
		Options options = new Options();
		options.setDebugger(SysErrDebugger.INSTANCE);
		// when
		boolean result = Minimatch.minimatch("bar.foo", "*.foo", options);
		// then
		Assert.assertTrue(result);
	}
}
