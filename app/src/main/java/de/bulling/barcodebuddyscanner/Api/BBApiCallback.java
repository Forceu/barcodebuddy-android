package de.bulling.barcodebuddyscanner.Api;

import com.google.gson.JsonElement;

import retrofit2.Response;

public interface BBApiCallback {

	int ERROR_NETWORK      = 0;
	int ERROR_UNAUTHORIZED = 1;
	int ERROR_OTHER        = 2;

	void onResult(Object result);

	void onError(int errorCode, String errorMessage, Response<JsonElement> response);

}
