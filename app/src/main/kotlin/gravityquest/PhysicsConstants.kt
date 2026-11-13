package gravityquest
import gravityquest.models.*; import org.example.Juego.*
typealias IDouble = Double; typealias IInt = Int; typealias ILong = Long; typealias IBoolean = Boolean
typealias IString = String; typealias IUnit = Unit; typealias IAny = Any; typealias IList<T> = List<T>
typealias ISet<T> = Set<T>; typealias IMap<K, V> = Map<K, V>; typealias IMutableList<T> = MutableList<T>
typealias IMutableSet<T> = MutableSet<T>; typealias IMutableMap<K, V> = MutableMap<K, V>
typealias IDoubleArray = DoubleArray; typealias IIntArray = IntArray; typealias ILongArray = LongArray
typealias IPunto2D = Pair<Double, Double>; typealias IParTextos = Pair<String, String>
typealias IParAcciones = Pair<() -> Unit, () -> Unit>; typealias IParCallbacksMotor = Pair<(ObjetoSimulado) -> Unit, () -> Unit>
typealias IParDirCalc = Pair<DireccionMovimiento, ICalculadoraFisica>; typealias IParNivelFlag = Pair<Dificultad, Boolean>
typealias IParDominioApp = Pair<IRepositorioProblemas, ICalculadoraFisica>; typealias IParEstadoApp = Pair<IGestorProgreso, IRenderizadorVista>

interface IConstantesFisicas {
    val GRAVEDAD_TIERRA: IDouble
    val TOLERANCIA_PORCENTAJE: IDouble
    val TOLERANCIA_ABSOLUTA_MINIMA: IDouble
    val FPS_OBJETIVO: IInt
    val DELTA_TIEMPO_FRAME: IDouble
    fun esGravedadValida(): IBoolean
}

object PhysicsConstants : IConstantesFisicas {
    override val GRAVEDAD_TIERRA: IDouble = 9.8
    override val TOLERANCIA_PORCENTAJE: IDouble = 0.05
    override val TOLERANCIA_ABSOLUTA_MINIMA: IDouble = 0.01
    override val FPS_OBJETIVO: IInt = 60
    override val DELTA_TIEMPO_FRAME: IDouble = 1.0 / 60.0

    override fun esGravedadValida(): IBoolean {
        return GRAVEDAD_TIERRA > 0.0 && FPS_OBJETIVO > 0
    }
}

interface IConfigReglasJuego {
    val VIDAS_INICIALES: IInt
    val TOTAL_PROBLEMAS_POR_NIVEL: IInt
    val MINIMO_PARA_DESBLOQUEAR: IInt
    val PUNTOS_BASE_ACIERTO: IInt
    val BONO_POR_RACHA: IInt
    fun reglasValidas(): IBoolean
}

interface IConfigEntornoJuego : IConfigReglasJuego {
    val BONO_POR_VIDA_RESTANTE: IInt
    val ANCHO_VENTANA: IDouble
    val ALTO_VENTANA: IDouble
    val RUTA_ESTILOS_CSS: IString
    fun entornoValido(): IBoolean
}

open class BaseGameConfig : IConfigReglasJuego {
    override val VIDAS_INICIALES: IInt = 3
    override val TOTAL_PROBLEMAS_POR_NIVEL: IInt = 20
    override val MINIMO_PARA_DESBLOQUEAR: IInt = 2
    override val PUNTOS_BASE_ACIERTO: IInt = 100
    override val BONO_POR_RACHA: IInt = 25

    override fun reglasValidas(): IBoolean {
        return VIDAS_INICIALES > 0 && TOTAL_PROBLEMAS_POR_NIVEL >= MINIMO_PARA_DESBLOQUEAR
    }
}

object GameConfig : BaseGameConfig(), IConfigEntornoJuego {
    override val BONO_POR_VIDA_RESTANTE: IInt = 150
    override val ANCHO_VENTANA: IDouble = 1024.0
    override val ALTO_VENTANA: IDouble = 720.0
    override val RUTA_ESTILOS_CSS: IString = "/assets/style.css"

    override fun entornoValido(): IBoolean {
        return ANCHO_VENTANA > 0.0 && ALTO_VENTANA > 0.0 && RUTA_ESTILOS_CSS.isNotEmpty()
    }
}
