package org.example.Juego
import gravityquest.PhysicsConstants; import kotlin.math.abs; import kotlin.math.max; import kotlin.math.min; import kotlin.math.sqrt
internal typealias ctorEstrategiaCaidaForm = EstrategiaCaidaLibre; internal typealias ctorEstrategiaTiroForm = EstrategiaTiroVertical
internal typealias ctorCalcForm = CalculadoraFisica

interface IDireccionMovimiento {
    val signoVertical: Double
    val descripcion: String
    fun esAscendenteInicial(): Boolean
    fun elegirEstrategia(caida: IEstrategiaCalculo, tiro: IEstrategiaCalculo): IEstrategiaCalculo
}

enum class DireccionMovimiento : IDireccionMovimiento {
    CAIDA_LIBRE {
        override val signoVertical: Double = 1.0
        override val descripcion: String = "\u0043a\u00edda \u004cibre"
        override fun elegirEstrategia(caida: IEstrategiaCalculo, tiro: IEstrategiaCalculo): IEstrategiaCalculo {
            return caida
        }
    },
    LANZAMIENTO_ABAJO {
        override val signoVertical: Double = 1.0
        override val descripcion: String = "\u004canzamiento \u0056ertical hacia \u0041bajo"
        override fun elegirEstrategia(caida: IEstrategiaCalculo, tiro: IEstrategiaCalculo): IEstrategiaCalculo {
            return caida
        }
    },
    LANZAMIENTO_ARRIBA {
        override val signoVertical: Double = -1.0
        override val descripcion: String = "\u004canzamiento \u0056ertical hacia \u0041rriba"
        override fun elegirEstrategia(caida: IEstrategiaCalculo, tiro: IEstrategiaCalculo): IEstrategiaCalculo {
            return tiro
        }
    };

    override fun esAscendenteInicial(): Boolean {
        return signoVertical < 0.0
    }
}

interface IEstrategiaCalculo {
    val nombreEstrategia: String
    val gravedadPorDefecto: Double
    fun validarParametros(altura: Double, velocidadInicial: Double, tiempo: Double)
    fun calcularPosicion(alturaInicial: Double, velocidadInicial: Double, tiempo: Double): Double
    fun calcularVelocidad(velocidadInicial: Double, tiempo: Double): Double
    fun calcularTiempoImpacto(alturaInicial: Double, velocidadInicial: Double): Double
}

class EstrategiaMRU(
    override val gravedadPorDefecto: Double = 0.0
) : IEstrategiaCalculo {
    override val nombreEstrategia: String = "MRU"

    override fun validarParametros(altura: Double, velocidadInicial: Double, tiempo: Double) {
        require(altura >= 0.0 && tiempo >= 0.0)
        require(velocidadInicial >= 0.0)
    }

    override fun calcularPosicion(alturaInicial: Double, velocidadInicial: Double, tiempo: Double): Double {
        validarParametros(alturaInicial, velocidadInicial, tiempo)
        return max(0.0, alturaInicial - velocidadInicial * tiempo)
    }

    override fun calcularVelocidad(velocidadInicial: Double, tiempo: Double): Double {
        validarParametros(0.0, velocidadInicial, tiempo)
        return velocidadInicial
    }

    override fun calcularTiempoImpacto(alturaInicial: Double, velocidadInicial: Double): Double {
        validarParametros(alturaInicial, velocidadInicial, 0.0)
        if (alturaInicial == 0.0 || velocidadInicial <= 0.0) return 0.0
        return alturaInicial / velocidadInicial
    }
}

class EstrategiaCaidaLibre(
    override val gravedadPorDefecto: Double = PhysicsConstants.GRAVEDAD_TIERRA
) : IEstrategiaCalculo {
    override val nombreEstrategia: String = "\u0043a\u00edda \u004cibre"

    override fun validarParametros(altura: Double, velocidadInicial: Double, tiempo: Double) {
        require(altura >= 0.0 && tiempo >= 0.0)
        require(velocidadInicial >= 0.0 && gravedadPorDefecto > 0.0)
    }

    override fun calcularPosicion(alturaInicial: Double, velocidadInicial: Double, tiempo: Double): Double {
        validarParametros(alturaInicial, velocidadInicial, tiempo)
        val desplazado = velocidadInicial * tiempo + 0.5 * gravedadPorDefecto * tiempo * tiempo
        return max(0.0, alturaInicial - desplazado)
    }

    override fun calcularVelocidad(velocidadInicial: Double, tiempo: Double): Double {
        validarParametros(0.0, velocidadInicial, tiempo)
        return velocidadInicial + gravedadPorDefecto * tiempo
    }

    override fun calcularTiempoImpacto(alturaInicial: Double, velocidadInicial: Double): Double {
        validarParametros(alturaInicial, velocidadInicial, 0.0)
        if (alturaInicial == 0.0) return 0.0
        val vf = sqrt(velocidadInicial * velocidadInicial + 2.0 * gravedadPorDefecto * alturaInicial)
        return (vf - velocidadInicial) / gravedadPorDefecto
    }
}

