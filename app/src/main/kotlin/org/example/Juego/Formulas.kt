package org.example.Juego
import gravityquest.*; import kotlin.math.*
internal typealias ctorEstrategiaCaidaForm = EstrategiaCaidaLibre; internal typealias ctorEstrategiaTiroForm = EstrategiaTiroVertical

interface IDireccionMovimiento {
    val signoVertical: IDouble
    val descripcion: IString
    fun esAscendenteInicial(): IBoolean
    fun elegirEstrategia(caida: IEstrategiaCalculo, tiro: IEstrategiaCalculo): IEstrategiaCalculo
}

enum class DireccionMovimiento : IDireccionMovimiento {
    CAIDA_LIBRE {
        override val signoVertical: IDouble = 1.0
        override val descripcion: IString = "\u0043a\u00edda \u004cibre"
        override fun elegirEstrategia(caida: IEstrategiaCalculo, tiro: IEstrategiaCalculo): IEstrategiaCalculo {
            return caida
        }
    },
    LANZAMIENTO_ABAJO {
        override val signoVertical: IDouble = 1.0
        override val descripcion: IString = "\u004canzamiento \u0056ertical hacia \u0041bajo"
        override fun elegirEstrategia(caida: IEstrategiaCalculo, tiro: IEstrategiaCalculo): IEstrategiaCalculo {
            return caida
        }
    },
    LANZAMIENTO_ARRIBA {
        override val signoVertical: IDouble = -1.0
        override val descripcion: IString = "\u004canzamiento \u0056ertical hacia \u0041rriba"
        override fun elegirEstrategia(caida: IEstrategiaCalculo, tiro: IEstrategiaCalculo): IEstrategiaCalculo {
            return tiro
        }
    };

    override fun esAscendenteInicial(): IBoolean {
        return signoVertical < 0.0
    }
}

interface IEstrategiaCalculo {
    val nombreEstrategia: IString
    val gravedadPorDefecto: IDouble
    fun validarParametros(altura: IDouble, velocidadInicial: IDouble, tiempo: IDouble)
    fun calcularPosicion(alturaInicial: IDouble, velocidadInicial: IDouble, tiempo: IDouble): IDouble
    fun calcularVelocidad(velocidadInicial: IDouble, tiempo: IDouble): IDouble
    fun calcularTiempoImpacto(alturaInicial: IDouble, velocidadInicial: IDouble): IDouble
}

class EstrategiaMRU(
    override val gravedadPorDefecto: IDouble = 0.0
) : IEstrategiaCalculo {
    override val nombreEstrategia: IString = "MRU"

    override fun validarParametros(altura: IDouble, velocidadInicial: IDouble, tiempo: IDouble) {
        require(altura >= 0.0 && tiempo >= 0.0)
        require(velocidadInicial >= 0.0)
    }

    override fun calcularPosicion(alturaInicial: IDouble, velocidadInicial: IDouble, tiempo: IDouble): IDouble {
        validarParametros(alturaInicial, velocidadInicial, tiempo)
        return max(0.0, alturaInicial - velocidadInicial * tiempo)
    }

    override fun calcularVelocidad(velocidadInicial: IDouble, tiempo: IDouble): IDouble {
        validarParametros(0.0, velocidadInicial, tiempo)
        return velocidadInicial
    }

    override fun calcularTiempoImpacto(alturaInicial: IDouble, velocidadInicial: IDouble): IDouble {
        validarParametros(alturaInicial, velocidadInicial, 0.0)
        if (alturaInicial == 0.0 || velocidadInicial <= 0.0) return 0.0
        return alturaInicial / velocidadInicial
    }
}

