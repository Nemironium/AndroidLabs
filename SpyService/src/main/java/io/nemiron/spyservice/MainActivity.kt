package io.nemiron.spyservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.nemiron.spyservice.services.SpyService
import io.nemiron.spyservice.services.SpyService.Companion.isServiceRun
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    private fun initViews() {
        start_button.setOnClickListener {
            // проверяем, что сервис уже не запущен
            if (!isServiceRun) {
                // создаём Intent для сервиса
                val intent = Intent(this, SpyService::class.java)
                // запускаем сервис
                startService(intent)
                textView.text = getString(R.string.service_is_running)
            }
            else
                textView.text = getString(R.string.service_already_running)
        }

        stop_button.setOnClickListener {
            // проверяем, что сервис запущен
            if (isServiceRun) {
                val intent = Intent(this, SpyService::class.java)
                // останавливаем сервис
                stopService(intent)
                textView.text = getString(R.string.service_is_not_running)
            }
            else
                textView.text = getString(R.string.service_not_running_yet)
        }
    }
}
