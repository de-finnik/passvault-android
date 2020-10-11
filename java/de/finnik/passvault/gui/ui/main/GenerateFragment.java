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
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.finnik.passvault.R;
import de.finnik.passvault.gui.PassActivity;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.pass.PasswordGenerator;

public class GenerateFragment extends Fragment {
    private int password_length;
    private Map<String, Boolean> characters = new HashMap<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_generate, container, false);
        root.findViewById(R.id.button_generate_options).setOnClickListener(view->{
            View layout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_generate_options, null);
            AlertDialog generate_options = new AlertDialog.Builder(getContext()).setView(layout).create();
            generate_options.setContentView(layout);
            generate_options.show();

            SeekBar password_length = layout.findViewById(R.id.seekBar_password_length);
            password_length.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int newLength = progress + 5;
                    GenerateFragment.this.password_length = newLength;
                    ((TextView) layout.findViewById(R.id.textView_password_length)).setText(getString(R.string.password_length, newLength));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            password_length.setProgress(GenerateFragment.this.password_length-5);
            Switch[] switches = new Switch[]{layout.findViewById(R.id.switch_big_letters),layout.findViewById(R.id.switch_small_letters),layout.findViewById(R.id.switch_numbers),layout.findViewById(R.id.switch_specials)};
            for (Switch s :switches){
                s.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    GenerateFragment.this.characters.put(s.getText().toString(), isChecked);
                });
                s.setChecked(GenerateFragment.this.characters.get(s.getText()));
            }
        });
        password_length = 12;
        characters.put(getString(R.string.big_letters), true);
        characters.put(getString(R.string.small_letters), true);
        characters.put(getString(R.string.numbers), true);
        characters.put(getString(R.string.specials), false);
        root.findViewById(R.id.button_generate).setOnClickListener(v->{
            List<PasswordGenerator.PassChars> passChars = new ArrayList<>();
            characters.forEach((s,b)->{
                if(b) {
                    if(s.equals(getString(R.string.big_letters))) {
                        passChars.add(PasswordGenerator.PassChars.BIG_LETTERS);
                    } else if(s.equals(getString(R.string.small_letters))) {
                        passChars.add(PasswordGenerator.PassChars.SMALL_LETTERS);
                    } else if(s.equals(getString(R.string.numbers))) {
                        passChars.add(PasswordGenerator.PassChars.NUMBERS);
                    } else if(s.equals(getString(R.string.specials))) {
                        passChars.add(PasswordGenerator.PassChars.SPECIAL_CHARACTERS);
                    }
                }
            });
            String password = PasswordGenerator.generatePassword(password_length, passChars.toArray(new PasswordGenerator.PassChars[0]));
            ((EditText) root.findViewById(R.id.edit_text_pass)).setText(password);
        });
        Button save = root.findViewById(R.id.button_save);
        ((EditText) root.findViewById(R.id.edit_text_other)).setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                save.callOnClick();
            }
            return false;
        });
        save.setOnClickListener(v->{
            EditText[] editTexts = new EditText[]{root.findViewById(R.id.edit_text_pass),root.findViewById(R.id.edit_text_site),root.findViewById(R.id.edit_text_user),root.findViewById(R.id.edit_text_other)};
            Password password = new Password(editTexts[0].getText().toString(),editTexts[1].getText().toString(),editTexts[2].getText().toString(),editTexts[3].getText().toString());
            PassActivity.passwordList.add(password);
            for (EditText editText : editTexts) {
                editText.setText("");
            }
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            PassActivity.synchronize(getContext());
        });
        return root;
    }
}
