package de.tbspace.imagepost;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.*;
import okhttp3.MultipartBody;
/**
 * Created by tobias on 19.02.17.
 */

public class Uploader
{
	private final OkHttpClient client = new OkHttpClient();

	public void uploadImage(Context cnx, Uri url, CountingFileRequestBody.ProgressListener progressListener, final ProgressFinished finishCallback) throws Exception
	{
		Log.d("ImagePost", url.toString());

		RequestBody requestBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("username", "tobi31061")
				.addFormDataPart("password", "mySuperSecurePassword")
				.addPart(
						Headers.of("Content-Disposition", "form-data; name=\"image\"; filename=\"filename.jpg\""),
						new CountingFileRequestBody(cnx, url, "image/*", progressListener))
				.build();

		Request request = new Request.Builder()
				.url("https://screenshot.tbspace.de/uploadImagePost.php")
				.post(requestBody)
				.build();

		client.newCall(request).enqueue(new Callback() {
			@Override public void onFailure(Call call, IOException e) {
				e.printStackTrace();
			}

			@Override public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

				finishCallback.finished(response.body().string(),response.headers());

			}
		});
	}
	public byte[] readBytes(InputStream inputStream) throws IOException {
		// this dynamically extends to take the bytes you read
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		// this is storage overwritten on each iteration with bytes
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		// we need to know how may bytes were read to write them to the byteBuffer
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}

		// and then we can return your byte array.
		return byteBuffer.toByteArray();
	}
	public interface ProgressFinished {
		void finished(String body, Headers headers);
	}


}
