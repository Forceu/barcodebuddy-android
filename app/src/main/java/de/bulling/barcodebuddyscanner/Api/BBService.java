package de.bulling.barcodebuddyscanner.Api;

import com.google.gson.JsonElement;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface BBService {

	@GET("system/info")
	Call<JsonElement> getSystemInfo(@Header("BBUDDY-API-KEY") String authorization);

	@FormUrlEncoded
	@POST("action/scan")
	Call<JsonElement> postBarcode(@Header("BBUDDY-API-KEY") String authorization, @Field("barcode") String barcode);



	@FormUrlEncoded
	@POST("action/scan")
	Call<ResponseBody> postBarcodeDebug(@Header("BBUDDY-API-KEY") String authorization, @Field("barcode") String barcode);

}
