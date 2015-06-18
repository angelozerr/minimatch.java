package minimatch;

import minimatch.Minimatch;
import minimatch.Options;
import minimatch.SysErrDebugger;

import org.junit.Assert;
import org.junit.Test;

public class MinimatchTest {

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

	@Test
	public void testPatternWichMatch() {
		// Given
		Options options = new Options();
		options.setDebugger(SysErrDebugger.INSTANCE);
		// when
		boolean result = Minimatch.minimatch("js/test.js", "**/**.js", options);
		// then
		Assert.assertTrue(result);
	}

	@Test
	public void testPatternWichDoesntMatch() {
		// Given
		Options options = new Options();
		options.setDebugger(SysErrDebugger.INSTANCE);
		// when
		boolean result = Minimatch
				.minimatch("js/test.jsa", "**/**.js", options);
		// then
		Assert.assertFalse(result);
	}
}
