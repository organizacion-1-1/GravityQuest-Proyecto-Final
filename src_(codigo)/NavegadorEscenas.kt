package gravityquest

import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas

/**
 * ============================================================================
 * GravityQuest - Gestor de Navegación y Principios SOLID
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
 * (Ajustes, Créditos, Game Over, Tutorial) sin modificar ni acoplar las vistas existentes.
 */
interface INavegadorEscenas {
    /**
     * Navega a la pantalla del menú inicial.
     */
    fun irAMenu()

    /**
     * Navega a la pantalla de juego cargando la dificultad seleccionada.
     *
     * @param dificultad Dificultad del juego seleccionada por el usuario.
     */
    fun irAJuego(dificultad: Dificultad)

    /**
     * Cambia la escena activa del Stage por una nueva raíz visual.
     * Facilita la extensión futura de nuevas pantallas sin alterar el código existente (OCP).
     *
     * @param nodo Nodo raíz que conformará la nueva vista.
     * @param titulo Título de la ventana para la nueva escena.
     */
    fun cambiarEscena(nodo: Parent, titulo: String = "GravityQuest")

    /**
     * Nodo raíz visual actualmente presentado en pantalla.
     */
    val escenaActual: Parent? get() = null

    /**
     * Título de la ventana actualmente establecido.
     */
    val tituloActual: String get() = ""
}

/**
 * Gestor centralizado de escenas para JavaFX en GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Centraliza y aisla exclusivamente el control de la ventana ([Stage]), la creación
 * e intercambio de objetos [Scene] y la inyección uniforme de la hoja de estilos CSS.
 * Las vistas delegan toda la lógica de cambio de pantalla a este gestor.
 *
 * Principio de Inversión de Dependencias (DIP):
 * Implementa [INavegadorEscenas] y suministra a las vistas abstracciones para sus dependencias
 * ([IRepositorioProblemas], [ICalculadoraFisica]), eliminando acoplamientos rígidos.
 *
 * Principio Abierto/Cerrado (OCP):
 * Abierto a la navegación de nuevas pantallas sin requerir la modificación de vistas previas.
 *
 * @property stage Escenario principal ([Stage]) de la aplicación JavaFX (opcional en pruebas unitarias).
 * @property repositorio Repositorio de problemas para suministrar a las pantallas.
 * @property calculadora Calculadora de cinemática y física para la simulación.
 * @property anchoPredeterminado Ancho por defecto de la ventana en píxeles.
 * @property altoPredeterminado Alto por defecto de la ventana en píxeles.
 */
class NavegadorEscenas(
    val stage: Stage? = null,
    val repositorio: IRepositorioProblemas = GeneradorProblemas(),
    val calculadora: ICalculadoraFisica = CalculadoraFisica(),
    val anchoPredeterminado: Double = 840.0,
    val altoPredeterminado: Double = 560.0
) : INavegadorEscenas {

    /** Ruta del recurso de la hoja de estilos CSS común */
    val rutaEstilos: String = "/assets/style.css"

    /** Nodo raíz visual actualmente activo en la escena */
    private var _escenaActual: Parent? = null
    override val escenaActual: Parent? get() = _escenaActual

    /** Título actual de la ventana */
    private var _tituloActual: String = ""
    override val tituloActual: String get() = _tituloActual

    /**
     * Navega a la pantalla del menú inicial creando una nueva instancia de [VistaMenu].
     */
    override fun irAMenu() {
        val vistaMenu = VistaMenu(this)
        cambiarEscena(vistaMenu.root, "GravityQuest - Menú Principal")
    }

    /**
     * Navega a la pantalla de juego instanciando [VistaJuego] con la dificultad especificada.
     *
     * @param dificultad Dificultad elegida por el usuario.
     */
    override fun irAJuego(dificultad: Dificultad) {
        val vistaJuego = VistaJuego(dificultad, this, repositorio, calculadora)
        cambiarEscena(vistaJuego.root, "GravityQuest - Misión: ${dificultad.nombreVisible}")
    }

    /**
     * Aplica el cambio de contenido en la ventana [Stage] reutilizando o instanciando la [Scene].
     * Asegura la carga de la hoja de estilos espacial en cada transición.
     *
     * @param nodo Nodo raíz que se mostrará en pantalla.
     * @param titulo Título de la ventana.
     */
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

    /**
     * Agrega la hoja de estilos CSS a la [Scene] si aún no se encuentra vinculada.
     */
    private fun aplicarEstilos(scene: Scene) {
        val cssResource = javaClass.getResource(rutaEstilos)?.toExternalForm()
        if (cssResource != null && !scene.stylesheets.contains(cssResource)) {
            scene.stylesheets.add(cssResource)
        }
    }
}
