package de.bulling.barcodebuddyscanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.bulling.barcodebuddyscanner.Api.BBApi;
import de.bulling.barcodebuddyscanner.Api.BBApiCallback;
import de.bulling.barcodebuddyscanner.Helper.SharedPrefHelper;
import retrofit2.Response;

import static de.bulling.barcodebuddyscanner.Api.BBApiCallback.ERROR_NETWORK;
import static de.bulling.barcodebuddyscanner.Api.BBApiCallback.ERROR_OTHER;
import static de.bulling.barcodebuddyscanner.Api.BBApiCallback.ERROR_UNAUTHORIZED;

public class SetupActivity extends AppCompatActivity {

	private Button      buttonScan;
	private Button      buttonConnect;
	private ProgressBar progressBar;
	private EditText    editTextUrl;
	private EditText    editTextApi;

	private final int MIN_BBUDDY_VERSION = 1500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		setupUi();
	}

	//When setup QR code is scanned
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result != null) {
			if (result.getContents() != null) {
				parseQrData(result.getContents());
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void parseQrData(String qrCodeContent) {
		if (!qrCodeContent.contains("issetup")) {
			Toast.makeText(SetupActivity.this, R.string.e_invalidqr, Toast.LENGTH_LONG).show();
			processError(ERROR_OTHER, null);
			buttonConnect.setVisibility(View.INVISIBLE);
		} else {
			try {
				JSONObject setupData = new JSONObject(qrCodeContent);
				String     url       = setupData.getString("url");
				String     key       = setupData.getString("key");
				processConnection(url, key);
			} catch (JSONException e) {
				e.printStackTrace();
				processError(ERROR_OTHER, e.getMessage());
			}
		}
	}


	private void processConnection(final String url, final String key) {
		buttonScan.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
		editTextUrl.setText(url);
		editTextUrl.setVisibility(View.VISIBLE);
		editTextApi.setText(key);
		editTextApi.setVisibility(View.VISIBLE);
		BBApi api;
		try {
			api = new BBApi(url, key);
		} catch (IllegalArgumentException e) {
			processError(ERROR_OTHER, e.getMessage());
			return;
		}
		api.getVersionInfo(new BBApiCallback() {
			@Override
			public void onResult(Object result) {
				if (result instanceof Integer) {
					if ((Integer) result >= MIN_BBUDDY_VERSION) {
						Toast.makeText(SetupActivity.this,
								SetupActivity.this.getString(R.string.tut_connected),
								Toast.LENGTH_LONG).show();

						SharedPrefHelper.saveApiDetails(SetupActivity.this, url, key);
						SetupActivity.this.finish();
					} else
						processError(ERROR_OTHER, SetupActivity.this.getString(R.string.error_too_old));
				} else
					processError(ERROR_OTHER, SetupActivity.this.getString(R.string.error_unex));
			}

			@Override
			public void onError(int errorCode, String errorMessage, Response<JsonElement> response) {
				processError(errorCode, errorMessage);
			}
		});
	}

	private void setupUi() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		progressBar   = findViewById(R.id.progressBar);
		editTextUrl   = findViewById(R.id.editText);
		editTextApi   = findViewById(R.id.editText2);
		buttonConnect = findViewById(R.id.button2);
		buttonScan    = findViewById(R.id.button);
		setUpButtons();
	}

	private void setUpButtons() {
		TextView privacyPolicy = findViewById(R.id.textView3);
		buttonScan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(SetupActivity.this);
				integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
				integrator.setPrompt(getString(R.string.tut_scan));
				integrator.setOrientationLocked(false);
				integrator.setBeepEnabled(true);
				integrator.setBarcodeImageEnabled(true);
				integrator.initiateScan();
			}
		});
		buttonConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonConnect.setEnabled(false);
				editTextApi.setEnabled(false);
				editTextUrl.setEnabled(false);
				ensureTrailingSlashExists(editTextUrl);
				processConnection(editTextUrl.getText().toString(), editTextApi.getText().toString());
			}
		});

		privacyPolicy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "https://www.bulling.mobi/en/privacypolicy.html";
				Intent i   = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
	}


	private void processError(int errorCode, String errorMessage) {
		//TODO
		switch (errorCode) {
			case ERROR_NETWORK:
				break;
			case ERROR_UNAUTHORIZED:
				break;
			case ERROR_OTHER:
				break;
		}
		progressBar.setVisibility(View.GONE);
		buttonConnect.setEnabled(true);
		buttonConnect.setVisibility(View.VISIBLE);
		editTextUrl.setEnabled(true);
		editTextApi.setEnabled(true);
		if (errorMessage != null)
			Toast.makeText(SetupActivity.this,
					SetupActivity.this.getString(R.string.error_cnc) + errorMessage,
					Toast.LENGTH_LONG).show();
	}


	@SuppressLint("SetTextI18n")
	private static void ensureTrailingSlashExists(EditText editText) {
		String text = editText.getText().toString();
		if (!text.endsWith("/"))
			editText.setText(text + "/");
	}
}