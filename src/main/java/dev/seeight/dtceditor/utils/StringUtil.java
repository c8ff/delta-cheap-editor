package dev.seeight.dtceditor.utils;

public class StringUtil {
	public static String checkPath(String path) {
		if (path.charAt(path.length() - 1) == '/') {
			if (path.length() == 1) {
				return path;
			}

			return path.substring(0, path.length() - 1);
		}

		return path;
	}
}
