package io.nemiron.spyapplication.spymodule

import android.util.Log
import dalvik.system.DexClassLoader
import java.io.File


@Suppress("UNCHECKED_CAST")
fun <T> loadModule(className: String, dexFile: File, cacheDir: File, parent: ClassLoader) : T? {
    try {
        val classLoader = DexClassLoader(dexFile.absolutePath, cacheDir.absolutePath, null, parent)
        val moduleClass = classLoader.loadClass(className)

        Log.d("LOG_TAG", "class name: ${moduleClass.name}")
        Log.d("LOG_TAG", "class loader: ${moduleClass.classLoader}")
        Log.d("LOG_TAG", "class interface: ${moduleClass.interfaces[0].name}")


        /*if (SpyInfoInterface::class.java.isAssignableFrom(moduleClass))*/
            return moduleClass.newInstance() as T
        } catch (e: Exception) {
            Log.e("LOG_TAG", "${e.message}")
        }

    return null
}
