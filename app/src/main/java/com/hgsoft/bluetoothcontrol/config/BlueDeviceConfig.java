package com.hgsoft.bluetoothcontrol.config;

/**
 * Created by liyuxian on 2017/11/16.
 */

public class BlueDeviceConfig {


    public final static String serverUuid = "0000fee7-0000-1000-8000-00805f9b34fb";
    public final static String writeUuid = "0000fec7-0000-1000-8000-00805f9b34fb";
    public final static String indicateUuid = "0000fec8-0000-1000-8000-00805f9b34fb";
    public final static String readUuid = "0000fec9-0000-1000-8000-00805f9b34fb";

    public final static String serverInfoUuid = "0000180a-0000-1000-8000-00805f9b34fb";
    public final static String characterInfoUuid = "00002a29-0000-1000-8000-00805f9b34fb";

    public final static String companyInfo = "4641495257494E";
    /**
     * Message.what
     */
    public static final int HANDLE_MESSAGE_AUTH_REQUEST = 0X10;//Auth请求
    public static final int HANDLE_MESSAGE_INIT_REQUEST = 0X11;//init请求
    public static final int HANDLE_MESSAGE_APP_DATA = 0X12;//APP下发数据
    public static final int HANDLE_MESSAGE_APP_SPLIT_DATA = 0X13;//处理分段(每次下发20字节)下发的数据
    public static final int HANDLE_MESSAGE_RESPONSE_INIT_DATA = 0X14;//收到设备初始化的响应
    public static final int HANDLE_MESSAGE_RESPONSE_WRITE_DATA = 0X15;//收到设备写卡的响应
    public static final int HANDLE_MESSAGE_IS_HGSOFT = 0X16;//判断是否为HG设备

    public static final String authCommand = "2711";
    public static final String initCommand = "2713";
    public static final String dataCommand = "7531";
    public static final String responseDataCommand = "2712";


    public static final String authResponData = "FE01001A4E2100010A06080012024F4B1206313233313234B04A";
    public static final String initResponData = "FE0100144E2300020A06080012024F4B10001800";

    public static final String appData = "FE010029753100030A00121B33068016A300120080" +
            "10810700A40000021001020500B095002B891800";

}
