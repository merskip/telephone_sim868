package pl.merskip.telephone_sim868

import java.time.Instant

class Logger<T>(
    private val cls: Class<T>
) {

    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    var onLogPrinted: (() -> Unit)? = null

    fun verbose(message: String) = log(Level.VERBOSE, message)

    fun debug(message: String) = log(Level.DEBUG, message)

    fun info(message: String) = log(Level.INFO, message)

    fun warning(message: String) = log(Level.WARNING, message)

    fun error(message: String) = log(Level.ERROR, message)

    private fun log(level: Level, message: String) {
        val semiSimpleClassName = cls.name.split('.').joinToString(".") {
            if (it == cls.simpleName) it
            else it[0].toString()
        }
        println("\r${Instant.now()} [$semiSimpleClassName] [$level] $message".colored(getColorByLevel(level)))
        onLogPrinted?.invoke()
    }

    private fun getColorByLevel(level: Level): Color {
        return when (level) {
            Level.VERBOSE -> Color.DarkGray
            Level.DEBUG -> Color.LightGray
            Level.INFO -> Color.Cyan
            Level.WARNING -> Color.Yellow
            Level.ERROR -> Color.Red
        }
    }
}