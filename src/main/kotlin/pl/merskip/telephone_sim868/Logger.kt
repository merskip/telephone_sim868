package pl.merskip.telephone_sim868

import java.time.Instant

class Logger<T>(
    cls: Class<T>
) {

    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    fun verbose(message: String) = log(Level.VERBOSE, message)

    fun debug(message: String) = log(Level.DEBUG, message)

    fun info(message: String) = log(Level.INFO, message)

    fun warning(message: String) = log(Level.WARNING, message)

    fun error(message: String) = log(Level.ERROR, message)

    private fun log(level: Level, message: String) {
        println("${Instant.now()} [$level] $message")
    }
}