package pl.merskip.telephone_sim868.sim868

class Response(
    val status: Status,
    val entities: List<Entity>,
    val data: String?
) {

    enum class Status {
        OK,
        ERROR
    }

    operator fun get(command: String, index: Int? = null): Entity =
        if (index != null) getList(command)[index]
        else getList(command).single()

    fun has(command: String): Boolean = getList(command).isNotEmpty()

    fun getList(command: String): List<Entity> =
        entities.filter { it.command == command }

    val string: String get() = get(0).string

    val integer: Int get() = get(0).integer

    operator fun get(index: Int) = entities.single()[index]

    class Entity(
        val command: String,
        private val values: List<Value>,
        val data: String?
    ) {

        operator fun get(index: Int) = values[index]

        class Value(
            private val text: String
        ) {

            val string: String get() = text.removePrefix("\"").removeSuffix("\"")

            val integer: Int get() = text.toInt()
        }
    }
}