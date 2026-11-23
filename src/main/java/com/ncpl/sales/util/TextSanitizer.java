package com.ncpl.sales.util;

/**
 * Cleans user-entered text before it is saved.
 *
 * Tabs, line breaks and other control characters are often pasted in from
 * spreadsheets. They break JSON output and make values hard to search, so they
 * are removed or replaced before the entity is persisted.
 */
public final class TextSanitizer {

    private TextSanitizer() {
    }

    /**
     * For free text (names, addresses, remarks): each run of control characters
     * becomes a single space, and the ends are trimmed.
     * Returns null when the input is null.
     */
    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\p{Cntrl}+", " ").trim();
    }

    /**
     * For codes and numbers (PIN, GST, PAN, phone): control characters are removed
     * entirely, and the ends are trimmed.
     * Returns null when the input is null.
     */
    public static String cleanCode(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\p{Cntrl}", "").trim();
    }
}
