package com.mengle.lib.wiget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ConfirmDialog {

	public static interface OnClickListener{
		public void onPositiveClick();
		public void onNegativeClick();
	}
	
	public static void open(final Context context,String title,String message,final OnClickListener clickListener){
		open(context, title, message, "取消", "确定", clickListener);
	}
	
	public static void open(final Context context,String title,String message,String posText, String negText, final OnClickListener clickListener){
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(posText, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(clickListener != null){
		        	clickListener.onNegativeClick();
		        }
			}
		    })
		 .setNegativeButton(negText, new DialogInterface.OnClickListener() {
			
			 public void onClick(DialogInterface dialog, int whichButton) {
			        if(clickListener != null){
			        	clickListener.onPositiveClick();
			        }
			 }
			 
			
		}).show();
	}
}
