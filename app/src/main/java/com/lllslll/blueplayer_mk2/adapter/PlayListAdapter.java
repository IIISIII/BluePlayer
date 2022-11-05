package com.lllslll.blueplayer_mk2.adapter;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import com.lllslll.blueplayer_mk2.*;
import com.lllslll.blueplayer_mk2.file.*;


public class PlayListAdapter extends BaseAdapter
{
	private LayoutInflater inflater = null;

	private Resources res = null;

	private ArrayList<FileData> itemList = new ArrayList<>();

	private View[] viewList = null;
	
	private int selectedIndex = -1;


	public PlayListAdapter(Context c)
	{
		this.inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.res = c.getResources();
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent)
	{
		if(this.viewList[pos] != null) {
			if(pos == this.selectedIndex)
				this.viewList[pos].setBackgroundResource(R.drawable.playlist_current_item_background);
			else
				this.viewList[pos].setBackgroundResource(R.drawable.playlist_item_background);
			return this.viewList[pos];
		}

		FileData data = this.itemList.get(pos);
		if(!(data instanceof MusicFile))
			return null;
		MusicFile mf = (MusicFile)data;
			
		View v = this.inflater.inflate(R.layout.playlist_item_layout, parent, false);
		ImageView thumb = (ImageView)v.findViewById(R.id.playlist_thumbnail);
		TextView title = (TextView)v.findViewById(R.id.playlist_title);
		
		Bitmap thum = mf.getThumbnail();
		if(thum != null)
			thumb.setImageBitmap(thum);
		title.setText(mf.getTitle());
		if(pos == this.selectedIndex)
			v.setBackgroundResource(R.drawable.playlist_current_item_background);
		else
			v.setBackgroundResource(R.drawable.playlist_item_background);
			
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
	
	public void select(int i)
	{
		this.selectedIndex = i;
		this.notifyDataSetChanged();
	}
}
