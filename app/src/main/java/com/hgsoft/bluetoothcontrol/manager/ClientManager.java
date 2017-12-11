package com.hgsoft.bluetoothcontrol.manager;

import android.content.Context;

import com.inuker.bluetooth.library.BluetoothClient;

/**
 *  蓝牙设备管理
 */
public class ClientManager {

    private static BluetoothClient mClient;

    public static BluetoothClient getClient(Context context) {
        if (mClient == null) {
            synchronized (ClientManager.class) {
                if (mClient == null) {
                    mClient = new BluetoothClient(context);
                }
            }
        }
        return mClient;
    }
}
