package de.finnik.passvault.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Switch;

import de.finnik.passvault.R;
import de.finnik.passvault.pass.PasswordGenerator;

public class CharSwitch extends Switch {
    private final PasswordGenerator.PassChars passChars;

    public CharSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CharSwitch);
        passChars = PasswordGenerator.PassChars.values()[typedArray.getInt(R.styleable.CharSwitch_passChar, 0)];
        typedArray.recycle();
    }

    public PasswordGenerator.PassChars getPassChars() {
        return passChars;
    }


}