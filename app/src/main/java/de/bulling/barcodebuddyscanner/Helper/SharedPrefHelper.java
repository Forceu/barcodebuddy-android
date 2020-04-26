package de.bulling.barcodebuddyscanner.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;

import de.bulling.barcodebuddyscanner.Api.BBApi;

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
		return (volInInt / 20f);
	}

	public final static int NO_ORIENTATION_CHANGE = -1;


	@SuppressWarnings("ConstantConditions")
	public int getPreferredOrientation() {
		int orientation = Integer.parseInt(this.globalPreferences.getString("orientation", "0"));
		switch (orientation) {
			case 1:
				return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			case 2:
				return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			default:
				return NO_ORIENTATION_CHANGE;
		}
	}

	public BBApi initBBApi() {
		return new BBApi(this.globalPreferences.getString("url", null),
				this.globalPreferences.getString("key", null),
				this.globalPreferences.getBoolean("unsafe", false));
	}

}
