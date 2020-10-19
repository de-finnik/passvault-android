package de.finnik.passvault.drive;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.finnik.passvault.pass.Password;
import de.finnik.passvault.utils.Var;

public class DriveServiceHelper {

    private static final String TAG = "DriveServiceHelper";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    public Task<Boolean> fileExists() {
        return Tasks.call(mExecutor, () -> mDriveService.files().list().setQ("name = 'pass'").setSpaces("appDataFolder").execute().getFiles().size() > 0);
    }

    public Task<String> deleteFile() {
        return Tasks.call(mExecutor, () -> {
            mDriveService.files().delete(getPassFileId()).execute();
            return "";
        });

    }

    public Task<Password[]> readPasswords(String pass) {
        return Tasks.call(mExecutor, () -> {
            InputStream is = mDriveService.files().get(getPassFileId()).executeMediaAsInputStream();
            return Password.readPasswords(is, pass).toArray(new Password[0]);
        });
    }

    public Task<String> savePasswords(Context context, List<Password> passwords, String pass) {
        return Tasks.call(mExecutor, () -> {
            java.io.File tempFile = new java.io.File(context.getCacheDir(), "pass.vault");
            if (tempFile.delete()) {
                tempFile.createNewFile();
            }
            Password.savePasswords(passwords, new FileOutputStream(tempFile), pass);

            File result;
            try  {
                File old = mDriveService.files().get(getPassFileId()).execute();
                result = new File();
                result.setName(old.getName());
                result.setMimeType("text/plain");
                mDriveService.files().update(old.getId(), result, new FileContent("text/plain", tempFile)).execute();
            } catch (IndexOutOfBoundsException e){
                File old = new File();
                old.setParents(Collections.singletonList("appDataFolder"));
                old.setName(Var.PASS_FILE);

                result = mDriveService.files()
                        .create(old, new FileContent("text/plain", tempFile))
                        .setFields("id")
                        .execute();
            }

            return result.getId();
        });
    }

    private String getPassFileId() throws IOException {
        return mDriveService.files().list().setQ("name = 'pass'").setSpaces("appDataFolder").execute().getFiles().get(0).getId();
    }
}
