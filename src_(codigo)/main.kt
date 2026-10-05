package gravityquest

import javafx.animation.AnimationTimer
import javafx.animation.ScaleTransition
import javafx.animation.TranslateTransition
import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import javafx.util.Duration
import java.util.Locale

/**
 * Modelo de estado que encapsula las propiedades físicas del objeto en caída libre.
 *
 * Principio POO (Encapsulamiento):
 * Agrupa y protege las variables del estado físico ([posicionY], [velocidadY], [tiempo] y [enSuelo])
 * impidiendo modificaciones inconsistentes desde el exterior y exponiendo métodos de actualización controlada.
 */
class ObjetoSimulado(
    val alturaMaxima: Double = 20.0
) {
    /** Posición vertical actual en metros (desde el punto de lanzamiento y = 0 hasta alturaMaxima). */
    var posicionY: Double = 0.0
        private set

    /** Velocidad vertical instantánea en m/s. */
    var velocidadY: Double = 0.0
        private set

    /** Tiempo transcurrido de la caída en segundos. */
    var tiempo: Double = 0.0
        private set

    /** Indica si el objeto ha llegado al suelo (fin de la caída). */
    var enSuelo: Boolean = false
        private set

    /**
     * Actualiza el estado físico del objeto asegurando que la posición se mantenga dentro de los límites válidos.
     *
     * @param posicion Posición vertical actual en metros.
     * @param velocidad Velocidad instantánea actual en m/s.
     * @param tiempoTranscurrido Tiempo transcurrido de simulación en segundos.
     * @param haLlegadoAlSuelo Indicador de si el objeto alcanzó el suelo.
     */
    fun actualizar(posicion: Double, velocidad: Double, tiempoTranscurrido: Double, haLlegadoAlSuelo: Boolean) {
        this.posicionY = posicion.coerceIn(0.0, alturaMaxima)
        this.velocidadY = velocidad
        this.tiempo = tiempoTranscurrido
        this.enSuelo = haLlegadoAlSuelo
    }

    /**
     * Restablece el estado físico a las condiciones de inicio (reposo en la altura inicial).
     */
    fun reiniciar() {
        posicionY = 0.0
        velocidadY = 0.0
        tiempo = 0.0
        enSuelo = false
    }
}

/**
 * Contrato de abstracción para el renderizado visual de la simulación.
 *
 * Principio de Inversión de Dependencias (DIP):
 * Desacopla la lógica de simulación y actualización del estado físico de los detalles
 * concretos del subsistema de renderizado gráfico (JavaFX Canvas, GraphicsContext, etc.).
 */
interface RenderizadorSimulacion {
    /**
     * Dibuja el estado actual del [ObjetoSimulado] sobre la superficie de renderizado.
     */
    fun renderizar(objeto: ObjetoSimulado)

    /**
     * Limpia la superficie de renderizado para preparar el siguiente fotograma.
     */
    fun limpiar()
}

/**
 * Implementación de [RenderizadorSimulacion] para un [Canvas] de JavaFX.
 *
 * Principio de Responsabilidad Única (SRP):
 * Se encarga exclusivamente de:
 * 1. La conversión de unidades físicas (metros) a coordenadas del lienzo (píxeles).
 * 2. La limpieza y renderizado de la escena en el [GraphicsContext].
 */
