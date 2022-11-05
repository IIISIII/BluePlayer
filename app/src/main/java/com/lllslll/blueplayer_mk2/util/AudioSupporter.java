package com.lllslll.blueplayer_mk2.util;

import android.content.*;
import android.media.*;
import android.media.session.*;
import android.os.*;

import com.lllslll.blueplayer_mk2.receiver.*;


public abstract class AudioSupporter implements AudioManager.OnAudioFocusChangeListener
{
	private boolean resume = false;
	
	private Context context = null;
	
	private AudioManager audioManager = null;
	
	private IntentFilter headsetDisableFilter = null;
	
	private AudioFocusRequest focusRequest = null;
	
	private MusicPlayer player = null;
	
	
	public AudioSupporter(Context c, MusicPlayer p)
	{
		this.context = c;
		
		this.audioManager = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
		
		this.player = p;
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			AudioAttributes attributes = new AudioAttributes.Builder()
			 .setUsage(AudioAttributes.USAGE_MEDIA)
			 .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
			 .build();
			this.focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
			 .setOnAudioFocusChangeListener(this)
			 .setAudioAttributes(attributes)
			 .setAcceptsDelayedFocusGain(true)
			 .setWillPauseWhenDucked(true)
			 .build();
		}
		
		this.headsetDisableFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		this.headsetDisableFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
	}

	@Override
	public void onAudioFocusChange(int focusChange)
	{
		switch(focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN :
				this.onGainedFocus(resume);
				break;
			case AudioManager.AUDIOFOCUS_LOSS :
				this.resume = false;
				this.onLostFocus(false);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT :
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK :
				this.resume = true;
				this.onLostFocus(true);
		}
	}
	
	public void requestFocus()
	{
		int res = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			res = this.audioManager.requestAudioFocus(this.focusRequest);
		else
			res = this.audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}
	
	public void abandonFocus()
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			this.audioManager.abandonAudioFocusRequest(this.focusRequest);
		else
			this.audioManager.abandonAudioFocus(this);
	}
	
	public void enableHeadsetDisableEvent()
	{
		this.context.registerReceiver(this.headsetDisableReceiver, this.headsetDisableFilter);
	}
	
	public void disableHeadsetDisableEvent()
	{
		this.context.unregisterReceiver(this.headsetDisableReceiver);
	}
	
	public void release()
	{
		this.abandonFocus();
		this.disableHeadsetDisableEvent();
	}
	
	public void onGainedFocus(boolean resumePlayer)
	{
		this.player.setFocus(true);
	}
	
	public void onLostFocus(boolean resumeOnFocusGain)
	{
		this.player.setFocus(false);
	}
	
	public abstract void onHeadsetDisableEvent();
	
	private BroadcastReceiver headsetDisableReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context c, Intent i)
		{
			String action = i.getAction();
			if(action == null)
				return;
			if(action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
				onHeadsetDisableEvent();
		}
	};
}
