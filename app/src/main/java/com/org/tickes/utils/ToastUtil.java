package com.org.tickes.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	/**
	 * Toast a message<br/>
	 * using Toast.LENGTH_SHORT<br/>
	 * This method can be called from none UI thread,this function has been removed<br/>
	 */
	public static void xtShort(Context context, CharSequence text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Toast a message<br/>
	 * using Toast.LENGTH_LONG<br/>
	 * This method can be called from none UI thread,this function has been removed<br/>
	 */
	public static void xtLong(Context context, CharSequence text) {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

}
