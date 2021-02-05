package com.eostek.smartbox.face.recognition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.eostek.smartbox.data.SmartBoxInfo;
import com.eostek.smartbox.face.proto.Msg;
import com.google.protobuf.ByteString;


public class FacePicture {
	private static final String TAG = "proto";
	
	
	public byte[] getFacePictureReq() {
		byte[] b = File2Bytes("/sdcard/2.jpg");
		if (b != null && b.length > 1) {
			Msg.Message.SetFacePictureReq.Builder bSetFacePictureReq = Msg.Message.SetFacePictureReq.newBuilder();
			bSetFacePictureReq.setName("neddy");
			bSetFacePictureReq.setId(1);
			bSetFacePictureReq.setType("JPG");
			bSetFacePictureReq.setFacePic(ByteString.copyFrom(b));
			Msg.Message.Builder builderForValue = Msg.Message.newBuilder().setSetFacepictureReq(bSetFacePictureReq);
			return getPackage(builderForValue, 1);
		}else{
			return null;
		}
	}
	
	public static byte[]  getPackage(Msg.Message.Builder message , int seq) {
		Msg.Package.Builder  packgebuilder=Msg.Package.newBuilder();
        packgebuilder.setSize(message.build().toByteArray().length);
        packgebuilder.setSeq(seq);
        packgebuilder.setData(message.build().toByteString());
        Msg.Package m = packgebuilder.build();//Msg.Package.newBuilder(packgebuilder.build()).setSize(packgebuilder.build().toByteArray().length).build();
        if(m.toByteArray().length != packgebuilder.getSize())
        {
            packgebuilder.setSize(m.toByteArray().length);
            m = packgebuilder.build();
        }
        return m.toByteArray();
    }
	
	public byte[] getFaceCountReq(){
		Msg.Message.GetFaceCountReq.Builder bGetFaceCountReq = Msg.Message.GetFaceCountReq.newBuilder();
		
		return bGetFaceCountReq.build().toByteArray();
	}
	
	public Bitmap getImageBitmap(String url) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            
            Log.d(TAG, "MalformedURLException :" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IOException :" + e.getMessage());
        }
        return bitmap;
}
	
	public ByteString sss(){
		File file = new File("/sdcard/2.jpg");
		 if (file.isFile()) {
		        FileInputStream fis = null;
		        try {
		            fis = new FileInputStream(file);
		            byte[] buffer = new byte[1024];
		            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		            int len = 0;
		            while ((len = fis.read(buffer)) != -1) {
		                outputStream.write(buffer, 0, len);
		            }
		            return ByteString.copyFrom(outputStream.toByteArray());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    } else {
		        System.out.println("file not found!");
		    }
		 
		 return null;

	}
	public ByteString encodeImage(Bitmap bitmap) {
		Log.d(TAG, "encodeImage :" + bitmap);
		if (bitmap != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// 读取图片到ByteArrayOutputStream
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // 参数如果为100那么就不压缩
			byte[] bytes = baos.toByteArray();

			return ByteString.copyFrom(bytes);
		}
		return null;

	}
	
	 public static byte[] getFileStream(String url){
	     try {
	         URL httpUrl = new URL(url);
	         HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
	         conn.setRequestMethod("GET");
	         conn.setConnectTimeout(5 * 1000);
	         InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
	         byte[] btImg = readInputStream(inStream);//得到图片的二进制数据
	         return btImg;
	     } catch (Exception e) {
	        e.printStackTrace();
	     }
	     return null;
	 }
	 public static byte[] readInputStream(InputStream inStream) throws Exception{
		  ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		  byte[] buffer = new byte[1024];
		  int len = 0;
		  while( (len=inStream.read(buffer)) != -1 ){
		      outStream.write(buffer, 0, len);
		  }
		  inStream.close();
		  return outStream.toByteArray();
		}

	  public static byte[] File2Bytes(String name) {
	        File file = new File(name);
	        if (file.exists()) {
	            Log.e(TAG,"file exists"+name);
	            int byte_size = 1024;
	            byte[] b = new byte[byte_size];
	            try {
	                FileInputStream fileInputStream = new FileInputStream(file);
	                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(byte_size);
	                for (int length; (length = fileInputStream.read(b)) != -1; ) {
	                    outputStream.write(b, 0, length);
	                }
	                fileInputStream.close();
	                outputStream.close();
	                return outputStream.toByteArray();
	                //return Base64.encode(outputStream.toByteArray(), Base64.DEFAULT);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }else
	            Log.e(TAG,"file no exists"+name);

	        return null;
	    }

	private String getPhotoType(String photoUrl){
		if(photoUrl.toLowerCase().endsWith("jpg".toLowerCase())){
			return "JPG";
		}
		return "PPM";
	}

	public byte[] getFacePictureReq2(int Value, int seq) {
		if(SmartBoxInfo.getDeviceUserAllInfo() != null && SmartBoxInfo.getDeviceUserAllInfo().size() > Value){
			int id = SmartBoxInfo.getDeviceUserAllInfo().get(Value).getId();
			String name = SmartBoxInfo.getDeviceUserAllInfo().get(Value).getName();
			String url = SmartBoxInfo.getDeviceUserAllInfo().get(Value).getPhoto();
			String type = getPhotoType(SmartBoxInfo.getDeviceUserAllInfo().get(Value).getPhoto());
			Log.d(TAG,"getFacePictureReq2 : "+id +" "+name+" "+url+" "+type);
			byte[] b = getFileStream(url);
			if (b != null && b.length > 1) {
				Msg.Message.SetFacePictureReq.Builder bSetFacePictureReq = Msg.Message.SetFacePictureReq.newBuilder();
				bSetFacePictureReq.setName(name);
				bSetFacePictureReq.setId(id);
				bSetFacePictureReq.setType(type);
				bSetFacePictureReq.setFacePic(ByteString.copyFrom(b));
				Msg.Message.Builder builderForValue = Msg.Message.newBuilder().setSetFacepictureReq(bSetFacePictureReq);
				return getPackage(builderForValue, seq);
			}
		}
		return null;


//		byte[] b = getFileStream("http://172.23.67.78:8013/ada/inc/user/1/1.jpg");
//		if (b != null && b.length > 1) {
//			Msg.Message.SetFacePictureReq.Builder bSetFacePictureReq = Msg.Message.SetFacePictureReq.newBuilder();
//			bSetFacePictureReq.setName("neddy2");
//			bSetFacePictureReq.setId(2);
//			bSetFacePictureReq.setType("JPG");
//			// bSetFacePictureReq.setFacePic(ByteString.copyFrom(File2Bytes("/sdcard/1.jpg")));
//			bSetFacePictureReq.setFacePic(ByteString.copyFrom(b));
//			Msg.Message.Builder builderForValue = Msg.Message.newBuilder().setSetFacepictureReq(bSetFacePictureReq);
//			return getPackage(builderForValue, 2);
//		} else {
//			return null;
//		}
	}
	
}
