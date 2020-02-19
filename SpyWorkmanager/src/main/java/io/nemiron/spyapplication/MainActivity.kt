package io.nemiron.spyapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import io.nemiron.spyapplication.worker.BackgroundSpyWorker
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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

    // отменяем работу по созданному тэгу
    private fun stopTask() = WorkManager.getInstance(applicationContext).cancelAllWorkByTag("SPYWORK")
}
