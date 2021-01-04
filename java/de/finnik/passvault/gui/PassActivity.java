package de.finnik.passvault.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.tabs.TabLayout;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.finnik.passvault.AES.AES;
import de.finnik.passvault.R;
import de.finnik.passvault.drive.DriveLocalHelper;
import de.finnik.passvault.gui.onboarding.OnboardingActivity;
import de.finnik.passvault.gui.ui.main.ManageFragment;
import de.finnik.passvault.gui.ui.main.SectionsPagerAdapter;
import de.finnik.passvault.pass.PassProperty;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.utils.FileUtils;
import de.finnik.passvault.utils.GUIUtils;
import de.finnik.passvault.utils.Var;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class PassActivity extends AppCompatActivity implements LifecycleObserver {
    public static String password;
    public static List<Password> passwordList;

    public static GifDrawable button_synchronize_drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle intent extras
        assert getIntent().getExtras() != null : "Intent has no extras";
        password = getIntent().getStringExtra("pass");
        try {
            passwordList = Password.readPasswords(openFileInput(Var.PASS_FILE), password);
        } catch (Exception e) {
            passwordList = new ArrayList<>();
            if (getIntent().getExtras().containsKey("passwords")) {
                passwordList = Arrays.asList(new Gson().fromJson(getIntent().getStringExtra("passwords"), Password[].class));
            }
        }
        if (getIntent().getExtras().containsKey("drivePass")) {
            PassProperty.DRIVE_PASSWORD.setValue(this, getAES().encrypt(getIntent().getStringExtra("drivePass")));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        ImageView button_settings = findViewById(R.id.btn_settings);
        button_settings.setOnClickListener(v -> showSettings());

        GifImageView button_synchronize = findViewById(R.id.button_synchronize);
        button_synchronize_drawable = (GifDrawable) button_synchronize.getDrawable();
        button_synchronize_drawable.stop();
        button_synchronize_drawable.setSpeed(4.0f);
        button_synchronize.setOnClickListener(v -> {
            if (GoogleSignIn.getLastSignedInAccount(this) == null) {
                signIn();
            } else {
                synchronize(this);
            }
        });

        synchronize(this);

        // Don't permit screenshots
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    boolean pause = false;

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void pause() {
        pause = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void resumeAndGoToLogin() {
        if (!pause)
            return;
        Intent intent = new Intent(PassActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Signs in the user to their Google account
     *
     * @see PassActivity#startActivityForResult(Intent, int)
     */
    public void signIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .requestEmail()
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        startActivityForResult(client.getSignInIntent(), 400);
    }

    /**
     * Signs out the user from their Google account
     */
    public void signOut() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .requestEmail()
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        PassProperty.DRIVE_PASSWORD.setValue(this, "");
        client.signOut()
                .addOnCompleteListener(s -> Toast.makeText(this, getString(R.string.disconnected_drive), Toast.LENGTH_LONG).show())
                .addOnFailureListener(s -> Toast.makeText(this, getString(R.string.error_disconnecting_drive), Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 400) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(googleSignInAccount -> DriveLocalHelper.synchronize(this, googleSignInAccount))
                    .addOnFailureListener(e -> {
                        assert e.getMessage() != null;
                        if (!e.getMessage().startsWith("12501"))
                            GUIUtils.messageDialog(this, getString(R.string.error_while_synchronizing, Objects.requireNonNull(e.getMessage()).replaceAll("[^0-9]", "")));
                    });
        }
    }

    /**
     * Creates an {@link AES} object with the users {@link this#password}
     *
     * @return {@link AES} object containing {@link this#password}
     */
    public static AES getAES() {
        return new AES(password);
    }

    /**
     *
     *
     * @param activity the context to be passed
     */
    public static void synchronize(Activity activity) {
        GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastAccount != null) {
            DriveLocalHelper.synchronize(activity, lastAccount);
        }
        FileUtils.savePasswords(activity);
        ManageFragment.refreshPasswords();
    }

    /**
     *
     */
    private void showSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);

        Button btnChangeMasterPass = dialogView.findViewById(R.id.btn_change_master_pass);
        btnChangeMasterPass.setOnClickListener(v1 -> GUIUtils.inputDialog(this, getString(R.string.enter_main_password), in -> {
            if (in.toString().equals(password)) {
                Intent intent = new Intent(this, MainPassActivity.class);
                intent.putExtra("showOnboarding", false);
                intent.putExtra("passwords", new Gson().toJson(passwordList));
                if (!PassProperty.DRIVE_PASSWORD.getValue().isEmpty())
                    intent.putExtra("drivePass", getAES().decrypt(PassProperty.DRIVE_PASSWORD.getValue()));
                startActivity(intent);
            }
        }, true));

        Button btnHelp = dialogView.findViewById(R.id.btn_help);
        btnHelp.setOnClickListener(v1 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://finnik.de/passvault"));
            startActivity(browserIntent);
        });

        Button btnOnboarding = dialogView.findViewById(R.id.btn_onboarding);
        btnOnboarding.setOnClickListener(v1 -> {
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
        });

        Button btnShowDrive = dialogView.findViewById(R.id.btn_show_drive_pass);
        btnShowDrive.setOnClickListener(v1 -> GUIUtils.inputDialog(this, getString(R.string.enter_main_password), in -> {
            if (in.toString().equals(password)) {
                String drivePass = PassActivity.getAES().decrypt(PassProperty.DRIVE_PASSWORD.getValue());
                GUIUtils.messageDialog(this, getString(R.string.drive_password, drivePass));
            }
        }, true));
        if (!PassProperty.DRIVE_PASSWORD.getValue().isEmpty()) {
            btnShowDrive.setVisibility(View.VISIBLE);
        }

        Button btnDisconnectDrive = dialogView.findViewById(R.id.btn_disconnect_drive);
        btnDisconnectDrive.setOnClickListener(v1 -> GUIUtils.confirmDialog(this, getString(R.string.confirm_disconnecting_drive), b -> {
            if (b) {
                signOut();
                btnDisconnectDrive.setVisibility(View.GONE);
                btnShowDrive.setVisibility(View.GONE);
            }
        }));

        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            btnDisconnectDrive.setVisibility(View.VISIBLE);
        }

        Button btnContact = dialogView.findViewById(R.id.btn_contact);
        btnContact.setOnClickListener(v1 -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "contact@finnik.de", null));
            startActivity(intent);
        });

        builder.setView(dialogView);
        builder.show();
    }
}