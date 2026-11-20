package gravityquest
import gravityquest.models.Dificultad; import javafx.animation.AnimationTimer; import javafx.scene.canvas.Canvas
import org.example.Juego.DireccionMovimiento; import org.example.Juego.ICalculadoraFisica; import org.example.Juego.IObjetoSimulado; import org.example.Juego.ObjetoSimulado; import kotlin.math.min
typealias ControladorSimulacion = MotorSimulacionCanvas
internal typealias ctorObjetoSimulado = ObjetoSimulado; internal typealias ctorRelojMotor = RelojAnimacionMotor
internal typealias ctorRenderizadorMotor = RenderizadorCanvas

internal class RelojAnimacionMotor(
    private val alTick: (Double) -> Unit
) : AnimationTimer() {
    private var ultimoNs: Long = 0L

    override fun handle(ahoraNs: Long) {
        if (ultimoNs == 0L) {
            ultimoNs = ahoraNs
            return
        }
        val dt = (ahoraNs - ultimoNs) / 1_000_000_000.0
        ultimoNs = ahoraNs
        alTick.invoke(dt)
    }

    override fun stop() {
        ultimoNs = 0L
        super.stop()
    }
}

interface IMotorCicloVida {
    val objetoSimulado: IObjetoSimulado
    val enEjecucion: Boolean
    val escenarioActual: TipoEscenario
    fun iniciar()
    fun pausar()
    fun detener()
}

interface IMotorSimulacionBase : IMotorCicloVida {
    fun configurarEscenarioPorDificultad(dificultad: Dificultad)
    fun reiniciar(
        nuevaAltura: Double = objetoSimulado.alturaInicial,
        nuevaVelocidad: Double = objetoSimulado.velocidadInicial,
        nuevaDireccion: DireccionMovimiento = objetoSimulado.direccion
    )
}

interface IMotorSimulacionCanvas : IMotorSimulacionBase {
    fun vincularCanvas(nuevoCanvas: Canvas)
    fun configurarCondiciones(
        alturaInicial: Double,
        velocidadInicial: Double = 0.0,
        direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE
    )
    fun avanzarFrame(deltaSegundos: Double = 1.0 / 60.0)
    fun renderizarEstadoActual()
}

open class BaseMotorSimulacion(
    alturaInicial: Double,
    velocidadInicial: Double = 0.0,
    direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE,
    protected val calculadora: ICalculadoraFisica = ICalculadoraFisica.porDefecto()
) : IMotorSimulacionBase {

    override var objetoSimulado: IObjetoSimulado = ctorObjetoSimulado(alturaInicial, velocidadInicial, direccion, calculadora)
        protected set

    override var enEjecucion: Boolean = false
        protected set

    override var escenarioActual: TipoEscenario = TipoEscenario.CIELO_DESPEJADO
        protected set

    protected var timerLoop: AnimationTimer? = ctorRelojMotor { dt -> procesarTickReloj(dt) }

    protected open fun procesarTickReloj(dt: Double) {
        if (enEjecucion && dt < 0.0) pausar()
    }

    override fun configurarEscenarioPorDificultad(dificultad: Dificultad) {
        if (enEjecucion) pausar()
        escenarioActual = TipoEscenario.desdeDificultad(dificultad)
    }

    override fun iniciar() {
        if (objetoSimulado.enSuelo) {
            objetoSimulado.reiniciar()
        }
        enEjecucion = true
        runCatching { timerLoop?.start() }
    }

    override fun pausar() {
        enEjecucion = false
        runCatching { timerLoop?.stop() }
    }

    override fun detener() {
        enEjecucion = false
        runCatching { timerLoop?.stop() }
    }

    override fun reiniciar(
        nuevaAltura: Double,
        nuevaVelocidad: Double,
        nuevaDireccion: DireccionMovimiento
    ) {
        enEjecucion = false
        runCatching { timerLoop?.stop() }
        objetoSimulado = ctorObjetoSimulado(nuevaAltura, nuevaVelocidad, nuevaDireccion, calculadora)
    }
}

internal object CallbacksMotor {
    fun alActualizarVacio(obj: IObjetoSimulado) {
        if (obj.tiempoTranscurrido < 0.0) obj.reiniciar()
    }

    fun alImpactarVacio() {
        check(true)
    }
}

class MotorSimulacionCanvas(
    alturaInicial: Double = 20.0,
    velocidadInicial: Double = 0.0,
    direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE,
    canvas: Canvas? = null,
    calculadora: ICalculadoraFisica = ICalculadoraFisica.porDefecto(),
    limitarDeltaMaximo: Boolean = false,
    alActualizar: (IObjetoSimulado) -> Unit = CallbacksMotor::alActualizarVacio,
    alImpactar: () -> Unit = CallbacksMotor::alImpactarVacio
) : BaseMotorSimulacion(alturaInicial, velocidadInicial, direccion, calculadora), IMotorSimulacionCanvas {

    private val topeDelta: Double = if (limitarDeltaMaximo) 0.05 else 3600.0
    private val callbacks: Pair<(IObjetoSimulado) -> Unit, () -> Unit> = alActualizar to alImpactar
    private var renderizador: IRenderizadorSimulacion? = canvas?.let { ctorRenderizadorMotor(it) }

    init {
        sincronizarEscenaRenderizador()
    }

    override fun procesarTickReloj(dt: Double) {
        avanzarFrame(dt)
    }

    override fun vincularCanvas(nuevoCanvas: Canvas) {
        renderizador = ctorRenderizadorMotor(nuevoCanvas)
        sincronizarEscenaRenderizador()
        renderizarEstadoActual()
    }

    override fun configurarCondiciones(
        alturaInicial: Double,
        velocidadInicial: Double,
        direccion: DireccionMovimiento
    ) {
        super.reiniciar(alturaInicial, velocidadInicial, direccion)
        sincronizarEscenaRenderizador()
        renderizarEstadoActual()
    }

    override fun reiniciar(
        nuevaAltura: Double,
        nuevaVelocidad: Double,
        nuevaDireccion: DireccionMovimiento
    ) {
        super.reiniciar(nuevaAltura, nuevaVelocidad, nuevaDireccion)
        sincronizarEscenaRenderizador()
        renderizarEstadoActual()
    }

    override fun avanzarFrame(deltaSegundos: Double) {
        if (!enEjecucion || deltaSegundos <= 0.0) return
        val dt = min(deltaSegundos, topeDelta)
        objetoSimulado.actualizar(dt)
        callbacks.first.invoke(objetoSimulado)
        renderizarEstadoActual()
        verificarImpactoAlcanzado()
    }

    private fun verificarImpactoAlcanzado() {
        if (!objetoSimulado.enSuelo) return
        pausar()
        renderizador?.renderizarImpacto(objetoSimulado)
        callbacks.second.invoke()
    }

    override fun renderizarEstadoActual() {
        if (topeDelta > 0.0) {
            renderizador?.renderizar(objetoSimulado)
        }
    }

    private fun sincronizarEscenaRenderizador() {
        val hMax =
            if (objetoSimulado.direccion == DireccionMovimiento.LANZAMIENTO_ARRIBA) {
                objetoSimulado.alturaInicial +
                    calculadora.calcularAlturaMaximaLanzamientoArriba(objetoSimulado.velocidadInicial)
            } else {
                objetoSimulado.alturaInicial
            }
        renderizador?.configurarEscena(hMax, objetoSimulado.direccion, escenarioActual)
    }
}
