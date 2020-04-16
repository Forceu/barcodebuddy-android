package de.bulling.barcodebuddyscanner.Api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BBApi {

	private final BBService bbApi;
	private final String    apiKey;

	private final static int REQUEST_SYSTEM_INFO    = 0;
	private final static int REQUEST_ACTION_BARCODE = 1;

	public BBApi(final String url, final String apiKey) {
		Retrofit retrofit = new Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(url)
				.build();
		this.apiKey = apiKey;
		this.bbApi  = retrofit.create(BBService.class);
	}

	private void processResponse(final int request, final Call<JsonElement> result, final BBApiCallback callback) {
		result.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (response.isSuccessful()) {
					Object result = null;
					try {
						JsonObject responseBody = response.body().getAsJsonObject();
						switch (request) {
							case REQUEST_SYSTEM_INFO:
								result = (Integer) responseBody.get("data").getAsJsonObject().get("version_int").getAsInt();
								break;
							case REQUEST_ACTION_BARCODE:
								result = (String) responseBody.get("data").getAsJsonObject().get("result").getAsString();
								break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						callback.onError(BBApiCallback.ERROR_OTHER, e.getMessage(), response);
					}
					callback.onResult(result);
				} else {
					switch (response.raw().code()) {
						case 401:
							callback.onError(BBApiCallback.ERROR_UNAUTHORIZED, "Invalid API key", response);
							break;
						case 404:
							callback.onError(BBApiCallback.ERROR_OTHER, "Invalid URL. Please make sure that the URL is correct and URL rewriting enabled.", response);
							break;
						case 500:
							callback.onError(BBApiCallback.ERROR_OTHER, "Server error", response);
							break;
						default:
							callback.onError(BBApiCallback.ERROR_OTHER, "Unknown error occurred. Please check URL.", response);
							break;
					}
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				if (t instanceof IOException) {
					callback.onError(BBApiCallback.ERROR_NETWORK, t.getMessage(), null);
				} else {
					callback.onError(BBApiCallback.ERROR_OTHER, t.getMessage(), null);
				}

			}
		});
	}

	public void getVersionInfo(final BBApiCallback callback) {
		processResponse(REQUEST_SYSTEM_INFO, this.bbApi.getSystemInfo(this.apiKey), callback);
	}

	public void postBarcode(String barcode, final BBApiCallback callback) {
		processResponse(REQUEST_ACTION_BARCODE, this.bbApi.postBarcode(this.apiKey, barcode), callback);
	}



	public void postBarcodeDebug(String barcode, final BBApiCallback callback) {
		this.bbApi.postBarcodeDebug(this.apiKey, barcode).enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				callback.onResult(response);
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				int x=0;
			}
		});
	}
}
