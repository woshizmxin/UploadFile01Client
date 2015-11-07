package com.test.http.http;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import util.FileUtil;
import util.HttpUtil;


public class main extends Activity implements OnClickListener {
    private String urlUp  = "http://192.168.199.132:8080/upload";
    private String urlstr = "https://www.baidu.com/";
    private TextView webdatashow;
    private String tag = "zm";
    private Button urlConnection;
    private Button httpClient;

    private Handler handler = null;
    private static final  int MSG_Urlshow = 1;
    private static final  int MSG_Urldown = 2;
    private static final  int MSG_Urlup   = 3;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webdata);
        urlConnection = (Button) findViewById(R.id.urlConnection);
        httpClient = (Button)findViewById(R.id.httpClient);
        webdatashow = (TextView)findViewById(R.id.webDataShow);
        urlConnection.setOnClickListener(this);
        httpClient.setOnClickListener(this);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_Urlshow :                         //将获取到的数据演示出来
                            webdatashow.setText((String)msg.obj);
                        break;
                    case MSG_Urldown:
                            Log.i("zm",(String)msg.obj);       //下载并存入SD卡中   down_well
                        break;
                    case MSG_Urlup:
                            Log.i("zm",(String)msg.obj);      //上传数据
                        break;
                    default:
                        break;
                }

            }
        };
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.urlConnection:
                new HttpUtil(handler).urldown(urlstr, "mp3/", "hou.lrc");                 //下载网页，并存储到SD卡中
                new HttpUtil(handler).urlshow(urlstr);                                    //下载网页，并展示
                new HttpUtil(handler).urlup("/sdcard/zhoumao",urlUp);                    //上传文件
                webdatashow.setText(new FileUtil().FileReadString("/sdcard/zhoumao"));    //测试SD卡工具
                break;
            case R.id.httpClient:                      //not used
                Log.i("zm","httpClient");
//                if(httpClientThread == null)
//                {
//                    httpClientThread = new Thread(httpClientRunnable);
//                    httpClientThread.start();
//                }
                break;
            default:
                break;
        }
    }
}
