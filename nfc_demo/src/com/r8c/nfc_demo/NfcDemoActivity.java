package com.r8c.nfc_demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.r8c.nfc_demo.read.ReadNdefOrNdefFormatable;
import com.r8c.nfc_demo.read.ReadTechMifareClassic;
import com.r8c.nfc_demo.read.ReadTechMifareUltralight;
import com.r8c.nfc_demo.read.ReadTechNfcF;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NfcDemoActivity extends Activity {

	// NFC适配器
	private NfcAdapter nfcAdapter = null;
	// 传达意图
	private PendingIntent pi = null;
	// 文本控件
	private TextView promt = null;
	// 是否支持NFC功能的标签
	private boolean isNFC_support = false;
	private TextView contTextView;
	
//	private Tag tagFromIntent;
	private  String[][] mTechLists;
	private IntentFilter ndef;
	private IntentFilter[] mFilters;
	Tool tool;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc_demo);
		setupViews();
		initNFCData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isNFC_support) {
			return;
		}
		startNFC_Listener();// 开始监听NFC设备是否连接
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isNFC_support) {// 当前Activity如果不在手机的最前端，就停止NFC设备连接的监听
			stopNFC_Listener();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 当前app正在前端界面运行，这个时候有intent发送过来，那么系统就会调用onNewIntent回调方法，将intent传送过来
		// 我们只需要在这里检验这个intent是否是NFC相关的intent，如果是，就调用处理方法
		Log.e("hjo", "传过来的Intent类型："+intent.getAction());

		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
			processIntent(intent);
		}
	}
	
	
	private void setupViews() {
		// 控件的绑定
		promt = (TextView) findViewById(R.id.promt);
		contTextView=(TextView)findViewById(R.id.content);
		promt.setText("请靠近标签进行读取：");
		tool=new Tool();
	}

	private void initNFCData() {
		// 初始化设备支持NFC功能
		isNFC_support = true;
		// 得到默认nfc适配器
		nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
		String metaInfo = "";
		// 判定设备是否支持NFC或启动NFC
		if (nfcAdapter == null) {
			metaInfo = "设备不支持NFC！";
			Toast.makeText(this, metaInfo, Toast.LENGTH_SHORT).show();
			isNFC_support = false;
		}
		if (!nfcAdapter.isEnabled()) {
			metaInfo = "请在系统设置中先启用NFC功能！";
			Toast.makeText(this, metaInfo, Toast.LENGTH_SHORT).show();
			isNFC_support = false;
		}

		if (isNFC_support == true) {
			init_NFC();
		} else {
			promt.setTextColor(Color.RED);
			promt.setText(metaInfo);
		}
	}
	
	/**
	 * 断开监听
	 */
	private void stopNFC_Listener() {
		// 停止监听NFC设备是否连接
		nfcAdapter.disableForegroundDispatch(this);
	}

	/**
	 * 设置NFC监听
	 */
	private void startNFC_Listener() {
		// 开始监听NFC设备是否连接，如果连接就发pi意图
		nfcAdapter.enableForegroundDispatch(this, pi,mFilters, mTechLists);//后两个参数理论上为空时  能过滤所有 
		
	}
	
	/**
	 * 设定过滤标签   筛选类型  
	 */
	private void init_NFC() {
		// 初始化PendingIntent，当有NFC设备连接上的时候，就交给当前Activity处理
		pi = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
		//设定要过滤的标签动作  三种种类的Tag全部过滤
				ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
				ndef.addCategory("*/*");
				IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
				IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);		
				
				mFilters = new IntentFilter[] { ndef,tag,tech };// 过滤器
				
				//定义筛选的类型     //允许扫描的标签类型
				mTechLists = new String[][] {
						new String[] { NfcF.class.getName() },
						new String[] { NfcA.class.getName() },
						new String[] { NfcB.class.getName() },
						new String[] { NfcV.class.getName() },
						new String[] { Ndef.class.getName() },
						new String[] { NdefFormatable.class.getName() },
						new String[] { IsoDep.class.getName() },
						new String[] { MifareClassic.class.getName() },
						new String[] { MifareUltralight.class.getName() } };
 
	}
	private void ShowContent(String str){
		contTextView.setText(str);
		
	}
	
	

	
	public void processIntent(Intent intent) {
		if (isNFC_support == false)
			return;
		String action=intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			ReadNdefOrNdefFormatable readNdefOrFormatable=new ReadNdefOrNdefFormatable();
			ShowContent(readNdefOrFormatable.ReadNfcTag(intent));//readNDEF(intent)
		}else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			ShowContent(readTECHandTAG(intent));
			
		}else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
			ShowContent(readTECHandTAG(intent));
		}
	}
	
	private String readTECHandTAG(Intent intent){
		Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		StringBuffer buffer=new StringBuffer();
		buffer.append("readTECHandTAG 读取的卡片类型为："+tag.toString()+"\n");
		buffer.append("卡片ID："+tool.bytesToHexString(tag.getId()) + "\n");
		String[] techs = tag.getTechList();//获取tag中的卡片类型
		List<String>techList=new ArrayList<String>();//用来存储tag中的类型
		for (String str: techs) {
			techList.add(str);
		}
		
		List<String>listclassName=tool.getClassesFromPackage(this, "com.r8c.nfc_demo.read");//获取该包下类名
		
		boolean ContentNotNull=false;
		for (int i = 0; i < techList.size(); i++) {
			String str=techList.get(i);
			str=str.substring(str.lastIndexOf(".")+1, str.length());//截取最后一个字段
			
			if (str.equals("MifareUltralight")) {
				buffer.append(readTech_MifareUltralight(tag)); 
				break;
			}else{
			
			for (int j = 0; j < listclassName.size(); j++) {
				if (listclassName.get(j ).contains(str)) {
					try {
						Class readNFC=Class.forName(listclassName.get(j ));
						Object object=readNFC.newInstance();
							Method readNfcmethod=readNFC.getMethod("ReadNfcTag", Intent.class);//获取ReadNfcTag方法并执行
							Object content=readNfcmethod.invoke(object, intent);
							if (content!=null) {
								ContentNotNull=true;
								buffer.append(content.toString());
								break;
							}
					} catch (Exception e) {
						buffer.append("读取Tag出错");
						e.printStackTrace();
					} 
				}
			}
			}
			if (ContentNotNull) {//读取到内容时退出
				break;
			}else if (!ContentNotNull && i == techList.size()-1 ) {//没有读取到内容  且已经循环到最后一个
				buffer.append("没有读取该tag的实例");
			}
		}
		
		
		
		
//		/**以下读取方式  根据排列方式优先读取*/
//		if (techList.contains("android.nfc.tech.MifareUltralight")) {
//			ReadTechMifareUltralight readTechMifareUltralight=new ReadTechMifareUltralight();
//			ReadNdefOrNdefFormatable readNdefOrFormatable=new ReadNdefOrNdefFormatable();
//			
//			String content=readTechMifareUltralight.ReadNfcTag(intent);//readTech_MifareUltralight(tag)
//			buffer.append("内容1："+content+"\n");
//			buffer.append("MifareUltralight 内容2："+readNdefOrFormatable.ReadNfcTag(intent)+"\n");//readNDEF(intent)
//			
//		}else if (techList.contains("android.nfc.tech.MifareClassic")){
//			ReadTechMifareClassic mifareClassic=new ReadTechMifareClassic();
//			
//			String content=mifareClassic.ReadNfcTag(intent);//readTech_MifareClassic(tag)
//			buffer.append("MifareClassic内容："+content+"\n");
//			
//		}else if (techList.contains("android.nfc.tech.Ndef")) {
//			ReadNdefOrNdefFormatable readNdefOrFormatable=new ReadNdefOrNdefFormatable();
//			String content=readNdefOrFormatable.ReadNfcTag(intent);//readNdef(intent)
//			buffer.append("Ndef 内容："+content+"\n");
//			
//		}else if (techList.contains("android.nfc.tech.NdefFormatable")) {//待验证
//			
//			ReadNdefOrNdefFormatable readNdefOrFormatable=new ReadNdefOrNdefFormatable();
//			String content=readNdefOrFormatable.ReadNfcTag(intent);//readNdef(intent)
//			buffer.append("内容："+content+"\n");
//			
//		}else if (techList.contains("android.nfc.tech.NfcF")) {
//			ReadTechNfcF readTechNfcF=new ReadTechNfcF();
//			ShowContent(readTechNfcF.ReadNfcTag(intent));
//		}

		return buffer.toString();
	}
	
	
