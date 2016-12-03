package com.mingmay.bulan.util;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;

public class QRCodeUtil {
	/**
	 * 用字符串生成二维码
	 * 
	 * @param str
	 * @author zhouzhe@lenovo-cw.com
	 * @return
	 * @throws WriterException
	 */
	public Bitmap Create2DCode(String str,int h) throws WriterException {
		Bitmap qrCodeBitmap = EncodingHandler.createQRCode(str, h);
		return qrCodeBitmap;
	}
}
