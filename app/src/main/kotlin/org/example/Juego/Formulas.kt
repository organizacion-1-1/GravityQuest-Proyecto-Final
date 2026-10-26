package gravityquest

/**
 * Formulas.kt - GravityQuest: Físicas y cálculos cinemáticos del juego.
 *
 * Contiene las interfaces segregadas de cálculo cinemático, la jerarquía polimórfica
 * de estrategias de cálculo físico ([IEstrategiaCalculo]) y la clase [CalculadoraFisica]
 * con la lógica matemática pura para el juego GravityQuest.
 *
 * Cumple estrictamente con la Programación Orientada a Objetos (POO) y Principios SOLID:
 * - Single Responsibility Principle (SRP): Contiene exclusivamente fórmulas
 *   matemáticas y cinemática física pura, sin dependencias con JavaFX ni subsistemas visuales.
 *   Es una clase de servicio pura determinista y sin efectos secundarios.
 * - Open/Closed Principle (OCP): Abierta a extensiones para nuevos modelos cinemáticos
 *   mediante el patrón Strategy ([IEstrategiaCalculo]), sin modificar ni romper el comportamiento
 *   de las operaciones preexistentes.
 * - Interface Segregation Principle (ISP): Segrega los contratos físicos en interfaces
 *   específicas ([ICalculadoraCaidaLibre], [ICalculadoraLanzamientoAbajo], [ICalculadoraLanzamientoArriba]),
 *   evitando interfaces monolíticas y permitiendo a los clientes depender sólo de lo necesario.
 * - Dependency Inversion Principle (DIP): Los módulos de simulación y vistas dependen
 *   de las abstracciones físicas e interfaces de estrategia.
 * - Encapsulamiento y Contratos de Dominio: Aplica precondiciones firmes con `require`,
 *   rechazando valores fuera de dominio físico mediante [IllegalArgumentException].
 * - Don't Repeat Yourself (DRY): Centraliza la constante gravitatoria y márgenes en [PhysicsConstants].
 */

import gravityquest.models.TipoProblema
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Dirección del movimiento físico vertical para simulación y renderizado gráfico.
 */
enum class DireccionMovimiento {
    /** Movimiento vertical en sentido descendente (hacia el suelo). */
    ABAJO,
    /** Movimiento en tiro vertical hacia arriba desacelerado por gravedad. */
    ARRIBA
}

/**
 * Contrato de abstracción para la estrategia polimórfica de cálculo y simulación física (POO + OCP + DIP).
 * Permite resolver la cinemática de cada nivel sin condicionales rígidos `when`.
 */
interface IEstrategiaCalculo {
    val tipo: TipoProblema
    val direccionMovimiento: DireccionMovimiento
    val formulasPista: String

    /** Valida los invariantes físicos propios del tipo de movimiento (ej. v₀ > 0, h >= 0). */
    fun validarParametros(altura: Double, velocidadInicial: Double)

    /** Calcula el tiempo total de simulación cinemática. */
    fun calcularTiempoTotal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double

    /** Calcula la velocidad final de impacto o conclusión del movimiento. */
    fun calcularVelocidadFinal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double

    /** Calcula la posición vertical (en metros) transcurrido un tiempo t. */
    fun calcularPosicionEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double

    /** Calcula la velocidad vertical instantánea (en m/s) transcurrido un tiempo t. */
    fun calcularVelocidadEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double

    /** Posición vertical final de la simulación al detenerse. */
    fun posicionFinal(altura: Double): Double

    /** Altura de referencia para escalar visualmente el lienzo gráfico Canvas. */
    fun calcularAlturaReferencia(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double
}

/**
 * Estrategia cinemática para Caída Libre (Nivel Fácil, v₀ = 0 m/s).
 * Fórmulas: t = √(2h / g), v_f = √(2gh), y(t) = 1/2 * g * t².
 */
object EstrategiaCaidaLibre : IEstrategiaCalculo {
    override val tipo: TipoProblema = TipoProblema.CAIDA_LIBRE
    override val direccionMovimiento: DireccionMovimiento = DireccionMovimiento.ABAJO

