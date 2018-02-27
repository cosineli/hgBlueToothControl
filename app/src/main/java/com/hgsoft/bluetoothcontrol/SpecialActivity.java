package com.hgsoft.bluetoothcontrol;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hgsoft.bluetoothcontrol.manager.ClientManager;
import com.hgsoft.bluetoothcontrol.manager.ConnectHgSoftFactoryImpl;
import com.hgsoft.bluetoothcontrol.util.TransformUtils;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.lnt.connectfactorylibrary.BlueToothDevice;
import com.lnt.connectfactorylibrary.ConnectReturnImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class SpecialActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = "SpecialActivity";

    private Button btn_start,btn_end, btn_sendcommand,btn_power_on,btn_power_off;
    private TextView tvResult,tvResponse;
    private EditText etCommand;
    private boolean isConnect;

    private String btMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special);

        initView();

    }




    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_sp);
        toolbar.setTitle("直连设备");
////        setSupportActionBar(toolbar);
////        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//
        btn_start = (Button) findViewById(R.id.btn_start_sp);
        btn_start.setOnClickListener(this);
        btn_end = (Button) findViewById(R.id.btn_end_sp);
        btn_end.setOnClickListener(this);
        btn_sendcommand = (Button) findViewById(R.id.btn_send_sp);
        btn_sendcommand.setOnClickListener(this);
        btn_power_on = (Button) findViewById(R.id.btn_power_on_sp);
        btn_power_on.setOnClickListener(this);
        btn_power_off = (Button) findViewById(R.id.btn_power_off_sp);
        btn_power_off.setOnClickListener(this);

        tvResult = (TextView) findViewById(R.id.tv_result_sp);
        tvResponse = (TextView) findViewById(R.id.tv_response_sp);
        etCommand = (EditText) findViewById(R.id.et_command_sp);

        tvResponse.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        boolean open = ClientManager.getClient(this).isBluetoothOpened();
        if(!open){
            Toast.makeText(this,"请先打开蓝牙",Toast.LENGTH_LONG).show();
            return;
        }
        switch (v.getId()) {

            case R.id.btn_end_sp:
                if(!isConnect){
                    showToast("请先连接！");
                    return;
                }
                ConnectHgSoftFactoryImpl.getInstance(this).closeConnection();
                tvResult.setText("断开成功！");
                isConnect = false;
                break;
            case R.id.btn_start_sp:

                showCircleDialog("正在连接...",true);
                //扫描最近的fairwin设备并连接
                scanDevice(new FairwinDeviceImpl() {
                    @Override
                    public void getBleMac(final String mac, String name) {
                        if(mac==null){
                            return;
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                ConnectHgSoftFactoryImpl.getInstance(SpecialActivity.this).connection(SpecialActivity.this, mac, new ConnectReturnImpl() {
                                    @Override
                                    public void connectResult(final boolean b, final String s) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(b){
                                                    Log.e(TAG,"连接成功："+s);
                                                    tvResult.setText("连接成功:"+s);
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

                    }
                });


                break;

            case R.id.btn_send_sp:
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
            case R.id.btn_power_on_sp://上电
                if(!isConnect){
                    showToast("请先连接！");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean o = (boolean) ConnectHgSoftFactoryImpl.getInstance(SpecialActivity.this).powerOn();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(o){
                                    tvResult.setText("上电成功！");
                                }
                            }
                        });

                    }
                }).start();
                break;
            case R.id.btn_power_off_sp://下电
                if(!isConnect){
                    showToast("请先连接！");
                    return;
                }
                ConnectHgSoftFactoryImpl.getInstance(SpecialActivity.this).powerOff();
                tvResult.setText("下电成功！");
                isConnect = false;
                break;



        }
    }

    private interface FairwinDeviceImpl{
        void getBleMac(String mac,String name);
    }


    private void scanDevice(final FairwinDeviceImpl fairwinDevice){

        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(1000, 1)   // 先扫BLE设备3次，每次3s // TODO: 2017/12/13  扫描更改为1次
                .searchBluetoothClassicDevice(1000) // 再扫经典蓝牙3s
                .searchBluetoothLeDevice(1000)      // 再扫BLE设备2s
                .build();

        ClientManager.getClient(SpecialActivity.this).search(request, new SearchResponse() {
            boolean flag = false;
            @Override
            public void onSearchStarted() {
                flag = true;
                Log.e("onSearchStarted","==============");
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                String address = device.getAddress();
                String name = device.getName();

                if("FAIRWIN".equals(name)){
                    if(flag){
                        fairwinDevice.getBleMac(address,name);
                        flag = false;
                    }

                }
                Log.e("onDeviceFounded","==============");

            }

            @Override
            public void onSearchStopped() {
                flag = false;
                Log.e("onSearchStopped","==============");
            }

            @Override
            public void onSearchCanceled() {
                flag = false;
            }
        });

    }


    /**
     * 读取余额，适用于子线程
     * @param command
     * @return
     */
    private String sendCommand(String command){
        byte[] data = TransformUtils.hexStringToBytes(command);
        byte[] transmit = ConnectHgSoftFactoryImpl.getInstance(SpecialActivity.this).transmit(data);
        return TransformUtils.byte2hex(transmit);
    }




}