class EstrategiaTiroVertical(
    override val gravedadPorDefecto: Double = PhysicsConstants.GRAVEDAD_TIERRA
) : IEstrategiaCalculo {
    override val nombreEstrategia: String = "\u0054iro \u0056ertical"

    override fun validarParametros(altura: Double, velocidadInicial: Double, tiempo: Double) {
        require(altura >= 0.0 && tiempo >= 0.0)
        require(velocidadInicial >= 0.0 && gravedadPorDefecto > 0.0)
    }

    override fun calcularPosicion(alturaInicial: Double, velocidadInicial: Double, tiempo: Double): Double {
        validarParametros(alturaInicial, velocidadInicial, tiempo)
        val desplazado = velocidadInicial * tiempo - 0.5 * gravedadPorDefecto * tiempo * tiempo
        return max(0.0, alturaInicial + desplazado)
    }

    override fun calcularVelocidad(velocidadInicial: Double, tiempo: Double): Double {
        validarParametros(0.0, velocidadInicial, tiempo)
        return abs(velocidadInicial - gravedadPorDefecto * tiempo)
    }

    override fun calcularTiempoImpacto(alturaInicial: Double, velocidadInicial: Double): Double {
        validarParametros(alturaInicial, velocidadInicial, 0.0)
        if (alturaInicial == 0.0 && velocidadInicial == 0.0) return 0.0
        val disc = velocidadInicial * velocidadInicial + 2.0 * gravedadPorDefecto * alturaInicial
        return (velocidadInicial + sqrt(disc)) / gravedadPorDefecto
    }
}

class EstrategiaGenerica(
    override val nombreEstrategia: String = "\u0047en\u00e9rica",
    override val gravedadPorDefecto: Double = PhysicsConstants.GRAVEDAD_TIERRA
) : IEstrategiaCalculo {

    override fun validarParametros(altura: Double, velocidadInicial: Double, tiempo: Double) {
        require(!altura.isNaN() && !velocidadInicial.isNaN())
        require(!tiempo.isNaN() && gravedadPorDefecto >= 0.0)
    }

    override fun calcularPosicion(alturaInicial: Double, velocidadInicial: Double, tiempo: Double): Double {
        validarParametros(alturaInicial, velocidadInicial, tiempo)
        return max(0.0, alturaInicial)
    }

    override fun calcularVelocidad(velocidadInicial: Double, tiempo: Double): Double {
        validarParametros(0.0, velocidadInicial, tiempo)
        return abs(velocidadInicial)
    }

    override fun calcularTiempoImpacto(alturaInicial: Double, velocidadInicial: Double): Double {
        validarParametros(alturaInicial, velocidadInicial, 0.0)
        return 0.0
    }
}

interface ICalculadoraCaidaLibre {
    val gravedadPorDefecto: Double
    fun calcularAlturaCaidaLibre(tiempo: Double, gravedad: Double = gravedadPorDefecto): Double
    fun calcularVelocidadFinalCaidaLibre(tiempo: Double, gravedad: Double = gravedadPorDefecto): Double
    fun calcularVelocidadPorAltura(altura: Double, gravedad: Double = gravedadPorDefecto): Double
    fun calcularTiempoCaidaLibre(altura: Double, gravedad: Double = gravedadPorDefecto): Double
}

interface ICalculadoraLanzamientoAbajo {
    val gravedadPorDefecto: Double
    fun calcularPosicionVertical(alturaInicial: Double, velocidadInicial: Double, tiempo: Double): Double
    fun calcularVelocidadConVelocidadInicial(
        velocidadInicial: Double,
        tiempo: Double,
        gravedad: Double = gravedadPorDefecto
    ): Double
    fun calcularTiempoConVelocidadInicial(
        altura: Double,
        velocidadInicial: Double,
        gravedad: Double = gravedadPorDefecto
    ): Double
}

