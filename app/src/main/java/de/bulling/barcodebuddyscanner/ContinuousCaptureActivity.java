package de.bulling.barcodebuddyscanner;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

import de.bulling.barcodebuddyscanner.Helper.ApiConnection;
import de.bulling.barcodebuddyscanner.Helper.SharedPrefHelper;


public class ContinuousCaptureActivity extends Activity {
	private DecoratedBarcodeView barcodeView;
	private long                 lastScanTime = 0;
	private String               lastBarcode  = null;

	private       ApiConnection apiConnection = null;
	private final boolean       IS_DEBUG      = de.bulling.barcodebuddyscanner.BuildConfig.IS_DEBUG;


	@SuppressLint("SourceLockedOrientationActivity")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.continuous_scan);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		apiConnection = new ApiConnection(this, IS_DEBUG);

		barcodeView = findViewById(R.id.barcode_scanner);
		barcodeView.initializeFromIntent(getIntent());
		barcodeView.decodeContinuous(callback);

		int requestOrientation = apiConnection.getSharedPrefHelper().getPreferredOrientation();
		if (requestOrientation != SharedPrefHelper.NO_ORIENTATION_CHANGE)
			this.setRequestedOrientation(requestOrientation);
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
		barcodeView.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		barcodeView.pause();
	}

}