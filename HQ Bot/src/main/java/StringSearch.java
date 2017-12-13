
public class StringSearch {
	public static int match(String text, String... patterns) {
		int matches = 0;
		for (int i = 0; i <= (text.length() - minLength(patterns)); i++) {
			for (String p : patterns) {
				if (text.substring(i, (i + p.length())).equalsIgnoreCase(p)) {
					matches++;
				}
			}
		}
		return matches;
	}

	public static int[] matchIndividual(String text, String... patterns) {
		int[] matches = new int[patterns.length];
		for (int i = 0; i <= (text.length() - minLength(patterns)); i++) {
			for (int x = 0; x < patterns[i].length(); x++) {
				if (text.substring(i, (i + patterns[i].length())).equalsIgnoreCase(patterns[i])) {
					matches[x]++;
				}
			}
		}
		return matches;
	}

	private static int minLength(String[] patterns) {
		int min = patterns[0].length();
		for (int x = 1; x < patterns.length; x++) {
			int l = patterns[x].length();
			if (l < min)
				min = l;
		}
		return min;
	}
}
