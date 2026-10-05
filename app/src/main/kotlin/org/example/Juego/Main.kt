package gravityquest

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Stage

/**
 * Ventana principal de la aplicación GravityQuest implementada con JavaFX.
 *
 * Estructurada bajo el paradigma de Programación Orientada a Objetos (POO)
 * y el Principio de Responsabilidad Única (SRP):
 * - Física y cálculos: Delegados a [CalculadoraFisica] en Formulas.kt.
 * - Estructura visual y eventos: Encapsulados en [Main].
 * - Apariencia estética: Definida exclusivamente en assets/style.css.
 */
class Main : Application() {

    // 1. Instancia de la clase de lógica física (SRP: delegación de física)
    val calculadora: CalculadoraFisica = CalculadoraFisica()

    // Altura para el problema de caída libre
    val alturaProblema: Double = 20.0

    // 2. Componentes de la interfaz visual encapsulados como propiedades
    val lblTitulo: Label = Label("GravityQuest")
    val lblInstruccion: Label = Label("Un objeto cae desde 20m. ¿Cuál es su tiempo de caída en segundos?")
    val txtRespuesta: TextField = TextField()
    val btnValidar: Button = Button("Validar Respuesta")
    val lblResultado: Label = Label()

    init {
        // Asignación de clases de estilo CSS a los componentes visuales
        lblTitulo.styleClass.add("titulo")
        lblInstruccion.styleClass.add("label")
        txtRespuesta.styleClass.add("text-field")
        btnValidar.styleClass.add("button")
        lblResultado.styleClass.add("label")
    }

    /**
     * Sobrescribe el método del ciclo de vida de JavaFX para inicializar
     * y desplegar el escenario principal (Stage y Scene).
     */
    override fun start(primaryStage: Stage) {
        // Configuración de controles
        txtRespuesta.promptText = "Ingresa tu respuesta (ej. 2.02)"
        txtRespuesta.maxWidth = 200.0

        // Invocación de objetos y manejo del evento OnAction
        btnValidar.setOnAction {
            validarRespuesta()
        }

        // Organización de componentes en contenedor VBox con espaciado y relleno
        val contenedorPrincipal = VBox(15.0).apply {
            alignment = Pos.CENTER
            padding = Insets(20.0)
            styleClass.add("root")
            children.addAll(
                lblTitulo,
                lblInstruccion,
                txtRespuesta,
                btnValidar,
                lblResultado
            )
        }

        // Creación del objeto Scene con dimensiones recomendadas (400 x 320)
        val scene = Scene(contenedorPrincipal, 400.0, 320.0)

        // Carga del archivo de recursos assets/style.css y asociación al objeto Scene
        scene.stylesheets.add(javaClass.getResource("/assets/style.css")?.toExternalForm())

        // Configuración del Stage
        primaryStage.title = "GravityQuest - Motor Básico"
        primaryStage.scene = scene
        primaryStage.show()
    }

    /**
     * Valida la respuesta numérica ingresada por el usuario delegando la verificación
     * al objeto [calculadora] y actualizando la interfaz y las clases CSS dinámicas.
     */
    fun validarRespuesta() {
        val entrada = txtRespuesta.text.trim().replace(',', '.')
        val valorIngresado = entrada.toDoubleOrNull()

        // Limpiar clases de estado previas para mantener consistencia visual
        lblResultado.styleClass.removeAll("resultado-exito", "resultado-error")

        if (valorIngresado == null) {
            lblResultado.text = "Por favor, ingresa un número válido."
            lblResultado.styleClass.add("resultado-error")
            return
        }

        // Delegar cálculo del tiempo a la instancia de CalculadoraFisica
        val tiempoEsperado = calculadora.calcularTiempo(alturaProblema)

        // Invocar el método miembro validarResultado para evaluar la respuesta
        val esCorrecto = calculadora.validarResultado(valorIngresado, tiempoEsperado)

        if (esCorrecto) {
            val tiempoFormateado = String.format(java.util.Locale.US, "%.2f", tiempoEsperado)
            lblResultado.text = "¡Correcto! El tiempo de caída es aproximadamente $tiempoFormateado s."
            lblResultado.styleClass.add("resultado-exito")
        } else {
            lblResultado.text = "Incorrecto. Intenta de nuevo."
            lblResultado.styleClass.add("resultado-error")
        }
    }
}

/**
 * Punto de entrada principal de la aplicación.
 */
fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}
