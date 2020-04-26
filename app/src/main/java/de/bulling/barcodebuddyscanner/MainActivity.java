package de.bulling.barcodebuddyscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.bulling.barcodebuddyscanner.Helper.ApiConnection;
import de.bulling.barcodebuddyscanner.Helper.SharedPrefHelper;

public class MainActivity extends AppCompatActivity {

	private static final int RESULT_SETUP_COMPLETE  = 0;
	private static final int RESULT_SETTINGS_OPENED = 1;

	private boolean       useBarcodeScanner = false;
	private String        receivedBarcode   = "";
	private ApiConnection apiConnection     = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupUI();

		if (SharedPrefHelper.noApiDetailsSaved(this)) {
			showSetupScreen();
			return;
		}
		checkIfBarcodeScannerEnabled();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case RESULT_SETUP_COMPLETE:
				if (SharedPrefHelper.noApiDetailsSaved(this))
					finish();
				break;
			case RESULT_SETTINGS_OPENED:
				checkIfBarcodeScannerEnabled();
				break;

		}
	}

	private void checkIfBarcodeScannerEnabled() {
		useBarcodeScanner = SharedPrefHelper.isBtScannerEnabled(this);

		TextView info = this.findViewById(R.id.textView4);
		if (useBarcodeScanner) {
			apiConnection = new ApiConnection(this, BuildConfig.IS_DEBUG);
			info.setVisibility(View.VISIBLE);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else
			info.setVisibility(View.INVISIBLE);
	}

	private void setupUI() {
		ImageView image = findViewById(R.id.imageView2);
		image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ContinuousCaptureActivity.class));
			}
		});
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.action_logout:
				SharedPrefHelper.clearSettings(MainActivity.this);
				showSetupScreen();
				return true;

			case R.id.action_settings:
				Intent i = new Intent(MainActivity.this, SettingsActivity.class);
				startActivityForResult(i, RESULT_SETTINGS_OPENED);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void showSetupScreen() {
		startActivityForResult(new Intent(MainActivity.this, SetupActivity.class), RESULT_SETUP_COMPLETE);
	}


	private boolean isAllowedKeycode(int keycode) {
		return ((keycode >= KeyEvent.KEYCODE_0 && keycode <= KeyEvent.KEYCODE_9) ||
				(keycode >= KeyEvent.KEYCODE_NUMPAD_0 && keycode <= KeyEvent.KEYCODE_NUMPAD_9) ||
				(keycode >= KeyEvent.KEYCODE_A && keycode <= KeyEvent.KEYCODE_Z) ||
				keycode == KeyEvent.KEYCODE_PERIOD ||
				keycode == KeyEvent.KEYCODE_NUMPAD_SUBTRACT ||
				keycode == KeyEvent.KEYCODE_NUMPAD_DOT ||
				keycode == KeyEvent.KEYCODE_NUMPAD_ENTER ||
				keycode == KeyEvent.KEYCODE_ENTER ||
				keycode == KeyEvent.KEYCODE_MINUS);
	}

	private void processKeypress(final KeyEvent event, final int keycode) {
		if (keycode == KeyEvent.KEYCODE_ENTER || keycode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
			if (event.getAction() == KeyEvent.ACTION_UP) {
				if (apiConnection != null && !receivedBarcode.isEmpty())
					apiConnection.processBarcode(receivedBarcode);
				receivedBarcode = "";
			}
		} else {
			if (event.getAction() == KeyEvent.ACTION_UP) {
				receivedBarcode = receivedBarcode + (char) event.getUnicodeChar();
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		final int keycode = event.getKeyCode();
		if (useBarcodeScanner && isAllowedKeycode(keycode)) {
			processKeypress(event, keycode);
			return true;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}


}
