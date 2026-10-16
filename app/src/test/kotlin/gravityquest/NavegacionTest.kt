package gravityquest

import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.BeforeAll
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas

/**
 * ============================================================================
 * Pruebas Unitarias para el Sistema de Pantallas y Navegación (SOLID & POO)
 * ============================================================================
 *
 * Valida:
 * 1. Single Responsibility Principle (SRP):
 *    - NavegadorEscenas controla el Stage de JavaFX y el intercambio de escenas.
 *    - VistaMenu construye exclusivamente el menú inicial y selector de dificultad.
 *    - VistaJuego construye exclusivamente la pantalla del ejercicio y simulación.
 * 2. Dependency Inversion Principle (DIP):
 *    - Las vistas se comunican mediante INavegadorEscenas sin acoplamiento entre sí.
 * 3. Open/Closed Principle (OCP):
 *    - El navegador permite incorporar nuevas vistas arbitrarias mediante cambiarEscena().
 */
class NavegacionTest {

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

        /**
         * Ejecuta un bloque de código garantizando su ejecución en el hilo JavaFX Application Thread.
         */
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

    /**
     * Falso gestor de navegación (Mock / Espía) para pruebas desacopladas (DIP).
     */
    class NavegadorEspia : INavegadorEscenas {
        var menuInvocado: Boolean = false
        var dificultadJuegoInvocada: Dificultad? = null
        override var escenaActual: Parent? = null
        override var tituloActual: String = ""

        override fun irAMenu() {
            menuInvocado = true
        }

        override fun irAJuego(dificultad: Dificultad) {
            dificultadJuegoInvocada = dificultad
        }

        override fun cambiarEscena(nodo: Parent, titulo: String) {
            this.escenaActual = nodo
            this.tituloActual = titulo
        }
    }

    // ========================================================================
    // 1. Pruebas para NavegadorEscenas (SRP & DIP)
    // ========================================================================

    @Test
    fun testNavegadorEscenasInicializacionYContratoDIP() {
        ejecutarEnFx {
            val stage = Stage()
            val navegador = NavegadorEscenas(stage)

            assertNotNull(navegador, "NavegadorEscenas debe instanciarse")
            val esNavegadorEscenas: Any = navegador
            assertTrue(esNavegadorEscenas is INavegadorEscenas, "Debe implementar INavegadorEscenas (DIP)")
            assertEquals(stage, navegador.stage, "NavegadorEscenas debe encapsular el Stage (SRP)")
            assertNotNull(navegador.repositorio, "El navegador debe contar con un repositorio de problemas")
            assertNotNull(navegador.calculadora, "El navegador debe contar con una calculadora física")
        }
    }

    @Test
    fun testNavegadorEscenasIrAMenu() {
        ejecutarEnFx {
            val stage = Stage()
            val navegador = NavegadorEscenas(stage)

            navegador.irAMenu()

            assertNotNull(stage.scene, "irAMenu() debe configurar la Scene en el Stage")
            assertTrue(stage.scene.root is VistaMenu, "La raíz de la escena debe ser una instancia de VistaMenu")
            assertTrue(stage.title.contains("Menú Principal"), "El título debe reflejar el Menú Principal")
            assertTrue(navegador.escenaActual is VistaMenu)
        }
    }

    @Test
    fun testNavegadorEscenasIrAJuego() {
        ejecutarEnFx {
            val stage = Stage()
            val navegador = NavegadorEscenas(stage)

            navegador.irAJuego(Dificultad.MEDIO)

            assertNotNull(stage.scene, "irAJuego() debe configurar la Scene en el Stage")
            assertTrue(stage.scene.root is VistaJuego, "La raíz de la escena debe ser una instancia de VistaJuego")
            val vistaJuego = stage.scene.root as VistaJuego
            assertEquals(Dificultad.MEDIO, vistaJuego.dificultad, "La vista de juego debe recibir la dificultad MEDIO")
            assertTrue(stage.title.contains("Medio"), "El título debe reflejar la dificultad seleccionada")
        }
    }

    @Test
    fun testNavegadorEscenasExtensibilidadOCP() {
        ejecutarEnFx {
            val stage = Stage()
            val navegador = NavegadorEscenas(stage)

            // Demostración de OCP: Navegar a una nueva pantalla hipotética (ej. Ajustes) sin modificar el motor
            val vistaAjustesMock = VBox(Label("Pantalla de Ajustes"))
            navegador.cambiarEscena(vistaAjustesMock, "GravityQuest - Ajustes")

            assertNotNull(stage.scene)
            assertEquals(vistaAjustesMock, stage.scene.root, "El navegador debe permitir montar cualquier nueva pantalla (OCP)")
            assertEquals("GravityQuest - Ajustes", stage.title)
        }
    }

    // ========================================================================
    // 2. Pruebas para VistaMenu (SRP, UI y Estilos CSS)
    // ========================================================================