    override val formulasPista: String = """
        • Tiempo de caída:   t = √(2 · h / g)
        • Velocidad final:   v_f = √(2 · g · h)
        • Posición vertical: y(t) = 1/2 · g · t²
        (Aceleración g = ${PhysicsConstants.GRAVEDAD_TIERRA} m/s², v₀ = 0 m/s)
    """.trimIndent()

    override fun validarParametros(altura: Double, velocidadInicial: Double) {
        require(altura >= 0.0 && !altura.isNaN() && !altura.isInfinite()) { "La altura no puede ser negativa ni inválida: $altura m" }
        require(velocidadInicial >= 0.0 && !velocidadInicial.isNaN() && !velocidadInicial.isInfinite()) {
            "La velocidad inicial no puede ser negativa ni inválida: $velocidadInicial m/s"
        }
    }

    override fun calcularTiempoTotal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        calculadora.calcularTiempo(altura)

    override fun calcularVelocidadFinal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        calculadora.calcularVelocidadFinal(altura)

    override fun calcularPosicionEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double =
        calculadora.calcularPosicionCaida(tiempo)

    override fun calcularVelocidadEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double =
        calculadora.g * tiempo

    override fun posicionFinal(altura: Double): Double = altura

    override fun calcularAlturaReferencia(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        if (altura > 0.0) altura else PhysicsConstants.ALTURA_DEFAULT
}

/**
 * Estrategia cinemática para Lanzamiento Vertical Hacia Abajo (Nivel Medio, v₀ > 0 m/s).
 * Fórmulas: v_f = √(v₀² + 2gh), t = (-v₀ + √(v₀² + 2gh)) / g, y(t) = v₀·t + 1/2·g·t².
 */
object EstrategiaLanzamientoAbajo : IEstrategiaCalculo {
    override val tipo: TipoProblema = TipoProblema.LANZAMIENTO_VERTICAL_ABAJO
    override val direccionMovimiento: DireccionMovimiento = DireccionMovimiento.ABAJO

    override val formulasPista: String = """
        • Velocidad final:   v_f = √(v₀² + 2 · g · h)
        • Tiempo de caída:   t = (-v₀ + √(v₀² + 2 · g · h)) / g
        • Posición vertical: y(t) = v₀ · t + 1/2 · g · t²
        • Velocidad:         v(t) = v₀ + g · t
        (Movimiento descendente acelerado con velocidad inicial v₀ > 0 m/s)
    """.trimIndent()

    override fun validarParametros(altura: Double, velocidadInicial: Double) {
        require(altura >= 0.0 && !altura.isNaN() && !altura.isInfinite()) { "La altura no puede ser negativa ni inválida: $altura m" }
        require(velocidadInicial >= 0.0 && !velocidadInicial.isNaN() && !velocidadInicial.isInfinite()) {
            "La velocidad inicial no puede ser negativa ni inválida: $velocidadInicial m/s"
        }
    }

    override fun calcularTiempoTotal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        calculadora.calcularTiempoLanzamientoAbajo(velocidadInicial, altura)

    override fun calcularVelocidadFinal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        calculadora.calcularVelocidadFinalLanzamientoAbajo(velocidadInicial, altura)

    override fun calcularPosicionEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double =
        calculadora.calcularPosicionLanzamientoAbajo(velocidadInicial, tiempo)

    override fun calcularVelocidadEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double =
        calculadora.calcularVelocidadLanzamientoAbajo(velocidadInicial, tiempo)

    override fun posicionFinal(altura: Double): Double = altura

    override fun calcularAlturaReferencia(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        if (altura > 0.0) altura else PhysicsConstants.ALTURA_DEFAULT
}

/**
 * Estrategia cinemática para Lanzamiento Vertical Hacia Arriba (Nivel Difícil, v₀ > 0 m/s).
 * Fórmulas: h_máx = v₀² / (2g), t_subida = v₀ / g, t_total = 2v₀ / g, y(t) = v₀·t - 1/2·g·t².
 */
object EstrategiaLanzamientoArriba : IEstrategiaCalculo {
    override val tipo: TipoProblema = TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA
    override val direccionMovimiento: DireccionMovimiento = DireccionMovimiento.ARRIBA