//	private String readNdef(Intent intent){
//	    	
//	        NdefMessage[] msgs = null;
//	            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//	            if (rawMsgs != null) {
//	                msgs = new NdefMessage[rawMsgs.length];
//	                for (int i = 0; i < rawMsgs.length; i++) {
//	                    msgs[i] = (NdefMessage) rawMsgs[i];
//	                }
//	            } else {
//	                byte[] empty = new byte[] {};
//	                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
//	                NdefMessage msg = new NdefMessage(new NdefRecord[] {
//	                    record
//	                });
//	                msgs = new NdefMessage[] {
//	                    msg
//	                };
//	            }
//	        byte[] payload = msgs[0].getRecords()[0].getPayload();
//	        String content = "";
//			try {
//				content = new String(payload,"UTF-8");
//			} catch (UnsupportedEncodingException e) {
//				content="读取错误";
//				e.printStackTrace();
//			}
//	        return content;
//	    }
	
//	private String readNfcF(Tag tag){
//		   NfcF nfc = NfcF.get(tag);
//		   String contetn="";
//           try {
//               nfc.connect();
//               byte[] felicaIDm = new byte[]{0};
//               byte[] req = readWithoutEncryption(felicaIDm, 10);
//               byte[] res = nfc.transceive(req);
//               nfc.close();
//               contetn= tool.ByteArrayToHexString(res);
//           } catch (Exception e) {
//           }
//		return contetn;
//	}
//	
//	 private byte[] readWithoutEncryption(byte[] idm, int size)
//	            throws IOException {
//	        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);
//	        bout.write(0);           
//	        bout.write(0x06);       
//	        bout.write(idm);        
//	        bout.write(1);          
//	        bout.write(0x0f);       
//	        bout.write(0x09);        
//	        bout.write(size);        
//	        for (int i = 0; i < size; i++) {
//	            bout.write(0x80);    
//	            bout.write(i);      
//	        }
//
//	        byte[] msg = bout.toByteArray();
//	        msg[0] = (byte) msg.length; 
//	        return msg;
//	    }
	
	
	
