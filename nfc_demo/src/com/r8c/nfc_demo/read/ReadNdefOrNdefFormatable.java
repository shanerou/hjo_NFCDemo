package com.r8c.nfc_demo.read;

import java.io.UnsupportedEncodingException;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import com.r8c.nfc_demo.read_interface.ReadNFC;

public class ReadNdefOrNdefFormatable implements ReadNFC{

	@Override
	public String ReadNfcTag(Intent intent) {
		NdefMessage[] msgs = null;
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
        } else {
            byte[] empty = new byte[] {};
            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
            NdefMessage msg = new NdefMessage(new NdefRecord[] {
                record
            });
            msgs = new NdefMessage[] {
                msg
            };
        }
    byte[] payload = msgs[0].getRecords()[0].getPayload();
    String content = "";
	try {
		content = new String(payload,"UTF-8");
	} catch (UnsupportedEncodingException e) {
		content="¶ÁÈ¡´íÎó";
		e.printStackTrace();
	}
    return content;
	}

}
