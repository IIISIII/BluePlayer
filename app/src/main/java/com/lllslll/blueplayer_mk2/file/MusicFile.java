package com.lllslll.blueplayer_mk2.file;

import android.graphics.*;
import android.media.*;

import java.io.*;


public class MusicFile extends FileData
{
	private FileData parentData = null;
	
	private int duration = -1;
	
	private String title, artist;
	
	public MusicFile(String path, FileData parentData)
	{
		super(path);
		
		this.parentData = parentData;
		
		MediaMetadataRetriever mtr = new MediaMetadataRetriever();
		mtr.setDataSource(path);
		
		this.title = mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		if(this.title == null)
			this.title = this.getName();
		this.artist = mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		if(this.artist == null)
			this.artist = "unknown";
	}
	
	public MusicFile(File f, FileData parentData)
	{
		super(f);
		
		this.parentData = parentData;
		
		MediaMetadataRetriever mtr = new MediaMetadataRetriever();
		mtr.setDataSource(this.getPath());

		this.title = mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		if(this.title == null)
			this.title = this.getName();
		this.artist = mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		if(this.artist == null)
			this.artist = "unknown";
		this.duration = Integer.parseInt(mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
	}
	
	private MusicFile(String path, String title, String artist, FileData parentData)
	{
		super(path, title);

		if(title == null || artist == null) {
			MediaMetadataRetriever mtr = new MediaMetadataRetriever();
			mtr.setDataSource(path);

			if(title == null) {
				this.title = mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				if (this.title == null) {
					File f = new File(path);
					this.title = f.getName();
				}
				if (this.title == null) {
					this.title = "unknown";
				}
			}
			this.name = this.title;

			if(artist == null) {
				this.artist = mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				if (this.artist == null)
					this.artist = "unknown";
			}
		}
		else {
			this.title = title;
			this.artist = artist;
		}
		this.parentData = parentData;
	}
	
	public String getTitle()
	{
		try {
			if(this.title == null) {
				MediaMetadataRetriever mtr = new MediaMetadataRetriever();
				mtr.setDataSource(this.getPath());

				this.title = mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				if(this.title == null) {
					if(this.getName() != null)
						this.title = this.getName();
					else {
						File f = new File(this.getPath());
						this.title = f.getName();
					}
				}
			}
			return this.title;
		} catch(Exception err) {}
		return "";
	}
	
	public String getArtist()
	{
		try {
			if(this.artist == null) {
				MediaMetadataRetriever mtr = new MediaMetadataRetriever();
				mtr.setDataSource(this.getPath());

				this.artist = mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				if(this.artist == null)
					this.artist = "unknown";
			}
			return this.artist;
		} catch(Exception err) {}
		return "";
	}
	
	public FileData getParentData()
	{
		return this.parentData;
	}
	
	public int getDuration()
	{
		try {
			if(this.duration == -1) {
				MediaMetadataRetriever mtr = new MediaMetadataRetriever();
				mtr.setDataSource(this.getPath());
				this.duration = Integer.parseInt(mtr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			}
			return this.duration;
		} catch(Exception err) {}
		return 0;
	}
	
	public Bitmap getThumbnail()
	{
		try {
			MediaMetadataRetriever mtr = new MediaMetadataRetriever();
			mtr.setDataSource(this.getPath());
			byte[] thumb = mtr.getEmbeddedPicture();
			if(thumb == null)
				return null;
			return BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
		} catch(Exception err) {}
		return null;
	}
	
	public static String durationToTimetext(int dur)
	{
		int mi = ((dur / 1000) % 3600) / 60;
		int se = ((dur / 1000) % 3600) % 60;
		return (mi < 10 ? "0" + mi : mi) + ":" + (se < 10 ? "0" + se : se);
	}
	
	public static MusicFile createMusicFile(String path, String title, String artist, FileData parentData)
	{
		return new MusicFile(path, title, artist, parentData);
	}
}
