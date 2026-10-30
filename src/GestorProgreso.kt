package gravityquest

import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas
import gravityquest.models.Problema
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.SimpleIntegerProperty
import java.util.Locale
import kotlin.math.max

/**
 * ============================================================================
 * GravityQuest - Sistema de Progreso, Vidas y Puntuación (POO & SOLID)
 * ============================================================================
 *
 * Arquitectura y Principios SOLID:
 * 1. Single Responsibility Principle (SRP):
 *    - [GestorProgreso] gestiona el progreso del jugador, el desbloqueo en cadena
 *      de niveles (Fácil → Medio → Difícil), el cálculo de puntuación y coordina
 *      con [GestorVidas] el estado de las 3 vidas por intento.
 *    - Las vistas ([VistaMenu], [VistaJuego], [MarcadorPuntosView], [PanelVidasView])
 *      quedan liberadas de la lógica de negocio; solo consultan o reciben notificaciones.
 *
 * 2. Open/Closed Principle (OCP) y Encapsulamiento:
 *    - El sistema de desbloqueo está basado en la abstracción [IReglaDesbloqueo] y
 *      en la cadena polimórfica de [Dificultad.siguienteNivel].
 *    - Encapsula el estado de las vidas (`private var vidas: Int = 3`) y el nivel activo,
 *      exponiendo únicamente métodos mutadores seguros ([perderVida], [reiniciarVidas]).
 *
 * 3. Dependency Inversion Principle (DIP) & Patrón Observer:
 *    - Expone [IntegerProperty] ([vidasProperty]) y [ReadOnlyIntegerProperty] ([puntajeProperty]),
 *      además de los contratos [IPuntajeListener] e [IVidasListener], para que [PanelVidasView]
 *      y [MarcadorPuntosView] reaccionen automáticamente sin acoplamiento directo.
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
 * Desbloquea una [dificultad] cuando se han completado los ejercicios requeridos de la [dificultadPrevia].
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
 * Contrato de abstracción para el gestor de estado del progreso, vidas y puntuación del jugador (DIP).
 */
interface IGestorProgreso {
    /** Puntaje actual acumulado por el jugador (nunca menor que 0). */
    val puntajeActual: Int
        get() = 0

    /** Propiedad observable de solo lectura para enlace reactivo del puntaje en JavaFX. */
    val puntajeProperty: ReadOnlyIntegerProperty

    /** Modelo delegado o integrado de gestión de vidas (SRP / DIP). */
    val gestorVidas: IGestorVidas
        get() = GestorVidas()

    /** Cantidad de vidas restantes en el intento actual (inicia en 3). */
    val vidasActuales: Int
        get() = gestorVidas.vidasActuales

    /** Propiedad observable de JavaFX (`IntegerProperty`) para reaccionar a la pérdida/reinicio de vidas. */
    val vidasProperty: IntegerProperty
        get() = SimpleIntegerProperty(GestorVidas.MAX_VIDAS_DEFAULT)

    /** Nivel de dificultad actualmente activo en la sesión de juego. */
    val nivelActivo: Dificultad
        get() = Dificultad.FACIL

