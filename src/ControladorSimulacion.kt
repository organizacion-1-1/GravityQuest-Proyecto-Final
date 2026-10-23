package gravityquest

import javafx.animation.AnimationTimer
import gravityquest.models.Problema
import gravityquest.models.TipoProblema

/**
 * ============================================================================
 * GravityQuest - Controlador del Bucle de Simulación Cinemática (SRP & DRY)
 * ============================================================================
 *
 * Principio de Responsabilidad Única (SRP):
 * Desacopla y encapsula exclusivamente la sincronización temporal (60 FPS), el cálculo
 * de deltas de tiempo cinemáticos y la actualización del estado del [ObjetoSimulado].
 *
 * Principio Abierto/Cerrado (OCP) y Polimorfismo:
 * Delega la cinemática en la abstracción [IEstrategiaCalculo], permitiendo simular
 * caída libre, lanzamiento hacia abajo y tiro vertical hacia arriba sin bifurcaciones rígidas.
 *
 * Principio de Inversión de Dependencias (DIP):
 * Depende de [ICalculadoraFisica] e [IEstrategiaCalculo], notificando mediante lambdas
 * al renderizador gráfico desacoplado.
 */
class ControladorSimulacion(
    val objetoSimulado: ObjetoSimulado,
    val calculadora: ICalculadoraFisica = CalculadoraFisica(),
    var alturaMetros: Double = PhysicsConstants.ALTURA_DEFAULT,
    var velocidadInicial: Double = 0.0,
    var direccion: DireccionMovimiento = DireccionMovimiento.ABAJO,
    var estrategia: IEstrategiaCalculo = EstrategiaCaidaLibre,
    private val alActualizar: (ObjetoSimulado) -> Unit = {}
) {
    var timer: AnimationTimer? = null
        private set

    private var tiempoInicioNano: Long = 0L

    /** Indica si el bucle de animación física se encuentra actualmente activo. */
    var enCurso: Boolean = false
        private set

    init {
        configurarTimer()
    }

    /**
     * Configura el [AnimationTimer] para calcular el progreso del movimiento a 60 FPS
     * mediante la estrategia cinemática activa (OCP).
     */
    private fun configurarTimer() {
        timer = object : AnimationTimer() {
            override fun handle(now: Long) {
                if (tiempoInicioNano == 0L) {
                    tiempoInicioNano = now
                }

                val deltaSegundos = (now - tiempoInicioNano) / 1_000_000_000.0
                val tiempoTotal = estrategia.calcularTiempoTotal(calculadora, alturaMetros, velocidadInicial)

                if (deltaSegundos >= tiempoTotal) {
                    val posFinal = estrategia.posicionFinal(alturaMetros)
                    val vFinal = estrategia.calcularVelocidadFinal(calculadora, alturaMetros, velocidadInicial)
                    objetoSimulado.actualizar(posFinal, vFinal, tiempoTotal, haLlegadoAlSuelo = true)
                    alActualizar(objetoSimulado)
                    detener()
                } else {
                    val posY = estrategia.calcularPosicionEnTiempo(calculadora, alturaMetros, velocidadInicial, deltaSegundos)
                    val vY = estrategia.calcularVelocidadEnTiempo(calculadora, alturaMetros, velocidadInicial, deltaSegundos)
                    objetoSimulado.actualizar(posY, vY, deltaSegundos, haLlegadoAlSuelo = false)
                    alActualizar(objetoSimulado)
                }
            }
        }
    }

    /**
     * Configura las condiciones iniciales del problema en el controlador de simulación.
     */
    fun configurarCondiciones(
        nuevaAltura: Double,
        nuevaVelocidadInicial: Double = 0.0,
        nuevaDireccion: DireccionMovimiento = DireccionMovimiento.ABAJO,
        nuevaEstrategia: IEstrategiaCalculo? = null
    ) {
        this.alturaMetros = nuevaAltura
        this.velocidadInicial = nuevaVelocidadInicial
        this.direccion = nuevaDireccion
        this.estrategia = nuevaEstrategia ?: when (nuevaDireccion) {
            DireccionMovimiento.ARRIBA -> EstrategiaLanzamientoArriba
            DireccionMovimiento.ABAJO -> if (nuevaVelocidadInicial > 0.0) EstrategiaLanzamientoAbajo else EstrategiaCaidaLibre
        }
    }

    /**
     * Adapta y configura los parámetros de simulación directamente desde un [Problema] físico.
     */
    fun configurarParaProblema(problema: Problema) {
        val alturaEfectiva = if (problema.tipo == TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA) {
            problema.estrategiaCalculo.calcularAlturaReferencia(calculadora, problema.alturaMetros, problema.velocidadInicial)
        } else {
            problema.alturaMetros
        }

        configurarCondiciones(
            nuevaAltura = alturaEfectiva,
            nuevaVelocidadInicial = problema.velocidadInicial,
            nuevaDireccion = problema.direccionMovimiento,
            nuevaEstrategia = problema.estrategiaCalculo
        )
    }

    /**
     * Inicia o reinicia la simulación restableciendo el reloj de precisión y el estado físico.
     */
    fun iniciarOReiniciar(
        nuevaAltura: Double = this.alturaMetros,
        nuevaVelocidadInicial: Double = this.velocidadInicial,
        nuevaDireccion: DireccionMovimiento = this.direccion,
        nuevaEstrategia: IEstrategiaCalculo = this.estrategia
    ) {
        detener()
        this.alturaMetros = nuevaAltura
        this.velocidadInicial = nuevaVelocidadInicial
        this.direccion = nuevaDireccion
        this.estrategia = nuevaEstrategia
        tiempoInicioNano = 0L
        objetoSimulado.reiniciar()
        alActualizar(objetoSimulado)
        enCurso = true
        timer?.start()
    }

    /**
     * Detiene inmediatamente el temporizador de la simulación gráfica.
     */
    fun detener() {
        timer?.stop()
        enCurso = false
    }
}
