package de.finnik.passvault.utils;

import android.content.Context;
import android.content.Intent;

import java.io.FileNotFoundException;

import de.finnik.passvault.gui.MainPassActivity;
import de.finnik.passvault.gui.PassActivity;
import de.finnik.passvault.pass.Password;

public class FileUtils {
    public static void savePasswords(Context context) {
        if (PassActivity.password.isEmpty()) {
            Intent intent = new Intent(context, MainPassActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            return;
        }
        try {
            Password.savePasswords(PassActivity.passwordList, context.openFileOutput(Var.PASS_FILE, Context.MODE_PRIVATE), PassActivity.password);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
