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
