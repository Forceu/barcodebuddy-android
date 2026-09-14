package de.bulling.barcodebuddyscanner;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

import de.bulling.barcodebuddyscanner.Helper.ApiConnection;
import de.bulling.barcodebuddyscanner.Helper.PermissionHelper;
import de.bulling.barcodebuddyscanner.Helper.SharedPrefHelper;


public class ContinuousCaptureActivity extends Activity {
	private DecoratedBarcodeView barcodeView;
	private Button               modeButton;
	private long                 lastScanTime = 0;
	private String               lastBarcode  = null;

	private       ApiConnection apiConnection = null;
	private final boolean       IS_DEBUG      = BuildConfig.DEBUG;


	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.continuous_scan);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// ACCESS_LOCAL_NETWORK is optional: if denied, connecting to a
		// Barcode Buddy server on the same LAN might not succeed, but we
		// still go ahead and create the API instance either way.
		PermissionHelper.requestLocalNetworkPermissionIfNeeded(this);
		apiConnection = new ApiConnection(this, IS_DEBUG);

		barcodeView = findViewById(R.id.barcode_scanner);
		modeButton = findViewById(R.id.button_mode);
		barcodeView.setStatusText("");
		modeButton.setOnClickListener(v -> showOnClickMenu(getApplicationContext(),v));
		barcodeView.initializeFromIntent(getIntent());
		barcodeView.decodeContinuous(callback);

		int requestOrientation = apiConnection.getSharedPrefHelper().getPreferredOrientation();
		if (requestOrientation != SharedPrefHelper.NO_ORIENTATION_CHANGE)
			this.setRequestedOrientation(requestOrientation);
	}

	private void showOnClickMenu(Context context, View view) {
		PopupMenu    popupMenu = new PopupMenu(context, view);
		MenuInflater inflater  = popupMenu.getMenuInflater();
		inflater.inflate(R.menu.modeselect, popupMenu.getMenu());

		popupMenu.setOnMenuItemClickListener(item -> {
			int itemId = item.getItemId();

			if (itemId == R.id.modesel_p) {
				apiConnection.setMode(2);
				return true;
			} else if (itemId == R.id.modesel_c) {
				apiConnection.setMode(0);
				return true;
			} else if (itemId == R.id.modesel_o) {
				apiConnection.setMode(3);
				return true;
			} else if (itemId == R.id.modesel_i) {
				apiConnection.setMode(4);
				return true;
			} else if (itemId == R.id.modesel_s) {
				apiConnection.setMode(5);
				return true;
			} else if (itemId == R.id.modesel_ca) {
				apiConnection.setMode(6);
				return true;
			} else if (itemId == R.id.modesel_cs) {
				apiConnection.setMode(1);
				return true;
			} else {
				return false;
			}
		});

		popupMenu.show();
	}

	private BarcodeCallback callback = new BarcodeCallback() {
		@Override
		public void barcodeResult(BarcodeResult result) {
			long millisSinceLastScan = (System.currentTimeMillis() - lastScanTime);
			if (result.getText() == null || (result.getText().equals(lastBarcode) && millisSinceLastScan < 1500)) {
				// Prevent duplicate scans
				return;
			}

			lastScanTime = System.currentTimeMillis();
			lastBarcode  = result.getText();
			barcodeView.setStatusText(result.getText());
			if (!result.getText().isEmpty())
				apiConnection.processBarcode(result.getText());
		}

		@Override
		public void possibleResultPoints(List<ResultPoint> resultPoints) {
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		startScanningIfCameraPermitted();
	}

	/**
	 * The camera permission is required for scanning to work at all, so unlike
	 * ACCESS_LOCAL_NETWORK this one blocks: we only resume the barcode view once
	 * it's actually granted.
	 */
	private void startScanningIfCameraPermitted() {
		if (PermissionHelper.hasCameraPermission(this)) {
			barcodeView.resume();
		} else {
			PermissionHelper.requestCameraPermission(this);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PermissionHelper.REQUEST_CODE_CAMERA) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				barcodeView.resume();
			} else {
				Toast.makeText(this, R.string.error_camera_permission_denied, Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		barcodeView.pause();
	}

}