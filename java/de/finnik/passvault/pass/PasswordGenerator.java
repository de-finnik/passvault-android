package de.finnik.passvault.pass;

import java.util.*;
import java.util.stream.*;

/**
 * Generates a password out of a given length and given characters
 */
public class PasswordGenerator {
    /**
     * Generates a password by assembling chars at random indexes of a given {@link String} until the password length equals to the given length.
     *
     * @param chars  The chars to use for the random password assembled to a string
     * @param length The length of the output password
     * @return The generated password
     */
    public static String generatePassword(int length, PassChars... chars) {
        List<Character> password = new ArrayList<>();
        String all = Arrays.stream(chars).map(PassChars::get).collect(Collectors.joining());
        for (int i = 0; i < length; i++) {
            password.add(randomChar(all));
        }
        List<PassChars> nonMatching;
        while (!(nonMatching = Arrays.stream(chars).filter(c -> !c.contains(password.stream().map(String::valueOf).collect(Collectors.joining()))).collect(Collectors.toList())).isEmpty()) {
            for (PassChars passChars : nonMatching) {
                password.set(0, randomChar(passChars.get()));
                Collections.shuffle(password);
            }
        }
        return password.stream().map(String::valueOf).collect(Collectors.joining());
    }

    private static char randomChar(String chars) {
        return chars.charAt((int) (Math.random() * chars.length()));
    }

    /**
     * All types of characters a generated password may contain
     */
    public enum PassChars {
        BIG_LETTERS, SMALL_LETTERS, NUMBERS, SPECIAL_CHARACTERS;

        /**
         * Returns all characters in the ASCII code between two indices
         *
         * @param first  First index
         * @param second Second index
         * @return The characters between first and second assembled to a string
         */
        private static String GET_LETTERS_BETWEEN(int first, int second) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = first; i <= second; i++) {
                stringBuilder.append((char) i);
            }
            return stringBuilder.toString();
        }

        /**
         * Returns all chars matching to a type assembled to a string
         *
         * @return All valid chars for {@code this} type
         */
        public String get() {
            switch (this) {
                case BIG_LETTERS:
                    return GET_LETTERS_BETWEEN(65, 90);
                case SMALL_LETTERS:
                    return GET_LETTERS_BETWEEN(97, 122);
                case NUMBERS:
                    return GET_LETTERS_BETWEEN(48, 57);
                case SPECIAL_CHARACTERS:
                    return GET_LETTERS_BETWEEN(33, 47) + GET_LETTERS_BETWEEN(58, 64) + GET_LETTERS_BETWEEN(91, 96) + GET_LETTERS_BETWEEN(123, 126);
            }
            return "";
        }

        public PassProperty getMatchingProp() {
            switch (this) {
                case BIG_LETTERS:
                    return PassProperty.GEN_BIG;
                case SMALL_LETTERS:
                    return PassProperty.GEN_SMALL;
                case NUMBERS:
                    return PassProperty.GEN_NUM;
                case SPECIAL_CHARACTERS:
                    return PassProperty.GEN_SPE;
            }
            return null;
        }

        public static PassChars getMatchingChar(PassProperty passProperty) {
            switch (passProperty) {
                case GEN_BIG:
                    return BIG_LETTERS;
                case GEN_SMALL:
                    return SMALL_LETTERS;
                case GEN_NUM:
                    return NUMBERS;
                case GEN_SPE:
                    return SPECIAL_CHARACTERS;
            }
            return null;
        }

        public boolean contains(String s) {
            return Arrays.stream(get().split("")).anyMatch(s::contains);
        }
    }
}
