package de.bulling.barcodebuddyscanner;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import de.bulling.barcodebuddyscanner.Helper.EdgeToEdgeHelper;

public class DebugActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		EditText debugtext = findViewById(R.id.editText3);
		// This EditText fills the whole window, so pad it clear of every
		// system bar (edge-to-edge is enforced when targeting SDK 35+).
		EdgeToEdgeHelper.applyInsetsAsPadding(debugtext, true, true, true, true);
		if (getIntent().getStringExtra("debug") != null) {
			debugtext.setText(getIntent().getStringExtra("debug"));
		}
	}
}