    fun esNivelDesbloqueado(dificultad: Dificultad): Boolean
    fun esProblemaCompletado(idProblema: Int): Boolean
    fun registrarVictoria(problema: Problema)
    fun registrarVictoria(problema: Problema, usoPista: Boolean, intentosPrevios: Int = 0) {
        registrarVictoria(problema)
    }
    fun registrarVictoria(dificultad: Dificultad) {
        desbloquearSiguienteNivel(dificultad)
    }
    fun registrarGameOver() {
        reiniciarProgreso()
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

    // Operaciones del sistema de vidas y progresión en cadena (Milestone 3 - Issue #18)
    fun perderVida(): Int = vidasActuales
    fun reiniciarVidas() {}
    fun esGameOver(): Boolean = vidasActuales <= 0
    fun tieneVidas(): Boolean = vidasActuales > 0
    fun cambiarNivelActivo(dificultad: Dificultad) {}
    fun obtenerSiguienteNivel(dificultadActual: Dificultad = nivelActivo): Dificultad? = dificultadActual.siguienteNivel
    fun desbloquearSiguienteNivel(dificultadActual: Dificultad = nivelActivo): Dificultad? {
        val siguiente = dificultadActual.siguienteNivel
        if (siguiente != null) {
            desbloquearNivelManualmente(siguiente)
        }
        return siguiente
    }
    fun agregarListenerVidas(listener: IVidasListener) {}
    fun removerListenerVidas(listener: IVidasListener) {}

    // Operaciones de estadísticas de partida y pantallas finales (Milestone 3 - Issue #20)
    val rachaActual: Int
        get() = 0

    val rachaMaxima: Int
        get() = 0

    val tiempoUltimoNivelSegundos: Double
        get() = 0.0

    val ultimosPuntosSumados: Int
        get() = 0

    fun registrarTiempoNivel(segundos: Double) {}

    fun obtenerEstadisticasPartida(
        dificultad: Dificultad = nivelActivo,
        tiempoUsadoSegundos: Double = tiempoUltimoNivelSegundos
    ): EstadisticasPartida = EstadisticasPartida(
        puntosTotales = puntajeActual,
        rachaMaxima = rachaMaxima,
        ejerciciosResueltos = cantidadProblemasCompletados(),
        tiempoUsadoSegundos = max(0.0, tiempoUsadoSegundos),
        vidasRestantes = vidasActuales,
        puntosSumadosNivel = ultimosPuntosSumados,
        bonoPorRacha = rachaActual * GestorProgreso.BONO_POR_RACHA,
        bonificacionVidas = vidasActuales * GestorProgreso.BONO_POR_VIDA_CONSERVADA,
        rachaActual = rachaActual,
        dificultad = dificultad
    )

    fun generarEstadisticasGameOver(
        dificultad: Dificultad = nivelActivo,
        tiempoUsadoSegundos: Double = tiempoUltimoNivelSegundos
    ): EstadisticasPartida = obtenerEstadisticasPartida(dificultad, tiempoUsadoSegundos).copy(
        vidasRestantes = 0,
        bonificacionVidas = 0
    )
}

/**
 * Gestor en memoria del progreso del jugador, desbloqueo en cadena de niveles,
 * sistema de vidas (3 vidas por intento), estadísticas de partida y sistema de puntuación en GravityQuest.
 */
class GestorProgreso(
    val repositorio: IRepositorioProblemas = GeneradorProblemas(),
    reglasPersonalizadas: List<IReglaDesbloqueo>? = null,
    override val gestorVidas: IGestorVidas = GestorVidas()
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

        /** Cantidad inicial de vidas por intento de nivel (Issue #18). */
        const val VIDAS_INICIALES: Int = GestorVidas.MAX_VIDAS_DEFAULT

        /** Bono calculado por cada acierto consecutivo en la racha activa (Issue #20). */
        const val BONO_POR_RACHA: Int = 25

        /** Bonificación calculada por cada vida conservada al completar el nivel (Issue #20). */
        const val BONO_POR_VIDA_CONSERVADA: Int = 50

        /** Formatea un valor numérico de puntaje al estándar estilizado de la interfaz. */
        fun formatearPuntaje(puntos: Int): String =
            String.format(Locale.US, "PUNTAJE: %04d", max(PUNTAJE_MINIMO, puntos))
    }

    // Encapsulamiento Fuerte: Estado privado protegido de vidas y nivel activo (Issue #18)
    private var vidas: Int = gestorVidas.vidasActuales
    private var _nivelActivo: Dificultad = Dificultad.FACIL

    override val nivelActivo: Dificultad
        get() = _nivelActivo

    override val vidasActuales: Int
        get() = vidas

    override val vidasProperty: IntegerProperty
        get() = gestorVidas.vidasProperty

