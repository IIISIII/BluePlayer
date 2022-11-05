package com.lllslll.blueplayer_mk2.service;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;

import java.util.*;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.*;
import android.support.v4.media.MediaBrowserCompat.*;
import android.support.v4.media.MediaBrowserServiceCompat.*;

import com.lllslll.blueplayer_mk2.*;
import com.lllslll.blueplayer_mk2.file.*;
import com.lllslll.blueplayer_mk2.util.*;
import android.widget.*;


public class MusicPlayerService extends MediaBrowserServiceCompat implements MusicPlayer.OnMusicPlayerListener, MediaButtonSupporter.Callback
{
	private static final int ID = 7;
	
	private static final String TAG = "blueplayerSession";
	
	private boolean isStarted = false;
	
	private MusicPlayer player = null;
	
	private Resources res = null;
	
	private PendingIntent[] playerPending = new PendingIntent[5];
	
	private NotificationCompat.Action[] playerAction = new NotificationCompat.Action[5];
	
	private AudioSupporter audioSupporter = null;
	
	private MediaButtonSupporter mediaButtonSupporter = null;
	
	
	public class Action
	{
		public static final String PREVIOUS = "previous";
		public static final String NEXT = "next";
		public static final String PLAY = "play";
		public static final String PAUSE = "pause";
		public static final String PLAY_PAUSE = "play/pause";
		public static final String LOOP = "loop";
		public static final String EXIT = "exit";
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		this.res = this.getResources();
		
		this.player = MusicPlayer.getInstance();
		this.player.addListener(this, true);
		
		if(this.player.getMusicFile() == null)
			PlayerDataManager.readPlayerState(this, this.player);
		
		this.audioSupporter = new AudioSupporter(this, this.player)
		{
			@Override
			public void onLostFocus(boolean resumeOnFocusGain)
			{
				super.onLostFocus(resumeOnFocusGain);
				
				if(player.isPlaying())
					mediaButtonSupporter.pause();
				
				mediaButtonSupporter.disableMediaButtonEvent();
			}

			@Override
			public void onGainedFocus(boolean resume)
			{
				super.onGainedFocus(resume);
				
				mediaButtonSupporter.enableMediaButtonEvent();
				
				if(resume)
					mediaButtonSupporter.play();
			}
			
			@Override
			public void onHeadsetDisableEvent()
			{
				mediaButtonSupporter.pause();
			}
		};
	}

	@Override
	public IBinder onBind(Intent i)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		String action = intent.getAction();
		
		if(this.mediaButtonSupporter == null || this.mediaButtonSupporter.isReleased())
			this.mediaButtonSupporter = new MediaButtonSupporter(this, this.player, this, TAG);
		if(!this.mediaButtonSupporter.isEnabled())
			this.mediaButtonSupporter.enableMediaButtonEvent();
		this.mediaButtonSupporter.updateState(this.player.isPlaying());
		
		this.mediaButtonSupporter.handleIntent(intent);
		
		if(action != null) {
			switch(action) {
				case Action.PLAY :
					this.mediaButtonSupporter.play();
					break;
				case Action.PAUSE :
					this.mediaButtonSupporter.pause();
					break;
				case Action.PLAY_PAUSE :
					this.mediaButtonSupporter.playPause();
					break;
				case Action.PREVIOUS :
					this.mediaButtonSupporter.skipToPrevious();
					break;
				case Action.NEXT :
					this.mediaButtonSupporter.skipToNext();
					break;
				case Action.LOOP :
					this.player.setLooping(!this.player.isLooping());
					break;
				case Action.EXIT :
					this.stopService();
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		this.isStarted = false;
		
		MediaNotificationHelper.removeChannel(this);
		this.audioSupporter.release();
		this.mediaButtonSupporter.release();
		
		this.player.removeListener(this);
		this.player.pause();
		
		PlayerDataManager.savePlayerState(this, this.player);
		
		super.onDestroy();
	}

	@Override
	public MediaBrowserServiceCompat.BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints)
	{
		try {
			if(clientPackageName.equals(this.getPackageName()))
				return new BrowserRoot(this.getString(R.string.app_name), null);
		} catch(Exception err) {}
		return null;
	}

	@Override
	public void onLoadChildren(String parentId, Result<List<MediaItem>> result)
	{
		result.sendResult(null);
	}
	
	//MusicPlayer.OnMusicPlayerListener-----------------------

	@Override
	public void onPrepared(MusicPlayer player)
	{
		PlayerDataManager.savePlayerState(this, player);
	}

	@Override
	public void onCompletion(MusicPlayer player)
	{
		this.mediaButtonSupporter.skipToNext();
	}

