package de.finnik.passvault.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.finnik.passvault.AES.AES;
import de.finnik.passvault.R;
import de.finnik.passvault.pass.PassProperty;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.utils.GUIUtils;
import de.finnik.passvault.utils.Var;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PassProperty.load(this);
        try {
            openFileInput(Var.PASS_FILE);
        } catch (FileNotFoundException e) {
            // No passwords are saved -> Skip login
            startMainPassActivity();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login = findViewById(R.id.btn_login);
        EditText et = findViewById(R.id.edit_text_login);
        et.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login.callOnClick();
            }
            return false;
        });
        login.setOnClickListener(view -> {
            try {
                String pass = et.getText().toString();
                Password.readPasswords(openFileInput(Var.PASS_FILE), pass);
                startPassActivity(pass);
            } catch (AES.WrongPasswordException e) {
                GUIUtils.messageDialog(this, R.string.wrong_pass);
                et.setText("");
            } catch (IOException e) {
                e.printStackTrace();
                startPassActivity("");
            }
        });
    }

    private void startPassActivity(String pass) {
        Intent intent = new Intent(MainActivity.this, PassActivity.class);
        intent.putExtra("pass", pass);
        startActivity(intent);
        finish();
    }

    private void startMainPassActivity() {
        Intent intent = new Intent(MainActivity.this, MainPassActivity.class);
        startActivity(intent);
        finish();
    }
}
