package de.finnik.passvault.gui.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.Objects;

import de.finnik.passvault.R;
import de.finnik.passvault.gui.CharSwitch;
import de.finnik.passvault.gui.PassActivity;
import de.finnik.passvault.pass.PassProperty;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.pass.PasswordGenerator;

public class GenerateFragment extends Fragment {
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_generate, container, false);
        root.findViewById(R.id.button_generate_options).setOnClickListener(view -> {
            View layout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_generate_options, null);
            LinearLayout linearLayout = layout.findViewById(R.id.linear_generate_options);
            AlertDialog generate_options = new AlertDialog.Builder(getContext()).setView(layout).setOnCancelListener(dialog -> {
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    View childAt = linearLayout.getChildAt(i);
                    if (childAt.getClass() == CharSwitch.class) {
                        CharSwitch charSwitch = (CharSwitch) childAt;
                        assert charSwitch.getPassChars().getMatchingProp() != null;
                        charSwitch.getPassChars().getMatchingProp().setValue(getContext(), charSwitch.isChecked());
                    }
                }
            }).create();
            generate_options.setContentView(layout);
            generate_options.show();

            SeekBar password_length = layout.findViewById(R.id.seekBar_password_length);
            password_length.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int newLength = progress + 5;
                    PassProperty.GEN_LENGTH.setValue(getContext(), newLength);
                    ((TextView) layout.findViewById(R.id.textView_password_length)).setText(getString(R.string.password_length, newLength));
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            password_length.setProgress(Integer.parseInt(PassProperty.GEN_LENGTH.getValue()) - 5);

            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                if (linearLayout.getChildAt(i).getClass() == CharSwitch.class) {
                    CharSwitch charSwitch = ((CharSwitch) linearLayout.getChildAt(i));
                    assert charSwitch.getPassChars().getMatchingProp() != null;
                    charSwitch.setChecked(Boolean.parseBoolean(charSwitch.getPassChars().getMatchingProp().getValue()));
                }
            }
        });
        Button btnGenerate = root.findViewById(R.id.button_generate);
        btnGenerate.setOnClickListener(v -> {
            PasswordGenerator.PassChars[] passChars = Arrays.stream(PassProperty.values())
                    .filter(prop -> prop.name().startsWith("GEN"))
                    .filter(prop -> Boolean.parseBoolean(prop.getValue()))
                    .map(PasswordGenerator.PassChars::getMatchingChar)
                    .toArray(PasswordGenerator.PassChars[]::new);
            String password = PasswordGenerator.generatePassword(Integer.parseInt(PassProperty.GEN_LENGTH.getValue()), passChars);
            ((EditText) root.findViewById(R.id.edit_text_pass)).setText(password);
        });

        Button btnSave = root.findViewById(R.id.button_save);
        ((EditText) root.findViewById(R.id.edit_text_other)).setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnSave.callOnClick();
            }
            return false;
        });
        btnSave.setOnClickListener(v -> {
            EditText[] editTexts = new EditText[]{root.findViewById(R.id.edit_text_pass), root.findViewById(R.id.edit_text_site), root.findViewById(R.id.edit_text_user), root.findViewById(R.id.edit_text_other)};
            Password password = new Password(editTexts[0].getText().toString(), editTexts[1].getText().toString(), editTexts[2].getText().toString(), editTexts[3].getText().toString());
            PassActivity.passwordList.add(password);
            for (EditText editText : editTexts) {
                editText.setText("");
            }
            ((InputMethodManager) Objects.requireNonNull(Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE))).hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            PassActivity.synchronize(getActivity());
        });
        return root;
    }
}