    override val formulasPista: String = """
        • Altura máxima:     h_máx = v₀² / (2 · g)
        • Tiempo de subida:   t_sub = v₀ / g
        • Tiempo total vuelo: t_total = 2 · v₀ / g
        • Posición vertical: y(t) = v₀ · t - 1/2 · g · t²
        • Velocidad:         v(t) = v₀ - g · t
        (En la cúspide la velocidad instantánea se anula: v = 0)
    """.trimIndent()

    override fun validarParametros(altura: Double, velocidadInicial: Double) {
        require(velocidadInicial > 0.0 && !velocidadInicial.isNaN() && !velocidadInicial.isInfinite()) {
            "Para lanzamiento vertical hacia arriba se requiere velocidad inicial v₀ > 0 (recibido: $velocidadInicial m/s)."
        }
    }

    override fun calcularTiempoTotal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        calculadora.calcularTiempoTotalVuelo(velocidadInicial)

    override fun calcularVelocidadFinal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        -velocidadInicial

    override fun calcularPosicionEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double =
        calculadora.calcularPosicionLanzamientoArriba(velocidadInicial, tiempo)

    override fun calcularVelocidadEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double =
        calculadora.calcularVelocidadLanzamientoArriba(velocidadInicial, tiempo)

    override fun posicionFinal(altura: Double): Double = 0.0

    override fun calcularAlturaReferencia(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double =
        calculadora.calcularAlturaMaxima(velocidadInicial)
}

/**
 * Estrategia cinemática extensible para Movimiento Rectilíneo Uniforme (MRU).
 */
object EstrategiaMRU : IEstrategiaCalculo {
    override val tipo: TipoProblema = TipoProblema.MRU
    override val direccionMovimiento: DireccionMovimiento = DireccionMovimiento.ABAJO

    override val formulasPista: String = """
        • Distancia recorrida: d = v · t
        • Velocidad constante: v = d / t
        (Aceleración nula: a = 0 m/s²)
    """.trimIndent()

    override fun validarParametros(altura: Double, velocidadInicial: Double) {}

    override fun calcularTiempoTotal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double = 0.0

    override fun calcularVelocidadFinal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double = velocidadInicial

    override fun calcularPosicionEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double =
        velocidadInicial * tiempo

    override fun calcularVelocidadEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double =
        velocidadInicial

    override fun posicionFinal(altura: Double): Double = altura

    override fun calcularAlturaReferencia(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double = altura
}

/**
 * Estrategia genérica para ejercicios adicionales (OCP).
 */
object EstrategiaGenerica : IEstrategiaCalculo {
    override val tipo: TipoProblema = TipoProblema.OTRO
    override val direccionMovimiento: DireccionMovimiento = DireccionMovimiento.ABAJO

    override val formulasPista: String = "• Ecuaciones horarias cinemáticas generales aplicables según gravedad local."

    override fun validarParametros(altura: Double, velocidadInicial: Double) {}

    override fun calcularTiempoTotal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double = 0.0

    override fun calcularVelocidadFinal(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double = 0.0

    override fun calcularPosicionEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double = 0.0

    override fun calcularVelocidadEnTiempo(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double, tiempo: Double): Double = 0.0

    override fun posicionFinal(altura: Double): Double = altura

    override fun calcularAlturaReferencia(calculadora: ICalculadoraFisica, altura: Double, velocidadInicial: Double): Double = altura
}

/**
 * ============================================================================
 * GravityQuest - Cálculos Auxiliares y Resolución de Estrategias (POO & SRP)
 * ============================================================================
 *
 * Encapsula funciones matemáticas auxiliares, redondeo y resolución polimórfica
 * de estrategias cinemáticas en un objeto singleton dedicado.
 */
object CalculosAuxiliares {

    /**
     * Resuelve la estrategia física correspondiente a partir del [TipoProblema] para máxima extensibilidad (OCP).
     */
    fun resolverEstrategia(tipo: TipoProblema): IEstrategiaCalculo = when (tipo) {
        TipoProblema.CAIDA_LIBRE -> EstrategiaCaidaLibre
        TipoProblema.LANZAMIENTO_VERTICAL_ABAJO -> EstrategiaLanzamientoAbajo
        TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA -> EstrategiaLanzamientoArriba
        TipoProblema.MRU -> EstrategiaMRU
        TipoProblema.MRUV, TipoProblema.OTRO -> EstrategiaGenerica
    }

