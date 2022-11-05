package com.lllslll.blueplayer_mk2.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import android.support.v4.app.NotificationCompat;

import com.lllslll.blueplayer_mk2.PlayerActivity;
import com.lllslll.blueplayer_mk2.R;
import com.lllslll.blueplayer_mk2.file.MusicFile;


public class MediaNotificationHelper
{
	private static boolean isChannelEnabled = false;
	
	private static final String channelId = "BluePlayerChannel";
	private static final String channelName = "BluePlayer";
	private static final String channelDescription = "BluePlayer Notification Channel";
	
	public static String getChannelId(Context c)
	{
		if(isChannelEnabled)
			return channelId;
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager manager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
			channel.setDescription(channelDescription);
			channel.enableLights(false);
			channel.enableVibration(false);
			channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			channel.setShowBadge(false);
			manager.createNotificationChannel(channel);
			
			isChannelEnabled = true;
		}
		
		return channelId;
	}
	
	public static void removeChannel(Context c)
	{
		if(!isChannelEnabled)
			return;
		
		NotificationManager manager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.deleteNotificationChannel(channelId);
		
		isChannelEnabled = false;
	}
	
	public static NotificationCompat.Builder from(Context c, MusicPlayer p)
	{
		MusicFile mf = p.getMusicFile();
		if(mf == null)
			return null;
		
		android.support.v4.media.app.NotificationCompat.MediaStyle style = new android.support.v4.media.app.NotificationCompat.MediaStyle();
		style.setShowActionsInCompactView(0, 1, 2);
		
		Intent mainIntent = new Intent(c, PlayerActivity.class);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
		stackBuilder.addNextIntentWithParentStack(mainIntent);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(c, getChannelId(c));
		
		builder
		 .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
		 .setSmallIcon(R.drawable.mp_icon)
		 .setOngoing(true)
		 .setAutoCancel(false)
		 .setVibrate(null)
		 .setColorized(true)
		 .setPriority(NotificationCompat.PRIORITY_HIGH)
		 .setColor(c.getResources().getColor(R.color.notification_color))
		 .setStyle(style)
		 .setContentTitle(mf.getTitle())
		 .setContentText(mf.getArtist())
		 .setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
		
		Bitmap thum = mf.getThumbnail();
		if(thum == null)
			thum = BitmapFactory.decodeResource(c.getResources(), R.drawable.empty_cover);
		builder.setLargeIcon(thum);
		
		return builder;
	}
}
