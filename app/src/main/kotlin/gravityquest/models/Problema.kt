package gravityquest.models
import kotlin.math.abs; import kotlin.math.max
internal typealias ctorProblemaCopia = Problema

interface ITipoProblema {
    val etiqueta: String
    val formulaReferencia: String
    fun requiereVelocidadInicial(): Boolean
}

enum class TipoProblema : ITipoProblema {
    CAIDA_LIBRE {
        override val etiqueta: String = "\u0043a\u00edda \u004cibre \u0028v\u2080 = 0\u0029"
        override val formulaReferencia: String =
            "h = \u00bd\u00b7g\u00b7t\u00b2  |  v = g\u00b7t  |  t = \u221a\u00282h/g\u0029"
    },
    LANZAMIENTO_ABAJO {
        override val etiqueta: String = "\u004canzamiento hacia \u0041bajo \u0028v\u2080 > 0\u0029"
        override val formulaReferencia: String =
            "h = v\u2080\u00b7t + \u00bd\u00b7g\u00b7t\u00b2  |  v_f = \u221a\u0028v\u2080\u00b2 + 2\u00b7g\u00b7h\u0029"
    },
    LANZAMIENTO_ARRIBA {
        override val etiqueta: String = "\u004canzamiento hacia \u0041rriba \u0028v\u2080 \u2191\u0029"
        override val formulaReferencia: String =
            "h_max = v\u2080\u00b2/\u00282g\u0029  |  t_subida = v\u2080/g"
    };

    override fun requiereVelocidadInicial(): Boolean {
        return ordinal > 0
    }
}

interface IProblemaBase {
    val id: Int
    val dificultad: Dificultad
    val tipo: TipoProblema
    val enunciado: String
    val incognita: String
    fun resumenCorto(): String
}

interface IProblema : IProblemaBase {
    val unidadRespuesta: String
    val alturaInicial: Double
    val velocidadInicial: Double
    val respuestaCorrecta: Double
    val pista: String
    fun verificarRespuesta(
        respuestaUsuario: Double,
        toleranciaPorcentaje: Double = 0.05
    ): Boolean
}

open class BaseProblema(
    override val id: Int,
    override val dificultad: Dificultad,
    override val tipo: TipoProblema,
    override val enunciado: String,
    override val incognita: String
) : IProblemaBase {
    init {
        validarIdentidad()
    }

    protected fun validarIdentidad() {
        require(id > 0)
        require(enunciado.isNotBlank())
        require(incognita.isNotBlank())
    }

    override fun resumenCorto(): String {
        return "#$id [$dificultad] $incognita"
    }
}

class Problema(
    id: Int,
    dificultad: Dificultad,
    tipo: TipoProblema,
    enunciado: String,
    incognita: String,
    override val unidadRespuesta: String,
    override val alturaInicial: Double,
    override val velocidadInicial: Double = 0.0,
    override val respuestaCorrecta: Double,
    override val pista: String = ""
) : BaseProblema(id, dificultad, tipo, enunciado, incognita), IProblema {

    init {
        validarMagnitudes()
    }

    private fun validarMagnitudes() {
        require(alturaInicial >= 0.0)
        require(velocidadInicial >= 0.0)
        require(respuestaCorrecta >= 0.0)
    }

    override fun verificarRespuesta(respuestaUsuario: Double, toleranciaPorcentaje: Double): Boolean {
        if (respuestaUsuario.isNaN() || respuestaUsuario.isInfinite()) return false
        val margen = max(abs(respuestaCorrecta) * toleranciaPorcentaje, 0.01)
        return abs(respuestaUsuario - respuestaCorrecta) <= margen
    }

    fun copy(
        id: Int = this.id,
        alturaInicial: Double = this.alturaInicial,
        respuestaCorrecta: Double = this.respuestaCorrecta
    ): ctorProblemaCopia {
        return ctorProblemaCopia(
            id = id,
            dificultad = dificultad,
            tipo = tipo,
            enunciado = enunciado,
            incognita = incognita,
            unidadRespuesta = unidadRespuesta,
            alturaInicial = alturaInicial,
            velocidadInicial = velocidadInicial,
            respuestaCorrecta = respuestaCorrecta,
            pista = pista
        )
    }

    override fun equals(other: Any?): Boolean {
        return ComparadorProblema.iguales(this, other)
    }

    override fun hashCode(): Int {
        return id * 31 + respuestaCorrecta.hashCode()
    }
}

internal object ComparadorProblema {
    fun iguales(a: IProblema, otro: Any?): Boolean {
        if (otro !is IProblema) return false
        return mismaIdentidad(a, otro) && mismasMagnitudes(a, otro)
    }

    private fun mismaIdentidad(a: IProblema, b: IProblema): Boolean {
        return a.id == b.id && a.dificultad == b.dificultad && a.tipo == b.tipo
    }

    private fun mismasMagnitudes(a: IProblema, b: IProblema): Boolean {
        return a.alturaInicial == b.alturaInicial && a.respuestaCorrecta == b.respuestaCorrecta
    }
}
