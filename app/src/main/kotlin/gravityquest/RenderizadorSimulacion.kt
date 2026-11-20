package gravityquest
import gravityquest.models.Dificultad; import javafx.scene.canvas.Canvas; import javafx.scene.canvas.GraphicsContext
import org.example.Juego.DireccionMovimiento; import org.example.Juego.IObjetoSimulado; import org.example.Juego.ObjetoSimulado; import kotlin.math.max; import kotlin.math.sin
internal typealias ctorPuntoEstela = PuntoEstela; internal typealias ctorSistemaParticulas = SistemaParticulas
internal typealias ctorMeteorito = MeteoritoRenderizable; internal typealias ctorEfectosCanvas = GestorEfectosCanvas

interface IRenderizadorFondo {
    val nombreEscenario: String
    val coloresFondo: IParTextos
    fun renderizarFondo(gc: GraphicsContext, ancho: Double, alto: Double)
    fun renderizarEscenaCompleta(gc: GraphicsContext, dim: IPunto2D, datosAltura: IPunto2D)
}

enum class TipoEscenario : IRenderizadorFondo {
    CIELO_DESPEJADO {
        override val nombreEscenario: String = "\u0043ielo \u0044espejado"
        override val coloresFondo: IParTextos = "#0B132B" to "#1C2541"
    },
    ATMOSFERA_MEDIA {
        override val nombreEscenario: String = "\u0041tm\u00f3sfera \u004dedia"
        override val coloresFondo: IParTextos = "#1E1B4B" to "#311042"
    },
    TORMENTA_GRAVITACIONAL {
        override val nombreEscenario: String = "\u0054ormenta \u0047ravitacional"
        override val coloresFondo: IParTextos = "#090D16" to "#2A081C"
    };

    override fun renderizarFondo(gc: GraphicsContext, ancho: Double, alto: Double) {
        PintorEscenarioCanvas.pintarCielo(gc, ancho to alto, coloresFondo)
    }

    override fun renderizarEscenaCompleta(gc: GraphicsContext, dim: IPunto2D, datosAltura: IPunto2D) {
        val ySup = 44.0
        val ySuelo = datosAltura.first
        PintorEscenarioCanvas.pintarCielo(gc, dim, coloresFondo)
        PintorEscenarioCanvas.pintarReglaAltura(gc, ySup to ySuelo, datosAltura.second)
        PintorEscenarioCanvas.pintarSuelo(gc, dim, ySuelo)
    }

    companion object {
        fun desdeDificultad(dificultad: Dificultad): TipoEscenario {
            return entries.getOrElse(dificultad.ordinal) { CIELO_DESPEJADO }
        }
    }
}

interface IEntidadRenderizable {
    fun renderizarEntidad(gc: GraphicsContext, centro: IPunto2D, radio: Double)

    companion object {
        fun porDefecto(): IEntidadRenderizable = ctorMeteorito()
    }
}

interface IMeteoritoSprite {
    val rutaSpriteOpcional: String?
}

class MeteoritoRenderizable(
    override val rutaSpriteOpcional: String? = null
) : IEntidadRenderizable, IMeteoritoSprite {
    override fun renderizarEntidad(gc: GraphicsContext, centro: IPunto2D, radio: Double) {
        if (rutaSpriteOpcional != null && rutaSpriteOpcional.isEmpty()) return
        GraficosUtils.dibujarMeteorito(gc, centro, radio)
    }
}

internal class GestorEfectosCanvas {
    private val estela: MutableList<ctorPuntoEstela> = mutableListOf()
    private val particulas: ctorSistemaParticulas = ctorSistemaParticulas()

    fun limpiar() {
        estela.clear()
        particulas.limpiar()
    }

