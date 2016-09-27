package com.r8c.nfc_demo.read;

import java.io.IOException;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.widget.Toast;

import com.r8c.nfc_demo.Tool;
import com.r8c.nfc_demo.read_interface.ReadNFC;

public class ReadTechMifareClassic implements ReadNFC{

	@Override
	public String ReadNfcTag(Intent intent) {
		// TODO Auto-generated method stub
		Tag tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Tool tool=new Tool();
		boolean auth = false;
		MifareClassic mfc = MifareClassic.get(tag);
		String metaInfo = "";
		// 读取TAG
		try {
			
			int type = mfc.getType();// 获取TAG的类型
			int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
			String typeS = "";
			switch (type) {
			case MifareClassic.TYPE_CLASSIC:
				typeS = "TYPE_CLASSIC";
				break;
			case MifareClassic.TYPE_PLUS:
				typeS = "TYPE_PLUS";
				break;
			case MifareClassic.TYPE_PRO:
				typeS = "TYPE_PRO";
				break;
			case MifareClassic.TYPE_UNKNOWN:
				typeS = "TYPE_UNKNOWN";
				break;
			}
			metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
			+ mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()+ "B\n";
			
			for (int j = 0; j < sectorCount; j++) {

				auth = mfc.authenticateSectorWithKeyA(j,
						MifareClassic.KEY_DEFAULT);
				int bCount;
				int bIndex;
				if (auth) {
					metaInfo += "Sector " + j + ":验证成功\n";
					// 读取扇区中的块
					bCount = mfc.getBlockCountInSector(j);
					bIndex = mfc.sectorToBlock(j);
					for (int i = 0; i < bCount; i++) {
						byte[] data = mfc.readBlock(bIndex);
						metaInfo += "Block " + bIndex + " : "
								+ tool.ByteArrayToHexString(data) + "\n";
						bIndex++;
					}
				} else {
					metaInfo += "Sector " + j + ":验证失败\n";
				}
			}
			return metaInfo;
		} catch (Exception e) {
//			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} finally {
			if (mfc != null) {
				try {
					mfc.close();
				} catch (IOException e) {
//					Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
//							.show();
				}
			}
		}
		return metaInfo;
		
	}

}
