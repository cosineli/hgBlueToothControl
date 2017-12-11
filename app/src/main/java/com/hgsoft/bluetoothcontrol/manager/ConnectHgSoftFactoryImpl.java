package com.hgsoft.bluetoothcontrol.manager;

import android.content.Context;
import android.util.Log;

import com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig;
import com.hgsoft.bluetoothcontrol.dataPack.BasePack;
import com.hgsoft.bluetoothcontrol.dataPack.DataPack;
import com.hgsoft.bluetoothcontrol.util.TransformUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.lnt.connectfactorylibrary.ConnectFactoryImpl;
import com.lnt.connectfactorylibrary.ConnectReturnImpl;

import java.util.List;
import java.util.UUID;

import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.HANDLE_MESSAGE_APP_DATA;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.HANDLE_MESSAGE_AUTH_REQUEST;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.HANDLE_MESSAGE_INIT_REQUEST;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.HANDLE_MESSAGE_RESPONSE_WRITE_DATA;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.characterInfoUuid;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.companyInfo;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.serverInfoUuid;
import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED;

/**
 * Created by liyuxian on 2017/11/15.
 */

public class ConnectHgSoftFactoryImpl implements ConnectFactoryImpl {
    private static String TAG = ConnectHgSoftFactoryImpl.class.getSimpleName();

    private static ConnectHgSoftFactoryImpl instance;

    private  BluetoothClient mClient;

    private Context context;


    private String mBTMac;

    private ConnectReturnImpl mConnectReturn;


    private byte[] B3Data;

    public static ConnectHgSoftFactoryImpl getInstance(Context context){
        if(instance == null){
            instance = new ConnectHgSoftFactoryImpl(context);
        }
        return instance;
    }

    private ConnectHgSoftFactoryImpl(Context context) {
        this.context = context;
    }



