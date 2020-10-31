package de.finnik.passvault.pass;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

public enum PassProperty {
    LANG, DRIVE_PASSWORD, GEN_LENGTH, GEN_BIG, GEN_SMALL, GEN_NUM, GEN_SPE;
    private static final String TAG = "PassProperty";

    /**
     * The file where the properties are saved
     */
    public static final String FILE = "passvault.properties";

    /**
     * The value of the property
     */
    private String value;

    /**
     * Loads the properties from {@link PassProperty#FILE}
     */
    public static void load(Context context) {
        Properties properties = new Properties();
        try {
            properties.load(context.openFileInput(FILE));
        } catch (IOException e) {
            Log.e(TAG, "load: Error while loading application properties!", e);
        }

        for (PassProperty property : PassProperty.values()) {
            property.setValue(context, properties.getProperty(property.name(), property.getDefault()));
        }
    }

    /**
     * Stores the properties to {@link PassProperty#FILE}
     */
    public static void store(Context context) {
        Properties properties = new Properties();
        Arrays.stream(PassProperty.values()).forEach(prop -> properties.setProperty(prop.name(), String.valueOf(prop.getValue())));
        try {
            properties.store(context.openFileOutput(FILE, Context.MODE_PRIVATE), "PassVault Settings");
        } catch (IOException e) {
            Log.e(TAG, "store: Error while storing application.properties", e);
        }
    }

    /**
     * Returns the value of this property
     *
     * @return Value of this property
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this property. Casts the given object to a string via {@link String#valueOf(Object)},
     * checks whether the value is valid ({@link PassProperty#matches(String)}) for this property and loads the default value ({@link PassProperty#getDefault()}) if not.
     *
     * @param value The value to be assigned to the property
     * @return Whether the input was valid or not
     */
    public boolean setValue(Context context, Object value) {
        String s = String.valueOf(value);
        try {
            if (matches(s)) {
                if (this.value == null) {
                    Log.i(TAG, "setValue: Loaded property " + this.name() + ": " + s);
                } else {
                    Log.i(TAG, "setValue: Set property " + this.name() + ": " + s);
                }
                this.value = s;
            } else if (this.value == null) {
                this.value = getDefault();
            }
            store(context);
        } catch (Exception e) {
            this.value = getDefault();
            return false;
        }
        return matches(s);
    }

    // TODO check for languages
    String[] availableLanguages = new String[]{"en"};

    /**
     * Loads the default value of each property
     *
     * @return Default value of {@code this}
     */
    private String getDefault() {
        switch (this) {
            case LANG:
                String systemLang = Locale.getDefault().getLanguage();
                return Arrays.asList(availableLanguages).contains(systemLang) ? systemLang : "en";
            case DRIVE_PASSWORD:
                return "";
            case GEN_BIG:
            case GEN_SMALL:
            case GEN_NUM:
            case GEN_SPE:
                return "true";
            case GEN_LENGTH:
                return "12";
        }
        return null;
    }

    /**
     * Checks whether a input is valid for {@code this} property.
     *
     * @param value The input to check
     * @return Valid or not
     */
    private boolean matches(String value) {
        switch (this) {
            case LANG:
                return Arrays.asList(availableLanguages).contains(value);
            case DRIVE_PASSWORD:
                return value != null;
            case GEN_BIG:
            case GEN_SMALL:
            case GEN_NUM:
            case GEN_SPE:
                return value.equals("true") || value.equals("false");
            case GEN_LENGTH:
                int z = Integer.parseInt(value);
                return z >= 5 && z <= 30;
            default:
                return false;
        }
    }
}
