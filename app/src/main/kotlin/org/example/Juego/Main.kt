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
 * ============================================================================
 * GravityQuest - Bucle de Juego Interactivo y Arquitectura SOLID
 * ============================================================================
 *
 * Estructura de Arquitectura:
 * 1. Principio de Responsabilidad Única (SRP):
 *    - Cálculos matemáticos y cinemáticos: [CalculadoraFisica] ([ICalculadoraFisica]) en Formulas.kt.
 *    - Ciclo de vida y orquestación general: [GravityQuestApp].
 *    - Componentes de interfaz desacoplados:
 *        * [VistaFormulario]: Entradas de datos, enunciado, botón de validación y etiqueta de estado.
 *        * [VistaSimulacion]: Lienzo gráfico [Canvas] y controles de simulación.
 *        * [GestorFeedbackVisual]: Animaciones y estilos CSS dinámicos de retroalimentación.
 *        * [RenderizadorCanvas]: Conversión de física a píxeles y renderizado gráfico.
 *
 * 2. Principio Abierto/Cerrado (OCP):
 *    - Definición de ejercicios mediante la abstracción [NivelEjercicio].
 *    - Extensible para futuros niveles (tiro vertical, planetas con gravedades alternas)
 *      en el Milestone 2 sin modificar el orquestador ni la interfaz gráfica.
 *
 * 3. Inversión de Dependencias (DIP) y Encapsulamiento:
 *    - Vistas y controladores dependen de interfaces ([ICalculadoraFisica], [RenderizadorSimulacion],
 *      [IGestorFeedback], [NivelEjercicio]).
 *    - Estado físico protegido en [ObjetoSimulado] mediante propiedades con setters privados.
 *    - Todas las llamadas de cálculo y validación física se canalizan mediante la instancia
 *      de [CalculadoraFisica].
 */

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
 * Contrato de abstracción para definir ejercicios o niveles en GravityQuest.
 *
 * Principio Abierto/Cerrado (OCP):
 * Permite incorporar nuevos desafíos físicos (lanzamientos con velocidad inicial, gravedad lunar, etc.)
 * en el Milestone 2 sin necesidad de reescribir la interfaz de usuario ni la orquestación principal.
 */
interface NivelEjercicio {
    val idNivel: Int
    val titulo: String
    val enunciado: String
    val alturaInicialMetros: Double
    val unidadRespuesta: String
    val margenError: Double

    /**
     * Calcula la respuesta teórica esperada utilizando la física asociada al nivel.
     */
    fun calcularRespuestaEsperada(calculadora: ICalculadoraFisica): Double

    /**
     * Valida la respuesta numérica ingresada por el usuario contra el valor físico teórico.
     */
    fun validarRespuesta(calculadora: ICalculadoraFisica, valorIngresado: Double): Boolean {
        val valorEsperado = calcularRespuestaEsperada(calculadora)
        return calculadora.validarResultado(valorIngresado, valorEsperado, margenError)
    }
}

/**
 * Nivel 1: Caída Libre desde 20.0 metros.
 *
 * Implementa [NivelEjercicio] para el problema de cinemática fundamental.
 */
class NivelCaidaLibre(
    override val idNivel: Int = 1,
    override val titulo: String = "Nivel 1: Caída Libre",
    override val enunciado: String = "Un objeto cae libremente desde una altura de 20.0 metros (20m). Calcula el tiempo de caída en segundos (g = 9.81 m/s²).",
    override val alturaInicialMetros: Double = 20.0,
    override val unidadRespuesta: String = "s",
    override val margenError: Double = 0.05
) : NivelEjercicio {
    override fun calcularRespuestaEsperada(calculadora: ICalculadoraFisica): Double {
        return calculadora.calcularTiempo(alturaInicialMetros)
    }
}

/**
 * Contrato de abstracción para el renderizado visual de la simulación.
 *
 * Principio de Inversión de Dependencias (DIP):
 * Desacopla la lógica de simulación y actualización del estado físico de los detalles
 * concretos del subsistema de dibujo gráfico (Canvas, GraphicsContext, etc.).
 */
interface RenderizadorSimulacion {
    /** Dibuja el estado actual del [ObjetoSimulado] sobre la superficie de renderizado. */
    fun renderizar(objeto: ObjetoSimulado)

    /** Limpia la superficie de renderizado para preparar el siguiente fotograma. */
    fun limpiar()
}

