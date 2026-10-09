package gravityquest

import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.Problema
import gravityquest.models.TipoProblema
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pruebas unitarias para validar la arquitectura POO y Principios SOLID de los modelos de dominio:
 * - [Dificultad]: Inmutabilidad, atributos encapsulados, alias DIFÍCIL y validaciones de rango.
 * - [Problema]: Invariantes de dominio, encapsulamiento, OCP y validación matemática de respuestas.
 * - [GeneradorProblemas]: SRP, DIP mediante repositorio, filtrado por dificultad y protección de unicidad.
 */
class ModelosTest {

    // ========================================================================
    // 1. Pruebas para Dificultad (POO & Encapsulamiento)
    // ========================================================================

    @Test
    fun testDificultadesExistentesYPropiedades() {
        val facil = Dificultad.FACIL
        assertEquals("Fácil", facil.nombreVisible)
        assertTrue(facil.descripcion.isNotBlank())
        assertEquals(0.05, facil.margenErrorTolerable)

        val medio = Dificultad.MEDIO
        assertEquals("Medio", medio.nombreVisible)
        assertTrue(medio.descripcion.isNotBlank())
        assertEquals(0.05, medio.margenErrorTolerable)

        val dificil = Dificultad.DIFICIL
        assertEquals("Difícil", dificil.nombreVisible)
        assertTrue(dificil.descripcion.isNotBlank())
        assertEquals(0.05, dificil.margenErrorTolerable)
    }

    @Test
    fun testAliasDificilConTilde() {
        // Valida el alias con tilde requerido en la especificación
        assertEquals(Dificultad.DIFICIL, Dificultad.`DIFÍCIL`)
    }

    @Test
    fun testDificultadDesdeTexto() {
        assertEquals(Dificultad.FACIL, Dificultad.desdeTexto("Fácil"))
        assertEquals(Dificultad.FACIL, Dificultad.desdeTexto("facil"))
        assertEquals(Dificultad.MEDIO, Dificultad.desdeTexto("Medio"))
        assertEquals(Dificultad.DIFICIL, Dificultad.desdeTexto("Difícil"))
        assertEquals(Dificultad.DIFICIL, Dificultad.desdeTexto("DIFICIL"))
        assertNull(Dificultad.desdeTexto("inexistente"))
        assertNull(Dificultad.desdeTexto(null))
        assertNull(Dificultad.desdeTexto("   "))
    }

    // ========================================================================
    // 2. Pruebas para Problema (Invariantes de Dominio, OCP y Validación)
    // ========================================================================

    @Test
    fun testProblemaCreacionExitosaEInmutabilidad() {
        val problema = Problema(
            id = 1,
            enunciado = "Objeto en caída libre de 20m.",
            dificultad = Dificultad.FACIL,
            valorEsperado = 2.02,
            unidadMedida = "s",
            pista = "Usa t = sqrt(2h/g)"
        )

        assertEquals(1, problema.id)
        assertEquals("Objeto en caída libre de 20m.", problema.enunciado)
        assertEquals(Dificultad.FACIL, problema.dificultad)
        assertEquals(2.02, problema.valorEsperado)
        assertEquals("s", problema.unidadMedida)
        assertEquals("Usa t = sqrt(2h/g)", problema.pista)
        assertEquals(TipoProblema.CAIDA_LIBRE, problema.tipo)
        assertEquals("2.02 s", problema.respuestaFormateada())
    }

    @Test
    fun testProblemaValidacionRespuesta() {
        val problema = Problema(
            id = 1,
            enunciado = "Calcula el tiempo de caída.",
            dificultad = Dificultad.FACIL,
            valorEsperado = 2.02,
            unidadMedida = "s",
            pista = "Pista"
        )

        // Respuesta exacta
        assertTrue(problema.validarRespuesta(2.02))

        // Respuestas dentro de tolerancia (±0.05)
        assertTrue(problema.validarRespuesta(2.05))
        assertTrue(problema.validarRespuesta(1.98))

        // Respuestas fuera de tolerancia
        assertFalse(problema.validarRespuesta(2.15))
        assertFalse(problema.validarRespuesta(1.90))

        // Validación con margen de error personalizado
        assertTrue(problema.validarRespuesta(2.20, margenError = 0.20))
        assertFalse(problema.validarRespuesta(2.20, margenError = 0.10))
    }

    @Test
    fun testProblemaInvariantesDeDominioIdInvalido() {
        assertThrows<IllegalArgumentException> {
            Problema(
                id = 0,
                enunciado = "Enunciado válido",
                dificultad = Dificultad.FACIL,
                valorEsperado = 5.0,
                unidadMedida = "s",
                pista = "Pista válida"
            )
        }

        assertThrows<IllegalArgumentException> {
            Problema(
                id = -5,
                enunciado = "Enunciado válido",
                dificultad = Dificultad.FACIL,
                valorEsperado = 5.0,
                unidadMedida = "s",
                pista = "Pista válida"
            )
        }
    }

