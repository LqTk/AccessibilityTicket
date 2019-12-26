package com.org.tickes.ui;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.org.tickes.MainActivity;

public class CashHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public final String TAG = "CatchExcep";
    BrokenRestart applicatioin;

    public CashHandler(BrokenRestart applicatioin) {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.applicatioin = applicatioin;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.d(TAG,e.toString());
        if (!handlerException(e) && mDefaultHandler !=null){
            //如果用户没有处理，则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(t,e);
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            Toast.makeText(applicatioin.getApplicationContext(),e.toString(), Toast.LENGTH_SHORT).show();
            Log.d("errorBroken",e.toString());
            Intent intent = new Intent(applicatioin.getApplicationContext(), MainActivity.class);
            @SuppressLint("WrongConstant") PendingIntent restartIntent = PendingIntent.getActivity(applicatioin.getApplicationContext(),0,intent,Intent.FLAG_ACTIVITY_NEW_TASK);
            //退出程序
            AlarmManager mgr = (AlarmManager) applicatioin.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC,System.currentTimeMillis() + 1000,restartIntent);//1秒后重启
            applicatioin.finishActivity();
        }
    }

    /**
     * 自定义错误处理，收集错误信息，发送错误报告等操作
     * return true 如果处理该异常信息；否则返回false
     */
    private boolean handlerException(final Throwable ex){
        if (ex == null){
            return false;
        }
        new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(applicatioin.getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        return true;
    }

}
