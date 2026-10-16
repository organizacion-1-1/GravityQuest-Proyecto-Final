package gravityquest

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import gravityquest.models.Dificultad

/**
 * ============================================================================
 * GravityQuest - Pantalla del Menú Inicial (VistaMenu)
 * ============================================================================
 *
 * Principio de Responsabilidad Única (SRP):
 * Se encarga exclusivamente de construir, organizar y renderizar los elementos
 * visuales del menú inicial (título con estética espacial neón, selector de dificultad
 * interactivo y botón de inicio de misión). No controla la ventana ([javafx.stage.Stage])
 * ni instancia directamente la vista del juego.
 *
 * Principio de Inversión de Dependencias (DIP):
 * Depende de la abstracción [INavegadorEscenas] para delegar la navegación hacia la
 * pantalla de juego, desacoplándose de otras vistas o del escenario principal.
 *
 * Encapsulamiento:
 * Extiende [VBox] y expone propiedades públicas de sólo lectura para que la interfaz
 * sea comprobable mediante pruebas unitarias sin romper el encapsulamiento interno.
 *
 * @param navegador Abstracción del gestor de navegación para coordinar transiciones.
 */
class VistaMenu(
    private val navegador: INavegadorEscenas
) : VBox() {

    /** Nodo raíz de la vista (referencia a sí mismo como contenedor). */
    val root: Parent get() = this

    // 1. Título principal y subtítulo con temática espacial
    val lblTitulo: Label = Label("GravityQuest").apply {
        styleClass.addAll("titulo")
    }

    val lblSubtitulo: Label = Label("Misiones de Cinemática y Física Gravitatoria").apply {
        styleClass.addAll("label", "subtitulo-menu")
    }

    // 2. Encabezado de la sección de selección de dificultad
    val lblInstruccionDificultad: Label = Label("SELECCIONA EL NIVEL DE MISIÓN").apply {
        styleClass.addAll("label")
        style = "-fx-font-weight: bold; -fx-text-fill: #94a3b8; -fx-font-size: 11px;"
    }

    // 3. Grupo y botones interactivos de selección de Dificultad (SRP / POO)
    val grupoDificultad: ToggleGroup = ToggleGroup()
    val botonesDificultad: MutableMap<Dificultad, ToggleButton> = mutableMapOf()

    // Etiqueta descriptiva que se actualiza reactivamente según la dificultad seleccionada
    val lblDescripcionDificultad: Label = Label().apply {
        styleClass.addAll("label", "tarjeta-dificultad-descripcion")
        isWrapText = true
        maxWidth = 420.0
        alignment = Pos.CENTER
        style = "-fx-text-fill: #38bdf8; -fx-alignment: center; -fx-text-alignment: center; -fx-font-size: 12px;"
    }

    /** Dificultad actualmente seleccionada por el usuario en el menú. */
    var dificultadSeleccionada: Dificultad = Dificultad.FACIL
        private set

    // 4. Botón principal de Iniciar Misión
    val btnIniciar: Button = Button("🚀 Iniciar Misión").apply {
        styleClass.addAll("button", "btn-iniciar")
        prefWidth = 260.0
    }

    // 5. Contenedor tipo tarjeta central del menú (.menu-container)
    val menuContainer: VBox

    init {
        // Configuración del layout base
        alignment = Pos.CENTER
        styleClass.add("root")
        spacing = 20.0

        // Construcción de los botones para cada nivel de dificultad del enum Dificultad
        val panelBotonesDificultad = HBox(12.0).apply {
            alignment = Pos.CENTER
        }

        // Niveles de dificultad en orden de desafío
        val dificultades = listOf(Dificultad.FACIL, Dificultad.MEDIO, Dificultad.DIFICIL)

        for (dif in dificultades) {
            val btn = ToggleButton(dif.nombreVisible.uppercase()).apply {
                styleClass.addAll("button", "btn-dificultad")
                toggleGroup = grupoDificultad
                userData = dif
                prefWidth = 120.0
            }
            botonesDificultad[dif] = btn
            panelBotonesDificultad.children.add(btn)
        }

        // Selección inicial por defecto: Nivel Fácil
        val botonInicial = botonesDificultad[Dificultad.FACIL]
        botonInicial?.isSelected = true
        actualizarDificultad(Dificultad.FACIL)

        // Escuchador reactivo de cambio de selección
        grupoDificultad.selectedToggleProperty().addListener { _, oldToggle, newToggle ->
            if (newToggle == null) {
                // Impide que el usuario deseleccione todas las opciones (mantiene la previa)
                oldToggle?.isSelected = true
            } else {
                val difSeleccionada = newToggle.userData as? Dificultad ?: Dificultad.FACIL
                actualizarDificultad(difSeleccionada)
            }
        }

        // Acción del botón Iniciar Misión: Navega al juego pasando la dificultad seleccionada
        btnIniciar.setOnAction {
            navegador.irAJuego(dificultadSeleccionada)
        }

        // Armado del contenedor estilizado del menú
        menuContainer = VBox(22.0).apply {
            alignment = Pos.CENTER
            styleClass.add("menu-container")
            children.addAll(
                lblTitulo,
                lblSubtitulo,
                lblInstruccionDificultad,
                panelBotonesDificultad,
                lblDescripcionDificultad,
                btnIniciar
            )
        }

        children.add(menuContainer)
    }

    /**
     * Actualiza el estado de la dificultad seleccionada y refresca la descripción visual.
     *
     * @param dif Nueva [Dificultad] seleccionada.
     */
    fun actualizarDificultad(dif: Dificultad) {
        this.dificultadSeleccionada = dif
        this.lblDescripcionDificultad.text = "Condiciones: ${dif.descripcion} (Margen permitido: ±${dif.margenErrorTolerable})"

        // Sincroniza la selección del botón si fue invocado programáticamente
        val btn = botonesDificultad[dif]
        if (btn != null && !btn.isSelected) {
            btn.isSelected = true
        }
    }
}
