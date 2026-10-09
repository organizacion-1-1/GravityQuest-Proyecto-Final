package gravityquest

/**
 * Formulas.kt - GravityQuest: Físicas y cálculos cinemáticos del juego.
 *
 * Contiene la interfaz [ICalculadoraFisica] y la clase [CalculadoraFisica]
 * con la lógica matemática y fórmulas cinemáticas puras para el juego GravityQuest.
 *
 * Cumple estrictamente con la Programación Orientada a Objetos (POO) y Principios SOLID:
 * - Principio de Responsabilidad Única (SRP): Contiene exclusivamente las fórmulas
 *   matemáticas y cinemática física pura, sin dependencias con JavaFX ni subsistemas visuales.
 *   Es una clase de servicio pura determinista y sin efectos secundarios.
 * - Principio Abierto/Cerrado (OCP): Diseñada para ser extendida con nuevos modelos cinemáticos
 *   (caída libre del Milestone 1, lanzamiento hacia abajo en Issue 12 y tiro vertical hacia arriba en Issue 13)
 *   sin modificar, alterar ni romper el comportamiento de las operaciones preexistentes.
 * - Inversión de Dependencias (DIP): Los módulos del motor y vistas dependen de
 *   la abstracción [ICalculadoraFisica].
 * - Encapsulamiento y Contratos de Dominio: Aplica precondiciones firmes con `require`,
 *   rechazando valores negativos o fuera de dominio físico mediante [IllegalArgumentException].
 *
 * Fórmulas cinemáticas (aceleración gravitatoria terrestre estándar g = 9.81 m/s²):
 * 1. Caída libre (v₀ = 0):
 *    - Velocidad final: v_f = √(2 * g * h)
 *    - Tiempo de caída:  t = √(2 * h / g)
 *    - Posición:        y = 1/2 * g * t²
 * 2. Lanzamiento hacia abajo (v₀ >= 0, a favor de la gravedad):
 *    - Velocidad final: v_f = √(v₀² + 2 * g * h)
 *    - Tiempo de caída:  t = (-v₀ + √(v₀² + 2 * g * h)) / g
 * 3. Lanzamiento hacia arriba (v₀ > 0, en desaceleración contra la gravedad):
 *    - Altura máxima:     h_máx = v₀² / (2 * g)
 *    - Tiempo de subida:   t_subida = v₀ / g
 *    - Tiempo total vuelo: t_total = 2 * v₀ / g
 * 4. Validación de tolerancia: |v_ingresado - v_esperado| <= margenError
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
    /** Aceleración de la gravedad en m/s². */
    val g: Double

    // --- Caída Libre (Milestone 1) ---
    fun calcularVelocidadFinal(altura: Double): Double
    fun calcularTiempo(altura: Double): Double
    fun calcularPosicionCaida(tiempo: Double): Double
    fun calcularPosicion(tiempo: Double): Double
    fun validarResultado(valorIngresado: Double, valorEsperado: Double, margenError: Double = 0.05): Boolean

    // --- Lanzamiento Vertical Hacia Abajo (Milestone 2 - Issue 12) ---
    fun calcularVelocidadFinalLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double
    fun calcularTiempoLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double

    // --- Lanzamiento Vertical Hacia Arriba (Milestone 2 - Issue 13) ---
    fun calcularAlturaMaxima(velocidadInicial: Double): Double
    fun calcularTiempoSubida(velocidadInicial: Double): Double
    fun calcularTiempoTotalVuelo(velocidadInicial: Double): Double
}

/**
 * Clase de servicio que encapsula las fórmulas de física pura del juego GravityQuest.
 *
 * Cumple estrictamente con:
 * - Single Responsibility Principle (SRP): Realiza únicamente cálculos cinemáticos
 *   deterministas sin efectos secundarios, sin acoplamiento a la interfaz gráfica ni librerías de UI.
 * - Open/Closed Principle (OCP): Extiende las capacidades del motor físico soportando
 *   caída libre, lanzamiento vertical hacia abajo y tiro vertical hacia arriba sin alterar
 *   el contrato ni la precisión de cálculos previos.
 *
 * Utiliza la aceleración de gravedad terrestre estándar (g = 9.81 m/s²).
 */
class CalculadoraFisica : ICalculadoraFisica {

    /** Aceleración de la gravedad en la Tierra (m/s²). */
    override val g: Double = 9.81

    // ========================================================================
    // 1. Fórmulas de Caída Libre (Milestone 1 - v₀ = 0 m/s)
    // ========================================================================

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
        require(altura >= 0) { "La altura no puede ser negativa: $altura m" }
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
        require(altura >= 0) { "La altura no puede ser negativa: $altura m" }
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
        require(tiempo >= 0) { "El tiempo no puede ser negativo: $tiempo s" }
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

    // ========================================================================
    // 2. Fórmulas de Lanzamiento Vertical Hacia Abajo (Milestone 2 - Issue 12)
    //    Movimiento con velocidad inicial a favor de la gravedad (v₀ >= 0)
    // ========================================================================

