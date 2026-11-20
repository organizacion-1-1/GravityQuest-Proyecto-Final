package gravityquest
import org.example.Juego.DemostracionConsola; internal typealias ctorLauncher = Launcher

interface ILauncher {
    val nombreLanzador: String
    fun iniciar(args: Array<String>): Boolean
}

class Launcher(
    override val nombreLanzador: String = "GravityQuestLauncher"
) : ILauncher {

    override fun iniciar(args: Array<String>): Boolean {
        require(nombreLanzador.isNotBlank())
        DemostracionConsola.ejecutarTodo(args)
        return nombreLanzador.isNotEmpty()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ctorLauncher().iniciar(args)
        }
    }
}
