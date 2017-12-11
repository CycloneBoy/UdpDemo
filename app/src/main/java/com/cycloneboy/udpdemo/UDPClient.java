package com.cycloneboy.udpdemo;

import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;

/**
 * Created by CycloneBoy on 2017/12/9.
 */
public class UDPClient implements Runnable{
    final static int CLIENT_PORT = 6000; // 安卓本机  端口
    final static String HOST_IP = "192.168.31.199";
    final static int HOST_PORT = 5000;
    private static DatagramSocket socket = null;
    private static DatagramPacket packetSend,packetRcv;
    private boolean udpLife = true; //udp生命线程
    private byte[] msgRcv = new byte[1024]; //接收消息

    private String hostIp=null;
    private int hostPort=0;

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
                String RcvMsg = new String(packetRcv.getData(),packetRcv.getOffset(),packetRcv.getLength());
                //将收到的消息发给主界面
                System.out.println(" UDP 接收到一包数据： " + packetRcv.getAddress().toString() + " : " + packetRcv.getPort());
                Intent RcvIntent = new Intent();
                RcvIntent.setAction("udpRcvMsg");
                RcvIntent.putExtra("udpRcvMsg", RcvMsg);
                RcvIntent.putExtra("udpRcvMsgByte",packetRcv.getData());
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
