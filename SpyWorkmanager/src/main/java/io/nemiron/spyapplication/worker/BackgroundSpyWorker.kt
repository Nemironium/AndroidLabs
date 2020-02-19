package io.nemiron.spyapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.nemiron.spyapplication.network.Api
import io.nemiron.spyapplication.network.SpyData
import io.nemiron.spyapplication.util.SpyUtils

class BackgroundSpyWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    // тэг для отладки в LogCat'е
    private val TAG = "SPY_APP"

    /* в этом переопределённом методе должен быть весь функционал работы
    он запускает в отдельном background потоке
    метод всегда должен возвращать какой-либо результат работы */
    override fun doWork(): Result {
        try {
            val spyData = SpyData(
                date = SpyUtils().getCurrentDate(),
                memory = SpyUtils().getAvailableMemory(applicationContext),
                battery= SpyUtils().getBatteryPct(applicationContext)
            )
            val call = Api.getInstance().uploadData(spyData)

            /* Так как WorkManager уже работает в отдельном потоке, то можно сделать запрос в сеть синхронным.
            * Во всех остальных случаях это вызовет исключение, потому что запрос должен быть асинхронным
            * через enqueue() */

            val response = call.execute()

            if (response.isSuccessful) {
                Log.d(TAG, "Successfully post'ed")
                return Result.success()
            } else {
                Log.d("SPY_APP", "Request failed. URL: ${call.request().url}. Retrying...")
                return Result.retry()
            }

        } catch (e: Exception) {
            Log.d(TAG, "Exception occurred: ${e.message}")
            return Result.failure()
        }
    }

}