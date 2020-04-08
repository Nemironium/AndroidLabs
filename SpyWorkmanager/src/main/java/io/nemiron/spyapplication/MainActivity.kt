package io.nemiron.spyapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import io.nemiron.spyapplication.spymodule.SpyInterface
import io.nemiron.spyapplication.spymodule.loadModule
import io.nemiron.spyapplication.worker.BackgroundSpyWorker
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private var spyClass: SpyInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        spyClass = loadModule<SpyInterface>(
            "io.nemiron.spyapplication.spymodule.SpyClass",
            copyDexFile("spyModule.dex"),
            cacheDir,
            classLoader
        )

        if (spyClass != null) {
            Log.d("LOG_TAG", "${spyClass!!.getAvailableMemory(this)} MB")
            Log.d("LOG_TAG", "${spyClass!!.getBatteryPct(this)} %")
            Log.d("LOG_TAG", spyClass!!.getCurrentDate())
        }

        super.onCreate(savedInstanceState)
        startTask()
    }

    // Метод для запуска задачи
    private fun startTask() {
        // Ограничение на запуск работы только при доступности любого интернет-подключения
        val constrains = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

         /* Ограничиваем работу: исполняться раз в 15 минут; минимальный интервал 5 минут;
        Добавляем тэг для возможности в дальнейшем отменить задачу по тэгу */
        val spyWork = PeriodicWorkRequestBuilder<BackgroundSpyWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES)
                .addTag("SPYWORK")
                .setConstraints(constrains)
                .build()
         /*запускаем "уникальную" переодическую работу
        "уникальность" предотвращает повторный запуск работы*/
        WorkManager.getInstance(applicationContext)
                .enqueueUniquePeriodicWork("spyJob", ExistingPeriodicWorkPolicy.KEEP, spyWork)
    }

    // метод для отмены работы по созданному тэгу
    private fun stopTask() = WorkManager.getInstance(applicationContext).cancelAllWorkByTag("SPYWORK")

    // метод для копирования файла из assets в директорию приложения
    private fun copyDexFile(name: String) = FileUtils.copyToFile(assets.open(name), File(sourceFile(name)))
        .let { File(sourceFile(name)) }

    // метод для получения пути директории, в которую будет скопирован .dex файл
    private fun sourceFile(name: String) = packageManager.getPackageInfo(packageName, 0)
        .applicationInfo.dataDir + "/files/" + name
}
