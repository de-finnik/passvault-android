package de.finnik.passvault.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.function.Consumer;

import de.finnik.passvault.R;

public class GUIUtils {
    public static void openKeyboard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public static void inputDialog(Activity activity, String string, Consumer<CharSequence> after, boolean isPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_input, null);

        ImageView button_accept = dialogView.findViewById(R.id.button_accept_confirm);
        ImageView button_decline = dialogView.findViewById(R.id.button_decline_confirm);

        EditText input = dialogView.findViewById(R.id.edit_text_input);
        if (isPassword) {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        builder.setView(dialogView);

        TextView view = dialogView.findViewById(R.id.textView_message);
        view.setText(string);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        button_accept.setOnClickListener(v -> {
            after.accept(input.getText());
            dialog.dismiss();
        });
        button_decline.setOnClickListener(v -> {
            after.accept("");
            dialog.dismiss();
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            button_accept.callOnClick();
            return false;
        });

        dialog.show();
    }

    public static void confirmDialog(Activity activity, String string, Consumer<Boolean> after) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm, null);

        ImageView button_accept = dialogView.findViewById(R.id.button_accept_confirm);
        ImageView button_decline = dialogView.findViewById(R.id.button_decline_confirm);

        builder.setView(dialogView);

        TextView view = dialogView.findViewById(R.id.textView_message);
        view.setText(string);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        button_accept.setOnClickListener(v -> {
            after.accept(true);
            dialog.dismiss();
        });
        button_decline.setOnClickListener(v -> {
            after.accept(false);
            dialog.dismiss();
        });

        dialog.show();
    }


    public static void messageDialog(Activity activity, int resID) {
        messageDialog(activity, activity.getString(resID));
    }

    public static void messageDialog(Activity activity, String string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_message, null);

        ImageView imageButton = dialogView.findViewById(R.id.button_close_message);
        builder.setView(dialogView);

        TextView view = dialogView.findViewById(R.id.textView_message);
        view.setText(string);

        final AlertDialog dialog = builder.create();
        imageButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}
