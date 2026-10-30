package gravityquest

import gravityquest.models.Problema
import gravityquest.models.TipoProblema
import javafx.animation.AnimationTimer

/**
 * ============================================================================
 * GravityQuest - Motor de Simulación y Ciclo de Animación Desacoplado
 * (Issue #21 - Milestone 4: Optimización, SOLID, POO y DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - Encapsula exclusivamente el manejo del [AnimationTimer], el cálculo de
 *      tiempo real mediante Delta Time ($\Delta t = \text{now} - \text{lastNanoTime}$),
 *      la actualización cinemática de [ObjetoSimulado] y la validación de colisión
 *      con los límites físicos (suelo / altura máxima).
 *    - Libera por completo a [VistaJuego] de cálculos temporales o físicos en el
 *      hilo de interfaz gráfica.
 *
 * 2. Gestión Fina de Recursos y Memoria (Cero Asignaciones en `handle`):
 *    - Reutiliza la misma instancia de [ObjetoSimulado] y cachea los parámetros
 *      cinemáticos (`tiempoTotalCacheado`, `velocidadFinalCacheada`) al configurar
 *      el ejercicio, evitando instanciar objetos de corta vida dentro de `handle(now: Long)`
 *      para minimizar la presión sobre el Garbage Collector (GC).
 *    - Implementa [IRecursoLiberable] para garantizar que `stop()` se invoque
 *      explícitamente al cambiar de escena en [NavegadorEscenas].
 *
 * 3. Independencia de Tasa de Refresco ($\Delta t$ Real):
 *    - Calcula el paso temporal real entre pulsos ($\Delta t = (\text{now} - \text{lastNanoTime}) / 10^9$),
 *      asegurando que la simulación corra a velocidad física constante sin importar
 *      si el monitor opera a 60Hz, 120Hz, 144Hz o sufre caídas momentáneas de FPS.
 */

/**
 * Contrato para componentes y vistas que mantienen recursos activos (timers, listeners)
 * que deben liberarse explícitamente al cambiar de escena o cerrar la aplicación (SRP / DIP).
 */
interface IRecursoLiberable {
    /** Detiene temporizadores activos y desvincula listeners para prevenir fugas de memoria. */
    fun liberarRecursos()
}

/**
 * Contrato de abstracción para el motor del bucle de simulación física (DIP & ISP).
 * La vista ([VistaJuego]) solo interactúa con este contrato para iniciar, pausar o detener
 * la animación sin conocer los detalles internos del [AnimationTimer].
 */
interface IMotorSimulacion : IRecursoLiberable {
    /** Instancia subyacente del [AnimationTimer] de JavaFX. */
    val timer: AnimationTimer?

    /** Indica si el bucle de animación está actualmente en ejecución. */
    val enCurso: Boolean

    /** Indica si la simulación se encuentra en estado de pausa. */
    val pausado: Boolean

    /** Marca de tiempo en nanosegundos del último pulso procesado (`lastNanoTime`). */
    val lastNanoTime: Long

    /** Último diferencial de tiempo ($\Delta t$) calculado en segundos entre dos frames consecutivos. */
    val ultimoDeltaSegundos: Double

    /** Tiempo físico total acumulado en segundos desde el inicio de la simulación actual. */
    val tiempoAcumuladoSegundos: Double

    /** Inicia o reanuda la ejecución del motor de animación (`play`). */
    fun play()

    /** Pausa temporalmente el motor de animación conservando el tiempo acumulado (`pause`). */
    fun pause()

    /** Detiene por completo el motor de animación y libera el temporizador activo (`stop`). */
    fun stop()

    /** Configura los parámetros cinemáticos a partir de un [Problema] de dominio. */
    fun configurarParaProblema(problema: Problema)

    /** Inicia o reinicia la simulación desde $t = 0$ con las condiciones especificadas. */
    fun iniciarOReiniciar(
        nuevaAltura: Double,
        nuevaVelocidadInicial: Double = 0.0,
        nuevaDireccion: DireccionMovimiento = DireccionMovimiento.ABAJO,
        nuevaEstrategia: IEstrategiaCalculo = EstrategiaCaidaLibre
    )

    /** Procesa manualmente un pulso del reloj en nanosegundos (útil para pruebas deterministas). */
    fun avanzarFrame(nowNano: Long)
}

/**
 * Motor especializado de simulación cinemática basado en [AnimationTimer] y Delta Time ($\Delta t$).
 */
