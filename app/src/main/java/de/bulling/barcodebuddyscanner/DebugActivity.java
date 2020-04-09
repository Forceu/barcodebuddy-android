package de.bulling.barcodebuddyscanner;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class DebugActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		if (getIntent().getStringExtra("debug") != null) {
			EditText debugtext = findViewById(R.id.editText3);
			debugtext.setText(getIntent().getStringExtra("debug"));
		}
	}
}
