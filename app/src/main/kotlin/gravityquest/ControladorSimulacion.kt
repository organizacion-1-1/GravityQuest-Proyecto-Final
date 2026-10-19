package gravityquest

import javafx.animation.AnimationTimer

/**
 * ============================================================================
 * GravityQuest - Controlador del Bucle de Simulación Cinemática (SRP & DRY)
 * ============================================================================
 *
 * Principio de Responsabilidad Única (SRP):
 * Desacopla y encapsula exclusivamente la sincronización temporal (60 FPS), el cálculo
 * de deltas de tiempo cinemáticos y la actualización del estado del [ObjetoSimulado].
 * Evita la duplicación del bucle de animación presente anteriormente en múltiples vistas.
 *
 * Principio de Inversión de Dependencias (DIP):
 * Depende de la interfaz [ICalculadoraFisica] para computar los valores de física pura
 * y notifica mediante lambdas de actualización al renderizador gráfico desacoplado.
 */
class ControladorSimulacion(
    val objetoSimulado: ObjetoSimulado,
    val calculadora: ICalculadoraFisica = CalculadoraFisica(),
    var alturaMetros: Double = PhysicsConstants.ALTURA_DEFAULT,
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
     * Configura el [AnimationTimer] para calcular el progreso de caída libre a 60 FPS.
     */
    private fun configurarTimer() {
        timer = object : AnimationTimer() {
            override fun handle(now: Long) {
                if (tiempoInicioNano == 0L) {
                    tiempoInicioNano = now
                }

                val deltaSegundos = (now - tiempoInicioNano) / 1_000_000_000.0
                val tiempoTotalCaida = calculadora.calcularTiempo(alturaMetros)

                if (deltaSegundos >= tiempoTotalCaida) {
                    val vFinal = calculadora.calcularVelocidadFinal(alturaMetros)
                    objetoSimulado.actualizar(alturaMetros, vFinal, tiempoTotalCaida, haLlegadoAlSuelo = true)
                    alActualizar(objetoSimulado)
                    detener()
                } else {
                    val posY = calculadora.calcularPosicionCaida(deltaSegundos)
                    val vY = calculadora.g * deltaSegundos
                    objetoSimulado.actualizar(posY, vY, deltaSegundos, haLlegadoAlSuelo = false)
                    alActualizar(objetoSimulado)
                }
            }
        }
    }

    /**
     * Inicia o reinicia la simulación restableciendo el reloj de precisión y el estado físico.
     *
     * @param nuevaAltura Altura opcional para actualizar el alcance de la simulación.
     */
    fun iniciarOReiniciar(nuevaAltura: Double = this.alturaMetros) {
        detener()
        this.alturaMetros = nuevaAltura
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
