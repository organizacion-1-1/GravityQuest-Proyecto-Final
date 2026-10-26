package gravityquest

import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas
import gravityquest.models.Problema
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.ReadOnlyIntegerWrapper
import java.util.Locale
import kotlin.math.max

/**
 * ============================================================================
 * GravityQuest - Sistema de Progreso y Puntuación del Jugador (POO & SOLID)
 * ============================================================================
 *
 * Arquitectura y Principios SOLID:
 * 1. Single Responsibility Principle (SRP):
 *    - [GestorProgreso] tiene la única responsabilidad de gestionar el progreso del jugador,
 *      calcular/acumular la puntuación según las reglas del dominio y aplicar las reglas de
 *      desbloqueo de niveles y problemas.
 *    - Las vistas ([VistaMenu], [VistaJuego], [MarcadorPuntosView]) quedan liberadas de la
 *      lógica de cálculo; únicamente consultan o reciben notificaciones reactivas.
 *
 * 2. Open/Closed Principle (OCP):
 *    - El sistema de desbloqueo está basado en la abstracción [IReglaDesbloqueo].
 *    - Incorporar nuevas reglas, logros o dificultades futuras solo requiere crear una
 *      nueva clase que implemente [IReglaDesbloqueo] sin modificar [GestorProgreso].
 *
 * 3. Dependency Inversion Principle (DIP) & Patrón Observer:
 *    - Define el contrato funcional [IPuntajeListener] y expone [ReadOnlyIntegerProperty]
 *      para que los componentes visuales ([MarcadorPuntosView]) se actualicen de forma
 *      reactiva y desacoplada cuando cambia el puntaje.
 *
 * 4. Encapsulamiento Fuerte:
 *    - Las colecciones internas y el estado del puntaje son estrictamente privados.
 *    - Solo se expone información mediante métodos de consulta inmutables y mutaciones controladas.
 */

/**
 * Contrato de observación (Observer / Listener) para notificar cambios en el puntaje
 * sin acoplar el modelo de dominio a componentes concretos de la interfaz (DIP).
 */
fun interface IPuntajeListener {
    /**
     * Se invoca automáticamente cada vez que el puntaje del jugador cambia.
     *
     * @param nuevoPuntaje Puntaje total acumulado tras la actualización.
     * @param delta Variación aplicada en la operación (+100, +50, -10, etc.).
     */
    fun alCambiarPuntaje(nuevoPuntaje: Int, delta: Int)
}

/**
 * Contrato de abstracción para reglas de desbloqueo de niveles y contenido (OCP).
 */
interface IReglaDesbloqueo {
    val dificultad: Dificultad

    /**
     * Evalúa si la dificultad correspondiente está desbloqueada dado el progreso actual.
     *
     * @param problemasCompletados Conjunto inmutable de identificadores de problemas completados.
     * @param repositorio Repositorio de problemas para verificar requisitos por nivel.
     * @return `true` si se cumplen los requisitos de desbloqueo, `false` en caso contrario.
     */
    fun estaDesbloqueada(problemasCompletados: Set<Int>, repositorio: IRepositorioProblemas): Boolean
}

/**
 * Regla de desbloqueo para niveles sin requisitos previos (ej. Fácil).
 */
class ReglaDesbloqueoInicial(
    override val dificultad: Dificultad = Dificultad.FACIL
) : IReglaDesbloqueo {
    override fun estaDesbloqueada(problemasCompletados: Set<Int>, repositorio: IRepositorioProblemas): Boolean = true
}

/**
 * Regla de desbloqueo secuencial (OCP):
 * Desbloquea una [dificultad] cuando se han completado los ejercicios de la [dificultadPrevia].
 *
 * @property dificultad Dificultad objetivo a desbloquear.
 * @property dificultadPrevia Dificultad que actúa como prerrequisito.
 * @property minimoRequerido Cantidad mínima de ejercicios a completar; si es `null`, requiere todos los de la previa.
 */
