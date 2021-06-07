package com.example.passman

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.passman.domain.EncodedRSAKeys

class QrCodeScannerContract : ActivityResultContract<Unit, EncodedRSAKeys>() {
    override fun createIntent(context: Context, input: Unit?): Intent = Intent(context, QrCodeScannerActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): EncodedRSAKeys? {
        val qrcode = intent?.getStringExtra("QR")

        val keyPair = qrcode?.split("|")

        if (keyPair != null) {
            return EncodedRSAKeys(keyPair[0], keyPair[1])
        }

        return EncodedRSAKeys("", "")
    }


}
