package de.bulling.barcodebuddyscanner.Helper;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import de.bulling.barcodebuddyscanner.Api.BBApi;
import de.bulling.barcodebuddyscanner.Api.BBApiCallback;
import de.bulling.barcodebuddyscanner.R;

public class ApiConnection {


    private final BBApi bbApi;
    private final Context context;
    private final BeepManager beepManager;
    private final SharedPrefHelper prefHelper;
    private final boolean isDebug;


    public ApiConnection(Activity activity, boolean isDebug) {
        this.context = activity;
        this.isDebug = isDebug;
        this.prefHelper = new SharedPrefHelper(context);
        this.bbApi = prefHelper.initBBApi();

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

    private String modeIntToStr(int input) {
        switch (input) {
            case 0:
                return "Consume";
            case 1:
                return "Consume (spoiled)";
            case 2:
                return "Purchase";
            case 3:
                return "Open";
            case 4:
                return "Inventory";
            case 5:
                return "Add to shoppinglist";
            case 6:
                return "Consume all";
            default:
                return "Invalid state";
        }
    }

    public void setMode(final int mode) {
        this.beep();
        bbApi.setMode(mode, new BBApiCallback() {
            @Override
            public void onResult(Object result) {
                if (result instanceof String)
                    Toast.makeText(context, "Mode set to: \"" + modeIntToStr(mode) + "\"", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, R.string.error_unex, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(int errorCode, String errorMessage, Integer statusCode) {
                Toast.makeText(context, context.getString(R.string.error) + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void processBarcode(String barcode) {
        this.beep();
        processBarcodeInternal(barcode);
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
            public void onError(int errorCode, String errorMessage, Integer statusCode) {
                Toast.makeText(context, context.getString(R.string.error) + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
