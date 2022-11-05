package com.lllslll.blueplayer_mk2.adapter;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import com.lllslll.blueplayer_mk2.*;
import com.lllslll.blueplayer_mk2.file.*;


public class FileDataAdapter extends BaseAdapter
{
	private LayoutInflater inflater = null;
	
	private Resources res = null;
	
	private ArrayList<FileData> itemList = new ArrayList<>();
	
	private View[] viewList = null;
	
	private ArrayList<ArrayList<FileData>> saveList = new ArrayList<>();
	
	public FileDataAdapter(Context c)
	{
		this.inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.res = c.getResources();
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent)
	{
		if(this.viewList[pos] != null)
			return this.viewList[pos];
		
		FileData data = this.itemList.get(pos);
		
		View v = null;
		if(data instanceof Folder) {
			v = this.inflater.inflate(R.layout.folder_item_layout, parent, false);
			TextView name = (TextView)v.findViewById(R.id.name);
			
			name.setText(data.getName());
		}
		else if(data instanceof MusicFile) {
			MusicFile mf = (MusicFile)data;
			v = this.inflater.inflate(R.layout.music_item_layout, parent, false);
			ImageView thumb = (ImageView)v.findViewById(R.id.thumbnail);
			TextView title = (TextView)v.findViewById(R.id.title);
			TextView artist = (TextView)v.findViewById(R.id.artist);
			TextView duration = (TextView)v.findViewById(R.id.duration);
			
			Bitmap thumbnail = mf.getThumbnail();
			if(thumbnail != null)
				thumb.setImageBitmap(thumbnail);
			title.setText(mf.getTitle());
			artist.setText(mf.getArtist());
			duration.setText(MusicFile.durationToTimetext(mf.getDuration()));
		}
		this.viewList[pos] = v;
		
		return v;
	}

	@Override
	public Object getItem(int pos)
	{
		if(this.itemList == null || pos < 0 || pos >= this.itemList.size())
			return null;
		return this.itemList.get(pos);
	}

	@Override
	public long getItemId(int pos)
	{
		if(this.itemList == null || pos < 0 || pos >= this.itemList.size())
			return -1;
		return pos;
	}

	@Override
	public int getCount()
	{
		if(this.itemList == null)
			return 0;
		return this.itemList.size();
	}
	
	public void setDataList(ArrayList<FileData> list)
	{
		if(list == null) {
			this.itemList.clear();
			this.viewList = null;
			return;
		}
		this.itemList = list;
		this.viewList = new View[list.size()];
		this.notifyDataSetChanged();
	}
	
	public ArrayList<FileData> getDataList()
	{
		return this.itemList;
	}
	
	public boolean deleteData(int pos)
	{
		if(this.itemList == null || pos < 0 || pos >= this.itemList.size())
			return false;
		this.itemList.remove(pos);
		this.viewList = new View[this.getCount()];
		this.notifyDataSetChanged();
		return true;
	}
	
	public void saveList()
	{
		this.saveList.add(0, this.itemList);
	}
	
	public boolean restoreSavedList()
	{
		if(this.saveList.size() > 0) {
			this.setDataList(this.saveList.remove(0));
			return true;
		}
		return false;
	}
	
	public void clearSavedList()
	{
		this.saveList.clear();
	}
}
