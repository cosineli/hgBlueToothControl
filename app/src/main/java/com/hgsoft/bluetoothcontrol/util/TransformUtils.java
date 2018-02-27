package com.hgsoft.bluetoothcontrol.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.HANDLE_MESSAGE_AUTH_REQUEST;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.HANDLE_MESSAGE_INIT_REQUEST;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.HANDLE_MESSAGE_RESPONSE_INIT_DATA;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.HANDLE_MESSAGE_RESPONSE_WRITE_DATA;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.authCommand;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.initCommand;
import static com.hgsoft.bluetoothcontrol.config.BlueDeviceConfig.responseDataCommand;

/**
 * Created by liyuxian on 2017/11/16.
 */

public class TransformUtils {
    private static String TAG = TransformUtils.class.getSimpleName();
    /**
     * 保存完整的从设备接收的包
     */
    private static StringBuilder serverData = new StringBuilder();

    private static int serverDataLength = 0;

    /**
     * 保存单个分包的数据
     */
    private static StringBuilder onePackageData = new StringBuilder();
    private static int packageDataLength = 0;

    private static boolean hasOtherPackage = false;
    /**
     * 是否最后一包数据
     */
    private static boolean isLastOne = false;
    /**
     * 保存所有分包的数据
     */
    private static List<String> packageDatas = new ArrayList<>();
    /**
     * 是否接收完所有数据
     */
    private static boolean isFinish = false;
    /**
     * 判断从设备上是否接收完整个通信包
     * //fe 01 001a 2711 0003 0a 00 18 84 80 04 20 01 28 02 3a 06
       //00 17 ea 8e 22 fd
         fe 01 001a 2711 0004 0a 00 18848004200128023a060017ea8e22fd

         FE 01 006F 2712 0005 0A 00 12 61 33 06 81 5CB300005E

     FE01001A271100010A001884 8004 20 0128023A06

     FE01001A271200060A00120C 3306 00 07 FFFFFFFFFF9000 6E1800
     * @param str
     * @return
     */
    public static boolean isFullServerData(String str){
        //是否是包的头部
        if(str.toLowerCase().startsWith("fe01")){
            //判断是否有分包
            String command = str.substring(24,26);
            String p = str.substring(28,30);
            if("33".equals(command)&&!"80".equals(p)){ //分包

                //////////////分包//////////////////////
                hasOtherPackage = true;
                onePackageData = new StringBuilder();
                onePackageData.append(str);
                //包的长度
                packageDataLength = hexStringToAlgorism(str.substring(4,8))*2;

                if("00".equals(p)){//分包结束，最后一个包
                    isLastOne = true;
                }

                if(packageDataLength>str.length()){ //数据未接收完
                    return false;
                }else if(packageDataLength==str.length()){ //数据接收完成
                    packageDatas.add(onePackageData.toString());
                    hasOtherPackage = false;
                    isLastOne = false;
                    return true;
                }

            }else {
                //////////////不分包////////////
                serverData = new StringBuilder();
                serverData.append(str);
                //包的长度
                serverDataLength = hexStringToAlgorism(str.substring(4,8))*2;
                if(serverDataLength>str.length()){ //数据未接收完
                    return false;
                }else if(serverDataLength==str.length()){ //数据接收完成
                    return true;
                }
            }

        }else {
            if(hasOtherPackage){
                onePackageData.append(str);
                isFinish = false;
                if(packageDataLength==onePackageData.length()){
                    packageDatas.add(onePackageData.toString());
                    if(isLastOne){
                        isFinish =true;
                        hasOtherPackage = false;
                        isLastOne = false;
                    }
                }
            }else {
                serverData.append(str);
                isFinish = serverDataLength==serverData.length();
            }
        }

        return isFinish;
    }



    /**
     * 获取设备上传的数据
     * @return
     */
    public static String getServerData(){
        //有分包
        if(packageDatas!=null&&packageDatas.size()>0){
            StringBuilder data = new StringBuilder();
            for(int i=0,len=packageDatas.size();i<len;i++){
                String ss;
                if(i==0){
                    String tmp = packageDatas.get(i);
                    ss = tmp.substring(0,tmp.length()-6);
                } else if(len>1&&i==len-1){
                    String tmp1 = packageDatas.get(i);
                    ss = tmp1.substring(32);
                } else {
                    String tmp2 = packageDatas.get(i);
                    ss = tmp2.substring(32,tmp2.length()-6);
                }
                data.append(ss);
            }
            packageDatas.clear();
            return data.toString();
        }else {
            if (serverData!=null) {
                return serverData.toString();
            }else {
                serverData = new StringBuilder();
                return serverData.toString();
            }
        }


    }