class RenderizadorCanvas(
    val canvas: Canvas,
    val alturaMaximaMetros: Double = 20.0
) : RenderizadorSimulacion {

    private val gc: GraphicsContext = canvas.graphicsContext2D
    val radioObjeto: Double = 12.0
    val diametroObjeto: Double = radioObjeto * 2.0
    val margenSuperior: Double = 35.0
    val margenInferior: Double = 35.0

    /**
     * Factor de escala para convertir metros a píxeles en el lienzo.
     */
    val escalaPixelesPorMetro: Double
        get() {
            val alturaUtil = canvas.height - margenSuperior - margenInferior - diametroObjeto
            return if (alturaMaximaMetros > 0.0) alturaUtil / alturaMaximaMetros else 1.0
        }

    /**
     * Convierte una posición física en metros a la coordenada vertical Y en píxeles.
     */
    fun convertirMetroAPixelY(metros: Double): Double {
        return margenSuperior + (metros * escalaPixelesPorMetro)
    }

    override fun limpiar() {
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
    }

    override fun renderizar(objeto: ObjetoSimulado) {
        limpiar()

        val ancho = canvas.width
        val alto = canvas.height

        // Fondo del lienzo con temática espacial oscura
        gc.fill = Color.web("#0f172a")
        gc.fillRect(0.0, 0.0, ancho, alto)

        // Borde estilizado del contenedor del lienzo
        gc.stroke = Color.web("#334155")
        gc.lineWidth = 1.5
        gc.strokeRect(1.0, 1.0, ancho - 2.0, alto - 2.0)

        // Línea de referencia inicial (h = 20 m / y = 0 m)
        val inicioY = margenSuperior + diametroObjeto
        gc.stroke = Color.web("#475569")
        gc.lineWidth = 1.0
        gc.strokeLine(25.0, inicioY, ancho - 25.0, inicioY)
        gc.fill = Color.web("#94a3b8")
        gc.font = Font.font("Segoe UI", FontWeight.NORMAL, 10.0)
        gc.fillText("0 m (Inicio)", 25.0, margenSuperior - 6.0)

        // Línea de suelo (y = alturaMaximaMetros)
        val sueloY = margenSuperior + (alturaMaximaMetros * escalaPixelesPorMetro) + diametroObjeto
        gc.stroke = Color.web("#38bdf8")
        gc.lineWidth = 2.0
        gc.strokeLine(20.0, sueloY, ancho - 20.0, sueloY)
        gc.fill = Color.web("#38bdf8")
        gc.font = Font.font("Segoe UI", FontWeight.BOLD, 10.0)
        gc.fillText("${alturaMaximaMetros.toInt()} m (Suelo)", 25.0, sueloY + 16.0)

        // Guía vertical de trayectoria de caída libre
        gc.stroke = Color.web("#1e293b")
        gc.lineWidth = 1.0
        gc.strokeLine(ancho / 2.0, inicioY, ancho / 2.0, sueloY)

        // Conversión de unidades físicas a coordenadas del lienzo (píxeles)
        val pixelY = convertirMetroAPixelY(objeto.posicionY)
        val pixelX = (ancho / 2.0) - radioObjeto

        // Dibujo del objeto en caída libre (esfera con resplandor neón)
        val colorEsfera = if (objeto.enSuelo) Color.web("#4ade80") else Color.web("#38bdf8")
        gc.fill = colorEsfera
        gc.fillOval(pixelX, pixelY, diametroObjeto, diametroObjeto)

        // Contorno nítido de la esfera
        gc.stroke = Color.web("#ffffff")
        gc.lineWidth = 1.5
        gc.strokeOval(pixelX, pixelY, diametroObjeto, diametroObjeto)

        // Telemetría física en tiempo real en la parte superior del lienzo
        gc.fill = Color.web("#e2e8f0")
        gc.font = Font.font("Segoe UI", FontWeight.BOLD, 10.0)
        val infoTexto = String.format(
            Locale.US,
            "t: %.2fs | y: %.1fm | v: %.1fm/s",
            objeto.tiempo,
            objeto.posicionY,
            objeto.velocidadY
        )
        gc.fillText(infoTexto, ancho - 180.0, 18.0)
    }
}

/**
 * Contrato de abstracción para la gestión de retroalimentación de la interfaz de usuario.
 *
 * Principio de Inversión de Dependencias (DIP):
 * Permite que los controladores o componentes de lógica dependan de una interfaz de feedback
 * en lugar de implementaciones concretas de animaciones y estilos de JavaFX.
 */
