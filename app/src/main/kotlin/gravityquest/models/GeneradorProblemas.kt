package gravityquest.models
import gravityquest.*; import kotlin.math.sqrt; import kotlin.random.Random
typealias IRandom = Random

interface IRepositorioConsulta {
    fun obtenerTodosLosProblemas(): IList<Problema>
    fun obtenerProblemaPorId(id: IInt): Problema?
    fun contarPorDificultad(dificultad: Dificultad): IInt
}

interface IRepositorioProblemas : IRepositorioConsulta {
    fun obtenerProblemasPorDificultad(dificultad: Dificultad): IList<Problema>
    fun obtenerProblemasMezclados(dificultad: Dificultad, generadorAleatorio: IRandom = Random.Default): IList<Problema>
}

interface IFabricaBancoNivel {
    fun construir(): IList<Problema>
}

class FabricaBancoFacil : IFabricaBancoNivel {
    private val g: IDouble = PhysicsConstants.GRAVEDAD_TIERRA

    override fun construir(): IList<Problema> {
        return (1..20).map { indice -> crearProblemaFacil(indice) }
    }

    private fun crearProblemaFacil(indice: IInt): Problema {
        val t = indice * 0.5 + 0.5
        val h = 0.5 * g * t * t
        val resp = if (indice % 2 == 1) t else g * t
        return armarProblemaCaida(indice, h, resp)
    }

    private fun armarProblemaCaida(id: IInt, h: IDouble, resp: IDouble): Problema {
        val inc = obtenerIncognitaFacil(id)
        val uni = obtenerUnidadFacil(id)
        return Problema(
            id = id,
            dificultad = Dificultad.FACIL,
            tipo = TipoProblema.CAIDA_LIBRE,
            enunciado = "\u0053onda #$id cae libremente desde ${"%.2f".format(h)} m \u0028g = 9.8 m/s\u00b2\u0029. \u0048alla $inc.",
            incognita = inc,
            unidadRespuesta = uni,
            alturaInicial = h,
            velocidadInicial = 0.0,
            respuestaCorrecta = resp,
            pista = "\u0041plica t = \u221a\u00282h/g\u0029 o v = g\u00b7t"
        )
    }

    private fun obtenerIncognitaFacil(id: IInt): IString {
        return if (id % 2 == 1) "\u0054iempo de ca\u00edda" else "\u0056elocidad de impacto"
    }

    private fun obtenerUnidadFacil(id: IInt): IString {
        return if (id % 2 == 1) "s" else "m/s"
    }
}

class FabricaBancoMedio : IFabricaBancoNivel {
    private val g: IDouble = PhysicsConstants.GRAVEDAD_TIERRA

    override fun construir(): IList<Problema> {
        return (1..20).map { idx -> crearProblemaMedio(20 + idx, idx) }
    }

    private fun crearProblemaMedio(id: IInt, idx: IInt): Problema {
        val v0 = idx * 2.0 + 3.0
        if (idx % 2 == 1) return crearLanzamientoAbajo(id, idx * 8.0 + 10.0, v0)
        return crearLanzamientoArriba(id, v0)
    }

    private fun crearLanzamientoAbajo(id: IInt, h: IDouble, v0: IDouble): Problema {
        val vf = sqrt(v0 * v0 + 2.0 * g * h)
        return Problema(
            id = id,
            dificultad = Dificultad.MEDIO,
            tipo = TipoProblema.LANZAMIENTO_ABAJO,
            enunciado = "\u0043\u00e1psula #$id se lanza hacia abajo a $v0 m/s desde $h m. \u0048alla su rapidez final.",
            incognita = "\u0056elocidad final",
            unidadRespuesta = "m/s",
            alturaInicial = h,
            velocidadInicial = v0,
            respuestaCorrecta = vf,
            pista = "\u0055sa v_f = \u221a\u0028v\u2080\u00b2 + 2\u00b7g\u00b7h\u0029"
        )
    }

    private fun crearLanzamientoArriba(id: IInt, v0: IDouble): Problema {
        val hMax = (v0 * v0) / (2.0 * g)
        return Problema(
            id = id,
            dificultad = Dificultad.MEDIO,
            tipo = TipoProblema.LANZAMIENTO_ARRIBA,
            enunciado = "\u0050royectil #$id se dispara verticalmente hacia arriba a $v0 m/s. \u0048alla su altura m\u00e1xima.",
            incognita = "\u0041ltura m\u00e1xima",
            unidadRespuesta = "m",
            alturaInicial = hMax,
            velocidadInicial = v0,
            respuestaCorrecta = hMax,
            pista = "\u0055sa h_max = v\u2080\u00b2 / \u00282\u00b7g\u0029"
        )
    }
}

class FabricaBancoDificil : IFabricaBancoNivel {
    private val g: IDouble = PhysicsConstants.GRAVEDAD_TIERRA

    override fun construir(): IList<Problema> {
        return (1..20).map { idx -> crearProblemaDificil(40 + idx, idx) }
    }

    private fun crearProblemaDificil(id: IInt, idx: IInt): Problema {
        val h = idx * 12.5 + 25.0
        val v0 = idx * 3.5 + 5.0
        val vf = sqrt(v0 * v0 + 2.0 * g * h)
        val t = (vf - v0) / g
        return armarProblemaDificil(id, h, v0 to t)
    }

    private fun armarProblemaDificil(id: IInt, h: IDouble, par: IPunto2D): Problema {
        val v0 = par.first
        val t = par.second
        return Problema(
            id = id,
            dificultad = Dificultad.DIFICIL,
            tipo = TipoProblema.LANZAMIENTO_ABAJO,
            enunciado = "\u004deteorito #$id desciende desde $h m con v\u2080 = $v0 m/s. \u0043alcula el tiempo exacto de impacto.",
            incognita = "\u0054iempo de impacto",
            unidadRespuesta = "s",
            alturaInicial = h,
            velocidadInicial = v0,
            respuestaCorrecta = t,
            pista = "\u0043alcula primero v_f = \u221a\u0028v\u2080\u00b2 + 2gh\u0029 y luego t = \u0028v_f - v\u2080\u0029 / g"
        )
    }
}

class GeneradorProblemas(
    private val fabricas: IList<IFabricaBancoNivel> = listOf(
        FabricaBancoFacil(),
        FabricaBancoMedio(),
        FabricaBancoDificil()
    )
) : IRepositorioProblemas {

    private val bancoProblemas: IList<Problema> = fabricas.flatMap { it.construir() }

    override fun obtenerTodosLosProblemas(): IList<Problema> {
        return bancoProblemas.toList()
    }

    override fun obtenerProblemasPorDificultad(dificultad: Dificultad): IList<Problema> {
        return bancoProblemas.filter { it.dificultad == dificultad }
    }

    override fun obtenerProblemasMezclados(
        dificultad: Dificultad,
        generadorAleatorio: IRandom
    ): IList<Problema> {
        return obtenerProblemasPorDificultad(dificultad).shuffled(generadorAleatorio)
    }

    override fun obtenerProblemaPorId(id: IInt): Problema? {
        return bancoProblemas.find { it.id == id }
    }

    override fun contarPorDificultad(dificultad: Dificultad): IInt {
        return bancoProblemas.count { it.dificultad == dificultad }
    }
}
