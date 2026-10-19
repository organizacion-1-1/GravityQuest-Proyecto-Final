package gravityquest

import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas

/**
 * ============================================================================
 * GravityQuest - Gestor de Navegación y Principios SOLID (NavegadorEscenas)
 * ============================================================================
 *
 * Contrato de abstracción para la navegación entre escenas y pantallas del juego.
 *
 * Principio de Inversión de Dependencias (DIP):
 * Las pantallas y vistas no dependen de la implementación concreta del navegador
 * ni manipulan directamente el [Stage] de JavaFX; dependen exclusivamente de este contrato.
 *
 * Principio Abierto/Cerrado (OCP):
 * Provee el método extensible [cambiarEscena], permitiendo incorporar nuevas vistas
 * sin modificar ni acoplar las vistas existentes.
 */
interface INavegadorEscenas {
    /** Navega a la pantalla del menú inicial. */
    fun irAMenu()

    /**
     * Navega a la pantalla de juego cargando la dificultad seleccionada.
     *
     * @param dificultad Dificultad del juego seleccionada por el usuario.
     */
    fun irAJuego(dificultad: Dificultad)

    /**
     * Cambia la escena activa del Stage por una nueva raíz visual.
     *
     * @param nodo Nodo raíz que conformará la nueva vista.
     * @param titulo Título de la ventana para la nueva escena.
     */
    fun cambiarEscena(nodo: Parent, titulo: String = GameConfig.TITULO_APP)

    /** Nodo raíz visual actualmente presentado en pantalla. */
    val escenaActual: Parent? get() = null

    /** Título de la ventana actualmente establecido. */
    val tituloActual: String get() = ""
}

/**
 * Gestor centralizado de escenas para JavaFX en GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Centraliza el control de la ventana ([Stage]), la inyección de estilos y
 * la coordinación del estado compartido de progreso entre pantallas.
 */
class NavegadorEscenas(
    val stage: Stage? = null,
    val repositorio: IRepositorioProblemas = GeneradorProblemas(),
    val calculadora: ICalculadoraFisica = CalculadoraFisica(),
    val gestorProgreso: IGestorProgreso = GestorProgreso(repositorio),
    val anchoPredeterminado: Double = GameConfig.ANCHO_VENTANA_DEFAULT,
    val altoPredeterminado: Double = GameConfig.ALTO_VENTANA_DEFAULT
) : INavegadorEscenas {

    /** Ruta del recurso de la hoja de estilos CSS común (referencia a [GameConfig]). */
    val rutaEstilos: String = GameConfig.RUTA_ESTILOS

    private var _escenaActual: Parent? = null
    override val escenaActual: Parent? get() = _escenaActual

    private var _tituloActual: String = ""
    override val tituloActual: String get() = _tituloActual

    override fun irAMenu() {
        val vistaMenu = VistaMenu(this, gestorProgreso)
        cambiarEscena(vistaMenu.root, "${GameConfig.TITULO_APP} - Menú Principal")
    }

    override fun irAJuego(dificultad: Dificultad) {
        val vistaJuego = VistaJuego(dificultad, this, repositorio, calculadora, gestorProgreso, stage)
        cambiarEscena(vistaJuego.root, "${GameConfig.TITULO_APP} - Misión: ${dificultad.nombreVisible}")
    }

    override fun cambiarEscena(nodo: Parent, titulo: String) {
        this._escenaActual = nodo
        this._tituloActual = titulo

        val st = stage ?: return
        val sceneActual = st.scene
        if (sceneActual == null) {
            val nuevaScene = Scene(nodo, anchoPredeterminado, altoPredeterminado)
            aplicarEstilos(nuevaScene)
            st.scene = nuevaScene
        } else {
            sceneActual.root = nodo
            aplicarEstilos(sceneActual)
        }

        st.title = titulo
        if (!st.isShowing) {
            st.show()
        }
    }

    private fun aplicarEstilos(scene: Scene) {
        val cssResource = javaClass.getResource(rutaEstilos)?.toExternalForm()
        if (cssResource != null && !scene.stylesheets.contains(cssResource)) {
            scene.stylesheets.add(cssResource)
        }
    }
}
