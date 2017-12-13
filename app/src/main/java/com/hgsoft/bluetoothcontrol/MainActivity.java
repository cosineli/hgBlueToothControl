package com.hgsoft.bluetoothcontrol;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hgsoft.bluetoothcontrol.manager.ClientManager;
import com.hgsoft.bluetoothcontrol.manager.ConnectHgSoftDeviceListImpl;
import com.lnt.connectfactorylibrary.BlueToothDevice;
import com.lnt.connectfactorylibrary.DeviceListImpl;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button btnScan;
    private ListView listView;
    private List<BlueToothDevice> data = new ArrayList<>();
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init() {
        btnScan = (Button) findViewById(R.id.btn_scan);
        listView = (ListView) findViewById(R.id.list_view);
        btnScan.setOnClickListener(this);
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_scan:
                boolean open = ClientManager.getClient(this).isBluetoothOpened();
                if(!open){
                    Toast.makeText(this,"请先打开蓝牙",Toast.LENGTH_LONG).show();
                    return;
                }
                data.clear();
                adapter.setData(data);

                showCircleDialog("正在扫描,请稍后...",true);
                ConnectHgSoftDeviceListImpl.getInstance(this).getDeviceList(this, new DeviceListImpl() {
                    @Override
                    public void devicesResult(ArrayList<BlueToothDevice> arrayList) {
                        data = arrayList;
                        adapter.setData(arrayList);
                        dismissCircleDialog();
                    }
                });

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.e(TAG,"点击了第"+position+"个");
        Bundle bundle = new Bundle();
        bundle.putString("mac",data.get(position).getAddress());

        AnyScanActivity.startActivity(this,bundle);

    }

    private class MyAdapter extends BaseAdapter{

        List<BlueToothDevice> data = new ArrayList<>();
        Context context;
        LayoutInflater inflater;

        public void setData(List<BlueToothDevice> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        private MyAdapter(Context context){
            this.context = context;

            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = new ViewHolder();
            BlueToothDevice blueToothDevice = data.get(position);
            if(convertView==null){
                convertView = inflater.inflate(R.layout.item_list,null);
            }
            holder.tvAddress = (TextView) convertView.findViewById(R.id.tv_address);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);

            holder.tvName.setText(blueToothDevice.getName());
            holder.tvAddress.setText(blueToothDevice.getAddress());

            return convertView;
        }


        private class ViewHolder{
            TextView tvName;
            TextView tvAddress;
        }


    }


}




