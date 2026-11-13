package gravityquest
import gravityquest.models.*; import org.example.Juego.ICalculadoraFisica; import org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.BeforeAll; import org.junit.jupiter.api.Test
internal typealias ctorStageNavTest = IStage; internal typealias ctorNavEscenasTest = NavegadorEscenas
internal typealias ctorVistaMenuTest = VistaMenu; internal typealias ctorVistaJuegoTest = VistaJuego; internal typealias ctorStatsTest = EstadisticasPartida

interface INavegadorEspia : INavegadorEscenas {
    var menuInvocado: IBoolean
    var dificultadJuegoInvocada: Dificultad?
}

class NavegadorEspia(
    override val stage: IStage,
    override val repositorio: IRepositorioProblemas = ctorGeneradorTest(),
    override val calculadora: ICalculadoraFisica = ctorCalcTest(),
    override val gestorProgreso: IGestorProgreso = ctorGestorProgresoTest(repositorio)
) : BaseNavegadorEscenas(stage, repositorio, calculadora, gestorProgreso), INavegadorEspia {

    override var menuInvocado: IBoolean = false
    override var dificultadJuegoInvocada: Dificultad? = null

    override fun mostrarMenu() {
        menuInvocado = true
    }

    override fun mostrarJuego(dificultad: Dificultad) {
        dificultadJuegoInvocada = dificultad
    }

    override fun mostrarGameOver(dificultad: Dificultad, estadisticas: EstadisticasPartida) {
        if (estadisticas.puntosTotales >= 0) dificultadJuegoInvocada = dificultad
    }

    override fun mostrarNivelCompletado(
        dificultad: Dificultad,
        estadisticas: EstadisticasPartida,
        desbloqueoSiguienteNivel: IBoolean
    ) {
        if (desbloqueoSiguienteNivel || estadisticas.puntosTotales >= 0) {
            dificultadJuegoInvocada = dificultad
        }
    }
}

internal typealias ctorNavEspia = NavegadorEspia

open class BaseFxNavegacionTest {
    protected fun verificarEntornoFx(): IBoolean {
        FxTestSupport.inicializarToolkit()
        return true
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun inicializarFx() {
            FxTestSupport.inicializarToolkit()
        }
    }
}

class NavegacionEscenasTest : BaseFxNavegacionTest() {

    @Test
    fun testNavegadorMuestraMenuYJuego() {
        FxTestSupport.ejecutarEnFx {
            val stage = ctorStageNavTest()
            val nav = ctorNavEscenasTest(stage)
            nav.mostrarMenu()
            assertNotNull(stage.scene)
            assertTrue(IVistaMenu::class.isInstance(stage.scene.root))
            nav.mostrarJuego(Dificultad.FACIL)
            assertTrue(IVistaJuegoSimulacion::class.isInstance(stage.scene.root))
            nav.mostrarMenu()
            assertEquals(1, nav.limpiezasEjecutadas)
        }
    }

    @Test
    fun testNavegadorMuestraPantallasFinales() {
        FxTestSupport.ejecutarEnFx {
            val stage = ctorStageNavTest()
            val nav = ctorNavEscenasTest(stage)
            val stats = ctorStatsTest(350, 4, 12, 65L, 2)
            nav.mostrarGameOver(Dificultad.MEDIO, stats)
            assertTrue(IVistaGameOver::class.isInstance(stage.scene.root))
            nav.mostrarNivelCompletado(Dificultad.FACIL, stats, desbloqueoSiguienteNivel = true)
            assertTrue(IVistaNivelCompletado::class.isInstance(stage.scene.root))
        }
    }

    @Test
    fun testVistaMenuSeleccionaDificultadDesbloqueada() {
        FxTestSupport.ejecutarEnFx {
            val espia = ctorNavEspia(ctorStageNavTest())
            val menu = ctorVistaMenuTest(espia, espia.gestorProgreso)
            assertEquals(3, menu.botonesDificultad.size)
            assertFalse(menu.seleccionarDificultad(Dificultad.DIFICIL))
            assertTrue(menu.seleccionarDificultad(Dificultad.FACIL))
            assertEquals(Dificultad.FACIL, espia.dificultadJuegoInvocada)
        }
    }
}

class VistaJuegoInteraccionTest : BaseFxNavegacionTest() {

    @Test
    fun testCargaPreguntaYValidacionRespuesta() {
        FxTestSupport.ejecutarEnFx {
            val espia = ctorNavEspia(ctorStageNavTest())
            val vista = ctorVistaJuegoTest(Dificultad.FACIL, espia)
            val prob = vista.problemaActual
            assertNotNull(prob)
            assertTrue(vista.lblProgresoPregunta.text.startsWith("\u0050rogreso: 0/2"))
            vista.txtRespuesta.text = "invalido"
            assertFalse(vista.validarRespuestaUsuario())
            vista.txtRespuesta.text = prob!!.respuestaCorrecta.toString()
            assertTrue(vista.validarRespuestaUsuario())
            assertTrue(vista.lblProgresoPregunta.text.startsWith("\u0050rogreso: 1/2"))
            assertTrue(vista.simulacionEnCurso)
            vista.pausarSimulacion()
            assertFalse(vista.simulacionEnCurso)
        }
    }

    @Test
    fun testAvancePreguntaYPista() {
        FxTestSupport.ejecutarEnFx {
            val espia = ctorNavEspia(ctorStageNavTest())
            val vista = ctorVistaJuegoTest(Dificultad.FACIL, espia)
            val idInicial = vista.problemaActual?.id
            val panelPista = VentanaPista.construirContenidoPista(vista.problemaActual!!)
            assertTrue(panelPista.children.isNotEmpty())
            vista.avanzarSiguientePregunta()
            assertNotNull(vista.problemaActual)
            assertTrue(vista.problemaActual?.id != idInicial)
            vista.volverAlMenu()
            assertTrue(espia.menuInvocado)
        }
    }
}
