package de.bulling.barcodebuddyscanner.Api;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class BBApi {

	private final String baseUrl;
	private final String apiKey;
	private final boolean isUnsafe;

	private final ExecutorService executor = Executors.newFixedThreadPool(4);
	private final Handler mainHandler = new Handler(Looper.getMainLooper());

	private static final int REQUEST_SYSTEM_INFO = 0;
	private static final int REQUEST_ACTION_BARCODE = 1;
	private static final int REQUEST_SET_MODE = 2;

	public BBApi(final String url, final String apiKey, final boolean isUnsafe) {
		String formattedUrl = url != null ? url.trim() : "";
		if (!formattedUrl.endsWith("/")) {
			formattedUrl += "/";
		}
		this.baseUrl = formattedUrl;
		this.apiKey = apiKey;
		this.isUnsafe = isUnsafe;
	}


	private void executeRequest(final int requestType, final String endpoint, final String method,
	                            final Map<String, String> params, final BBApiCallback callback) {
		executor.execute(() -> {
			HttpURLConnection connection = null;
			try {
				String fullUrl = baseUrl + endpoint;

				URL url = new URL(fullUrl);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod(method);
				connection.setConnectTimeout(15000);
				connection.setReadTimeout(15000);

				// Set API Key Header
				if (apiKey != null) {
					connection.setRequestProperty("BBUDDY-API-KEY", apiKey);
				}

				// Apply Unsafe SSL Configuration if requested
				if (isUnsafe && connection instanceof HttpsURLConnection) {
					UnsafeSslHelper.applyUnsafeSsl((HttpsURLConnection) connection);
				}

				// Handle Form-URL-Encoded POST body
				if ("POST".equalsIgnoreCase(method) && params != null && !params.isEmpty()) {
					connection.setDoOutput(true);
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

					StringBuilder postData = new StringBuilder();
					for (Map.Entry<String, String> param : params.entrySet()) {
						if (postData.length() != 0) postData.append('&');
						postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
						postData.append('=');
						postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
					}

					byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
					try (OutputStream os = connection.getOutputStream()) {
						os.write(postDataBytes);
					}
				}

				final int statusCode = connection.getResponseCode();

				if (statusCode >= 200 && statusCode < 300) {
					InputStream is = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
					StringBuilder responseBuilder = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						responseBuilder.append(line);
					}
					reader.close();

					JsonElement jsonElement = JsonParser.parseString(responseBuilder.toString());
					JsonObject responseBody = jsonElement.getAsJsonObject();

					Object result = null;
					switch (requestType) {
						case REQUEST_SYSTEM_INFO:
							result = responseBody.get("data").getAsJsonObject().get("version_int").getAsInt();
							break;
						case REQUEST_SET_MODE:
							result = responseBody.get("result").getAsJsonObject().get("result").getAsString();
							break;
						case REQUEST_ACTION_BARCODE:
							result = responseBody.get("data").getAsJsonObject().get("result").getAsString();
							break;
					}

					final Object finalResult = result;
					mainHandler.post(() -> callback.onResult(finalResult));

				} else {
					mainHandler.post(() -> {
						switch (statusCode) {
							case 400:
								callback.onError(BBApiCallback.ERROR_OTHER, "Illegal API Parameter", statusCode);
								break;
							case 401:
								callback.onError(BBApiCallback.ERROR_UNAUTHORIZED, "Invalid API key", statusCode);
								break;
							case 404:
								callback.onError(BBApiCallback.ERROR_OTHER, "Invalid URL or incorrect response. Please make sure that the URL is correct and URL rewriting enabled.", statusCode);
								break;
							case 500:
								callback.onError(BBApiCallback.ERROR_OTHER, "Server error", statusCode);
								break;
							default:
								callback.onError(BBApiCallback.ERROR_OTHER, "Unknown error occurred. Please check URL.", statusCode);
								break;
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
				final String errorMessage = e.getMessage();
				mainHandler.post(() -> {
					if (e instanceof java.io.IOException) {
						callback.onError(BBApiCallback.ERROR_NETWORK, errorMessage, null);
					} else {
						callback.onError(BBApiCallback.ERROR_OTHER, errorMessage, null);
					}
				});
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		});
	}

	public void getVersionInfo(final BBApiCallback callback) {
		executeRequest(REQUEST_SYSTEM_INFO, "system/info", "GET", null, callback);
	}

	public void postBarcode(String barcode, final BBApiCallback callback) {
		Map<String, String> params = new HashMap<>();
		params.put("barcode", barcode);
		executeRequest(REQUEST_ACTION_BARCODE, "action/scan", "POST", params, callback);
	}

	public void setMode(int mode, final BBApiCallback callback) {
		Map<String, String> params = new HashMap<>();
		params.put("state", String.valueOf(mode));
		executeRequest(REQUEST_SET_MODE, "state/setmode", "POST", params, callback);
	}

}