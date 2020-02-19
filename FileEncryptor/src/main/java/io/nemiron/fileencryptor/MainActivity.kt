package io.nemiron.fileencryptor

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import io.nemiron.fileencryptor.utils.Cipher
import io.nemiron.wifiscanner.utils.ManagePermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        // код для запроса File Chooser
        private const val encryptCode = 101
        // код для запроса permissions
        private const val REQ_CODE = 103
    }
    // экземпляр класса для выдачи разрешений.
    // lateinit показывает, что свойство будет определено в дальнейшем
    private lateinit var managePermissions: ManagePermissions

    private lateinit var cipher: Cipher



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициируем класс для работы с шифрованием
        cipher = Cipher(this)

        checkSDcard()
        initPermissions()
        checkDirs()
        initViews()
    }

    private fun initViews() {
        // обработчики кнопок
        crypt_button.setOnClickListener{
            checkSDcard()
            checkDirs()
            cipher.cipherFile(select_encrypt_text.text.toString(), false)
        }

        decrypt_button.setOnClickListener{
            checkSDcard()
            checkDirs()
            cipher.decipherFile((select_decrypt_spinner.selectedItem?.toString()) ?: "")
        }

        select_encrypt_text.setOnClickListener {
            // создаём интент с вызовом File Explorer приложения с любым типом файлов
            // EXTRA_LOCAL_ONLY позволяет выбрать только файлы на устройстве
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.setType("*/*").putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                startActivityForResult(Intent.createChooser(it, getString(R.string.select_file)), encryptCode)
            }
        }

        refreshSpinner()
    }

    // обновляет список файлов, доступных для расшифрования
    private fun refreshSpinner() {
        val cryptDir = File(Environment.getExternalStorageDirectory().toString() + "/crypt")
        // список, в котором хранятся имена всех файлов из директории crypt
        val fileList = mutableListOf<String>()

        cryptDir.listFiles()?.toList()?.forEach {
            fileList.add(it.name)
        }

        if (fileList.isNotEmpty()) {
            val adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_spinner_item, fileList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            select_decrypt_spinner.adapter = adapter
        }
    }

    private fun checkSDcard() {
        // проверяем, что External storage доступно для чтения и записи
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Toast.makeText(this, getString(R.string.not_mounted), Toast.LENGTH_LONG).show()
        }
    }

    private fun initPermissions() {
        // задаём необходимые привелегии
        val permissionsList = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        managePermissions = ManagePermissions(this, permissionsList, REQ_CODE)
        managePermissions.checkPermissions()
    }

    private fun checkDirs() {
        val cryptPath = Environment.getExternalStorageDirectory().toString() + "/crypt"
        val decryptPath = Environment.getExternalStorageDirectory().toString() + "/decrypt"
        val cryptDir = File(cryptPath)
        val decryptDir = File(decryptPath)

        if (!cryptDir.isDirectory) {
            cryptDir.mkdir()
            Toast.makeText(this, getString(R.string.crypt_created), Toast.LENGTH_LONG).show()
        }
        if (!decryptDir.isDirectory) {
            decryptDir.mkdir()
            Toast.makeText(this, getString(R.string.decrypt_created), Toast.LENGTH_LONG).show()
        }

        // проверяем, что в директории только шифрованные файлы
        if (cryptDir.listFiles() == null)
            return
        for (file in cryptDir.listFiles()!!) {
            if ("_encrypted" !in file.path) {
                cipher.cipherFile(file.path, true)
            }
        }
    }

    // обработка результатов intent'ов
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // проверяем результат intent'а с ACTION_GET_CONTENT
        if (resultCode == Activity.RESULT_OK && requestCode == encryptCode) {
            // получаем путь к выбранному файлу через URI
            var selectedFile = data?.data?.path.toString()
            // заменяем URI путь на абсолютный путь
            when {
                "/document/primary:" in selectedFile -> {
                    selectedFile = selectedFile.replace("/document/primary:",
                        Environment.getExternalStorageDirectory().absolutePath + "/")
                    select_encrypt_text.text = selectedFile
                }
                else -> Toast.makeText(this, getString(R.string.error_path), Toast.LENGTH_LONG).show()
            }
        }
    }

    // обработка результата запроса привелегий
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when(requestCode) {
            REQ_CODE -> {
                val isPermissionsGranted = managePermissions.processPermissionsResult(grantResults)

                if(isPermissionsGranted){
                    Toast.makeText(this, getString(R.string.granted), Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this, getString(R.string.denied), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // заполнение меню
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // обработка нажатий в меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.settings ->
                Toast.makeText(this, getString(R.string.password_selection), Toast.LENGTH_LONG).show()
            R.id.refresh -> refreshSpinner()
        }
        return super.onOptionsItemSelected(item)
    }
}
