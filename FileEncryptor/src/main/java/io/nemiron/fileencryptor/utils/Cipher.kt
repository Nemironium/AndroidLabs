package io.nemiron.fileencryptor.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import io.nemiron.fileencryptor.R

class Cipher(private val context: Context) {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    fun cipherFile(filePath: String, isRemove: Boolean) {
        if (!encryptFile(filePath,
                Environment.getExternalStorageDirectory().toString() + "/crypt/", isRemove)) {
            Toast.makeText(context, context.getString(R.string.error_encrypt), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, context.getString(R.string.encrypt_success), Toast.LENGTH_LONG).show()
        }
    }

    fun decipherFile(filePath: String) {
        if ("_encrypted" !in filePath) {
            Toast.makeText(context, context.getString(R.string.cant_decrypt), Toast.LENGTH_LONG).show()
            return
        }
        if (!decryptFile(
                Environment.getExternalStorageDirectory().toString() + "/crypt/" + filePath,
                Environment.getExternalStorageDirectory().toString() + "/decrypt/")) {
            Toast.makeText(context, context.getString(R.string.error_decrypt), Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(context, context.getString(R.string.decrypt_success), Toast.LENGTH_LONG).show()
        }
    }

    private external fun decryptFile(filePath: String, dirPath: String): Boolean
    private external fun encryptFile(filePath: String, dirPath: String, isRemove:Boolean): Boolean
}