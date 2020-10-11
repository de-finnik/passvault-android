package de.finnik.passvault.AES;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class AESWriter extends BufferedWriter {
    private AES aes;

    public AESWriter(Writer out, AES aes) {
        super(out);
        this.aes = aes;
    }

    @Override
    public void write(String str) throws IOException {
        super.write(aes.encrypt(convertToUnicode(str)));
    }

    /**
     * Converts a string to a string containing the unicode values for each character separated by ' '
     *
     * @param in The string to convert
     * @return The converted string containing unicode values separated by ' '
     */
    private String convertToUnicode(String in) {
        char[] c = in.toCharArray();
        StringBuilder out = new StringBuilder();
        for (char value : c) {
            out.append(Integer.toHexString(value | 0x10000).substring(1));
            out.append(" ");
        }
        return out.toString();
    }
}
