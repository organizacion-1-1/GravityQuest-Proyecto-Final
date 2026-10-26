package gravityquest

import javafx.animation.AnimationTimer
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
 * Estructura de Arquitectura y Principios SOLID:
 * 1. Principio de Responsabilidad Única (SRP):
 *    - Cálculos matemáticos y cinemáticos: [CalculadoraFisica] ([ICalculadoraFisica]) en Formulas.kt.
 *    - Ciclo de vida y orquestación general: [GravityQuestApp].
 *    - Bucle de simulación cinemática temporal: [ControladorSimulacion].
 *    - Gestión del progreso del jugador y reglas de negocio: [GestorProgreso] ([IGestorProgreso]).
 *    - Ventana modal de ayuda pedagógica: [VentanaPista].
 *    - Componentes de interfaz desacoplados:
 *        * [VistaFormulario]: Entradas de datos, enunciado, botón de validación y etiqueta de estado.
 *        * [VistaSimulacion]: Lienzo gráfico [Canvas] y controles de simulación.
 *        * [GestorFeedbackVisual]: Animaciones y estilos CSS dinámicos de retroalimentación.
 *        * [RenderizadorCanvas]: Conversión de física a píxeles y renderizado gráfico.
 *
 * 2. Principio Abierto/Cerrado (OCP):
 *    - Ejercicios desacoplados vía [NivelEjercicio].
 *    - Reglas de desbloqueo extensibles vía [IReglaDesbloqueo].
 *
 * 3. Inversión de Dependencias (DIP) y Encapsulamiento:
 *    - Vistas y controladores dependen de interfaces ([ICalculadoraFisica], [RenderizadorSimulacion],
 *      [IGestorFeedback], [NivelEjercicio], [INavegadorEscenas], [IGestorProgreso]).
 *    - Estado físico protegido en [ObjetoSimulado] y estado de progreso en [GestorProgreso].
 */

/**
 * Modelo de estado que encapsula las propiedades físicas del objeto en caída.
 */
class ObjetoSimulado(
    val alturaMaxima: Double = PhysicsConstants.ALTURA_DEFAULT
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

    fun actualizar(posicion: Double, velocidad: Double, tiempoTranscurrido: Double, haLlegadoAlSuelo: Boolean) {
        this.posicionY = posicion.coerceIn(0.0, alturaMaxima)
        this.velocidadY = velocidad
        this.tiempo = tiempoTranscurrido
        this.enSuelo = haLlegadoAlSuelo
    }

    fun reiniciar() {
        posicionY = 0.0
        velocidadY = 0.0
        tiempo = 0.0
        enSuelo = false
    }
}

/**
 * Contrato de abstracción para definir ejercicios o niveles en GravityQuest.
 */
interface NivelEjercicio {
    val idNivel: Int
    val titulo: String
    val enunciado: String
    val alturaInicialMetros: Double
    val unidadRespuesta: String
    val margenError: Double

    fun calcularRespuestaEsperada(calculadora: ICalculadoraFisica): Double

    fun validarRespuesta(calculadora: ICalculadoraFisica, valorIngresado: Double): Boolean {
        val valorEsperado = calcularRespuestaEsperada(calculadora)
        return calculadora.validarResultado(valorIngresado, valorEsperado, margenError)
    }
}

/**
 * Nivel 1: Caída Libre desde 20.0 metros.
 */
class NivelCaidaLibre(
    override val idNivel: Int = 1,
    override val titulo: String = "Nivel 1: Caída Libre",
    override val enunciado: String = "Un objeto cae libremente desde una altura de 20.0 metros (20m). Calcula el tiempo de caída en segundos (g = ${PhysicsConstants.GRAVEDAD_TIERRA} m/s²).",
    override val alturaInicialMetros: Double = PhysicsConstants.ALTURA_DEFAULT,
    override val unidadRespuesta: String = "s",
    override val margenError: Double = PhysicsConstants.MARGEN_ERROR_DEFAULT
) : NivelEjercicio {
    override fun calcularRespuestaEsperada(calculadora: ICalculadoraFisica): Double =
        calculadora.calcularTiempo(alturaInicialMetros)
}

/**
 * Nivel 2: Lanzamiento Vertical Hacia Abajo (v₀ >= 0).
 */
