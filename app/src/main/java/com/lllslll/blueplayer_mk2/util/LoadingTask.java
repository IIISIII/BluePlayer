package com.lllslll.blueplayer_mk2.util;

import android.content.*;
import android.os.*;

import java.util.*;

import com.lllslll.blueplayer_mk2.*;
import com.lllslll.blueplayer_mk2.adapter.*;
import com.lllslll.blueplayer_mk2.file.*;


public abstract class LoadingTask extends AsyncTask<Object, Void, Boolean>
{
	private Context context = null;
	
	private FileDataAdapter adapter = null;
	
	private ArrayList<FileData> dataList = null;
	
	
	public LoadingTask(Context c)
	{
		this.context = c;
	}
	
	/*
		[0] : type
		[1] : data
	 */
	@Override
	protected Boolean doInBackground(Object[] params)
	{
		if(this.adapter == null)
			return false;
		
		ListType type = (ListType)params[0];
		FileData data = (FileData)params[1];
		
		if(type == ListType.FOLDER)
			this.dataList = FileSearcher.searchFileByExt(data != null ? data : new Folder(Environment.getExternalStorageDirectory()), true, ".mp3");
		else if(type == ListType.LIBRARY) {
			
		}
		else if(type == ListType.MEDIA_DB) {
			this.dataList = MediaDataReader.getMusicFileList(this.context, ".mp3");
		}
		else if(type == ListType.PLAYLIST)
			this.dataList = data == null ? MediaDataReader.getPlaylist(this.context) : MediaDataReader.getMusicFileFromPlaylist(this.context, (PlayListData)data, ".mp3");
		
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		
		this.adapter.setDataList(this.dataList);
		
		this.onPost();
	}
	
	public void setAdapter(FileDataAdapter adapter)
	{
		this.adapter = adapter;
	}
	
	public abstract void onPost();
}
