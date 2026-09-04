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

    // Probar el cálculo de velocidad final para una altura de 20.0 metros
    val altura = 20.0
    val velocidadFinal = calculadora.calcularVelocidadFinal(altura)

    println("=== GravityQuest: Cálculo de Velocidad Final ===\n")
    println("  Gravedad (g):     ${calculadora.g} m/s²")
    println("  Altura:           $altura m")
    println("  Velocidad final:  ${"%.2f".format(velocidadFinal)} m/s")
}
