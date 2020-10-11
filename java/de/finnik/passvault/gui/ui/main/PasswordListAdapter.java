package de.finnik.passvault.gui.ui.main;

import android.content.Context;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.finnik.passvault.R;
import de.finnik.passvault.pass.Password;

public class PasswordListAdapter extends ArrayAdapter<Password> {
    private Context mContext;
    private int mResource;
    public PasswordListAdapter(@NonNull Context context, int resource, @NonNull List<Password> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Password password = getItem(position);

        assert password != null;

        PassViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new PassViewHolder();
            holder.pass = convertView.findViewById(R.id.textView_list_pass);
            holder.site = convertView.findViewById(R.id.textView_list_site);
            holder.user = convertView.findViewById(R.id.textView_list_user);
            holder.other = convertView.findViewById(R.id.textView_list_other);

            convertView.setTag(holder);
        } else {
            holder = (PassViewHolder) convertView.getTag();
        }

        holder.pass.setText(password.getPass());
        holder.pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        holder.site.setText(password.getSite());
        holder.user.setText(password.getUser());
        holder.other.setText(password.getOther());

        return convertView;
    }

    private static class PassViewHolder {
        TextView pass;
        TextView site;
        TextView user;
        TextView other;
    }
}
