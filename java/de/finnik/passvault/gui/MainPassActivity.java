package de.finnik.passvault.gui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.nulabinc.zxcvbn.Zxcvbn;

import de.finnik.passvault.R;
import de.finnik.passvault.gui.onboarding.OnboardingActivity;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.utils.GUIUtils;

public class MainPassActivity extends AppCompatActivity {
    /**
     * Colors to be used to display the password strength
     */
    public int[] colors = new int[]{0xFFFF0000, 0xFFFF3C00, 0xFFFFBF00, 0xFF238823, 0xFF007000};

    private EditText input_first, input_repeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pass);

        input_first = findViewById(R.id.edit_text_create_main);
        setupInputFirst();

        input_repeat = findViewById(R.id.edit_text_repeat_main);
        input_repeat.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirm(v);
            }
            return false;
        });

        // Show onboarding screen when intent extras don't restrict
        if (getIntent().getBooleanExtra("showOnboarding", true)) {
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            // Makes sure that onboarding screen is just shown once
            getIntent().putExtra("showOnboarding", false);
        }
    }

    /**
     * Sets tint color of {@link R.id#progress_create_main_feedback} progress bar to given color
     *
     * @param c Color to be applied to progress bar
     */
    private void setProgressColor(int c) {
        ProgressBar progress = findViewById(R.id.progress_create_main_feedback);
        progress.setProgressTintList(ColorStateList.valueOf(c));
    }

    /**
     * Processes the user's inputted passwords
     *
     * @param v Not important at all; Is needed so that android:onclick can be called from xml
     */
    public void confirm(View v) {
        // Checks whether user has already inputted password twice
        if (input_repeat.getVisibility() == View.VISIBLE) {
            // User has already inputted two passwords
            // Check whether inputs are identical
            if (input_first.getText().toString().equals(input_repeat.getText().toString())) {
                Intent intent = new Intent(this, PassActivity.class);
                intent.putExtra("pass", input_first.getText().toString());
                Password[] passwords = new Password[0];

                // Checks whether activity was called with already given passwords and drive passwords
                if (getIntent().getExtras() != null) {
                    if (getIntent().getExtras().containsKey("passwords")) {
                        // Passwords were forwarded to activity and need to be passed to the creating activity
                        passwords = new Gson().fromJson(getIntent().getStringExtra("passwords"), Password[].class);
                    }

                    if (getIntent().getExtras().containsKey("drivePass")) {
                        // Drive password was forwarded to activity and needs to be passed to the creating activity
                        intent.putExtra("drivePass", getIntent().getStringExtra("drivePass"));
                    }
                }
                intent.putExtra("passwords", new Gson().toJson(passwords));
                startActivity(intent);
                finish();
            } else {
                // Repeated password doesn't match the first one
                GUIUtils.messageDialog(this, R.string.wrong_pass);
            }
        } else if (new Zxcvbn().measure(input_first.getText()).getScore() > 2) {
            // User has inputted one password and this passwords strength is bigger than 2
            showRepeat();

            input_first.setOnKeyListener((view, actionId, keyEvent) -> {
                hideRepeat();
                setupInputFirst();
                return false;
            });
        } else {
            // Inputted password is too weak
            GUIUtils.messageDialog(this, R.string.weak_main_pass);
        }
    }

    /**
     * Displays the {@link MainPassActivity#input_repeat} edit text and its label
     *
     * {@link R.id#edit_text_repeat_main}
     * @see R.id#textView_repeat_main
     */
    private void showRepeat() {
        input_first.setEnabled(false);

        findViewById(R.id.textView_repeat_main).setVisibility(View.VISIBLE);
        input_repeat.setVisibility(View.VISIBLE);

        input_repeat.requestFocus();
        GUIUtils.openKeyboard(this);

        input_first.setEnabled(true);
    }

    /**
     * Hides the {@link MainPassActivity#input_repeat} edit text and its label
     *
     * {@link R.id#edit_text_repeat_main}
     * @see R.id#textView_repeat_main
     */
    private void hideRepeat() {
        input_first.setOnKeyListener(null);
        findViewById(R.id.textView_repeat_main).setVisibility(View.GONE);
        input_repeat.setVisibility(View.GONE);
        input_repeat.setText("");
    }

    /**
     * Setups {@link MainPassActivity#input_first}
     *
     * @see R.id#edit_text_create_main
     */
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
        input_first.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirm(v);
            }
            return false;
        });
    }
}