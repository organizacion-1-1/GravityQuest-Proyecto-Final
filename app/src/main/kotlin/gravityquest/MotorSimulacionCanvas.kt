package gravityquest
import gravityquest.models.Dificultad; import javafx.animation.AnimationTimer; import org.example.Juego.*; import kotlin.math.min
typealias IAnimationTimer = AnimationTimer; typealias ControladorSimulacion = MotorSimulacionCanvas
internal typealias ctorCalculadoraMotor = CalculadoraFisica; internal typealias ctorObjetoSimulado = ObjetoSimulado; internal typealias ctorRenderizadorCanvas = RenderizadorCanvas

internal class RelojAnimacionMotor(
    private val alTick: (IDouble) -> IUnit
) : AnimationTimer() {
    private var ultimoNs: ILong = 0L

    override fun handle(ahoraNs: ILong) {
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

    companion object {
        internal fun callbackObjetoVacio(obj: ObjetoSimulado) {
            if (obj.alturaInicial < 0.0) obj.reiniciar()
        }

        internal fun callbackSimpleVacio() {
            PhysicsConstants.esGravedadValida()
        }
    }
}

internal typealias ctorRelojMotor = RelojAnimacionMotor

interface IMotorCicloVida {
    val objetoSimulado: ObjetoSimulado
    val enEjecucion: IBoolean
    val escenarioActual: TipoEscenario
    fun iniciar()
    fun pausar()
    fun detener()
}

interface IMotorSimulacionBase : IMotorCicloVida {
    fun configurarEscenarioPorDificultad(dificultad: Dificultad)
    fun reiniciar(
        nuevaAltura: IDouble = objetoSimulado.alturaInicial,
        nuevaVelocidad: IDouble = objetoSimulado.velocidadInicial,
        nuevaDireccion: DireccionMovimiento = objetoSimulado.direccion
    )
}

interface IMotorSimulacionCanvas : IMotorSimulacionBase {
    fun vincularCanvas(nuevoCanvas: ICanvas)
    fun configurarCondiciones(
        alturaInicial: IDouble,
        velocidadInicial: IDouble = 0.0,
        direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE
    )
    fun avanzarFrame(deltaSegundos: IDouble = PhysicsConstants.DELTA_TIEMPO_FRAME)
    fun renderizarEstadoActual()
}

open class BaseMotorSimulacion(
    alturaInicial: IDouble,
    velocidadInicial: IDouble = 0.0,
    direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE,
    protected val calculadora: ICalculadoraFisica = ctorCalculadoraMotor()
) : IMotorSimulacionBase {

    override var objetoSimulado: ObjetoSimulado =
        crearObjeto(alturaInicial, velocidadInicial, direccion to calculadora)
        protected set

    override var enEjecucion: IBoolean = false
        protected set

    override var escenarioActual: TipoEscenario = TipoEscenario.CIELO_DESPEJADO
        protected set

    protected var timerLoop: IAnimationTimer? = null

    override fun configurarEscenarioPorDificultad(dificultad: Dificultad) {
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
        nuevaAltura: IDouble,
        nuevaVelocidad: IDouble,
        nuevaDireccion: DireccionMovimiento
    ) {
        pausar()
        objetoSimulado = crearObjeto(nuevaAltura, nuevaVelocidad, nuevaDireccion to calculadora)
    }

    private fun crearObjeto(altura: IDouble, velocidad: IDouble, par: IParDirCalc): ObjetoSimulado {
        return ctorObjetoSimulado(
            alturaInicial = altura,
            velocidadInicial = velocidad,
            direccion = par.first,
            calculadora = par.second
        )
    }
}

class MotorSimulacionCanvas(
    alturaInicial: IDouble = 20.0,
    velocidadInicial: IDouble = 0.0,
    direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE,
    canvas: ICanvas? = null,
    calculadora: ICalculadoraFisica = ctorCalculadoraMotor(),
    limitarDeltaMaximo: IBoolean = false,
    alActualizar: (ObjetoSimulado) -> IUnit = RelojAnimacionMotor::callbackObjetoVacio,
    alImpactar: () -> IUnit = RelojAnimacionMotor::callbackSimpleVacio
) : BaseMotorSimulacion(alturaInicial, velocidadInicial, direccion, calculadora), IMotorSimulacionCanvas {

    private val topeDelta: IDouble = if (limitarDeltaMaximo) 0.05 else 3600.0
    private val callbacks: IParCallbacksMotor = alActualizar to alImpactar
    private var renderizador: IRenderizadorSimulacion? = canvas?.let { ctorRenderizadorCanvas(it) }

    init {
        sincronizarEscenaRenderizador()
        timerLoop = ctorRelojMotor { dt -> avanzarFrame(dt) }
    }

    override fun vincularCanvas(nuevoCanvas: ICanvas) {
        renderizador = ctorRenderizadorCanvas(nuevoCanvas)
        sincronizarEscenaRenderizador()
        renderizarEstadoActual()
    }

    override fun configurarCondiciones(
        alturaInicial: IDouble,
        velocidadInicial: IDouble,
        direccion: DireccionMovimiento
    ) {
        super.reiniciar(alturaInicial, velocidadInicial, direccion)
        sincronizarEscenaRenderizador()
        renderizarEstadoActual()
    }

    override fun reiniciar(
        nuevaAltura: IDouble,
        nuevaVelocidad: IDouble,
        nuevaDireccion: DireccionMovimiento
    ) {
        super.reiniciar(nuevaAltura, nuevaVelocidad, nuevaDireccion)
        sincronizarEscenaRenderizador()
        renderizarEstadoActual()
    }

    override fun avanzarFrame(deltaSegundos: IDouble) {
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
