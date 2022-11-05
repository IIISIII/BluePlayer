package com.lllslll.blueplayer_mk2.util;

import android.content.*;
import android.database.*;
import android.net.*;
import android.provider.MediaStore.*;

import java.util.*;

import com.lllslll.blueplayer_mk2.file.*;


public class MediaDataReader
{
	public static ArrayList<FileData> getMusicFileList(Context context, String ext)
	{
		ContentResolver cr = context.getContentResolver();

		Cursor mAudioCursor;
		String[] projection = {
			Audio.Media._ID,
			Audio.Media.DATA,
			Audio.Media.TITLE,
			Audio.Media.ARTIST
		};
		mAudioCursor = cr.query(Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
		ArrayList<FileData> list = new ArrayList<>();

		if(mAudioCursor.moveToFirst()) {
			do {
				int id = (int)mAudioCursor.getLong(mAudioCursor.getColumnIndex(Audio.Media._ID));
				String url = mAudioCursor.getString(mAudioCursor.getColumnIndex(Audio.Media.DATA));
				String title = mAudioCursor.getString(mAudioCursor.getColumnIndex(Audio.Media.TITLE));
				String artist = mAudioCursor.getString(mAudioCursor.getColumnIndex(Audio.Media.ARTIST));

				if(!url.endsWith(ext)) continue;

				list.add(MusicFile.createMusicFile(url, title, artist, null));
			} while(mAudioCursor.moveToNext());

			mAudioCursor.close();
		}

		Collections.sort(list);
		return list;
	}

	public static ArrayList<FileData> getPlaylist(Context context)
	{
		ArrayList<FileData> list = new ArrayList<>();

		ContentResolver cr = context.getContentResolver();
		String[] projection = {
			Audio.Playlists._ID,
			Audio.Playlists.NAME
		};
		Cursor mAudioCursor = cr.query(Audio.Playlists.EXTERNAL_CONTENT_URI, projection, null, null, null);

		if(mAudioCursor.moveToFirst()) {
			do {
			    long id = mAudioCursor.getLong(mAudioCursor.getColumnIndex(Audio.Playlists._ID));
				String name = mAudioCursor.getString(mAudioCursor.getColumnIndex(Audio.Playlists.NAME));

				list.add(new PlayListData(name, id));
			} while(mAudioCursor.moveToNext());

			mAudioCursor.close();
		}

		Collections.sort(list);
		return list;
	}

	public static ArrayList<FileData> getMusicFileFromPlaylist(Context context, PlayListData playList, String ext)
	{
		ContentResolver cr = context.getContentResolver();

		String[] memberProjection = {
			Audio.Playlists.Members._ID,
			Audio.Playlists.Members.DATA,
			Audio.Playlists.Members.TITLE,
			Audio.Playlists.Members.ARTIST
		};
		Uri membersUri = Audio.Playlists.Members.getContentUri("external", playList.getId());
		Cursor membersCursor = cr.query(membersUri, memberProjection, null, null, null);
		ArrayList<FileData> mediaData = new ArrayList<>();

		if(membersCursor.moveToFirst()) {
			do {
				int cid = (int)membersCursor.getLong(membersCursor.getColumnIndex(Audio.Playlists.Members._ID));
				String url = membersCursor.getString(membersCursor.getColumnIndex(Audio.Playlists.Members.DATA));
				String title = membersCursor.getString(membersCursor.getColumnIndex(Audio.Playlists.Members.TITLE));
				String artist = membersCursor.getString(membersCursor.getColumnIndex(Audio.Playlists.Members.ARTIST));

				if(!url.endsWith(ext)) continue;

				mediaData.add(MusicFile.createMusicFile(url, title, artist, playList));
			} while(membersCursor.moveToNext());

			membersCursor.close();
		}

		Collections.sort(mediaData);
		return mediaData;
	}
}
