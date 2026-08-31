package gravityquest

/**
 * Formulas.kt - GravityQuest: Físicas y cálculos del juego.
 *
 * Este archivo contiene todas las funciones de cálculo físico y las
 * constantes gravitacionales utilizadas en GravityQuest. Está separado
 * de main.kt para mantener una arquitectura limpia: la lógica de
 * dominio (física) vive aquí, y el punto de entrada (main) queda en
 * su propio archivo.
 *
 * Fórmula principal: v_f = √(2 * g * h)
 * Derivada de la ecuación de movimiento:
 *   v² = v₀² + 2 * a * d
 * Para caída libre con v₀ = 0 → v = √(2 * g * h)
 */

import kotlin.math.sqrt

// ──────────────────────────────────────────────────────────
// Constantes gravitacionales (m/s²)
// ──────────────────────────────────────────────────────────

/** Aceleración de la gravedad en la Tierra. */
const val GRAVEDAD_TERRESTRE = 9.81

/** Aceleración de la gravedad en la Luna (aprox. 1/6 de la terrestre). */
const val GRAVEDAD_LUNAR = 1.62

/** Aceleración de la gravedad en Marte. */
const val GRAVEDAD_MARTE = 3.72

// ──────────────────────────────────────────────────────────
// Funciones de caída libre
// ──────────────────────────────────────────────────────────

/**
 * Calcula la velocidad final de un objeto en caída libre desde una altura dada.
 *
 * @param altura La altura desde la que cae el objeto (en metros). Debe ser >= 0.
 * @param g      La aceleración de la gravedad (en m/s²). Por defecto [GRAVEDAD_TERRESTRE].
 * @return La velocidad final en m/s.
 * @throws IllegalArgumentException si la altura es negativa.
 */
fun calcularVelocidadFinal(altura: Double, g: Double = GRAVEDAD_TERRESTRE): Double {
    require(altura >= 0) { "La altura no puede ser negativa: $altura" }
    return sqrt(2.0 * g * altura)
}

/**
 * Calcula el tiempo que tarda un objeto en caer desde una altura dada (caída libre).
 *
 * Fórmula: t = √(2 * h / g)
 *
 * @param altura La altura de caída (en metros). Debe ser >= 0.
 * @param g      La aceleración de la gravedad (en m/s²). Por defecto [GRAVEDAD_TERRESTRE].
 * @return El tiempo de caída en segundos.
 * @throws IllegalArgumentException si la altura es negativa.
 */
fun calcularTiempo(altura: Double, g: Double = GRAVEDAD_TERRESTRE): Double {
    require(altura >= 0) { "La altura no puede ser negativa: $altura" }
    return sqrt(2.0 * altura / g)
}

/**
 * Calcula la altura desde la que debe caer un objeto para alcanzar una velocidad final dada.
 *
 * Fórmula: h = v² / (2 * g)
 *
 * @param velocidad La velocidad final deseada (en m/s). Debe ser >= 0.
 * @param g         La aceleración de la gravedad (en m/s²). Por defecto [GRAVEDAD_TERRESTRE].
 * @return La altura necesaria en metros.
 * @throws IllegalArgumentException si la velocidad es negativa.
 */
fun calcularAltura(velocidad: Double, g: Double = GRAVEDAD_TERRESTRE): Double {
    require(velocidad >= 0) { "La velocidad no puede ser negativa: $velocidad" }
    return (velocidad * velocidad) / (2.0 * g)
}