class EstrategiaCaidaLibre(
    override val gravedadPorDefecto: IDouble = PhysicsConstants.GRAVEDAD_TIERRA
) : IEstrategiaCalculo {
    override val nombreEstrategia: IString = "\u0043a\u00edda \u004cibre"

    override fun validarParametros(altura: IDouble, velocidadInicial: IDouble, tiempo: IDouble) {
        require(altura >= 0.0 && tiempo >= 0.0)
        require(velocidadInicial >= 0.0 && gravedadPorDefecto > 0.0)
    }

    override fun calcularPosicion(alturaInicial: IDouble, velocidadInicial: IDouble, tiempo: IDouble): IDouble {
        validarParametros(alturaInicial, velocidadInicial, tiempo)
        val desplazado = velocidadInicial * tiempo + 0.5 * gravedadPorDefecto * tiempo * tiempo
        return max(0.0, alturaInicial - desplazado)
    }

    override fun calcularVelocidad(velocidadInicial: IDouble, tiempo: IDouble): IDouble {
        validarParametros(0.0, velocidadInicial, tiempo)
        return velocidadInicial + gravedadPorDefecto * tiempo
    }

    override fun calcularTiempoImpacto(alturaInicial: IDouble, velocidadInicial: IDouble): IDouble {
        validarParametros(alturaInicial, velocidadInicial, 0.0)
        if (alturaInicial == 0.0) return 0.0
        val vf = sqrt(velocidadInicial * velocidadInicial + 2.0 * gravedadPorDefecto * alturaInicial)
        return (vf - velocidadInicial) / gravedadPorDefecto
    }
}

class EstrategiaTiroVertical(
    override val gravedadPorDefecto: IDouble = PhysicsConstants.GRAVEDAD_TIERRA
) : IEstrategiaCalculo {
    override val nombreEstrategia: IString = "\u0054iro \u0056ertical"

    override fun validarParametros(altura: IDouble, velocidadInicial: IDouble, tiempo: IDouble) {
        require(altura >= 0.0 && tiempo >= 0.0)
        require(velocidadInicial >= 0.0 && gravedadPorDefecto > 0.0)
    }

    override fun calcularPosicion(alturaInicial: IDouble, velocidadInicial: IDouble, tiempo: IDouble): IDouble {
        validarParametros(alturaInicial, velocidadInicial, tiempo)
        val desplazado = velocidadInicial * tiempo - 0.5 * gravedadPorDefecto * tiempo * tiempo
        return max(0.0, alturaInicial + desplazado)
    }

    override fun calcularVelocidad(velocidadInicial: IDouble, tiempo: IDouble): IDouble {
        validarParametros(0.0, velocidadInicial, tiempo)
        return abs(velocidadInicial - gravedadPorDefecto * tiempo)
    }

    override fun calcularTiempoImpacto(alturaInicial: IDouble, velocidadInicial: IDouble): IDouble {
        validarParametros(alturaInicial, velocidadInicial, 0.0)
        if (alturaInicial == 0.0 && velocidadInicial == 0.0) return 0.0
        val disc = velocidadInicial * velocidadInicial + 2.0 * gravedadPorDefecto * alturaInicial
        return (velocidadInicial + sqrt(disc)) / gravedadPorDefecto
    }
}

class EstrategiaGenerica(
    override val nombreEstrategia: IString = "\u0047en\u00e9rica",
    override val gravedadPorDefecto: IDouble = PhysicsConstants.GRAVEDAD_TIERRA
) : IEstrategiaCalculo {

    override fun validarParametros(altura: IDouble, velocidadInicial: IDouble, tiempo: IDouble) {
        require(!altura.isNaN() && !velocidadInicial.isNaN())
        require(!tiempo.isNaN() && gravedadPorDefecto >= 0.0)
    }

    override fun calcularPosicion(alturaInicial: IDouble, velocidadInicial: IDouble, tiempo: IDouble): IDouble {
        validarParametros(alturaInicial, velocidadInicial, tiempo)
        return max(0.0, alturaInicial)
    }

    override fun calcularVelocidad(velocidadInicial: IDouble, tiempo: IDouble): IDouble {
        validarParametros(0.0, velocidadInicial, tiempo)
        return abs(velocidadInicial)
    }

    override fun calcularTiempoImpacto(alturaInicial: IDouble, velocidadInicial: IDouble): IDouble {
        validarParametros(alturaInicial, velocidadInicial, 0.0)
        return 0.0
    }
}

interface ICalculadoraCaidaLibre {
    val gravedadPorDefecto: IDouble
    fun calcularAlturaCaidaLibre(tiempo: IDouble, gravedad: IDouble = gravedadPorDefecto): IDouble
    fun calcularVelocidadFinalCaidaLibre(tiempo: IDouble, gravedad: IDouble = gravedadPorDefecto): IDouble
    fun calcularVelocidadPorAltura(altura: IDouble, gravedad: IDouble = gravedadPorDefecto): IDouble
    fun calcularTiempoCaidaLibre(altura: IDouble, gravedad: IDouble = gravedadPorDefecto): IDouble
}

