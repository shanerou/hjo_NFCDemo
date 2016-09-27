package com.r8c.nfc_demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import dalvik.system.DexFile;

public class Tool {
	
	// 字符序列转换为16进制字符串
	public String bytesToHexString(byte[] src) {
		return bytesToHexString(src, true);
	}

	private String bytesToHexString(byte[] src, boolean isPrefix) {
		StringBuilder stringBuilder = new StringBuilder();
		if (isPrefix == true) {
			stringBuilder.append("0x");
		}
		if (src == null || src.length <= 0) {
			return null;
		}
		char[] buffer = new char[2];
		for (int i = 0; i < src.length; i++) {
			buffer[0] = Character.toUpperCase(Character.forDigit(
					(src[i] >>> 4) & 0x0F, 16));
			buffer[1] = Character.toUpperCase(Character.forDigit(src[i] & 0x0F,
					16));
			System.out.println(buffer);
			stringBuilder.append(buffer);
		}
		return stringBuilder.toString();
	}
	
	public  String ByteArrayToHexString(byte[] inarray) { // converts byte
		// arrays to string
		int i, j, in;
		String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
				"B", "C", "D", "E", "F" };
		String out = "";

		for (j = 0; j < inarray.length; ++j) {
			in = inarray[j] & 0xff;
			i = (in >> 4) & 0x0f;
			out += hex[i];
			i = in & 0x0f;
			out += hex[i];
		}
		return out;
	}
	
	
	/**
	 * 获取指定包名下的所有类
	 * @param context
	 * @param packageName
	 * @return
	 */
	public   List<String> getClassesFromPackage(Context context,String packageName) 
	{       
		ArrayList<String> classes = new ArrayList<String>();      
		try {  
			DexFile df = new DexFile(context.getPackageCodePath());      
		Enumeration<String> entries = df.entries();        
		while (entries.hasMoreElements()) {           
			String className = (String) entries.nextElement();       
			if (className.contains(packageName)) {                
				classes.add(className);                
				}        
			}        } catch (IOException e) {   
				e.printStackTrace();    
				}     
		return classes; 
		}


}
