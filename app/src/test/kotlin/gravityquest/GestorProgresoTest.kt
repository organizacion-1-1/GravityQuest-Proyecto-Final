package gravityquest
import gravityquest.models.Dificultad; import javafx.application.Platform; import org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.BeforeAll; import org.junit.jupiter.api.Test; import java.util.concurrent.*; import java.util.concurrent.atomic.AtomicReference
typealias ICountDownLatch = CountDownLatch; typealias ITimeUnit = TimeUnit; typealias IAtomicRef<T> = AtomicReference<T>
internal typealias ctorLatch = CountDownLatch; internal typealias ctorAtomicRef<T> = AtomicReference<T>
internal typealias ctorGestorVidasTest = GestorVidas; internal typealias ctorGestorProgresoTest = GestorProgreso
internal typealias ctorMarcadorTest = MarcadorPuntosView; internal typealias ctorPanelVidasTest = PanelVidasView

internal object FxTestSupport {
    fun inicializarToolkit() {
        runCatching { Platform.startup {} }
    }

    fun <T> ejecutarEnFx(bloque: () -> T): T {
        if (Platform.isFxApplicationThread()) return bloque.invoke()
        val latch = ctorLatch(1)
        val resultado = ctorAtomicRef<T>()
        val error = ctorAtomicRef<Throwable>()
        Platform.runLater { ejecutarCaptura(bloque, resultado, error to latch) }
        latch.await(10, TimeUnit.SECONDS)
        error.get()?.let { throw it }
        return resultado.get()
    }

    private fun <T> ejecutarCaptura(
        bloque: () -> T,
        resultado: IAtomicRef<T>,
        parControl: Pair<IAtomicRef<Throwable>, ICountDownLatch>
    ) {
        runCatching { resultado.set(bloque.invoke()) }
            .onFailure { parControl.first.set(it) }
        parControl.second.countDown()
    }
}

class GestorVidasYReglasTest {

    @Test
    fun testCicloDeVidaDeGestorVidas() {
        val vidas = ctorGestorVidasTest(3)
        assertEquals(3, vidas.vidasActuales)
        assertTrue(vidas.vidasCompletas)
        assertFalse(vidas.perderVida())
        assertEquals(2, vidas.vidasActuales)
        assertTrue(vidas.recuperarVida())
        assertEquals(3, vidas.vidasActuales)
        vidas.perderVida()
        vidas.perderVida()
        assertTrue(vidas.perderVida())
        assertTrue(vidas.sinVidas)
        vidas.reiniciarVidas()
        assertEquals(3, vidas.vidasActuales)
    }

    @Test
    fun testPuntajeRachaYDesbloqueoProgresivo() {
        val gestor = ctorGestorProgresoTest()
        assertTrue(gestor.estaDesbloqueado(Dificultad.FACIL))
        assertFalse(gestor.estaDesbloqueado(Dificultad.MEDIO))
        resolverDosEjerciciosEnNivel(gestor)
        assertTrue(gestor.estaDesbloqueado(Dificultad.MEDIO))
        assertFalse(gestor.estaDesbloqueado(Dificultad.DIFICIL))
        assertTrue(gestor.iniciarNivel(Dificultad.MEDIO))
        resolverDosEjerciciosEnNivel(gestor)
        assertTrue(gestor.estaDesbloqueado(Dificultad.DIFICIL))
        assertEquals(2, gestor.rachaMaxima)
        assertTrue(gestor.puntajeTotal >= 600)
    }

    private fun resolverDosEjerciciosEnNivel(gestor: IGestorProgreso) {
        val p1 = gestor.obtenerProblemaActual()!!
        assertTrue(gestor.registrarIntento(p1.respuestaCorrecta, 2L))
        assertFalse(gestor.nivelTerminado())
        val p2 = gestor.avanzarSiguienteProblema()!!
        assertTrue(gestor.registrarIntento(p2.respuestaCorrecta, 2L))
        assertTrue(gestor.nivelTerminado())
    }

    @Test
    fun testEstadisticasFinalesYReinicioTotal() {
        val gestor = ctorGestorProgresoTest()
        val p = gestor.obtenerProblemaActual()!!
        gestor.registrarIntento(p.respuestaCorrecta + 500.0, 5L)
        assertEquals(2, gestor.gestorVidas.vidasActuales)
        val stats = gestor.generarEstadisticasFinales()
        assertEquals(2, stats.vidasRestantes)
        assertEquals("00:05", stats.tiempoFormateado())
        assertEquals(4, stats.construirTarjetasResumen().size)
        gestor.reiniciarTodo()
        assertEquals(0, gestor.puntajeTotal)
        assertEquals(3, gestor.gestorVidas.vidasActuales)
    }
}

class ComponentesHudJavaFxTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun configurarFx() {
            FxTestSupport.inicializarToolkit()
        }
    }

    @Test
    fun testMarcadorPuntosViewSeActualizaReactivamente() {
        FxTestSupport.ejecutarEnFx {
            val gestor = ctorGestorProgresoTest()
            val marcador = ctorMarcadorTest(gestor.puntajeTotalProperty)
            assertEquals(0, marcador.puntajeMostrado)
            val prob = gestor.obtenerProblemaActual()!!
            gestor.registrarIntento(prob.respuestaCorrecta, 1L)
            assertEquals(100, marcador.puntajeMostrado)
            assertTrue(marcador.obtenerTextoPuntos().contains("100"))
            marcador.mostrarIncremento(125, rachaActual = 2)
            assertTrue(marcador.obtenerTextoRacha().contains("2x"))
            marcador.desvincular()
        }
    }

    @Test
    fun testPanelVidasViewSincronizaIconos() {
        FxTestSupport.ejecutarEnFx {
            val vidas = ctorGestorVidasTest(3)
            val panel = ctorPanelVidasTest(vidas.vidasProperty, vidas.maxVidas)
            assertEquals(3, panel.vidasMostradas)
            assertEquals(3, panel.iconosVidas.count { it.styleClass.contains("life-active") })
            vidas.perderVida()
            assertEquals(2, panel.vidasMostradas)
            assertEquals(1, panel.iconosVidas.count { it.styleClass.contains("life-lost") })
            panel.desvincular()
        }
    }
}