class ReglaDesbloqueoSecuencial(
    override val dificultad: Dificultad,
    val dificultadPrevia: Dificultad,
    val minimoRequerido: Int? = null
) : IReglaDesbloqueo {

    override fun estaDesbloqueada(problemasCompletados: Set<Int>, repositorio: IRepositorioProblemas): Boolean {
        val problemasPrevios = repositorio.obtenerPorDificultad(dificultadPrevia)
        if (problemasPrevios.isEmpty()) return true

        val completadosPrevios = problemasPrevios.count { problemasCompletados.contains(it.id) }
        val objetivo = minimoRequerido ?: problemasPrevios.size

        return completadosPrevios >= objetivo
    }
}

/**
 * Contrato de abstracción para el gestor de estado del progreso y puntuación del jugador (DIP).
 */
interface IGestorProgreso {
    /** Puntaje actual acumulado por el jugador (nunca menor que 0). */
    val puntajeActual: Int
        get() = 0

    /** Propiedad observable de solo lectura para enlace reactivo en JavaFX. */
    val puntajeProperty: ReadOnlyIntegerProperty

    fun esNivelDesbloqueado(dificultad: Dificultad): Boolean
    fun esProblemaCompletado(idProblema: Int): Boolean
    fun registrarVictoria(problema: Problema)
    fun registrarVictoria(problema: Problema, usoPista: Boolean, intentosPrevios: Int = 0) {
        registrarVictoria(problema)
    }
    fun registrarProblemaCompletado(idProblema: Int)
    fun cantidadProblemasCompletados(): Int
    fun problemasCompletadosIds(): Set<Int>
    fun cantidadCompletadosPorDificultad(dificultad: Dificultad): Int
    fun totalProblemasPorDificultad(dificultad: Dificultad): Int
    fun progresoPorDificultad(dificultad: Dificultad): Double
    fun reiniciarProgreso()
    fun desbloquearNivelManualmente(dificultad: Dificultad)

    // Operaciones del sistema de puntuación (Milestone 3 - Issue #17)
    fun registrarAcierto(alPrimerIntentoSinPista: Boolean = true): Int = puntajeActual
    fun registrarIntentoFallido(idProblema: Int? = null): Int = puntajeActual
    fun registrarPistaUsada(idProblema: Int) {}
    fun haUsadoPista(idProblema: Int): Boolean = false
    fun obtenerIntentosFallidos(idProblema: Int): Int = 0
    fun reiniciarEstadoIntentoProblema(idProblema: Int) {}
    fun obtenerPuntajeFormateado(): String = String.format(Locale.US, "PUNTAJE: %04d", puntajeActual)
    fun agregarListenerPuntaje(listener: IPuntajeListener) {}
    fun removerListenerPuntaje(listener: IPuntajeListener) {}
}

/**
 * Gestor en memoria del progreso del jugador, desbloqueo de niveles y sistema de puntuación en GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP) y Encapsulamiento Fuerte:
 * Concentra exclusivamente las reglas de negocio de puntaje (+100 al primer intento, +50 con pistas o
 * intentos previos, -10 por respuesta incorrecta sin bajar de 0) y notifica a los observadores registrados.
 */
