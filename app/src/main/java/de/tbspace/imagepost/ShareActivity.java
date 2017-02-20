package de.tbspace.imagepost;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import okhttp3.Headers;

public class ShareActivity extends AppCompatActivity
{
	static int notificationID = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		final Context ctx = this;
		if (Intent.ACTION_PASTE.equals(action))
		{
			String imageURL = intent.getStringExtra("imageURL");
			Log.d("ImagePost", imageURL);
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText(imageURL, imageURL);
			clipboard.setPrimaryClip(clip);
			this.finish();
		}
		if (Intent.ACTION_SEND.equals(action) && type != null)
		{
			if (type.startsWith("image/") || type.startsWith("video/"))
			{
				final Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

				if (imageUri != null)
				{
					Uploader ul = new Uploader();
					try
					{
						final NotificationManager mNotifyManager =
								(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

						mBuilder.setContentTitle("Upload läuft")
								.setContentText("...")
								.setSmallIcon(R.drawable.ic_imagepostalpha);

						notificationID++;

						ul.uploadImage(getApplicationContext(), imageUri, new CountingFileRequestBody.ProgressListener()
						{
							@Override
							public void transferred(long num, long size)
							{
								int progress = (int) (((float) num / (float) size) * 1000);
								mBuilder.setProgress(1000, progress, false);
								mNotifyManager.notify(notificationID, mBuilder.build());
							}
						}, new Uploader.ProgressFinished()
						{
							@Override
							public void finished(String body, Headers headers)
							{
								Log.d("ImagePost", body);

								Intent resultIntent = new Intent(Intent.ACTION_PASTE, Uri.parse(body) ,ctx, ShareActivity.class);
								resultIntent.putExtra("imageURL", body);
								final PendingIntent resultPendingIntent =
										PendingIntent.getActivity(
												ctx,
												0,
												resultIntent,
												PendingIntent.FLAG_UPDATE_CURRENT
										);


								mBuilder.setContentTitle("Upload erfolgreich")
										.setProgress(0,0,false)
										.setContentIntent(resultPendingIntent);

								mNotifyManager.notify(notificationID, mBuilder.build());
							}
						});
					} catch (Exception e)
					{
						Log.d("ImagePost", e.toString());
					}
				}
			}
		}

		this.finish();
	}

}
