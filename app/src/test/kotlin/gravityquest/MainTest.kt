package gravityquest
import gravityquest.models.Dificultad; import org.example.Juego.*; import org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test
internal typealias ctorMainTest = Main; internal typealias ctorRenderConsolaTest = RenderizadorConsola; internal typealias ctorRenderFxTest = RenderizadorJavaFx

class MainArquitecturaTest {

    @Test
    fun testMainExtiendeApplicationYConfiguraModulos() {
        val app = ctorMainTest()
        assertTrue(IApplication::class.isInstance(app))
        assertTrue(GravityQuestApp::class.isInstance(app))
        assertNotNull(app.repositorio)
        assertNotNull(app.calculadora)
        assertNotNull(app.gestorProgreso)
        assertEquals(Dificultad.FACIL, app.dificultadActiva)
        assertTrue(INivelEjercicio::class.isInstance(app.nivelActual))
    }

    @Test
    fun testPolimorfismoNivelesEjercicio() {
        val app = ctorMainTest()
        val pFacil = app.cargarProblemaPorId(1)
        assertNotNull(pFacil)
        val n1 = FabricaNivelEjercicio.desdeProblema(pFacil!!, app.calculadora)
        assertTrue(n1.verificarRespuesta(n1.calcularRespuestaTeorica()))
        val pMedio = app.cargarProblemaPorId(21)
        val n2 = FabricaNivelEjercicio.desdeProblema(pMedio!!, app.calculadora)
        assertTrue(n2.resumenFormativo().isNotBlank())
    }
}

class MainEjecucionTest {

    @Test
    fun testRenderizadoresYFlujoRespuesta() {
        val rc = ctorRenderConsolaTest()
        val rfx = ctorRenderFxTest()
        val app = ctorMainTest(renderizador = rc)
        val prob = app.gestorProgreso.obtenerProblemaActual()!!
        assertTrue(app.responderProblemaActual(prob.respuestaCorrecta))
        assertTrue(rc.ultimoResumenRenderizado.isNotBlank())
        assertTrue(rfx.renderizar(Dificultad.FACIL, prob, null).isNotBlank())
        assertFalse(app.seleccionarDificultad(Dificultad.DIFICIL))
    }

    @Test
    fun testDemostracionConsolaYMainHeadless() {
        val repo = DemostracionConsola.demostrarModelosDominio()
        assertEquals(60, repo.obtenerTodosLosProblemas().size)
        val calc = DemostracionConsola.demostrarMotorFisico()
        assertEquals(9.8, calc.gravedadPorDefecto, 1e-3)
        assertTrue(DemostracionConsola.demostrarNavegacion())
        val gestor = DemostracionConsola.demostrarProgresoJugador()
        assertTrue(gestor.puntajeTotal > 0)
        Launcher.main(arrayOf("--console"))
    }
}
