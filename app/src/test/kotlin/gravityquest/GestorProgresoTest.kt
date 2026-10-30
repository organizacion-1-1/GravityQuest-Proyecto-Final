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

    // ========================================================================
    // 3. Pruebas para el Sistema de Puntuación y MarcadorPuntosView (Issue #17)
    // ========================================================================

    @Test
    fun testSistemaPuntuacionReglasDeNegocio() {
        val gestor = GestorProgreso()
        assertEquals(0, gestor.puntajeActual)
        assertEquals("PUNTAJE: 0000", gestor.obtenerPuntajeFormateado())

        // Respuesta incorrecta con 0 puntos: no debe bajar de 0
        gestor.registrarIntentoFallido(idProblema = 1)
        assertEquals(0, gestor.puntajeActual, "El puntaje no debe bajar de 0")

        // Acierto en problema #1 con intento previo fallido -> +50 puntos
        val p1 = GeneradorProblemas.crearCatalogoBase().first { it.id == 1 }
        gestor.registrarVictoria(p1)
        assertEquals(50, gestor.puntajeActual, "Acierto con intento fallido previo otorga +50 puntos")
        assertEquals("PUNTAJE: 0050", gestor.obtenerPuntajeFormateado())

        // Acierto al primer intento limpio en problema #2 -> +100 puntos
        val p2 = GeneradorProblemas.crearCatalogoBase().first { it.id == 2 }
        gestor.registrarVictoria(p2)
        assertEquals(150, gestor.puntajeActual, "Acierto al primer intento sin pistas otorga +100 puntos")

        // Respuesta incorrecta -> -10 puntos
        gestor.registrarIntentoFallido(idProblema = 3)
        assertEquals(140, gestor.puntajeActual, "Respuesta incorrecta resta 10 puntos")

        // Acierto usando pista en problema #4 -> +50 puntos
        val p4 = GeneradorProblemas.crearCatalogoBase().first { it.id == 4 }
        gestor.registrarPistaUsada(p4.id)
        gestor.registrarVictoria(p4)
        assertEquals(190, gestor.puntajeActual, "Acierto tras consultar pista otorga +50 puntos")
    }

    @Test
    fun testMarcadorPuntosViewActualizacionReactivaObserver() {
        ejecutarEnFx {
            val gestor = GestorProgreso()
            val marcador = MarcadorPuntosView(gestor)

            assertTrue(marcador.styleClass.contains("marcador-puntuacion"))
            assertTrue(marcador.lblPuntos.styleClass.contains("puntos-texto"))
            assertTrue(marcador.lblIncremento.styleClass.contains("puntos-incremento"))
            assertEquals("PUNTAJE: 0000", marcador.lblPuntos.text)

            // Al cambiar el puntaje en el modelo, MarcadorPuntosView debe actualizarse automáticamente
            gestor.registrarAcierto(alPrimerIntentoSinPista = true)
            assertEquals("PUNTAJE: 0100", marcador.lblPuntos.text)
            assertEquals("+100", marcador.lblIncremento.text)

            gestor.registrarIntentoFallido()
            assertEquals("PUNTAJE: 0090", marcador.lblPuntos.text)
            assertEquals("-10", marcador.lblIncremento.text)
        }
    }

    // ========================================================================
    // 4. Pruebas para el Sistema de Vidas, PanelVidasView y Progresión en Cadena (Issue #18)
    // ========================================================================

    @Test
    fun testGestorVidasReglasDeNegocioYEncapsulamiento() {
        val gestorVidas = GestorVidas()
        assertEquals(3, gestorVidas.vidasActuales, "Debe iniciar con 3 vidas")
        assertTrue(gestorVidas.tieneVidas())
        assertFalse(gestorVidas.esGameOver())

        assertEquals(2, gestorVidas.perderVida())
        assertEquals(1, gestorVidas.perderVida())
        assertFalse(gestorVidas.esGameOver())

        assertEquals(0, gestorVidas.perderVida())
        assertTrue(gestorVidas.esGameOver(), "Al llegar a 0 vidas debe reportar Game Over")
        assertFalse(gestorVidas.tieneVidas())

        // No debe bajar de 0 vidas
        assertEquals(0, gestorVidas.perderVida())

        // Reinicio de vidas
        gestorVidas.reiniciarVidas()
        assertEquals(3, gestorVidas.vidasActuales, "reiniciarVidas() debe restaurar las 3 vidas")
        assertFalse(gestorVidas.esGameOver())
    }

    @Test
    fun testPanelVidasViewReaccionAutomaticaIntegerProperty() {
        ejecutarEnFx {
            val gestor = GestorProgreso()
            val panelVidas = PanelVidasView(gestor)

            assertTrue(panelVidas.styleClass.contains("panel-vidas"))
            assertEquals(3, panelVidas.iconosVidas.size, "PanelVidasView debe mostrar 3 íconos de vida")
            assertTrue(panelVidas.iconosVidas.all { it.styleClass.contains("icono-vida") })
            assertTrue(panelVidas.iconosVidas.none { it.styleClass.contains("icono-vida-perdida") })

            // Perder 1 vida -> el último ícono pasa a .icono-vida-perdida
            gestor.perderVida()
            assertEquals(2, panelVidas.vidasMostradas)
            assertFalse(panelVidas.iconosVidas[0].styleClass.contains("icono-vida-perdida"))
            assertFalse(panelVidas.iconosVidas[1].styleClass.contains("icono-vida-perdida"))
            assertTrue(panelVidas.iconosVidas[2].styleClass.contains("icono-vida-perdida"))

            // Perder las 2 vidas restantes
            gestor.perderVida()
            gestor.perderVida()
            assertEquals(0, panelVidas.vidasMostradas)
            assertTrue(panelVidas.iconosVidas.all { it.styleClass.contains("icono-vida-perdida") })

            // Reiniciar vidas -> los 3 íconos recuperan su estado activo
            gestor.reiniciarVidas()
            assertEquals(3, panelVidas.vidasMostradas)
            assertTrue(panelVidas.iconosVidas.none { it.styleClass.contains("icono-vida-perdida") })
        }
    }

    @Test
    fun testVistaJuegoGameOverYReinicioAutomaticoDeNivel() {
        ejecutarEnFx {
            val navegador = NavegacionTest.NavegadorEspia()
            val gestor = GestorProgreso()
            val vista = VistaJuego(Dificultad.FACIL, navegador, gestorProgreso = gestor)

            assertEquals(3, gestor.vidasActuales)

            // Fallo 1 -> quedan 2 vidas
            vista.txtRespuesta.text = "999.0"
            assertFalse(vista.validarRespuesta())
            assertEquals(2, gestor.vidasActuales)

            // Fallo 2 -> queda 1 vida
            vista.txtRespuesta.text = "888.0"
            assertFalse(vista.validarRespuesta())
            assertEquals(1, gestor.vidasActuales)

            // Fallo 3 -> llega a 0 vidas: despliega Game Over y reinicia automáticamente a 3 vidas
            vista.txtRespuesta.text = "777.0"
            assertFalse(vista.validarRespuesta())
            assertEquals(1, vista.reiniciosPorGameOver, "Debe haber ejecutado el reinicio automático del nivel")
            assertEquals(3, gestor.vidasActuales, "Las 3 vidas deben haberse restablecido tras el reinicio")
            assertTrue(
                vista.lblResultado.text.contains("Game Over") || vista.lblResultado.text.contains("Nivel Fallido"),
                "Debe desplegar el mensaje de Game Over / Nivel Fallido"
            )
        }
    }

    @Test
    fun testProgresionEnCadenaFacilAMedioYMedioADificilEnMenuYJuego() {
        ejecutarEnFx {
            val navegador = NavegacionTest.NavegadorEspia()
            val gestor = GestorProgreso()
            val vistaMenu = VistaMenu(navegador, gestor)

            // Al inicio: Medio y Difícil deshabilitados en el menú
            assertFalse(vistaMenu.botonesDificultad[Dificultad.FACIL]!!.isDisable)
            assertTrue(vistaMenu.botonesDificultad[Dificultad.MEDIO]!!.isDisable)
            assertTrue(vistaMenu.botonesDificultad[Dificultad.DIFICIL]!!.isDisable)

            // Resolver ejercicio de Nivel Fácil en VistaJuego
            val juegoFacil = VistaJuego(Dificultad.FACIL, navegador, gestorProgreso = gestor)
            juegoFacil.txtRespuesta.text = juegoFacil.problemaActual.valorEsperado.toString()
            assertTrue(juegoFacil.validarRespuesta())

            // Debe desbloquear inmediatamente el nivel Medio y habilitarlo en el menú
            assertTrue(gestor.esNivelDesbloqueado(Dificultad.MEDIO), "Resolver Fácil debe desbloquear Medio inmediatamente")
            vistaMenu.refrescarEstadoProgreso()
            assertFalse(vistaMenu.botonesDificultad[Dificultad.MEDIO]!!.isDisable, "Botón Medio debe quedar habilitado")
            assertTrue(vistaMenu.botonesDificultad[Dificultad.DIFICIL]!!.isDisable, "Botón Difícil sigue deshabilitado")

            // Avanzar directamente al nivel Medio desde VistaJuego
            assertTrue(juegoFacil.avanzarSiguienteNivel())
            assertEquals(Dificultad.MEDIO, navegador.dificultadJuegoInvocada)

            // Resolver ejercicio de Nivel Medio
            val juegoMedio = VistaJuego(Dificultad.MEDIO, navegador, gestorProgreso = gestor)
            juegoMedio.txtRespuesta.text = juegoMedio.problemaActual.valorEsperado.toString()
            assertTrue(juegoMedio.validarRespuesta())

            // Debe desbloquear inmediatamente el nivel Difícil
            assertTrue(gestor.esNivelDesbloqueado(Dificultad.DIFICIL), "Resolver Medio debe desbloquear Difícil inmediatamente")
            vistaMenu.refrescarEstadoProgreso()
            assertFalse(vistaMenu.botonesDificultad[Dificultad.DIFICIL]!!.isDisable, "Botón Difícil debe quedar habilitado")
        }
    }
}
