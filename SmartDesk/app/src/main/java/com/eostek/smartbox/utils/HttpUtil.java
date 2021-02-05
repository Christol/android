
package com.eostek.smartbox.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HttpUtil {
    public final static String POST = "POST";

    public final static String GET = "GET";

	private static final String TAG = "HttpUtil";

    private String url;

    private Map<String, List<String>> parameterMap;// 参数列表

    private Map<String, String> heardMap;// 参数列表

    private String requestMethod = GET;// 请求方式

    private String encode = "UTF-8";

    private String contentType = "text/html; charset=UTF-8";

    public static int connectTimeout = 30000;

    public static int readTimeout = 30000;

    private HttpURLConnection conn;

    /**
     * 设置url参数
     * 
     * @param url
     * @param parameter
     */
    public HttpUtil(String url, Map<String, List<String>> parameter) {
        parameterMap = parameter;
        this.url = url;
    }

    /**
     * 设置url 参数
     * 
     * @param url
     * @param parameter
     */
    public HttpUtil(String url, String parameter) {
        addParameter("", parameter);
        this.url = url;
    }

    /**
     * 设置url
     * 
     * @param url
     */
    public HttpUtil(String url) {
        this.url = url;
    }

    /**
     * 获取网页内容
     * 
     * @return
     * @throws IOException
     * @throws IOException
     * @throws IOException
     */
    public String getUrlContent() throws IOException, UnknownHostException {
        BufferedReader breader = null;
        try {
            InputStream in = getUrlInputStream();
            if ("gzip".equals(conn.getContentEncoding())) {
                in = new GZIPInputStream(in);
            }
            if (in == null) {
                return "";
            }
            breader = new BufferedReader(new InputStreamReader(in, encode));
            StringBuilder content = new StringBuilder();
            String str;
            while ((str = breader.readLine()) != null) {
                content.append(str).append("\n");
            }
            if (content.length() > 0)
                content.deleteCharAt(content.length() - 1);
            return content.toString();
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (breader != null)
                try {
                    breader.close();
                } catch (IOException ie) {
                }
        }
    }

    /**
     * 下载文件到本地
     * 
     * @param filename 本地文件名
     * @throws Exception 各种异常
     */
    public void download(String filename) throws IOException {
        getHttpURLConnection();// 初始conn
        OutputStream os = null;
        InputStream is = null;
        try {
            is = getUrlInputStream();
            if (is == null)
                throw new IOException("UrlInputStream is null");
            File file = new File(filename);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            byte[] bs = new byte[1024];// 1K的数据缓区
            os = new FileOutputStream(filename);
            int len;
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException ie) {
                }
            if (is != null)
                try {
                    is.close();
                } catch (IOException ie) {
                }
        }
    }

    public void addHeardMap(String name, String value) {
        if (heardMap == null)
            heardMap = new HashMap<String, String>();
        heardMap.put(name, value);
    }

    /**
     * 添加参数
     * 
     * @param name
     * @param value
     */
    public void addParameter(String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        addParameter(name, values);
    }

    /**
     * 添加参数
     * 
     * @param name
     * @param values
     */
    public void addParameter(String name, List<String> values) {
        if (parameterMap == null)
            parameterMap = new HashMap<String, List<String>>();

        parameterMap.put(name, values);
    }

    /**
     * 添加参数
     * 
     * @param parameterMap
     */
    public void addParameterMap(Map<String, List<String>> parameterMap) {
        if (parameterMap == null)
            this.parameterMap = parameterMap;
        else
            this.parameterMap.putAll(parameterMap);
    }

    /**
     * 获取参数
     * 
     * @return
     */
    public Map<String, List<String>> getParameterMap() {
        return parameterMap;
    }

    /**
     * 获取请求数据
     * 
     * @return
     */
    public Map<String, List<String>> getRequestProperty() {
        return conn.getRequestProperties();
    }

    /**
     * 获取url的流
     * 
     * @return
     * @throws IOException
     */
    private InputStream getUrlInputStream() throws IOException, UnknownHostException {
        getHttpURLConnection();// 初始conn

        if (heardMap != null && heardMap.size() > 0) {
            Iterator<String> keys = heardMap.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();

                conn.setRequestProperty(key, heardMap.get(key));
            }
            heardMap.clear();
        }
        // 设置超时
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setAllowUserInteraction(false);

        if (POST.equals(requestMethod) && parameterMap != null
                && parameterMap.size() > 0) {
            conn.setRequestMethod(requestMethod);
            conn.setDoOutput(true);
            byte[] post = toString(parameterMap).getBytes();
            conn.setRequestProperty("Content-Type", contentType);
            conn.setRequestProperty("Content-Length", String.valueOf(post.length));
            conn.addRequestProperty("Ttag", "BQ00ADRD0000MB01");
            conn.addRequestProperty("Tcip", System.currentTimeMillis() + "");
            conn.getOutputStream().write(post);
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
        }
        conn.connect();

        try {
            return conn.getInputStream();
        } catch (IOException e) {
            return conn.getErrorStream();
        }
    }

    /**
     * 获取HttpURLConnection
     * 
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private HttpURLConnection getHttpURLConnection()
            throws MalformedURLException, IOException {
        if (conn == null) {
            String tempUrl = url;
            if (GET.equals(requestMethod) && parameterMap != null
                    && parameterMap.size() > 0) {
                tempUrl += toString(parameterMap);
            }
            conn = (HttpURLConnection) (new URL(tempUrl)).openConnection();
        }
        return conn;
    }

    /**
     * list 格式化成字符串
     * 
     * @param list
     * @return
     */
    private String toString(List<String> list) {
        if (list != null && list.size() > 0) {
            StringBuilder str = new StringBuilder();
            for (String s : list) {
                str.append(s).append("; ");
            }
            str.delete(str.length() - 2, str.length());
            return str.toString();
        }
        return "";
    }

    /**
     * map 格式化成字符串
     * 
     * @param map
     * @return
     */
    private String toString(Map<String, List<String>> map) {
        if (map != null) {
            StringBuilder str = new StringBuilder();

            Iterator<String> keys = map.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    if (key != null && !key.equals("")) {
                        str.append(key)
                                .append("=")
                                .append(URLEncoder.encode(
                                        toString(map.get(key)), encode))
                                .append("&");
                    } else {
                        str.append(toString(map.get(key))).append("&");
                    }
                } catch (UnsupportedEncodingException e) {
                    str.append(key).append("=").append(toString(map.get(key)))
                            .append("&");
                }
            }
            str.deleteCharAt(str.length() - 1);
            return str.toString();
        }
        return "";
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getEncode() {
        return encode;
    }

    public void setEncode(String encode) {
        this.encode = encode;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public static String encoder(String str) {
        if (!isNull(str)) {
            try {
                str = URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        return str;
    }

    public static boolean isNull(String str) {
        return str == null || "".equals(str.trim());
    }
    
    /**
     * 判断当前是否有网络连接,但是如果该连接的网络无法上网，也会返回true
     * @param mContext
     * @return
     */
	public static boolean isNetConnection(Context mContext) {
		if (mContext != null) {
			ConnectivityManager connectivityManager = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			if (networkInfo != null) {
				boolean connected = networkInfo.isConnected();
				if (networkInfo != null && connected) {
					if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 在子线程里开启该方法，可检测当前网络是否能打开网页
	 * true是可以上网，false是不能上网
	 * 
	 */
	public static boolean isOnline() {
		URL url;
		try {
			url = new URL("https://www.baidu.com");
			InputStream stream = url.openStream();
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
    public static boolean ping() {
    	String ip = "114.114.114.114";// ping 的地址，可以换成任何一种可靠的外网
    	Process process = null;
    	try {
    		process = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);
    		//获取进程的标准输入流
            final InputStream is1 = process.getInputStream();
            //获取进城的错误流
            final InputStream is2 = process.getErrorStream();
            //启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流
            new Thread() {
                public void run() {
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));
                    try {
                        String line1 = null;
                        while ((line1 = br1.readLine()) != null) {
                            if (line1 != null){}
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally{
                        try {
                            is1.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

            new Thread() {
                public void  run() {
                    BufferedReader br2 = new  BufferedReader(new  InputStreamReader(is2));
                    try {
                        String line2 = null ;
                        while ((line2 = br2.readLine()) !=  null ) {
                            if (line2 != null){}
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally{
                        try {
                            is2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

            //可能导致进程阻塞，甚至死锁
            int ret = process.waitFor();
            if(ret == 0) {
                if (null != process) {
                	process.destroy();
                }
            	return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            try{
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
            }catch(Exception ee){
            
            }
        }
        if (null != process) {
        	process.destroy();
        }
    	return false;
    }
    
}