class NivelLanzamientoAbajo(
    override val idNivel: Int = 2,
    override val titulo: String = "Nivel 2: Lanzamiento Hacia Abajo",
    override val enunciado: String = "Un dron lanza un paquete hacia abajo desde 20.0 metros con v₀ = 5.0 m/s. Calcula la velocidad final en m/s (g = ${PhysicsConstants.GRAVEDAD_TIERRA} m/s²).",
    override val alturaInicialMetros: Double = PhysicsConstants.ALTURA_DEFAULT,
    val velocidadInicial: Double = 5.0,
    override val unidadRespuesta: String = "m/s",
    override val margenError: Double = PhysicsConstants.MARGEN_ERROR_DEFAULT
) : NivelEjercicio {
    override fun calcularRespuestaEsperada(calculadora: ICalculadoraFisica): Double =
        calculadora.calcularVelocidadFinalLanzamientoAbajo(velocidadInicial, alturaInicialMetros)
}

/**
 * Nivel 3: Lanzamiento Vertical Hacia Arriba (v₀ > 0).
 */
class NivelLanzamientoArriba(
    override val idNivel: Int = 3,
    override val titulo: String = "Nivel 3: Lanzamiento Hacia Arriba",
    override val enunciado: String = "Un cohete sonda es disparado verticalmente hacia arriba con v₀ = 15.0 m/s. Calcula la altura máxima alcanzada en metros (g = ${PhysicsConstants.GRAVEDAD_TIERRA} m/s²).",
    override val alturaInicialMetros: Double = 0.0,
    val velocidadInicial: Double = 15.0,
    override val unidadRespuesta: String = "m",
    override val margenError: Double = PhysicsConstants.MARGEN_ERROR_DEFAULT
) : NivelEjercicio {
    override fun calcularRespuestaEsperada(calculadora: ICalculadoraFisica): Double =
        calculadora.calcularAlturaMaxima(velocidadInicial)
}

/**
 * Contrato de abstracción para la gestión de retroalimentación de la interfaz de usuario.
 */
interface IGestorFeedback {
    fun mostrarExito(mensaje: String = "¡Correcto! Respuesta dentro del margen de error.")
    fun mostrarErrorPrecision(mensaje: String = "¡Intenta de nuevo! Tu respuesta no es precisa.")
    fun mostrarErrorFormato(mensaje: String = "¡Entrada inválida! Por favor ingresa un número.")
    fun mostrarGameOver(mensaje: String = "💀 ¡Game Over / Nivel Fallido! Reiniciando nivel con 3 vidas...") {
        mostrarErrorPrecision(mensaje)
    }
    fun limpiar()
}

/**
 * Gestor dinámico de retroalimentación visual con animaciones seguras y estilos CSS.
 */
class GestorFeedbackVisual(
    private val labelResultado: Label
) : IGestorFeedback {

    override fun mostrarExito(mensaje: String) {
        actualizarEstilo(esExito = true, mensaje = mensaje)
        FxAnimationUtils.animarPulso(labelResultado) { restablecerTransformaciones() }
    }

    override fun mostrarErrorPrecision(mensaje: String) {
        mostrarFallo(mensaje)
    }

    override fun mostrarErrorFormato(mensaje: String) {
        mostrarFallo(mensaje)
    }

    override fun mostrarGameOver(mensaje: String) {
        mostrarFallo(mensaje)
        if (!labelResultado.styleClass.contains("resultado-gameover")) {
            labelResultado.styleClass.add("resultado-gameover")
        }
    }

    private fun mostrarFallo(mensaje: String) {
        labelResultado.styleClass.remove("resultado-gameover")
        actualizarEstilo(esExito = false, mensaje = mensaje)
        FxAnimationUtils.animarVibracion(labelResultado) { restablecerTransformaciones() }
    }

    private fun actualizarEstilo(esExito: Boolean, mensaje: String) {
        labelResultado.text = mensaje
        val agregar = if (esExito) "resultado-exito" else "resultado-error"
        val remover = if (esExito) "resultado-error" else "resultado-exito"
        labelResultado.styleClass.remove(remover)
        if (!labelResultado.styleClass.contains(agregar)) {
            labelResultado.styleClass.add(agregar)
        }
        restablecerTransformaciones()
    }

    override fun limpiar() {
        labelResultado.text = ""
        labelResultado.styleClass.removeAll("resultado-exito", "resultado-error", "resultado-gameover")
        restablecerTransformaciones()
    }

    private fun restablecerTransformaciones() {
        labelResultado.scaleX = 1.0
        labelResultado.scaleY = 1.0
        labelResultado.translateX = 0.0
    }
}

