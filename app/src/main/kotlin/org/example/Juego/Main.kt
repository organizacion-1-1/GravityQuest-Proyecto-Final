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
import kotlin.math.abs
import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.Problema
import gravityquest.models.TipoProblema
import gravityquest.models.IRepositorioProblemas

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
 * Nivel 2: Lanzamiento Vertical Hacia Abajo (Milestone 2 - Issue 12).
 *
 * Implementa [NivelEjercicio] para un objeto lanzado hacia abajo con velocidad inicial v₀.
 * Principio OCP: Amplía la cinemática física soportada sin modificar clases existentes.
 */
class NivelLanzamientoAbajo(
    override val idNivel: Int = 2,
    override val titulo: String = "Nivel 2: Lanzamiento Hacia Abajo",
    override val enunciado: String = "Un dron lanza un paquete hacia abajo desde 20.0 metros con v₀ = 5.0 m/s. Calcula la velocidad final en m/s (g = 9.81 m/s²).",
    override val alturaInicialMetros: Double = 20.0,
    val velocidadInicial: Double = 5.0,
    override val unidadRespuesta: String = "m/s",
    override val margenError: Double = 0.05
) : NivelEjercicio {
    override fun calcularRespuestaEsperada(calculadora: ICalculadoraFisica): Double {
        return calculadora.calcularVelocidadFinalLanzamientoAbajo(velocidadInicial, alturaInicialMetros)
    }
}

/**
 * Nivel 3: Lanzamiento Vertical Hacia Arriba (Milestone 2 - Issue 13).
 *
 * Implementa [NivelEjercicio] para un objeto lanzado verticalmente hacia arriba desacelerando por gravedad.
 * Principio OCP: Incorpora cinemática en desaceleración preservando la arquitectura y clases previas.
 */
