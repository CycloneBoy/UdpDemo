package com.cycloneboy.udpdemo;

import android.os.Message;

/**
 * Created by CycloneBoy on 2017/12/9.
 */
public class MsgSendThread implements Runnable{
    private String strMsg;
    private Byte[] bytesMsg;
    private UDPClient client = null;
    private MainActivity.MyHandler myHandler;

    public MsgSendThread(UDPClient client,String strMsg,MainActivity.MyHandler myHandler) {
        this.client = client;
        this.strMsg = strMsg;
        this.myHandler = myHandler;
    }

    public MsgSendThread(UDPClient client,Byte[] bytesMsg,MainActivity.MyHandler myHandler) {
        this.client = client;
        this.bytesMsg = bytesMsg;
        this.myHandler = myHandler;
    }


    @Override
    public void run() {
        Message message = new Message();
        message.what = ConstParam.MSG_TYPE_SEND;
        if (strMsg.trim()!=""){
            client.send(strMsg);
            message.obj = strMsg;
            myHandler.sendMessage(message);
        }else if (bytesMsg != null){
            //client.send(bytesMsg,bytesMsg.length);
            message.obj = strMsg;
            myHandler.sendMessage(message);
        }


    }

    public String getStrMsg() {
        return strMsg;
    }

    public void setStrMsg(String strMsg) {
        this.strMsg = strMsg;
    }

    public Byte[] getBytesMsg() {
        return bytesMsg;
    }

    public void setBytesMsg(Byte[] bytesMsg) {
        this.bytesMsg = bytesMsg;
    }
}