interface ICalculadoraTiroVertical : ICalculadoraLanzamientoAbajo {
    fun calcularAlturaMaximaLanzamientoArriba(
        velocidadInicial: Double,
        gravedad: Double = gravedadPorDefecto
    ): Double
    fun calcularTiempoSubida(velocidadInicial: Double, gravedad: Double = gravedadPorDefecto): Double
    fun calcularPosicionLanzamientoArriba(alturaInicial: Double, velocidadInicial: Double, tiempo: Double): Double
}

interface ICalculadoraValidacion {
    fun verificarRespuesta(
        respuestaUsuario: Double,
        respuestaCorrecta: Double,
        toleranciaPorcentaje: Double = PhysicsConstants.TOLERANCIA_PORCENTAJE
    ): Boolean
}

interface ICalculadoraFisica : ICalculadoraCaidaLibre, ICalculadoraTiroVertical, ICalculadoraValidacion {
    fun obtenerEstrategia(direccion: DireccionMovimiento): IEstrategiaCalculo

    companion object {
        fun porDefecto(): ICalculadoraFisica {
            return ctorCalcForm()
        }
    }
}

open class BaseCalculadoraCaidaLibre(
    override val gravedadPorDefecto: Double = PhysicsConstants.GRAVEDAD_TIERRA
) : ICalculadoraCaidaLibre, ICalculadoraValidacion {

    init {
        require(gravedadPorDefecto > 0.0)
    }

    protected fun exigirPositivo(magnitud: Double, gravedad: Double) {
        require(magnitud >= 0.0 && gravedad > 0.0)
    }

    override fun calcularAlturaCaidaLibre(tiempo: Double, gravedad: Double): Double {
        exigirPositivo(tiempo, gravedad)
        return 0.5 * gravedad * tiempo * tiempo
    }

    override fun calcularVelocidadFinalCaidaLibre(tiempo: Double, gravedad: Double): Double {
        exigirPositivo(tiempo, gravedad)
        return gravedad * tiempo
    }

    override fun calcularVelocidadPorAltura(altura: Double, gravedad: Double): Double {
        exigirPositivo(altura, gravedad)
        return sqrt(2.0 * gravedad * altura)
    }

    override fun calcularTiempoCaidaLibre(altura: Double, gravedad: Double): Double {
        exigirPositivo(altura, gravedad)
        return sqrt((2.0 * altura) / gravedad)
    }

    override fun verificarRespuesta(
        respuestaUsuario: Double,
        respuestaCorrecta: Double,
        toleranciaPorcentaje: Double
    ): Boolean {
        if (respuestaUsuario.isNaN() || respuestaUsuario.isInfinite()) return false
        val margen = max(abs(respuestaCorrecta) * toleranciaPorcentaje, PhysicsConstants.TOLERANCIA_ABSOLUTA_MINIMA)
        return abs(respuestaUsuario - respuestaCorrecta) <= margen
    }
}

class CalculadoraFisica(
    gravedadPorDefecto: Double = 9.8
) : BaseCalculadoraCaidaLibre(gravedadPorDefecto), ICalculadoraFisica {

    private val estrategiaCaida: IEstrategiaCalculo = ctorEstrategiaCaidaForm(gravedadPorDefecto)
    private val estrategiaTiro: IEstrategiaCalculo = ctorEstrategiaTiroForm(gravedadPorDefecto)

    override fun obtenerEstrategia(direccion: DireccionMovimiento): IEstrategiaCalculo {
        return direccion.elegirEstrategia(estrategiaCaida, estrategiaTiro)
    }

    override fun calcularPosicionVertical(
        alturaInicial: Double,
        velocidadInicial: Double,
        tiempo: Double
    ): Double {
        return estrategiaCaida.calcularPosicion(alturaInicial, velocidadInicial, tiempo)
    }

    override fun calcularVelocidadConVelocidadInicial(
        velocidadInicial: Double,
        tiempo: Double,
        gravedad: Double
    ): Double {
        exigirPositivo(velocidadInicial, gravedad)
        require(tiempo >= 0.0)
        return velocidadInicial + gravedad * tiempo
    }

    override fun calcularTiempoConVelocidadInicial(
        altura: Double,
        velocidadInicial: Double,
        gravedad: Double
    ): Double {
        exigirPositivo(altura, gravedad)
        require(velocidadInicial >= 0.0)
        if (altura == 0.0) return 0.0
        val vf = sqrt(velocidadInicial * velocidadInicial + 2.0 * gravedad * altura)
        return (vf - velocidadInicial) / gravedad
    }

    override fun calcularAlturaMaximaLanzamientoArriba(velocidadInicial: Double, gravedad: Double): Double {
        exigirPositivo(velocidadInicial, gravedad)
        return (velocidadInicial * velocidadInicial) / (2.0 * gravedad)
    }

    override fun calcularTiempoSubida(velocidadInicial: Double, gravedad: Double): Double {
        exigirPositivo(velocidadInicial, gravedad)
        return velocidadInicial / gravedad
    }

    override fun calcularPosicionLanzamientoArriba(
        alturaInicial: Double,
        velocidadInicial: Double,
        tiempo: Double
    ): Double {
        return estrategiaTiro.calcularPosicion(alturaInicial, velocidadInicial, tiempo)
    }
}

