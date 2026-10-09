package com.ncpl.sales.cashflow.service;

import java.util.regex.Pattern;

/** Identifies explicitly named retention ledgers; does not estimate retention percentages. */
public final class RetentionLedgerClassifier {
    private static final Pattern NAME = Pattern.compile("(?:^|[^a-z])(retention|retainage)(?:$|[^a-z])", Pattern.CASE_INSENSITIVE);
    private RetentionLedgerClassifier() {}
    public static boolean isRetention(String ledgerName) {
        return ledgerName != null && NAME.matcher(ledgerName).find();
    }
}
