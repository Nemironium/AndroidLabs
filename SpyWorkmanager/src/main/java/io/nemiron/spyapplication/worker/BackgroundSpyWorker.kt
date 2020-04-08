package io.nemiron.spyapplication.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.nemiron.spyapplication.network.Api
import io.nemiron.spyapplication.network.SpyData
import io.nemiron.spyapplication.spymodule.SpyInterface
import io.nemiron.spyapplication.spymodule.loadModule
import org.apache.commons.io.FileUtils
import java.io.File

class BackgroundSpyWorker(private val appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    // тэг для отладки в LogCat'е
    private val TAG = "SPY_APP"

    private val spyClass = loadModule<SpyInterface>(
        "io.nemiron.spyapplication.spymodule.SpyClass",
        copyDexFile("spyModule.dex"),
        appContext.cacheDir,
        appContext.classLoader
    )

    /* в этом переопределённом методе должен быть весь функционал работы
    он запускает в отдельном background потоке
    метод всегда должен возвращать какой-либо результат работы */
    override fun doWork(): Result {
        try {
            if (spyClass != null) {
                val spyData = SpyData(
                    date = spyClass.getCurrentDate(),
                    memory = spyClass.getAvailableMemory(applicationContext),
                    battery= spyClass.getBatteryPct(applicationContext)
                )

                Log.d(TAG, "$spyData")

                val call = Api.getInstance().uploadData(spyData)

                /* Так как WorkManager уже работает в отдельном потоке, то можно сделать запрос в сеть синхронным.
                * Во всех остальных случаях это вызовет исключение, потому что запрос должен быть асинхронным
                * через enqueue() */

                val response = call.execute()

                return if (response.isSuccessful) {
                    Log.d(TAG, "Successfully post'ed")
                    Result.success()
                } else {
                    Log.d("SPY_APP", "Request failed. URL: ${call.request().url}. Retrying...")
                    Result.retry()
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "Exception occurred: ${e.message}")
            return Result.failure()
        }

        return Result.failure()
    }

    // метод для копирования файла из assets в директорию приложения
    private fun copyDexFile(name: String) = FileUtils.copyToFile(appContext.assets.open(name), File(sourceFile(name)))
        .let { File(sourceFile(name)) }

    // метод для получения пути директории, в которую будет скопирован .dex файл
    private fun sourceFile(name: String) = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        .applicationInfo.dataDir + "/files/" + name
}