/**
 * Implementación de [RenderizadorSimulacion] sobre un [Canvas] de JavaFX.
 *
 * Principio de Responsabilidad Única (SRP):
 * Se encarga exclusivamente de:
 * 1. La conversión de unidades físicas (metros) a coordenadas del lienzo (píxeles).
 * 2. La limpieza y renderizado estético de la escena en el [GraphicsContext].
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

    /** Factor de escala para convertir metros físicos a píxeles en el lienzo. */
    val escalaPixelesPorMetro: Double
        get() {
            val alturaUtil = canvas.height - margenSuperior - margenInferior - diametroObjeto
            return if (alturaMaximaMetros > 0.0) alturaUtil / alturaMaximaMetros else 1.0
        }

    /** Convierte una posición física en metros a la coordenada vertical Y en píxeles. */
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

        // Esfera con resplandor neón según estado
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
 * Permite que los controladores dependan de un contrato de feedback sin acoplarse
 * a las clases concretas de animación o controles de JavaFX.
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
 * y los efectos de animación ([ScaleTransition], [TranslateTransition]) sobre el componente [Label].
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
     * Ejecuta una animación [TranslateTransition] para generar un efecto de vibración horizontal ante respuestas erróneas.
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
 * Componente desacoplado de la vista del formulario de entrada y estado.
 *
 * Principio de Responsabilidad Única (SRP):
 * Aísla y estructura los componentes de control del usuario: título, enunciado del ejercicio,
 * campo de texto para la respuesta numérica, botón "Validar y Simular" y etiqueta de resultado.
 */
class VistaFormulario(
    val lblTitulo: Label = Label("GravityQuest"),
    val lblInstruccion: Label = Label(),
    val txtRespuesta: TextField = TextField(),
    val btnValidar: Button = Button("Validar y Simular"),
    val lblResultado: Label = Label()
) {
    val root: VBox

    init {
        lblTitulo.styleClass.add("titulo")
        lblInstruccion.styleClass.addAll("label", "enunciado")
        lblInstruccion.isWrapText = true
        lblInstruccion.maxWidth = 320.0

        txtRespuesta.styleClass.add("text-field")
        txtRespuesta.promptText = "Ingresa tu respuesta (ej. 2.02)"
        txtRespuesta.maxWidth = 220.0

        btnValidar.styleClass.addAll("button", "btn-primario")
        btnValidar.maxWidth = 220.0

        lblResultado.styleClass.add("label")
        lblResultado.isWrapText = true
        lblResultado.maxWidth = 320.0

        root = VBox(16.0).apply {
            alignment = Pos.CENTER
            maxWidth = 340.0
            styleClass.add("panel-control")
            children.addAll(
                lblTitulo,
                lblInstruccion,
                txtRespuesta,
                btnValidar,
                lblResultado
            )
        }
    }

    fun configurarEnunciado(texto: String) {
        lblInstruccion.text = texto
    }
}

/**
 * Componente desacoplado de la vista del área de simulación física.
 *
 * Principio de Responsabilidad Única (SRP):
 * Aísla el contenedor del lienzo [Canvas] y los controles auxiliares de reproducción visual.
 */
class VistaSimulacion(
    val canvasSimulacion: Canvas = Canvas(300.0, 300.0),
    val btnSimular: Button = Button("Simular Caída")
) {
    val root: VBox

    init {
        canvasSimulacion.styleClass.add("canvas-simulacion")
        btnSimular.styleClass.addAll("button", "btn-secundario")
        btnSimular.maxWidth = 200.0

        root = VBox(14.0).apply {
            alignment = Pos.CENTER
            styleClass.add("panel-simulacion")
            children.addAll(
                canvasSimulacion,
                btnSimular
            )
        }
    }
}

/**
 * Orquestador principal y ciclo de vida de JavaFX para GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Conecta el bucle de interacción de usuario, la validación física y el bucle
 * de sincronización gráfica en tiempo real.
 */
open class GravityQuestApp : Application() {

    // 1. Delegación física mediante CalculadoraFisica (SRP / DIP)
    val calculadora: CalculadoraFisica = CalculadoraFisica()

    // 2. Modelo de ejercicio extensible (OCP)
    var nivelActual: NivelEjercicio = NivelCaidaLibre()

    // Altura del problema actual en metros
    val alturaProblema: Double
        get() = nivelActual.alturaInicialMetros

    // 3. Estado físico simulado encapsulado (POO)
    val objetoSimulado: ObjetoSimulado = ObjetoSimulado(alturaMaxima = alturaProblema)

    // 4. Vistas desacopladas (SRP)
    val vistaFormulario: VistaFormulario = VistaFormulario()
    val vistaSimulacion: VistaSimulacion = VistaSimulacion()

    // Acceso directo a controles para máxima compatibilidad
    val lblTitulo: Label get() = vistaFormulario.lblTitulo
    val lblInstruccion: Label get() = vistaFormulario.lblInstruccion
    val txtRespuesta: TextField get() = vistaFormulario.txtRespuesta
    val btnValidar: Button get() = vistaFormulario.btnValidar
    val lblResultado: Label get() = vistaFormulario.lblResultado
    val canvasSimulacion: Canvas get() = vistaSimulacion.canvasSimulacion
    val btnSimular: Button get() = vistaSimulacion.btnSimular

    // Abstracciones de renderizado y feedback (DIP)
    var renderizador: RenderizadorSimulacion
    var gestorFeedback: IGestorFeedback

    // Control del temporizador de animación sincronizado a 60 FPS
    var timerSimulacion: AnimationTimer? = null
    var tiempoInicioNano: Long = 0L
    var simulacionEnCurso: Boolean = false
        private set