    @Test
    fun testVistaMenuComponentesUIYEstilos() {
        ejecutarEnFx {
            val espia = NavegadorEspia()
            val menu = VistaMenu(espia)

            // Título estilizado con efecto espacial neón
            assertEquals("GravityQuest", menu.lblTitulo.text)
            assertTrue(menu.lblTitulo.styleClass.contains("titulo"), "El título debe tener clase CSS 'titulo'")

            // Subtítulo
            assertTrue(menu.lblSubtitulo.styleClass.contains("subtitulo-menu"), "Debe tener clase CSS 'subtitulo-menu'")

            // Contenedor tarjeta central
            assertTrue(menu.menuContainer.styleClass.contains("menu-container"), "El contenedor debe tener clase CSS 'menu-container'")

            // Selector de dificultad: deben existir botones para las 3 dificultades
            assertEquals(3, menu.botonesDificultad.size, "Debe haber 3 botones de dificultad")
            val btnFacil = menu.botonesDificultad[Dificultad.FACIL]
            val btnMedio = menu.botonesDificultad[Dificultad.MEDIO]
            val btnDificil = menu.botonesDificultad[Dificultad.DIFICIL]

            assertNotNull(btnFacil)
            assertNotNull(btnMedio)
            assertNotNull(btnDificil)

            assertTrue(btnFacil.styleClass.contains("btn-dificultad"), "Los botones deben tener clase 'btn-dificultad'")
            assertTrue(btnMedio.styleClass.contains("btn-dificultad"))
            assertTrue(btnDificil.styleClass.contains("btn-dificultad"))

            // Botón Iniciar Misión
            assertTrue(menu.btnIniciar.text.contains("Iniciar Misión"), "El botón debe decir Iniciar Misión")
            assertTrue(menu.btnIniciar.styleClass.contains("btn-iniciar"), "Debe tener clase CSS 'btn-iniciar'")
        }
    }

    @Test
    fun testVistaMenuSeleccionDeDificultadYCambioReactivo() {
        ejecutarEnFx {
            val espia = NavegadorEspia()
            val menu = VistaMenu(espia)

            // Por defecto debe iniciar en Fácil
            assertEquals(Dificultad.FACIL, menu.dificultadSeleccionada)
            assertTrue(menu.botonesDificultad[Dificultad.FACIL]?.isSelected == true)
            assertTrue(menu.lblDescripcionDificultad.text.contains("sin velocidad inicial"))

            // Cambiar a Medio
            val btnMedio = menu.botonesDificultad[Dificultad.MEDIO]!!
            btnMedio.fire()
            assertEquals(Dificultad.MEDIO, menu.dificultadSeleccionada)
            assertTrue(menu.lblDescripcionDificultad.text.contains("Lanzamiento vertical hacia abajo"))

            // Cambiar a Difícil
            val btnDificil = menu.botonesDificultad[Dificultad.DIFICIL]!!
            btnDificil.fire()
            assertEquals(Dificultad.DIFICIL, menu.dificultadSeleccionada)
            assertTrue(menu.lblDescripcionDificultad.text.contains("hacia arriba desacelerando"))
        }
    }

    @Test
    fun testVistaMenuBotonIniciarNavegaAlJuegoConDificultad() {
        ejecutarEnFx {
            val espia = NavegadorEspia()
            val menu = VistaMenu(espia)

            // Seleccionar Difícil y presionar Iniciar
            menu.actualizarDificultad(Dificultad.DIFICIL)
            menu.btnIniciar.fire()

            assertEquals(Dificultad.DIFICIL, espia.dificultadJuegoInvocada,
                "Al pulsar Iniciar Misión debe invocar irAJuego con la dificultad seleccionada en el navegador")
        }
    }

    // ========================================================================
    // 3. Pruebas para VistaJuego (SRP, Carga de Problemas, Navegación y Validación)
    // ========================================================================

    @Test
    fun testVistaJuegoCargaProblemaSegunDificultad() {
        ejecutarEnFx {
            val espia = NavegadorEspia()
            val repo = GeneradorProblemas()

            val juegoFacil = VistaJuego(Dificultad.FACIL, espia, repo)
            assertEquals(Dificultad.FACIL, juegoFacil.problemaActual.dificultad)

            val juegoMedio = VistaJuego(Dificultad.MEDIO, espia, repo)
            assertEquals(Dificultad.MEDIO, juegoMedio.problemaActual.dificultad)

            val juegoDificil = VistaJuego(Dificultad.DIFICIL, espia, repo)
            assertEquals(Dificultad.DIFICIL, juegoDificil.problemaActual.dificultad)
        }
    }

    @Test
    fun testVistaJuegoBotonVolverAlMenu() {
        ejecutarEnFx {
            val espia = NavegadorEspia()
            val juego = VistaJuego(Dificultad.FACIL, espia)

            assertTrue(juego.btnVolverMenu.text.contains("Volver al Menú"))
            assertTrue(juego.btnVolverMenu.styleClass.contains("btn-volver"))

            // Al pulsar volver al menú, debe delegar a irAMenu() en el navegador
            assertFalse(espia.menuInvocado)
            juego.btnVolverMenu.fire()
            assertTrue(espia.menuInvocado, "El botón 'Volver al Menú' debe invocar irAMenu() en el gestor de navegación")
        }
    }

    @Test
    fun testVistaJuegoValidacionRespuestaFisica() {
        ejecutarEnFx {
            val espia = NavegadorEspia()
            val juego = VistaJuego(Dificultad.FACIL, espia)

            // Validación con respuesta errónea
            juego.txtRespuesta.text = "999.9"
            val resultadoError = juego.validarRespuesta()
            assertFalse(resultadoError, "Una respuesta incorrecta debe ser rechazada")
            assertTrue(juego.lblResultado.styleClass.contains("resultado-error"))

            // Validación con respuesta correcta esperada
            juego.txtRespuesta.text = juego.problemaActual.valorEsperado.toString()
            val resultadoExito = juego.validarRespuesta()
            assertTrue(resultadoExito, "La respuesta exacta del problema debe ser aprobada")
            assertTrue(juego.lblResultado.styleClass.contains("resultado-exito"))
        }
    }
}
