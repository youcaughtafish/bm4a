package com.spacepocalypse.util;


public class StrUtl {
	public static boolean endsWith(String str, String suffix) {
        if (str == null || suffix == null) {
            return (str == null && suffix == null);
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return str.regionMatches(false, strOffset, suffix, 0, suffix.length());
	}
	
	public static boolean startsWith(String str, String prefix) {
		if (str == null || prefix == null) {
            return (str == null && prefix == null);
        }
        if (prefix.length() > str.length()) {
            return false;
        }
        return str.regionMatches(false, 0, prefix, 0, prefix.length());
	}
	
	public static String trunc(String str, int maxLen) {
		if (str == null) {
			return null;
		}
		
		if (maxLen <= 3 && str.length() > maxLen) {
			return str.substring(0, maxLen);
		}
		
		if (maxLen <= 3) {
			return str;
		}
		
		if (str.length() > maxLen - 3) {
			return Conca.t(str.substring(0, maxLen-3), "...");
		}
		
		return str;
	}
}