/**
 * Componente desacoplado de la vista del formulario de entrada y estado.
 */
class VistaFormulario(
    val lblTitulo: Label = UIFactory.crearTitulo(),
    val lblInstruccion: Label = UIFactory.crearEtiqueta(
        clasesEstilo = listOf("label", "enunciado"),
        anchoMax = 320.0,
        wrap = true
    ),
    val txtRespuesta: TextField = UIFactory.crearCampoTexto(
        prompt = "Ingresa tu respuesta (ej. 2.02)",
        anchoMax = 220.0
    ),
    val btnValidar: Button = UIFactory.crearBotonPrimario("Validar y Simular", anchoMax = 220.0),
    val lblResultado: Label = UIFactory.crearEtiqueta(
        clasesEstilo = listOf("label"),
        anchoMax = 320.0,
        wrap = true
    )
) {
    val root: VBox = VBox(16.0).apply {
        alignment = Pos.CENTER
        maxWidth = 340.0
        styleClass.add("panel-control")
        children.addAll(lblTitulo, lblInstruccion, txtRespuesta, btnValidar, lblResultado)
    }

    fun configurarEnunciado(texto: String) {
        lblInstruccion.text = texto
    }
}

/**
 * Componente desacoplado de la vista del área de simulación física.
 */
class VistaSimulacion(
    val canvasSimulacion: Canvas = Canvas(300.0, 300.0),
    val btnSimular: Button = UIFactory.crearBotonSecundario("Simular Caída", anchoMax = 200.0)
) {
    val root: VBox = VBox(14.0).apply {
        alignment = Pos.CENTER
        styleClass.add("panel-simulacion")
        children.addAll(
            canvasSimulacion.apply { styleClass.add("canvas-simulacion") },
            btnSimular
        )
    }
}

/**
 * Orquestador principal y ciclo de vida de JavaFX para GravityQuest.
 */
open class GravityQuestApp : Application() {

    // 1. Delegación física mediante CalculadoraFisica (SRP / DIP)
    val calculadora: CalculadoraFisica = CalculadoraFisica()

    // 2. Modelo de ejercicio extensible (OCP)
    var nivelActual: NivelEjercicio = NivelCaidaLibre()

    // Repositorio de problemas de física
    val repositorioProblemas: GeneradorProblemas = GeneradorProblemas()

    // 3. Gestor de progreso del jugador desacoplado (SRP / DIP)
    val gestorProgreso: IGestorProgreso = GestorProgreso(repositorioProblemas)

    // Altura del problema actual en metros
    val alturaProblema: Double
        get() = nivelActual.alturaInicialMetros

    // 4. Estado físico simulado encapsulado (POO)
    val objetoSimulado: ObjetoSimulado = ObjetoSimulado(alturaMaxima = alturaProblema)

    // 5. Vistas desacopladas (SRP)
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

    // 6. Controlador de simulación cinemática desacoplado (DRY / SRP)
    private val controladorSimulacion: ControladorSimulacion

    val timerSimulacion: AnimationTimer? get() = controladorSimulacion.timer
    var tiempoInicioNano: Long = 0L
    val simulacionEnCurso: Boolean get() = controladorSimulacion.enCurso

    init {
        vistaFormulario.configurarEnunciado(nivelActual.enunciado)
        renderizador = RenderizadorCanvas(canvasSimulacion, alturaProblema)
        gestorFeedback = GestorFeedbackVisual(lblResultado)
        controladorSimulacion = ControladorSimulacion(
            objetoSimulado = objetoSimulado,
            calculadora = calculadora,
            alturaMetros = alturaProblema,
            alActualizar = { renderizador.renderizar(it) }
        )
        renderizador.renderizar(objetoSimulado)
    }

    fun iniciarOReiniciarSimulacion() {
        controladorSimulacion.iniciarOReiniciar(alturaProblema)
    }