    /**
     * Redondea un valor decimal a una cantidad específica de posiciones decimales.
     */
    fun redondear(valor: Double, decimales: Int = 2): Double {
        var factor = 1.0
        repeat(decimales) { factor *= 10.0 }
        return kotlin.math.round(valor * factor) / factor
    }

    /**
     * Parsea una cadena de texto admitiendo coma o punto como separador decimal.
     */
    fun parsearNumeroDecimal(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()
}

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
    fun calcularPosicionLanzamientoAbajo(velocidadInicial: Double, tiempo: Double, y0: Double = 0.0): Double
    fun calcularVelocidadLanzamientoAbajo(velocidadInicial: Double, tiempo: Double): Double
}

/**
 * Operaciones cinemáticas para lanzamientos verticales hacia arriba con desaceleración gravitatoria (v₀ > 0 m/s).
 */
interface ICalculadoraLanzamientoArriba {
    fun calcularAlturaMaxima(velocidadInicial: Double): Double
    fun calcularTiempoSubida(velocidadInicial: Double): Double
    fun calcularTiempoTotalVuelo(velocidadInicial: Double): Double
    fun calcularPosicionLanzamientoArriba(velocidadInicial: Double, tiempo: Double): Double
    fun calcularVelocidadLanzamientoArriba(velocidadInicial: Double, tiempo: Double): Double
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
        require(g > 0.0 && !g.isNaN() && !g.isInfinite()) { "La aceleración gravitatoria debe ser estrictamente positiva: $g m/s²" }
    }

    // ========================================================================
    // 1. Fórmulas de Caída Libre (v₀ = 0 m/s)
    // ========================================================================

    /**
     * Calcula la velocidad final de un objeto en caída libre desde una altura dada.
     * Fórmula: v_f = √(2 * g * h)
     */
    override fun calcularVelocidadFinal(altura: Double): Double {
        require(altura >= 0.0 && !altura.isNaN() && !altura.isInfinite()) { "La altura no puede ser negativa: $altura m" }
        return sqrt(2.0 * g * altura)
    }

    /**
     * Calcula el tiempo de caída de un objeto en reposo desde una altura dada.
     * Fórmula: t = √(2 * h / g)
     */
    override fun calcularTiempo(altura: Double): Double {
        require(altura >= 0.0 && !altura.isNaN() && !altura.isInfinite()) { "La altura no puede ser negativa: $altura m" }
        return sqrt(2.0 * altura / g)
    }

    /**
     * Calcula la posición (distancia recorrida hacia abajo) en un tiempo determinado.
     * Fórmula: y = 1/2 * g * t²
     */
    override fun calcularPosicionCaida(tiempo: Double): Double {
        require(tiempo >= 0.0 && !tiempo.isNaN() && !tiempo.isInfinite()) { "El tiempo no puede ser negativo: $tiempo s" }
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
        require(margenError >= 0.0 && !margenError.isNaN()) { "El margen de error no puede ser negativo: $margenError" }
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
        require(velocidadInicial >= 0.0 && !velocidadInicial.isNaN() && !velocidadInicial.isInfinite()) {
            "La velocidad inicial no puede ser negativa: $velocidadInicial m/s"
        }
        require(altura >= 0.0 && !altura.isNaN() && !altura.isInfinite()) { "La altura no puede ser negativa: $altura m" }
        return sqrt((velocidadInicial * velocidadInicial) + (2.0 * g * altura))
    }

    /**
     * Calcula el tiempo de caída de un objeto lanzado hacia abajo.
     * Fórmula: t = (-v₀ + √(v₀² + 2 * g * h)) / g
     */
    override fun calcularTiempoLanzamientoAbajo(velocidadInicial: Double, altura: Double): Double {
        require(velocidadInicial >= 0.0 && !velocidadInicial.isNaN() && !velocidadInicial.isInfinite()) {
            "La velocidad inicial no puede ser negativa: $velocidadInicial m/s"
        }
        require(altura >= 0.0 && !altura.isNaN() && !altura.isInfinite()) { "La altura no puede ser negativa: $altura m" }
        val discriminante = (velocidadInicial * velocidadInicial) + (2.0 * g * altura)
        return (-velocidadInicial + sqrt(discriminante)) / g
    }