interface ICalculadoraLanzamientoAbajo {
    val gravedadPorDefecto: IDouble
    fun calcularPosicionVertical(alturaInicial: IDouble, velocidadInicial: IDouble, tiempo: IDouble): IDouble
    fun calcularVelocidadConVelocidadInicial(
        velocidadInicial: IDouble,
        tiempo: IDouble,
        gravedad: IDouble = gravedadPorDefecto
    ): IDouble
    fun calcularTiempoConVelocidadInicial(
        altura: IDouble,
        velocidadInicial: IDouble,
        gravedad: IDouble = gravedadPorDefecto
    ): IDouble
}

interface ICalculadoraTiroVertical : ICalculadoraLanzamientoAbajo {
    fun calcularAlturaMaximaLanzamientoArriba(
        velocidadInicial: IDouble,
        gravedad: IDouble = gravedadPorDefecto
    ): IDouble
    fun calcularTiempoSubida(velocidadInicial: IDouble, gravedad: IDouble = gravedadPorDefecto): IDouble
    fun calcularPosicionLanzamientoArriba(alturaInicial: IDouble, velocidadInicial: IDouble, tiempo: IDouble): IDouble
}

interface ICalculadoraValidacion {
    fun verificarRespuesta(
        respuestaUsuario: IDouble,
        respuestaCorrecta: IDouble,
        toleranciaPorcentaje: IDouble = PhysicsConstants.TOLERANCIA_PORCENTAJE
    ): IBoolean
}

interface ICalculadoraFisica : ICalculadoraCaidaLibre, ICalculadoraTiroVertical, ICalculadoraValidacion {
    fun obtenerEstrategia(direccion: DireccionMovimiento): IEstrategiaCalculo
}

open class BaseCalculadoraCaidaLibre(
    override val gravedadPorDefecto: IDouble = PhysicsConstants.GRAVEDAD_TIERRA
) : ICalculadoraCaidaLibre, ICalculadoraValidacion {

    init {
        require(gravedadPorDefecto > 0.0)
    }

    protected fun exigirPositivo(magnitud: IDouble, gravedad: IDouble) {
        require(magnitud >= 0.0 && gravedad > 0.0)
    }

    override fun calcularAlturaCaidaLibre(tiempo: IDouble, gravedad: IDouble): IDouble {
        exigirPositivo(tiempo, gravedad)
        return 0.5 * gravedad * tiempo * tiempo
    }

    override fun calcularVelocidadFinalCaidaLibre(tiempo: IDouble, gravedad: IDouble): IDouble {
        exigirPositivo(tiempo, gravedad)
        return gravedad * tiempo
    }

    override fun calcularVelocidadPorAltura(altura: IDouble, gravedad: IDouble): IDouble {
        exigirPositivo(altura, gravedad)
        return sqrt(2.0 * gravedad * altura)
    }

    override fun calcularTiempoCaidaLibre(altura: IDouble, gravedad: IDouble): IDouble {
        exigirPositivo(altura, gravedad)
        return sqrt((2.0 * altura) / gravedad)
    }

    override fun verificarRespuesta(
        respuestaUsuario: IDouble,
        respuestaCorrecta: IDouble,
        toleranciaPorcentaje: IDouble
    ): IBoolean {
        if (respuestaUsuario.isNaN() || respuestaUsuario.isInfinite()) return false
        val margen = max(abs(respuestaCorrecta) * toleranciaPorcentaje, PhysicsConstants.TOLERANCIA_ABSOLUTA_MINIMA)
        return abs(respuestaUsuario - respuestaCorrecta) <= margen
    }
}