    // Encapsulamiento Fuerte: Estado privado de rachas y estadísticas de partida (Issue #20)
    private var _rachaActual: Int = 0
    private var _rachaMaxima: Int = 0
    private var _tiempoUltimoNivelSegundos: Double = 0.0
    private var _ultimosPuntosSumados: Int = 0

    override val rachaActual: Int
        get() = _rachaActual

    override val rachaMaxima: Int
        get() = _rachaMaxima

    override val tiempoUltimoNivelSegundos: Double
        get() = _tiempoUltimoNivelSegundos

    override val ultimosPuntosSumados: Int
        get() = _ultimosPuntosSumados

    // Encapsulamiento Fuerte: Estado privado protegido de progreso y desbloqueo (Issues #15 & #18)
    private val _usaReglasPersonalizadas: Boolean = reglasPersonalizadas != null
    private var nivelesDesbloqueados: MutableSet<Dificultad> =
        if (_usaReglasPersonalizadas) mutableSetOf() else mutableSetOf(Dificultad.FACIL)
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

    init {
        // Sincroniza el campo privado `vidas` si `gestorVidas` cambia directamente
        gestorVidas.vidasProperty.addListener { _, _, nuevoValor ->
            vidas = nuevoValor.toInt()
        }
    }

    /**
     * Descuenta 1 vida al jugador de forma segura (`private var vidas` nunca menor a 0).
     *
     * @return Cantidad de vidas restantes tras la pérdida.
     */
    override fun perderVida(): Int {
        vidas = gestorVidas.perderVida()
        return vidas
    }

    /**
     * Restablece las vidas a su valor inicial de 3 vidas para un nuevo intento.
     */
    override fun reiniciarVidas() {
        gestorVidas.reiniciarVidas()
        vidas = gestorVidas.vidasActuales
    }

    /**
     * Indica si las vidas llegaron a 0 (Game Over / Nivel Fallido).
     */
    override fun esGameOver(): Boolean = vidas <= GestorVidas.MIN_VIDAS

    override fun tieneVidas(): Boolean = vidas > GestorVidas.MIN_VIDAS

    override fun cambiarNivelActivo(dificultad: Dificultad) {
        this._nivelActivo = dificultad
    }

    override fun obtenerSiguienteNivel(dificultadActual: Dificultad): Dificultad? =
        dificultadActual.siguienteNivel

    /**
     * Desbloquea inmediatamente el siguiente nivel en la cadena secuencial (FÁCIL → MEDIO → DIFÍCIL).
     *
     * @param dificultadActual Nivel que acaba de ser superado.
     * @return El siguiente nivel desbloqueado ([Dificultad.MEDIO] o [Dificultad.DIFICIL]), o `null` si ya era [Dificultad.DIFICIL].
     */
    override fun desbloquearSiguienteNivel(dificultadActual: Dificultad): Dificultad? {
        val siguiente = dificultadActual.siguienteNivel
        if (siguiente != null) {
            desbloquearNivelManualmente(siguiente)
        }
        return siguiente
    }

    override fun agregarListenerVidas(listener: IVidasListener) {
        gestorVidas.agregarListenerVidas(listener)
    }

    override fun removerListenerVidas(listener: IVidasListener) {
        gestorVidas.removerListenerVidas(listener)
    }