//	private String  readNDEF(Intent intent){
//		StringBuffer buffer=new StringBuffer();
//		Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//		buffer.append("卡片ID："+tool.bytesToHexString(tag.getId()) + "\n");
//		
//		  Parcelable[] rawMsgs = intent.getParcelableArrayExtra( NfcAdapter.EXTRA_NDEF_MESSAGES);
//	        if (rawMsgs!=null) {
//	        	  NdefMessage msg = (NdefMessage) rawMsgs[0];
//	        	  NdefRecord mNdefRecord = msg.getRecords()[0];
//	        	  try {
//	        		  buffer.append("卡片内容："+new String(mNdefRecord.getPayload(),"UTF-8")+"\n");
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//			}
//		return buffer.toString();
//	}
	
	
//	public String readTech_MifareClassic(Tag tag) {
//		boolean auth = false;
//		MifareClassic mfc = MifareClassic.get(tag);
//		// 读取TAG
//		try {
//			String metaInfo = "";
//			int type = mfc.getType();// 获取TAG的类型
//			int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
//			String typeS = "";
//			switch (type) {
//			case MifareClassic.TYPE_CLASSIC:
//				typeS = "TYPE_CLASSIC";
//				break;
//			case MifareClassic.TYPE_PLUS:
//				typeS = "TYPE_PLUS";
//				break;
//			case MifareClassic.TYPE_PRO:
//				typeS = "TYPE_PRO";
//				break;
//			case MifareClassic.TYPE_UNKNOWN:
//				typeS = "TYPE_UNKNOWN";
//				break;
//			}
//			metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
//			+ mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()+ "B\n";
//			
//			for (int j = 0; j < sectorCount; j++) {
//
//				auth = mfc.authenticateSectorWithKeyA(j,
//						MifareClassic.KEY_DEFAULT);
//				int bCount;
//				int bIndex;
//				if (auth) {
//					metaInfo += "Sector " + j + ":验证成功\n";
//					// 读取扇区中的块
//					bCount = mfc.getBlockCountInSector(j);
//					bIndex = mfc.sectorToBlock(j);
//					for (int i = 0; i < bCount; i++) {
//						byte[] data = mfc.readBlock(bIndex);
//						metaInfo += "Block " + bIndex + " : "
//								+ tool.ByteArrayToHexString(data) + "\n";
//						bIndex++;
//					}
//				} else {
//					metaInfo += "Sector " + j + ":验证失败\n";
//				}
//			}
//			return metaInfo;
//		} catch (Exception e) {
//			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//			e.printStackTrace();
//		} finally {
//			if (mfc != null) {
//				try {
//					mfc.close();
//				} catch (IOException e) {
//					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
//							.show();
//				}
//			}
//		}
//		return null;
//
//	}
	
	public String readTech_MifareUltralight(Tag tag){
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
			content="读取失败";
		}finally{
			if (mifareUltralight!=null) {
				try {
					mifareUltralight.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					content=" 读取失败";
					e.printStackTrace();
				}
			}
		}
		return content;
	}
	





}
