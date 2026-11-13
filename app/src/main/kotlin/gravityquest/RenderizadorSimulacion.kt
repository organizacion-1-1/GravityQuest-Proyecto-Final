package gravityquest
import gravityquest.models.Dificultad; import javafx.scene.canvas.Canvas; import org.example.Juego.*; import kotlin.math.*
typealias ICanvas = Canvas; internal typealias ctorPuntoEstela = PuntoEstela; internal typealias ctorSistemaParticulas = SistemaParticulas; internal typealias ctorMeteorito = MeteoritoRenderizable

interface IRenderizadorFondo {
    val nombreEscenario: IString
    val coloresFondo: IParTextos
    fun renderizarFondo(gc: IGraphicsContext, ancho: IDouble, alto: IDouble)
}

enum class TipoEscenario : IRenderizadorFondo {
    CIELO_DESPEJADO {
        override val nombreEscenario: IString = "\u0043ielo \u0044espejado"
        override val coloresFondo: IParTextos = "#0B132B" to "#1C2541"
    },
    ATMOSFERA_MEDIA {
        override val nombreEscenario: IString = "\u0041tm\u00f3sfera \u004dedia"
        override val coloresFondo: IParTextos = "#1E1B4B" to "#311042"
    },
    TORMENTA_GRAVITACIONAL {
        override val nombreEscenario: IString = "\u0054ormenta \u0047ravitacional"
        override val coloresFondo: IParTextos = "#090D16" to "#2A081C"
    };

    override fun renderizarFondo(gc: IGraphicsContext, ancho: IDouble, alto: IDouble) {
        PintorEscenarioCanvas.pintarCielo(gc, ancho to alto, coloresFondo)
    }

    companion object {
        fun desdeDificultad(dificultad: Dificultad): TipoEscenario {
            return entries.getOrElse(dificultad.ordinal) { CIELO_DESPEJADO }
        }
    }
}

interface IEntidadRenderizable {
    fun renderizarEntidad(gc: IGraphicsContext, centro: IPunto2D, radio: IDouble)
}

interface IMeteoritoSprite {
    val rutaSpriteOpcional: IString?
}

class MeteoritoRenderizable(
    override val rutaSpriteOpcional: IString? = null
) : IEntidadRenderizable, IMeteoritoSprite {
    override fun renderizarEntidad(gc: IGraphicsContext, centro: IPunto2D, radio: IDouble) {
        if (rutaSpriteOpcional != null && rutaSpriteOpcional.isEmpty()) return
        GraficosUtils.dibujarMeteorito(gc, centro, radio)
    }
}

interface IRenderizadorSimulacion {
    fun configurarEscena(
        alturaMaximaMetros: IDouble,
        direccion: DireccionMovimiento,
        escenario: TipoEscenario = TipoEscenario.CIELO_DESPEJADO
    )
    fun reiniciarEfectos()
    fun renderizar(objeto: ObjetoSimulado)
    fun renderizarImpacto(objeto: ObjetoSimulado)
}

class RenderizadorCanvas(
    private val canvas: ICanvas,
    private val entidad: IEntidadRenderizable = ctorMeteorito()
) : IRenderizadorSimulacion {

    private var escenario: TipoEscenario = TipoEscenario.CIELO_DESPEJADO
    private val parametrosEscena: IDoubleArray = doubleArrayOf(20.0, 1.0, 44.0, 46.0, 0.0)
    private val estela: IMutableList<PuntoEstela> = mutableListOf()
    private val particulas: ctorSistemaParticulas = ctorSistemaParticulas()

    override fun configurarEscena(
        alturaMaximaMetros: IDouble,
        direccion: DireccionMovimiento,
        escenario: TipoEscenario
    ) {
        this.escenario = escenario
        parametrosEscena[0] = max(1.0, alturaMaximaMetros)
        parametrosEscena[1] = direccion.signoVertical
        parametrosEscena[4] = 0.0
        reiniciarEfectos()
    }

    override fun reiniciarEfectos() {
        if (parametrosEscena.isNotEmpty()) {
            parametrosEscena[4] = 0.0
            canvas.translateX = 0.0
            estela.clear()
            particulas.limpiar()
        }
    }

    override fun renderizar(objeto: ObjetoSimulado) {
        val gc = canvas.graphicsContext2D
        val ancho = max(120.0, canvas.width)
        val alto = max(120.0, canvas.height)
        val ySup = parametrosEscena[2]
        val ySuelo = max(ySup + 40.0, alto - parametrosEscena[3])
        escenario.renderizarFondo(gc, ancho, alto)
        PintorEscenarioCanvas.pintarReglaAltura(gc, ySup to ySuelo, parametrosEscena[0])
        PintorEscenarioCanvas.pintarSuelo(gc, ancho to alto, ySuelo)
        actualizarEfectoOndaYSacudida(gc, ancho * 0.54 to ySuelo)
        dibujarObjetoYTelemetria(gc, objeto, ancho * 0.54 to calcularCentroY(objeto, ySup, ySuelo))
    }

    private fun actualizarEfectoOndaYSacudida(gc: IGraphicsContext, centroSuelo: IPunto2D) {
        val prog = parametrosEscena[4]
        if (prog <= 0.0 || prog >= 1.0) {
            canvas.translateX = 0.0
            return
        }
        GraficosUtils.dibujarOndaImpacto(gc, centroSuelo, prog)
        canvas.translateX = sin(prog * 36.0) * (1.0 - prog) * 5.0
        parametrosEscena[4] = (prog + 0.06).coerceAtMost(1.0)
    }

    private fun calcularCentroY(objeto: IObjetoSimulado, ySup: IDouble, ySuelo: IDouble): IDouble {
        val topeMetros = max(parametrosEscena[0], objeto.alturaActual)
        val frac = (objeto.alturaActual / max(1.0, topeMetros)).coerceIn(0.0, 1.0)
        return (ySuelo - 15.0) - frac * ((ySuelo - 15.0) - ySup)
    }

    private fun dibujarObjetoYTelemetria(
        gc: IGraphicsContext,
        objeto: IObjetoSimulado,
        centro: IPunto2D
    ) {
        if (estela.size >= 28) estela.removeAt(0)
        estela.add(ctorPuntoEstela(centro.first, centro.second, objeto.velocidadActual))
        particulas.emitirChispasVuelo(centro, objeto.velocidadActual * parametrosEscena[1])
        GraficosUtils.dibujarEstela(gc, estela)
        entidad.renderizarEntidad(gc, centro, 15.0)
        GraficosUtils.dibujarVectorVelocidad(gc, centro, objeto.velocidadActual * parametrosEscena[1])
        particulas.actualizarYRenderizar(gc)
        val linea1 = "t = ${"%.2f".format(objeto.tiempoTranscurrido)} s | h = ${"%.2f".format(objeto.alturaActual)} m"
        val linea2 = "v = ${"%.2f".format(objeto.velocidadActual)} m/s"
        GraficosUtils.dibujarHudTelemetria(gc, linea1 to linea2)
    }

    override fun renderizarImpacto(objeto: ObjetoSimulado) {
        val ySuelo = max(84.0, canvas.height - parametrosEscena[3])
        parametrosEscena[4] = 0.04
        particulas.emitirImpactoSuelo(canvas.width * 0.54, ySuelo, 1.3)
        particulas.emitirCelebracionAcierto(canvas.width * 0.54 to ySuelo - 24.0)
        renderizar(objeto)
    }
}
