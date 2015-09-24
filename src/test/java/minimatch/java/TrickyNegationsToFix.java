package minimatch.java;

import org.junit.Assert;
import org.junit.Test;

import minimatch.Minimatch;
import minimatch.Options;
import minimatch.SysErrDebugger;

public class TrickyNegationsToFix {

	@Test
	public void bar() {
		// Given
		Options options = new Options();
		options.setDebugger(SysErrDebugger.INSTANCE);
		// when
		boolean result = Minimatch.minimatch("bar.min.js", "!*.+(js|css)", options);
		// then
		Assert.assertFalse(result);
	}

}
