package de.finnik.passvault.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

public class PassActivity extends AppCompatActivity {
    public static String password;
    public static List<Password> passwordList;

    private static final String TAG = "PassActivity";
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

        ImageButton button_synchronize = findViewById(R.id.button_synchronize);
        button_synchronize.setOnClickListener(v-> {
            if(GoogleSignIn.getLastSignedInAccount(this)==null) {
                signIn(v);
            } else {
                synchronize(this);
            }
        });

        synchronize(this);
    }

    public void signIn(View v) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        startActivityForResult(client.getSignInIntent(), 400);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 400) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(googleSignInAccount -> DriveLocalHelper.synchronize(this, googleSignInAccount))
                    .addOnFailureListener(e-> GUIUtils.messageDialog(this, getString(R.string.error_while_synchronizing, Objects.requireNonNull(e.getMessage()).replaceAll("[^0-9]",""))));
        }


    }

    public static AES getAES() {
        return new AES(password);
    }

    public static void synchronize(Context context) {
        GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(context);
        if(lastAccount != null) {
            DriveLocalHelper.synchronize(context, lastAccount);
        }
        FileUtils.savePasswords(context);
        ManageFragment.refreshPasswords();
    }
}