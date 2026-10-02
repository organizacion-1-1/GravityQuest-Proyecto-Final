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
    // Instanciar la calculadora física
    val calculadora = CalculadoraFisica()

    // Altura de prueba
    val altura = 20.0

    println("=== GravityQuest: Cálculos de Caída Libre ===\n")
    println("  Gravedad (g):     ${calculadora.g} m/s²")
    println("  Altura:           $altura m\n")

    // 1. Calcular velocidad final
    val velocidadFinal = calculadora.calcularVelocidadFinal(altura)
    println("  Velocidad final:  ${"%.2f".format(velocidadFinal)} m/s")

    // 2. Calcular tiempo de caída
    val tiempo = calculadora.calcularTiempo(altura)
    println("  Tiempo de caída:  ${"%.2f".format(tiempo)} s\n")

    // Validación de error con altura negativa
    println("--- Prueba de validación (altura negativa) ---")
    try {
        calculadora.calcularTiempo(-5.0)
    } catch (e: IllegalArgumentException) {
        println("  ✓ Excepción capturada correctamente: ${e.message}\n")
    }

    // 3. Pruebas de validación de resultados (validarResultado)
    println("=== Pruebas de Validación de Resultados (validarResultado) ===\n")

    // Caso exitoso (dentro de la tolerancia)
    val estimacionCercana = 19.82
    val esValidoExitoso = calculadora.validarResultado(estimacionCercana, velocidadFinal, 0.05)
    println("  [Caso Exitoso]")
    println("    Valor ingresado: $estimacionCercana")
    println("    Valor esperado:  ${"%.2f".format(velocidadFinal)}")
    println("    Margen de error: 0.05")
    println("    ¿Resultado válido?: $esValidoExitoso\n")

    // Caso fallido (fuera de la tolerancia)
    val estimacionLejana = 21.50
    val esValidoFallido = calculadora.validarResultado(estimacionLejana, velocidadFinal, 0.05)
    println("  [Caso Fallido]")
    println("    Valor ingresado: $estimacionLejana")
    println("    Valor esperado:  ${"%.2f".format(velocidadFinal)}")
    println("    Margen de error: 0.05")
    println("    ¿Resultado válido?: $esValidoFallido\n")

    // Prueba del manejo de excepciones mediante un bloque try-catch cuando se pasa un margenError negativo
    println("--- Prueba de validación (margenError negativo) ---")
    try {
        calculadora.validarResultado(estimacionCercana, velocidadFinal, margenError = -0.05)
    } catch (e: IllegalArgumentException) {
        println("  ✓ Excepción capturada correctamente: ${e.message}")
    }
}