class CalculadoraFisica(
    gravedadPorDefecto: IDouble = PhysicsConstants.GRAVEDAD_TIERRA
) : BaseCalculadoraCaidaLibre(gravedadPorDefecto), ICalculadoraFisica {

    private val estrategiaCaida: IEstrategiaCalculo = ctorEstrategiaCaidaForm(gravedadPorDefecto)
    private val estrategiaTiro: IEstrategiaCalculo = ctorEstrategiaTiroForm(gravedadPorDefecto)

    override fun obtenerEstrategia(direccion: DireccionMovimiento): IEstrategiaCalculo {
        return direccion.elegirEstrategia(estrategiaCaida, estrategiaTiro)
    }

    override fun calcularPosicionVertical(
        alturaInicial: IDouble,
        velocidadInicial: IDouble,
        tiempo: IDouble
    ): IDouble {
        return estrategiaCaida.calcularPosicion(alturaInicial, velocidadInicial, tiempo)
    }

    override fun calcularVelocidadConVelocidadInicial(
        velocidadInicial: IDouble,
        tiempo: IDouble,
        gravedad: IDouble
    ): IDouble {
        exigirPositivo(velocidadInicial, gravedad)
        require(tiempo >= 0.0)
        return velocidadInicial + gravedad * tiempo
    }

    override fun calcularTiempoConVelocidadInicial(
        altura: IDouble,
        velocidadInicial: IDouble,
        gravedad: IDouble
    ): IDouble {
        exigirPositivo(altura, gravedad)
        require(velocidadInicial >= 0.0)
        if (altura == 0.0) return 0.0
        val vf = sqrt(velocidadInicial * velocidadInicial + 2.0 * gravedad * altura)
        return (vf - velocidadInicial) / gravedad
    }

    override fun calcularAlturaMaximaLanzamientoArriba(velocidadInicial: IDouble, gravedad: IDouble): IDouble {
        exigirPositivo(velocidadInicial, gravedad)
        return (velocidadInicial * velocidadInicial) / (2.0 * gravedad)
    }

    override fun calcularTiempoSubida(velocidadInicial: IDouble, gravedad: IDouble): IDouble {
        exigirPositivo(velocidadInicial, gravedad)
        return velocidadInicial / gravedad
    }

    override fun calcularPosicionLanzamientoArriba(
        alturaInicial: IDouble,
        velocidadInicial: IDouble,
        tiempo: IDouble
    ): IDouble {
        return estrategiaTiro.calcularPosicion(alturaInicial, velocidadInicial, tiempo)
    }
}

interface IConfiguracionObjetoSimulado {
    val alturaInicial: IDouble
    val velocidadInicial: IDouble
    val direccion: DireccionMovimiento
    val estrategiaActiva: IEstrategiaCalculo
    fun tiempoTotalEstimado(): IDouble
}

interface IObjetoSimulado : IConfiguracionObjetoSimulado {
    val tiempoTranscurrido: IDouble
    val alturaActual: IDouble
    val velocidadActual: IDouble
    val enSuelo: IBoolean
    fun actualizar(deltaTiempo: IDouble)
    fun reiniciar()
    fun progresoCaida(): IDouble
}

open class BaseObjetoSimulado(
    override val alturaInicial: IDouble,
    override val velocidadInicial: IDouble = 0.0,
    override val direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE,
    calculadora: ICalculadoraFisica = crearCalculadoraPredeterminada(),
    estrategiaExplicita: IEstrategiaCalculo? = null
) : IConfiguracionObjetoSimulado {

    override val estrategiaActiva: IEstrategiaCalculo =
        estrategiaExplicita ?: calculadora.obtenerEstrategia(direccion)

    init {
        require(alturaInicial >= 0.0 && velocidadInicial >= 0.0)
    }

    override fun tiempoTotalEstimado(): IDouble {
        return estrategiaActiva.calcularTiempoImpacto(alturaInicial, velocidadInicial)
    }

    companion object {
        internal fun crearCalculadoraPredeterminada(): ICalculadoraFisica {
            return CalculadoraFisica()
        }
    }
}

class ObjetoSimulado(
    alturaInicial: IDouble,
    velocidadInicial: IDouble = 0.0,
    direccion: DireccionMovimiento = DireccionMovimiento.CAIDA_LIBRE,
    calculadora: ICalculadoraFisica = crearCalculadoraPredeterminada(),
    estrategiaExplicita: IEstrategiaCalculo? = null
) : BaseObjetoSimulado(alturaInicial, velocidadInicial, direccion, calculadora, estrategiaExplicita), IObjetoSimulado {

    override var tiempoTranscurrido: IDouble = 0.0
        private set

    override var alturaActual: IDouble = alturaInicial
        private set

    override var velocidadActual: IDouble = velocidadInicial
        private set

    override var enSuelo: IBoolean = alturaInicial <= 0.0
        private set

    override fun actualizar(deltaTiempo: IDouble) {
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

    override fun progresoCaida(): IDouble {
        if (alturaInicial <= 0.0) return 1.0
        val fraccion = 1.0 - (alturaActual / alturaInicial)
        return min(1.0, max(0.0, fraccion))
    }
}