    init {
        vistaFormulario.configurarEnunciado(nivelActual.enunciado)
        renderizador = RenderizadorCanvas(canvasSimulacion, alturaProblema)
        gestorFeedback = GestorFeedbackVisual(lblResultado)
        configurarAnimationTimer()
        renderizador.renderizar(objetoSimulado)
    }

    /**
     * Configura el bucle de animación a 60 FPS con [AnimationTimer].
     *
     * Sincroniza en tiempo real la cinemática de caída libre:
     * - Calcula delta de tiempo real en segundos.
     * - Actualiza la posición física usando CalculadoraFisica (y = 1/2 * g * t²).
     * - Renderiza la esfera en la coordenada escalada en píxeles.
     * - Detiene automáticamente la simulación al alcanzar el suelo.
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
                    val vFinal = calculadora.calcularVelocidadFinal(alturaProblema)
                    objetoSimulado.actualizar(alturaProblema, vFinal, tiempoTotalCaida, haLlegadoAlSuelo = true)
                    renderizador.renderizar(objetoSimulado)
                    detenerSimulacion()
                } else {
                    val posY = calculadora.calcularPosicionCaida(deltaSegundos)
                    val vY = calculadora.g * deltaSegundos
                    objetoSimulado.actualizar(posY, vY, deltaSegundos, haLlegadoAlSuelo = false)
                    renderizador.renderizar(objetoSimulado)
                }
            }
        }
    }

    /**
     * Inicia o reinicia la simulación del objeto en caída libre en el Canvas.
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
     * Detiene el temporizador de la simulación gráfica.
     */
    fun detenerSimulacion() {
        timerSimulacion?.stop()
        simulacionEnCurso = false
    }

    /**
     * Valida la respuesta numérica ingresada en el [TextField]:
     * 1. Captura el valor y gestiona excepciones si no es un número ([toDoubleOrNull]).
     * 2. Calcula internamente el valor real mediante [CalculadoraFisica.calcularTiempo].
     * 3. Invoca [CalculadoraFisica.validarResultado] con tolerancia (margenError = 0.05).
     * 4. Feedback dinámico:
     *    - Correcto: Aplica clase CSS .resultado-exito, pulso animado ([ScaleTransition])
     *      y desencadena en tiempo real la animación gráfica de caída.
     *    - Incorrecto / Inválido: Aplica clase CSS .resultado-error, vibración horizontal
     *      ([TranslateTransition]) y bloquea/detiene la simulación gráfica.
     *
     * @return `true` si la respuesta fue correcta, `false` en caso contrario.
     */
    fun validarRespuesta(): Boolean {
        val entrada = txtRespuesta.text.trim().replace(',', '.')
        val valorIngresado = entrada.toDoubleOrNull()

        if (valorIngresado == null) {
            detenerSimulacion()
            objetoSimulado.reiniciar()
            renderizador.renderizar(objetoSimulado)
            gestorFeedback.mostrarErrorFormato("¡Entrada inválida! Por favor ingresa un número.")
            return false
        }

        val esCorrecto = nivelActual.validarRespuesta(calculadora, valorIngresado)

        if (esCorrecto) {
            gestorFeedback.mostrarExito("¡Correcto! Respuesta dentro del margen de error.")
            iniciarOReiniciarSimulacion()
            return true
        } else {
            detenerSimulacion()
            objetoSimulado.reiniciar()
            renderizador.renderizar(objetoSimulado)
            gestorFeedback.mostrarErrorPrecision("¡Intenta de nuevo! Tu respuesta no es precisa.")
            return false
        }
    }

    /**
     * Inicializa y despliega la ventana principal de la aplicación JavaFX.
     */
    override fun start(primaryStage: Stage) {
        btnValidar.setOnAction {
            validarRespuesta()
        }

        txtRespuesta.setOnAction {
            validarRespuesta()
        }

        btnSimular.setOnAction {
            iniciarOReiniciarSimulacion()
        }

        renderizador.renderizar(objetoSimulado)

        val contenedorPrincipal = HBox(25.0).apply {
            alignment = Pos.CENTER
            padding = Insets(20.0)
            styleClass.add("root")
            children.addAll(
                vistaFormulario.root,
                vistaSimulacion.root
            )
        }

        val scene = Scene(contenedorPrincipal, 740.0, 460.0)
        scene.stylesheets.add(javaClass.getResource("/assets/style.css")?.toExternalForm())

        primaryStage.title = "GravityQuest - Motor Básico"
        primaryStage.scene = scene
        primaryStage.show()
    }

    override fun stop() {
        detenerSimulacion()
        super.stop()
    }
}

/**
 * Subclase de compatibilidad directa con puntos de entrada y pruebas preexistentes.
 */
class Main : GravityQuestApp()

/**
 * Punto de entrada principal de la aplicación.
 */
fun main(args: Array<String>) {
    Application.launch(GravityQuestApp::class.java, *args)
}