interface IGestorFeedback {
    fun mostrarExito(mensaje: String = "¡Correcto! Respuesta dentro del margen de error.")
    fun mostrarErrorPrecision(mensaje: String = "¡Intenta de nuevo! Tu respuesta no es precisa.")
    fun mostrarErrorFormato(mensaje: String = "¡Entrada inválida! Por favor ingresa un número.")
    fun limpiar()
}

/**
 * Gestor dinámico de retroalimentación visual con animaciones y estilos JavaFX.
 *
 * Principio de Responsabilidad Única (SRP):
 * Centraliza y aisla exclusivamente los estilos visuales CSS (.resultado-exito, .resultado-error)
 * y los efectos de animación ([ScaleTransition], [TranslateTransition]) sobre el componente [Label],
 * manteniéndolos completamente desacoplados de la lógica de validación matemática.
 */
class GestorFeedbackVisual(
    private val labelResultado: Label
) : IGestorFeedback {

    override fun mostrarExito(mensaje: String) {
        labelResultado.text = mensaje
        labelResultado.styleClass.removeAll("resultado-error")
        if (!labelResultado.styleClass.contains("resultado-exito")) {
            labelResultado.styleClass.add("resultado-exito")
        }
        ejecutarAnimacionExito()
    }

    override fun mostrarErrorPrecision(mensaje: String) {
        mostrarError(mensaje)
    }

    override fun mostrarErrorFormato(mensaje: String) {
        mostrarError(mensaje)
    }

    private fun mostrarError(mensaje: String) {
        labelResultado.text = mensaje
        labelResultado.styleClass.removeAll("resultado-exito")
        if (!labelResultado.styleClass.contains("resultado-error")) {
            labelResultado.styleClass.add("resultado-error")
        }
        ejecutarAnimacionError()
    }

    override fun limpiar() {
        labelResultado.text = ""
        labelResultado.styleClass.removeAll("resultado-exito", "resultado-error")
        restablecerTransformaciones()
    }

    private fun restablecerTransformaciones() {
        labelResultado.scaleX = 1.0
        labelResultado.scaleY = 1.0
        labelResultado.translateX = 0.0
    }

    /**
     * Ejecuta una animación [ScaleTransition] para generar un efecto de pulso ante respuestas correctas.
     */
    private fun ejecutarAnimacionExito() {
        restablecerTransformaciones()
        try {
            val scaleTransition = ScaleTransition(Duration.millis(200.0), labelResultado).apply {
                fromX = 1.0
                fromY = 1.0
                toX = 1.15
                toY = 1.15
                cycleCount = 2
                isAutoReverse = true
                setOnFinished {
                    restablecerTransformaciones()
                }
            }
            scaleTransition.play()
        } catch (_: Exception) {
            // Protección ante entornos de pruebas headless sin soporte de animación
        }
    }

    /**
     * Ejecuta una animación [TranslateTransition] para generar un efecto de vibración ante respuestas erróneas.
     */
    private fun ejecutarAnimacionError() {
        restablecerTransformaciones()
        try {
            val translateTransition = TranslateTransition(Duration.millis(50.0), labelResultado).apply {
                fromX = -8.0
                toX = 8.0
                cycleCount = 6
                isAutoReverse = true
                setOnFinished {
                    restablecerTransformaciones()
                }
            }
            translateTransition.play()
        } catch (_: Exception) {
            // Protección ante entornos de pruebas headless sin soporte de animación
        }
    }
}

