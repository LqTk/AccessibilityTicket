package com.org.tickes.ui;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;

public class BrokenRestart extends Application {
	private static BrokenRestart mInstance;

	ArrayList<Activity> list = new ArrayList<>();
	public static Application getInstance(){
		return mInstance;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		mInstance=this;
	}

	public void onTerminate(){
		super.onTerminate();
		mInstance=null;
	}

	public void init(){
		//设置该CashHandler为程序的默认处理器
		CashHandler unCashHandler = new CashHandler(this);
		Thread.setDefaultUncaughtExceptionHandler(unCashHandler);
	}

	public void addActivity(Activity activity){
		list.add(activity);
	}

	/**
	 *
	 * @param activity activity关闭时，删除Activity列表中的activity对象
	 */
	public void removeActivity(Activity activity){
		list.remove(activity);
	}

	/**
	 * 关闭activity中的所有activity
	 */
	public void finishActivity(){
		for (Activity activity:list){
			if (null != activity){
				activity.finish();
			}
		}
		//杀死应用进程
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
