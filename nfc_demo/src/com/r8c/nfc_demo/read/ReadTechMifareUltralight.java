package com.r8c.nfc_demo.read;

import java.io.IOException;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;

import com.r8c.nfc_demo.Tool;
import com.r8c.nfc_demo.read_interface.ReadNFC;

public class ReadTechMifareUltralight implements ReadNFC{

	@Override
	public String ReadNfcTag(Intent intent) {
		Tool tool=new Tool();
		Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		String content="";
		MifareUltralight mifareUltralight=MifareUltralight.get(tag);
		
		try {
			mifareUltralight.connect();
			int size=mifareUltralight.PAGE_SIZE;
			byte[] payload = mifareUltralight.readPages(0);
			content="page1："+tool.ByteArrayToHexString(payload)+"\n"+"总容量："+String.valueOf(size)+"\n";
			
			//这里只读取了其中几个page、
			byte[] payload1 = mifareUltralight.readPages(4);
			byte[] payload2 = mifareUltralight.readPages(8);
			byte[] payload3 = mifareUltralight.readPages(12);
			content+="page4:"+tool.ByteArrayToHexString(payload1)+"\npage8:"+tool.ByteArrayToHexString(payload2)+
					"\npage12："+tool.ByteArrayToHexString(payload3)+"\n";
			
		} catch (Exception e) {
			// TODO: handle exception
//			content="读取失败";
		}finally{
			if (mifareUltralight!=null) {
				try {
					mifareUltralight.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					content=" 读取失败";
					e.printStackTrace();
				}
			}
		}
		return content;
		
	}

}
