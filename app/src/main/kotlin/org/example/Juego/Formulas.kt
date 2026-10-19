package gravityquest

/**
 * Formulas.kt - GravityQuest: Físicas y cálculos cinemáticos del juego.
 *
 * Contiene las interfaces segregadas de cálculo cinemático y la clase [CalculadoraFisica]
 * con la lógica matemática pura para el juego GravityQuest.
 *
 * Cumple estrictamente con la Programación Orientada a Objetos (POO) y Principios SOLID:
 * - Single Responsibility Principle (SRP): Contiene exclusivamente fórmulas
 *   matemáticas y cinemática física pura, sin dependencias con JavaFX ni subsistemas visuales.
 *   Es una clase de servicio pura determinista y sin efectos secundarios.
 * - Open/Closed Principle (OCP): Abierta a extensiones para nuevos modelos cinemáticos
 *   sin modificar ni romper el comportamiento de las operaciones preexistentes.
 * - Interface Segregation Principle (ISP): Segrega los contratos físicos en interfaces
 *   específicas ([ICalculadoraCaidaLibre], [ICalculadoraLanzamientoAbajo], [ICalculadoraLanzamientoArriba]),
 *   evitando interfaces monolíticas y permitiendo a los clientes depender sólo de lo necesario.
 * - Dependency Inversion Principle (DIP): Los módulos de simulación y vistas dependen
 *   de las abstracciones físicas.
 * - Encapsulamiento y Contratos de Dominio: Aplica precondiciones firmes con `require`,
 *   rechazando valores fuera de dominio físico mediante [IllegalArgumentException].
 * - Don't Repeat Yourself (DRY): Centraliza la constante gravitatoria y márgenes en [PhysicsConstants].
 */

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Operaciones cinemáticas para movimientos de caída libre pura (v₀ = 0 m/s).
 */
interface ICalculadoraCaidaLibre {
    /** Aceleración de la gravedad en m/s². */
    val g: Double

    fun calcularVelocidadFinal(altura: Double): Double
    fun calcularTiempo(altura: Double): Double
    fun calcularPosicionCaida(tiempo: Double): Double
    fun calcularPosicion(tiempo: Double): Double
    fun validarResultado(
        valorIngresado: Double,
        valorEsperado: Double,
        margenError: Double = PhysicsConstants.MARGEN_ERROR_DEFAULT
    ): Boolean
}

/**
 * Operaciones cinemáticas para lanzamientos verticales hacia abajo con velocidad inicial (v₀ >= 0 m/s).
 */
interface ICalculadoraLanzamientoAbajo {
    fun calcularVelocidadFinalLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double
    fun calcularTiempoLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double
}

/**
 * Operaciones cinemáticas para lanzamientos verticales hacia arriba con desaceleración gravitatoria (v₀ > 0 m/s).
 */
interface ICalculadoraLanzamientoArriba {
    fun calcularAlturaMaxima(velocidadInicial: Double): Double
    fun calcularTiempoSubida(velocidadInicial: Double): Double
    fun calcularTiempoTotalVuelo(velocidadInicial: Double): Double
}

/**
 * Contrato unificado de cálculo físico para el juego GravityQuest.
 *
 * Principio de Segregación de Interfaces (ISP):
 * Compone interfaces especializadas en lugar de forzar una única interfaz indivisible.
 */
interface ICalculadoraFisica :
    ICalculadoraCaidaLibre,
    ICalculadoraLanzamientoAbajo,
    ICalculadoraLanzamientoArriba

/**
 * Servicio puro que encapsula las fórmulas cinemáticas de física del juego GravityQuest.
 *
 * Utiliza por defecto la constante centralizada [PhysicsConstants.GRAVEDAD_TIERRA] (g = 9.81 m/s²).
 */