    fun detenerSimulacion() {
        controladorSimulacion.detener()
    }

    fun validarRespuesta(): Boolean {
        val valorIngresado = UIFactory.obtenerValorDecimal(txtRespuesta)

        if (valorIngresado == null) {
            detenerSimulacion()
            objetoSimulado.reiniciar()
            renderizador.renderizar(objetoSimulado)
            gestorFeedback.mostrarErrorFormato("¡Entrada inválida! Por favor ingresa un número.")
            return false
        }

        val esCorrecto = nivelActual.validarRespuesta(calculadora, valorIngresado)

        return if (esCorrecto) {
            gestorProgreso.registrarProblemaCompletado(nivelActual.idNivel)
            gestorFeedback.mostrarExito("¡Correcto! Respuesta dentro del margen de error.")
            iniciarOReiniciarSimulacion()
            true
        } else {
            detenerSimulacion()
            objetoSimulado.reiniciar()
            renderizador.renderizar(objetoSimulado)
            gestorFeedback.mostrarErrorPrecision("¡Intenta de nuevo! Tu respuesta no es precisa.")
            false
        }
    }

    // 7. Gestor de navegación de escenas centralizado (SRP / DIP)
    var navegador: NavegadorEscenas? = null
        private set

    override fun start(primaryStage: Stage) {
        val nav = NavegadorEscenas(primaryStage, repositorioProblemas, calculadora, gestorProgreso)
        this.navegador = nav
        nav.irAMenu()
    }

    fun iniciarEscenaMotorBasico(primaryStage: Stage) {
        btnValidar.setOnAction { validarRespuesta() }
        txtRespuesta.setOnAction { validarRespuesta() }
        btnSimular.setOnAction { iniciarOReiniciarSimulacion() }

        renderizador.renderizar(objetoSimulado)

        val contenedorPrincipal = HBox(25.0).apply {
            alignment = Pos.CENTER
            padding = Insets(20.0)
            styleClass.add("root")
            children.addAll(vistaFormulario.root, vistaSimulacion.root)
        }

        val scene = Scene(contenedorPrincipal, 740.0, 460.0)
        scene.stylesheets.add(javaClass.getResource(GameConfig.RUTA_ESTILOS)?.toExternalForm())

        primaryStage.title = "${GameConfig.TITULO_APP} - Motor Básico"
        primaryStage.scene = scene
        primaryStage.show()
    }

    override fun stop() {
        detenerSimulacion()
        super.stop()
    }
}

/**
 * Objeto utilitario que encapsula las demostraciones por consola, validaciones de
 * contratos de dominio y pruebas de integración visual/diagnóstica (SRP / POO).
 */
object DemostracionConsola {

    inline fun probarInvariante(descripcion: String, bloque: () -> Unit) {
    try {
        bloque()
        println("✗ Fallo: Se permitió una condición inválida ($descripcion).")
    } catch (e: IllegalArgumentException) {
        println("✓ Invariante activo: $descripcion.")
        println("  Excepción capturada: \"${e.message}\"")
    }
}

/**
 * Realiza una demostración y validación en consola de los modelos de dominio (POO y Principios SOLID).
 */
fun demostrarModelosDominio(repositorio: IRepositorioProblemas = GeneradorProblemas()): IRepositorioProblemas {
    println("=".repeat(85))
    println("🌌 GRAVITYQUEST - DEMOSTRACIÓN DE MODELOS DE DOMINIO Y PRINCIPIOS SOLID")
    println("=".repeat(85))
    println("Catálogo de problemas inicializado correctamente.")
    println("Total de problemas registrados: ${repositorio.cantidadTotal()}\n")

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
        datosAdicionales = mapOf("lugar" to "Luna", "gravedad" to 1.62, "alturaMetros" to 10.0)
    )
    repositorio.agregarProblema(nuevoProblemaLunar)
    val problemaRecuperado = repositorio.obtenerPorId(99)
    println("✓ Nuevo problema registrado dinámicamente: ID=${problemaRecuperado?.id}, Tipo=${problemaRecuperado?.tipo?.nombre}")
    println("✓ Total de problemas actualizados en el repositorio: ${repositorio.cantidadTotal()}")

