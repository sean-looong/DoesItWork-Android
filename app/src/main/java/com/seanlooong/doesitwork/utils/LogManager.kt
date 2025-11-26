// Logger.kt
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

sealed class LogLevel(val level: Int, val tag: String) {
    object VERBOSE : LogLevel(0, "V")
    object DEBUG : LogLevel(1, "D")
    object INFO : LogLevel(2, "I")
    object WARNING : LogLevel(3, "W")
    object ERROR : LogLevel(4, "E")
}

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
)

data class LoggerConfig(
    val minLogLevel: LogLevel = LogLevel.DEBUG,
    val enableConsoleLogging: Boolean = true,
    val enableFileLogging: Boolean = true,
    val maxFileSize: Long = 5 * 1024 * 1024, // 5MB
    val maxFiles: Int = 10
)

class Logger internal constructor(
    private val config: LoggerConfig
) {

    private var applicationContext: Context? = null
    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var isInitialized = false

    internal fun initialize(context: Context) {
        this.applicationContext = context.applicationContext
        this.isInitialized = true

        if (config.enableFileLogging) {
            startLogWriter()
        }

        i("Logger", "Logger initialized with level: ${config.minLogLevel.tag}")
    }

    fun v(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.VERBOSE, tag, message, throwable)
    }

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, tag, message, throwable)
    }

    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.INFO, tag, message, throwable)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARNING, tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }

    fun e(tag: String, throwable: Throwable, message: String? = null) {
        log(LogLevel.ERROR, tag, message ?: throwable.message ?: "An error occurred", throwable)
    }

    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        if (level.level < config.minLogLevel.level) return

        val entry = LogEntry(System.currentTimeMillis(), level, tag, message, throwable)

        // 控制台输出
        if (config.enableConsoleLogging) {
            printToConsole(entry)
        }

        // 文件记录
        if (config.enableFileLogging && isInitialized) {
            logQueue.offer(entry)
        }
    }

    private fun printToConsole(entry: LogEntry) {
        val time = dateFormat.format(Date(entry.timestamp))
        val levelTag = entry.level.tag
        val logMessage = "$time ${entry.tag}/$levelTag: ${entry.message}"

        when (entry.level) {
            is LogLevel.ERROR -> android.util.Log.e(entry.tag, logMessage, entry.throwable)
            is LogLevel.WARNING -> android.util.Log.w(entry.tag, logMessage, entry.throwable)
            is LogLevel.INFO -> android.util.Log.i(entry.tag, logMessage)
            is LogLevel.DEBUG -> android.util.Log.d(entry.tag, logMessage)
            is LogLevel.VERBOSE -> android.util.Log.v(entry.tag, logMessage)
        }
    }

    private fun startLogWriter() {
        CoroutineScope(Dispatchers.IO).launch {
            while (isInitialized) {
                try {
                    val entry = logQueue.poll()
                    if (entry != null) {
                        writeToFile(entry)
                    } else {
                        kotlinx.coroutines.delay(100)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Logger", "Error writing log to file", e)
                }
            }
        }
    }

    private fun writeToFile(entry: LogEntry) {
        val context = applicationContext ?: return
        val logFile = getCurrentLogFile(context)
        val logMessage = formatLogEntry(entry)

        FileOutputStream(logFile, true).use { fos ->
            fos.write("$logMessage\n".toByteArray())
        }

        checkFileSizeAndRollover(context, logFile)
    }

    private fun formatLogEntry(entry: LogEntry): String {
        val time = dateFormat.format(Date(entry.timestamp))
        val levelTag = entry.level.tag
        var logMessage = "$time [${entry.tag}/$levelTag] ${entry.message}"

        entry.throwable?.let { throwable ->
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            logMessage += "\n${sw}"
        }

        return logMessage
    }

    private fun getCurrentLogFile(context: Context): File {
        val date = fileDateFormat.format(Date())
        val logDir = File(context.getExternalFilesDir(null), "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        return File(logDir, "app_$date.log")
    }

    private fun checkFileSizeAndRollover(context: Context, logFile: File) {
        if (logFile.length() > config.maxFileSize) {
            rolloverLogFiles(context, logFile)
        }
    }

    private fun rolloverLogFiles(context: Context, currentFile: File) {
        val logDir = currentFile.parentFile
        val files = logDir.listFiles { file ->
            file.name.startsWith("app_") && file.name.endsWith(".log")
        }?.sortedBy { it.name }

        files?.let {
            if (it.size >= config.maxFiles) {
                it.first().delete()
            }
        }

        val timestamp = System.currentTimeMillis()
        val newName = currentFile.name.replace(".log", "_$timestamp.log")
        currentFile.renameTo(File(logDir, newName))
    }

    fun getLogFiles(context: Context): List<File> {
        val logDir = File(context.getExternalFilesDir(null), "logs")
        return logDir.listFiles { file ->
            file.name.startsWith("app_") && file.name.endsWith(".log")
        }?.sortedByDescending { it.name } ?: emptyList()
    }

    fun clearLogs(context: Context) {
        val logDir = File(context.getExternalFilesDir(null), "logs")
        logDir.listFiles()?.forEach { it.delete() }
    }

    fun release() {
        isInitialized = false
        applicationContext = null
        logQueue.clear()
    }
}

object LoggerFactory {
    private var logger: Logger? = null

    fun initialize(context: Context, config: LoggerConfig? = null) {
        val finalConfig = config ?: createDefaultConfig()
        logger = Logger(finalConfig).apply {
            initialize(context)
        }
    }

    fun getLogger(): Logger {
        return logger ?: throw IllegalStateException("Logger must be initialized first. Call LoggerFactory.initialize() first.")
    }

    private fun createDefaultConfig(): LoggerConfig {
        return LoggerConfig(
            minLogLevel = if (isDebugBuild()) LogLevel.DEBUG else LogLevel.INFO,
            enableConsoleLogging = true,
            enableFileLogging = true,
            maxFileSize = 2 * 1024 * 1024,
            maxFiles = 5
        )
    }

    private fun isDebugBuild(): Boolean {
        return try {
            // 方法1：检查调试属性
            android.os.Debug.isDebuggerConnected()
        } catch (e: Exception) {
            // 方法2：检查是否有调试特征
            try {
                Class.forName("java.lang.Boolean").getDeclaredField("TRUE")
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun release() {
        logger?.release()
        logger = null
    }

    fun isInitialized(): Boolean {
        return logger != null
    }
}

class ComposeLogger(private val tag: String) {

    fun v(message: String, throwable: Throwable? = null) {
        LoggerFactory.getLogger().v(tag, message, throwable)
    }

    fun d(message: String, throwable: Throwable? = null) {
        LoggerFactory.getLogger().d(tag, message, throwable)
    }

    fun i(message: String, throwable: Throwable? = null) {
        LoggerFactory.getLogger().i(tag, message, throwable)
    }

    fun w(message: String, throwable: Throwable? = null) {
        LoggerFactory.getLogger().w(tag, message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        LoggerFactory.getLogger().e(tag, message, throwable)
    }

    fun e(throwable: Throwable, message: String? = null) {
        LoggerFactory.getLogger().e(tag, throwable, message)
    }
}

@Composable
fun rememberLogger(tag: String): ComposeLogger {
    return remember(tag) {
        ComposeLogger(tag)
    }
}

@Composable
fun LogLifecycle(
    tag: String,
    message: String,
    level: LogLevel = LogLevel.DEBUG
) {
    val logger = rememberLogger(tag)

    LaunchedEffect(Unit) {
        when (level) {
            is LogLevel.VERBOSE -> logger.v("Lifecycle: $message")
            is LogLevel.DEBUG -> logger.d("Lifecycle: $message")
            is LogLevel.INFO -> logger.i("Lifecycle: $message")
            is LogLevel.WARNING -> logger.w("Lifecycle: $message")
            is LogLevel.ERROR -> logger.e("Lifecycle: $message")
        }
    }
}

@Composable
fun LogRecomposition(tag: String) {
    val logger = rememberLogger(tag)
    var recompositionCount by remember { mutableStateOf(0) }

    recompositionCount++

    LaunchedEffect(recompositionCount) {
        if (recompositionCount > 1) {
            logger.d("Recomposed $recompositionCount times")
        }
    }
}