    /**
     * 判断是否Auth验证包
     *  //fe 01 001a 2711 0004 0a 00 18848004200128023a060017ea8e22fd
     * @return
     */
    public static boolean isAuthData(String data){
        String authCommand = data.substring(8,12);
        return "2711".equals(authCommand);
    }

    /**
     * 判断命令类型
     * @param data
     * @return
     */
    public static int getCommandType (String data){
        String command = data.substring(8,12);
        //                                      16
        //FE01 0027 2712 0003 0A00 1219 3305 80 14 B2 0011 C0 34343032313636313130303035393137 FB1800
        int type =0;
        switch (command){
            case authCommand:
                type = HANDLE_MESSAGE_AUTH_REQUEST;
                break;
            case initCommand:
                type = HANDLE_MESSAGE_INIT_REQUEST;
                break;
            case responseDataCommand:

//                if(data.length()>34 && "B2".equals(data.substring(32,34))){
//
//                    type = HANDLE_MESSAGE_RESPONSE_INIT_DATA;
//                }else {
//
//                }
                type = HANDLE_MESSAGE_RESPONSE_WRITE_DATA;

                break;

        }

        return type;

    }
    //                                                             25
    //fe01 0049 2712 0003 0a 00 12 3b 33 06 80 36 B3 00003100812f022d
    //b9e3b6ab 0000000116104401000000 00000000022014071520201231d4c14131323334 35000000000100009000 451800
    //FE01 001A 2712 0003 0A 00 12 0C 33 06 80 07 B3 000002008100B1 1800
    //FE01 006F 2712 000C 0A 00 12 61 33 06 81 5C B3 00005E00815C015A FFFFFFFFFFFFFFFF5100001199904426030002FFFFFFFF201505182025123120150518038122010100000000067F000101115100001199904426720200000000FF8000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF501800
    //                                                                ffffffffffffffff5100001199904426030002ffffffff201505182025123120150518038122010100000000067f000101115100001199904426720200000000ff8000ffffffffffffffffffffffffffffffff
    /**
     * 获取返回的B3数据
     * @param serverData
     * @return
     */
    public static byte[] getB3Data(String serverData){
        if(serverData.length()<56){
            return null;
        }
        String str = serverData.substring(50,serverData.length()-6);
        return hexStringToBytes(str);
    }
    //0
    //FE010018271200030A00120A33068005B3CF000000FF1800  无卡返回
    //FE01002A271200040A00121C33068017B3000012008110010E6F0A84085041592E415050599000A91800 有卡返回
    public static byte[] powerOnResult(String serverData){
        if(serverData.length()<38){
            return new byte[0];
        }
        String str = serverData.substring(34);
        return hexStringToBytes(str);
    }

 /**
     * 判断是否为HG设备
     * @param serverData
     * @return
     */
    public static boolean isHGDevice(String serverData){
        //                                         17      20                   27
        //FE01 0027 2712 0003 0A00 1219 3305 80 14 B2 0011 C0 34 34 30 32 31 36 36 313130303035393137 FB1800

        if(serverData.length()<60){
            return false;
        }
        String str = serverData.substring(52,54);
        return "36".equals(str);
    }










    /**
     * 将数据切割成若干个byte[]
     * @param data
     * @param subsize
     * @return
     */
    public static List<byte[]> getSplitArray(String data,int subsize){
        byte[] bytes = hexStringToBytes(data);
        return getSplitArray(bytes,subsize);
    }


    /**
     * 将byte[] 分割成若干个byte[]
     * @param bytes original bytes
     * @param subSize sub byte
     * @return list<byte[]>
     */
    public static List<byte[]> getSplitArray(byte[] bytes,int subSize){

        List<byte[]> lists = new ArrayList<>();
        try {
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
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return lists;
    }



    /**
     * 字节数组转换为十六进制字符串
     *
     * @param b
     *            byte[] 需要转换的字节数组
     * @return String 十六进制字符串
     */
    public static String byte2hex(byte b[]) {
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



    /**
     * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }


    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
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

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }





    /**
     * 十六进制字符串转十进制
     *
     * @param hex
     *            十六进制字符串
     * @return 十进制数值
     */
    private static int hexStringToAlgorism(String hex) {
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
        return result;
    }
  /**
     * 十六进制字符串转十进制,保留两位小数
     *
     * @param hex
     *            十六进制字符串
     * @return 十进制数值
     */
    public static BigDecimal hexStringToDecimal(String hex) {

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

        return decimal;
    }



}
