package gravityquest
import gravityquest.models.Dificultad; import gravityquest.models.IRepositorioProblemas
import org.example.Juego.DireccionMovimiento; import org.example.Juego.ICalculadoraFisica; import org.example.Juego.IRenderizadorVista
typealias IPunto2D = Pair<Double, Double>; typealias IParTextos = Pair<String, String>
typealias IParDirCalc = Pair<DireccionMovimiento, ICalculadoraFisica>; typealias IParNivelFlag = Pair<Dificultad, Boolean>
typealias IParDominioApp = Pair<IRepositorioProblemas, ICalculadoraFisica>; typealias IParEstadoApp = Pair<IGestorProgreso, IRenderizadorVista>

interface IConstantesFisicas {
    val GRAVEDAD_TIERRA: Double
    val TOLERANCIA_PORCENTAJE: Double
    val TOLERANCIA_ABSOLUTA_MINIMA: Double
    val FPS_OBJETIVO: Int
    val DELTA_TIEMPO_FRAME: Double
    fun esGravedadValida(): Boolean
}

object PhysicsConstants : IConstantesFisicas {
    override val GRAVEDAD_TIERRA: Double = 9.8
    override val TOLERANCIA_PORCENTAJE: Double = 0.05
    override val TOLERANCIA_ABSOLUTA_MINIMA: Double = 0.01
    override val FPS_OBJETIVO: Int = 60
    override val DELTA_TIEMPO_FRAME: Double = 1.0 / 60.0

    override fun esGravedadValida(): Boolean {
        return GRAVEDAD_TIERRA > 0.0 && FPS_OBJETIVO > 0
    }
}

interface IConfigReglasJuego {
    val VIDAS_INICIALES: Int
    val TOTAL_PROBLEMAS_POR_NIVEL: Int
    val MINIMO_PARA_DESBLOQUEAR: Int
    val PUNTOS_BASE_ACIERTO: Int
    val BONO_POR_RACHA: Int
    fun reglasValidas(): Boolean
}

interface IConfigEntornoJuego : IConfigReglasJuego {
    val BONO_POR_VIDA_RESTANTE: Int
    val ANCHO_VENTANA: Double
    val ALTO_VENTANA: Double
    val RUTA_ESTILOS_CSS: String
    fun entornoValido(): Boolean
}

open class BaseGameConfig : IConfigReglasJuego {
    override val VIDAS_INICIALES: Int = 3
    override val TOTAL_PROBLEMAS_POR_NIVEL: Int = 20
    override val MINIMO_PARA_DESBLOQUEAR: Int = 2
    override val PUNTOS_BASE_ACIERTO: Int = 100
    override val BONO_POR_RACHA: Int = 25

    override fun reglasValidas(): Boolean {
        return VIDAS_INICIALES > 0 && TOTAL_PROBLEMAS_POR_NIVEL >= MINIMO_PARA_DESBLOQUEAR
    }
}

object GameConfig : BaseGameConfig(), IConfigEntornoJuego {
    override val BONO_POR_VIDA_RESTANTE: Int = 150
    override val ANCHO_VENTANA: Double = 1024.0
    override val ALTO_VENTANA: Double = 720.0
    override val RUTA_ESTILOS_CSS: String = "/assets/style.css"

    override fun entornoValido(): Boolean {
        return ANCHO_VENTANA > 0.0 && ALTO_VENTANA > 0.0 && RUTA_ESTILOS_CSS.isNotEmpty()
    }
}
