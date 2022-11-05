package com.lllslll.blueplayer_mk2.util;

import android.media.*;

import java.io.*;
import java.util.*;

import com.lllslll.blueplayer_mk2.file.*;
import android.util.*;
import android.content.*;
import android.widget.*;

public class MusicPlayer extends MediaPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener
{
	public interface OnMusicPlayerListener
	{
		public void onPrepared(MusicPlayer player);
		public void onCompletion(MusicPlayer player);
		public void onError(MusicPlayer player, int what, int extra);
		public void onPlayStateChanged(MusicPlayer player, boolean isPlaying);
		public void onLoopStateChanged(MusicPlayer player, boolean isLooping);
	}
	
	private boolean isPreparing = false, isLooping = false, isPrepared = false;
	
	private boolean isHaveFocus = false;
	
	private int currentPosition = 0;
	
	private float volume = 1.0f;
	
	private MusicFile musicFile = null;
	
	private ArrayList<OnMusicPlayerListener> listenerGroup = new ArrayList<>();
	
	
	private static MusicPlayer instance = null;
	
	public static MusicPlayer getInstance()
	{
		if(instance == null)
			instance = new MusicPlayer();
		return instance;
	}
	
	@Override
	public void onPrepared(MediaPlayer player)
	{
		this.isPreparing = false;
		this.isPrepared = true;
		
		this.setVolume(this.volume);
		super.setLooping(this.isLooping);
		this.seekTo(this.currentPosition);
		this.start();
		
		for(int a = 0, l = this.listenerGroup.size(); a < l; a ++)
			this.listenerGroup.get(a).onPrepared(this);
	}

	@Override
	public void onCompletion(MediaPlayer player)
	{
		this.isPreparing = false;
		this.isPrepared = false;
		
		this.currentPosition = 0;
		
		for(int a = 0, l = this.listenerGroup.size(); a < l; a ++)
			this.listenerGroup.get(a).onCompletion(this);
	}

	@Override
	public boolean onError(MediaPlayer player, int what, int extra)
	{
		this.isPreparing = false;
		this.isPrepared = false;
		
		for(int a = 0, l = this.listenerGroup.size(); a < l; a ++)
			this.listenerGroup.get(a).onError(this, what, extra);
			
		return false;
	}

	@Override
	public void start() throws IllegalStateException
	{
		super.start();
		
		for(int a = 0, l = this.listenerGroup.size(); a < l; a ++)
			this.listenerGroup.get(a).onPlayStateChanged(this, true);
	}

	@Override
	public void stop() throws IllegalStateException
	{
		super.stop();
		
		for(int a = 0, l = this.listenerGroup.size(); a < l; a ++)
			this.listenerGroup.get(a).onPlayStateChanged(this, false);
	}

	@Override
	public void pause() throws IllegalStateException
	{
		super.pause();
		
		for(int a = 0, l = this.listenerGroup.size(); a < l; a ++)
			this.listenerGroup.get(a).onPlayStateChanged(this, false);
	}
	
	@Override
	public void prepare()
	{
		if(this.musicFile == null)
			return;
		if(this.isPlaying())
			super.stop();
		this.reset();
		try {
			this.setOnPreparedListener(this);
			this.setOnCompletionListener(this);
			this.setOnErrorListener(this);

			this.isPreparing = true;
			this.isPrepared = false;

			this.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this.setDataSource(this.musicFile.getPath());
			this.prepareAsync();
		} catch(IOException err) {
			throw new Error(err.getMessage());
		}
	}
	
	@Override
	public void setLooping(boolean looping)
	{
		this.isLooping = looping;
		super.setLooping(looping);

		for(int a = 0, l = this.listenerGroup.size(); a < l; a ++)
			this.listenerGroup.get(a).onLoopStateChanged(this, looping);
	}

	@Override
	public boolean isLooping()
	{
		return this.isLooping;
	}
	
	public void play()
	{
		if(this.isPreparing)
			return;
		if(!this.isPrepared)
			this.prepare();
		else
			this.start();
	}
	
	public void setMusicFile(MusicFile m)
	{
		this.isPrepared = false;
		this.musicFile = m;
	}
	
	public void setVolume(float v)
	{
		this.volume = v;
		if(!this.isPreparing && this.isPrepared)
			super.setVolume(v, v);
	}
	
	public float getVolume()
	{
		return this.volume;
	}
	
	public void duckVolume()
	{
		super.setVolume(0.1f, 0.1f);
	}
	
	public void resetVolume()
	{
		super.setVolume(this.volume, this.volume);
	}
	
	public void addListener(OnMusicPlayerListener listener, boolean highPriority)
	{
		if(this.listenerGroup.indexOf(listener) >= 0)
			return;
		if(highPriority)
			this.listenerGroup.add(0, listener);
		else
			this.listenerGroup.add(listener);
	}
	
	public void removeListener(OnMusicPlayerListener listener)
	{
		if(this.listenerGroup.indexOf(listener) < 0)
			return;
		this.listenerGroup.remove(listener);
	}
	
	public void clearListener()
	{
		this.listenerGroup.clear();
	}
	
	public void setProgress(int progress)
	{
		this.currentPosition = progress;
		if(!this.isPreparing)
			this.seekTo(progress);
	}
	
	public int getProgress()
	{
		if(!this.isPrepared || this.isPreparing())
			return this.currentPosition;
		return this.getCurrentPosition();
	}
	
	public boolean isPreparing()
	{
		return this.isPreparing;
	}
	
	public MusicFile getMusicFile()
	{
		return this.musicFile;
	}
	
	public void setFocus(boolean focus)
	{
		this.isHaveFocus = focus;
	}
	
	public boolean isHaveFocus()
	{
		return this.isHaveFocus;
	}
}
