
package com.eostek.smartbox.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.eostek.smartbox.MyApplication;
import com.eostek.smartbox.data.Constants;
import com.eostek.smartbox.eloud.UserData;

public class JsonHelp {

    private static final String TAG = "JsonHelp";

    /**
     * 带超时的网络请求
     *
     * @param serviceUrl
     * @return
     * @throws SocketTimeoutException
     */
    public static long getDeviceTime(String serviceUrl) throws SocketTimeoutException {
        try {
            URL url = new URL(serviceUrl );
            Log.d(TAG, "url: " + serviceUrl + " write: " );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.connect();

            long dateL=conn.getDate();
            Date date=new Date(dateL);
            long msec = date.getTime();

            Log.d(TAG, "Date : " + date.toString());
            return msec;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            Log.i(TAG, "SocketTimeoutException");
            throw e;
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.i(TAG, "IOException:" + e1);
        }
        return -1;
    }


    /**
     * 带超时的网络请求
     *
     * @param serviceUrl
     * @param write
     * @return
     * @throws SocketTimeoutException
     */
    public static JSONObject getJsonObjectWithTimeOut(String serviceUrl, String write) throws SocketTimeoutException {
        try {
            URL url = new URL(serviceUrl + write);
            Log.d(TAG, "url: " + serviceUrl + " write: " + write);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(HttpUtil.connectTimeout);
            conn.setReadTimeout(HttpUtil.readTimeout);
            conn.setDoOutput(true);
            conn.connect();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "responseCode : " + responseCode);
            InputStream u = null;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                u = conn.getInputStream();
            } else {
                u = conn.getErrorStream();
            }

            //InputStream u = conn.getInputStream();// 获取servlet返回值，接收
            BufferedReader in = new BufferedReader(new InputStreamReader(u,
                    "utf-8"));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            Log.d(TAG, "result: " + buffer.toString());
            conn.disconnect();
            try {
                return new JSONObject(new JSONTokener(buffer.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "JSONException: " + e.getMessage());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            Log.i(TAG, "SocketTimeoutException");
            throw e;
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.i(TAG, "IOException:" + e1);
        }
        return null;
    }

