package de.bulling.barcodebuddyscanner.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.bulling.barcodebuddyscanner.Api.BBApi;
import de.bulling.barcodebuddyscanner.Api.BBApiCallback;
import de.bulling.barcodebuddyscanner.DebugActivity;
import de.bulling.barcodebuddyscanner.R;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class ApiConnection {


	private final BBApi            bbApi;
	private final Context          context;
	private final BeepManager      beepManager;
	private final SharedPrefHelper prefHelper;
	private final boolean          isDebug;


	public ApiConnection(Activity activity, boolean isDebug) {
		this.context    = activity;
		this.isDebug    = isDebug;
		this.prefHelper = new SharedPrefHelper(context);
		this.bbApi      = prefHelper.initBBApi();

		beepManager = new BeepManager(activity);
		beepManager.setBeepEnabled(prefHelper.isSoundEnabled());
		beepManager.setVibrateEnabled(prefHelper.isVibrationEnabled());
		beepManager.setBeepVolume(prefHelper.getBeepVolume());
	}

	public SharedPrefHelper getSharedPrefHelper() {
		return this.prefHelper;
	}

	public void beep() {
		beepManager.playBeepSoundAndVibrate();
	}

	public void processBarcode(String barcode) {
		this.beep();
		if (!isDebug)
			processBarcodeInternal(barcode);
		else
			processBarcodeInternalDebug(barcode);
	}

	private void processBarcodeInternal(String barcode) {
		bbApi.postBarcode(barcode, new BBApiCallback() {
			@Override
			public void onResult(Object result) {
				if (result instanceof String)
					Toast.makeText(context, (String) result, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(context, R.string.error_unex, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(int errorCode, String errorMessage, Response<JsonElement> response) {
				Toast.makeText(context, context.getString(R.string.error) + errorMessage,
						Toast.LENGTH_LONG).show();
			}
		});
	}

	private void processBarcodeInternalDebug(String barcode) {
		bbApi.postBarcodeDebug(barcode, new BBApiCallback() {
			@Override
			public void onResult(Object result) {
				Response<ResponseBody> response     = (Response<ResponseBody>) result;
				StringBuilder          debugMessage = new StringBuilder();
				if (response.body() == null) {
					debugMessage.append("Received Body is null.\n");
				} else {
					debugMessage.append("Received Body:\n\n'");
					try {
						debugMessage.append(response.body().string());
						debugMessage.append("'\n\n");
					} catch (Exception e) {
						debugMessage.append(convertStacktraceToString(e));
						e.printStackTrace();
					}
				}
				if (response.errorBody() == null) {
					debugMessage.append("Received Error Body is null.\n");
				} else {
					debugMessage.append("Received Error Body:\n\n'");
					try {
						debugMessage.append(response.errorBody().string());
						debugMessage.append("'\n\n");
					} catch (Exception e) {
						debugMessage.append(convertStacktraceToString(e));
						e.printStackTrace();
					}
				}

				Intent i = new Intent(context, DebugActivity.class);
				i.putExtra("debug", debugMessage.toString());
				context.startActivity(i);
			}

			@Override
			public void onError(int errorCode, String errorMessage, Response<JsonElement> response) {
				String debugMessage = errorMessage + "\n";
				if (response != null) {
					if (response.body() != null)
						debugMessage = debugMessage + "Received:\n'" + response.body().getAsString() + "'\n\n";
					try {
						debugMessage = debugMessage + "Received:\n'" + response.errorBody().string() + "'";
					} catch (Exception e) {
						debugMessage = debugMessage + convertStacktraceToString(e);
						e.printStackTrace();
					}
				}
				Intent i = new Intent(context, DebugActivity.class);
				i.putExtra("debug", debugMessage);
				context.startActivity(i);
			}
		});
	}

	private String convertStacktraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return "\n\nGot Error: " + e.getMessage() + "\n" + sw.toString() + "\n\n";
	}
}
