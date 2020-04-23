package de.bulling.barcodebuddyscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.bulling.barcodebuddyscanner.Helper.SharedPrefHelper;

public class MainActivity extends AppCompatActivity {

	private final int RESULT_SETUP_COMPLETE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupUI();

		if (SharedPrefHelper.noApiDetailsSaved(this))
			showSettingsScreen();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_SETUP_COMPLETE) {
			if (SharedPrefHelper.noApiDetailsSaved(this))
				finish();
		}
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
		// At the moment only one item, but leaving switch anyways
		switch (item.getItemId()) {
			case R.id.action_logout:
				SharedPrefHelper.clearSettings(MainActivity.this);
				showSettingsScreen();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void showSettingsScreen() {
		startActivityForResult(new Intent(MainActivity.this, SetupActivity.class), RESULT_SETUP_COMPLETE);
	}

}
