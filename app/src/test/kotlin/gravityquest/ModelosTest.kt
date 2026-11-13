package gravityquest
import gravityquest.models.*; import org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test; import kotlin.random.Random
internal typealias ctorProblemaTest = Problema; internal typealias ctorGeneradorTest = GeneradorProblemas; internal typealias ctorRandomTest = Random

class DificultadYProblemaModelTest {

    @Test
    fun testPropiedadesYTransicionDificultad() {
        assertEquals(1, Dificultad.FACIL.multiplicadorPuntos)
        assertEquals(2, Dificultad.MEDIO.multiplicadorPuntos)
        assertEquals(3, Dificultad.DIFICIL.multiplicadorPuntos)
        assertEquals(Dificultad.MEDIO, Dificultad.FACIL.siguiente())
        assertEquals(Dificultad.DIFICIL, Dificultad.MEDIO.siguiente())
        assertNull(Dificultad.DIFICIL.siguiente())
    }

    @Test
    fun testCreacionProblemaYVerificacionRespuesta() {
        val p1 = crearProblemaEjemplo(1)
        val p2 = p1.copy(id = 99)
        assertEquals(1, p1.id)
        assertEquals(99, p2.id)
        assertTrue(p1.verificarRespuesta(2.0))
        assertTrue(p1.verificarRespuesta(2.05))
        assertFalse(p1.verificarRespuesta(3.5))
    }

    @Test
    fun testValidacionesInvariantesProblema() {
        assertThrows(IllegalArgumentException::class.java) {
            crearProblemaInvalido(0, 20.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            crearProblemaInvalido(1, -5.0)
        }
    }

    private fun crearProblemaEjemplo(id: IInt): Problema {
        return ctorProblemaTest(
            id = id,
            dificultad = Dificultad.FACIL,
            tipo = TipoProblema.CAIDA_LIBRE,
            enunciado = "\u004f\u0062jeto cae desde 19.6 m",
            incognita = "\u0054iempo",
            unidadRespuesta = "s",
            alturaInicial = 19.6,
            velocidadInicial = 0.0,
            respuestaCorrecta = 2.0,
            pista = "\u0055sa t = \u221a\u00282h/g\u0029"
        )
    }

    private fun crearProblemaInvalido(id: IInt, altura: IDouble): Problema {
        return ctorProblemaTest(
            id = id,
            dificultad = Dificultad.FACIL,
            tipo = TipoProblema.CAIDA_LIBRE,
            enunciado = "\u0045nunciado",
            incognita = "\u0054iempo",
            unidadRespuesta = "s",
            alturaInicial = altura,
            velocidadInicial = 0.0,
            respuestaCorrecta = 1.0
        )
    }
}

class GeneradorProblemasCatalogoTest {

    private val generador: GeneradorProblemas = ctorGeneradorTest()

    @Test
    fun testCantidadTotalYPorDificultad() {
        val todos = generador.obtenerTodosLosProblemas()
        assertEquals(GameConfig.TOTAL_PROBLEMAS_POR_NIVEL * 3, todos.size)
        for (dif in Dificultad.entries) {
            val lista = generador.obtenerProblemasPorDificultad(dif)
            assertEquals(GameConfig.TOTAL_PROBLEMAS_POR_NIVEL, lista.size)
            assertTrue(lista.all { it.dificultad == dif })
        }
    }

    @Test
    fun testUnicidadDeIdsYEnunciados() {
        val todos = generador.obtenerTodosLosProblemas()
        assertEquals(todos.size, todos.map { it.id }.toSet().size)
        assertEquals(todos.size, todos.map { it.enunciado }.toSet().size)
        val p1 = generador.obtenerProblemaPorId(1)
        assertNotNull(p1)
        assertEquals(Dificultad.FACIL, p1?.dificultad)
        assertNull(generador.obtenerProblemaPorId(9999))
    }

    @Test
    fun testMezclaAleatoriaYCoberturaTipos() {
        val original = generador.obtenerProblemasPorDificultad(Dificultad.MEDIO)
        val mezclados = generador.obtenerProblemasMezclados(Dificultad.MEDIO, Random(42))
        assertEquals(original.map { it.id }.toSet(), mezclados.map { it.id }.toSet())
        val tipos = generador.obtenerTodosLosProblemas().map { it.tipo }.toSet()
        assertTrue(tipos.containsAll(TipoProblema.entries))
    }
}