/**
 * Ventana principal de la aplicación GravityQuest implementada con JavaFX.
 *
 * Arquitectura basada en Programación Orientada a Objetos (POO) y Principios SOLID:
 * - SRP:
 *     * Cálculos y fórmulas físicas: Delegados a [CalculadoraFisica].
 *     * Representación y dibujo en pantalla: Delegados a [RenderizadorCanvas].
 *     * Estilos y animaciones de feedback: Delegados a [GestorFeedbackVisual].
 * - DIP:
 *     * Desacoplamiento entre la lógica de simulación y el renderizado a través de [RenderizadorSimulacion].
 *     * Desacoplamiento de la retroalimentación visual a través de [IGestorFeedback].
 *     * Desacoplamiento de la calculadora mediante [ICalculadoraFisica].
 */
class Main : Application() {

    // 1. Delegación de física (SRP)
    val calculadora: CalculadoraFisica = CalculadoraFisica()

    // Altura fijada para el problema de caída libre
    val alturaProblema: Double = 20.0

    // 2. Modelo de estado físico simulado (POO / Encapsulamiento)
    val objetoSimulado: ObjetoSimulado = ObjetoSimulado(alturaMaxima = alturaProblema)

    // 3. Componentes visuales de la interfaz de usuario
    val lblTitulo: Label = Label("GravityQuest")
    val lblInstruccion: Label = Label("Un objeto cae desde 20m. ¿Cuál es su tiempo de caída en segundos?")
    val txtRespuesta: TextField = TextField()
    val btnValidar: Button = Button("Validar Respuesta")
    val lblResultado: Label = Label()

    // Componentes del área de simulación física
    val canvasSimulacion: Canvas = Canvas(300.0, 300.0)
    val btnSimular: Button = Button("Simular Caída")

    // 4. Abstracciones de renderizado y feedback (DIP)
    var renderizador: RenderizadorSimulacion
    var gestorFeedback: IGestorFeedback

    // Control del bucle de animación con AnimationTimer a 60 FPS
    var timerSimulacion: AnimationTimer? = null
    var tiempoInicioNano: Long = 0L
    var simulacionEnCurso: Boolean = false
        private set

    init {
        // Asignación de clases de estilo CSS a los componentes visuales
        lblTitulo.styleClass.add("titulo")
        lblInstruccion.styleClass.add("label")
        txtRespuesta.styleClass.add("text-field")
        btnValidar.styleClass.add("button")
        btnSimular.styleClass.add("button")
        lblResultado.styleClass.add("label")
        canvasSimulacion.styleClass.add("canvas-simulacion")

        // Instanciación de abstracciones desacopladas (DIP / SRP)
        renderizador = RenderizadorCanvas(canvasSimulacion, alturaProblema)
        gestorFeedback = GestorFeedbackVisual(lblResultado)

        // Configuración del bucle de simulación a 60 FPS
        configurarAnimationTimer()

        // Renderizado del fotograma inicial en reposo
        renderizador.renderizar(objetoSimulado)
    }

    /**
     * Configura el bucle de animación de 60 FPS utilizando [AnimationTimer].
     *
     * Bucle de renderizado:
     * - Actualiza la posición del objeto usando la física de caída libre (y = 1/2 * g * t²).
     * - Convierte unidades físicas (metros) a coordenadas del lienzo (píxeles) en el renderizador.
     * - Dibuja el estado actualizado limpiando la pantalla en cada fotograma.
     */
    private fun configurarAnimationTimer() {
        timerSimulacion = object : AnimationTimer() {
            override fun handle(now: Long) {
                if (tiempoInicioNano == 0L) {
                    tiempoInicioNano = now
                }

                val deltaSegundos = (now - tiempoInicioNano) / 1_000_000_000.0
                val tiempoTotalCaida = calculadora.calcularTiempo(alturaProblema)

                if (deltaSegundos >= tiempoTotalCaida) {
                    // El objeto alcanza el suelo: se detiene el movimiento
                    val vFinal = calculadora.calcularVelocidadFinal(alturaProblema)
                    objetoSimulado.actualizar(alturaProblema, vFinal, tiempoTotalCaida, haLlegadoAlSuelo = true)
                    renderizador.renderizar(objetoSimulado)
                    detenerSimulacion()
                } else {
                    // Actualización de física delegada a CalculadoraFisica (SRP): y = 1/2 * g * t²
                    val posY = calculadora.calcularPosicionCaida(deltaSegundos)
                    val vY = calculadora.g * deltaSegundos
                    objetoSimulado.actualizar(posY, vY, deltaSegundos, haLlegadoAlSuelo = false)
                    renderizador.renderizar(objetoSimulado)
                }
            }
        }
    }

