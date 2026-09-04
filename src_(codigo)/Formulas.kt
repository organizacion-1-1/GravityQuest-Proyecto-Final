package gravityquest

/**
 * Formulas.kt - GravityQuest: Físicas y cálculos del juego.
 *
 * Contiene la clase [CalculadoraFisica] con la lógica matemática
 * y las fórmulas de física para el juego GravityQuest.
 *
 * Fórmula principal: v_f = √(2 * g * h)
 * Derivada de la ecuación de movimiento:
 *   v² = v₀² + 2 * a * d
 * Para caída libre con v₀ = 0 → v = √(2 * g * h)
 */

import kotlin.math.sqrt

/**
 * Clase que encapsula las fórmulas de física del juego GravityQuest.
 *
 * Utiliza únicamente la aceleración de gravedad terrestre (g = 9.81 m/s²).
 * Los distintos tipos de movimiento (caída libre, lanzamiento hacia abajo,
 * lanzamiento hacia arriba) se determinan por el contexto de uso, no por
 * el planeta.
 */
class CalculadoraFisica {

    /** Aceleración de la gravedad en la Tierra (m/s²). */
    val g: Double = 9.81

    /**
     * Calcula la velocidad final de un objeto en caída libre desde una altura dada.
     *
     * Fórmula: v_f = √(2 * g * h)
     *
     * @param altura La altura desde la que cae el objeto (en metros). Debe ser >= 0.
     * @return La velocidad final en m/s.
     * @throws IllegalArgumentException si la altura es negativa.
     */
    fun calcularVelocidadFinal(altura: Double): Double {
        require(altura >= 0) { "La altura no puede ser negativa: $altura" }
        return sqrt(2.0 * g * altura)
    }

    /**
     * Calcula el tiempo transcurrido en movimiento de caída libre (MRUV con v₀ = 0).
     *
     * Fórmula: t = √(2 * h / g)
     *
     * @param altura La altura de caída en metros. Debe ser >= 0.
     * @return El tiempo de caída en segundos.
     * @throws IllegalArgumentException si la altura es negativa.
     */
    fun calcularTiempo(altura: Double): Double {
        require(altura >= 0) { "La altura no puede ser negativa: $altura" }
        return sqrt(2.0 * altura / g)
    }
}