class NivelLanzamientoArriba(
    override val idNivel: Int = 3,
    override val titulo: String = "Nivel 3: Lanzamiento Hacia Arriba",
    override val enunciado: String = "Un cohete sonda es disparado verticalmente hacia arriba con v₀ = 15.0 m/s. Calcula la altura máxima alcanzada en metros (g = 9.81 m/s²).",
    override val alturaInicialMetros: Double = 0.0,
    val velocidadInicial: Double = 15.0,
    override val unidadRespuesta: String = "m",
    override val margenError: Double = 0.05
) : NivelEjercicio {
    override fun calcularRespuestaEsperada(calculadora: ICalculadoraFisica): Double {
        return calculadora.calcularAlturaMaxima(velocidadInicial)
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

    // Repositorio de problemas de física estructurados con POO y SOLID (SRP / DIP)
    val repositorioProblemas: GeneradorProblemas = GeneradorProblemas()

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
 * Realiza una demostración y validación en consola de los modelos de dominio (POO y Principios SOLID).
 *
 * 1. Single Responsibility Principle (SRP): Separa los modelos de datos ([Dificultad], [Problema])
 *    de la lógica del repositorio ([GeneradorProblemas]).
 * 2. Encapsulamiento y Contratos Seguros: Imprime la información inmutable ([val]) y comprueba
 *    la validación de respuestas matemáticas en tiempo de ejecución.
 * 3. Open/Closed Principle (OCP): Incorpora un nuevo tipo de ejercicio sin modificar clases existentes.
 * 4. Invariantes de Dominio: Demuestra que los bloques require/init bloquean estados inválidos.
 *
 * @param repositorio Instancia de [IRepositorioProblemas] a consultar y demostrar.
 * @return El repositorio con la información procesada.
 */
fun demostrarModelosDominio(repositorio: IRepositorioProblemas = GeneradorProblemas()): IRepositorioProblemas {
    println("=".repeat(85))
    println("🌌 GRAVITYQUEST - DEMOSTRACIÓN DE MODELOS DE DOMINIO Y PRINCIPIOS SOLID")
    println("=".repeat(85))
    println("Catálogo de problemas inicializado correctamente.")
    println("Total de problemas registrados: ${repositorio.cantidadTotal()}\n")

    // 1. Consulta y visualización por cada Dificultad (SRP y Encapsulamiento)
    for (dificultad in Dificultad.entries) {
        println("─".repeat(85))
        println("📌 NIVEL: ${dificultad.nombreVisible.uppercase()} (${dificultad.name})")
        println("   Descripción : ${dificultad.descripcion}")
        println("   Tolerancia  : ±${dificultad.margenErrorTolerable}")
        println("─".repeat(85))

        val problemasNivel = repositorio.obtenerPorDificultad(dificultad)
        println("   Ejercicios disponibles en este nivel: ${problemasNivel.size}")

        for (problema in problemasNivel) {
            println("\n   [Problema #${problema.id}] ── Tipo: ${problema.tipo.nombre}")
            println("   • Enunciado : ${problema.enunciado}")
            println("   • Solución  : ${problema.respuestaFormateada()} (tolerancia: ±${problema.dificultad.margenErrorTolerable})")
            println("   • Pista     : ${problema.pista}")

            // Validación POO encapsulada
            val esExacto = problema.validarRespuesta(problema.valorEsperado)
            val esDentroTolerancia = problema.validarRespuesta(problema.valorEsperado + (problema.dificultad.margenErrorTolerable * 0.8))
            val esFueraTolerancia = problema.validarRespuesta(problema.valorEsperado + 5.0)

            println("   • Validación POO:")
            println("       ✓ Valor exacto (${problema.valorEsperado})                   -> ${if (esExacto) "APROBADO" else "RECHAZADO"}")
            println("       ✓ Con tolerancia límite (+${problema.dificultad.margenErrorTolerable * 0.8})     -> ${if (esDentroTolerancia) "APROBADO" else "RECHAZADO"}")
            println("       ✗ Valor erróneo (+5.0)                         -> ${if (!esFueraTolerancia) "RECHAZADO CORRECTAMENTE" else "FALLO"}")
        }
        println()
    }

    // 2. Demostración de Principio Abierto/Cerrado (OCP)
    println("=".repeat(85))
    println("🚀 DEMOSTRACIÓN DE PRINCIPIO ABIERTO/CERRADO (OCP)")
    println("=".repeat(85))
    println("Extendiendo el catálogo con un nuevo ejercicio (Caída en la Luna) sin modificar estructuras existentes...")

    val nuevoProblemaLunar = Problema(
        id = 99,
        enunciado = "Un astronauta suelta un martillo desde 10.0 metros en la Luna (g = 1.62 m/s²). ¿Cuál es el tiempo de caída?",
        dificultad = Dificultad.MEDIO,
        valorEsperado = 3.51,
        unidadMedida = "s",
        pista = "Aplica t = √(2h / g_luna) con g_luna = 1.62 m/s².",
        tipo = TipoProblema.OTRO,
        datosAdicionales = mapOf("lugar" to "Luna", "gravedad" to 1.62, "altura" to 10.0)
    )
    repositorio.agregarProblema(nuevoProblemaLunar)
    val problemaRecuperado = repositorio.obtenerPorId(99)
    println("✓ Nuevo problema registrado dinámicamente: ID=${problemaRecuperado?.id}, Tipo=${problemaRecuperado?.tipo?.nombre}")
    println("✓ Total de problemas actualizados en el repositorio: ${repositorio.cantidadTotal()}")

    // 3. Demostración de Contratos Seguros e Invariantes de Dominio
    println("\n" + "=".repeat(85))
    println("🛡️ DEMOSTRACIÓN DE CONTRATOS SEGUROS E INVARIANTES DE DOMINIO")
    println("=".repeat(85))

    try {
        // Intento de instanciar un problema con datos inconsistentes (enunciado vacío)
        Problema(
            id = 100,
            enunciado = "",
            dificultad = Dificultad.FACIL,
            valorEsperado = 10.0,
            unidadMedida = "s",
            pista = "Pista de prueba"
        )
        println("✗ Fallo: Se permitió crear un problema con enunciado vacío.")
    } catch (e: IllegalArgumentException) {
        println("✓ Invariante de dominio activo: Se impidió crear un problema con enunciado vacío.")
        println("  Excepción capturada: \"${e.message}\"")
    }

    try {
        // Intento de registrar problema con ID duplicado
        repositorio.agregarProblema(nuevoProblemaLunar)
        println("✗ Fallo: Se permitió duplicar un ID de problema existente.")
    } catch (e: IllegalArgumentException) {
        println("✓ Invariante de repositorio activo: Se impidió duplicar un problema con ID ya existente.")
        println("  Excepción capturada: \"${e.message}\"")
    }

    println("=".repeat(85))
    println("✨ DEMOSTRACIÓN DE MODELOS DE DOMINIO COMPLETADA CON ÉXITO")
    println("=".repeat(85) + "\n")

    return repositorio
}

/**
 * Realiza una demostración y validación por consola de las extensiones del motor físico
 * aplicando Programación Orientada a Objetos (POO) y Principios SOLID (OCP, SRP y contratos de dominio).
 *
 * Cubre:
 * 1. Lanzamiento Vertical Hacia Abajo (Milestone 2 - Issue 12):
 *    - Velocidad final: v_f = √(v₀² + 2gh) con v₀ = 5.0 m/s, h = 20.0 m.
 *    - Tiempo de caída:  t = (-v₀ + √(v₀² + 2gh)) / g.
 *    - Comparación con valores teóricos y validación con margen de tolerancia (±0.05).
 *    - Demostración de OCP: Equivalencia con caída libre cuando v₀ = 0.0 m/s.
 *    - Manejo riguroso de excepciones mediante bloques try-catch ante entradas inválidas (v₀ < 0 y h < 0).
 *
 * 2. Lanzamiento Vertical Hacia Arriba (Milestone 2 - Issue 13):
 *    - Altura máxima:     h_máx = v₀² / (2g) con v₀ = 15.0 m/s.
 *    - Tiempo de subida:   t_subida = v₀ / g.
 *    - Tiempo total vuelo: t_total = 2v₀ / g.
 *    - Comprobación de simetría cinemática (t_total == 2 * t_subida).
 *    - Manejo riguroso de excepciones mediante bloques try-catch ante entradas inválidas (v₀ <= 0).
 *
 * @param calculadora Instancia de la clase de servicio puro [CalculadoraFisica].
 * @return La calculadora evaluada.
 */
fun demostrarMotorFisico(calculadora: CalculadoraFisica = CalculadoraFisica()): CalculadoraFisica {
    println("=".repeat(85))
    println("🔬 GRAVITYQUEST - DEMOSTRACIÓN DEL MOTOR FÍSICO (POO Y PRINCIPIOS SOLID)")
    println("=".repeat(85))
    println("Aceleración de gravedad terrestre estándar: g = ${calculadora.g} m/s²\n")

    // ========================================================================
    // 1. Demostración Issue 12: Lanzamiento Vertical Hacia Abajo (v₀ >= 0, a favor de g)
    // ========================================================================
    println("─".repeat(85))
    println("⬇️ MILESTONE 2 (ISSUE 12): LANZAMIENTO VERTICAL HACIA ABAJO")
    println("   Principio OCP: Extensión de CalculadoraFisica sin alterar caída libre previa.")
    println("   Principio SRP: Servicio puro de cálculo cinemático sin dependencias de UI.")
    println("─".repeat(85))

    val v0Abajo = 5.0 // m/s
    val hAbajo = 20.0 // m

    // Valores teóricos:
    // v_f = sqrt(5.0^2 + 2 * 9.81 * 20.0) = sqrt(25.0 + 392.4) = sqrt(417.4) ≈ 20.4304 m/s
    // t   = (-5.0 + 20.43037) / 9.81 ≈ 1.5729 s
    val vfTeorico = 20.43
    val tTeorico = 1.57

    val vfCalculado = calculadora.calcularVelocidadFinalLanzamientoAbajo(v0Abajo, hAbajo)
    val tCalculado = calculadora.calcularTiempoLanzamientoAbajo(v0Abajo, hAbajo)

    val vfValido = calculadora.validarResultado(vfCalculado, vfTeorico, margenError = 0.05)
    val tValido = calculadora.validarResultado(tCalculado, tTeorico, margenError = 0.05)

    println("Parámetros de prueba: v₀ = $v0Abajo m/s, h = $hAbajo m")
    println(String.format(Locale.US, "• Velocidad final calculada : %.4f m/s (Teórico: %.2f m/s) -> %s",
        vfCalculado, vfTeorico, if (vfValido) "APROBADO (±0.05)" else "RECHAZADO"))
    println(String.format(Locale.US, "• Tiempo de caída calculado  : %.4f s   (Teórico: %.2f s)   -> %s",
        tCalculado, tTeorico, if (tValido) "APROBADO (±0.05)" else "RECHAZADO"))

    // Comprobación de OCP: caso v0 = 0 coincide exactamente con caída libre
    val vfCaidaLibre = calculadora.calcularVelocidadFinal(hAbajo)
    val vfAbajoV0Cero = calculadora.calcularVelocidadFinalLanzamientoAbajo(0.0, hAbajo)
    val tCaidaLibre = calculadora.calcularTiempo(hAbajo)
    val tAbajoV0Cero = calculadora.calcularTiempoLanzamientoAbajo(0.0, hAbajo)
    println("\n• Verificación OCP (Consistencia con Caída Libre si v₀ = 0 m/s):")
    println(String.format(Locale.US, "  - v_f (Caída Libre: %.4f m/s) vs v_f (Lanzamiento v₀=0: %.4f m/s) -> %s",
        vfCaidaLibre, vfAbajoV0Cero, if (abs(vfCaidaLibre - vfAbajoV0Cero) < 1e-9) "COINCIDEN EXACTAMENTE" else "DISCREPANCIA"))
    println(String.format(Locale.US, "  - t   (Caída Libre: %.4f s)   vs t   (Lanzamiento v₀=0: %.4f s)   -> %s",
        tCaidaLibre, tAbajoV0Cero, if (abs(tCaidaLibre - tAbajoV0Cero) < 1e-9) "COINCIDEN EXACTAMENTE" else "DISCREPANCIA"))

    // Validación de excepciones y contratos de dominio (try-catch)
    println("\n• Validación de Precondiciones y Manejo de Excepciones (try-catch):")
    try {
        print("  - Probando velocidad inicial negativa (v₀ = -5.0 m/s): ")
        calculadora.calcularVelocidadFinalLanzamientoAbajo(-5.0, hAbajo)
        println("✗ ERROR: Se permitió una velocidad inicial negativa.")
    } catch (e: IllegalArgumentException) {
        println("✓ Excepción capturada exitosamente: \"${e.message}\"")
    }

    try {
        print("  - Probando altura negativa (h = -20.0 m): ")
        calculadora.calcularTiempoLanzamientoAbajo(v0Abajo, -20.0)
        println("✗ ERROR: Se permitió una altura negativa.")
    } catch (e: IllegalArgumentException) {
        println("✓ Excepción capturada exitosamente: \"${e.message}\"")
    }

    // ========================================================================
    // 2. Demostración Issue 13: Lanzamiento Vertical Hacia Arriba (v₀ > 0, contra g)
    // ========================================================================
    println("\n" + "─".repeat(85))
    println("⬆️ MILESTONE 2 (ISSUE 13): LANZAMIENTO VERTICAL HACIA ARRIBA (DESACELERACIÓN)")
    println("   Principio OCP: Incorporación de física en desaceleración sin alterar métodos previos.")
    println("   Invariante de Dominio: Velocidad inicial estrictamente positiva (v₀ > 0).")
    println("─".repeat(85))

    val v0Arriba = 15.0 // m/s

    // Valores teóricos:
    // h_máx   = 15.0^2 / (2 * 9.81) = 225.0 / 19.62 ≈ 11.4679 m
    // t_sub   = 15.0 / 9.81 ≈ 1.5291 s
    // t_total = 2 * 15.0 / 9.81 ≈ 3.0581 s
    val hMaxTeorico = 11.47
    val tSubidaTeorico = 1.53
    val tTotalTeorico = 3.06

    val hMaxCalculada = calculadora.calcularAlturaMaxima(v0Arriba)
    val tSubidaCalculado = calculadora.calcularTiempoSubida(v0Arriba)
    val tTotalCalculado = calculadora.calcularTiempoTotalVuelo(v0Arriba)

    val hMaxValida = calculadora.validarResultado(hMaxCalculada, hMaxTeorico, margenError = 0.05)
    val tSubidaValido = calculadora.validarResultado(tSubidaCalculado, tSubidaTeorico, margenError = 0.05)
    val tTotalValido = calculadora.validarResultado(tTotalCalculado, tTotalTeorico, margenError = 0.05)

    println("Parámetros de prueba: v₀ = $v0Arriba m/s")
    println(String.format(Locale.US, "• Altura máxima calculada   : %.4f m (Teórico: %.2f m) -> %s",
        hMaxCalculada, hMaxTeorico, if (hMaxValida) "APROBADO (±0.05)" else "RECHAZADO"))
    println(String.format(Locale.US, "• Tiempo de subida calculado: %.4f s (Teórico: %.2f s) -> %s",
        tSubidaCalculado, tSubidaTeorico, if (tSubidaValido) "APROBADO (±0.05)" else "RECHAZADO"))
    println(String.format(Locale.US, "• Tiempo total de vuelo     : %.4f s (Teórico: %.2f s) -> %s",
        tTotalCalculado, tTotalTeorico, if (tTotalValido) "APROBADO (±0.05)" else "RECHAZADO"))

    // Comprobación de simetría cinemática
    val esSimetrico = abs(tTotalCalculado - (2.0 * tSubidaCalculado)) < 1e-9
    println(String.format(Locale.US, "• Simetría temporal (t_total == 2 * t_subida): %s (%.4fs == 2 * %.4fs)",
        if (esSimetrico) "VERIFICADA EXITOSAMENTE" else "FALLO EN SIMETRÍA",
        tTotalCalculado, tSubidaCalculado))

    // Validación de excepciones y contratos de dominio ante v₀ <= 0 (try-catch)
    println("\n• Validación de Contratos Seguros e Invariantes de Dominio (v₀ <= 0):")
    try {
        print("  - Probando velocidad inicial nula (v₀ = 0.0 m/s en calcularAlturaMaxima): ")
        calculadora.calcularAlturaMaxima(0.0)
        println("✗ ERROR: Se permitió v₀ = 0 en lanzamiento hacia arriba.")
    } catch (e: IllegalArgumentException) {
        println("✓ Excepción capturada exitosamente: \"${e.message}\"")
    }

    try {
        print("  - Probando velocidad inicial negativa (v₀ = -10.0 m/s en calcularTiempoSubida): ")
        calculadora.calcularTiempoSubida(-10.0)
        println("✗ ERROR: Se permitió v₀ < 0 en cálculo de tiempo de subida.")
    } catch (e: IllegalArgumentException) {
        println("✓ Excepción capturada exitosamente: \"${e.message}\"")
    }

    try {
        print("  - Probando velocidad inicial negativa (v₀ = -15.0 m/s en calcularTiempoTotalVuelo): ")
        calculadora.calcularTiempoTotalVuelo(-15.0)
        println("✗ ERROR: Se permitió v₀ < 0 en tiempo total de vuelo.")
    } catch (e: IllegalArgumentException) {
        println("✓ Excepción capturada exitosamente: \"${e.message}\"")
    }

    println("=".repeat(85))
    println("✨ DEMOSTRACIÓN DE MOTOR FÍSICO COMPLETADA CON ÉXITO")
    println("=".repeat(85) + "\n")

    return calculadora
}

/**
 * Punto de entrada principal de la aplicación.
 *
 * 1. Ejecuta la demostración y validación en consola de los modelos de dominio (POO / SOLID).
 * 2. Ejecuta la demostración y validación en consola de las extensiones del motor físico (Issues 12 y 13).
 * 3. Inicializa la interfaz gráfica JavaFX interactiva (a menos que se especifique modo consola).
 */
fun main(args: Array<String>) {
    // 1. Demostración y validación por consola de los modelos de dominio
    demostrarModelosDominio()

    // 2. Demostración y validación por consola de las extensiones del motor físico (Issues 12 y 13)
    demostrarMotorFisico()

    // 3. Si se solicitó ejecución exclusiva en consola (ej. CI o entornos headless), no iniciar JavaFX
    if (args.contains("--cli") || args.contains("--console") || args.contains("--demo-only")) {
        return
    }

    // 4. Lanzamiento de la interfaz gráfica JavaFX
    try {
        Application.launch(GravityQuestApp::class.java, *args)
    } catch (e: Exception) {
        println("Aviso: Ejecución JavaFX finalizada o entorno sin display gráfico: ${e.message}")
    }
}
