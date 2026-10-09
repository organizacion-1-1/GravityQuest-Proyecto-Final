package gravityquest

/**
 * Formulas.kt - GravityQuest: Físicas y cálculos cinemáticos del juego.
 *
 * Contiene la interfaz [ICalculadoraFisica] y la clase [CalculadoraFisica]
 * con la lógica matemática y fórmulas cinemáticas puras para el juego GravityQuest.
 *
 * Cumple estrictamente con los principios POO y SOLID:
 * - Principio de Responsabilidad Única (SRP): Contiene exclusivamente las fórmulas
 *   matemáticas y física pura, sin dependencias con JavaFX ni subsistemas visuales.
 * - Inversión de Dependencias (DIP): Los módulos del motor y vistas dependen de
 *   la abstracción [ICalculadoraFisica].
 *
 * Fórmulas cinemáticas (para caída libre con v₀ = 0 y aceleración g = 9.81 m/s²):
 * - Velocidad final: v_f = √(2 * g * h)
 * - Tiempo de caída:  t = √(2 * h / g)
 * - Posición:        y = 1/2 * g * t²
 * - Validación:      |v_ingresado - v_esperado| <= margenError
 */

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Interfaz que define las operaciones de cálculo físico para el juego GravityQuest.
 *
 * Cumple con el Principio de Inversión de Dependencias (DIP) y Responsabilidad Única (SRP),
 * permitiendo que los módulos de simulación física y renderizado dependan de una abstracción.
 */
interface ICalculadoraFisica {
    val g: Double
    fun calcularVelocidadFinal(altura: Double): Double
    fun calcularTiempo(altura: Double): Double
    fun calcularPosicionCaida(tiempo: Double): Double
    fun calcularPosicion(tiempo: Double): Double
    fun validarResultado(valorIngresado: Double, valorEsperado: Double, margenError: Double = 0.05): Boolean
}

/**
 * Clase que encapsula las fórmulas de física pura del juego GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Contiene únicamente las fórmulas matemáticas y física pura, sin acoplamiento
 * a la interfaz gráfica ni a librerías de UI.
 *
 * Utiliza la aceleración de gravedad terrestre estándar (g = 9.81 m/s²).
 */
class CalculadoraFisica : ICalculadoraFisica {

    /** Aceleración de la gravedad en la Tierra (m/s²). */
    override val g: Double = 9.81

    /**
     * Calcula la velocidad final de un objeto en caída libre desde una altura dada.
     *
     * Fórmula: v_f = √(2 * g * h)
     *
     * @param altura La altura desde la que cae el objeto (en metros). Debe ser >= 0.
     * @return La velocidad final en m/s.
     * @throws IllegalArgumentException si la altura es negativa.
     */
    override fun calcularVelocidadFinal(altura: Double): Double {
        require(altura >= 0) { "La altura no puede ser negativa: $altura" }
        return sqrt(2.0 * g * altura)
    }

    /**
     * Calcula el tiempo transcurrido de un objeto en caída libre desde una altura dada.
     *
     * Fórmula: t = √(2 * h / g)
     *
     * @param altura La altura desde la que cae el objeto (en metros). Debe ser >= 0.
     * @return El tiempo en segundos.
     * @throws IllegalArgumentException si la altura es negativa.
     */
    override fun calcularTiempo(altura: Double): Double {
        require(altura >= 0) { "La altura no puede ser negativa: $altura" }
        return sqrt(2.0 * altura / g)
    }

    /**
     * Calcula la posición física (distancia recorrida en caída libre) en un tiempo determinado.
     *
     * Fórmula: y = 1/2 * g * t²
     *
     * @param tiempo El tiempo transcurrido en segundos. Debe ser >= 0.
     * @return La distancia recorrida en metros.
     * @throws IllegalArgumentException si el tiempo es negativo.
     */
    override fun calcularPosicionCaida(tiempo: Double): Double {
        require(tiempo >= 0) { "El tiempo no puede ser negativo: $tiempo" }
        return 0.5 * g * tiempo * tiempo
    }

    /**
     * Alias de [calcularPosicionCaida] para delegación de cálculo de posición física (SRP).
     */
    override fun calcularPosicion(tiempo: Double): Double {
        return calcularPosicionCaida(tiempo)
    }

    /**
     * Valida si un valor ingresado coincide con el valor esperado dentro de un margen de error permitido (tolerancia).
     *
     * Función pura miembro de la clase que calcula la diferencia absoluta entre el valor
     * ingresado y el valor esperado, evaluando si no excede el margen de error especificado.
     *
     * @param valorIngresado El valor obtenido, ingresado o estimado a validar.
     * @param valorEsperado El valor teórico o calculado de referencia.
     * @param margenError Tolerancia o margen de error permitido (por defecto 0.05). Debe ser >= 0.
     * @return `true` si la diferencia absoluta es menor o igual al margen de error, `false` en caso contrario.
     * @throws IllegalArgumentException si [margenError] es menor que 0.
     */
    override fun validarResultado(valorIngresado: Double, valorEsperado: Double, margenError: Double): Boolean {
        require(margenError >= 0) { "El margen de error no puede ser negativo: $margenError" }
        return abs(valorIngresado - valorEsperado) <= margenError
    }
}
