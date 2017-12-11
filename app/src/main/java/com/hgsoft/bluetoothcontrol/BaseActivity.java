package com.hgsoft.bluetoothcontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.widget.Toast;


import java.lang.ref.WeakReference;


public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void showToast(String message){
        showToast(message, true);
    }

    public void showToast(@StringRes int resId){
        showToast(resId, true);
    }

    public void showToast(String message, boolean isShortDuration){
        if(TextUtils.isEmpty(message))
            return;
        Toast.makeText(this, message, isShortDuration ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    public void showToast(int resId, boolean isShortDuration){
        Toast.makeText(this, resId, isShortDuration ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    public void showToastOnUIThread(final String message){

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(message);
            }
        });
    }

    ProgressDialog pd;
    protected void showCircleDialog(String message,boolean canCelable){
        if (pd != null && pd.isShowing()) {
            return;
        }
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(message);
        pd.setCanceledOnTouchOutside(canCelable);
        pd.setCancelable(true);
        pd.show();
    }
    protected void dismissCircleDialog(){
        if(pd!=null) {
            pd.dismiss();
            pd = null;
        }
    }



    private InnerWeakHandler mHandler;
    public InnerWeakHandler getHandlerInstance(){
        if(mHandler == null)
            mHandler = new InnerWeakHandler(this);
        return mHandler;
    }

    private static class InnerWeakHandler extends Handler {
        private WeakReference<BaseActivity> context;
        public InnerWeakHandler(BaseActivity context){
            this.context = new WeakReference<BaseActivity>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseActivity activity = context.get();
            if(activity != null)
                activity.handleHandlerMessage(msg);
        }

    }

    /**
     * 子类重写这个方法
     * @param msg
     */
    public void handleHandlerMessage(Message msg){

    }



    public static void startToActivity(Context context , Class clazz){
        Intent intent = new Intent();
        intent.setClass(context,clazz);
        context.startActivity(intent);
    }


}
