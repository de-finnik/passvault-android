package de.finnik.passvault.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import java.util.function.Consumer;

import de.finnik.passvault.R;

public class GUIUtils {
    public static void inputDialog(Context context, String message, Consumer<String> after) {
        TextInputLayout inputLayout = new TextInputLayout(context);
        int padding = (int) context.getResources().getDimension(R.dimen.builder_padding);
        inputLayout.setPadding(padding,0,padding,0);
        EditText input = new EditText(context);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setSingleLine();
        inputLayout.addView(input);
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setView(inputLayout)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    after.accept(input.getText().toString());
                })
                .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
                })
                .create();
        input.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                after.accept(input.getText().toString());
                alertDialog.hide();
            }
            return false;
        });
        alertDialog
                .show();
    }

    public static void messageDialog(Context context, String string) {
        new AlertDialog.Builder(context).setMessage(string).show();
    }

    public static void messageDialog(Context context, int resID) {
        new AlertDialog.Builder(context).setMessage(resID).show();
    }

    public static void confirmDialog(Context context, String string, Consumer<Boolean> after) {
        new AlertDialog.Builder(context)
                .setMessage(string)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, ((dialog, which) -> after.accept(true)))
                .setNegativeButton(android.R.string.no, ((dialog, which) -> after.accept(false)))
                .show();
    }
}