class GestorProgreso(
    val repositorio: IRepositorioProblemas = GeneradorProblemas(),
    reglasPersonalizadas: List<IReglaDesbloqueo>? = null
) : IGestorProgreso {

    companion object {
        /** Puntos otorgados al acertar un problema en el primer intento y sin usar pistas. */
        const val PUNTOS_PRIMER_INTENTO: Int = 100

        /** Puntos otorgados al acertar un problema habiendo consultado pistas o con intentos fallidos previos. */
        const val PUNTOS_CON_PISTA_O_INTENTOS: Int = 50

        /** Penalización de puntos por cada respuesta incorrecta. */
        const val PENALIZACION_RESPUESTA_INCORRECTA: Int = 10

        /** Límite inferior del puntaje (nunca puede ser negativo). */
        const val PUNTAJE_MINIMO: Int = 0

        /** Formatea un valor numérico de puntaje al estándar estilizado de la interfaz. */
        fun formatearPuntaje(puntos: Int): String =
            String.format(Locale.US, "PUNTAJE: %04d", max(PUNTAJE_MINIMO, puntos))
    }

    // Encapsulamiento Fuerte: Estado privado protegido de progreso
    private val _problemasCompletados: MutableSet<Int> = mutableSetOf()
    private val _dificultadesDesbloqueadasManualmente: MutableSet<Dificultad> = mutableSetOf()

    // Encapsulamiento Fuerte: Estado privado del sistema de puntuación y seguimiento por problema
    private val _puntajeProperty: ReadOnlyIntegerWrapper = ReadOnlyIntegerWrapper(PUNTAJE_MINIMO)
    override val puntajeProperty: ReadOnlyIntegerProperty
        get() = _puntajeProperty.readOnlyProperty

    override val puntajeActual: Int
        get() = _puntajeProperty.get()

    private val _intentosFallidosPorProblema: MutableMap<Int, Int> = mutableMapOf()
    private val _pistasUsadasPorProblema: MutableSet<Int> = mutableSetOf()
    private val _listenersPuntaje: MutableList<IPuntajeListener> = mutableListOf()

    // Reglas de negocio configurables y extensibles (OCP)
    private val reglas: List<IReglaDesbloqueo> = reglasPersonalizadas ?: listOf(
        ReglaDesbloqueoInicial(Dificultad.FACIL),
        ReglaDesbloqueoSecuencial(Dificultad.MEDIO, Dificultad.FACIL),
        ReglaDesbloqueoSecuencial(Dificultad.DIFICIL, Dificultad.MEDIO)
    )

    /**
     * Consulta si un nivel de dificultad específico está desbloqueado para el jugador.
     */
    override fun esNivelDesbloqueado(dificultad: Dificultad): Boolean {
        if (_dificultadesDesbloqueadasManualmente.contains(dificultad)) {
            return true
        }
        val regla = reglas.firstOrNull { it.dificultad == dificultad } ?: return false
        return regla.estaDesbloqueada(_problemasCompletados.toSet(), repositorio)
    }

    /**
     * Consulta si un problema individual por su identificador ya fue completado.
     */
    override fun esProblemaCompletado(idProblema: Int): Boolean =
        _problemasCompletados.contains(idProblema)

    /**
     * Registra la resolución exitosa de un ejercicio físico evaluando automáticamente
     * si fue resuelto al primer intento (+100 pts) o con pistas/intentos previos (+50 pts).
     */
    override fun registrarVictoria(problema: Problema) {
        val tuvoIntentosPrevios = obtenerIntentosFallidos(problema.id) > 0
        val usoPista = haUsadoPista(problema.id)
        registrarVictoria(problema, usoPista = usoPista, intentosPrevios = if (tuvoIntentosPrevios) 1 else 0)
    }

    /**
     * Sobrecarga para registrar la victoria permitiendo especificar indicadores explícitos de ayuda o intentos.
     */
    override fun registrarVictoria(problema: Problema, usoPista: Boolean, intentosPrevios: Int) {
        val historialIntentos = obtenerIntentosFallidos(problema.id) + max(0, intentosPrevios)
        val historialPista = haUsadoPista(problema.id) || usoPista
        val esPrimerIntentoLimpio = (historialIntentos == 0) && !historialPista

        registrarAcierto(alPrimerIntentoSinPista = esPrimerIntentoLimpio)
        registrarProblemaCompletado(problema.id)
        reiniciarEstadoIntentoProblema(problema.id)
    }

    /**
     * Incrementa el puntaje según las reglas de negocio:
     * - Acierto al primer intento (sin pistas ni errores previos): +100 puntos.
     * - Acierto con pistas o intentos previos: +50 puntos.
     *
     * @param alPrimerIntentoSinPista `true` si no hubo intentos fallidos ni pistas, `false` en caso contrario.
     * @return El nuevo puntaje acumulado.
     */
    override fun registrarAcierto(alPrimerIntentoSinPista: Boolean): Int {
        val puntosGanados = if (alPrimerIntentoSinPista) {
            PUNTOS_PRIMER_INTENTO
        } else {
            PUNTOS_CON_PISTA_O_INTENTOS
        }
        aplicarCambioPuntaje(puntosGanados)
        return puntajeActual
    }

    /**
     * Penaliza una respuesta incorrecta restando 10 puntos (sin bajar nunca de 0)
     * y contabiliza el intento fallido para el problema indicado.
     *
     * @param idProblema Identificador opcional del problema en el que ocurrió el fallo.
     * @return El nuevo puntaje acumulado tras la penalización.
     */
    override fun registrarIntentoFallido(idProblema: Int?): Int {
        if (idProblema != null && idProblema > 0) {
            _intentosFallidosPorProblema[idProblema] = obtenerIntentosFallidos(idProblema) + 1
        }
        aplicarCambioPuntaje(-PENALIZACION_RESPUESTA_INCORRECTA)
        return puntajeActual
    }

    /**
     * Registra que el jugador ha consultado la pista para el problema especificado.
     */
    override fun registrarPistaUsada(idProblema: Int) {
        if (idProblema > 0) {
            _pistasUsadasPorProblema.add(idProblema)
        }
    }

    override fun haUsadoPista(idProblema: Int): Boolean =
        _pistasUsadasPorProblema.contains(idProblema)

    override fun obtenerIntentosFallidos(idProblema: Int): Int =
        _intentosFallidosPorProblema[idProblema] ?: 0

    override fun reiniciarEstadoIntentoProblema(idProblema: Int) {
        _intentosFallidosPorProblema.remove(idProblema)
        _pistasUsadasPorProblema.remove(idProblema)
    }

    override fun obtenerPuntajeFormateado(): String =
        formatearPuntaje(puntajeActual)

    override fun agregarListenerPuntaje(listener: IPuntajeListener) {
        if (!_listenersPuntaje.contains(listener)) {
            _listenersPuntaje.add(listener)
        }
    }

    override fun removerListenerPuntaje(listener: IPuntajeListener) {
        _listenersPuntaje.remove(listener)
    }

    private fun aplicarCambioPuntaje(deltaSolicitado: Int) {
        val puntajeAnterior = _puntajeProperty.get()
        val nuevoPuntaje = max(PUNTAJE_MINIMO, puntajeAnterior + deltaSolicitado)
        val deltaEfectivo = nuevoPuntaje - puntajeAnterior
        _puntajeProperty.set(nuevoPuntaje)
        notificarListenersPuntaje(nuevoPuntaje, deltaEfectivo)
    }

    private fun notificarListenersPuntaje(nuevoPuntaje: Int, delta: Int) {
        val copiaListeners = _listenersPuntaje.toList()
        for (listener in copiaListeners) {
            listener.alCambiarPuntaje(nuevoPuntaje, delta)
        }
    }

    /**
     * Registra un problema como completado por su identificador único.
     */
    override fun registrarProblemaCompletado(idProblema: Int) {
        require(idProblema > 0) { "El ID del problema debe ser positivo: $idProblema" }
        _problemasCompletados.add(idProblema)
    }

    override fun cantidadProblemasCompletados(): Int = _problemasCompletados.size

    override fun problemasCompletadosIds(): Set<Int> = _problemasCompletados.toSet()

    override fun cantidadCompletadosPorDificultad(dificultad: Dificultad): Int {
        val problemasDificultad = repositorio.obtenerPorDificultad(dificultad)
        return problemasDificultad.count { _problemasCompletados.contains(it.id) }
    }

    override fun totalProblemasPorDificultad(dificultad: Dificultad): Int =
        repositorio.cantidadPorDificultad(dificultad)

    override fun progresoPorDificultad(dificultad: Dificultad): Double {
        val total = totalProblemasPorDificultad(dificultad)
        if (total == 0) return 0.0
        return (cantidadCompletadosPorDificultad(dificultad).toDouble() / total).coerceIn(0.0, 1.0)
    }

    override fun reiniciarProgreso() {
        _problemasCompletados.clear()
        _dificultadesDesbloqueadasManualmente.clear()
        _intentosFallidosPorProblema.clear()
        _pistasUsadasPorProblema.clear()
        val puntajePrevio = _puntajeProperty.get()
        _puntajeProperty.set(PUNTAJE_MINIMO)
        if (puntajePrevio != PUNTAJE_MINIMO) {
            notificarListenersPuntaje(PUNTAJE_MINIMO, -puntajePrevio)
        }
    }

    override fun desbloquearNivelManualmente(dificultad: Dificultad) {
        _dificultadesDesbloqueadasManualmente.add(dificultad)
    }
}
