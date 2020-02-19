#include <jni.h>            // For JNI types and JNI functions
#include <stdio.h>          // For FILE
#include <string.h>         // For strlen(), strcat(), strncpy(), strdup()
#include <stdlib.h>         // For calloc(), free()
#include <libgen.h>         // For basename() - only for Linux
#include <android/log.h>    // For __android_log_print()

// размер блока для считывания/записи данных в файле (лучше выбирать кратным размеру блока ФС)
const size_t BUF_SIZE = 4096;

// for logging from NDK
//#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,"NDK_TAG",__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"NDK_TAG",__VA_ARGS__)

// шифрует строку XOR-шифрованием с ключом 0xff
bool encryptDecryptFile(const char *filePath, const char *dirPath, size_t mode, bool isRemove)
{
    FILE *inFile = NULL;
    FILE *outFile = NULL;
    inFile = fopen(filePath, "rb");

    if (inFile == NULL) {
        LOGE("Error: cannot open file %s", filePath);
        return false;
    }

    char *newFile = NULL;
    // выделяем имя файла из абсолютного пути
    char *pathBuf = strdup(filePath);
    char *fileName = basename(pathBuf);

    // шифрование файла
    if (mode == 1) {
        // новая память под путь: папка decrypt + имя файла + суффикс _encrypted
        newFile = (char *)calloc(strlen(dirPath) + strlen(fileName) + strlen("_encrypted") + 1, sizeof(char));
        strcat(strcat(strcat(newFile, dirPath), fileName), "_encrypted");
    }
        // расшифрование файла
    else if (mode == 2) {
        // память под имя файла без _encrypted
        char *cutFileName = (char *)calloc(strlen(fileName) - strlen("_encrypted"), sizeof(char));
        strncpy(cutFileName, fileName, strlen(fileName) - strlen("_encrypted"));
        // новая память под путь: папка decrypt + имя файла
        newFile = (char *)calloc(strlen(dirPath) + strlen(cutFileName), sizeof(char));
        strcat(strcat(newFile, dirPath), cutFileName);
        free(cutFileName);
    }

    outFile = fopen(newFile, "wb");
    if (outFile == NULL) {
        LOGE("Error: cannot open file %s", newFile);
        return false;
    }

    // считывание данных блоками, XOR-шифрование, запись шифротекста в новый файл
    size_t readCtr = 0;
    char buf[BUF_SIZE] = {0};
    while ((readCtr = fread(buf, sizeof(char), BUF_SIZE, inFile))) {
        for (size_t i = 0; i < readCtr; i++) {
            buf[i] ^= 0xff;
        }
        fwrite(buf, sizeof(char), readCtr, outFile);
    }

    fclose(inFile);
    fclose(outFile);
    free(newFile);
    free(pathBuf);

    if (isRemove)
        remove(filePath);

    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_io_nemiron_fileencryptor_utils_Cipher_decryptFile(JNIEnv *env, jobject, jstring filePath,
                                                 jstring dirPath) {
    const char *nativeFilePath = env->GetStringUTFChars(filePath, JNI_FALSE);
    const char *nativeDirPath = env->GetStringUTFChars(dirPath, JNI_FALSE);

    bool check = encryptDecryptFile(nativeFilePath, nativeDirPath, 2, false);
    env->ReleaseStringUTFChars(filePath, nativeFilePath);
    env->ReleaseStringUTFChars(dirPath, nativeDirPath);

    if (!check)
        return  JNI_FALSE;
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_io_nemiron_fileencryptor_utils_Cipher_encryptFile(JNIEnv *env, jobject, jstring filePath,
                                                 jstring dirPath, jboolean isRemove) {

    const char *nativeFilePath = env->GetStringUTFChars(filePath, JNI_FALSE);
    const char *nativeDirPath = env->GetStringUTFChars(dirPath, JNI_FALSE);

    bool check = encryptDecryptFile(nativeFilePath, nativeDirPath, 1, isRemove);

    env->ReleaseStringUTFChars(filePath, nativeFilePath);
    env->ReleaseStringUTFChars(dirPath, nativeDirPath);

    if (!check)
        return JNI_FALSE;
    return JNI_TRUE;
}