    /**
     * Consulta si un nivel de dificultad específico está desbloqueado para el jugador.
     */
    override fun esNivelDesbloqueado(dificultad: Dificultad): Boolean {
        if (nivelesDesbloqueados.contains(dificultad) || _dificultadesDesbloqueadasManualmente.contains(dificultad)) {
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
     * Registra la superación de un nivel de dificultad, desbloqueando el siguiente
     * eslabón en la secuencia Fácil → Medio → Difícil (Issues #15 & #18).
     */
    override fun registrarVictoria(dificultad: Dificultad) {
        nivelesDesbloqueados.add(dificultad)
        val siguiente = dificultad.siguienteNivel
        if (siguiente != null) {
            nivelesDesbloqueados.add(siguiente)
            _dificultadesDesbloqueadasManualmente.add(siguiente)
        }
    }

    /**
     * Registra el estado de Game Over cuando el jugador pierde sus 3 vidas:
     * - Reinicia por completo el progreso de desbloqueo (`MEDIO` y `DIFÍCIL` vuelven a quedar bloqueados).
     * - Restablece el nivel activo a [Dificultad.FACIL].
     * - Restaura las 3 vidas iniciales para el nuevo intento (Issue #18).
     */
    override fun registrarGameOver() {
        nivelesDesbloqueados = mutableSetOf(Dificultad.FACIL)
        _problemasCompletados.clear()
        _dificultadesDesbloqueadasManualmente.clear()
        _intentosFallidosPorProblema.clear()
        _pistasUsadasPorProblema.clear()
        _rachaActual = 0
        _nivelActivo = Dificultad.FACIL
        reiniciarVidas()
    }

    /**
     * Registra la resolución exitosa de un ejercicio físico evaluando automáticamente
     * si fue resuelto al primer intento (+100 pts) o con pistas/intentos previos (+50 pts),
     * y desbloquea en cadena el siguiente nivel de dificultad (Fácil → Medio → Difícil).
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

        // Progresión en cadena (Issue #18): Resolver Fácil desbloquea Medio, resolver Medio desbloquea Difícil
        if (!_usaReglasPersonalizadas) {
            registrarVictoria(problema.dificultad)
        }
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
        _ultimosPuntosSumados = puntosGanados
        _rachaActual += 1
        if (_rachaActual > _rachaMaxima) {
            _rachaMaxima = _rachaActual
        }
        aplicarCambioPuntaje(puntosGanados)
        return puntajeActual
    }

    /**
     * Penaliza una respuesta incorrecta restando 10 puntos (sin bajar nunca de 0),
     * reinicia la racha actual y contabiliza el intento fallido para el problema indicado.
     *
     * @param idProblema Identificador opcional del problema en el que ocurrió el fallo.
     * @return El nuevo puntaje acumulado tras la penalización.
     */
    override fun registrarIntentoFallido(idProblema: Int?): Int {
        if (idProblema != null && idProblema > 0) {
            _intentosFallidosPorProblema[idProblema] = obtenerIntentosFallidos(idProblema) + 1
        }
        _rachaActual = 0
        aplicarCambioPuntaje(-PENALIZACION_RESPUESTA_INCORRECTA)
        return puntajeActual
    }

    /**
     * Registra la duración en segundos empleada en el nivel o intento actual (Issue #20).
     */
    override fun registrarTiempoNivel(segundos: Double) {
        if (!segundos.isNaN() && !segundos.isInfinite()) {
            _tiempoUltimoNivelSegundos = max(0.0, segundos)
        }
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
        nivelesDesbloqueados = mutableSetOf(Dificultad.FACIL)
        _problemasCompletados.clear()
        _dificultadesDesbloqueadasManualmente.clear()
        _intentosFallidosPorProblema.clear()
        _pistasUsadasPorProblema.clear()
        _rachaActual = 0
        _rachaMaxima = 0
        _tiempoUltimoNivelSegundos = 0.0
        _ultimosPuntosSumados = 0
        reiniciarVidas()
        _nivelActivo = Dificultad.FACIL
        val puntajePrevio = _puntajeProperty.get()
        _puntajeProperty.set(PUNTAJE_MINIMO)
        if (puntajePrevio != PUNTAJE_MINIMO) {
            notificarListenersPuntaje(PUNTAJE_MINIMO, -puntajePrevio)
        }
    }

    override fun desbloquearNivelManualmente(dificultad: Dificultad) {
        nivelesDesbloqueados.add(dificultad)
        _dificultadesDesbloqueadasManualmente.add(dificultad)
    }
}
