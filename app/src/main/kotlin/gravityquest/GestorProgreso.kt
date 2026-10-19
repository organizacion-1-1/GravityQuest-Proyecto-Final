package gravityquest

import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas
import gravityquest.models.Problema

/**
 * ============================================================================
 * GravityQuest - Sistema de Progreso del Jugador (POO & SOLID)
 * ============================================================================
 *
 * Arquitectura y Principios SOLID:
 * 1. Single Responsibility Principle (SRP):
 *    - [GestorProgreso] tiene la única responsabilidad de gestionar el progreso del jugador
 *      en memoria y aplicar las reglas de desbloqueo de niveles y problemas.
 *    - Las vistas ([VistaMenu], [VistaJuego]) quedan liberadas de la lógica de negocio;
 *      únicamente consultan o notifican el progreso.
 *
 * 2. Open/Closed Principle (OCP):
 *    - El sistema de desbloqueo está basado en la abstracción [IReglaDesbloqueo].
 *    - Incorporar nuevas reglas, logros o dificultades futuras solo requiere crear una
 *      nueva clase que implemente [IReglaDesbloqueo] sin modificar [GestorProgreso].
 *
 * 3. Encapsulamiento Fuerte:
 *    - La colección interna de problemas completados es estrictamente privada ([_problemasCompletados]).
 *    - Solo se expone información mediante métodos de consulta inmutables ([esNivelDesbloqueado],
 *      [esProblemaCompletado]) y mutaciones controladas ([registrarVictoria], [registrarProblemaCompletado]).
 */

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
 * Contrato de abstracción para el gestor de estado del progreso del jugador (DIP).
 */
interface IGestorProgreso {
    fun esNivelDesbloqueado(dificultad: Dificultad): Boolean
    fun esProblemaCompletado(idProblema: Int): Boolean
    fun registrarVictoria(problema: Problema)
    fun registrarProblemaCompletado(idProblema: Int)
    fun cantidadProblemasCompletados(): Int
    fun problemasCompletadosIds(): Set<Int>
    fun cantidadCompletadosPorDificultad(dificultad: Dificultad): Int
    fun totalProblemasPorDificultad(dificultad: Dificultad): Int
    fun progresoPorDificultad(dificultad: Dificultad): Double
    fun reiniciarProgreso()
    fun desbloquearNivelManualmente(dificultad: Dificultad)
}

/**
 * Gestor en memoria del progreso del jugador y desbloqueo de niveles en GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP) y Encapsulamiento Fuerte.
 */
class GestorProgreso(
    val repositorio: IRepositorioProblemas = GeneradorProblemas(),
    reglasPersonalizadas: List<IReglaDesbloqueo>? = null
) : IGestorProgreso {

    // Encapsulamiento Fuerte: Estado privado protegido
    private val _problemasCompletados: MutableSet<Int> = mutableSetOf()
    private val _dificultadesDesbloqueadasManualmente: MutableSet<Dificultad> = mutableSetOf()

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
     * Registra la resolución exitosa de un ejercicio físico.
     */
    override fun registrarVictoria(problema: Problema) {
        registrarProblemaCompletado(problema.id)
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
    }

    override fun desbloquearNivelManualmente(dificultad: Dificultad) {
        _dificultadesDesbloqueadasManualmente.add(dificultad)
    }
}
