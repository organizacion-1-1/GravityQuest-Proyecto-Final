package gravityquest
import gravityquest.models.Dificultad; import gravityquest.models.IRepositorioProblemas
import javafx.stage.Stage; import org.example.Juego.ICalculadoraFisica
import org.junit.jupiter.api.Assertions.assertEquals; import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull; import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll; import org.junit.jupiter.api.Test
internal typealias ctorNavEscenasTest = NavegadorEscenas
internal typealias ctorVistaMenuTest = VistaMenu; internal typealias ctorVistaJuegoTest = VistaJuego; internal typealias ctorStatsTest = EstadisticasPartida

interface INavegadorEspia : INavegadorEscenas {
    val menuInvocado: Boolean
    val dificultadJuegoInvocada: Dificultad?
}

class NavegadorEspia(
    stage: Stage
) : BaseNavegadorEscenas(stage), INavegadorEspia {

    override val gestorProgreso: IGestorProgreso = ProveedorGestorNavegador.crearGestor(repositorio)
    override var menuInvocado: Boolean = false
        private set
    override var dificultadJuegoInvocada: Dificultad? = null
        private set

    override fun mostrarMenu() {
        menuInvocado = true
    }

    override fun mostrarJuego(dificultad: Dificultad) {
        dificultadJuegoInvocada = dificultad
    }

    override fun mostrarGameOver(dificultad: Dificultad, estadisticas: IEstadisticasPartida) {
        if (estadisticas.puntosTotales >= 0) dificultadJuegoInvocada = dificultad
    }

    override fun mostrarNivelCompletado(
        dificultad: Dificultad,
        estadisticas: IEstadisticasPartida,
        desbloqueoSiguienteNivel: Boolean
    ) {
        if (desbloqueoSiguienteNivel || estadisticas.puntosTotales >= 0) {
            dificultadJuegoInvocada = dificultad
        }
    }
}

internal typealias ctorNavEspia = NavegadorEspia

open class BaseFxNavegacionTest {
    protected fun verificarEntornoFx(): Boolean {
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
            val stage = Stage()
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
    fun testVistaMenuSeleccionaDificultadDesbloqueada() {
        FxTestSupport.ejecutarEnFx {
            val espia = ctorNavEspia(Stage())
            val menu = ctorVistaMenuTest(espia, espia.gestorProgreso)
            assertEquals(3, menu.botonesDificultad.size)
            assertFalse(menu.seleccionarDificultad(Dificultad.DIFICIL))
            assertTrue(menu.seleccionarDificultad(Dificultad.FACIL))
            assertEquals(Dificultad.FACIL, espia.dificultadJuegoInvocada)
        }
    }
}

class NavegacionPantallasFinalesTest : BaseFxNavegacionTest() {

    @Test
    fun testNavegadorMuestraPantallasFinales() {
        FxTestSupport.ejecutarEnFx {
            val stage = Stage()
            val nav = ctorNavEscenasTest(stage)
            val stats = ctorStatsTest(350, 4, 12, 65L, 2)
            nav.mostrarGameOver(Dificultad.MEDIO, stats)
            assertTrue(IVistaGameOver::class.isInstance(stage.scene.root))
            nav.mostrarNivelCompletado(Dificultad.FACIL, stats, desbloqueoSiguienteNivel = true)
            assertTrue(IVistaNivelCompletado::class.isInstance(stage.scene.root))
        }
    }
}

class VistaJuegoInteraccionTest : BaseFxNavegacionTest() {

    @Test
    fun testCargaPreguntaYValidacionRespuesta() {
        FxTestSupport.ejecutarEnFx {
            val espia = ctorNavEspia(Stage())
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
            val espia = ctorNavEspia(Stage())
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
