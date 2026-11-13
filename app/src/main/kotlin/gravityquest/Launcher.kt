package gravityquest
import org.example.Juego.DemostracionConsola; internal typealias ctorLauncher = Launcher

interface ILauncher {
    val nombreLanzador: IString
    fun iniciar(args: Array<IString>): IBoolean
}

class Launcher(
    override val nombreLanzador: IString = "GravityQuestLauncher"
) : ILauncher {

    override fun iniciar(args: Array<IString>): IBoolean {
        require(nombreLanzador.isNotBlank())
        DemostracionConsola.ejecutarTodo(args)
        return nombreLanzador.isNotEmpty()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<IString>) {
            ctorLauncher().iniciar(args)
        }
    }
}
