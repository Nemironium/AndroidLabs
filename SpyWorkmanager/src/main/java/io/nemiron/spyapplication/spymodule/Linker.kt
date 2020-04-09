package io.nemiron.spyapplication.spymodule

import dalvik.system.DexClassLoader
import java.io.File

inline fun <reified T> loadModule(className: String, dexFile: File, cacheDir: File, parent: ClassLoader) : T? {
    try {
        val classLoader = DexClassLoader(dexFile.absolutePath, cacheDir.absolutePath, null, parent)
        val moduleClass = classLoader.loadClass(className)

        if (T::class.java.isAssignableFrom(moduleClass))
            return moduleClass.newInstance() as T
        } catch (e: Exception) {
            e.printStackTrace()
        }

    return null
}
