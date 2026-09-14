package de.bulling.barcodebuddyscanner.Helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Central place for the two runtime permissions used by the app:
 * <p>
 * - ACCESS_LOCAL_NETWORK (SDK 35+): needed to reliably reach a Barcode Buddy
 * server on the same LAN. This is optional - the app still works if it is
 * denied, but the connection to a server on the local network might fail.
 * <p>
 * - CAMERA: needed to actually scan a barcode. Without it, scanning cannot
 * work at all.
 */
public class PermissionHelper {

	// Referenced by string instead of Manifest.permission.ACCESS_LOCAL_NETWORK
	// so this compiles regardless of the compileSdkVersion in use.
	private static final String PERMISSION_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK";
	private static final int    MIN_SDK_LOCAL_NETWORK     = 35;

	public static final int REQUEST_CODE_LOCAL_NETWORK = 2001;
	public static final int REQUEST_CODE_CAMERA        = 2002;

	private PermissionHelper() {
	}

	public static boolean hasLocalNetworkPermission(final Context context) {
		if (Build.VERSION.SDK_INT < MIN_SDK_LOCAL_NETWORK) {
			// Permission does not exist / is not enforced on older versions
			return true;
		}
		return ContextCompat.checkSelfPermission(context, PERMISSION_LOCAL_NETWORK)
				== PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * Requests ACCESS_LOCAL_NETWORK if it isn't already granted. This is fire
	 * and forget: the permission is optional, so callers should go on and
	 * create their API instance right away instead of waiting for a result.
	 */
	public static void requestLocalNetworkPermissionIfNeeded(final Activity activity) {
		if (Build.VERSION.SDK_INT >= MIN_SDK_LOCAL_NETWORK && !hasLocalNetworkPermission(activity)) {
			ActivityCompat.requestPermissions(activity,
					new String[]{PERMISSION_LOCAL_NETWORK}, REQUEST_CODE_LOCAL_NETWORK);
		}
	}

	public static boolean hasCameraPermission(final Context context) {
		return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
				== PackageManager.PERMISSION_GRANTED;
	}

	public static void requestCameraPermission(final Activity activity) {
		ActivityCompat.requestPermissions(activity,
				new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
	}
}