    fun renderizarVuelo(gc: GraphicsContext, objeto: IObjetoSimulado, centroYSigno: DoubleArray) {
        val centro = centroYSigno[0] to centroYSigno[1]
        val vyConSigno = objeto.velocidadActual * centroYSigno[2]
        if (estela.size >= 28) estela.removeAt(0)
        estela.add(ctorPuntoEstela(centro.first, centro.second, objeto.velocidadActual))
        particulas.emitirChispasVuelo(centro, vyConSigno)
        GraficosUtils.dibujarEstela(gc, estela)
        GraficosUtils.dibujarVectorVelocidad(gc, centro, vyConSigno)
        particulas.actualizarYRenderizar(gc)
        val l1 = "t = ${"%.2f".format(objeto.tiempoTranscurrido)} s | h = ${"%.2f".format(objeto.alturaActual)} m"
        val l2 = "v = ${"%.2f".format(objeto.velocidadActual)} m/s"
        GraficosUtils.dibujarHudTelemetria(gc, l1 to l2)
    }

    fun emitirImpacto(xImpacto: Double, ySuelo: Double) {
        particulas.emitirImpactoSuelo(xImpacto, ySuelo, 1.3)
        particulas.emitirCelebracionAcierto(xImpacto to ySuelo - 24.0)
    }

    fun dibujarOnda(gc: GraphicsContext, xImpacto: Double, ySuelo: Double) {
        if (estela.isNotEmpty()) {
            GraficosUtils.dibujarOndaImpacto(gc, xImpacto to ySuelo, 0.35)
        }
    }
}

interface IRenderizadorSimulacion {
    fun configurarEscena(
        alturaMaximaMetros: Double,
        direccion: DireccionMovimiento,
        escenario: TipoEscenario = TipoEscenario.CIELO_DESPEJADO
    )
    fun reiniciarEfectos()
    fun renderizar(objeto: IObjetoSimulado)
    fun renderizarImpacto(objeto: IObjetoSimulado)
}

class RenderizadorCanvas(
    private val canvas: Canvas,
    private val entidad: IEntidadRenderizable = IEntidadRenderizable.porDefecto()
) : IRenderizadorSimulacion {

    private var escenario: TipoEscenario = TipoEscenario.CIELO_DESPEJADO
    private val parametrosEscena: DoubleArray = doubleArrayOf(20.0, 1.0, 44.0, 46.0, 0.0)
    private val efectos: ctorEfectosCanvas = ctorEfectosCanvas()

    override fun configurarEscena(
        alturaMaximaMetros: Double,
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
            efectos.limpiar()
        }
    }

    override fun renderizar(objeto: IObjetoSimulado) {
        val gc = canvas.graphicsContext2D
        val ancho = max(120.0, canvas.width)
        val alto = max(120.0, canvas.height)
        val ySup = parametrosEscena[2]
        val ySuelo = max(ySup + 40.0, alto - parametrosEscena[3])
        escenario.renderizarEscenaCompleta(gc, ancho to alto, ySuelo to parametrosEscena[0])
        actualizarSacudidaYOnda(gc, ancho * 0.54, ySuelo)
        val cy = calcularCentroY(objeto, ySup, ySuelo)
        efectos.renderizarVuelo(gc, objeto, doubleArrayOf(ancho * 0.54, cy, parametrosEscena[1]))
        entidad.renderizarEntidad(gc, ancho * 0.54 to cy, 15.0)
    }

    private fun actualizarSacudidaYOnda(gc: GraphicsContext, xCentro: Double, ySuelo: Double) {
        val prog = parametrosEscena[4]
        if (prog <= 0.0 || prog >= 1.0) {
            canvas.translateX = 0.0
            return
        }
        efectos.dibujarOnda(gc, xCentro, ySuelo)
        canvas.translateX = sin(prog * 36.0) * (1.0 - prog) * 5.0
        parametrosEscena[4] = (prog + 0.06).coerceAtMost(1.0)
    }

    private fun calcularCentroY(objeto: IObjetoSimulado, ySup: Double, ySuelo: Double): Double {
        val topeMetros = max(parametrosEscena[0], objeto.alturaActual)
        val frac = (objeto.alturaActual / max(1.0, topeMetros)).coerceIn(0.0, 1.0)
        return (ySuelo - 15.0) - frac * ((ySuelo - 15.0) - ySup)
    }

    override fun renderizarImpacto(objeto: IObjetoSimulado) {
        val ySuelo = max(84.0, canvas.height - parametrosEscena[3])
        parametrosEscena[4] = 0.04
        efectos.emitirImpacto(canvas.width * 0.54, ySuelo)
        renderizar(objeto)
    }
}
