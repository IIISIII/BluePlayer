package com.lllslll.blueplayer_mk2.util;

import android.app.PendingIntent;
import android.content.*;
import android.os.RemoteException;

import android.support.v4.media.session.*;

import com.lllslll.blueplayer_mk2.service.MusicPlayerService;


public class MediaButtonSupporter
{
	private final String TAG;
	
	private boolean isReleased = false;
	
	private boolean isEnabled = false;
	
	private Context context = null;
	
	private MediaSessionCompat session = null;
	
	private MusicPlayer player = null;
	
	private Callback callback = null;
	
	public MediaButtonSupporter(Context c, MusicPlayer p, Callback cb, String tag)
	{
		TAG = tag;

		this.context = c;

		this.player = p;

		this.callback = cb;


		ComponentName mediaButtonReceiver = new ComponentName(c.getApplicationContext(), MediaButtonReceiver.class);
		this.session = new MediaSessionCompat(c.getApplicationContext(), TAG, mediaButtonReceiver, null);

		this.session.setCallback(this.sessionCallback);
		this.session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS|MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setClass(c, MediaButtonReceiver.class);
		PendingIntent pend = PendingIntent.getBroadcast(c, 0, mediaButtonIntent, 0);
		this.session.setMediaButtonReceiver(pend);
	}
	
	public MediaButtonSupporter(Context c, MusicPlayer p, Callback cb)
	{
		this(c, p, cb, "mediaButtonSupporter");
	}
	
	public void updateState(boolean isPlaying)
	{
		if(this.isReleased)
			return;
		
		PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
		if(isPlaying) {
			stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
			stateBuilder.setActions(PlaybackStateCompat.ACTION_PAUSE|PlaybackStateCompat.ACTION_PLAY_PAUSE|PlaybackStateCompat.ACTION_SKIP_TO_NEXT|PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
		}
		else {
			stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
			stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY|PlaybackStateCompat.ACTION_PLAY_PAUSE|PlaybackStateCompat.ACTION_SKIP_TO_NEXT|PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
		}
		this.session.setPlaybackState(stateBuilder.build());
	}
	
	public void enableMediaButtonEvent()
	{
		if(this.isReleased)
			return;
		
		this.session.setActive(true);
		
		this.isEnabled = true;
	}
	
	public void disableMediaButtonEvent()
	{
		if(this.isReleased)
			return;
		
		this.session.setActive(false);
		
		this.isEnabled = false;
	}
	
	public void release()
	{
		if(this.session != null) {
			this.session.setActive(false);
			this.session.release();
			this.session = null;
		}
		
		this.isEnabled = false;
		this.isReleased = true;
	}
	
	public void play()
	{
		if(this.isReleased || !this.isEnabled)
			return;
		this.getController().getTransportControls().play();
	}
	
	public void pause()
	{
		if(this.isReleased || !this.isEnabled)
			return;
		this.getController().getTransportControls().pause();
	}
	
	public void playPause()
	{
		if(this.isReleased || !this.isEnabled)
			return;
		MediaControllerCompat controller = this.getController();
		if(controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING)
			controller.getTransportControls().pause();
		else
			controller.getTransportControls().play();
	}
	
	public void skipToPrevious()
	{
		if(this.isReleased || !this.isEnabled)
			return;
		this.getController().getTransportControls().skipToPrevious();
	}
	
	public void skipToNext()
	{
		if(this.isReleased || !this.isEnabled)
			return;
		this.getController().getTransportControls().skipToNext();
	}
	
	public void handleIntent(Intent i)
	{
		if(this.isReleased || !this.isEnabled)
			return;
		MediaButtonReceiver.handleIntent(this.session, i);
	}
	
	public boolean isEnabled()
	{
		return this.isEnabled;
	}
	
	public boolean isReleased()
	{
		return this.isReleased;
	}
	
	public MediaControllerCompat getController()
	{
		try {
			return new MediaControllerCompat(this.context, this.session.getSessionToken());
		} catch(RemoteException err) {}
		return null;
	}
	
	private MediaSessionCompat.Callback sessionCallback = new MediaSessionCompat.Callback()
	{
		@Override
		public void onPlay()
		{
			super.onPlay();
			callback.onPlayEvent();
		}

		@Override
		public void onPause()
		{
			super.onPause();
			callback.onPauseEvent();
		}

		@Override
		public void onSkipToPrevious()
		{
			super.onSkipToPrevious();
			callback.onSkipToPreviousEvent();
		}

		@Override
		public void onSkipToNext()
		{
			super.onSkipToNext();
			callback.onSkipToNextEvent();
		}
	};
	
	public interface Callback
	{
		public void onPlayEvent();
		public void onPauseEvent();
		public void onSkipToNextEvent();
		public void onSkipToPreviousEvent();
	}
}