    println("\n" + "=".repeat(85))
    println("🛡️ DEMOSTRACIÓN DE CONTRATOS SEGUROS E INVARIANTES DE DOMINIO")
    println("=".repeat(85))

    probarInvariante("Se impidió crear un problema con enunciado vacío") {
        Problema(id = 100, enunciado = "", dificultad = Dificultad.FACIL, valorEsperado = 10.0, unidadMedida = "s", pista = "Pista")
    }

    probarInvariante("Se impidió duplicar un problema con ID ya existente") {
        repositorio.agregarProblema(nuevoProblemaLunar)
    }

    println("=".repeat(85))
    println("✨ DEMOSTRACIÓN DE MODELOS DE DOMINIO COMPLETADA CON ÉXITO")
    println("=".repeat(85) + "\n")

    return repositorio
}

/**
 * Realiza una demostración y validación por consola de las extensiones del motor físico.
 */
fun demostrarMotorFisico(calculadora: CalculadoraFisica = CalculadoraFisica()): CalculadoraFisica {
    println("=".repeat(85))
    println("🔬 GRAVITYQUEST - DEMOSTRACIÓN DEL MOTOR FÍSICO (POO Y PRINCIPIOS SOLID)")
    println("=".repeat(85))
    println("Aceleración de gravedad terrestre estándar: g = ${calculadora.g} m/s²\n")

    // Lanzamiento hacia abajo
    println("─".repeat(85))
    println("⬇️ MILESTONE 2: LANZAMIENTO VERTICAL HACIA ABAJO")
    println("─".repeat(85))

    val v0Abajo = 5.0
    val hAbajo = 20.0
    val vfTeorico = 20.43
    val tTeorico = 1.57

    val vfCalculado = calculadora.calcularVelocidadFinalLanzamientoAbajo(v0Abajo, hAbajo)
    val tCalculado = calculadora.calcularTiempoLanzamientoAbajo(v0Abajo, hAbajo)

    val vfValido = calculadora.validarResultado(vfCalculado, vfTeorico, margenError = 0.05)
    val tValido = calculadora.validarResultado(tCalculado, tTeorico, margenError = 0.05)

    println("Parámetros: v₀ = $v0Abajo m/s, h = $hAbajo m")
    println(String.format(Locale.US, "• Velocidad final calculada : %.4f m/s (Teórico: %.2f m/s) -> %s",
        vfCalculado, vfTeorico, if (vfValido) "APROBADO (±0.05)" else "RECHAZADO"))
    println(String.format(Locale.US, "• Tiempo de caída calculado  : %.4f s   (Teórico: %.2f s)   -> %s",
        tCalculado, tTeorico, if (tValido) "APROBADO (±0.05)" else "RECHAZADO"))

    val vfCaidaLibre = calculadora.calcularVelocidadFinal(hAbajo)
    val vfAbajoV0Cero = calculadora.calcularVelocidadFinalLanzamientoAbajo(0.0, hAbajo)
    val tCaidaLibre = calculadora.calcularTiempo(hAbajo)
    val tAbajoV0Cero = calculadora.calcularTiempoLanzamientoAbajo(0.0, hAbajo)
    println("\n• Verificación OCP (Consistencia con Caída Libre si v₀ = 0 m/s):")
    println(String.format(Locale.US, "  - v_f (Caída: %.4f) vs v_f (v₀=0: %.4f) -> %s",
        vfCaidaLibre, vfAbajoV0Cero, if (abs(vfCaidaLibre - vfAbajoV0Cero) < 1e-9) "COINCIDEN EXACTAMENTE" else "DISCREPANCIA"))
    println(String.format(Locale.US, "  - t   (Caída: %.4f) vs t   (v₀=0: %.4f) -> %s",
        tCaidaLibre, tAbajoV0Cero, if (abs(tCaidaLibre - tAbajoV0Cero) < 1e-9) "COINCIDEN EXACTAMENTE" else "DISCREPANCIA"))

    println("\n• Validación de Precondiciones e Invariantes:")
    probarInvariante("Velocidad inicial negativa rechazada") {
        calculadora.calcularVelocidadFinalLanzamientoAbajo(-5.0, hAbajo)
    }
    probarInvariante("Altura negativa rechazada") {
        calculadora.calcularTiempoLanzamientoAbajo(v0Abajo, -20.0)
    }

    // Lanzamiento hacia arriba
    println("\n" + "─".repeat(85))
    println("⬆️ MILESTONE 2: LANZAMIENTO VERTICAL HACIA ARRIBA (DESACELERACIÓN)")
    println("─".repeat(85))

    val v0Arriba = 15.0
    val hMaxTeorico = 11.47
    val tSubidaTeorico = 1.53
    val tTotalTeorico = 3.06

    val hMaxCalculada = calculadora.calcularAlturaMaxima(v0Arriba)
    val tSubidaCalculado = calculadora.calcularTiempoSubida(v0Arriba)
    val tTotalCalculado = calculadora.calcularTiempoTotalVuelo(v0Arriba)

    val hMaxValida = calculadora.validarResultado(hMaxCalculada, hMaxTeorico, margenError = 0.05)
    val tSubidaValido = calculadora.validarResultado(tSubidaCalculado, tSubidaTeorico, margenError = 0.05)
    val tTotalValido = calculadora.validarResultado(tTotalCalculado, tTotalTeorico, margenError = 0.05)

    println("Parámetros: v₀ = $v0Arriba m/s")
    println(String.format(Locale.US, "• Altura máxima calculada   : %.4f m (Teórico: %.2f m) -> %s",
        hMaxCalculada, hMaxTeorico, if (hMaxValida) "APROBADO (±0.05)" else "RECHAZADO"))
    println(String.format(Locale.US, "• Tiempo de subida calculado: %.4f s (Teórico: %.2f s) -> %s",
        tSubidaCalculado, tSubidaTeorico, if (tSubidaValido) "APROBADO (±0.05)" else "RECHAZADO"))
    println(String.format(Locale.US, "• Tiempo total de vuelo     : %.4f s (Teórico: %.2f s) -> %s",
        tTotalCalculado, tTotalTeorico, if (tTotalValido) "APROBADO (±0.05)" else "RECHAZADO"))

    val esSimetrico = abs(tTotalCalculado - (2.0 * tSubidaCalculado)) < 1e-9
    println("• Simetría temporal (t_total == 2 * t_subida): " + if (esSimetrico) "VERIFICADA EXITOSAMENTE" else "FALLO")

    println("\n• Validación de Contratos Seguros (v₀ <= 0):")
    probarInvariante("Velocidad inicial nula rechazada en tiro vertical") {
        calculadora.calcularAlturaMaxima(0.0)
    }
    probarInvariante("Velocidad inicial negativa rechazada en tiempo de subida") {
        calculadora.calcularTiempoSubida(-10.0)
    }
    probarInvariante("Velocidad inicial negativa rechazada en tiempo total") {
        calculadora.calcularTiempoTotalVuelo(-15.0)
    }

    println("=".repeat(85))
    println("✨ DEMOSTRACIÓN DE MOTOR FÍSICO COMPLETADA CON ÉXITO")
    println("=".repeat(85) + "\n")

    return calculadora
}