    @Test
    fun testProblemaInvariantesDeDominioCamposVacios() {
        // Enunciado vacío o con puros espacios
        assertThrows<IllegalArgumentException> {
            Problema(1, "", Dificultad.FACIL, 5.0, "s", "Pista")
        }
        assertThrows<IllegalArgumentException> {
            Problema(1, "   ", Dificultad.FACIL, 5.0, "s", "Pista")
        }

        // Unidad vacía
        assertThrows<IllegalArgumentException> {
            Problema(1, "Enunciado", Dificultad.FACIL, 5.0, "", "Pista")
        }

        // Pista vacía
        assertThrows<IllegalArgumentException> {
            Problema(1, "Enunciado", Dificultad.FACIL, 5.0, "s", "")
        }
    }

    @Test
    fun testProblemaInvariantesDeDominioValoresNumericosInvalidos() {
        assertThrows<IllegalArgumentException> {
            Problema(1, "Enunciado", Dificultad.FACIL, Double.NaN, "s", "Pista")
        }
        assertThrows<IllegalArgumentException> {
            Problema(1, "Enunciado", Dificultad.FACIL, Double.POSITIVE_INFINITY, "s", "Pista")
        }
    }

    @Test
    fun testProblemaPrincipioAbiertoCerradoOCP() {
        // Extensibilidad con tipos de física diferentes (Lanzamiento hacia arriba, MRU, etc.)
        val problemaMRU = Problema(
            id = 10,
            enunciado = "Un rover viaja a velocidad constante de 15 m/s durante 8 s.",
            dificultad = Dificultad.FACIL,
            valorEsperado = 120.0,
            unidadMedida = "m",
            pista = "d = v * t",
            tipo = TipoProblema.MRU,
            datosAdicionales = mapOf("velocidad" to 15.0, "tiempo" to 8.0)
        )

        assertEquals(TipoProblema.MRU, problemaMRU.tipo)
        assertEquals(15.0, problemaMRU.datosAdicionales["velocidad"])
        assertTrue(problemaMRU.validarRespuesta(120.0))
    }

    // ========================================================================
    // 3. Pruebas para GeneradorProblemas (SRP, Repositorio & Filtrado)
    // ========================================================================

    @Test
    fun testGeneradorProblemasCatalogoInicial() {
        val generador = GeneradorProblemas()
        assertTrue(generador.cantidadTotal() >= 6, "El catálogo base debe contener al menos 6 problemas")

        val faciles = generador.obtenerPorDificultad(Dificultad.FACIL)
        val medios = generador.obtenerPorDificultad(Dificultad.MEDIO)
        val dificiles = generador.obtenerPorDificultad(Dificultad.DIFICIL)

        assertTrue(faciles.isNotEmpty(), "Debe haber problemas de nivel FÁCIL")
        assertTrue(medios.isNotEmpty(), "Debe haber problemas de nivel MEDIO")
        assertTrue(dificiles.isNotEmpty(), "Debe haber problemas de nivel DIFÍCIL")

        // Comprueba que todos los problemas filtrados pertenezcan a la dificultad solicitada
        faciles.forEach { assertEquals(Dificultad.FACIL, it.dificultad) }
        medios.forEach { assertEquals(Dificultad.MEDIO, it.dificultad) }
        dificiles.forEach { assertEquals(Dificultad.DIFICIL, it.dificultad) }
    }

    @Test
    fun testGeneradorProblemasObtenerPorId() {
        val generador = GeneradorProblemas()
        val problema1 = generador.obtenerPorId(1)
        assertNotNull(problema1)
        assertEquals(1, problema1.id)
        assertEquals(Dificultad.FACIL, problema1.dificultad)

        val inexistente = generador.obtenerPorId(9999)
        assertNull(inexistente)
    }

    @Test
    fun testGeneradorProblemasAgregarYEvitarDuplicados() {
        val generador = GeneradorProblemas(emptyList())
        assertEquals(0, generador.cantidadTotal())

        val p1 = Problema(10, "Problema A", Dificultad.FACIL, 5.0, "s", "Pista")
        generador.agregarProblema(p1)
        assertEquals(1, generador.cantidadTotal())

        // Intentar agregar problema con ID repetido debe lanzar excepción
        val pDuplicado = Problema(10, "Problema B", Dificultad.MEDIO, 8.0, "m", "Otra pista")
        assertThrows<IllegalArgumentException> {
            generador.agregarProblema(pDuplicado)
        }
    }

    @Test
    fun testGeneradorProblemasObtenerAleatorio() {
        val generador = GeneradorProblemas()
        val aleatorio = generador.obtenerProblemaAleatorio()
        assertNotNull(aleatorio)

        val aleatorioFacil = generador.obtenerProblemaAleatorio(Dificultad.FACIL)
        assertNotNull(aleatorioFacil)
        assertEquals(Dificultad.FACIL, aleatorioFacil.dificultad)
    }

    // ========================================================================
    // 4. Prueba de la función de Demostración
    // ========================================================================

    @Test
    fun testDemostrarModelosDominioEjecucionExitosa() {
        val generador = GeneradorProblemas()
        val resultado = demostrarModelosDominio(generador)
        assertNotNull(resultado)
        assertTrue(resultado.cantidadTotal() > 0)
    }
}
