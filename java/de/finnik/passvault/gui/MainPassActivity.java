package de.finnik.passvault.gui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.nulabinc.zxcvbn.Zxcvbn;

import de.finnik.passvault.R;
import de.finnik.passvault.gui.onboarding.OnboardingActivity;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.utils.GUIUtils;

public class MainPassActivity extends AppCompatActivity {
    public int[] colors = new int[]{0xFFFF0000, 0xFFFF3C00, 0xFFFFBF00, 0xFF238823, 0xFF007000};

    private EditText input_first, input_repeat;
    Button button_confirm;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pass);

        ProgressBar progress = findViewById(R.id.progress_create_main_feedback);
        progress.setProgressTintList(ColorStateList.valueOf(Color.RED));

        button_confirm = findViewById(R.id.button_confirm_main_pass);
        layout = findViewById(R.id.activity_main_pass);

        input_first = findViewById(R.id.edit_text_create_main);

        setupInputFirst();

        input_repeat = findViewById(R.id.edit_text_repeat_main);
        input_repeat.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirm(v);
            }
            return false;
        });

        if(getIntent().getBooleanExtra("showOnboarding", true)) {
            Intent intent = new Intent(this, OnboardingActivity.class);
            getIntent().putExtra("showOnboarding", false);
            startActivity(intent);
        }
    }

    private void setProgressColor(int c) {
        ProgressBar progress = findViewById(R.id.progress_create_main_feedback);
        progress.setProgressTintList(ColorStateList.valueOf(c));
    }

    public void confirm(View v) {
        if (input_repeat.getVisibility() == View.VISIBLE) {
            // Check whether inputs are identical
            if (input_first.getText().toString().equals(input_repeat.getText().toString())) {
                Intent intent = new Intent(this, PassActivity.class);
                intent.putExtra("pass", input_first.getText().toString());
                Password[] passwords = new Password[0];

                if(getIntent().getExtras() != null) {
                    if (getIntent().getExtras().containsKey("passwords")) {
                        passwords = new Gson().fromJson(getIntent().getStringExtra("passwords"), Password[].class);
                    }

                    if (getIntent().getExtras().containsKey("drivePass")) {
                        intent.putExtra("drivePass", getIntent().getStringExtra("drivePass"));
                    }
                }
                intent.putExtra("passwords", new Gson().toJson(passwords));
                startActivity(intent);
                finish();
            } else {
                GUIUtils.messageDialog(this, R.string.wrong_pass);
            }
        } else if (new Zxcvbn().measure(input_first.getText()).getScore() > 2) {
            input_first.setEnabled(false);

            findViewById(R.id.textView_repeat_main).setVisibility(View.VISIBLE);
            input_repeat.setVisibility(View.VISIBLE);
            input_repeat.requestFocus();
            GUIUtils.openKeyboard(this);

            input_first.setEnabled(true);
            input_first.setOnKeyListener((view, actionId, keyEvent) -> {
                hideRepeat();
                setupInputFirst();
                return false;
            });
        } else {
            GUIUtils.messageDialog(this, R.string.weak_main_pass);
        }
    }

    private void hideRepeat() {
        input_first.setOnKeyListener(null);
        findViewById(R.id.textView_repeat_main).setVisibility(View.GONE);
        input_repeat.setVisibility(View.GONE);
        input_repeat.setText("");
    }

    private void setupInputFirst() {
        input_first.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setProgressColor(colors[new Zxcvbn().measure(s).getScore()]);
            }
        });
        input_first.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input_first.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirm(v);
            }
            return false;
        });
    }
}