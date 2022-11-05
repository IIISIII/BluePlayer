package com.lllslll.blueplayer_mk2.util;

import android.content.*;

import java.io.*;
import java.util.*;

import org.json.*;

import com.lllslll.blueplayer_mk2.file.*;
import android.widget.*;


public class PlayerDataManager
{
	private class DataType
	{
		public static final int FOLDER = 0;
		public static final int LIBRARY = 1;
		public static final int MEDIA_DB = 2;
		public static final int PLAYLIST = 3;
	}
	
	public static synchronized void savePlayerState(Context c, MusicPlayer player)
	{
		MusicFile mf = MusicPlayList.getCurrentMusicFile();
		if(mf == null)
			return;
		
		FileData parent = mf.getParentData();
		
		int type = 0;
		String path = "";
		long id = 0;
		
		if(parent == null)
			type = DataType.MEDIA_DB;
		else if(parent instanceof Folder) {
			type = DataType.FOLDER;
			path = parent.getPath();
		}
		else if(parent instanceof LibraryData) {
			type = DataType.LIBRARY;
			path = parent.getName();
		}
		else if(parent instanceof PlayListData) {
			type = DataType.PLAYLIST;
			path = parent.getName();
			id = ((PlayListData)parent).getId();
		}
		
		try {
			JSONObject json = new JSONObject();
			json.put("type", type);
			json.put("path", path);
			json.put("id", id);
			json.put("index", MusicPlayList.getCurrentIndex());
			json.put("loop", player.isLooping());
			json.put("progress", player.getProgress());
			json.put("volume", player.getVolume());
			
			FileOutputStream output = c.openFileOutput("player_state.data", Context.MODE_PRIVATE);
			output.write(json.toString().getBytes());
			output.close();
		} catch(JSONException err) {
			
		} catch(IOException err) {
			
		}
	}
	
	public static synchronized void readPlayerState(Context c, MusicPlayer player)
	{
		try {
			FileInputStream input = c.openFileInput("player_state.data");
			
			byte[] buffer = new byte[128];
			StringBuffer sb = new StringBuffer();
			
			while(input.read(buffer) != -1)
				sb.append(new String(buffer));
			String data = sb.toString();
			
			input.close();
			
			JSONObject json = new JSONObject(data);
			ArrayList<FileData> dataList = null;
			int type = json.getInt("type");
			String path = json.getString("path");
			long id = json.getLong("id");
			boolean loop = json.getBoolean("loop");
			int index = json.getInt("index");
			int progress = json.getInt("progress");
			float volume = (float)json.getDouble("volume");
			switch(type) {
				case DataType.FOLDER :
					dataList = FileSearcher.searchFileByExt(new Folder(path), false, ".mp3");
					break;
				case DataType.LIBRARY :
					
					break;
				case DataType.MEDIA_DB :
					dataList = MediaDataReader.getMusicFileList(c, ".mp3");
					break;
				case DataType.PLAYLIST :
					dataList = MediaDataReader.getMusicFileFromPlaylist(c, new PlayListData(path, id), ".mp3");
			}
			
			MusicPlayList.setPlayList(dataList);
			MusicPlayList.setCurrentIndex(index);
			
			player.setMusicFile(MusicPlayList.getCurrentMusicFile());
			player.setLooping(loop);
			player.setProgress(progress);
			player.setVolume(volume);
		} catch(JSONException err) {
			
		} catch(IOException err) {
			
		} catch(Exception err) {
			
		}
	}
}
