package io.nemiron.wifiscanner

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import io.nemiron.wifiscanner.utils.ManagePermissions
import io.nemiron.wifiscanner.utils.calculateSignalLevel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQ_CODE = 123
        private const val TAG = "WIFI_TAG"
    }

    // экземпляр класса для выдачи разрешений.
    // lateinit показывает, что свойство будет определено в дальнейшем
    private lateinit var managePermissions: ManagePermissions

    // list для хранения результатов сканирования WiFi
    private var resultList = ArrayList<ScanResult>()

    // переменная для доступа к информации о WiFi
    private lateinit var wifiManager: WifiManager

    // приёмник широковещательных сообщений для информации о WiFi точках
    private val wifiReceiver = object : BroadcastReceiver() {
        // единственная необходимая функция в классе
        // она вызывается, если приходит какое-либо сообщения
        override fun onReceive(context: Context, intent: Intent) {
            // освобождаем приёмник, чтобы не расходовать ресурсы
            unregisterReceiver(this)
            // через интент проверяем, что сканирование было успешным
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            // обрабатываем результаты сканирования
            if (success) {
                scanSuccess()
                Log.d(TAG, "scan successful")
            }
            else {
                scanFailure()
                Log.d(TAG, "scan failed")
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermissions()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        scan_btn.setOnClickListener {
            // проверяем, включен ли Wi-Fi, включаем, если не был включен
            checkWifi()
            // начинаем сканирование ближайших точек
            scanWifi()
        }
    }

    private fun initPermissions() {
        // задаём необходимые привелегии
        val permissionsList = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        managePermissions = ManagePermissions(this, permissionsList, REQ_CODE)
        // проверяем, получены ли привелегии
        managePermissions.checkPermissions()
    }

    private fun checkWifi() {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "WiFi is disabled. Enabling WiFi...", Toast.LENGTH_LONG).show()
            wifiManager.isWifiEnabled = true
        }
    }

    private fun scanWifi() {
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show()
    }



    private fun scanSuccess() {
        resultList.clear()
        // заполняем массив новыми WiFi точками
        resultList = wifiManager.scanResults as ArrayList<ScanResult>
        // обновляем результаты в ListView
        //uploadListView()
        // только для отладки
        for (result in resultList) {
            Log.d(TAG, "SSID: ${result.SSID}")
            Log.d(TAG, "BSSID: ${result.BSSID}")
            Log.d(TAG, "level: ${calculateSignalLevel(result.level)}")
            Log.d(TAG, "frequency: ${result.frequency} hHz")
            Log.d(TAG, "capabilities: ${result.capabilities}")
        }
    }

    private fun scanFailure() {}

    // обработка результата запроса привелегий
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when(requestCode) {
            REQ_CODE -> {
                val isPermissionsGranted = managePermissions
                    .processPermissionsResult(grantResults)

                if(isPermissionsGranted){
                    Toast.makeText(this, "Permissions granted.", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
