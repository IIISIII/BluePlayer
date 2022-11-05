package com.lllslll.blueplayer_mk2.util;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.widget.*;

import com.lllslll.blueplayer_mk2.*;



public abstract class EditTextDialogManager
{
	private String title = null, message = null, hint = null;
	
	private Context context = null;

	private EditText text = null;
	

	public EditTextDialogManager(Context c, String title, String message, String hint)
	{
		this.context = c;

		this.title = title;
		this.message = message;
		this.hint = hint;
	}
	
	public EditTextDialogManager(Context c, int tid, int mid, int hid)
	{
		this.context = c;
		
		Resources res = c.getResources();
		
		this.title = res.getString(tid);
		this.message = res.getString(mid);
		this.hint = res.getString(hid);
	}

	public void show()
	{
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		FrameLayout layout = new FrameLayout(this.context);
		layout.setLayoutParams(params);
		this.text = new EditText(this.context);
		this.text.setHint(this.hint);
		layout.addView(this.text);
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this.context)
		 .setTitle(this.title)
		 .setMessage(this.message)
		 .setCancelable(true)
		 .setView(layout)
		 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
		  {
			 @Override
			 public void onClick(DialogInterface di, int id)
			 {
				 onClickPositive(text.getText().toString());
			 }
		  })
		 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
		  {
			 @Override
			 public void onClick(DialogInterface di, int id)
			 {
				 onClickNegative(text.getText().toString());
			 }
		  });
		builder.create().show();
	}
	
	public abstract void onClickPositive(String t);
	public abstract void onClickNegative(String t);
}
