package com.cycloneboy.udpdemo.utils;

/**
 * Created by CycloneBoy on 2017/12/9.
 */
final public class HexUtils {
    /**
     * 字符串转换成十六进制字符串
     *
     * @param str
     *            str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexStr
     *            str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param  b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
//          sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     *
     * @param strText
     *            strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText) throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else
                // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     *
     * @param hex
     *            hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    /**
     *将32位的int值放到4字节的byte数组
     * @param num
     * @return
     */
    public static byte[] intToByteArray(int num) {
        byte[] result = new byte[4];
        result[0] = (byte)(num >>> 24);//取最高8位放到0下标
        result[1] = (byte)(num >>> 16);//取次高8为放到1下标
        result[2] = (byte)(num >>> 8); //取次低8位放到2下标
        result[3] = (byte)(num );      //取最低8位放到3下标
        return result;
    }

    /**
     * 将4字节的byte数组转成一个int值
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte[] b){
        byte[] a = new byte[4];
        int i = a.length - 1,j = b.length - 1;
        for (; i >= 0 ; i--,j--) {//从b的尾部(即int值的低位)开始copy数据
            if(j >= 0)
                a[i] = b[j];
            else
                a[i] = 0;//如果b.length不足4,则将高位补0
        }
        int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
        int v1 = (a[1] & 0xff) << 16;
        int v2 = (a[2] & 0xff) << 8;
        int v3 = (a[3] & 0xff) ;
        return v0 + v1 + v2 + v3;
    }

    /**
     * float转换byte
     *
     * @param bb
     * @param x
     * @param index
     */
    public static void putFloat(byte[] bb, float x, int index) {
        // byte[] b = new byte[4];
        int l = Float.floatToIntBits(x);
        for (int i = 0; i < 4; i++) {
            bb[index + i] = new Integer(l).byteValue();
            l = l >> 8;
        }
    }

    /**
     * 通过byte数组取得float
     *
     * @param b
     * @param index
     * @return
     */
    public static float getFloat(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static void main(String[] args) {
//        String hex = "ef2c71b29202f3e642f2abd8d518f367ec3fbf6a6a61beb678ae0c871ee368ac";
//        System.out.println(HexUtils.hexStr2Str(hex));
//        byte[] temp = HexUtils.hexStr2Bytes(hex);
//        for (int i = 0; i < temp.length; i++) {
//            byte b = temp[i];
//            System.out.print(b + " ");
//        }

        byte[] cmd = new byte[7];
        cmd[0] =(byte)  0xa5;
        cmd[1] =(byte)  0xa5;
        cmd[2] =(byte)  0x20;
        cmd[3] =(byte)  0x01;
        cmd[4] =(byte)  0x55;
        cmd[5] =(byte)  0xc8;
        cmd[6] =(byte)  0xc8;

        String str = HexUtils.byte2HexStr(cmd);
        System.out.println(str);

        float[] fReceiveData = new float[10];//udp 接收数据处理
        byte[] RcvMsgByte  = new byte[1024]; //接收消息

        //A5A55028 575F8242 A61E8F49 195F9341 00005043 000060C3 0F17BCBE 7F16153F 4907A800 179D9F43 5438AF4A C8C8
        RcvMsgByte[0] =(byte)0xa5;
        RcvMsgByte[1] =(byte)0xa5;

        RcvMsgByte[2] =(byte)0x50;
        RcvMsgByte[3] =(byte)0x28;

        RcvMsgByte[4] =(byte)0x57;
        RcvMsgByte[5] =(byte)0x5F;
        RcvMsgByte[6] =(byte)0x82;
        RcvMsgByte[7] =(byte)0x42;

        RcvMsgByte[8] =(byte)0xA6;
        RcvMsgByte[9] =(byte)0x1E;
        RcvMsgByte[10] =(byte)0x8F;
        RcvMsgByte[11] =(byte)0x49;

        RcvMsgByte[12] =(byte)0x19;
        RcvMsgByte[13] =(byte)0x5F;
        RcvMsgByte[14] =(byte)0x93;
        RcvMsgByte[15] =(byte)0x41;

        RcvMsgByte[16] =(byte)0xc8;
        RcvMsgByte[17] =(byte)0xc8;

        for(int i= 0; i < 3;i++){
            fReceiveData[i] = HexUtils.getFloat(RcvMsgByte,4+ 4*i);
            System.out.println(fReceiveData[i] );
        }


        //A5A550287A3398424D1E8F495A4496410000000000000000A8D185BE6A1B753E4D07A8009756BA43E737AF4AC8C8

    }
}