/**
 * Realiza una demostración y validación en consola de la arquitectura de navegación y pantallas.
 */
fun demostrarNavegacion() {
    println("=".repeat(85))
    println("🧭 GRAVITYQUEST - DEMOSTRACIÓN DE NAVEGACIÓN Y PANTALLAS (POO & SOLID)")
    println("=".repeat(85))
    println("✓ SRP: Stage encapsulado en NavegadorEscenas, aislado de las vistas.")
    println("✓ DIP: Vistas desacopladas mediante interfaces INavegadorEscenas e IRepositorioProblemas.")
    println("✓ OCP: Soporte para nuevas pantallas sin modificar VistaMenu ni VistaJuego.")
    println("✓ VistaMenu: Título neón estilizado, selector de dificultad y botón Iniciar Misión.")
    println("✓ VistaJuego: Problema cargado por Dificultad, botón Volver al Menú y simulación interactiva.")
    println("=".repeat(85) + "\n")
}

/**
 * Realiza una demostración en consola del sistema de progreso del jugador y desbloqueo secuencial (POO & SOLID).
 */
fun demostrarProgresoJugador(gestor: IGestorProgreso = GestorProgreso()): IGestorProgreso {
    println("=".repeat(85))
    println("🏆 GRAVITYQUEST - DEMOSTRACIÓN DE SISTEMA DE PROGRESO DEL JUGADOR (POO & SOLID)")
    println("=".repeat(85))

    println("1. Estado Inicial de Desbloqueo:")
    println("   • Nivel Fácil   : ${if (gestor.esNivelDesbloqueado(Dificultad.FACIL)) "DESBLOQUEADO (Inicial)" else "BLOQUEADO"}")
    println("   • Nivel Medio   : ${if (gestor.esNivelDesbloqueado(Dificultad.MEDIO)) "DESBLOQUEADO" else "BLOQUEADO (Requiere completar Fácil)"}")
    println("   • Nivel Difícil : ${if (gestor.esNivelDesbloqueado(Dificultad.DIFICIL)) "DESBLOQUEADO" else "BLOQUEADO (Requiere completar Medio)"}")

    println("\n2. Simulando resolución de ejercicios de Nivel Fácil...")
    val problemasFacil = listOf(1, 2, 3, 8)
    for (id in problemasFacil) {
        gestor.registrarProblemaCompletado(id)
        println("   ✓ Problema #$id completado. Progreso actual: ${gestor.cantidadProblemasCompletados()} problemas resueltos.")
    }

    println("\n3. Verificación tras completar Nivel Fácil:")
    val medioDesbloqueado = gestor.esNivelDesbloqueado(Dificultad.MEDIO)
    println("   • Nivel Medio desbloqueado: ${if (medioDesbloqueado) "SÍ (Desbloqueado exitosamente)" else "NO"}")

    println("\n4. Simulando resolución de ejercicios de Nivel Medio...")
    val problemasMedio = listOf(4, 5)
    for (id in problemasMedio) {
        gestor.registrarProblemaCompletado(id)
        println("   ✓ Problema #$id completado. Progreso actual: ${gestor.cantidadProblemasCompletados()} problemas resueltos.")
    }

    println("\n5. Verificación tras completar Nivel Medio:")
    val dificilDesbloqueado = gestor.esNivelDesbloqueado(Dificultad.DIFICIL)
    println("   • Nivel Difícil desbloqueado: ${if (dificilDesbloqueado) "SÍ (Desbloqueado exitosamente)" else "NO"}")

    println("=".repeat(85))
    println("✨ DEMOSTRACIÓN DE PROGRESO DEL JUGADOR COMPLETADA CON ÉXITO")
    println("=".repeat(85) + "\n")

    return gestor
}
}

