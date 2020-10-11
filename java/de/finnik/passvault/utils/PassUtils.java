package de.finnik.passvault.utils;

import java.util.List;
import java.util.stream.Collectors;

import de.finnik.passvault.pass.Password;

public class PassUtils {
    public static List<Password> getMatchingPasswords(String key, List<Password> passwords) {
        return passwords.stream().filter(p->p.getValues().anyMatch(v->v.toLowerCase().contains(key.toLowerCase()))).collect(Collectors.toList());
    }

    public static void deletePassword(Password password) {
        password.setPass("");
        password.setSite("");
        password.setUser("");
        password.setOther("");
    }
}
