/**
 * Copyright (c) 2015 Angelo ZERR
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package minimatch.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	private static final Pattern antiSlashPattern = Pattern.compile("\\\\");
	private static final String slash = "/";

	private static final Pattern unescape = Pattern.compile("\\\\(.)");

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean matches(Pattern p, CharSequence input) {
		Matcher m = p.matcher(input);
		return m.matches();
	}

	public static List<Character> asList(char[] chars) {
		List<Character> cList = new ArrayList<Character>();
		for (char c : chars) {
			cList.add(c);
		}
		return cList;
	}

	public static String replacePath(String s1) {
		// replace "\\" by "/"
		return replace(s1, slash, antiSlashPattern);
	}

	public static String replace(String s1, String s2, Pattern pattern) {
		return pattern.matcher(s1).replaceAll(s2);
	}

	// replace stuff like \* with *
	public static String globUnescape(String s) {
		return replaceAll(s, "$1", unescape);
	}

	public static String replaceAll(String s1, String s2, Pattern pattern) {
		return pattern.matcher(s1).replaceAll(s2);
	}

}
