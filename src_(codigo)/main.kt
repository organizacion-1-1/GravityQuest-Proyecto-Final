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
 * Estructurada bajo el paradigma de Programación Orientada a Objetos (POO),
 * hereda del ciclo de vida de [Application] y delega los cálculos físicos
 * a una instancia de [CalculadoraFisica].
 */
class Main : Application() {

    // 1. Instancia de la clase de lógica física
    val calculadora: CalculadoraFisica = CalculadoraFisica()

    // Altura para el problema de caída libre
    val alturaProblema: Double = 20.0

    // 2. Componentes de la interfaz visual encapsulados como propiedades
    val lblInstruccion: Label = Label("Un objeto cae desde 20m. ¿Cuál es su tiempo de caída en segundos?")
    val txtRespuesta: TextField = TextField()
    val btnValidar: Button = Button("Validar Respuesta")
    val lblResultado: Label = Label()

    /**
     * Sobrescribe el método del ciclo de vida de JavaFX para inicializar
     * y desplegar el escenario principal (Stage y Scene).
     */
    override fun start(primaryStage: Stage) {
        // Configuración de controles
        txtRespuesta.promptText = "Ingresa tu respuesta (ej. 2.02)"
        txtRespuesta.maxWidth = 200.0

        // 3. Invocación de objetos y manejo del evento OnAction
        btnValidar.setOnAction {
            validarRespuesta()
        }

        // Organización de componentes en contenedor VBox con espaciado y relleno
        val contenedorPrincipal = VBox(15.0).apply {
            alignment = Pos.CENTER
            padding = Insets(20.0)
            children.addAll(
                lblInstruccion,
                txtRespuesta,
                btnValidar,
                lblResultado
            )
        }

        // Creación del objeto Scene con dimensiones recomendadas (400 x 300)
        val scene = Scene(contenedorPrincipal, 400.0, 300.0)

        // Configuración del Stage
        primaryStage.title = "GravityQuest - Motor Básico"
        primaryStage.scene = scene
        primaryStage.show()
    }

    /**
     * Valida la respuesta numérica ingresada por el usuario delegando la verificación
     * al objeto [calculadora] y actualizando la interfaz con el resultado.
     */
    fun validarRespuesta() {
        val entrada = txtRespuesta.text.trim().replace(',', '.')
        val valorIngresado = entrada.toDoubleOrNull()

        if (valorIngresado == null) {
            lblResultado.text = "Por favor, ingresa un número válido."
            lblResultado.style = "-fx-text-fill: red;"
            return
        }

        // Delegar cálculo del tiempo a la instancia de CalculadoraFisica
        val tiempoEsperado = calculadora.calcularTiempo(alturaProblema)

        // Invocar el método miembro validarResultado para evaluar la respuesta
        val esCorrecto = calculadora.validarResultado(valorIngresado, tiempoEsperado)

        if (esCorrecto) {
            val tiempoFormateado = String.format(java.util.Locale.US, "%.2f", tiempoEsperado)
            lblResultado.text = "¡Correcto! El tiempo de caída es aproximadamente $tiempoFormateado s."
            lblResultado.style = "-fx-text-fill: green;"
        } else {
            lblResultado.text = "Incorrecto. Intenta de nuevo."
            lblResultado.style = "-fx-text-fill: red;"
        }
    }
}

/**
 * Punto de entrada principal de la aplicación.
 */
fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}
