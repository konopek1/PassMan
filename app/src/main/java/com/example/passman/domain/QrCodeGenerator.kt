package com.example.passman.domain

import android.R.dimen
import android.graphics.Bitmap

import androidmads.library.qrgenearator.QRGContents

import androidmads.library.qrgenearator.QRGEncoder




class QrCodeGenerator {

    fun getQrCodeBitMap(data: String,height: Int): Bitmap {
        val qrCode = QRGEncoder(data, null, QRGContents.Type.TEXT, height)

        return qrCode.encodeAsBitmap()
    }
}