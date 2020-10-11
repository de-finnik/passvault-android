package de.finnik.passvault.utils;

import android.content.Context;

import java.io.FileNotFoundException;

import de.finnik.passvault.R;
import de.finnik.passvault.gui.PassActivity;
import de.finnik.passvault.pass.Password;

public class FileUtils {
    public static void savePasswords(Context context) {
        if(PassActivity.password.isEmpty()) {
            GUIUtils.inputDialog(context, context.getString(R.string.enter_main_password), pass->{
                if(pass.isEmpty())
                    return;
                PassActivity.password = pass;
                savePasswords(context);
            });
            return;
        }
        try {

            Password.savePasswords(PassActivity.passwordList, context.openFileOutput(Var.PASS_FILE,Context.MODE_PRIVATE), PassActivity.password);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
