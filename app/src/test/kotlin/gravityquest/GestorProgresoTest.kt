package gravityquest

import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.Problema
import gravityquest.models.TipoProblema
import javafx.application.Platform
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * ============================================================================
 * Pruebas Unitarias para GestorProgreso y VentanaPista (POO & SOLID)
 * ============================================================================
 */
class GestorProgresoTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupJavaFx() {
            try {
                Platform.startup {}
            } catch (_: IllegalStateException) {
                // JavaFX platform ya iniciada
            }
        }

        fun ejecutarEnFx(bloque: () -> Unit) {
            if (Platform.isFxApplicationThread()) {
                bloque()
            } else {
                val latch = CountDownLatch(1)
                var error: Throwable? = null
                Platform.runLater {
                    try {
                        bloque()
                    } catch (t: Throwable) {
                        error = t
                    } finally {
                        latch.countDown()
                    }
                }
                val completado = latch.await(5, TimeUnit.SECONDS)
                if (!completado) {
                    throw TimeoutException("Tiempo de espera agotado al ejecutar en JavaFX Thread")
                }
                if (error != null) {
                    throw error
                }
            }
        }
    }

    // ========================================================================
    // 1. Pruebas para GestorProgreso (POO, SRP y Reglas de Negocio)
    // ========================================================================

    @Test
    fun testEstadoInicialFacilDesbloqueadoOtrosBloqueados() {
        val repo = GeneradorProblemas()
        val gestor = GestorProgreso(repo)

        assertTrue(gestor.esNivelDesbloqueado(Dificultad.FACIL), "Nivel Fácil debe iniciar desbloqueado por defecto")
        assertFalse(gestor.esNivelDesbloqueado(Dificultad.MEDIO), "Nivel Medio debe iniciar bloqueado")
        assertFalse(gestor.esNivelDesbloqueado(Dificultad.DIFICIL), "Nivel Difícil debe iniciar bloqueado")
        assertEquals(0, gestor.cantidadProblemasCompletados(), "Al inicio no debe haber problemas completados")
    }

    @Test
    fun testDesbloqueoSecuencialCompletandoFacilDesbloqueaMedio() {
        val repo = GeneradorProblemas()
        val gestor = GestorProgreso(repo)

        val problemasFaciles = repo.obtenerPorDificultad(Dificultad.FACIL)
        assertTrue(problemasFaciles.isNotEmpty())

        // Completar todos menos uno
        for (i in 0 until problemasFaciles.size - 1) {
            gestor.registrarProblemaCompletado(problemasFaciles[i].id)
            assertFalse(gestor.esNivelDesbloqueado(Dificultad.MEDIO), "Medio debe seguir bloqueado hasta completar todos")
        }

        // Completar el último
        val ultimo = problemasFaciles.last()
        gestor.registrarVictoria(ultimo)

        assertTrue(gestor.esNivelDesbloqueado(Dificultad.MEDIO), "Completar todos los ejercicios de Fácil debe desbloquear Medio")
        assertFalse(gestor.esNivelDesbloqueado(Dificultad.DIFICIL), "Difícil debe permanecer bloqueado")
    }

    @Test
    fun testDesbloqueoSecuencialCompletandoMedioDesbloqueaDificil() {
        val repo = GeneradorProblemas()
        val gestor = GestorProgreso(repo)

        // Completar Fácil
        repo.obtenerPorDificultad(Dificultad.FACIL).forEach { gestor.registrarProblemaCompletado(it.id) }
        assertTrue(gestor.esNivelDesbloqueado(Dificultad.MEDIO))

        // Completar Medio
        val problemasMedios = repo.obtenerPorDificultad(Dificultad.MEDIO)
        problemasMedios.forEach { gestor.registrarVictoria(it) }

        assertTrue(gestor.esNivelDesbloqueado(Dificultad.DIFICIL), "Completar todos los ejercicios de Medio debe desbloquear Difícil")
    }

    @Test
    fun testEncapsulamientoFuerteEInvariantes() {
        val gestor = GestorProgreso()

        assertThrows<IllegalArgumentException> {
            gestor.registrarProblemaCompletado(0)
        }
        assertThrows<IllegalArgumentException> {
            gestor.registrarProblemaCompletado(-3)
        }

        gestor.registrarProblemaCompletado(1)
        val completados = gestor.problemasCompletadosIds()
        assertTrue(completados.contains(1))
        assertEquals(1, completados.size)
    }

    @Test
    fun testDesbloqueoManualYReinicio() {
        val gestor = GestorProgreso()

        assertFalse(gestor.esNivelDesbloqueado(Dificultad.DIFICIL))
        gestor.desbloquearNivelManualmente(Dificultad.DIFICIL)
        assertTrue(gestor.esNivelDesbloqueado(Dificultad.DIFICIL), "El desbloqueo manual debe habilitar el nivel")

        gestor.reiniciarProgreso()
        assertFalse(gestor.esNivelDesbloqueado(Dificultad.DIFICIL), "Reiniciar progreso debe volver a bloquear")
        assertTrue(gestor.esNivelDesbloqueado(Dificultad.FACIL))
    }

    @Test
    fun testExtensibilidadOCPReglaPersonalizada() {
        val reglaEspecial = ReglaDesbloqueoSecuencial(Dificultad.DIFICIL, Dificultad.MEDIO, minimoRequerido = 1)
        val gestor = GestorProgreso(
            reglasPersonalizadas = listOf(
                ReglaDesbloqueoInicial(Dificultad.FACIL),
                ReglaDesbloqueoInicial(Dificultad.MEDIO),
                reglaEspecial
            )
        )

        assertFalse(gestor.esNivelDesbloqueado(Dificultad.DIFICIL))
        gestor.registrarProblemaCompletado(4) // Problema #4 es Medio
        assertTrue(gestor.esNivelDesbloqueado(Dificultad.DIFICIL), "Regla extensible OCP satisfecha")
    }

    // ========================================================================
    // 2. Pruebas para VentanaPista (SRP, DIP y Formatos de Fórmulas)
    // ========================================================================

    @Test
    fun testVentanaPistaFormulasPorTipo() {
        val formulaCaida = VentanaPista.obtenerFormulasParaTipo(TipoProblema.CAIDA_LIBRE)
        assertTrue(formulaCaida.contains("√(2 · h / g)"))
        assertTrue(formulaCaida.contains("v_f = √(2 · g · h)"))

        val formulaAbajo = VentanaPista.obtenerFormulasParaTipo(TipoProblema.LANZAMIENTO_VERTICAL_ABAJO)
        assertTrue(formulaAbajo.contains("v₀² + 2 · g · h"))

        val formulaArriba = VentanaPista.obtenerFormulasParaTipo(TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA)
        assertTrue(formulaArriba.contains("h_máx = v₀² / (2 · g)"))
        assertTrue(formulaArriba.contains("t_sub = v₀ / g"))

        val formulaMRU = VentanaPista.obtenerFormulasParaTipo(TipoProblema.MRU)
        assertTrue(formulaMRU.contains("d = v · t"))
    }

    @Test
    fun testVentanaPistaInstanciacionModal() {
        ejecutarEnFx {
            val problema = Problema(
                id = 1,
                enunciado = "Objeto en caída libre.",
                dificultad = Dificultad.FACIL,
                valorEsperado = 2.02,
                unidadMedida = "s",
                pista = "t = sqrt(2h/g)",
                tipo = TipoProblema.CAIDA_LIBRE
            )

            val stageModal = VentanaPista.mostrar(null, problema)
            assertNotNull(stageModal)
            assertEquals("Pistas y Fórmulas de Física", stageModal.title)
            assertNotNull(stageModal.scene)
        }
    }
}