open class MotorSimulacionCanvas(
    val objetoSimulado: ObjetoSimulado,
    val calculadora: ICalculadoraFisica = CalculadoraFisica(),
    alturaMetrosInicial: Double = PhysicsConstants.ALTURA_DEFAULT,
    velocidadInicialParam: Double = 0.0,
    direccionInicial: DireccionMovimiento = DireccionMovimiento.ABAJO,
    estrategiaInicial: IEstrategiaCalculo = EstrategiaCaidaLibre,
    val limitarDeltaMaximo: Boolean = true,
    val maxDeltaSegundos: Double = MAX_DELTA_SEGUNDOS_DEFAULT,
    private val alActualizar: (ObjetoSimulado) -> Unit = {}
) : IMotorSimulacion {

    companion object {
        /** Factor de conversión de nanosegundos a segundos ($10^9$ ns = 1 s). */
        const val NANOS_POR_SEGUNDO: Double = 1_000_000_000.0

        /**
         * Delta máximo permitido por frame (100 ms = 0.1 s, equivalente a 10 FPS mínimo)
         * para evitar que el objeto atraviese el suelo tras pausas del sistema operativo
         * o arrastre de la ventana.
         */
        const val MAX_DELTA_SEGUNDOS_DEFAULT: Double = 0.10
    }

    var alturaMetros: Double = alturaMetrosInicial.coerceAtLeast(0.0)
        protected set

    var velocidadInicial: Double = velocidadInicialParam
        protected set

    var direccion: DireccionMovimiento = direccionInicial
        protected set

    var estrategia: IEstrategiaCalculo = estrategiaInicial
        protected set

    override var timer: AnimationTimer? = null
        protected set

    override var enCurso: Boolean = false
        protected set

    override var pausado: Boolean = false
        protected set

    override var lastNanoTime: Long = 0L
        protected set

    override var ultimoDeltaSegundos: Double = 0.0
        protected set

    override var tiempoAcumuladoSegundos: Double = 0.0
        protected set

    // Valores cinemáticos precalculados fuera de `handle(now)` para evitar cálculos redundantes por frame
    private var tiempoTotalCacheado: Double = 0.0
    private var velocidadFinalCacheada: Double = 0.0
    private var posicionFinalCacheada: Double = 0.0

    init {
        recalcularCacheCinematica()
        inicializarTimer()
    }

    /**
     * Precalcula los límites cinemáticos de la simulación actual fuera del bucle de renderizado.
     */
    private fun recalcularCacheCinematica() {
        val alturaSegura = alturaMetros.coerceAtLeast(0.0)
        objetoSimulado.configurarAlturaMaxima(alturaSegura)
        tiempoTotalCacheado = try {
            estrategia.calcularTiempoTotal(calculadora, alturaSegura, velocidadInicial).coerceAtLeast(0.0)
        } catch (_: Exception) {
            0.0
        }
        velocidadFinalCacheada = try {
            estrategia.calcularVelocidadFinal(calculadora, alturaSegura, velocidadInicial)
        } catch (_: Exception) {
            0.0
        }
        posicionFinalCacheada = estrategia.posicionFinal(alturaSegura).coerceIn(0.0, alturaSegura)
    }

    /**
     * Instancia una única vez el [AnimationTimer] reutilizable.
     * Dentro de [AnimationTimer.handle] no se crean objetos temporales (Cero GC overhead).
     */
    private fun inicializarTimer() {
        timer = object : AnimationTimer() {
            override fun handle(now: Long) {
                avanzarFrame(now)
            }
        }
    }

    /**
     * Calcula $\Delta t = (\text{now} - \text{lastNanoTime}) / 10^9$ y actualiza el estado
     * cinemático en tiempo real sin instanciar objetos en memoria.
     */
    override fun avanzarFrame(nowNano: Long) {
        if (lastNanoTime == 0L) {
            lastNanoTime = nowNano
            ultimoDeltaSegundos = 0.0
            return
        }

        val deltaNanos = nowNano - lastNanoTime
        if (deltaNanos <= 0L) {
            lastNanoTime = nowNano
            ultimoDeltaSegundos = 0.0
            return
        }

        lastNanoTime = nowNano
        val deltaBruto = deltaNanos / NANOS_POR_SEGUNDO
        val deltaEfectivo = if (limitarDeltaMaximo) {
            deltaBruto.coerceIn(0.0, maxDeltaSegundos)
        } else {
            deltaBruto.coerceAtLeast(0.0)
        }

        ultimoDeltaSegundos = deltaEfectivo
        tiempoAcumuladoSegundos += deltaEfectivo

        val alturaSegura = alturaMetros.coerceAtLeast(0.0)
        val tiempoTotal = tiempoTotalCacheado

        if (tiempoTotal <= 0.0 || tiempoAcumuladoSegundos >= tiempoTotal) {
            finalizarEnSuelo()
            return
        }

        val posY = estrategia.calcularPosicionEnTiempo(
            calculadora,
            alturaSegura,
            velocidadInicial,
            tiempoAcumuladoSegundos
        )
        val vY = estrategia.calcularVelocidadEnTiempo(
            calculadora,
            alturaSegura,
            velocidadInicial,
            tiempoAcumuladoSegundos
        )

        // Validación estricta de colisión con el suelo para que el objeto jamás atraviese el límite inferior
        val haTocadoSuelo = if (direccion == DireccionMovimiento.ARRIBA) {
            tiempoAcumuladoSegundos > 0.0 && posY <= 0.0 && vY <= 0.0
        } else {
            posY >= alturaSegura
        }

        if (haTocadoSuelo) {
            finalizarEnSuelo()
        } else {
            val posAcotada = posY.coerceIn(0.0, alturaSegura)
            objetoSimulado.actualizar(
                posicion = posAcotada,
                velocidad = vY,
                tiempoTranscurrido = tiempoAcumuladoSegundos,
                haLlegadoAlSuelo = false,
                deltaSegundos = deltaEfectivo
            )
            alActualizar(objetoSimulado)
        }
    }

    /**
     * Fija el objeto exactamente sobre el límite del suelo, notifica el renderizado del
     * frame final y detiene inmediatamente el [AnimationTimer].
     */
    private fun finalizarEnSuelo() {
        objetoSimulado.actualizar(
            posicion = posicionFinalCacheada,
            velocidad = velocidadFinalCacheada,
            tiempoTranscurrido = tiempoTotalCacheado,
            haLlegadoAlSuelo = true,
            deltaSegundos = ultimoDeltaSegundos
        )
        alActualizar(objetoSimulado)
        stop()
    }

    /**
     * Configura las condiciones iniciales del ejercicio físico y actualiza la caché cinemática.
     */
    fun configurarCondiciones(
        nuevaAltura: Double,
        nuevaVelocidadInicial: Double = 0.0,
        nuevaDireccion: DireccionMovimiento = DireccionMovimiento.ABAJO,
        nuevaEstrategia: IEstrategiaCalculo? = null
    ) {
        this.alturaMetros = nuevaAltura.coerceAtLeast(0.0)
        this.velocidadInicial = nuevaVelocidadInicial
        this.direccion = nuevaDireccion
        this.estrategia = nuevaEstrategia ?: when (nuevaDireccion) {
            DireccionMovimiento.ARRIBA -> EstrategiaLanzamientoArriba
            DireccionMovimiento.ABAJO -> if (nuevaVelocidadInicial > 0.0) {
                EstrategiaLanzamientoAbajo
            } else {
                EstrategiaCaidaLibre
            }
        }
        recalcularCacheCinematica()
    }

    /**
     * Adapta y configura los parámetros de simulación directamente desde un [Problema] físico.
     */
    override fun configurarParaProblema(problema: Problema) {
        val alturaEfectiva = if (problema.tipo == TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA) {
            problema.estrategiaCalculo.calcularAlturaReferencia(
                calculadora,
                problema.alturaMetros,
                problema.velocidadInicial
            )
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
     * Inicia o reinicia la simulación desde $t = 0$ restableciendo el reloj Delta Time y el objeto.
     */
    override fun iniciarOReiniciar(
        nuevaAltura: Double,
        nuevaVelocidadInicial: Double,
        nuevaDireccion: DireccionMovimiento,
        nuevaEstrategia: IEstrategiaCalculo
    ) {
        stop()
        configurarCondiciones(nuevaAltura, nuevaVelocidadInicial, nuevaDireccion, nuevaEstrategia)
        lastNanoTime = 0L
        ultimoDeltaSegundos = 0.0
        tiempoAcumuladoSegundos = 0.0
        objetoSimulado.reiniciar()
        alActualizar(objetoSimulado)
        play()
    }

    /**
     * Inicia o reanuda el bucle de animación (`play`).
     * Al reanudar tras una pausa, reinicia `lastNanoTime = 0L` para evitar saltos bruscos de $\Delta t$.
     */
    override fun play() {
        if (objetoSimulado.enSuelo) {
            tiempoAcumuladoSegundos = 0.0
            objetoSimulado.reiniciar()
            alActualizar(objetoSimulado)
        }
        lastNanoTime = 0L
        pausado = false
        enCurso = true
        timer?.start()
    }

    /**
     * Pausa el bucle de animación conservando el estado actual (`pause`).
     */
    override fun pause() {
        timer?.stop()
        enCurso = false
        pausado = true
        lastNanoTime = 0L
    }

    /**
     * Detiene explícitamente el [AnimationTimer] y limpia marcas temporales (`stop`).
     */
    override fun stop() {
        timer?.stop()
        enCurso = false
        pausado = false
        lastNanoTime = 0L
    }

    /**
     * Alias semántico de [stop] para compatibilidad con controladores existentes.
     */
    fun detener() {
        stop()
    }

    override fun liberarRecursos() {
        stop()
    }
}

/**
 * Alias descriptivo para el ciclo de animación desacoplado (SRP - Issue #21).
 */
typealias CicloAnimacion = MotorSimulacionCanvas
