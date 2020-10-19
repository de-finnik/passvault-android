package de.finnik.passvault.drive;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;
import java.util.Collections;

import de.finnik.passvault.AES.AES;
import de.finnik.passvault.R;
import de.finnik.passvault.gui.PassActivity;
import de.finnik.passvault.gui.ui.main.ManageFragment;
import de.finnik.passvault.pass.PassProperty;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.pass.PasswordGenerator;
import de.finnik.passvault.utils.FileUtils;
import de.finnik.passvault.utils.GUIUtils;

public class DriveLocalHelper {
    private static final String TAG = "DriveLocalHelper";
    private static DriveServiceHelper mDriveServiceHelper;

    public static void synchronize(Context context, GoogleSignInAccount googleSignInAccount) {
        Log.i(TAG, "synchronize: ");
        if (mDriveServiceHelper == null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA));
            credential.setSelectedAccount(googleSignInAccount.getAccount());

            Drive drive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                    .setApplicationName(context.getString(R.string.app_name))
                    .build();

            mDriveServiceHelper = new DriveServiceHelper(drive);
        }

        mDriveServiceHelper.fileExists().addOnSuccessListener(fileExists -> {
            if (fileExists) {
                readPassFile(context, googleSignInAccount);
            } else {
                String drivePass = generateDrivePass();
                compareAndStore(context, drivePass, new Password[0]);
                GUIUtils.messageDialog(context, context.getString(R.string.created_drive_pass, drivePass));
            }
        });
    }

    private static String generateDrivePass() {
        return PasswordGenerator.generatePassword(12, PasswordGenerator.PassChars.BIG_LETTERS, PasswordGenerator.PassChars.SMALL_LETTERS, PasswordGenerator.PassChars.NUMBERS);
    }

    private static void readPassFile(Context context, GoogleSignInAccount googleSignInAccount) {
        if (PassProperty.DRIVE_PASSWORD.getValue().isEmpty()) {
            GUIUtils.inputDialog(context, context.getString(R.string.enter_drive_pass), drivePass -> mDriveServiceHelper.readPasswords(drivePass)
                    .addOnSuccessListener(drivePasswords -> compareAndStore(context, drivePass, drivePasswords))
                    .addOnFailureListener(e -> {
                if (e.getClass() == AES.WrongPasswordException.class) {
                    GUIUtils.confirmDialog(context, context.getString(R.string.wrong_drive_pass), b -> {
                        if (b) {
                            String confirm = context.getString(R.string.confirm_deleting_drive);
                            GUIUtils.inputDialog(context, confirm, s -> {
                                if (s.equals(confirm.split("'")[1])) {
                                    mDriveServiceHelper.deleteFile().addOnSuccessListener(nul -> synchronize(context, googleSignInAccount));
                                }
                            });
                        }
                    });
                }
            }));
        } else {
            String drivePass = PassActivity.getAES().decrypt(PassProperty.DRIVE_PASSWORD.getValue());
            mDriveServiceHelper.readPasswords(drivePass).addOnSuccessListener(drivePasswords -> compareAndStore(context, drivePass, drivePasswords)).addOnFailureListener(e -> {
                if (e.getClass() == AES.WrongPasswordException.class) {
                    PassProperty.DRIVE_PASSWORD.setValue(context, "");
                    synchronize(context, googleSignInAccount);
                }
            });
        }
    }

    private static void compareAndStore(Context context, String drivePass, Password[] drivePasswords) {
        PassActivity.passwordList = CompareVaults.compare(Arrays.asList(drivePasswords), PassActivity.passwordList);
        FileUtils.savePasswords(context);
        saveOnDrive(context, drivePass);
    }

    private static void saveOnDrive(Context context, String drivePass) {
        ManageFragment.refreshPasswords();
        PassActivity.button_synchronize_drawable.addAnimationListener(loopNumber -> {
            if(loopNumber == 0) {
                PassActivity.button_synchronize_drawable.stop();
            }
        });
        mDriveServiceHelper.savePasswords(context, PassActivity.passwordList, drivePass).addOnSuccessListener(fileId -> PassProperty.DRIVE_PASSWORD.setValue(context, PassActivity.getAES().encrypt(drivePass))).addOnFailureListener(Throwable::printStackTrace);
    }
}
