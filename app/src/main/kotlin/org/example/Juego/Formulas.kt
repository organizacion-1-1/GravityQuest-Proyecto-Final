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

import kotlin.math.abs
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
     * Calcula el tiempo transcurrido de un objeto en caída libre desde una altura dada.
     *
     * Fórmula: t = √(2 * h / g)
     *
     * @param altura La altura desde la que cae el objeto (en metros). Debe ser >= 0.
     * @return El tiempo en segundos.
     * @throws IllegalArgumentException si la altura es negativa.
     */
    fun calcularTiempo(altura: Double): Double {
        require(altura >= 0) { "La altura no puede ser negativa: $altura" }
        return sqrt(2.0 * altura / g)
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
    fun validarResultado(valorIngresado: Double, valorEsperado: Double, margenError: Double = 0.05): Boolean {
        require(margenError >= 0) { "El margen de error no puede ser negativo: $margenError" }
        return abs(valorIngresado - valorEsperado) <= margenError
    }
}
