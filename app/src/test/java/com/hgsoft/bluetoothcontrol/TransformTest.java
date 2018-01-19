package com.hgsoft.bluetoothcontrol;



import com.hgsoft.bluetoothcontrol.util.TransformUtils;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by liyuxian on 2017/11/16.
 */

public class TransformTest {

    /**
     * 将数据拆分成二维数组
     * @param value
     */
    public void writeValue(byte[] value){
        byte[][] valueCarbonTemp = new byte[value.length / 20 + 1][20];
        for (int i = 0; i < valueCarbonTemp.length; i++) {
            for (int j = 0; j < 20; j++) {
                if ((20 * i + j) >= value.length) {
                    break;
                }
                valueCarbonTemp[i][j] = value[i * 20 + j];
            }
        }
        for (int i = 0; i < valueCarbonTemp.length; i++) {
//            mApp.cubicBLEDevice.writeValue(SERVICEUUID_WRITE, CHARACTIC_WRITE, valueCarbonTemp[i]);
        }
    }




    @Test
    public void hexStringToAlgorism() {
        String hex = "000021A2";
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }

        BigDecimal decimal = new BigDecimal(result);
        decimal = decimal.divide(new BigDecimal(100),2, RoundingMode.HALF_UP);

        System.out.print(decimal);
    }

    @Test
    public void test11(){
        //FE 01 006F 2712 0005 0A 00 12 61 33 06 81 5CB300005E
        //FE010027271200030A00121933058014B20011C034343032313636313130303035393137FB1800
        String str = "FE010027271200030A00121933058014B20011C034343032313636313130303035393137FB1800";
        String ss = "000021A29000";
        System.out.println("===");
//        System.out.println(str.substring(52,56));
        System.out.println(ss.substring(0,ss.length()-4));
    }

    @Test
    public void test12(){
        //4D616E756661637475726572204E616D65
        //HuaYun     48756159756e
        //读取的内容：48756159756E
        String source = "48";

        int code ;

        code = Integer.parseInt(source, 16);

        // 如果30代表是 16进制的30话，就取16
        // 如果30代表是 10进制的30话，就取10
        // code = Integer.parseInt(source, 10);

        char result = (char) code;

        System.out.println(result);
    }

    /**
     * 字节数组转为普通字符串（ASCII对应的字符）
     *
     * @return String
     */
    @Test
    public void bytetoString() {
        //4641495257494e
        byte[] bytearray = TransformUtils.hexStringToBytes("4641495257494E");
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        System.out.println(result);
    }


    @Test
    public  void getSplitArray( ){
//        String com = "FE010029753100030A00121B33068016A30012008010810700A40000021001020500B095002B891800";
        String com = "FE0100144E2300020A06080012024F4B10001800";

        byte[] bytes = hexStringToBytes(com);
        int subsize = 20;

        List<byte[]> lists = new ArrayList<>();
        try {
            int subSize =subsize;
            int count = bytes.length%subSize==0 ? bytes.length/subSize : bytes.length/subSize +1;
            List<List<Integer>> subAryList = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                int index = i * subSize;
                List<Integer> list = new ArrayList<>();
                int j = 0;
                while (j < subSize && index < bytes.length) {
                    list.add((int)bytes[index]);
                    index++;
                    j++;
                }
                subAryList.add(list);
            }

            for(int i = 0; i < subAryList.size(); i++){
                List<Integer> subList = subAryList.get(i);
                byte[] subAryItem = new byte[subList.size()];
                for(int j = 0; j < subList.size(); j++){
                    subAryItem[j] = (byte) subList.get(j).intValue();
                }
                lists.add(subAryItem);
                System.out.println("---分包----:"+byte2hex(subAryItem));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String byte2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException("Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    private byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }


    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    /**
     *
     */
    @Test
    public void convertByteArray() {
        int n = 256;
        byte[] buf = new byte[2];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (n >> (i*8) & 0xff);
        }
        String str = byte2hex(buf);
        System.out.println(str);
    }

    @Test
    public void getDataPack(){
        String data = "00A40000021001";
        byte[] data1 = TransformUtils.hexStringToBytes(data);
        int dataLength = data1.length;
        byte[] TLBData = convertByteArray(dataLength+4);
        //流水号
        byte[] serialNums = intToByte2(3);
        //使用完后加1

        byte[]  dataPack = new byte[23+dataLength+4];
        byte[] dataPackLength = intToByte2(dataPack.length);

        dataPack[0] = (byte) 0xfe;
        dataPack[1] = 0x01;
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
        //粤通卡协议所有数据长度
        dataPack[11] = (byte) (dataLength+12+1);

        dataPack[12] = 0x33;
        dataPack[13] = 0x06;//异或开始
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

        System.arraycopy(data1,0,dataPack,24,dataLength);

        dataPack[23+dataLength+1] = getVerifyCode(dataPack,13,23+dataLength);

        dataPack[23+dataLength+2] = 0x18;
        dataPack[23+dataLength+3] = 0x00;

        String str = byte2hex(dataPack);
        System.out.println(str);
    }


    //                                                          24
    //fe01 0049 2712 0003 0a 00 12 3b 33 06 80 36 B300003100812f02
    //2db9e3b6ab 0000000116104401000000 00000000022014071520201231d4c14131323334 35000000000100009000 451800
    @Test
    public void getB3Data(){
        String serverData = "fe010049271200030a00123b33068036B300003100812f022db9e3b6ab000000011610440100000000000000022014071520201231d4c1413132333435000000000100009000451800";
        String str = serverData.substring(48,serverData.length()-6);

        byte[] data = hexStringToBytes(str);

        String str1 = byte2hex(data);
        System.out.println(str1);


    }



    public static byte[] intToByte2(int i) {
        byte[] targets = new byte[2];
        targets[1] = (byte) (i & 0xFF);
        targets[0] = (byte) (i >> 8 & 0xFF);
        return targets;
    }

    protected  byte getVerifyCode(byte[] bytes, int start, int end){
        byte tmp =0x00;
        for(int i=start;i<=end;i++){
            tmp ^= bytes[i];
        }
        return tmp;
    }
    public  byte[] convertByteArray(int n) {
        byte[] buf = new byte[2];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (n >> (i*8) & 0xff);
        }
        return buf;
    }


    /**
     * int整数转换为2字节的byte数组
     *
     * @param
     *
     * @return byte数组
     */
    @Test
    public void intToByte2() {
        int i = 34;
        byte[] targets = new byte[2];
        targets[1] = (byte) (i & 0xFF);
        targets[0] = (byte) (i >> 8 & 0xFF);
//        return targets;
    }

}
