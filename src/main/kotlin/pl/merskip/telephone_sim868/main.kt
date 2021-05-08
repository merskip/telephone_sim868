package pl.merskip.telephone_sim868

fun main(args: Array<String>) {
    val sim = SIMCom(args[0])
    sim.command("AT")
    sim.dispose()
}