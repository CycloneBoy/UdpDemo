package com.cycloneboy.udpdemo;

import android.content.Intent;
import android.util.Log;
import com.cycloneboy.udpdemo.utils.HexUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.Arrays;

/**
 * Created by CycloneBoy on 2017/12/9.
 */
public class UDPClient implements Runnable{
    final static int CLIENT_PORT = 6000; // 安卓本机  端口
    final static String HOST_IP = "192.168.1.102";
    final static int HOST_PORT = 2040;
    private static DatagramSocket socket = null;
    private static DatagramPacket packetSend,packetRcv;
    private boolean udpLife = true; //udp生命线程
    private byte[] msgRcv = new byte[1024]; //接收消息

    private String hostIp=null;
    private int hostPort=0;

    private float[] fReceiveData = new float[10];//udp 接收数据处理

    public UDPClient(){
        super();
        this.hostIp = HOST_IP;
        this.hostPort = HOST_PORT;
    }

    public UDPClient(String hostIp,int hostPort){
        super();
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    //返回udp生命线程因子是否存活
    public boolean isUdpLife(){
        if (udpLife){
            return true;
        }

        return false;
    }

    //更改UDP生命线程因子
    public void setUdpLife(boolean b){
        udpLife = b;
    }

    //发送消息
    public String send(String msgSend){
        this.send(msgSend.getBytes(),msgSend.length());
        return msgSend;
    }


    //发送消息
    public String send(byte[] msgSend,int msgLength){
        InetAddress hostAddress = null;

        try {
            hostAddress = InetAddress.getByName(hostIp);
        } catch (UnknownHostException e) {
            Log.i("udpClient","未找到服务器");
            e.printStackTrace();
        }

        packetSend = new DatagramPacket(msgSend,msgLength ,hostAddress,hostPort);

        try {
            socket.send(packetSend);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("udpClient","发送失败");
        }
        //   socket.close();
        return "success";
    }

    @Override
    public void run() {

        try {
            socket = new DatagramSocket(CLIENT_PORT);
            socket.setSoTimeout(3000);//设置超时为3s
            System.out.println(" udp 设置端口号：" + socket.getPort());
        } catch (SocketException e) {
            Log.i("udpClient","建立接收数据报失败");
            e.printStackTrace();
        }
        packetRcv = new DatagramPacket(msgRcv,msgRcv.length);
        while (udpLife){
            try {
                Log.i("udpClient", "UDP监听");
                socket.receive(packetRcv);

                String RcvMsg = null;
                byte[] RcvMsgByte = Arrays.copyOf(packetRcv.getData(),packetRcv.getLength());

                String recvCmdStr =null;
                String strTemp = null;
                int MsgLength = packetRcv.getLength();

                //判断接收到的是命令还是显示数据
                if(MsgLength >= 6  && RcvMsgByte[3] == MsgLength - 6 &&
                        RcvMsgByte[0] == (byte) 0xa5 && RcvMsgByte[1] == (byte)0xa5 &&
                        RcvMsgByte[ 4 + RcvMsgByte[3]] == (byte) 0xc8 && RcvMsgByte[ 5 + RcvMsgByte[3]] == (byte) 0xc8)
                {

                    switch (RcvMsgByte[2]){
                        case 0x01:{
                            System.out.println("接收到命令 0x01");
                        }
                        break;
                        case 0x02:{
                            System.out.println("接收到命令 0x02");
                        }
                        break;
                        case 0x03:{
                            System.out.println("接收到命令 0x03");
                        }
                        break;
                        case ConstParam.RECEIVE_ROBOT_DATA:{  //接收robot数据

                            // fMotorAMileage，fMotorBMileage,yaw ，Speed，Speed_MotorB，
                            // pitch,roll,temperature，Cylinder_Number_MotorA，Cylinder_Number_MotorB
                            for(int i= 0; i < 10;i++){
                                fReceiveData[i] = HexUtils.getFloat(RcvMsgByte,4+ 4*i);
                                System.out.println(fReceiveData[i]);
                            }
                            String strRobot = String.format("robot data : %.3f %.3f %.2f | %.2f %.2f | %.2f\r\n" ,
                                    fReceiveData[0],fReceiveData[1],fReceiveData[2],fReceiveData[3],fReceiveData[4], fReceiveData[5] );
                            System.out.println("接收到 robot数据");
                        }
                        break;
                        default:{
                            System.out.println("接收到其他命令");
                        }
                        break;

                    }
                    RcvMsg = HexUtils.byte2HexStr(RcvMsgByte);
                    strTemp = "接收到命令："+RcvMsg;
                }else{
                    RcvMsg = new String(packetRcv.getData(),packetRcv.getOffset(),packetRcv.getLength());
                    strTemp = "接收到数据：" + RcvMsg;
                }

                System.out.println("接收到数据1) ：" +packetRcv.getOffset() + " 长度： " + packetRcv.getLength());
                System.out.println("接收到数据2) ："+ HexUtils.byte2HexStr(RcvMsgByte));

                //将收到的消息发给主界面
                System.out.println(" UDP 接收到一包数据： " + packetRcv.getAddress().toString() + " : " + packetRcv.getPort());
                Intent RcvIntent = new Intent();
                RcvIntent.setAction("udpRcvMsg");
                RcvIntent.putExtra("udpRcvMsg", RcvMsg);
                RcvIntent.putExtra("udpRcvMsgByte",RcvMsgByte);
                RcvIntent.putExtra("udpRobotReceiveData",fReceiveData); //robot 传输数据
                MainActivity.context.sendBroadcast(RcvIntent);

                Log.i("Rcv",RcvMsg);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        Log.i("udpClient","UDP监听关闭");
        socket.close();
    }
}
