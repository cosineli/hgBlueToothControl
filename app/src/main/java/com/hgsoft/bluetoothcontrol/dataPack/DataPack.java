package com.hgsoft.bluetoothcontrol.dataPack;

import android.util.Log;

import com.hgsoft.bluetoothcontrol.util.TransformUtils;

/**
 * Created by liyuxian on 2017/11/17.
 */

public class DataPack extends BasePack{


    //FE01 0022 7531 0003 0A0012 14 330680  0F A300 0B00 80 09 8107 00A40000021001 89 18 00
    //FE01 0022 7531 0003 0A0012 14 330680  0F A300 0B00 80 09 8107 00A40000021001 99 18 00


    public static byte[] getA3DataPack(String data){
        byte[] data1 = TransformUtils.hexStringToBytes(data);
        int dataLength = data1.length;
        byte[] TLBData = convertByteArray(dataLength+4);
        //流水号
        byte[] serialNums = intToByte2(serialNum);
        //使用完后加1
//        serialNum++;
        byte[]  dataPack = new byte[23+dataLength+4];
        byte[] dataPackLength = intToByte2(dataPack.length);

        dataPack[0] = IDENTIFY_DATA;
        dataPack[1] = mVer;
        //包的长度
        dataPack[2] = dataPackLength[0];
        dataPack[3] = dataPackLength[1];
        //命令
        dataPack[4] = 0x75;
        dataPack[5] = 0x31;
        //流水号
        dataPack[6] = serialNums[0];
        dataPack[7] = serialNums[1];

        dataPack[8] = 0x0A;
        dataPack[9] = 0x00;
        dataPack[10] = 0x12;
        //粤通卡协议所有数据长度 包括校验码
        dataPack[11] = (byte) (dataLength+12+1);

        dataPack[12] = 0x33;
        dataPack[13] = 0x06;
        dataPack[14] = (byte) 0x80;
        //粤通卡协议里的A3指令数据长度
        dataPack[15] = (byte) (dataLength+8);

        dataPack[16] = (byte) 0xA3;
        dataPack[17] = 0x00;
        //所有卡TLB格式指令数据长度
        dataPack[18] = TLBData[0];
        dataPack[19] = TLBData[1];

        dataPack[20] = (byte) 0x80;
        //所有卡指令数据长度
        dataPack[21] = (byte) (dataLength+2);

        dataPack[22] = (byte) 0x81;
        //当条指令长度
        dataPack[23] = (byte) dataLength;

        System.arraycopy(data1,0,dataPack,24,dataLength);

        dataPack[23+dataLength+1] = getVerifyCode(dataPack,13,23+dataLength);

        dataPack[23+dataLength+2] = 0x18;
        dataPack[23+dataLength+3] = 0x00;


        return dataPack;
    }

    /**
     * 将读卡指令封装成A3包
     * @param data
     * @return
     */
    public static byte[] getA3DataPack(byte[] data){
        int dataLength = data.length;
        byte[] TLBData = convertByteArray(dataLength+4);
        //流水号
        byte[] serialNums = intToByte2(serialNum);
        //使用完后加1
//        serialNum++;

        byte[]  dataPack = new byte[23+dataLength+4];
        byte[] dataPackLength = intToByte2(dataPack.length);


        dataPack[0] = IDENTIFY_DATA;
        dataPack[1] = mVer;
        //包的长度
        dataPack[2] = dataPackLength[0];
        dataPack[3] = dataPackLength[1];
        //命令
        dataPack[4] = 0x75;
        dataPack[5] = 0x31;
        //流水号
        dataPack[6] = serialNums[0];
        dataPack[7] = serialNums[1];

        dataPack[8] = 0x0A;
        dataPack[9] = 0x00;
        dataPack[10] = 0x12;
        //粤通卡协议所有数据长度 包括校验码
        dataPack[11] = (byte) (dataLength+12+1);

        dataPack[12] = 0x33;
        dataPack[13] = 0x06;
        dataPack[14] = (byte) 0x80;
        //粤通卡协议里的A3指令数据长度
        dataPack[15] = (byte) (dataLength+8);

        dataPack[16] = (byte) 0xA3;
        dataPack[17] = 0x00;
        //所有卡TLB格式指令数据长度
        dataPack[18] = TLBData[0];
        dataPack[19] = TLBData[1];

        dataPack[20] = (byte) 0x80;
        //所有卡指令数据长度
        dataPack[21] = (byte) (dataLength+2);

        dataPack[22] = (byte) 0x01;
        //当条指令长度
        dataPack[23] = (byte) dataLength;

        System.arraycopy(data,0,dataPack,24,dataLength);

        dataPack[23+dataLength+1] = getVerifyCode(dataPack,13,23+dataLength);

        dataPack[23+dataLength+2] = 0x18;
        dataPack[23+dataLength+3] = 0x00;

        Log.e("dataPack:",TransformUtils.byte2hex(dataPack));
        return dataPack;
    }

    /**
     * 下电数据包
     * @return
     */
    public static byte[] getPowerOffPack(){
        String strData = "FE010016753100190A001208330E8003A501C3EA1800";
        return TransformUtils.hexStringToBytes(strData);
    }
    /**
     * 判断是否为HG设备的数据包
     * @return
     */
    public static byte[] getCheckHGPack(){
        String strData = "FE010014753100020A00120633058001A2261800";
        return TransformUtils.hexStringToBytes(strData);
    }
}
