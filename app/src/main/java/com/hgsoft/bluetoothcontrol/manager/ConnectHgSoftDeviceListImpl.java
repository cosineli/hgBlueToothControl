package com.hgsoft.bluetoothcontrol.manager;

import android.content.Context;

import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.lnt.connectfactorylibrary.BlueToothDevice;
import com.lnt.connectfactorylibrary.ConnectDeviceListImpl;
import com.lnt.connectfactorylibrary.DeviceListImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liyuxian on 2017/11/20.
 */
public class ConnectHgSoftDeviceListImpl implements ConnectDeviceListImpl {

    private static ConnectHgSoftDeviceListImpl instance;
    private Context context;


    public static ConnectHgSoftDeviceListImpl getInstance(Context context){
        if(instance==null){
            instance = new ConnectHgSoftDeviceListImpl(context);
        }
        return instance;
    }

    private ConnectHgSoftDeviceListImpl(Context context){
        this.context = context;
    }


    @Override
    public void getDeviceList(Context context, final DeviceListImpl deviceList) {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(3000) // 再扫经典蓝牙3s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        final ArrayList<BlueToothDevice> devices = new ArrayList<>();
        final Map<String ,String> map = new HashMap<>();

        ClientManager.getClient(context).search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                String address = device.getAddress();
                String name = device.getName();
                map.put(address,name);

            }

            @Override
            public void onSearchStopped() {
                for(String address:map.keySet()){
                    BlueToothDevice bd = new BlueToothDevice();
                    bd.setAddress(address);
                    bd.setName(map.get(address));
                    devices.add(bd);
                }
                deviceList.devicesResult(devices);
            }

            @Override
            public void onSearchCanceled() {
            }
        });
    }




}