interface IConfiguracionObjetoSimulado {
    val alturaInicial: Double
    val velocidadInicial: Double
    val direccion: DireccionMovimiento
    val estrategiaActiva: IEstrategiaCalculo
    fun tiempoTotalEstimado(): Double
}

interface IObjetoSimulado : IConfiguracionObjetoSimulado {
    val tiempoTranscurrido: Double
    val alturaActual: Double
    val velocidadActual: Double
    val enSuelo: Boolean
    fun actualizar(deltaTiempo: Double)
    fun reiniciar()
    fun progresoCaida(): Double
}

open class BaseObjetoSimulado(
    override val alturaInicial: Double,
    override val velocidadInicial: Double = 0.0,
    override val direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE,
    calculadora: ICalculadoraFisica = crearCalculadoraPredeterminada(),
    estrategiaExplicita: IEstrategiaCalculo? = null
) : IConfiguracionObjetoSimulado {

    override val estrategiaActiva: IEstrategiaCalculo =
        estrategiaExplicita ?: calculadora.obtenerEstrategia(direccion)

    init {
        require(alturaInicial >= 0.0 && velocidadInicial >= 0.0)
    }

    override fun tiempoTotalEstimado(): Double {
        return estrategiaActiva.calcularTiempoImpacto(alturaInicial, velocidadInicial)
    }

    companion object {
        fun crearCalculadoraPredeterminada(): ICalculadoraFisica {
            return ctorCalcForm()
        }

        fun callbackObjetoVacio(obj: ObjetoSimulado) {
            if (obj.alturaInicial < 0.0) obj.reiniciar()
        }

        fun callbackSimpleVacio() {
            return
        }
    }
}

class ObjetoSimulado(
    alturaInicial: Double,
    velocidadInicial: Double = 0.0,
    direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE,
    calculadora: ICalculadoraFisica = crearCalculadoraPredeterminada(),
    estrategiaExplicita: IEstrategiaCalculo? = null
) : BaseObjetoSimulado(alturaInicial, velocidadInicial, direccion, calculadora, estrategiaExplicita), IObjetoSimulado {

    override var tiempoTranscurrido: Double = 0.0
        private set

    override var alturaActual: Double = alturaInicial
        private set

    override var velocidadActual: Double = velocidadInicial
        private set

    override var enSuelo: Boolean = alturaInicial <= 0.0
        private set

    override fun actualizar(deltaTiempo: Double) {
        require(deltaTiempo >= 0.0)
        if (enSuelo) return
        tiempoTranscurrido += deltaTiempo
        sincronizarEstadoCinematica()
    }

    private fun sincronizarEstadoCinematica() {
        alturaActual = estrategiaActiva.calcularPosicion(alturaInicial, velocidadInicial, tiempoTranscurrido)
        velocidadActual = estrategiaActiva.calcularVelocidad(velocidadInicial, tiempoTranscurrido)
        if (alturaActual <= 0.0) {
            alturaActual = 0.0
            enSuelo = true
        }
    }

    override fun reiniciar() {
        tiempoTranscurrido = 0.0
        alturaActual = alturaInicial
        velocidadActual = velocidadInicial
        enSuelo = alturaInicial <= 0.0
    }

    override fun progresoCaida(): Double {
        if (alturaInicial <= 0.0) return 1.0
        val fraccion = 1.0 - (alturaActual / alturaInicial)
        return min(1.0, max(0.0, fraccion))
    }
}
