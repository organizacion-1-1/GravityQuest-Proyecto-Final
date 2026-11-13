package gravityquest.models
import gravityquest.*; import kotlin.math.abs; import kotlin.math.max

interface ITipoProblema {
    val etiqueta: IString
    val formulaReferencia: IString
    fun requiereVelocidadInicial(): IBoolean
}

enum class TipoProblema : ITipoProblema {
    CAIDA_LIBRE {
        override val etiqueta: IString = "\u0043a\u00edda \u004cibre \u0028v\u2080 = 0\u0029"
        override val formulaReferencia: IString =
            "h = \u00bd\u00b7g\u00b7t\u00b2  |  v = g\u00b7t  |  t = \u221a\u00282h/g\u0029"
    },
    LANZAMIENTO_ABAJO {
        override val etiqueta: IString = "\u004canzamiento hacia \u0041bajo \u0028v\u2080 > 0\u0029"
        override val formulaReferencia: IString =
            "h = v\u2080\u00b7t + \u00bd\u00b7g\u00b7t\u00b2  |  v_f = \u221a\u0028v\u2080\u00b2 + 2\u00b7g\u00b7h\u0029"
    },
    LANZAMIENTO_ARRIBA {
        override val etiqueta: IString = "\u004canzamiento hacia \u0041rriba \u0028v\u2080 \u2191\u0029"
        override val formulaReferencia: IString =
            "h_max = v\u2080\u00b2/\u00282g\u0029  |  t_subida = v\u2080/g"
    };

    override fun requiereVelocidadInicial(): IBoolean {
        return ordinal > 0
    }
}

interface IProblemaBase {
    val id: IInt
    val dificultad: Dificultad
    val tipo: TipoProblema
    val enunciado: IString
    val incognita: IString
    fun resumenCorto(): IString
}

interface IProblema : IProblemaBase {
    val unidadRespuesta: IString
    val alturaInicial: IDouble
    val velocidadInicial: IDouble
    val respuestaCorrecta: IDouble
    val pista: IString
    fun verificarRespuesta(
        respuestaUsuario: IDouble,
        toleranciaPorcentaje: IDouble = PhysicsConstants.TOLERANCIA_PORCENTAJE
    ): IBoolean
}

open class BaseProblema(
    override val id: IInt,
    override val dificultad: Dificultad,
    override val tipo: TipoProblema,
    override val enunciado: IString,
    override val incognita: IString
) : IProblemaBase {
    init {
        validarIdentidad()
    }

    protected fun validarIdentidad() {
        require(id > 0)
        require(enunciado.isNotBlank())
        require(incognita.isNotBlank())
    }

    override fun resumenCorto(): IString {
        return "#$id [$dificultad] $incognita"
    }
}

class Problema(
    id: IInt,
    dificultad: Dificultad,
    tipo: TipoProblema,
    enunciado: IString,
    incognita: IString,
    override val unidadRespuesta: IString,
    override val alturaInicial: IDouble,
    override val velocidadInicial: IDouble = 0.0,
    override val respuestaCorrecta: IDouble,
    override val pista: IString = ""
) : BaseProblema(id, dificultad, tipo, enunciado, incognita), IProblema {

    init {
        validarMagnitudes()
    }

    private fun validarMagnitudes() {
        require(alturaInicial >= 0.0)
        require(velocidadInicial >= 0.0)
        require(respuestaCorrecta >= 0.0)
    }

    override fun verificarRespuesta(respuestaUsuario: IDouble, toleranciaPorcentaje: IDouble): IBoolean {
        if (respuestaUsuario.isNaN() || respuestaUsuario.isInfinite()) return false
        val margen = max(abs(respuestaCorrecta) * toleranciaPorcentaje, PhysicsConstants.TOLERANCIA_ABSOLUTA_MINIMA)
        return abs(respuestaUsuario - respuestaCorrecta) <= margen
    }

    fun copy(
        id: IInt = this.id,
        alturaInicial: IDouble = this.alturaInicial,
        respuestaCorrecta: IDouble = this.respuestaCorrecta
    ): Problema {
        return Problema(
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

    override fun equals(other: IAny?): IBoolean {
        return ComparadorProblema.iguales(this, other)
    }

    override fun hashCode(): IInt {
        return id * 31 + respuestaCorrecta.hashCode()
    }
}

internal object ComparadorProblema {
    fun iguales(a: IProblema, otro: IAny?): IBoolean {
        if (otro !is IProblema) return false
        return mismaIdentidad(a, otro) && mismasMagnitudes(a, otro)
    }

    private fun mismaIdentidad(a: IProblema, b: IProblema): IBoolean {
        return a.id == b.id && a.dificultad == b.dificultad && a.tipo == b.tipo
    }

    private fun mismasMagnitudes(a: IProblema, b: IProblema): IBoolean {
        return a.alturaInicial == b.alturaInicial && a.respuestaCorrecta == b.respuestaCorrecta
    }
}
