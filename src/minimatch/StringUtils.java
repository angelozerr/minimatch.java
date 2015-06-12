package minimatch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

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

}
