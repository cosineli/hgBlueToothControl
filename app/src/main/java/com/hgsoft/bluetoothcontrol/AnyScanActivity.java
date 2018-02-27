package com.hgsoft.bluetoothcontrol;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hgsoft.bluetoothcontrol.manager.ClientManager;
import com.hgsoft.bluetoothcontrol.manager.ConnectHgSoftFactoryImpl;
import com.hgsoft.bluetoothcontrol.util.TransformUtils;
import com.lnt.connectfactorylibrary.ConnectReturnImpl;

import java.math.BigDecimal;
import java.util.Date;


public class AnyScanActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AnyScanActivity";

    private Button btn_start,btn_end, btn_sendcommand,btn_power_on,btn_power_off;
    private TextView tvResult,tvResponse;
    private EditText etCommand;
    private boolean isConnect;

    private long startTime;

    private String btMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_any_scan);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            btMac = extras.getString("mac");
            Log.e(TAG,"mac:"+btMac);
        }


        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectHgSoftFactoryImpl.getInstance(this).closeConnection();
    }

    @Override
    public void onClick(View v) {
        boolean open = ClientManager.getClient(this).isBluetoothOpened();
        if(!open){
            Toast.makeText(this,"请先打开蓝牙",Toast.LENGTH_LONG).show();
            return;
        }
        switch (v.getId()) {

            case R.id.btn_end:
                if(!isConnect){
                    showToast("请先连接！");
                    return;
                }
                ConnectHgSoftFactoryImpl.getInstance(this).closeConnection();
                tvResult.setText("断开成功！");
                isConnect = false;
                break;
            case R.id.btn_start:
                if(btMac==null){
                    return;
                }
                showCircleDialog("正在连接...",true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String mac3 = "54:4A:16:56:4A:D3";//5922设备
                        String mac = "02:11:23:34:56:D8";
//                        String mac1 = "00:17:EA:8E:22:FD";// 5917设备
//                        String mac2 = "A1:CC:6C:56:68:59";// 伟杰设备

                        ConnectHgSoftFactoryImpl.getInstance(AnyScanActivity.this).connection(AnyScanActivity.this, btMac, new ConnectReturnImpl() {
                            @Override
                            public void connectResult(final boolean b, final String s) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(b){
                                            Log.e(TAG,"连接成功："+s);
                                            Date date = new Date();
                                            tvResult.setText("连接成功"+date.getTime());
                                            isConnect = true;
                                        }else {
                                            Log.e(TAG,"连接失败！");
                                            tvResult.setText("连接失败");
                                        }
                                        dismissCircleDialog();
                                    }
                                });


                            }
                        });
                    }
                }).start();


                break;

            case R.id.btn_send:
                if(!isConnect){
                    showToast("请先连接！");
                    return;
                }

                showCircleDialog("正在读取...",true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        粤通卡
//                        00 A4 00 00 02 10 01
//                        80 5C 00 02 04
//                        岭南通
//                        00 A4 00 00 02 DD F1
//                        00 A4 00 00 02 AD F3
//                        80 5C 00 02 04


                        BigDecimal money = new BigDecimal(-1);

                        String str = sendCommand("00A40000021001");
                        //是否为粤通卡
                        if(str.endsWith("9000")){
                            //读余额
                            String moneyStr = sendCommand("805C000204");
                            if(moneyStr.endsWith("9000")){
                                money = TransformUtils.hexStringToDecimal(moneyStr.substring(0,moneyStr.length()-4));
                                Log.e(TAG,"读取的粤通卡余额："+money);
                            }

                        }else {
                            //发送判断是否为岭南通卡的命令
                            String s = sendCommand("00A4000002DDF1");
                            if(s.endsWith("9000")){
                                String s1 = sendCommand("00A4000002ADF3");
                                if(s1.endsWith("9000")){
                                    //读取余额
                                    String moneystr = sendCommand("805C000204");
                                    if(moneystr.endsWith("9000")){
                                        money = TransformUtils.hexStringToDecimal(moneystr.substring(0,moneystr.length()-4));
                                        Log.e(TAG,"读取的岭南通余额："+money);
                                    }
                                }

                            }
                        }

                        final BigDecimal realMoney = money;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BigDecimal test = new BigDecimal(-1);
                                if(test.compareTo(realMoney)==0){
                                    tvResponse.setText("读取余额出错!");
                                }else {
                                    tvResponse.setText("余额："+realMoney+"元");
                                }
                                dismissCircleDialog();
                            }
                        });

                    }
                }).start();


                break;
            case R.id.btn_power_on://上电
                if(!isConnect){
                    showToast("请先连接！");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean o = (boolean) ConnectHgSoftFactoryImpl.getInstance(AnyScanActivity.this).powerOn();
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               if(o){
                                   tvResult.setText("上电成功！");
                               }else {
                                   tvResult.setText("上电失败！");
                               }
                           }
                       });

                    }
                }).start();
                break;
            case R.id.btn_power_off://下电
                if(!isConnect){
                    showToast("请先连接！");
                    return;
                }
                ConnectHgSoftFactoryImpl.getInstance(AnyScanActivity.this).powerOff();
                tvResult.setText("下电成功！");
                isConnect = false;
                break;



        }
    }

    /**
     * 读取余额，适用于子线程
     * @param command
     * @return
     */
    private String sendCommand(String command){
        byte[] data = TransformUtils.hexStringToBytes(command);
        byte[] transmit = ConnectHgSoftFactoryImpl.getInstance(AnyScanActivity.this).transmit(data);
        return TransformUtils.byte2hex(transmit);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("搜索设备");
////        setSupportActionBar(toolbar);
////        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);
        btn_end = (Button) findViewById(R.id.btn_end);
        btn_end.setOnClickListener(this);
        btn_sendcommand = (Button) findViewById(R.id.btn_send);
        btn_sendcommand.setOnClickListener(this);
        btn_power_on = (Button) findViewById(R.id.btn_power_on);
        btn_power_on.setOnClickListener(this);
        btn_power_off = (Button) findViewById(R.id.btn_power_off);
        btn_power_off.setOnClickListener(this);

        tvResult = (TextView) findViewById(R.id.tv_result);
        tvResponse = (TextView) findViewById(R.id.tv_response);
        etCommand = (EditText) findViewById(R.id.et_command);

        tvResponse.setMovementMethod(ScrollingMovementMethod.getInstance());
    }



    public static void startActivity(Context context,Bundle bundle){
        Intent intent = new Intent();
        intent.setClass(context,AnyScanActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }



}
