package gravityquest

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import gravityquest.models.Problema
import gravityquest.models.TipoProblema

/**
 * ============================================================================
 * GravityQuest - Componente Modal de Pistas y Fórmulas (POO & SOLID)
 * ============================================================================
 *
 * Principio de Responsabilidad Única (SRP):
 * Centraliza exclusivamente la construcción, estilo, presentación y descarte del
 * cuadro de diálogo modal de ayuda pedagógica. Se encuentra completamente aislado
 * de la vista de juego principal ([VistaJuego]).
 *
 * Principio de Inversión de Dependencias (DIP):
 * Consume de forma genérica el modelo de datos [Problema], adaptando su información
 * y fórmulas teóricas sin acoplarse a niveles o controladores específicos.
 *
 * Don't Repeat Yourself (DRY):
 * Componente modal reutilizable disponible para cualquier tipo de ejercicio del catálogo.
 */
class VentanaPista private constructor(
    val stage: Stage,
    val problema: Problema
) {

    companion object {
        /**
         * Crea y despliega el diálogo modal de pista para el problema especificado.
         *
         * @param propietario Ventana ([Window] o [Stage]) sobre la cual se monta la modalidad.
         * @param problema Ejercicio físico activo del cual se extraerán pista y fórmulas.
         * @return La instancia del [Stage] modal configurado.
         */
        fun mostrar(propietario: Window?, problema: Problema): Stage {
            val modalStage = Stage().apply {
                title = "Pistas y Fórmulas de Física"
                initModality(Modality.APPLICATION_MODAL)
                propietario?.let { initOwner(it) }
            }

            val contenedor = construirContenido(modalStage, problema)
            val scene = Scene(contenedor, 460.0, 420.0).apply {
                val css = javaClass.getResource(GameConfig.RUTA_ESTILOS)?.toExternalForm()
                css?.let { stylesheets.add(it) }
            }

            modalStage.scene = scene
            modalStage.isResizable = false

            if (Platform.isFxApplicationThread()) {
                try {
                    modalStage.show()
                } catch (_: Exception) {
                    // Protección ante entornos de pruebas headless sin display gráfico
                }
            } else {
                Platform.runLater {
                    try {
                        modalStage.show()
                    } catch (_: Exception) {
                        // Protección ante entornos de pruebas headless
                    }
                }
            }

            return modalStage
        }

        /**
         * Construye la estructura visual del modal con temática espacial neón.
         */
        private fun construirContenido(stage: Stage, problema: Problema): VBox {
            // 1. Título destacado del modal
            val lblTitulo = Label("💡 Pistas y Fórmulas de Física").apply {
                styleClass.addAll("titulo", "modal-pista-titulo")
            }

            // 2. Subtítulo con tipo y dificultad del problema
            val lblSubtitulo = Label("Misión #${problema.id} ── ${problema.tipo.nombre} (${problema.dificultad.nombreVisible})").apply {
                styleClass.addAll("label", "subtitulo-menu")
            }

            // 3. Tarjeta de la pista conceptual
            val lblPistaTitulo = Label("Orientación Teórica:").apply {
                style = "-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 12px;"
            }
            val lblPistaTexto = Label(problema.pista).apply {
                styleClass.addAll("label", "enunciado")
                isWrapText = true
                maxWidth = 400.0
            }

            // 4. Panel con las fórmulas cinemáticas aplicables al tipo de problema
            val lblFormulasTitulo = Label("Fórmulas Cinemáticas Sugeridas:").apply {
                style = "-fx-font-weight: bold; -fx-text-fill: #4ade80; -fx-font-size: 12px;"
            }
            val formulasTexto = problema.estrategiaCalculo.formulasPista
            val lblFormulasTexto = Label(formulasTexto).apply {
                styleClass.addAll("label", "modal-pista-formula")
                isWrapText = true
                maxWidth = 400.0
            }

            // 5. Botón de cierre ("Entendido / Cerrar")
            val btnCerrar = Button("Entendido / Cerrar").apply {
                styleClass.addAll("button", "btn-primario")
                prefWidth = 200.0
                setOnAction { stage.close() }
            }

            return VBox(14.0).apply {
                alignment = Pos.CENTER
                padding = Insets(20.0)
                styleClass.add("modal-pista")
                children.addAll(
                    lblTitulo,
                    lblSubtitulo,
                    lblPistaTitulo,
                    lblPistaTexto,
                    lblFormulasTitulo,
                    lblFormulasTexto,
                    btnCerrar
                )
            }
        }

        /**
         * Retorna las ecuaciones físicas recomendadas delegando en la estrategia polimórfica (OCP).
         */
        fun obtenerFormulasParaTipo(tipo: TipoProblema): String =
            CalculosAuxiliares.resolverEstrategia(tipo).formulasPista
    }
}
