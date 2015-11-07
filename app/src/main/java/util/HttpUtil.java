package util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by zhoumao on 15/8/24.
 */
public class HttpUtil {
    private static final  int MSG_Urlshow = 1;
    private static final  int MSG_Urldown = 2;
    private static final  int MSG_Urlup   = 3;
    private  Handler handler ;

    public HttpUtil(Handler mhandler){
        handler = mhandler;
    }

    public void urlshow(final String urlstr){             //显示网上的文件；
        Thread thread=new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(urlstr);
                    HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                    if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Log.i("TAG", "---into-----urlConnection---success--");
                        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream(), "utf-8");
                        int i;
                        String content = "";
                        while ((i = isr.read()) != -1) {
                            content = content + (char) i;
                        }
                        handler.obtainMessage(MSG_Urlshow, content).sendToTarget();
                        isr.close();
                        httpConn.disconnect();
                    } else {
                        Log.d("TAG", "---into-----urlConnection---fail--");

                    }
                }catch (IOException e){
                    Log.i("zm","hello");
                }
            }
        });
        thread.start();
    }

    /**
     * 该函数返回整形 -1：代表下载文件出错 0：代表下载文件成功 1：代表文件已经存在
     */
    public  int urldown(final String urlStr, final String path,final String fileName){

        Thread thread=new Thread(new Runnable() {
            String ret  = "down_well";;
            public void run() {
                try {
                     FileUtil fileUtil = new FileUtil();

                    if (fileUtil.isFileExist(path + fileName)) {
                        ret = "file_exist" ;
                        Log.i("zm","file_exist");
                    } else {
                        URL url = new URL(urlStr);
                        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                        if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream inputStream = httpConn.getInputStream();
                            File resultFile = fileUtil.write2SDFromInput(path, fileName, inputStream);
                            try {
                                inputStream.close();
                                httpConn.disconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (resultFile == null) {
                                ret = "down_wrong";
                                Log.i("zm","resultfile");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ret = "down_wrong";
                    Log.i("zm","Exception");
                } finally {
                }
                Message message = Message.obtain();
                message.what =  MSG_Urldown;
                message.obj = ret;
                handler.sendMessage(message);
            }
        });
        thread.start();
        return 0;
    }

    // 上传文件
    // 成功返回1，失败返回0；
    // 还需加入   线程控制；
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10*10000000;
    private static final String CHARSET = "utf-8";
    public static final String SUCCESS="1";
    public static final String FAILURE="0";
    //public static final String requestURL="http://192.168.10.189:8400/uploadfile/FileUploadServlet";
    public  void  urlup(final String path,final String RequestURL)
    {
        Thread thread =new Thread(new Runnable() {
            String ret = "up_wrong";
            String  BOUNDARY =  UUID.randomUUID().toString();
            String PREFIX = "--" , LINE_END = "\r\n";
            String CONTENT_TYPE = "multipart/form-data";
            File file = new File(path);
            public void run() {
                try {
                    URL url = new URL(RequestURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(TIME_OUT);
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", "utf-8");
                    conn.setRequestProperty("connection", "keep-alive");                                //
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);    //
                    if (file != null) {
                        OutputStream outputSteam = conn.getOutputStream();

                        DataOutputStream dos = new DataOutputStream(outputSteam);
                        StringBuffer sb = new StringBuffer();
                        sb.append(PREFIX);
                        sb.append(BOUNDARY);
                        sb.append(LINE_END);

                        sb.append("Content-Disposition: form-data; name=\"img\"; filename=\"" + file.getName() + "\"" + LINE_END);
                        sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                        sb.append(LINE_END);
                        dos.write(sb.toString().getBytes());
                        InputStream is = new FileInputStream(file);
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        while ((len = is.read(bytes)) != -1) {
                            dos.write(bytes, 0, len);
                        }
                        is.close();
                        dos.write(LINE_END.getBytes());
                        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                        dos.write(end_data);
                        dos.flush();

                        int res = conn.getResponseCode();
                        String response = conn.getResponseMessage();
                        Log.e(TAG, "response code:" + res);
                        Log.i(TAG,"response: "+response);
                        if (res == 200) {
                            ret = "up_well";
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Message message = Message.obtain();
                message.what =  MSG_Urlup;
                message.obj = ret;
                handler.sendMessage(message);
            }
        });
        thread.start();
    }
}
