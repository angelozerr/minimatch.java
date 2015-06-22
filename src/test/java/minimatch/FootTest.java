package minimatch;

import minimatch.Minimatch;
import minimatch.Options;
import minimatch.SysErrDebugger;

import org.junit.Assert;
import org.junit.Test;

public class FootTest {

	@Test
	public void foo() {
		// Given
		Options options = new Options();
		options.setDebugger(SysErrDebugger.INSTANCE);
		// when
		boolean result = Minimatch.minimatch("bar.foo", "*.foo", options);
		// then
		Assert.assertTrue(result);
	}

}
