package com.microlend.common;


public final class VerhoeffValidator {

    private VerhoeffValidator() {
    }

    // Multiplication table (d)
    private static final int[][] D = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
            {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
            {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
            {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
            {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
            {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
            {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
            {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
            {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}
    };

    // Permutation table (p)
    private static final int[][] P = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
            {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
            {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
            {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
            {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
            {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
            {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
            {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}
    };

    // Computes the Verhoeff check digit for an 11-digit base, returning the full 12-digit number.
    public static String appendCheckDigit(String base11) {
        if (base11 == null || !base11.matches("\\d{11}")) {
            throw new IllegalArgumentException("base must be 11 digits");
        }
        int c = 0;
        int[] digits = base11.chars().map(ch -> ch - '0').toArray();
        // check digit is generated treating position 0 as the (soon-to-be) rightmost check slot
        for (int i = 0; i < digits.length; i++) {
            int digit = digits[digits.length - 1 - i];
            c = D[c][P[(i + 1) % 8][digit]];
        }
        int checkDigit = INV[c];
        return base11 + checkDigit;
    }

    // Inverse table (inv)
    private static final int[] INV = {0, 4, 3, 2, 1, 5, 6, 7, 8, 9};

    public static boolean isValid(String number) {
        if (number == null || !number.matches("\\d{12}")) {
            return false;
        }
        int c = 0;
        int[] reversedDigits = number.chars().map(ch -> ch - '0').toArray();
        // process from rightmost
        for (int i = 0; i < reversedDigits.length; i++) {
            int digit = reversedDigits[reversedDigits.length - 1 - i];
            c = D[c][P[i % 8][digit]];
        }
        return c == 0;
    }
}
