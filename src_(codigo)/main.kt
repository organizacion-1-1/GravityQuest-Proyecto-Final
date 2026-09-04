package gravityquest

/**
 * GravityQuest - Punto de entrada del programa (main.kt)
 *
 * Este archivo contiene la función main(), que actúa como
 * el punto de entrada de la aplicación. Instancia la clase
 * [CalculadoraFisica] y realiza pruebas de cálculo.
 *
 * Toda la lógica de cálculo físico se encuentra en Formulas.kt.
 */

fun main() {
    val calculadora = CalculadoraFisica()

    // ──────────────────────────────────────────────────
    // Ejemplo 1: Caída libre - Velocidad final
    // ──────────────────────────────────────────────────
    val altura = 20.0
    val velocidadFinal = calculadora.calcularVelocidadFinal(altura)

    println("=== GravityQuest: Cálculos Físicos ===\n")

    println("--- Caída Libre: Velocidad Final ---")
    println("  Gravedad (g):     ${calculadora.g} m/s²")
    println("  Altura:           $altura m")
    println("  Velocidad final:  ${"%.2f".format(velocidadFinal)} m/s\n")

    // ──────────────────────────────────────────────────
    // Ejemplo 2: Caída libre - Tiempo de caída
    // ──────────────────────────────────────────────────
    val tiempoCaida = calculadora.calcularTiempo(altura)

    println("--- Caída Libre: Tiempo de Caída ---")
    println("  Altura:           $altura m")
    println("  Tiempo de caída:  ${"%.2f".format(tiempoCaida)} s\n")

    // ──────────────────────────────────────────────────
    // Ejemplo 3: Manejo de errores
    // ──────────────────────────────────────────────────
    println("--- Pruebas de validación (entradas inválidas) ---\n")

    // Altura negativa en caída libre
    print("  calcularVelocidadFinal(-5.0):  ")
    try {
        calculadora.calcularVelocidadFinal(-5.0)
        println("¡Debería haber fallado!")
    } catch (e: IllegalArgumentException) {
        println("Error: ${e.message}")
    }

    // Altura negativa en tiempo de caída
    print("  calcularTiempo(-5.0):          ")
    try {
        calculadora.calcularTiempo(-5.0)
        println("¡Debería haber fallado!")
    } catch (e: IllegalArgumentException) {
        println("Error: ${e.message}")
    }
}
