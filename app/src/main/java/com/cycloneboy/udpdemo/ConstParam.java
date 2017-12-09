package com.cycloneboy.udpdemo;

/**
 * Created by CycloneBoy on 2017/12/9.
 */
public  class ConstParam {

    // UI MSG TYPE
    public static final int MSG_TYPE_SHOW_RCV = 1;
    public static final int MSG_TYPE_SEND = 2;
    public static final int MSG_TYPE_SHOW = 3;

    // 发送的控制命令ID
    public static final byte SEND_CMD_TURN_FORWARD = 1;    // 向前
    public static final byte SEND_CMD_TURN_LEFT = 2;       // 向左
    public static final byte SEND_CMD_TURN_RIGHT = 3;      // 向右
    public static final byte SEND_CMD_TURN_STOP = 4;       // 停止
    public static final byte SEND_CMD_TURN_BACKWARD = 5;   // 向后

}