/**
 * Clase principal de la aplicación GravityQuest (POO & JavaFX).
 * Punto de entrada oficial con [main] encapsulado en su [companion object] anotado con @JvmStatic.
 */
open class MainApp : GravityQuestApp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            DemostracionConsola.demostrarModelosDominio()
            DemostracionConsola.demostrarMotorFisico()
            DemostracionConsola.demostrarNavegacion()
            DemostracionConsola.demostrarProgresoJugador()

            if (args.contains("--cli") || args.contains("--console") || args.contains("--demo-only")) {
                return
            }

            try {
                Application.launch(GravityQuestApp::class.java, *args)
            } catch (e: Exception) {
                println("Aviso: Ejecución JavaFX finalizada o entorno sin display gráfico: ${e.message}")
            }
        }

        fun demostrarModelosDominio(repositorio: IRepositorioProblemas = GeneradorProblemas()): IRepositorioProblemas =
            DemostracionConsola.demostrarModelosDominio(repositorio)

        fun demostrarMotorFisico(calculadora: CalculadoraFisica = CalculadoraFisica()): CalculadoraFisica =
            DemostracionConsola.demostrarMotorFisico(calculadora)

        fun demostrarNavegacion() =
            DemostracionConsola.demostrarNavegacion()

        fun demostrarProgresoJugador(gestor: IGestorProgreso = GestorProgreso()): IGestorProgreso =
            DemostracionConsola.demostrarProgresoJugador(gestor)
    }
}

/**
 * Subclase de compatibilidad directa con puntos de entrada y pruebas preexistentes.
 */
class Main : MainApp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MainApp.main(args)
        }
    }
}

/**
 * Objeto de compatibilidad para puntos de entrada referenciados como MainKt.
 */
object MainKt {
    @JvmStatic
    fun main(args: Array<String>) {
        MainApp.main(args)
    }
}