    public JSONObject getJsonObject(String serviceUrl, String write) {
        try {
            URL url = new URL(serviceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
            conn.setRequestMethod("POST");// 提交模式
            conn.setDoOutput(true);

            Writer writer = new OutputStreamWriter(conn.getOutputStream(),
                    "utf-8");

            writer.write(write);
            writer.flush();
            conn.connect();
            Log.d(TAG, "conn: " + conn.getResponseCode());
            InputStream u = conn.getInputStream();// 获取servlet返回值，接收
            BufferedReader in = new BufferedReader(new InputStreamReader(u,
                    "utf-8"));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }

            conn.disconnect();
            try {
                return new JSONObject(new JSONTokener(buffer.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

    public static JSONObject getJsonObjectByGet(String url, String tag) {
        Log.d("DTGG", "getJsonObjectByGet ==> url : " + url);
        InputStream in = null;
        HttpURLConnection http = null;
        StringBuffer bufferRes = null;
        try {
            URL urlGet = new URL(url);
            http = (HttpURLConnection) urlGet.openConnection();
            // 连接超时
            // 读取超时 --服务器响应比较慢，增大时间
            http.setConnectTimeout(HttpUtil.connectTimeout);
            http.setReadTimeout(HttpUtil.readTimeout);
            http.setRequestMethod("GET");
            http.setRequestProperty("Ttag", tag + "_0.0.3490");
            http.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
            http.connect();
            in = http.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String valueString = null;
            bufferRes = new StringBuffer();
            while ((valueString = read.readLine()) != null) {
                bufferRes.append(valueString);
            }
            Log.d(TAG, "getJsonObjectByGet ==> bufferRes : " + bufferRes);
            try {
                return new JSONObject(bufferRes.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (http != null) {
                // 关闭连接
                http.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 完整的判断中文汉字和符号
     *
     * @param strName
     * @return
     */
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据Unicode编码完美的判断中文汉字和符号
     *
     * @param c
     * @return
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    String json = "[{\"id\": 1,\"name\": \"11\",\"depart\": \"ggg\",\"pos\": \"sdwq\",\"e-mail\": \"11@mail.1\",\"mobil\": \"1890000117\",\"photo\": \"inc/user/1/1.jpg\" },"
            + "{\"id\": 2,\"name\": \"44\",\"depart\": \"qwdq\", \"pos\": \"qwqf\",\"e-mail\": \"22@mail.2\",\"mobil\": \"1730000111\",\"photo\": \"inc/user/2/2.jpg\"},"
            + "{\"id\": 3,\"name\": \"55\",\"depart\": \"qwfqf\",\"pos\": \"UXfqwfwq\",\"e-mail\": \"33@mail.com\",\"mobil\": \"17374647784\",\"photo\": \"inc/user/3/3.jpg\"}]";

    public static UserData jsonObjectToUserListData(JSONObject json) {
        UserData userData = new UserData();
        if (json != null) {
            userData.setId(json.optInt("id"));
            userData.setName(json.optString("name"));
            userData.setDepart(json.optString("depart"));
            userData.setPosition(json.optString("pos"));
            userData.setEmail(json.optString("e-mail"));
            userData.setMobil(json.optString("mobil"));
            userData.setPhoto(json.optString("photo"));
        }

        return userData;
    }



    /*
     *  测试 id userId ip地址
     */

    public static void writeTestData(String path) {
        JSONArray jsonArray = new JSONArray();//创建JSONArray对象
        File file = new File(path);
        if (!file.exists())//判断文件是否存在，若不存在则新建
        {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);//实例化FileOutputStream
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");//将字符流转换为字节流

            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);//创建字符缓冲输出流对象

            JSONObject jsonObject = new JSONObject();//创建JSONObject对象
            jsonObject.put("id", Constants.SmarkBoxID);//产生1-100的随机数
            jsonObject.put("userId", Constants.SmarkBoxUserID);//产生18-25的随机数
            jsonObject.put("Ip", Constants.SmarkBoxIP);//产生60-100的随机数
            jsonObject.put("serverIp", Constants.SmarkBoxServerIP);//产生60-100的随机数
            jsonObject.put("serverPort", Constants.SmarkBoxServerProt);//产生60-100的随机数
            jsonArray.put(jsonObject);//将jsonObject对象旁如jsonarray数组中

            String jsonString = jsonArray.toString();//将jsonarray数组转化为字符串
            String JsonString = stringToJSON(jsonString);//将jsonarrray字符串格式化

            bufferedWriter.write(JsonString);//将格式化的jsonarray字符串写入文件
            bufferedWriter.flush();//清空缓冲区，强制输出数据
            bufferedWriter.close();//关闭输出流
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean readTestData( String path) {
        JSONArray jsonArray = new JSONArray();//创建JSONArray对象
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);//创建FileInputStream对象
            InputStreamReader inputStreamReader = inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");//创建InputStreamReader对象

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//创建字符输入流
            String Js = "";
            String Line = null;
            while ((Line = bufferedReader.readLine()) != null) {
                Js += Line;
            }
            JSONArray WE = new JSONArray(Js);//用读取到的字符串实例化JSONArray数组
//            for (int j = 0; j < WE.length(); j++) {
                JSONObject we = WE.getJSONObject(0);//将JSONArray中的json数据赋值给jsonobject对象
                int id =  we.getInt("id");
                if(id >= 0){
                    MyApplication.getInstance().setSmarkBoxFileID(id);
                }

                int UserId =  we.getInt("userId");
                if(UserId >= 0){
                    MyApplication.getInstance().setSmarkBoxUserFileID(UserId);
                }

                String IP =  we.getString("Ip");
                if(!IP.equals("127.0.0.1")){
                    MyApplication.getInstance().setSmarkBoxIP(IP);
                }

                String serverIP =  we.getString("serverIp");
                if(!serverIP.equals("127.0.0.1")){
                    MyApplication.getInstance().setSmarkBoxServerIP(serverIP);
                }

                String serverPort =  we.getString("serverPort");
                if(!serverPort.equals("0000")){
                    MyApplication.getInstance().setSmarkBoxServerProt(Integer.parseInt(serverPort));
                }

                Utils.print("smartbox","test :"+id+"  "+UserId+"  "+ IP +"  "+serverIP+"   "+serverPort);
//            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String stringToJSON(String strJson) {
        int tabNum = 0;
        StringBuffer jsonFormat = new StringBuffer();
        int length = strJson.length();
        for (int i = 0; i < length; i++) {
            char c = strJson.charAt(i);
            if (c == '{') {
                tabNum++;
                jsonFormat.append(c + "\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
            } else if (c == '}') {
                tabNum--;
                jsonFormat.append("\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
                jsonFormat.append(c);
            } else if (c == ',') {
                jsonFormat.append(c + "\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
            } else {
                jsonFormat.append(c);
            }
        }
        return jsonFormat.toString();
    }

    public static String getSpaceOrTab(int tabNum) {
        StringBuffer sbTab = new StringBuffer();
        for (int i = 0; i < tabNum; i++) {
            if (true) {
                sbTab.append('\t');
            } else {
                sbTab.append("    ");
            }
        }
        return sbTab.toString();
    }
}
