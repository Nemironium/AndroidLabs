package io.nemiron.spyservice.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import io.nemiron.spyapplication.util.SpyUtils

// наследуемся от IntentService. Необходимо обязательно ввести какое-либо имя
class SpyService : IntentService("SpyService") {
    // Тэг для удобства просмотра в LogCat
    private val TAG = "SERVICE_TAG"

    /* т.к. в Kotlin нет привычного модификатора static, то можно создать singleton-объект
    внутри нашего класса, к которому можно будет иметь доступ из любого места.
    Данная методика не подходит для нескольких потоков, но в нашем случае всегда
    будет ровно 1 поток */
    companion object {
        var isServiceRun = false
    }

    /* Обязательная Overridden функция, в которой должен находиться функционал сервиса.
   Исполняется в отдельном потоке */
    override fun onHandleIntent(intent: Intent?) {
        isServiceRun = true
        sendInformation()
    }

    /* Overridden функция, в которой происходит завершение сервиса.
    Можно непереопределять метод, но в нём мы меняем флаг о запуске сервиса */
    override fun onDestroy() {
        isServiceRun = false
        super.onDestroy()
    }

    // Выводим полученную информацию в LogCat
    private fun sendInformation() {
        Log.d(TAG, "battery state: ${SpyUtils().getBatteryPct(applicationContext)}%")
        Log.d(TAG, "available memory: ${SpyUtils().getAvailableMemory(applicationContext)} MB")
        Log.d(TAG, "date: ${SpyUtils().getCurrentDate()}")
    }
}