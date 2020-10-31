package de.finnik.passvault.gui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
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
import de.finnik.passvault.gui.ui.main.ManageFragment;
import de.finnik.passvault.gui.ui.main.SectionsPagerAdapter;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.utils.FileUtils;
import de.finnik.passvault.utils.GUIUtils;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class PassActivity extends AppCompatActivity implements LifecycleObserver {
    public static String password;
    public static List<Password> passwordList;

    private static final String TAG = "PassActivity";
    public static GifDrawable button_synchronize_drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        password = getIntent().getStringExtra("pass");
        passwordList = new ArrayList<>();
        passwordList.addAll(Arrays.asList(new Gson().fromJson(getIntent().getStringExtra("passwords"), Password[].class)));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        GifImageView button_synchronize = findViewById(R.id.button_synchronize);
        button_synchronize_drawable = (GifDrawable) button_synchronize.getDrawable();
        button_synchronize_drawable.stop();
        button_synchronize_drawable.setSpeed(4.0f);
        button_synchronize.setOnClickListener(v -> {
            if (GoogleSignIn.getLastSignedInAccount(this) == null) {
                signIn(v);
            } else {
                synchronize(this);
            }
        });
        registerForContextMenu(button_synchronize);

        ImageView imageViewLogo = findViewById(R.id.imageViewLogo);
        registerForContextMenu(imageViewLogo);

        synchronize(this);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    boolean pause = false;

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void pause() {
        pause = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void pauseAndSkipToLogin() {
        Log.i(TAG, "pauseAndSkipToLogin: " + pause);
        if (!pause)
            return;
        Intent intent = new Intent(PassActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void signIn(View v) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .requestEmail()
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        startActivityForResult(client.getSignInIntent(), 400);
    }

    public void signOut() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .requestEmail()
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        client.signOut().addOnCompleteListener(s -> {
            Toast.makeText(this, getString(R.string.disconnected_drive), Toast.LENGTH_LONG).show();
        }).addOnFailureListener(s -> {
            Toast.makeText(this, getString(R.string.error_disconnecting_drive), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 400) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(googleSignInAccount -> DriveLocalHelper.synchronize(this, googleSignInAccount))
                    .addOnFailureListener(e -> {
                        if (!e.getMessage().startsWith("12501"))
                            GUIUtils.messageDialog(this, getString(R.string.error_while_synchronizing, Objects.requireNonNull(e.getMessage()).replaceAll("[^0-9]", "")));
                    });
        }


    }

    public static AES getAES() {
        return new AES(password);
    }

    public static void synchronize(Activity activity) {
        GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(activity);
        if (lastAccount != null) {
            button_synchronize_drawable.start();
            DriveLocalHelper.synchronize(activity, lastAccount);
        }
        FileUtils.savePasswords(activity);
        ManageFragment.refreshPasswords();
    }

    // Disconnect drive
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.button_synchronize) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null)
                menu.add(1, 0, 0, R.string.disconnect_drive);
        } else if (v.getId() == R.id.imageViewLogo) {
            menu.add(2, 0,0, R.string.about);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        if (menuItem.getGroupId() == 1) {
            if (menuItem.getItemId() == 0) {
                signOut();
            }
        } else if(menuItem.getGroupId() == 2) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://finnik.de/passvault"));
            startActivity(browserIntent);
        } else {
            return false;
        }
        return true;
    }
}