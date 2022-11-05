package com.lllslll.blueplayer_mk2;
import android.icu.text.*;

public enum ListType
{
	FOLDER(0), LIBRARY(1), MEDIA_DB(2), PLAYLIST(3);
	
	private final int value;
	private ListType(int v)
	{
		this.value = v;
	}
	
	public int getValue()
	{
		return this.value;
	}
	
	public static ListType intToListType(int type)
	{
		switch(type) {
			case 0 :
				return FOLDER;
			case 1 :
				return LIBRARY;
			case 2 :
				return MEDIA_DB;
			default :
			    return PLAYLIST;
		}
	}
}