    /**
     * Calcula la velocidad final de un objeto lanzado verticalmente hacia abajo con velocidad inicial.
     *
     * Al estar orientada la velocidad inicial a favor de la gravedad, la aceleración suma magnitud:
     * Fórmula: v_f = √(v₀² + 2 * g * h)
     *
     * @param velocidadInicial Velocidad inicial de lanzamiento hacia abajo (en m/s). Debe ser >= 0.
     * @param altura Altura desde la cual se efectúa el lanzamiento (en metros). Debe ser >= 0.
     * @return La velocidad final de impacto en m/s.
     * @throws IllegalArgumentException si [velocidadInicial] o [altura] son negativas.
     */
    override fun calcularVelocidadFinalLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double {
        require(velocidadInicial >= 0) { "La velocidad inicial no puede ser negativa: $velocidadInicial m/s" }
        require(altura >= 0) { "La altura no puede ser negativa: $altura m" }
        return sqrt((velocidadInicial * velocidadInicial) + (2.0 * g * altura))
    }

    /**
     * Calcula el tiempo transcurrido hasta que el cuerpo lanzado verticalmente hacia abajo
     * recorre la altura dada y alcanza el suelo.
     *
     * Corresponde a la raíz positiva de la ecuación horaria cuadrática: 1/2 * g * t² + v₀ * t - h = 0:
     * Fórmula: t = (-v₀ + √(v₀² + 2 * g * h)) / g
     *
     * @param velocidadInicial Velocidad inicial de lanzamiento hacia abajo (en m/s). Debe ser >= 0.
     * @param altura Altura recorrida en la caída (en metros). Debe ser >= 0.
     * @return El tiempo de descenso en segundos.
     * @throws IllegalArgumentException si [velocidadInicial] o [altura] son negativas.
     */
    override fun calcularTiempoLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double {
        require(velocidadInicial >= 0) { "La velocidad inicial no puede ser negativa: $velocidadInicial m/s" }
        require(altura >= 0) { "La altura no puede ser negativa: $altura m" }
        val discriminante = (velocidadInicial * velocidadInicial) + (2.0 * g * altura)
        return (-velocidadInicial + sqrt(discriminante)) / g
    }

    // ========================================================================
    // 3. Fórmulas de Lanzamiento Vertical Hacia Arriba (Milestone 2 - Issue 13)
    //    Movimiento en desaceleración contra la gravedad (v₀ > 0)
    // ========================================================================

    /**
     * Calcula la altura máxima alcanzada por un objeto lanzado verticalmente hacia arriba.
     *
     * El objeto desacelera por la fuerza gravitatoria hasta que su velocidad instantánea
     * se anula transitoriamente en el punto más alto (v = 0).
     * Fórmula: h_máx = v₀² / (2 * g)
     *
     * @param velocidadInicial Velocidad inicial de lanzamiento ascendente (en m/s). Debe ser estrictamente > 0.
     * @return La altura máxima en metros alcanzada por el objeto.
     * @throws IllegalArgumentException si [velocidadInicial] es menor o igual a cero.
     */
    override fun calcularAlturaMaxima(velocidadInicial: Double): Double {
        require(velocidadInicial > 0) {
            "La velocidad inicial debe ser mayor a cero para un lanzamiento vertical hacia arriba (recibido: $velocidadInicial m/s)."
        }
        return (velocidadInicial * velocidadInicial) / (2.0 * g)
    }

    /**
     * Calcula el tiempo de subida de un cuerpo lanzado verticalmente hacia arriba hasta la cúspide.
     *
     * Es el tiempo necesario para que la gravedad reduzca la velocidad inicial a cero (v = v₀ - g * t = 0).
     * Fórmula: t_subida = v₀ / g
     *
     * @param velocidadInicial Velocidad inicial de lanzamiento ascendente (en m/s). Debe ser estrictamente > 0.
     * @return El tiempo transcurrido hasta alcanzar la altura máxima en segundos.
     * @throws IllegalArgumentException si [velocidadInicial] es menor o igual a cero.
     */
    override fun calcularTiempoSubida(velocidadInicial: Double): Double {
        require(velocidadInicial > 0) {
            "La velocidad inicial debe ser mayor a cero para un lanzamiento vertical hacia arriba (recibido: $velocidadInicial m/s)."
        }
        return velocidadInicial / g
    }

    /**
     * Calcula el tiempo total de vuelo de un objeto lanzado verticalmente hacia arriba
     * hasta retornar al mismo nivel de lanzamiento (suponiendo simetría temporal).
     *
     * Equivale al doble del tiempo de subida: t_total = 2 * t_subida.
     * Fórmula: t_total = (2 * v₀) / g
     *
     * @param velocidadInicial Velocidad inicial de lanzamiento ascendente (en m/s). Debe ser estrictamente > 0.
     * @return El tiempo total de vuelo en segundos (subida + bajada).
     * @throws IllegalArgumentException si [velocidadInicial] es menor o igual a cero.
     */
    override fun calcularTiempoTotalVuelo(velocidadInicial: Double): Double {
        require(velocidadInicial > 0) {
            "La velocidad inicial debe ser mayor a cero para un lanzamiento vertical hacia arriba (recibido: $velocidadInicial m/s)."
        }
        return (2.0 * velocidadInicial) / g
    }
}