	@Override
	public void onPlayStateChanged(MusicPlayer player, boolean isPlaying)
	{
		this.mediaButtonSupporter.release();
		
		if(isPlaying) {
			this.audioSupporter.requestFocus();
			this.audioSupporter.enableHeadsetDisableEvent();
		}
		else {
			this.audioSupporter.abandonFocus();
			this.audioSupporter.disableHeadsetDisableEvent();
		}
		
		if(this.mediaButtonSupporter == null || this.mediaButtonSupporter.isReleased())
			this.mediaButtonSupporter = new MediaButtonSupporter(this, this.player, this, TAG);
		
		if(!this.mediaButtonSupporter.isEnabled())
			this.mediaButtonSupporter.enableMediaButtonEvent();
		
		this.mediaButtonSupporter.updateState(isPlaying);
		
		PlayerDataManager.savePlayerState(this, player);
	}

	@Override
	public void onLoopStateChanged(MusicPlayer player, boolean isLooping)
	{
		this.updateNotification(player.isPlaying());
		
		PlayerDataManager.savePlayerState(this, player);
	}

	@Override
	public void onError(MusicPlayer player, int what, int extra)
	{
		this.stopService();
	}
	
	//MediaButtonSupporter.Callback-----------------
	
	@Override
	public void onPlayEvent()
	{
		this.updateNotification(true);
		
		this.player.play();
	}

	@Override
	public void onPauseEvent()
	{
		this.updateNotification(false);
		
		this.player.pause();
	}

	@Override
	public void onSkipToPreviousEvent()
	{
		this.moveTo(-1);
		
		this.updateNotification(true);
	}

	@Override
	public void onSkipToNextEvent()
	{
		this.moveTo(1);
		
		this.updateNotification(true);
	}
	
	//fuctions--------------------------------------

	private void moveTo(int i)
	{
		MusicPlayList.moveTo(i);
		MusicFile mf = MusicPlayList.getCurrentMusicFile();
		if(mf == null) {
			this.stopService();
			return;
		}
		this.player.setMusicFile(mf);
		this.player.setProgress(0);
		this.player.play();
	}

	public void stopService()
	{
		this.stopForeground(true);
		this.stopSelf();
	}
	
	private void updateNotification(boolean isPlaying)
	{
		if(this.isStarted)
			NotificationManagerCompat.from(this).notify(ID, this.getNotification(isPlaying));
		else {
			this.startForeground(ID, this.getNotification(isPlaying));
			this.isStarted = true;
		}
	}
	
	private Notification getNotification(boolean isPlaying)
	{
		NotificationCompat.Builder builder = MediaNotificationHelper.from(this, this.player);
		
		Intent preIntent = new Intent(this, MusicPlayerService.class);
		preIntent.setAction(Action.PREVIOUS);

		Intent playIntent = new Intent(this, MusicPlayerService.class);
		playIntent.setAction(Action.PLAY_PAUSE);

		Intent nextIntent = new Intent(this, MusicPlayerService.class);
		nextIntent.setAction(Action.NEXT);

		Intent loopIntent = new Intent(this, MusicPlayerService.class);
		loopIntent.setAction(Action.LOOP);

		Intent exitIntent = new Intent(this, MusicPlayerService.class);
		exitIntent.setAction(Action.EXIT);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			this.playerPending[0] = PendingIntent.getForegroundService(this, 0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.playerPending[1] = PendingIntent.getForegroundService(this, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.playerPending[2] = PendingIntent.getForegroundService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.playerPending[3] = PendingIntent.getForegroundService(this, 3, loopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.playerPending[4] = PendingIntent.getForegroundService(this, 4, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		else {
			this.playerPending[0] = PendingIntent.getService(this, 0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.playerPending[1] = PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.playerPending[2] = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.playerPending[3] = PendingIntent.getService(this, 3, loopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			this.playerPending[4] = PendingIntent.getService(this, 4, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		this.playerAction[0] = new NotificationCompat.Action(R.drawable.notification_rewind_button, "previous", this.playerPending[0]);
		this.playerAction[1] = new NotificationCompat.Action(isPlaying ? R.drawable.notification_pause_button : R.drawable.notification_play_button, "play/pause", this.playerPending[1]);
		this.playerAction[2] = new NotificationCompat.Action(R.drawable.notification_fast_forward_button, "next", this.playerPending[2]);
		this.playerAction[3] = new NotificationCompat.Action(this.player.isLooping() ? R.drawable.notification_repeat_button : R.drawable.notification_no_repeat_button, "loop", this.playerPending[3]);
		this.playerAction[4] = new NotificationCompat.Action(R.drawable.notification_exit_button, "exit", this.playerPending[4]);
		
		for(int a = 0, l = this.playerPending.length; a < l; a++)
			builder.addAction(this.playerAction[a]);
		
		return builder.build();
	}
}