    /**
     * Inicia o reinicia la simulación del objeto en caída libre.
     */
    fun iniciarOReiniciarSimulacion() {
        detenerSimulacion()
        tiempoInicioNano = 0L
        objetoSimulado.reiniciar()
        renderizador.renderizar(objetoSimulado)
        simulacionEnCurso = true
        timerSimulacion?.start()
    }

    /**
     * Detiene el temporizador de la simulación.
     */
    fun detenerSimulacion() {
        timerSimulacion?.stop()
        simulacionEnCurso = false
    }

    /**
     * Inicializa y despliega el escenario principal (Stage y Scene).
     */
    override fun start(primaryStage: Stage) {
        txtRespuesta.promptText = "Ingresa tu respuesta (ej. 2.02)"
        txtRespuesta.maxWidth = 200.0

        btnValidar.setOnAction {
            validarRespuesta()
        }

        btnSimular.setOnAction {
            iniciarOReiniciarSimulacion()
        }

        // Renderizado del fotograma inicial
        renderizador.renderizar(objetoSimulado)

        // Panel izquierdo: Formulario de interacción y validación
        val panelControl = VBox(15.0).apply {
            alignment = Pos.CENTER
            maxWidth = 320.0
            children.addAll(
                lblTitulo,
                lblInstruccion,
                txtRespuesta,
                btnValidar,
                lblResultado
            )
        }

        // Panel derecho: Lienzo de simulación gráfica y botón interactivo
        val panelSimulacion = VBox(12.0).apply {
            alignment = Pos.CENTER
            styleClass.add("panel-simulacion")
            children.addAll(
                canvasSimulacion,
                btnSimular
            )
        }

        // Contenedor principal horizontal
        val contenedorPrincipal = HBox(25.0).apply {
            alignment = Pos.CENTER
            padding = Insets(20.0)
            styleClass.add("root")
            children.addAll(
                panelControl,
                panelSimulacion
            )
        }

        val scene = Scene(contenedorPrincipal, 720.0, 440.0)
        scene.stylesheets.add(javaClass.getResource("/assets/style.css")?.toExternalForm())

        primaryStage.title = "GravityQuest - Motor Básico"
        primaryStage.scene = scene
        primaryStage.show()
    }

    /**
     * Valida la respuesta numérica ingresada por el usuario delegando la verificación física
     * a [calculadora] (SRP) y estructurando la retroalimentación dinámica y visual mediante [gestorFeedback].
     */
    fun validarRespuesta() {
        val entrada = txtRespuesta.text.trim().replace(',', '.')
        val valorIngresado = entrada.toDoubleOrNull()

        if (valorIngresado == null) {
            gestorFeedback.mostrarErrorFormato("¡Entrada inválida! Por favor ingresa un número.")
            return
        }

        // Delegar cálculo físico a CalculadoraFisica (SRP)
        val tiempoEsperado = calculadora.calcularTiempo(alturaProblema)
        val esCorrecto = calculadora.validarResultado(valorIngresado, tiempoEsperado)

        if (esCorrecto) {
            gestorFeedback.mostrarExito("¡Correcto! Respuesta dentro del margen de error.")
        } else {
            gestorFeedback.mostrarErrorPrecision("¡Intenta de nuevo! Tu respuesta no es precisa.")
        }
    }
}

/**
 * Punto de entrada principal de la aplicación.
 */
fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}
