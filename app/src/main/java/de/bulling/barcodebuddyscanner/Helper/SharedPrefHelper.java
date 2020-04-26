package de.bulling.barcodebuddyscanner.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefHelper {

	private final SharedPreferences globalPreferences;

	public static boolean noApiDetailsSaved(final Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getBoolean("needsSetup", true);
	}

	public static void saveApiDetails(final Context context, final String url, final String key, final boolean isUnsafe) {
		SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
		preferences.putBoolean("needsSetup", false);
		preferences.putString("url", url);
		preferences.putString("key", key);
		preferences.putBoolean("unsafe", isUnsafe);
		preferences.apply();
	}


	public static void clearSettings(final Context context) {
		SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
		preferences.clear();
		preferences.putBoolean("needsSetup", true);
		preferences.apply();
	}

	public SharedPrefHelper(final Context context) {
		this.globalPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean isVibrationEnabled() {
		return this.globalPreferences.getBoolean("use_vibration", true);
	}

	public boolean isSoundEnabled() {
		return this.globalPreferences.getBoolean("use_beep", true);
	}

	public float getBeepVolume() {
		int volInInt = this.globalPreferences.getInt("volume_beep", 5);
		return (volInInt/20f);
	}

}
