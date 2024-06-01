package com.laizhenghuo.binary_execute

import android.content.Context
import com.laizhenghuo.binary_execute_common.ZipUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BinaryExecute {

    ///是否初始化
    private var initialized = false;

    ///Python路径：环境变量
    private var pythonPath: String? = null

    ///ffmpeg路径：环境变量
    private var ffmpegPath: String? = null

    ///二进制执行文件路径：环境变量
    private var binaryPath: String? = null

    ///二进制目录
    private var binDir: File? = null

    ///Linux环境变量，用于指定查找共享库除默认路径外的其他路径
    private var ENV_LD_LIBRARY_PATH: String? = null

    ///Linux环境变量，用于指定SSL证书路径
    private var ENV_SSL_CERT_FILE: String? = null

    ///Linux环境变量，用于指定Python执行路径
    private var ENV_PYTHON_HOME: String? = null


    private const val baseName = "binary-execute"
    private const val packagesRoot = "packages"
    private const val pythonBinName = "libpython.so"
    private const val pythonLibName = "libpython.zip.so"
    private const val pythonDirName = "python"
    private const val ffmpegDirName = "ffmpeg"
    private const val ffmpegBinName = "libffmpeg.so"
    private const val binaryName = "yt-dlp"
    private const val binaryDirName = binaryName
    private const val binaryBin = binaryName
    private const val pythonLibVersion = "pythonLibVersion"


    @JvmStatic
    fun getInstance() = this

    @Synchronized
    fun init(appContext: Context) {
        if (initialized) return
        val baseDir = File(appContext.noBackupFilesDir, baseName)
        if (!baseDir.exists()) baseDir.mkdir()
        val packagesDir = File(baseDir, packagesRoot)

        binDir = File(appContext.applicationInfo.nativeLibraryDir)

        pythonPath = File(binDir, pythonBinName).absolutePath

        ffmpegPath = File(binDir, ffmpegBinName).absolutePath

        // 三个都是目录
        // be like:data/data/com.laizhenghuo.binary_execute/noBackup/$baseName/python
        val pythonDir = File(packagesDir, pythonDirName)
        val ffmpegDir = File(packagesDir, ffmpegDirName)
        val binaryDir = File(packagesDir, binaryDirName)

        binaryPath = File(binaryDir, binaryBin).absolutePath

        println(binaryPath)

        ENV_LD_LIBRARY_PATH = "${pythonDir.absolutePath}/usr/lib:${ffmpegDir.absolutePath}/usr/lib"

        println(ENV_LD_LIBRARY_PATH)

        ENV_SSL_CERT_FILE = "${pythonDir.absolutePath}/usr/etc/tls/cert.pem"

        println(ENV_SSL_CERT_FILE)

        ENV_PYTHON_HOME = "${pythonDir.absolutePath}/usr"

        println(ENV_PYTHON_HOME)

        initPython(pythonDir)
        initBinary(appContext,binaryDir)

        initialized = true
    }

    private fun initPython(pythonDir:File) {
        val pythonLib = File(binDir, pythonLibName)
        println(pythonLib.absolutePath)
        if (!pythonDir.exists()) {
            FileUtils.deleteQuietly(pythonDir)
            pythonDir.mkdirs()
            try {
                ZipUtils.unzip(pythonLib, pythonDir)
            } catch (e: Exception) {
                println("initPython:$e")
                FileUtils.deleteQuietly(pythonDir)
            }
        }
    }

    private fun initBinary(appContext: Context, binaryDir:File) {
        if (!binaryDir.exists()) binaryDir.mkdirs()
        val binary = File(binaryDir, binaryBin)
        if (!binary.exists()) {
            try {
                val binaryFileId = R.raw.ytdlp
                val outputFile = File(binary.absolutePath)
                copyRawResourceToFile(appContext,binaryFileId,outputFile)
            } catch (e:Exception) {
                println(e)
            }
        }
    }

    //原始资源拷贝到文件
    private fun copyRawResourceToFile(context: Context, resourceId: Int, file: File) {
        val inputStream = context.resources.openRawResource(resourceId)
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var read = inputStream.read(buffer)
        while (read != -1) {
            outputStream.write(buffer, 0, read)
            read = inputStream.read(buffer)
        }
        outputStream.close()
        inputStream.close()
    }

    private fun assertInit() {
        check(initialized) { "instance not initialized" }
    }

    fun execute(){
        assertInit()
        val process: Process
        val outBuffer = StringBuffer() //stdout
        val errBuffer = StringBuffer() //stderr
        val command: MutableList<String?> = ArrayList()
        command.addAll(listOf(pythonPath!!, binaryPath!!))
        val processBuilder = ProcessBuilder(command)
        processBuilder.environment().apply {
            this["LD_LIBRARY_PATH"] = ENV_LD_LIBRARY_PATH
            this["SSL_CERT_FILE"] = ENV_SSL_CERT_FILE
            this["PATH"] = System.getenv("PATH")!! + ":" + binDir!!.absolutePath
            this["PYTHONHOME"] = ENV_PYTHON_HOME
            this["HOME"] = ENV_PYTHON_HOME
        }
        process = try {
            processBuilder.start()
        } catch (e: IOException) {
            throw Exception(e)
        }
        val outStream = process.inputStream
        val errStream = process.errorStream
        val stdOutProcessor = StreamProcessExtractor(outBuffer, outStream, null)
        val stdErrProcessor = StreamGobbler(errBuffer, errStream)
        try {
            stdOutProcessor.join()
            stdErrProcessor.join()
            process.waitFor()
        } catch (e: InterruptedException) {
            process.destroy()
            throw e
        }
        val out = outBuffer.toString()
        val err = errBuffer.toString()
        println(out)
        println(err)
    }
}