class CalculadoraFisica(
    override val g: Double = PhysicsConstants.GRAVEDAD_TIERRA
) : ICalculadoraFisica {

    init {
        require(g > 0.0) { "La aceleración gravitatoria debe ser estrictamente positiva: $g m/s²" }
    }

    // ========================================================================
    // 1. Fórmulas de Caída Libre (v₀ = 0 m/s)
    // ========================================================================

    /**
     * Calcula la velocidad final de un objeto en caída libre desde una altura dada.
     * Fórmula: v_f = √(2 * g * h)
     */
    override fun calcularVelocidadFinal(altura: Double): Double {
        require(altura >= 0.0) { "La altura no puede ser negativa: $altura m" }
        return sqrt(2.0 * g * altura)
    }

    /**
     * Calcula el tiempo de caída de un objeto en reposo desde una altura dada.
     * Fórmula: t = √(2 * h / g)
     */
    override fun calcularTiempo(altura: Double): Double {
        require(altura >= 0.0) { "La altura no puede ser negativa: $altura m" }
        return sqrt(2.0 * altura / g)
    }

    /**
     * Calcula la posición (distancia recorrida hacia abajo) en un tiempo determinado.
     * Fórmula: y = 1/2 * g * t²
     */
    override fun calcularPosicionCaida(tiempo: Double): Double {
        require(tiempo >= 0.0) { "El tiempo no puede ser negativo: $tiempo s" }
        return 0.5 * g * tiempo * tiempo
    }

    /**
     * Alias de [calcularPosicionCaida] para mantener el contrato cinemático.
     */
    override fun calcularPosicion(tiempo: Double): Double = calcularPosicionCaida(tiempo)

    /**
     * Valida si un valor ingresado coincide con el valor esperado dentro del margen de error permitido.
     */
    override fun validarResultado(valorIngresado: Double, valorEsperado: Double, margenError: Double): Boolean {
        require(margenError >= 0.0) { "El margen de error no puede ser negativo: $margenError" }
        return abs(valorIngresado - valorEsperado) <= margenError
    }

    // ========================================================================
    // 2. Fórmulas de Lanzamiento Vertical Hacia Abajo (v₀ >= 0 m/s)
    // ========================================================================

    /**
     * Calcula la velocidad final de un objeto lanzado verticalmente hacia abajo.
     * Fórmula: v_f = √(v₀² + 2 * g * h)
     */
    override fun calcularVelocidadFinalLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double {
        require(velocidadInicial >= 0.0) { "La velocidad inicial no puede ser negativa: $velocidadInicial m/s" }
        require(altura >= 0.0) { "La altura no puede ser negativa: $altura m" }
        return sqrt((velocidadInicial * velocidadInicial) + (2.0 * g * altura))
    }

    /**
     * Calcula el tiempo de caída de un objeto lanzado hacia abajo.
     * Fórmula: t = (-v₀ + √(v₀² + 2 * g * h)) / g
     */
    override fun calcularTiempoLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double {
        require(velocidadInicial >= 0.0) { "La velocidad inicial no puede ser negativa: $velocidadInicial m/s" }
        require(altura >= 0.0) { "La altura no puede ser negativa: $altura m" }
        val discriminante = (velocidadInicial * velocidadInicial) + (2.0 * g * altura)
        return (-velocidadInicial + sqrt(discriminante)) / g
    }

    // ========================================================================
    // 3. Fórmulas de Lanzamiento Vertical Hacia Arriba (v₀ > 0 m/s)
    // ========================================================================

    /**
     * Calcula la altura máxima alcanzada por un cuerpo lanzado verticalmente hacia arriba.
     * Fórmula: h_máx = v₀² / (2 * g)
     */
    override fun calcularAlturaMaxima(velocidadInicial: Double): Double {
        validarVelocidadPositivaArriba(velocidadInicial)
        return (velocidadInicial * velocidadInicial) / (2.0 * g)
    }

    /**
     * Calcula el tiempo transcurrido hasta alcanzar la altura máxima.
     * Fórmula: t_subida = v₀ / g
     */
    override fun calcularTiempoSubida(velocidadInicial: Double): Double {
        validarVelocidadPositivaArriba(velocidadInicial)
        return velocidadInicial / g
    }

    /**
     * Calcula el tiempo total de vuelo de un lanzamiento vertical hacia arriba.
     * Fórmula: t_total = 2 * v₀ / g
     */
    override fun calcularTiempoTotalVuelo(velocidadInicial: Double): Double {
        validarVelocidadPositivaArriba(velocidadInicial)
        return (2.0 * velocidadInicial) / g
    }

    /**
     * Helper privado para validar la invariante de velocidad ascendente estrictamente positiva.
     */
    private fun validarVelocidadPositivaArriba(velocidadInicial: Double) {
        require(velocidadInicial > 0.0) {
            "La velocidad inicial debe ser mayor a cero para un lanzamiento vertical hacia arriba (recibido: $velocidadInicial m/s)."
        }
    }
}
