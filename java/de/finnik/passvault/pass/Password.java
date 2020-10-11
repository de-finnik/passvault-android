package de.finnik.passvault.pass;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import de.finnik.passvault.AES.AES;
import de.finnik.passvault.AES.AESReader;
import de.finnik.passvault.AES.AESWriter;

/**
 * A password object has four parameters:
 * 1. The password
 * 2. The website to which the password belongs
 * 3. The email/username that is used among the password
 * 4. Other information that is useful to know among the password
 */
public class Password {
    private final String ID;
    private String pass, site, user, other;
    private long lastModified;

    public Password(String pass, String site, String user, String other) {
        this.pass = pass;
        this.site = site;
        this.user = user;
        this.other = other;
        lastModified = System.currentTimeMillis();
        ID = UUID.randomUUID().toString();
    }

    private Password() {
        pass = "";
        site = "";
        user = "";
        other = "";
        lastModified = System.currentTimeMillis();
        ID = UUID.randomUUID().toString();
    }

    public Password(Password password) {
        pass = password.pass;
        site = password.site;
        user = password.user;
        other = password.other;
        lastModified = password.lastModified;
        ID = password.ID;
    }

    /**
     * Encrypts all {@link Password} objects with a given password and saves them in a given file,
     *
     * @param passwords List of {@link Password} objects
     * @param outputStream The OutputStream to save the encrypted passwords to
     * @param pass      The password to encrypt
     */
    public static void savePasswords(List<Password> passwords, OutputStream outputStream, String pass) {
        try (AESWriter aesWriter = new AESWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), new AES(pass))) {
            aesWriter.write(new Gson().toJson(passwords.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This static method returns all passwords with all of their parameters that are saved to an encrypted file
     *
     * @param inputStream The encrypted inputStream
     * @param pass        The password to decrypt
     * @return The List of {@link Password} objects
     */
    public static List<Password> readPasswords(InputStream inputStream, String pass) throws AES.WrongPasswordException, IOException {
        try (AESReader aesReader = new AESReader(new InputStreamReader(inputStream), new AES(pass))) {
            return new ArrayList<>(Arrays.asList(new Gson().fromJson(aesReader.readLine(), Password[].class)));
        } catch (AES.WrongPasswordException e) {
            // Wrong password
            throw new AES.WrongPasswordException();
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Creates a string containing all attributes of a {@link Password} objects excluding the password attribute
     *
     * @param password The password to convert to a string
     * @return The converted string
     */
    public static String log(Password password, String message) {
        StringBuilder out = new StringBuilder();
        String separator = ", ";
        if (password.site.length() > 0) {
            out.append("site: ");
            out.append(password.site);
            out.append(separator);
        }
        if (password.user.length() > 0) {
            out.append("user: ");
            out.append(password.user);
            out.append(separator);
        }
        if (password.other.length() > 0) {
            out.append("other information: ");
            out.append(password.other);
            out.append(separator);
        }
        if (out.length() > 0) {
            return String.format("%s: %s!", message, out.substring(0, out.length() - separator.length()));
        } else {
            return message + " with no information!";
        }
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
        lastModified = System.currentTimeMillis();
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
        lastModified = System.currentTimeMillis();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
        lastModified = System.currentTimeMillis();
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
        lastModified = System.currentTimeMillis();
    }

    public String id() {
        return ID;
    }

    public long lastModified() {
        return lastModified;
    }

    /**
     * Returns a stream of all parameters
     *
     * @return A stream of all parameters
     */
    public Stream<String> getValues() {
        return Stream.of(pass, site, user, other);
    }

    /**
     * Checks if all parameters of the password are empty
     *
     * @return {@code true} if every parameter is empty (equal to "") or {@code false} if not
     */
    public boolean isEmpty() {
        return Stream.of(pass, site, user, other).filter(String::isEmpty).count() == 4;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == getClass()) {
            Password password = (Password) obj;
            return password.getPass().equals(getPass())
                    && password.getSite().equals(getSite())
                    && password.getUser().equals(getUser())
                    && password.getOther().equals(getOther())
                    && password.id().equals(id())
                    && password.lastModified() == lastModified();
        }
        return false;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pass, site, user, other);
    }

}