    @Override
    public void connection(Context context, final String mac,final ConnectReturnImpl connectReturn) {
        Log.e("connection","开始连接");
        if(mac==null){
            connectReturn.connectResult(false,null);
            return;
        }
        mClient = ClientManager.getClient(context);

        int status = mClient.getConnectStatus(mac);

//        Log.e(TAG,"status:"+status);
//        Log.e(TAG,"STATUS_DEVICE_CONNECTED:"+STATUS_DEVICE_CONNECTED);
//        Log.e(TAG,"STATUS_UNKNOWN:"+STATUS_UNKNOWN);
//        Log.e(TAG,"STATUS_DEVICE_CONNECTING:"+STATUS_DEVICE_CONNECTING);
//        Log.e(TAG,"STATUS_DEVICE_DISCONNECTING:"+STATUS_DEVICE_DISCONNECTING);
//        Log.e(TAG,"STATUS_DEVICE_DISCONNECTED:"+STATUS_DEVICE_DISCONNECTED);

        if(status == STATUS_DEVICE_CONNECTED){
            connectReturn.connectResult(true,mac);
            return;
        }



        mConnectReturn = connectReturn;
        mBTMac = mac;

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(1)   // 连接如果失败重试1次
                .setConnectTimeout(3000)   // 连接超时3s
                .setServiceDiscoverRetry(1)  // 发现服务如果失败重试1次
                .setServiceDiscoverTimeout(2000)  // 发现服务超时2s
                .build();


        ClientManager.getClient(context).connect(mac, options,new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if(code==REQUEST_SUCCESS){
                    ////////////////////////////////////////////////////////
                    mClient.read(mBTMac, UUID.fromString(serverInfoUuid), UUID.fromString(characterInfoUuid), new BleReadResponse() {
                        @Override
                        public void onResponse(int code, byte[] data) {
                            String value = TransformUtils.byte2hex(data);
                            Log.e(TAG,"读取的内容："+value);
                            if (code == REQUEST_SUCCESS && companyInfo.equals(value)) {
                                mClient.indicate(mac, UUID.fromString(BlueDeviceConfig.serverUuid),
                                        UUID.fromString(BlueDeviceConfig.indicateUuid), new IndicatResponse());
                            }else {
                                closeConnection();
                                mConnectReturn.connectResult(false,mBTMac);
                            }
                        }
                    });
                    ////////////////////////////////////////////////////////
                }else {
                    mConnectReturn.connectResult(false,mac);
                }
            }
        });

    }

    @Override
    public Object closeConnection() {
        mClient.disconnect(mBTMac);
        return true;
    }


    @Override
    public byte[] transmit(byte[] bytes) {

        B3Data = null;
        byte[] data = DataPack.getA3DataPack(bytes);

        mClient = ClientManager.getClient(context);

        handleServerData(HANDLE_MESSAGE_APP_DATA,null,data);
        try {
            while (true){
                Thread.sleep(10);
                if(B3Data!=null){
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return B3Data;
    }

    @Override
    public Object ledShow() {
        return null;
    }

    @Override
    public Object shake() {
        return null;
    }

    @Override
    public Object powerOff() {
//        handleServerData(HANDLE_MESSAGE_APP_DATA,null,DataPack.getPowerOffPack());
//        return true;
        return false;
    }

    @Override
    public Object powerOn() {
//        00 A4 00 00 02 DD F1 判断上电指令
        byte[] b = {0x00, (byte) 0xA4,0x00,0x00,0x02, (byte) 0xDD, (byte) 0xF1};
        byte[] transmit = transmit(b);
//        Log.e(TAG,"powerOn返回："+TransformUtils.byte2hex(transmit));
        //返回FFFF表示超时 return false，其他返回true
        String result = TransformUtils.byte2hex(transmit);

        return !"FFFF".equals(result);
    }

    @Override
    public Object openCommunication() {
        return null;
    }

    @Override
    public Object closeCommunication() {
        return null;
    }

    @Override
    public int getElectricQuantity() {
        return -2;
    }

    @Override
    public Object getConnectState() {

        int status = ClientManager.getClient(context).getConnectStatus(mBTMac);

        return status == STATUS_DEVICE_CONNECTED;
    }





    /**
     * 打开通知
     */
    private class IndicatResponse implements BleNotifyResponse{
        /**
         * 接收设备的信息
         * @param service
         * @param character
         * @param serverData
         */
        @Override
        public void onNotify(UUID service, UUID character, byte[] serverData) {
            String value = TransformUtils.byte2hex(serverData);
            Log.e(TAG,"收到设备发来的信息："+value);

            if(TransformUtils.isFullServerData(value)){
                String data = TransformUtils.getServerData();
                Log.e(TAG,"完整数据："+data);
                int type = TransformUtils.getCommandType(data);
                //收到请求
                handleServerData(type,data,null);
            }
        }

        @Override
        public void onResponse(int code) {

        }
    }




    /**
     * 处理上传与下发的数据
     * @param type
     * @param data
     * @param appData
     */
    private void handleServerData(int type,String data,byte[] appData){

        switch (type){
            case HANDLE_MESSAGE_AUTH_REQUEST:
                //得到Auth包的流水号
                String serialNum = data.substring(12,16);
                //下发响应包
                byte[] bytes = BasePack.sendAuthRespon(serialNum);
                mClient.write(mBTMac, UUID.fromString(BlueDeviceConfig.serverUuid), UUID.fromString(BlueDeviceConfig.writeUuid), bytes, new BleWriteResponse() {
                    @Override
                    public void onResponse(int code) {
                    }
                });

                break;

            case HANDLE_MESSAGE_INIT_REQUEST:
                //FE 01 0016 4E23 0002 0A 06 08 00 12 02 4F 4B 10 00 18 00
                byte[] initResponBytes = TransformUtils.hexStringToBytes(BlueDeviceConfig.initResponData);
                mClient.write(mBTMac, UUID.fromString(BlueDeviceConfig.serverUuid), UUID.fromString(BlueDeviceConfig.writeUuid), initResponBytes, new BleWriteResponse() {
                    @Override
                    public void onResponse(int code) {
                        if (code == REQUEST_SUCCESS) {
                            // 下发判断是否为华工设备的指令
//                            handleServerData(HANDLE_MESSAGE_IS_HGSOFT,null,DataPack.getCheckHGPack());
                            mConnectReturn.connectResult(true,mBTMac);
                        }
                    }
                });

                break;

//            case HANDLE_MESSAGE_IS_HGSOFT://判断是否为HG SOFT的设备
//
//                mClient.write(mBTMac, UUID.fromString(BlueDeviceConfig.serverUuid),
//                        UUID.fromString(BlueDeviceConfig.writeUuid),
//                        appData, new BleWriteResponse() {
//                    @Override
//                    public void onResponse(int code) {
//                    }
//                });
//
//                break;
//
//            case HANDLE_MESSAGE_RESPONSE_INIT_DATA://判断是否为HG设备
//                if(TransformUtils.isHGDevice(data)){
//                    mConnectReturn.connectResult(true,mBTMac);
//                }else {
//                    closeConnection();
//                    mConnectReturn.connectResult(false,mBTMac);
//                }
//                break;

            case HANDLE_MESSAGE_APP_DATA://app下发数据

                List<byte[]> bytes1 = TransformUtils.getSplitArray(appData,20);

                writeToBle(bytes1);

                break;

            case HANDLE_MESSAGE_RESPONSE_WRITE_DATA://设备对下发数据的响应
                //完整的数据包
                byte[] result = TransformUtils.getB3Data(data);
                if(result==null){
                    result = new byte[0];
                }
                B3Data = result;

                break;

        }

    }


    /**
     * 下发数据包到蓝牙设备
     * @param bytes
     */
    private void writeToBle(final List<byte[]> bytes){
        if(bytes.size()==0){
            return;
        }
        byte[] b = bytes.remove(0);

        mClient.write(mBTMac, UUID.fromString(BlueDeviceConfig.serverUuid), UUID.fromString(BlueDeviceConfig.writeUuid), b, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {
                    writeToBle(bytes);
                }
            }
        });
    }


}
