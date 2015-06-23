package minimatch;

import org.junit.Assert;
import org.junit.Test;

/**
 * Same tests than
 * https://github.com/isaacs/minimatch/blob/master/test/extglob-ending
 * -with-state-char.js
 *
 */
public class ExtglobEndingWithStateChar {

	@Test
	public void extGlobNotOk() {
		boolean result = Minimatch.minimatch("ax", "a?(b*)");
		Assert.assertFalse(result);
	}

	@Test
	public void extGlobOk() {
		boolean result = Minimatch.minimatch("ax", "?(a*|b)");
		Assert.assertTrue(result);
	}
}
