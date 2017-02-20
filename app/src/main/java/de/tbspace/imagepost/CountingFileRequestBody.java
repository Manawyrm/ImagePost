package de.tbspace.imagepost;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class CountingFileRequestBody extends RequestBody
{

	private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

	private final Context cnx;
	private final Uri url;
	private final ProgressListener listener;
	private final String contentType;
	private final AssetFileDescriptor fd;

	public CountingFileRequestBody(Context cnx, Uri url, String contentType, ProgressListener listener) throws FileNotFoundException
	{
		this.cnx = cnx;
		this.url = url;
		this.contentType = contentType;
		this.listener = listener;

		this.fd = this.cnx.getContentResolver().openAssetFileDescriptor(this.url, "r");
	}

	@Override
	public long contentLength() {
		return fd.getLength();
	}

	@Override
	public MediaType contentType() {
		return MediaType.parse(contentType);
	}

	@Override
	public void writeTo(BufferedSink sink) throws IOException
	{
		Source source = null;
		try {
			source = Okio.source(fd.createInputStream());
			long total = 0;
			long read;

			while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
				total += read;
				sink.flush();
				this.listener.transferred(total, this.contentLength());

			}
		} finally {
			Util.closeQuietly(source);
		}
	}

	public interface ProgressListener {
		void transferred(long num, long size);
	}
}