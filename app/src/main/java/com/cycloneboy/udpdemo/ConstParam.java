package com.cycloneboy.udpdemo;

/**
 * Created by CycloneBoy on 2017/12/9.
 */
public  class ConstParam {

    // UI MSG TYPE
    public static final int MSG_TYPE_SHOW_RCV = 1;
    public static final int MSG_TYPE_SEND = 2;
    public static final int MSG_TYPE_SHOW = 3;
    public static final int MSG_TYPE_SHOW_RCV_ROBOT_DATA = 4;


    // 发送的控制命令ID
    public static final byte SEND_CMD_TURN_STOP = 0;       // 停止
    public static final byte SEND_CMD_TURN_FORWARD = 1;    // 向前
    public static final byte SEND_CMD_TURN_BACKWARD = 2;   // 向后
    public static final byte SEND_CMD_TURN_LEFT = 3;       // 向左
    public static final byte SEND_CMD_TURN_RIGHT = 4;      // 向右
    public static final byte SEND_CMD_TURN_CLOCKWISE = 5;       // 逆时针
    public static final byte SEND_CMD_TURN_ANTICLOCKWISE = 6;       // 逆时针
    public static final byte SEND_CMD_CALIBRATION_DIRECTION = 7;       //校准方向
    public static final byte SEND_CMD_OTHER_CMD = 8;       //其他命令

    // 发送 GPS数据
    public static final byte SEND_DATA_GPS =  (byte)0x50;        //发送GPS 数据
    public static final byte SEND_DATA_SET_COURSE =  (byte)0x30;        //发送设定航向数据
    public static final byte SEND_DATA_STOP_COURSE =  (byte)0x31;        //发送停止航向数据
    public static final byte SEND_DATA_SET_ROBOT_SPEED =  (byte)0x32;        //发送设定速度 数据


    // 接收数据命令ID
    public static final byte RECEIVE_ROBOT_DATA = (byte) 0x80;       //接收STM32F407 ROBOT data


}
