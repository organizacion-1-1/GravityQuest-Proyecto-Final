package gravityquest

/**
 * GravityQuest - Punto de entrada del programa (main.kt)
 *
 * Este archivo contiene únicamente la función main(), que actúa como
 * el punto de entrada de la aplicación. Toda la lógica de cálculo
 * físico se encuentra en Formulas.kt.
 *
 * Ejecutar este programa muestra ejemplos de prueba para distintos
 * escenarios del juego GravityQuest (caída libre en distintos
 * planetas).
 */

fun main() {
    // ──────────────────────────────────────────────────
    // Ejemplo 1: Caída en la Tierra
    // ──────────────────────────────────────────────────
    val alturaTierra = 20.0
    val vFinalTierra = calcularVelocidadFinal(alturaTierra)
    val tiempoTierra = calcularTiempo(alturaTierra)

    println("=== GravityQuest: Ejemplos de Caída Libre ===\n")

    println("--- Tierra (g = $GRAVEDAD_TERRESTRE m/s²) ---")
    println("  Altura:        $alturaTierra m")
    println("  Velocidad final: ${"%.2f".format(vFinalTierra)} m/s")
    println("  Tiempo de caída: ${"%.2f".format(tiempoTierra)} s\n")

    // ──────────────────────────────────────────────────
    // Ejemplo 2: Caída en la Luna
    // ──────────────────────────────────────────────────
    val alturaLuna = 20.0
    val vFinalLuna = calcularVelocidadFinal(alturaLuna, GRAVEDAD_LUNAR)
    val tiempoLuna = calcularTiempo(alturaLuna, GRAVEDAD_LUNAR)

    println("--- Luna (g = $GRAVEDAD_LUNAR m/s²) ---")
    println("  Altura:        $alturaLuna m")
    println("  Velocidad final: ${"%.2f".format(vFinalLuna)} m/s")
    println("  Tiempo de caída: ${"%.2f".format(tiempoLuna)} s\n")

    // ──────────────────────────────────────────────────
    // Ejemplo 3: Caída en Marte
    // ──────────────────────────────────────────────────
    val alturaMarte = 20.0
    val vFinalMarte = calcularVelocidadFinal(alturaMarte, GRAVEDAD_MARTE)
    val tiempoMarte = calcularTiempo(alturaMarte, GRAVEDAD_MARTE)

    println("--- Marte (g = $GRAVEDAD_MARTE m/s²) ---")
    println("  Altura:        $alturaMarte m")
    println("  Velocidad final: ${"%.2f".format(vFinalMarte)} m/s")
    println("  Tiempo de caída: ${"%.2f".format(tiempoMarte)} s\n")

    // ──────────────────────────────────────────────────
    // Ejemplo 4: Calcular altura necesaria (Titan)
    // ──────────────────────────────────────────────────
    val velocidadObjetivo = 15.0
    val alturaNecesaria = calcularAltura(velocidadObjetivo, GRAVEDAD_MARTE)

    println("--- Marte: Altura necesaria para alcanzar $velocidadObjetivo m/s ---")
    println("  Altura requerida: ${"%.2f".format(alturaNecesaria)} m")
}
