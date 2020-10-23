package de.finnik.passvault.gui.ui.main;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.finnik.passvault.R;
import de.finnik.passvault.gui.PassActivity;
import de.finnik.passvault.pass.Password;
import de.finnik.passvault.utils.GUIUtils;
import de.finnik.passvault.utils.PassUtils;

public class ManageFragment extends Fragment {
    private static List<Password> displayedPasswords = new ArrayList<>();
    private static PasswordListAdapter adapter;

    private static EditText edit_text_search;

    private static final String TAG = "ManageFragment";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manage, container, false);
        adapter = new PasswordListAdapter(requireContext(), R.layout.list_adapter, displayedPasswords);
        ListView list_view_passwords = root.findViewById(R.id.list_view_passwords);
        list_view_passwords.setAdapter(adapter);
        list_view_passwords.setOnItemClickListener((parent, view, position, id) -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", adapter.getItem(position).getPass());
            clipboard.setPrimaryClip(clip);
        });
        registerForContextMenu(list_view_passwords);

        edit_text_search = root.findViewById(R.id.edit_text_search);
        edit_text_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshPasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        refreshPasswords();
        return root;
    }

    public static void refreshPasswords() {
        displayedPasswords.clear();
        displayedPasswords.addAll(PassUtils.getMatchingPasswords(edit_text_search == null ? "" : edit_text_search.getText().toString(), PassActivity.passwordList).stream().filter(p -> !p.isEmpty()).collect(Collectors.toList()));
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.list_view_passwords) {
            menu.add(0, 0, 0, getString(R.string.show_password));
            menu.add(0, 1, 1, getString(R.string.edit));
            menu.add(0, 2, 2, getString(R.string.delete));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        if (menuItem.getGroupId() != 0)
            return false;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        Password selected = displayedPasswords.get(info.position);
        switch (menuItem.getItemId()) {
            case 0:
                ((TextView) adapter.getView(info.position, info.targetView, (ViewGroup) menuItem.getActionView()).findViewById(R.id.textView_list_pass)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                break;
            case 1:
                View layout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_password, null);
                AlertDialog edit_dialog = new AlertDialog.Builder(getContext()).setView(layout).create();
                edit_dialog.setContentView(layout);
                edit_dialog.show();

                EditText edit_pass = edit_dialog.findViewById(R.id.edit_text_edit_pass);
                EditText edit_site = edit_dialog.findViewById(R.id.edit_text_edit_site);
                EditText edit_user = edit_dialog.findViewById(R.id.edit_text_edit_user);
                EditText edit_other = edit_dialog.findViewById(R.id.edit_text_edit_other);

                edit_pass.setText(selected.getPass());
                edit_site.setText(selected.getSite());
                edit_user.setText(selected.getUser());
                edit_other.setText(selected.getOther());

                edit_dialog.findViewById(R.id.button_edit_save).setOnClickListener(v -> {
                    selected.setPass(edit_pass.getText().toString());
                    selected.setSite(edit_site.getText().toString());
                    selected.setUser(edit_user.getText().toString());
                    selected.setOther(edit_other.getText().toString());

                    PassActivity.synchronize(getActivity());
                    edit_dialog.hide();
                });
                break;
            case 2:
                GUIUtils.confirmDialog(getActivity(), getString(R.string.confirm_deleting_password), b -> {
                    if (b) {
                        PassUtils.deletePassword(selected);
                        PassActivity.synchronize(getActivity());
                    }
                });

                break;
            default:
                break;

        }
        return true;
    }
}
