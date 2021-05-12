package pl.merskip.telephone_sim868.sim868

data class CallState(
    val index: Int,
    val direction: Int,
    val state: State,
    val mode: Mode,
    val isMultipart: Boolean,
    val phoneNumber: String,
    val type: Int,
    val alphaId: String?
) {

    enum class State(override val rawValue: Int): RawValuable<Int> {
        Active(0),
        Held(1),
        Dialing(2),
        Alerting(3),
        Incoming(4),
        Waiting(5),
        Disconnect(6)
    }

    enum class Mode(override val rawValue: Int): RawValuable<Int> {
        Voice(0),
        Data(1),
        Fax(2)
    }

    constructor(entity: Message.Entity) : this(
        index = entity[0].integer,
        direction = entity[1].integer,
        state = RawValuable.fromRawValue(entity[2].integer),
        mode = RawValuable.fromRawValue(entity[3].integer),
        isMultipart = entity[4].integer == 1,
        phoneNumber = entity[5].string,
        type = entity[6].integer,
        alphaId = entity[7].string.ifEmpty { null },
    )

    override fun toString(): String {
        return "CallState(" +
                "index=$index, " +
                "direction=$direction, " +
                "state=$state, " +
                "mode=$mode, " +
                "isMultipart=$isMultipart, " +
                "phoneNumber='$phoneNumber', type=$type, alphaId=$alphaId)"
    }
}
