package com.cycloneboy.udpdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.cycloneboy.udpdemo.utils.HexUtils;
import com.cycloneboy.udpdemo.utils.LocationUtils;

import java.lang.ref.WeakReference;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private  static  String UdpTag = "UDP";

    TextView tvIpInfo,txt_Info;
    Button  btn_Send,btn_UdpConn,btn_UdpClose;
    Button  btnForward,btnTurnLeft,btnTurnRight,btnStop,btnBackward;
    Button  btnTurnClockwise,btnTurnAntiClockwise,btnCalibrationDirection,btnOtherCmd; //旋转按钮

    Button btnSendGps;

    EditText edit_Send,editTextIP,editTextPort;

    EditText editSetCourse,editSetCourseP,editSetCourseI,editSetCourseD,editSetRobotSpeed;
    Button btnSendSetCourse,btnSendStopCourse,btnSendSetRobotSpeed;


    private UDPClient client = null;
    public static Context context;
    private final MyHandler myHandler = new MyHandler(this);
    private StringBuffer udpRcvStrBuf=new StringBuffer(),udpSendStrBuf=new StringBuffer();

    private  byte[] sendCmdBuf = new byte[1024];
    MyBtnClick myBtnClick = new MyBtnClick();

    // 添加获取GPS
    private TextView postionView;
    private LocationManager locationManager;
    private String locationProvider;

    private boolean flag; //GPS

    //获取位置变化结果
    public float gpsGaccuracy =0.0f;//精确度，以米为单位
    public float gpsAltitude=0.0f ;//获取海拔高度
    public float gpsLongitude=0.0f ;//经度
    public float gpsLatitude=0.0f ;//纬度
    public float gpsSpeed=0.0f ;//速度

    //ROBOT 数据
    private float[] fRobotData = new float[10]; //ROBOT DATA
    private TextView robotView;

    // 定时发送GPS 数据

   //private Handler handlerSendGps = new Handler();

    private Timer timerSendGps = new Timer();

    TimerTask taskSendGps ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("sys init " + getLocalIp() + " : " + client.CLIENT_PORT);

        tvIpInfo = (TextView)findViewById(R.id.tvIpInfo);
        tvIpInfo.append(getLocalIp() + " : " + client.CLIENT_PORT);


        context = this;
        bindWidget();       //控件绑定
        bindListening();    //监听事件
        bindReceiver();     //注册broadcastReceiver接收器
        iniWidget();        //初始化控件状态
        initLocation();     //初始化定位GPS相关

        SetControlButtonEnableState(false);//设置控制按钮状态

    }

    private void bindWidget(){
        txt_Info = (TextView)findViewById(R.id.txt_Info);
        btn_Send = (Button)findViewById(R.id.btn_Send);
        btn_UdpConn = (Button)findViewById(R.id.btn_udpConn);
        btn_UdpClose = (Button)findViewById(R.id.btn_udpClose);

        edit_Send = (EditText)findViewById(R.id.edit_Send);
        editTextPort = (EditText)findViewById(R.id.editTextPort);
        editTextIP = (EditText)findViewById(R.id.editTextIP);

        btnForward = (Button)findViewById(R.id.btnForward);
        btnTurnLeft = (Button)findViewById(R.id.btnTurnLeft);
        btnTurnRight= (Button)findViewById(R.id.btnTurnRight);
        btnStop= (Button)findViewById(R.id.btnStop);
        btnBackward= (Button)findViewById(R.id.btnBackward);

        // 2017-12-11 09:00 添加旋转功能按钮
        btnTurnClockwise = (Button)findViewById(R.id.btnTurnClockwise);
        btnTurnAntiClockwise = (Button)findViewById(R.id.btnTurnAntiClockwise);
        btnCalibrationDirection = (Button)findViewById(R.id.btnCalibrationDirection);
        btnOtherCmd  = (Button)findViewById(R.id.btnOtherCmd);

        // 2017-12-30 22:32 添加发送GPS按钮
        btnSendGps =(Button)findViewById(R.id.btn_SendGps);

        robotView = (TextView)findViewById(R.id.robotView);

        // 2017-12-31 19:53 添加发送航向保持
        editSetCourse= (EditText)findViewById(R.id.edit_setCourse);
        editSetCourseP = (EditText)findViewById(R.id.edit_setCourseP);
        editSetCourseI = (EditText)findViewById(R.id.edit_setCourseI);
        editSetCourseD = (EditText)findViewById(R.id.edit_setCourseD);
        editSetRobotSpeed = (EditText)findViewById(R.id.edit_setMotorSpeed);

       btnSendSetCourse = (Button)findViewById(R.id.btn_SendSetCourse);
       btnSendStopCourse = (Button)findViewById(R.id.btn_SendStopCourse);
       btnSendSetRobotSpeed =(Button)findViewById(R.id.btn_SendSetMotorSpeed);

    }

    private void bindListening(){
        btn_Send.setOnClickListener(myBtnClick);
        btn_UdpConn.setOnClickListener(myBtnClick);
        btn_UdpClose.setOnClickListener(myBtnClick);

        btnForward.setOnClickListener(myBtnClick);
        btnTurnLeft.setOnClickListener(myBtnClick);
        btnTurnRight.setOnClickListener(myBtnClick);
        btnStop.setOnClickListener(myBtnClick);
        btnBackward.setOnClickListener(myBtnClick);

        // 2017-12-11  09:00 添加旋转功能按钮
        btnTurnClockwise.setOnClickListener(myBtnClick);
        btnTurnAntiClockwise.setOnClickListener(myBtnClick);
        btnCalibrationDirection.setOnClickListener(myBtnClick);
        btnOtherCmd.setOnClickListener(myBtnClick);

        // 2017-12-30 22:32 添加发送GPS按钮
        btnSendGps.setOnClickListener(myBtnClick);

        // 2017-12-31 19:53 添加发送航向保持
        btnSendSetCourse.setOnClickListener(myBtnClick);
        btnSendSetRobotSpeed.setOnClickListener(myBtnClick);
        btnSendStopCourse.setOnClickListener(myBtnClick);
    }

    private void bindReceiver(){
        IntentFilter udpRcvIntentFilter = new IntentFilter("udpRcvMsg");
        registerReceiver(broadcastReceiver,udpRcvIntentFilter);
    }

    private void iniWidget(){
        btn_Send.setEnabled(false);
    }

    private void initLocation(){
        //获取显示地理位置信息的TextView
        postionView = (TextView) findViewById(R.id.positionView);
        //获取地理位置管理器
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        for (String string : providers) {
            System.out.println("所有定位方式：>>>"+string);
        }
        if(providers.contains(LocationManager.GPS_PROVIDER)){
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        }else if(providers.contains(LocationManager.NETWORK_PROVIDER)){
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }else{
            Toast.makeText(this, "没有可用的位置提供器:请检查网络或GPS是否打开", Toast.LENGTH_SHORT).show();
            return ;
        }
        //获取Location
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if(location!=null){
            //不为空,显示地理位置经纬度
            showLocation(location);
        }
        //监视地理位置变化
        locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
    }

    /**
     * 显示地理位置经度和纬度信息
     * @param location
     */
    private void showLocation(Location location){
        //获取位置变化结果
        float accuracy = location.getAccuracy();//精确度，以米为单位
        double altitude = location.getAltitude();//获取海拔高度
        double longitude = location.getLongitude();//经度
        double latitude = location.getLatitude();//纬度
        float speed = location.getSpeed();//速度

        // 获取GPS 信息 全局变量
        gpsGaccuracy = accuracy;
        gpsAltitude =(float) altitude;
        gpsLongitude = (float) longitude;
        gpsLatitude = (float) latitude;
        gpsSpeed = speed;

        //显示位置信息
        StringBuffer sb= new StringBuffer();
        sb.append("accuracy:"+accuracy+" ");
        sb.append("altitude:"+altitude+"");
        sb.append("longitude:"+longitude+" ");
        sb.append("latitude:"+latitude+" ");
        sb.append("speed:"+speed+" ");


        String locationStr = "GPS坐标：经度：" + location.getLongitude() + "纬度：" + location.getLatitude();
        postionView.setText(locationStr);
        System.out.println(locationStr + sb.toString());
    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */
    LocationListener locationListener =  new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        //位置改变的时候调用，这个方法用于返回一些位置信息
        @Override
        public void onLocationChanged(Location location) {
            //如果位置发生变化,重新显示
            showLocation(location);

        }
    };

    public class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        String strTemp = null;
        public MyHandler(MainActivity activity){
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ConstParam.MSG_TYPE_SHOW_RCV:
                    String recvStr = msg.obj.toString();

                    udpRcvStrBuf.append(msg.obj.toString());
                    strTemp = "接收到数据：" + msg.obj.toString();
                    txt_Info.setText(strTemp);
                    //Log.i(UdpTag,"receive : " + recvCmdStr);

                    break;
                case ConstParam.MSG_TYPE_SEND:
                    udpSendStrBuf.append(msg.obj.toString());
                    strTemp = "发送：" + msg.obj.toString();
                    txt_Info.setText(strTemp);
                    //txt_Info.setText(udpSendStrBuf.toString());
                    break;
                case ConstParam.MSG_TYPE_SHOW_RCV_ROBOT_DATA:
                    // fMotorAMileage，fMotorBMileage,yaw ，Speed，Speed_MotorB，
                    // pitch,roll,temperature，Cylinder_Number_MotorA，Cylinder_Number_MotorB
                    String strRobot = String.format("robot data : %.3f \t %.3f \t | %.2f \t %.2f \t %.2f\n | " +
                                    "%.2f \t " + "%.2f \t | %.2f \t " + "%.2f \t | " + "%.2f\r\n" ,
                            fRobotData[0],fRobotData[1], fRobotData[2], fRobotData[5],fRobotData[6],
                            fRobotData[3], fRobotData[4],  fRobotData[8], fRobotData[9], fRobotData[7] );
                    robotView.setText(strRobot);
                    System.out.println("显示robot data : " + strRobot);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            //移除监听器
            locationManager.removeUpdates(locationListener);
        }
        if (timerSendGps != null) {

            timerSendGps.cancel( );

            timerSendGps = null;

        }

    }

        // 获取本机IP
    private String getLocalIp() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        Log.i(UdpTag, "int ip "+ipAddress);

        if (ipAddress == 0) return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }


    // 设置广播接收器
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("udpRcvMsg"))  {
                Message message = new Message();
                message.obj = intent.getStringExtra("udpRcvMsg");
                message.what = ConstParam.MSG_TYPE_SHOW_RCV;
                Log.i("主界面Broadcast","收到"+message.obj.toString());
                myHandler.sendMessage(message);
            }
            if (intent.hasExtra("udpRobotReceiveData"))  {
                fRobotData =  intent.getFloatArrayExtra("udpRobotReceiveData");
                Message message = new Message();
                //message.obj = fRobotData;
                message.what = ConstParam.MSG_TYPE_SHOW_RCV_ROBOT_DATA;
                Log.i("主界面Broadcast","收到 robot data");
                myHandler.sendMessage(message);
            }

        }
    };

    public void DisplayToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }



    // 按键消息处理函数
    class MyBtnClick implements Button.OnClickListener{

        @Override
        public void onClick(View v) {
            Thread thread = null;
            switch (v.getId()) {
//            case R.id.btn_CleanRecv:
//                udpRcvStrBuf.delete(0,udpRcvStrBuf.length());
//                Message message = new Message();
//                message.what = 3;
//                myHandler.sendMessage(message);
//                break;
                case R.id.btn_udpConn:
                    System.out.println(" btn_udpConn click once");
                    //建立线程池
                    ExecutorService exec = Executors.newCachedThreadPool();

                    String desIp = editTextIP.getText().toString();
                    int desPort = Integer.valueOf(editTextPort.getText().toString());
                    System.out.println( " des ip and port " + desIp + " : " + desPort);

                    client = new UDPClient(desIp,desPort);
                    exec.execute(client);
                    btn_UdpClose.setEnabled(true);
                    btn_UdpConn.setEnabled(false);
                    btn_Send.setEnabled(true);
                    editTextIP.setEnabled(false);
                    editTextPort.setEnabled(false);
                    SetControlButtonEnableState(true);//设置控制按钮状态

                    // 开启定时发送GPS 任务
                    if(taskSendGps !=null){
                        taskSendGps.cancel();
                    }
                    timerSendGps.purge();

                        taskSendGps = new TimerTask( ) {

                            public void run ( ) {
                                HexUtils.putFloat(sendCmdBuf,gpsLongitude,4);
                                HexUtils.putFloat(sendCmdBuf,gpsLatitude,8);
                                HexUtils.putFloat(sendCmdBuf,gpsSpeed,12);

                                String str = String.format("gps src %f %f %f \r\n",gpsLongitude,gpsLatitude,gpsSpeed);
                                Log.i(UdpTag,str);

                                float lngj =  HexUtils.getFloat(sendCmdBuf,4);
                                float latw =  HexUtils.getFloat(sendCmdBuf,8);
                                float speed =  HexUtils.getFloat(sendCmdBuf,12);
                                String strShow = String.format("gps des %f %f %f ",lngj,latw,speed);
                                Log.i(UdpTag,strShow);
                                Thread thread = sendCmd(ConstParam.SEND_DATA_GPS,sendCmdBuf,12);
                                thread.start();
                                System.out.println("发送一包GPS 数据\r\n");
                            }
                        };
                    // 开启定时发送任务
                    timerSendGps.schedule(taskSendGps,10000,1000);
                    break;
                case R.id.btn_udpClose:
                    System.out.println(" btn_udpClose click once");
                    client.setUdpLife(false);
                    btn_UdpConn.setEnabled(true);
                    btn_UdpClose.setEnabled(false);
                    btn_Send.setEnabled(false);
                    editTextIP.setEnabled(true);
                    editTextPort.setEnabled(true);

                    SetControlButtonEnableState(false);//设置控制按钮状态
                    // 关闭定时发送任务
                    if(taskSendGps !=null){
                        taskSendGps.cancel();
                    }
                    timerSendGps.purge();
                    break;
                case R.id.btn_Send:
                    System.out.println(" btn_Send click once");
                    thread= new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = ConstParam.MSG_TYPE_SEND;
                            if (edit_Send.getText().toString()!=""){
                                client.send(edit_Send.getText().toString());
                                message.obj = edit_Send.getText().toString();
                                myHandler.sendMessage(message);
                            }

                        }
                    });
                    thread.start();
                    break;
                case R.id.btnForward:
                    System.out.println(" btnForward click once");
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_TURN_FORWARD,sendCmdBuf,1);
//                    thread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Message message = new Message();
//                            message.what = ConstParam.MSG_TYPE_SEND;
//                            int cmdLength = 7;
//                            byte[] cmd = new byte[cmdLength];
//                            cmd[0] = (byte) 0xa5;  // 报头
//                            cmd[1] = (byte) 0xa5;
//                            cmd[2] = (byte) 0x01;  // 报文ID
//                            cmd[3] = (byte) 0x01;  // 报文内容长度 n （总的报文长度 n + 6)
//                            cmd[4] = (byte) 0x01;  // 报文内容
//                            cmd[5] = (byte) 0xc8;  // 报尾
//                            cmd[6] = (byte) 0xc8;
//
//                            client.send(cmd,cmdLength);
//                            message.obj = new String(cmd);
//                            myHandler.sendMessage(message);
//
//                        }
//                    });
                    thread.start();
                    break;
                case R.id.btnTurnLeft:{
                    System.out.println(" btnTurnLeft click once");
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_TURN_LEFT,sendCmdBuf,1);
                    thread.start();
                    break;
                }
                case R.id.btnTurnRight:{
                    System.out.println(" btnTurnRight click once");
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_TURN_RIGHT,sendCmdBuf,1);
                    thread.start();
                    break;
                }
                case R.id.btnStop:{
                    System.out.println(" btnStop click once");
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_TURN_STOP,sendCmdBuf,1);
                    thread.start();
                    break;
                }
                case R.id.btnBackward:{
                    System.out.println(" btnBackward click once");
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_TURN_BACKWARD,sendCmdBuf,1);
                    thread.start();
                    break;
                }
                // 2017-12-11  09:00 添加旋转功能按钮
                case R.id.btnTurnClockwise:{
                    Log.i(UdpTag,"btnTurnClockwise click once");
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_TURN_CLOCKWISE,sendCmdBuf,1);
                    thread.start();
                    break;
                }
                case R.id.btnTurnAntiClockwise:{
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_TURN_ANTICLOCKWISE,sendCmdBuf,1);
                    thread.start();
                    break;
                }
                case R.id.btnCalibrationDirection:{
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_CALIBRATION_DIRECTION,sendCmdBuf,1);
                    thread.start();
                    break;
                }
                case R.id.btnOtherCmd:{

                    getGPSLocation(); // 获取GPS坐标
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_CMD_OTHER_CMD,sendCmdBuf,1);
                    thread.start();
                    break;
                }
                case R.id.btn_SendGps:{

                    HexUtils.putFloat(sendCmdBuf,gpsLongitude,0);
                    HexUtils.putFloat(sendCmdBuf,gpsLatitude,4);
                    HexUtils.putFloat(sendCmdBuf,gpsSpeed,8);

                    String str = String.format("gps src %f %f %f \r\n",gpsLongitude,gpsLatitude,gpsSpeed);
                    Log.i(UdpTag,str);

                   float lngj =  HexUtils.getFloat(sendCmdBuf,0);
                   float latw =  HexUtils.getFloat(sendCmdBuf,4);
                   float speed =  HexUtils.getFloat(sendCmdBuf,8);
                    String strShow = String.format("gps des %f %f %f ",lngj,latw,speed);
                    Log.i(UdpTag,strShow);
                    thread = sendCmd(ConstParam.SEND_DATA_GPS,sendCmdBuf,12);
                    thread.start();
                    System.out.println("发送一包GPS 数据\r\n");
                    break;
                }
                case R.id.btn_SendSetCourse :{
                    float fSetCourse = Float.parseFloat( editSetCourse.getText().toString().trim());
                    float fSetCourseP = Float.parseFloat( editSetCourseP.getText().toString().trim());
                    float fSetCourseI = Float.parseFloat( editSetCourseI.getText().toString().trim());
                    float fSetCourseD = Float.parseFloat( editSetCourseD.getText().toString().trim());

                    HexUtils.putFloat(sendCmdBuf,fSetCourse,0);
                    HexUtils.putFloat(sendCmdBuf,fSetCourseP,4);
                    HexUtils.putFloat(sendCmdBuf,fSetCourseI,8);
                    HexUtils.putFloat(sendCmdBuf,fSetCourseD,12);
                    String str = String.format("set course  %f %f %f %f\r\n",fSetCourse,fSetCourseP,fSetCourseI,fSetCourseD);
                    Log.i(UdpTag,str);
                    System.out.println("设置航向\r\n");
                    thread = sendCmd(ConstParam.SEND_DATA_SET_COURSE,sendCmdBuf,16);
                    thread.start();

                    break;
                }

                case R.id.btn_SendStopCourse:{
                    sendCmdBuf[0] = 1;
                    thread = sendCmd(ConstParam.SEND_DATA_STOP_COURSE,sendCmdBuf,1);
                    thread.start();
                    break;
                }

                case R.id.btn_SendSetMotorSpeed:{
                    int SetSpeed = Integer.parseInt(editSetRobotSpeed.getText().toString().trim());

                    sendCmdBuf[0] = (byte) (SetSpeed/256);
                    sendCmdBuf[1] = (byte) (SetSpeed%256);
                    System.out.println("设置航速\r\n");
                    thread = sendCmd(ConstParam.SEND_DATA_SET_ROBOT_SPEED,sendCmdBuf,2);
                    thread.start();
                    break;
                }

                default:
                    break;
            }
        }
    }


     // 发送控制命令函数
    public Thread sendCmd(final byte cmdId, final byte[] cmdContent,final int cmdContentLength){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = ConstParam.MSG_TYPE_SEND;
                byte[] cmd = new byte[cmdContentLength +6];
                cmd[0] = (byte) 0xa5;  // 报头
                cmd[1] = (byte) 0xa5;
                cmd[2] =  cmdId;  // 报文ID
                cmd[3] = (byte) cmdContentLength;  // 报文内容长度 n （总的报文长度 n + 6)
                for (int i = 0; i < cmd[3]; i++) {
                    cmd[i+4] =cmdContent[i];
                }
                //cmd[4+ cmdContentLength] = (byte) 0x01;  // 报文内容
                cmd[4+ cmdContentLength] = (byte) 0xc8;  // 报尾
                cmd[5+ cmdContentLength] = (byte) 0xc8;

                client.send(cmd,cmd[3] + 6);
                if( cmdId != ConstParam.SEND_DATA_GPS){ //不显示发送的GPS命令
                    message.obj = new String(" 发送控制命令：" + cmdId);
                    myHandler.sendMessage(message);
                }


            }
        });
        return thread;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:{

                return true;
            }
            case R.id.action_video_settings:{
                showVideoSetting();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void showVideoSetting(){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this,VideoShow.class);
            startActivity(intent);

    }

    // 控制按钮设置响应开关
    public void SetControlButtonEnableState(boolean enableFlag){
        boolean setFlag = enableFlag;

        btnForward.setEnabled(setFlag);
        btnTurnLeft.setEnabled(setFlag);
        btnTurnRight.setEnabled(setFlag);
        btnStop.setEnabled(setFlag);
        btnBackward.setEnabled(setFlag);

        btnTurnClockwise.setEnabled(setFlag);
        btnTurnAntiClockwise.setEnabled(setFlag);
        btnCalibrationDirection.setEnabled(setFlag);
        btnOtherCmd.setEnabled(setFlag);  //旋转按钮
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                flag = true;
            }
        } else {
            flag = true;
        }
    }

    /**
     * 权限的结果回调函数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            flag = grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermission();//针对6.0以上版本做权限适配
    }

    /**
     * 通过GPS获取定位信息
     */
    public void getGPSLocation() {
        Location gps = LocationUtils.getGPSLocation(this);
        if (gps == null) {
            //设置定位监听，因为GPS定位，第一次进来可能获取不到，通过设置监听，可以在有效的时间范围内获取定位信息
            LocationUtils.addLocationListener(context, LocationManager.GPS_PROVIDER, new LocationUtils.ILocationListener() {
                @Override
                public void onSuccessLocation(Location location) {
                    if (location != null) {
                        showLocation(location); // 显示GPS
                        Toast.makeText(MainActivity.this, "gps onSuccessLocation location:  lat==" + location.getLatitude() + "     lng==" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "gps location is null", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "gps location: lat==" + gps.getLatitude() + "  lng==" + gps.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 通过网络等获取定位信息
     */
    private void getNetworkLocation() {
        Location net = LocationUtils.getNetWorkLocation(this);
        if (net == null) {
            Toast.makeText(this, "net location is null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "network location: lat==" + net.getLatitude() + "  lng==" + net.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 采用最好的方式获取定位信息
     */
    private void getBestLocation() {
        Criteria c = new Criteria();//Criteria类是设置定位的标准信息（系统会根据你的要求，匹配最适合你的定位供应商），一个定位的辅助信息的类
        c.setPowerRequirement(Criteria.POWER_LOW);//设置低耗电
        c.setAltitudeRequired(true);//设置需要海拔
        c.setBearingAccuracy(Criteria.ACCURACY_COARSE);//设置COARSE精度标准
        c.setAccuracy(Criteria.ACCURACY_LOW);//设置低精度
        //... Criteria 还有其他属性，就不一一介绍了
        Location best = LocationUtils.getBestLocation(this, c);
        if (best == null) {
            Toast.makeText(this, " best location is null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "best location: lat==" + best.getLatitude() + " lng==" + best.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    }

}
