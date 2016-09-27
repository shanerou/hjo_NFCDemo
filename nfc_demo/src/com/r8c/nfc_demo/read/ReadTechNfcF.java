package com.r8c.nfc_demo.read;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;

import com.r8c.nfc_demo.Tool;
import com.r8c.nfc_demo.read_interface.ReadNFC;

public class ReadTechNfcF implements ReadNFC{

	@Override
	public String ReadNfcTag(Intent intent) {
		Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Tool tool=new Tool();
		  NfcF nfc = NfcF.get(tag);
		   String contetn="";
          try {
              nfc.connect();
              byte[] felicaIDm = new byte[]{0};
              byte[] req = readWithoutEncryption(felicaIDm, 10);
              byte[] res = nfc.transceive(req);
              nfc.close();
              contetn= tool.ByteArrayToHexString(res);
          } catch (Exception e) {
          }
		return contetn;
	}
	 private byte[] readWithoutEncryption(byte[] idm, int size)
	            throws IOException {
	        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);
	        bout.write(0);           
	        bout.write(0x06);       
	        bout.write(idm);        
	        bout.write(1);          
	        bout.write(0x0f);       
	        bout.write(0x09);        
	        bout.write(size);        
	        for (int i = 0; i < size; i++) {
	            bout.write(0x80);    
	            bout.write(i);      
	        }

	        byte[] msg = bout.toByteArray();
	        msg[0] = (byte) msg.length; 
	        return msg;
	    }
}
