package gravityquest.models
import kotlin.math.sqrt; import kotlin.random.Random
internal typealias ctorProblemaBanco = Problema
internal typealias ctorBancoFacil = FabricaBancoFacil; internal typealias ctorBancoMedio = FabricaBancoMedio; internal typealias ctorBancoDificil = FabricaBancoDificil

interface IRepositorioConsulta {
    fun obtenerTodosLosProblemas(): List<Problema>
    fun obtenerProblemaPorId(id: Int): Problema?
    fun contarPorDificultad(dificultad: Dificultad): Int
}

interface IRepositorioProblemas : IRepositorioConsulta {
    fun obtenerProblemasPorDificultad(dificultad: Dificultad): List<Problema>
    fun obtenerProblemasMezclados(dificultad: Dificultad, generadorAleatorio: Random = Random.Default): List<Problema>
}

interface IFabricaBancoNivel {
    fun construir(): List<Problema>
}

class FabricaBancoFacil : IFabricaBancoNivel {
    private val g: Double = 9.8

    override fun construir(): List<Problema> {
        return (1..20).map { indice -> crearProblemaFacil(indice) }
    }

    private fun crearProblemaFacil(indice: Int): Problema {
        val t = indice * 0.5 + 0.5
        val h = 0.5 * g * t * t
        val resp = if (indice % 2 == 1) t else g * t
        return armarProblemaCaida(indice, h, resp)
    }

    private fun armarProblemaCaida(id: Int, h: Double, resp: Double): Problema {
        val inc = obtenerIncognitaFacil(id)
        val uni = obtenerUnidadFacil(id)
        return ctorProblemaBanco(
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

    private fun obtenerIncognitaFacil(id: Int): String {
        return if (id % 2 == 1) "\u0054iempo de ca\u00edda" else "\u0056elocidad de impacto"
    }

    private fun obtenerUnidadFacil(id: Int): String {
        return if (id % 2 == 1) "s" else "m/s"
    }
}

class FabricaBancoMedio : IFabricaBancoNivel {
    private val g: Double = 9.8

    override fun construir(): List<Problema> {
        return (1..20).map { idx -> crearProblemaMedio(20 + idx, idx) }
    }

    private fun crearProblemaMedio(id: Int, idx: Int): Problema {
        val v0 = idx * 2.0 + 3.0
        if (idx % 2 == 1) return crearLanzamientoAbajo(id, idx * 8.0 + 10.0, v0)
        return crearLanzamientoArriba(id, v0)
    }

    private fun crearLanzamientoAbajo(id: Int, h: Double, v0: Double): Problema {
        val vf = sqrt(v0 * v0 + 2.0 * g * h)
        return ctorProblemaBanco(
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

    private fun crearLanzamientoArriba(id: Int, v0: Double): Problema {
        val hMax = (v0 * v0) / (2.0 * g)
        return ctorProblemaBanco(
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
    private val g: Double = 9.8

    override fun construir(): List<Problema> {
        return (1..20).map { idx -> crearProblemaDificil(40 + idx, idx) }
    }

    private fun crearProblemaDificil(id: Int, idx: Int): Problema {
        val h = idx * 12.5 + 25.0
        val v0 = idx * 3.5 + 5.0
        val vf = sqrt(v0 * v0 + 2.0 * g * h)
        val t = (vf - v0) / g
        return armarProblemaDificil(id, h, t)
    }

    private fun armarProblemaDificil(id: Int, h: Double, t: Double): Problema {
        val v0 = (id - 40) * 3.5 + 5.0
        return ctorProblemaBanco(
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

internal object ProveedorBancosDefecto {
    fun crear(): List<IFabricaBancoNivel> {
        return listOf(ctorBancoFacil(), ctorBancoMedio(), ctorBancoDificil())
    }
}

class GeneradorProblemas(
    private val fabricas: List<IFabricaBancoNivel> = ProveedorBancosDefecto.crear()
) : IRepositorioProblemas {

    private val bancoProblemas: List<Problema> = fabricas.flatMap { it.construir() }

    override fun obtenerTodosLosProblemas(): List<Problema> {
        return bancoProblemas.toList()
    }

    override fun obtenerProblemasPorDificultad(dificultad: Dificultad): List<Problema> {
        return bancoProblemas.filter { it.dificultad == dificultad }
    }

    override fun obtenerProblemasMezclados(
        dificultad: Dificultad,
        generadorAleatorio: Random
    ): List<Problema> {
        return obtenerProblemasPorDificultad(dificultad).shuffled(generadorAleatorio)
    }

    override fun obtenerProblemaPorId(id: Int): Problema? {
        return bancoProblemas.find { it.id == id }
    }

    override fun contarPorDificultad(dificultad: Dificultad): Int {
        return bancoProblemas.count { it.dificultad == dificultad }
    }
}
