package com.hgsoft.bluetoothcontrol.dataPack;



import com.hgsoft.bluetoothcontrol.util.TransformUtils;


/**
 * Created by liyuxian on 2017/1/12.
 */

public class BasePack {

    private static final String TAG ="BasePack";
    //pack start and end tag
    protected final static byte IDENTIFY_DATA = (byte) 0xfe;

    //Msg title
    protected static byte mVer= 0x01;// Version

    static int serialNum = 3;

    //verify code
//    protected static byte[] mVerifyCode = {0x00};




    /**
     * 发送Auth请求的响应包
     *  //fe 01 000e 4e21 0001  0a 02 08 00 12 00
     * @param msgSerialNumber
     * @return
     */
    public static byte[] sendAuthRespon(String msgSerialNumber){
        byte[] serialNum = TransformUtils.hexStringToBytes(msgSerialNumber);
        byte[] data = new byte[14];
        data[0] = IDENTIFY_DATA;
        data[1] = mVer;

        data[2] = 0x00;
        data[3] = 0x0e;

        data[4] = 0x4e;
        data[5] = 0x21;

        data[6] = serialNum[0];
        data[7] = serialNum[1];

        data[8] = 0x0a;
        data[9] = 0x02;
        data[10] = 0x08;
        data[11] = 0x00;
        data[12] = 0x12;
        data[13] = 0x00;
        return data;
    }



    static byte getVerifyCode(byte[] bytes, int start, int end){
        byte tmp =0x00;
        for(int i=start;i<=end;i++){
            tmp ^= bytes[i];
        }
        return tmp;
    }



    /**
     * int整数转换为2字节的byte数组
     *
     * @param i
     *            整数
     * @return byte数组
     */
    static byte[] intToByte2(int i) {
        byte[] targets = new byte[2];
        targets[1] = (byte) (i & 0xFF);
        targets[0] = (byte) (i >> 8 & 0xFF);
//        Log.e(TAG,"0------:"+targets[0]);
//        Log.e(TAG,"1------:"+targets[1]);
        return targets;
    }


    /**
     *将int数据转换成 低位在前 的byte[]
     * byte[]长度为2
     * @param n
     * @return
     */
    static byte[] convertByteArray(int n) {
//        short n = 257;
        byte[] buf = new byte[2];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (n >> (i*8) & 0xff);
        }
        return buf;
//        String str = byte2hex(buf);
//        System.out.println(str);
    }



    /**
     * 将byte[] 分割成若干个byte[],重新按照蓝牙下发的格式组成新的byte[]放入集合中
     * 新的byte[]的长度为15，格式：第一位是有效元素数量，其余为有效元素，当有效元素不足14个时，后面元素填0；
     * 例如：
     *       原始byte[]:           7E20160728000500900102810107F77E
     * 分割后重新组合成2个新byte[]:
     *                            0E7E20160728000500900102810107
     *                            02F77E000000000000000000000000
     * 将这两个byte[]放入集合中返回
     * @param bytes original byte[]
     * @return List<byte[]>
     */
//    protected static List<byte[]> getAssignedPack(byte[] bytes){
//        List<byte[]> list = new ArrayList<>() ;
//        List<byte[]> list1= TransformUtils.getSplitArray(bytes,14);
//        for(int i=0;i<list1.size();i++){
//            byte[] b = getPack(list1.get(i));
//            list.add(b);
////            Log.e("BasePack","---组包----:"+ byte2hex(b));
//        }
//        return list;
//    }

    private static byte[] getPack(byte[] bytes){
        byte[] temp =null;
        if(bytes.length<14){
            temp = new byte[15];
//            temp[0]= Byte.parseByte(Integer.toHexString(bytes.length));
            temp[0]= (byte) bytes.length;
            for(int i=0;i<bytes.length;i++){
                temp[i+1]=bytes[i];
            }
            int j = 14-bytes.length;
            if(j>0){
                for(int i=1;i<=j;i++){
                    temp[bytes.length+i] = 0x00;
                }
            }
        }else if(bytes.length==14){
            temp = new byte[15];
            temp[0] = 0x0e;
            for(int i=0;i<bytes.length;i++){
                temp[i+1]=bytes[i];
            }
        }
        return temp;
    }






}
