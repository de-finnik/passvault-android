package de.finnik.passvault.AES;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class AESReader extends BufferedReader {
    private AES aes;

    public AESReader(Reader in, AES aes) {
        super(in);
        this.aes = aes;
    }


    @Override
    public String readLine() throws IOException, AES.WrongPasswordException {
        return convertFromUnicode(aes.decrypt(super.readLine()));
    }

    /**
     * Converts a string containing unicode values separated by ' ' into a string
     *
     * @param in The string containing unicode
     * @return The converted string
     */
    private String convertFromUnicode(String in) {
        String[] c = in.split(" ");
        StringBuilder out = new StringBuilder();
        for (String s : c) {
            out.append((char) Integer.parseInt(s, 16));
        }
        return out.toString();
    }
}
