package de.finnik.passvault.drive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import de.finnik.passvault.pass.Password;

public class CompareVaults {
    @SafeVarargs
    public static List<Password> compare(List<Password>... passwords) {
        List<Password> allPasswords = new ArrayList<>();
        Arrays.stream(passwords).forEach(allPasswords::addAll);
        List<Password> compared = new ArrayList<>();
        List<String> IDs = allPasswords.stream().map(Password::id).distinct().collect(Collectors.toList());
        for (String id : IDs) {
            allPasswords.stream().filter(password -> password.id().equals(id)).max(Comparator.comparing(Password::lastModified)).ifPresent(compared::add);
        }
        return compared;
    }

    public static String[] changeLog(List<Password> pre, List<Password> post) {
        List<String> changeLog = new ArrayList<>();
        for (Password postPass : post) {
            if (pre.stream().map(Password::id).anyMatch(p -> p.equals(postPass.id()))) {
                // Exists
                if (!pre.contains(postPass)) {
                    // Changed
                    Password prePass = pre.stream().filter(p -> p.id().equals(postPass.id())).findFirst().orElse(null);
                    assert prePass != null;
                    // Deleted
                    if(postPass.isEmpty()) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Deleted password"));
                        continue;
                    }
                    if (!prePass.getPass().equals(postPass.getPass())) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Changed 'password' in password"));
                    }
                    if (!prePass.getSite().equals(postPass.getSite())) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Changed 'site' to '" + postPass.getSite() + "' in password"));
                    }
                    if (!prePass.getUser().equals(postPass.getUser())) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Changed 'user' to '" + postPass.getUser() + "' in password"));
                    }
                    if (!prePass.getOther().equals(postPass.getOther())) {
                        changeLog.add(Password.log(prePass, "%s -> %s: Changed 'other' to '" + postPass.getOther() + "' in password"));
                    }
                }
            } else {
                // Created
                changeLog.add(Password.log(postPass, "%s -> %s: Created password"));
            }
        }
        return changeLog.toArray(new String[0]);
    }
}
