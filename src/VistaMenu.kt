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
 * Construye y renderiza exclusivamente los elementos del menú inicial.
 * Delega la navegación a [INavegadorEscenas] y la lógica de avance y desbloqueo
 * a la abstracción [IGestorProgreso].
 *
 * Principio de Inversión de Dependencias (DIP):
 * Depende de contratos abstractos ([INavegadorEscenas], [IGestorProgreso]).
 *
 * Integración de Progreso del Jugador:
 * Consulta a [IGestorProgreso] para deshabilitar botones de niveles bloqueados
 * y aplicarles la clase CSS [.nivel-bloqueado].
 */
class VistaMenu(
    private val navegador: INavegadorEscenas,
    val gestorProgreso: IGestorProgreso = GestorProgreso()
) : VBox() {

    /** Nodo raíz de la vista (referencia a sí mismo como contenedor). */
    val root: Parent get() = this

    // 1. Título principal y subtítulo con temática espacial neón
    val lblTitulo: Label = UIFactory.crearTitulo(GameConfig.TITULO_APP)

    val lblSubtitulo: Label = UIFactory.crearEtiqueta(
        texto = GameConfig.SUBTITULO_APP,
        clasesEstilo = listOf("label", "subtitulo-menu")
    )

    // 2. Encabezado de la sección de selección de dificultad
    val lblInstruccionDificultad: Label = UIFactory.crearEtiqueta(
        texto = "SELECCIONA EL NIVEL DE MISIÓN",
        clasesEstilo = listOf("label", "seccion-dificultad-header")
    )

    // 3. Grupo y botones interactivos de selección de Dificultad
    val grupoDificultad: ToggleGroup = ToggleGroup()
    val botonesDificultad: MutableMap<Dificultad, ToggleButton> = mutableMapOf()

    // Etiqueta descriptiva que se actualiza reactivamente según la dificultad seleccionada
    val lblDescripcionDificultad: Label = UIFactory.crearEtiqueta(
        texto = "",
        clasesEstilo = listOf("label", "tarjeta-dificultad-descripcion"),
        anchoMax = 440.0,
        wrap = true,
        alineacion = Pos.CENTER
    )

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

        // Construcción de botones y enlace de eventos
        val panelBotonesDificultad = construirBotonesDificultad()
        configurarEventosSeleccion()

        // Sincroniza el bloqueo/desbloqueo según el progreso del jugador
        refrescarEstadoProgreso()

        // Botón Iniciar Misión: Navega al juego pasando la dificultad seleccionada
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
     * Construye los botones de alternancia para cada dificultad disponible.
     */
    private fun construirBotonesDificultad(): HBox {
        val panel = HBox(12.0).apply { alignment = Pos.CENTER }
        val dificultades = listOf(Dificultad.FACIL, Dificultad.MEDIO, Dificultad.DIFICIL)

        for (dif in dificultades) {
            val btn = UIFactory.crearBotonDificultad(
                texto = dif.nombreVisible.uppercase(),
                grupo = grupoDificultad,
                datos = dif,
                anchoPref = 120.0
            )
            botonesDificultad[dif] = btn
            panel.children.add(btn)
        }

        // Selección inicial por defecto: Nivel Fácil
        botonesDificultad[Dificultad.FACIL]?.isSelected = true
        actualizarDificultad(Dificultad.FACIL)

        return panel
    }

    /**
     * Configura el escuchador reactivo de selección de dificultad.
     */
    private fun configurarEventosSeleccion() {
        grupoDificultad.selectedToggleProperty().addListener { _, oldToggle, newToggle ->
            if (newToggle == null) {
                // Impide deseleccionar todas las opciones manteniendo la última válida
                oldToggle?.isSelected = true
            } else {
                val difSeleccionada = newToggle.userData as? Dificultad ?: Dificultad.FACIL
                actualizarDificultad(difSeleccionada)
            }
        }
    }

    /**
     * Actualiza el estado de la dificultad seleccionada y refresca la descripción visual.
     *
     * @param dif Nueva [Dificultad] seleccionada.
     */
    fun actualizarDificultad(dif: Dificultad) {
        this.dificultadSeleccionada = dif
        val estaDesbloqueado = gestorProgreso.esNivelDesbloqueado(dif)
        val textoCondiciones = "Condiciones: ${dif.descripcion} (Margen permitido: ±${dif.margenErrorTolerable})"

        this.lblDescripcionDificultad.text = if (estaDesbloqueado) {
            textoCondiciones
        } else {
            "$textoCondiciones\n🔒 Nivel Bloqueado: Completa los ejercicios previos para acceder."
        }

        // Sincroniza la selección del botón si fue invocado programáticamente
        val btn = botonesDificultad[dif]
        if (btn != null && !btn.isSelected) {
            btn.isSelected = true
        }
    }

    /**
     * Consulta [GestorProgreso] y actualiza los botones deshabilitando aquellos bloqueados (`setDisable(true)`)
     * y aplicando la clase CSS `.nivel-bloqueado`. Si la dificultad seleccionada quedó bloqueada tras un
     * Game Over, restablece automáticamente la selección a [Dificultad.FACIL].
     */
    fun refrescarEstadoProgreso() {
        for ((dif, btn) in botonesDificultad) {
            val desbloqueado = gestorProgreso.esNivelDesbloqueado(dif)
            btn.isDisable = !desbloqueado
            if (!desbloqueado) {
                if (!btn.styleClass.contains("nivel-bloqueado")) {
                    btn.styleClass.add("nivel-bloqueado")
                }
            } else {
                btn.styleClass.remove("nivel-bloqueado")
            }
        }
        if (!gestorProgreso.esNivelDesbloqueado(dificultadSeleccionada)) {
            actualizarDificultad(Dificultad.FACIL)
        }
    }
}
