package gravityquest
import gravityquest.models.Dificultad; import javafx.application.Application
import org.example.Juego.DemostracionConsola; import org.example.Juego.FabricaNivelEjercicio; import org.example.Juego.GravityQuestApp
import org.example.Juego.INivelEjercicio; import org.example.Juego.Main; import org.example.Juego.RenderizadorConsola; import org.example.Juego.RenderizadorJavaFx
import org.junit.jupiter.api.Assertions.assertEquals; import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull; import org.junit.jupiter.api.Assertions.assertTrue; import org.junit.jupiter.api.Test
internal typealias ctorMainTest = Main; internal typealias ctorRenderConsolaTest = RenderizadorConsola; internal typealias ctorRenderFxTest = RenderizadorJavaFx

class MainArquitecturaTest {

    @Test
    fun testMainExtiendeApplicationYConfiguraModulos() {
        val app = ctorMainTest()
        assertTrue(Application::class.isInstance(app))
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
