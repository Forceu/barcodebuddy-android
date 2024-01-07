package de.bulling.barcodebuddyscanner;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupMenu;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

import de.bulling.barcodebuddyscanner.Helper.ApiConnection;
import de.bulling.barcodebuddyscanner.Helper.SharedPrefHelper;


public class ContinuousCaptureActivity extends Activity {
	private DecoratedBarcodeView barcodeView;
	private Button               modeButton;
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
			switch (item.getItemId()) {
				case R.id.modesel_p:
					apiConnection.setMode(2);
					return true;
				case R.id.modesel_c:
					apiConnection.setMode(0);
					return true;
				case R.id.modesel_o:
					apiConnection.setMode(3);
					return true;
				case R.id.modesel_i:
					apiConnection.setMode(4);
					return true;
				case R.id.modesel_s:
					apiConnection.setMode(5);
					return true;
				case R.id.modesel_ca:
					apiConnection.setMode(6);
					return true;
				case R.id.modesel_cs:
					apiConnection.setMode(1);
					return true;
				default:
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
		barcodeView.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		barcodeView.pause();
	}

}