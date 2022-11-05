package com.lllslll.blueplayer_mk2.util;

import java.util.*;

import com.lllslll.blueplayer_mk2.file.*;


public class MusicPlayList
{
	private static int currentIndex = -1;
	
	private static ArrayList<FileData> playList = null;
	
	public static void setPlayList(ArrayList<FileData> list)
	{
		playList = list;
	}
	
	public static ArrayList<FileData> getPlayList()
	{
		return playList;
	}
	
	public static void moveTo(int m)
	{
		if(playList == null)
			return;
		currentIndex += m;
		int max = playList.size();
		if(max == 0)
			return;
		while(currentIndex < 0)
			currentIndex += max;
		currentIndex %= max;
		
		if(!getCurrentMusicFile().isExist()) {
			playList.remove(currentIndex);
			moveTo(0);
		}
	}
	
	public static void setCurrentIndex(int index)
	{
		if(playList == null)
			return;
		currentIndex = index;
	}
	
	public static int getCurrentIndex()
	{
		if(playList == null)
			return -1;
		return currentIndex;
	}
	
	public static MusicFile getCurrentMusicFile()
	{
		if(playList == null || currentIndex < 0 || currentIndex >= playList.size())
			return null;
		return (MusicFile)playList.get(currentIndex);
	}
}
