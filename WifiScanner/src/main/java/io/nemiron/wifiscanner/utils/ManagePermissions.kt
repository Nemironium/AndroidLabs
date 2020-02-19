package io.nemiron.wifiscanner.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class ManagePermissions(
    private val activity: Activity,
    private val listPermissions: List<String>,
    private val code: Int
) {

    // проверить наличия разрешения
    fun checkPermissions() =
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert()
        } else {
            // оповестить пользователя, что привилегии необходимы для работы
        }


    // проверить статус разрешения
    private fun isPermissionsGranted(): Int {
        var counter = 0

        for (permission in listPermissions)
            counter += ContextCompat.checkSelfPermission(activity, permission)

        return counter
    }

    // найти первое запрещённое разрешение
    private fun deniedPermission(): String {
        for (permission in listPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    == PackageManager.PERMISSION_DENIED)
                return permission
        }
        return ""
    }

    // показать AlertDialog  спредупреждением о запросе разрешений
    private fun showAlert() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Need permission(s)")
        builder.setMessage("Some permissions are required to do the task.")
        builder.setPositiveButton("OK") { _, _ -> requestPermissions() }
        builder.setNeutralButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    // запрос разрешения
    private fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // здесь лучше сделать Snackbar заместо Toast
            Toast.makeText(activity, "Permissions are needed to detect WiFi points", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(activity, listPermissions.toTypedArray(), code)
    }

    // проверка полученных разрешений
    fun processPermissionsResult(grantResults: IntArray): Boolean {
        var result = 0
        if (grantResults.isNotEmpty()) {
            for (item in grantResults) {
                result += item
            }
        }
        if (result == PackageManager.PERMISSION_GRANTED)
            return true
        return false
    }
}
