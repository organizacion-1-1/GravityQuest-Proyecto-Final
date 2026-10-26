package gravityquest

import javafx.animation.AnimationTimer
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas
import gravityquest.models.Problema

/**
 * ============================================================================
 * GravityQuest - Pantalla de Juego (VistaJuego)
 * ============================================================================
 *
 * Principio de Responsabilidad Única (SRP):
 * Se encarga exclusivamente de presentar y coordinar la vista del ejercicio físico:
 * ensambla el marcador de puntos ([MarcadorPuntosView]) en su layout superior, muestra el
 * problema cargado, valida la respuesta del usuario, notifica los eventos a [IGestorProgreso]
 * (sin calcular ni almacenar el puntaje por sí misma), invoca el modal [VentanaPista] y
 * delega la simulación a [ControladorSimulacion].
 *
 * Principio de Inversión de Dependencias (DIP) y Polimorfismo (OCP):
 * Depende de contratos abstractos ([INavegadorEscenas], [IRepositorioProblemas], [ICalculadoraFisica],
 * [RenderizadorSimulacion], [IGestorFeedback], [IGestorProgreso], [IEstrategiaCalculo]).
 */
class VistaJuego(
    val dificultad: Dificultad,
    private val navegador: INavegadorEscenas,
    val repositorio: IRepositorioProblemas = GeneradorProblemas(),
    val calculadora: ICalculadoraFisica = CalculadoraFisica(),
    val gestorProgreso: IGestorProgreso = GestorProgreso(),
    val stagePropietario: Stage? = null,
    problemaInicial: Problema? = null
) : BorderPane() {

    /** Nodo raíz de la vista de juego (referencia a sí mismo como contenedor). */
    val root: Parent get() = this

    // 1. Problema físico actualmente activo cargado desde el repositorio o recibido externamente
    var problemaActual: Problema = problemaInicial ?: cargarProblemaParaDificultad(dificultad)
        private set

    // 2. Altura en metros del ejercicio para configurar el lienzo físico
    val alturaProblema: Double
        get() = problemaActual.alturaReferenciaSimulacion

    // 3. Estado físico y renderizado gráfico desacoplados (SRP / DIP)
    val objetoSimulado: ObjetoSimulado = ObjetoSimulado(alturaMaxima = alturaProblema)
    val canvasSimulacion: Canvas = Canvas(GameConfig.ANCHO_CANVAS_DEFAULT, GameConfig.ALTO_CANVAS_DEFAULT).apply {
        styleClass.add("canvas-simulacion")
    }
    var renderizador: RenderizadorSimulacion = RenderizadorCanvas(
        canvas = canvasSimulacion,
        alturaMaximaMetros = alturaProblema,
        direccion = problemaActual.direccionMovimiento,
        velocidadInicial = problemaActual.velocidadInicial
    )

    // 4. Componentes UI de navegación superior y marcador dinámico de puntos (Issue #17)
    val btnVolverMenu: Button = UIFactory.crearBotonSecundario("⬅ Volver al Menú", claseExtra = "btn-volver")

    val marcadorPuntos: MarcadorPuntosView = MarcadorPuntosView(gestorProgreso)

    val lblBadgeDificultad: Label = UIFactory.crearEtiqueta(clasesEstilo = listOf("badge-dificultad"))

    val btnVerPista: Button = UIFactory.crearBotonSecundario("💡 Ver Pista", claseExtra = "btn-pista")

    val btnOtroProblema: Button = UIFactory.crearBotonSecundario("🔄 Otro Ejercicio")

    // 5. Componentes UI del panel de control del problema
    val lblTituloMision: Label = UIFactory.crearEtiqueta(clasesEstilo = listOf("label", "titulo-mision"))

    val lblEnunciado: Label = UIFactory.crearEtiqueta(
        clasesEstilo = listOf("label", "enunciado"),
        anchoMax = 340.0,
        wrap = true
    )

    val lblPista: Label = UIFactory.crearEtiqueta(
        clasesEstilo = listOf("label", "pista-texto"),
        anchoMax = 340.0,
        wrap = true
    )

    val txtRespuesta: TextField = UIFactory.crearCampoTexto(anchoMax = 240.0)

    val btnValidar: Button = UIFactory.crearBotonPrimario("Validar y Simular", anchoMax = 240.0)

    val btnSimular: Button = UIFactory.crearBotonSecundario("Simular Movimiento", anchoMax = 200.0)

    val lblResultado: Label = UIFactory.crearEtiqueta(
        clasesEstilo = listOf("label"),
        anchoMax = 340.0,
        wrap = true,
        alineacion = Pos.CENTER
    )

    // 6. Gestor de retroalimentación visual (animaciones y clases de resultado)
    var gestorFeedback: IGestorFeedback = GestorFeedbackVisual(lblResultado)

    // 7. Controlador de simulación a 60 FPS desacoplado (SRP / DRY)
    private val controladorSimulacion: ControladorSimulacion = ControladorSimulacion(
        objetoSimulado = objetoSimulado,
        calculadora = calculadora,
        alturaMetros = alturaProblema,
        velocidadInicial = problemaActual.velocidadInicial,
        direccion = problemaActual.direccionMovimiento,
        estrategia = problemaActual.estrategiaCalculo,
        alActualizar = { renderizador.renderizar(it) }
    )

    val timerSimulacion: AnimationTimer? get() = controladorSimulacion.timer
    val simulacionEnCurso: Boolean get() = controladorSimulacion.enCurso

    init {
        // Estilos base de la pantalla
        styleClass.add("root")
        padding = Insets(16.0)

        // Ensamblado de layouts
        top = construirBarraNavegacion()
        center = construirContenedorCentral()

        // Inicialización reactiva de datos del problema y renderizado inicial
        actualizarDatosProblemaEnUI()

        // Configuración de eventos de interacción
        configurarEventos()
    }

    /**
     * Construye la barra de herramientas superior ensamblando la navegación, el marcador dinámico
     * de puntos ([MarcadorPuntosView]), el badge de dificultad y las acciones de ayuda.
     */
    private fun construirBarraNavegacion(): HBox =
        HBox(16.0).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("barra-navegacion")
            val espaciador = javafx.scene.layout.Region()
            HBox.setHgrow(espaciador, Priority.ALWAYS)
            children.addAll(
                btnVolverMenu,
                espaciador,
                marcadorPuntos,
                lblBadgeDificultad,
                btnVerPista,
                btnOtroProblema
            )
            BorderPane.setMargin(this, Insets(0.0, 0.0, 16.0, 0.0))
        }

    /**
     * Construye el panel central distribuyendo formulario a la izquierda y simulación a la derecha.
     */
    private fun construirContenedorCentral(): HBox {
        val panelControl = VBox(14.0).apply {
            alignment = Pos.CENTER
            maxWidth = 360.0
            styleClass.add("panel-control")
            children.addAll(lblTituloMision, lblEnunciado, lblPista, txtRespuesta, btnValidar, lblResultado)
        }

        val panelSimulacion = VBox(14.0).apply {
            alignment = Pos.CENTER
            styleClass.add("panel-simulacion")
            children.addAll(canvasSimulacion, btnSimular)
        }

        return HBox(22.0).apply {
            alignment = Pos.CENTER
            children.addAll(panelControl, panelSimulacion)
        }
    }

    /**
     * Conecta las acciones interactivas de los controles de la vista.
     */
    private fun configurarEventos() {
        btnVolverMenu.setOnAction {
            detenerSimulacion()
            navegador.irAMenu()
        }

        btnValidar.setOnAction { validarRespuesta() }
        txtRespuesta.setOnAction { validarRespuesta() }
        btnSimular.setOnAction { iniciarOReiniciarSimulacion() }
        btnOtroProblema.setOnAction { cargarNuevoProblema() }

        // Abre el diálogo modal de pistas y notifica al modelo de progreso (Issue #16 & #17)
        btnVerPista.setOnAction {
            gestorProgreso.registrarPistaUsada(problemaActual.id)
            val owner = stagePropietario ?: scene?.window
            VentanaPista.mostrar(owner, problemaActual)
        }
    }

    /**
     * Carga un problema correspondiente a la dificultad especificada desde el repositorio.
     */
    private fun cargarProblemaParaDificultad(dif: Dificultad): Problema {
        val problemas = repositorio.obtenerPorDificultad(dif)
        return problemas.randomOrNull()
            ?: problemas.firstOrNull()
            ?: GeneradorProblemas.crearCatalogoBase().first { it.dificultad == dif }
    }

    /**
     * Actualiza la interfaz con la información del problema actualmente seleccionado.
     */
    private fun actualizarDatosProblemaEnUI() {
        val difActual = problemaActual.dificultad
        lblBadgeDificultad.text = "Dificultad: ${difActual.nombreVisible} | ${problemaActual.tipo.nombre}"
        lblTituloMision.text = "MISIÓN #${problemaActual.id} ── ${difActual.nombreVisible.uppercase()}"
        lblEnunciado.text = problemaActual.enunciado
        lblPista.text = "💡 Pista: ${problemaActual.pista}"
        txtRespuesta.promptText = "Respuesta en ${problemaActual.unidadMedida} (ej. ${problemaActual.valorEsperado})"
        txtRespuesta.clear()
        gestorFeedback.limpiar()

        btnSimular.text = if (problemaActual.direccionMovimiento == DireccionMovimiento.ARRIBA) {
            "Simular Disparo"
        } else {
            "Simular Caída"
        }

        val alturaEfectiva = problemaActual.alturaReferenciaSimulacion

        objetoSimulado.reiniciar()
        renderizador = RenderizadorCanvas(
            canvas = canvasSimulacion,
            alturaMaximaMetros = alturaEfectiva,
            direccion = problemaActual.direccionMovimiento,
            velocidadInicial = problemaActual.velocidadInicial
        )
        controladorSimulacion.configurarParaProblema(problemaActual)
        renderizador.renderizar(objetoSimulado)
    }

    /**
     * Carga un nuevo problema aleatorio de la misma dificultad y refresca la pantalla.
     */
    fun cargarNuevoProblema() {
        detenerSimulacion()
        val candidatos = repositorio.obtenerPorDificultad(dificultad)
        val otro = candidatos.filter { it.id != problemaActual.id }.randomOrNull() ?: candidatos.firstOrNull()
        if (otro != null) {
            problemaActual = otro
            gestorProgreso.reiniciarEstadoIntentoProblema(problemaActual.id)
            actualizarDatosProblemaEnUI()
        }
    }

    /**
     * Valida la respuesta numérica ingresada por el usuario:
     * - Si es correcta: delega el registro de victoria y cálculo de puntos (+100 o +50) a [IGestorProgreso],
     *   muestra feedback de éxito e inicia la simulación.
     * - Si es incorrecta: delega la penalización (-10 puntos sin bajar de 0) a [IGestorProgreso],
     *   muestra feedback de error y detiene la animación.
     *
     * @return `true` si la respuesta fue admitida dentro del margen de error, `false` en caso contrario.
     */
    fun validarRespuesta(): Boolean {
        val valorIngresado = UIFactory.obtenerValorDecimal(txtRespuesta)

        if (valorIngresado == null) {
            detenerYReiniciarLienzo()
            gestorFeedback.mostrarErrorFormato("¡Entrada inválida! Por favor ingresa un número.")
            return false
        }

        val esCorrecto = problemaActual.validarRespuesta(valorIngresado)

        return if (esCorrecto) {
            // Delega el cálculo y acumulación de puntos al modelo de dominio (SRP)
            gestorProgreso.registrarVictoria(problemaActual)

            gestorFeedback.mostrarExito(
                "¡Correcto! Respuesta dentro de la tolerancia (±${dificultad.margenErrorTolerable} ${problemaActual.unidadMedida})."
            )
            iniciarOReiniciarSimulacion()
            true
        } else {
            // Delega la penalización por respuesta incorrecta al modelo de dominio (SRP)
            gestorProgreso.registrarIntentoFallido(problemaActual.id)

            detenerYReiniciarLienzo()
            gestorFeedback.mostrarErrorPrecision("¡Intenta de nuevo! Tu respuesta no es precisa.")
            false
        }
    }

    /**
     * Inicia o reinicia la simulación del objeto en movimiento en el Canvas según sus condiciones iniciales.
     */
    fun iniciarOReiniciarSimulacion() {
        val alturaEfectiva = problemaActual.alturaReferenciaSimulacion
        controladorSimulacion.iniciarOReiniciar(
            nuevaAltura = alturaEfectiva,
            nuevaVelocidadInicial = problemaActual.velocidadInicial,
            nuevaDireccion = problemaActual.direccionMovimiento,
            nuevaEstrategia = problemaActual.estrategiaCalculo
        )
    }

    /**
     * Detiene el temporizador de la simulación gráfica.
     */
    fun detenerSimulacion() {
        controladorSimulacion.detener()
    }

    private fun detenerYReiniciarLienzo() {
        detenerSimulacion()
        objetoSimulado.reiniciar()
        renderizador.renderizar(objetoSimulado)
    }
}
