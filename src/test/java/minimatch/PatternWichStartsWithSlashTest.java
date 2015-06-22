package minimatch;

import minimatch.Minimatch;
import minimatch.Options;
import minimatch.SysErrDebugger;

import org.junit.Assert;
import org.junit.Test;

public class PatternWichStartsWithSlashTest {

	@Test
	public void match() {
		// Given
		Options options = new Options();
		options.setDebugger(SysErrDebugger.INSTANCE);
		// when
		boolean result = Minimatch.minimatch("js/test.js", "**/**.js", options);
		// then
		Assert.assertTrue(result);
	}

	@Test
	public void dontMatch() {
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
