package com.example.kairos;

public class FlagUtils {
    // "FR" -> 🇫🇷
    public static String countryCodeToFlag(String code) {
        if (code == null || code.length() != 2) return "🏳️";
        int first = Character.codePointAt(code.toUpperCase(), 0) - 0x41 + 0x1F1E6;
        int second = Character.codePointAt(code.toUpperCase(), 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }
}