    /**
     * Calcula la posición de un objeto lanzado hacia abajo en el tiempo t:
     * Fórmula: y(t) = y₀ + v₀ * t + 1/2 * g * t²
     */
    override fun calcularPosicionLanzamientoAbajo(velocidadInicial: Double, tiempo: Double, y0: Double): Double {
        require(velocidadInicial >= 0.0 && !velocidadInicial.isNaN() && !velocidadInicial.isInfinite()) {
            "La velocidad inicial no puede ser negativa: $velocidadInicial m/s"
        }
        require(tiempo >= 0.0 && !tiempo.isNaN() && !tiempo.isInfinite()) { "El tiempo no puede ser negativo: $tiempo s" }
        require(!y0.isNaN() && !y0.isInfinite()) { "La posición inicial y₀ no puede ser inválida: $y0" }
        return y0 + (velocidadInicial * tiempo) + (0.5 * g * tiempo * tiempo)
    }

    /**
     * Calcula la velocidad instantánea de un objeto lanzado hacia abajo en el tiempo t:
     * Fórmula: v(t) = v₀ + g * t
     */
    override fun calcularVelocidadLanzamientoAbajo(velocidadInicial: Double, tiempo: Double): Double {
        require(velocidadInicial >= 0.0 && !velocidadInicial.isNaN() && !velocidadInicial.isInfinite()) {
            "La velocidad inicial no puede ser negativa: $velocidadInicial m/s"
        }
        require(tiempo >= 0.0 && !tiempo.isNaN() && !tiempo.isInfinite()) { "El tiempo no puede ser negativo: $tiempo s" }
        return velocidadInicial + (g * tiempo)
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
     * Calcula la posición (altura respecto al suelo) en el tiempo t:
     * Fórmula: y(t) = v₀ * t - 1/2 * g * t²
     */
    override fun calcularPosicionLanzamientoArriba(velocidadInicial: Double, tiempo: Double): Double {
        validarVelocidadPositivaArriba(velocidadInicial)
        require(tiempo >= 0.0 && !tiempo.isNaN() && !tiempo.isInfinite()) { "El tiempo no puede ser negativo: $tiempo s" }
        val y = (velocidadInicial * tiempo) - (0.5 * g * tiempo * tiempo)
        return y.coerceAtLeast(0.0)
    }

    /**
     * Calcula la velocidad instantánea en tiro vertical hacia arriba en el tiempo t:
     * Fórmula: v(t) = v₀ - g * t
     */
    override fun calcularVelocidadLanzamientoArriba(velocidadInicial: Double, tiempo: Double): Double {
        validarVelocidadPositivaArriba(velocidadInicial)
        require(tiempo >= 0.0 && !tiempo.isNaN() && !tiempo.isInfinite()) { "El tiempo no puede ser negativo: $tiempo s" }
        return velocidadInicial - (g * tiempo)
    }

    /**
     * Helper privado para validar la invariante de velocidad ascendente estrictamente positiva.
     */
    private fun validarVelocidadPositivaArriba(velocidadInicial: Double) {
        require(velocidadInicial > 0.0 && !velocidadInicial.isNaN() && !velocidadInicial.isInfinite()) {
            "La velocidad inicial debe ser mayor a cero para un lanzamiento vertical hacia arriba (recibido: $velocidadInicial m/s)."
        }
    }

    /**
     * Resuelve la estrategia física correspondiente delegando en [CalculosAuxiliares] (SRP / OCP).
     */
    fun resolverEstrategia(tipo: TipoProblema): IEstrategiaCalculo =
        CalculosAuxiliares.resolverEstrategia(tipo)

    /**
     * Redondea un valor numérico delegando en [CalculosAuxiliares] (SRP).
     */
    fun redondear(valor: Double, decimales: Int = 2): Double =
        CalculosAuxiliares.redondear(valor, decimales)
}
