package de.bulling.barcodebuddyscanner.Api;

public interface BBApiCallback {

	int ERROR_NETWORK      = 0;
	int ERROR_UNAUTHORIZED = 1;
	int ERROR_OTHER        = 2;

	void onResult(Object result);

	void onError(int errorCode, String errorMessage, Integer statusCode);
}