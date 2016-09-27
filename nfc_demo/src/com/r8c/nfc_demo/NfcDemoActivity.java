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

	// NFC������
	private NfcAdapter nfcAdapter = null;
	// ������ͼ
	private PendingIntent pi = null;
	// �ı��ؼ�
	private TextView promt = null;
	// �Ƿ�֧��NFC���ܵı�ǩ
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
		startNFC_Listener();// ��ʼ����NFC�豸�Ƿ�����
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isNFC_support) {// ��ǰActivity��������ֻ�����ǰ�ˣ���ֹͣNFC�豸���ӵļ���
			stopNFC_Listener();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// ��ǰapp����ǰ�˽������У����ʱ����intent���͹�������ôϵͳ�ͻ����onNewIntent�ص���������intent���͹���
		// ����ֻ��Ҫ������������intent�Ƿ���NFC��ص�intent������ǣ��͵��ô�����
		Log.e("hjo", "��������Intent���ͣ�"+intent.getAction());

		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
			processIntent(intent);
		}
	}
	
	
	private void setupViews() {
		// �ؼ��İ�
		promt = (TextView) findViewById(R.id.promt);
		contTextView=(TextView)findViewById(R.id.content);
		promt.setText("�뿿����ǩ���ж�ȡ��");
		tool=new Tool();
	}

	private void initNFCData() {
		// ��ʼ���豸֧��NFC����
		isNFC_support = true;
		// �õ�Ĭ��nfc������
		nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
		String metaInfo = "";
		// �ж��豸�Ƿ�֧��NFC������NFC
		if (nfcAdapter == null) {
			metaInfo = "�豸��֧��NFC��";
			Toast.makeText(this, metaInfo, Toast.LENGTH_SHORT).show();
			isNFC_support = false;
		}
		if (!nfcAdapter.isEnabled()) {
			metaInfo = "����ϵͳ������������NFC���ܣ�";
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
	 * �Ͽ�����
	 */
	private void stopNFC_Listener() {
		// ֹͣ����NFC�豸�Ƿ�����
		nfcAdapter.disableForegroundDispatch(this);
	}

	/**
	 * ����NFC����
	 */
	private void startNFC_Listener() {
		// ��ʼ����NFC�豸�Ƿ����ӣ�������Ӿͷ�pi��ͼ
		nfcAdapter.enableForegroundDispatch(this, pi,mFilters, mTechLists);//����������������Ϊ��ʱ  �ܹ������� 
		
	}
	
	/**
	 * �趨���˱�ǩ   ɸѡ����  
	 */
	private void init_NFC() {
		// ��ʼ��PendingIntent������NFC�豸�����ϵ�ʱ�򣬾ͽ�����ǰActivity����
		pi = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
		//�趨Ҫ���˵ı�ǩ����  ���������Tagȫ������
				ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
				ndef.addCategory("*/*");
				IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
				IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);		
				
				mFilters = new IntentFilter[] { ndef,tag,tech };// ������
				
				//����ɸѡ������     //����ɨ��ı�ǩ����
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
		buffer.append("readTECHandTAG ��ȡ�Ŀ�Ƭ����Ϊ��"+tag.toString()+"\n");
		buffer.append("��ƬID��"+tool.bytesToHexString(tag.getId()) + "\n");
		String[] techs = tag.getTechList();//��ȡtag�еĿ�Ƭ����
		List<String>techList=new ArrayList<String>();//�����洢tag�е�����
		for (String str: techs) {
			techList.add(str);
		}
		
		List<String>listclassName=tool.getClassesFromPackage(this, "com.r8c.nfc_demo.read");//��ȡ�ð�������
		
		boolean ContentNotNull=false;
		for (int i = 0; i < techList.size(); i++) {
			String str=techList.get(i);
			str=str.substring(str.lastIndexOf(".")+1, str.length());//��ȡ���һ���ֶ�
			
			if (str.equals("MifareUltralight")) {
				buffer.append(readTech_MifareUltralight(tag)); 
				break;
			}else{
			
			for (int j = 0; j < listclassName.size(); j++) {
				if (listclassName.get(j ).contains(str)) {
					try {
						Class readNFC=Class.forName(listclassName.get(j ));
						Object object=readNFC.newInstance();
							Method readNfcmethod=readNFC.getMethod("ReadNfcTag", Intent.class);//��ȡReadNfcTag������ִ��
							Object content=readNfcmethod.invoke(object, intent);
							if (content!=null) {
								ContentNotNull=true;
								buffer.append(content.toString());
								break;
							}
					} catch (Exception e) {
						buffer.append("��ȡTag����");
						e.printStackTrace();
					} 
				}
			}
			}
			if (ContentNotNull) {//��ȡ������ʱ�˳�
				break;
			}else if (!ContentNotNull && i == techList.size()-1 ) {//û�ж�ȡ������  ���Ѿ�ѭ�������һ��
				buffer.append("û�ж�ȡ��tag��ʵ��");
			}
		}
		
		
		
		
//		/**���¶�ȡ��ʽ  �������з�ʽ���ȶ�ȡ*/
//		if (techList.contains("android.nfc.tech.MifareUltralight")) {
//			ReadTechMifareUltralight readTechMifareUltralight=new ReadTechMifareUltralight();
//			ReadNdefOrNdefFormatable readNdefOrFormatable=new ReadNdefOrNdefFormatable();
//			
//			String content=readTechMifareUltralight.ReadNfcTag(intent);//readTech_MifareUltralight(tag)
//			buffer.append("����1��"+content+"\n");
//			buffer.append("MifareUltralight ����2��"+readNdefOrFormatable.ReadNfcTag(intent)+"\n");//readNDEF(intent)
//			
//		}else if (techList.contains("android.nfc.tech.MifareClassic")){
//			ReadTechMifareClassic mifareClassic=new ReadTechMifareClassic();
//			
//			String content=mifareClassic.ReadNfcTag(intent);//readTech_MifareClassic(tag)
//			buffer.append("MifareClassic���ݣ�"+content+"\n");
//			
//		}else if (techList.contains("android.nfc.tech.Ndef")) {
//			ReadNdefOrNdefFormatable readNdefOrFormatable=new ReadNdefOrNdefFormatable();
//			String content=readNdefOrFormatable.ReadNfcTag(intent);//readNdef(intent)
//			buffer.append("Ndef ���ݣ�"+content+"\n");
//			
//		}else if (techList.contains("android.nfc.tech.NdefFormatable")) {//����֤
//			
//			ReadNdefOrNdefFormatable readNdefOrFormatable=new ReadNdefOrNdefFormatable();
//			String content=readNdefOrFormatable.ReadNfcTag(intent);//readNdef(intent)
//			buffer.append("���ݣ�"+content+"\n");
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
//				content="��ȡ����";
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
//		buffer.append("��ƬID��"+tool.bytesToHexString(tag.getId()) + "\n");
//		
//		  Parcelable[] rawMsgs = intent.getParcelableArrayExtra( NfcAdapter.EXTRA_NDEF_MESSAGES);
//	        if (rawMsgs!=null) {
//	        	  NdefMessage msg = (NdefMessage) rawMsgs[0];
//	        	  NdefRecord mNdefRecord = msg.getRecords()[0];
//	        	  try {
//	        		  buffer.append("��Ƭ���ݣ�"+new String(mNdefRecord.getPayload(),"UTF-8")+"\n");
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//			}
//		return buffer.toString();
//	}
	
	
//	public String readTech_MifareClassic(Tag tag) {
//		boolean auth = false;
//		MifareClassic mfc = MifareClassic.get(tag);
//		// ��ȡTAG
//		try {
//			String metaInfo = "";
//			int type = mfc.getType();// ��ȡTAG������
//			int sectorCount = mfc.getSectorCount();// ��ȡTAG�а�����������
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
//			metaInfo += "��Ƭ���ͣ�" + typeS + "\n��" + sectorCount + "������\n��"
//			+ mfc.getBlockCount() + "����\n�洢�ռ�: " + mfc.getSize()+ "B\n";
//			
//			for (int j = 0; j < sectorCount; j++) {
//
//				auth = mfc.authenticateSectorWithKeyA(j,
//						MifareClassic.KEY_DEFAULT);
//				int bCount;
//				int bIndex;
//				if (auth) {
//					metaInfo += "Sector " + j + ":��֤�ɹ�\n";
//					// ��ȡ�����еĿ�
//					bCount = mfc.getBlockCountInSector(j);
//					bIndex = mfc.sectorToBlock(j);
//					for (int i = 0; i < bCount; i++) {
//						byte[] data = mfc.readBlock(bIndex);
//						metaInfo += "Block " + bIndex + " : "
//								+ tool.ByteArrayToHexString(data) + "\n";
//						bIndex++;
//					}
//				} else {
//					metaInfo += "Sector " + j + ":��֤ʧ��\n";
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
			content="page1��"+tool.ByteArrayToHexString(payload)+"\n"+"��������"+String.valueOf(size)+"\n";
			
			//����ֻ��ȡ�����м���page��
			byte[] payload1 = mifareUltralight.readPages(4);
			byte[] payload2 = mifareUltralight.readPages(8);
			byte[] payload3 = mifareUltralight.readPages(12);
			content+="page4:"+tool.ByteArrayToHexString(payload1)+"\npage8:"+tool.ByteArrayToHexString(payload2)+
					"\npage12��"+tool.ByteArrayToHexString(payload3)+"\n";
			
		} catch (Exception e) {
			// TODO: handle exception
			content="��ȡʧ��";
		}finally{
			if (mifareUltralight!=null) {
				try {
					mifareUltralight.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					content=" ��ȡʧ��";
					e.printStackTrace();
				}
			}
		}
		return content;
	}
	





}
