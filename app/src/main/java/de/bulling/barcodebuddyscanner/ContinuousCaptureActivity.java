package de.bulling.barcodebuddyscanner;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

import de.bulling.barcodebuddyscanner.Api.BBApi;
import de.bulling.barcodebuddyscanner.Api.BBApiCallback;
import de.bulling.barcodebuddyscanner.Helper.BeepManager;
import retrofit2.Response;


public class ContinuousCaptureActivity extends Activity {
	private DecoratedBarcodeView barcodeView;
	private BeepManager          beepManager;
	private long                 lastScanTime = 0;
	private String               lastBarcode  = null;
	private BBApi                bbApi        = null;

	private final boolean IS_DEBUG = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.continuous_scan);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		bbApi = new BBApi(preferences.getString("url", null), preferences.getString("key", null));

		barcodeView = findViewById(R.id.barcode_scanner);
		barcodeView.initializeFromIntent(getIntent());
		barcodeView.decodeContinuous(callback);

		beepManager = new BeepManager(this);
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
			beepManager.playBeepSoundAndVibrate();
			bbApi.postBarcode(result.getText(), new BBApiCallback() {
				@Override
				public void onResult(Object result) {
					if (result instanceof String)
						Toast.makeText(ContinuousCaptureActivity.this,
								(String) result, Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(ContinuousCaptureActivity.this,
								R.string.error_unex, Toast.LENGTH_LONG).show();
				}

				@Override
				public void onError(int errorCode, String errorMessage, Response<JsonElement> response) {
					Toast.makeText(ContinuousCaptureActivity.this,
							getString(R.string.error) + errorMessage,
							Toast.LENGTH_LONG).show();
					if (IS_DEBUG) {
						String debugMessage = errorMessage + "\n";
						if (response != null) {
							debugMessage = debugMessage + "Received:\n" + response.raw().body();
						}
						Intent i = new Intent(ContinuousCaptureActivity.this, DebugActivity.class);
						i.putExtra("debug", debugMessage);
						ContinuousCaptureActivity.this.startActivity(i);
					}
				}
			});

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


	public void triggerScan(View view) {
		barcodeView.decodeSingle(callback);
	}

}