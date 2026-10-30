package gravityquest

import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas
import gravityquest.models.Problema
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

/**
 * ============================================================================
 * GravityQuest - Modal de Transición de Nivel y Progresión en Cadena (SRP)
 * ============================================================================
 *
 * Presenta una pantalla/modal intermedia al superar un nivel (ej. Fácil → Medio
 * o Medio → Difícil) permitiendo avanzar directamente al siguiente nivel recién
 * desbloqueado o regresar al menú principal.
 */
object VentanaTransicionNivel {

    /**
     * Crea y despliega el modal de transición tras completar un nivel exitosamente.
     */
    fun mostrar(
        propietario: Window?,
        dificultadSuperada: Dificultad,
        siguienteNivel: Dificultad?,
        puntajeActual: Int,
        alAvanzarNivel: (Dificultad) -> Unit,
        alVolverMenu: () -> Unit
    ): Stage {
        val modalStage = Stage().apply {
            title = "¡Nivel ${dificultadSuperada.nombreVisible} Completado!"
            initModality(Modality.APPLICATION_MODAL)
            propietario?.let { initOwner(it) }
        }

        val lblTitulo = Label("🎉 ¡Nivel ${dificultadSuperada.nombreVisible} Superado!").apply {
            styleClass.addAll("titulo", "modal-pista-titulo")
        }

        val textoProgreso = if (siguienteNivel != null) {
            "🔓 ¡Has desbloqueado el Nivel ${siguienteNivel.nombreVisible}!\n" +
                "Puntaje acumulado: ${GestorProgreso.formatearPuntaje(puntajeActual)}"
        } else {
            "🏆 ¡Has completado todos los niveles de la cadena (Fácil → Medio → Difícil)!\n" +
                "Puntaje final: ${GestorProgreso.formatearPuntaje(puntajeActual)}"
        }

        val lblMensaje = Label(textoProgreso).apply {
            styleClass.addAll("label", "enunciado")
            isWrapText = true
            maxWidth = 380.0
            alignment = Pos.CENTER
        }

        val panelBotones = HBox(12.0).apply {
            alignment = Pos.CENTER
        }

        if (siguienteNivel != null) {
            val btnAvanzar = UIFactory.crearBotonPrimario("🚀 Avanzar a ${siguienteNivel.nombreVisible}").apply {
                setOnAction {
                    modalStage.close()
                    alAvanzarNivel(siguienteNivel)
                }
            }
            panelBotones.children.add(btnAvanzar)
        }

        val btnMenu = UIFactory.crearBotonSecundario("⬅ Volver al Menú").apply {
            setOnAction {
                modalStage.close()
                alVolverMenu()
            }
        }
        panelBotones.children.add(btnMenu)

        val contenedor = VBox(18.0).apply {
            alignment = Pos.CENTER
            padding = Insets(24.0)
            styleClass.addAll("modal-pista", "modal-transicion")
            children.addAll(lblTitulo, lblMensaje, panelBotones)
        }

        val scene = Scene(contenedor, 450.0, 260.0).apply {
            val css = VistaJuego::class.java.getResource(GameConfig.RUTA_ESTILOS)?.toExternalForm()
            css?.let { stylesheets.add(it) }
        }

        modalStage.scene = scene
        modalStage.isResizable = false

        if (Platform.isFxApplicationThread()) {
            try {
                modalStage.show()
            } catch (_: Exception) {
                // Tolerancia a entornos headless de pruebas automatizadas
            }
        }

        return modalStage
    }
}

/**
 * ============================================================================
 * GravityQuest - Pantalla de Juego y Controlador de Interacción (VistaJuego)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - Coordina la interfaz del ejercicio físico: ensambla el panel de vidas
 *      ([PanelVidasView]) y el marcador de puntos ([MarcadorPuntosView]) en la
 *      barra superior, aloja el nodo [Canvas] delegando el pintado cuadro a cuadro
 *      a [RenderizadorSimulacion] ([SimulacionCanvasView]) y delega las decisiones
 *      de negocio (vidas, Game Over, puntos y desbloqueo en cadena) a [IGestorProgreso].
 *
 * 2. Open/Closed Principle (OCP) y Polimorfismo:
 *    - Configura automáticamente el escenario gráfico polimórfico según la [Dificultad]
 *      y resuelve la progresión secuencial en cadena (`FÁCIL` → `MEDIO` → `DIFÍCIL`).
 *
 * 3. Dependency Inversion Principle (DIP):
 *    - Depende de abstracciones ([INavegadorEscenas], [IRepositorioProblemas],
 *      [ICalculadoraFisica], [RenderizadorSimulacion], [IGestorFeedback], [IGestorProgreso]).
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

    // 3. Estado físico y renderizado gráfico desacoplados en SimulacionCanvasView (SRP / OCP / DIP)
    val objetoSimulado: ObjetoSimulado = ObjetoSimulado(alturaMaxima = alturaProblema)
    val canvasSimulacion: Canvas = Canvas(GameConfig.ANCHO_CANVAS_DEFAULT, GameConfig.ALTO_CANVAS_DEFAULT).apply {
        styleClass.add("canvas-simulacion")
    }
    var renderizador: RenderizadorSimulacion = SimulacionCanvasView(
        canvas = canvasSimulacion,
        dificultad = dificultad,
        alturaMaximaMetros = alturaProblema,
        direccion = problemaActual.direccionMovimiento,
        velocidadInicial = problemaActual.velocidadInicial
    )

    // 4. Componentes UI de navegación superior, sistema de 3 vidas y marcador de puntos (Issues #17 & #18)
    val btnVolverMenu: Button = UIFactory.crearBotonSecundario("⬅ Volver al Menú", claseExtra = "btn-volver")

    val panelVidas: PanelVidasView = PanelVidasView(gestorProgreso)

    val marcadorPuntos: MarcadorPuntosView = MarcadorPuntosView(gestorProgreso)

    val lblBadgeDificultad: Label = UIFactory.crearEtiqueta(clasesEstilo = listOf("badge-dificultad"))

    val btnVerPista: Button = UIFactory.crearBotonSecundario("💡 Ver Pista", claseExtra = "btn-pista")

    val btnOtroProblema: Button = UIFactory.crearBotonSecundario("🔄 Otro Ejercicio")

    // 5. Componentes UI del panel de control del problema y transición de nivel
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

    val btnAvanzarNivel: Button = UIFactory.crearBotonPrimario(
        texto = obtenerTextoBotonSiguienteNivel(),
        anchoMax = 240.0
    ).apply {
        isVisible = false
        isManaged = false
    }

    val btnSimular: Button = UIFactory.crearBotonSecundario("Simular Movimiento", anchoMax = 200.0)

    val lblResultado: Label = UIFactory.crearEtiqueta(
        clasesEstilo = listOf("label"),
        anchoMax = 340.0,
        wrap = true,
        alineacion = Pos.CENTER
    )

    // 6. Gestor de retroalimentación visual (animaciones y clases de resultado)
    var gestorFeedback: IGestorFeedback = GestorFeedbackVisual(lblResultado)

    // 7. Seguimiento de reinicios por Game Over, tiempo de partida y estadísticas finales (Issues #18 & #20)
    var reiniciosPorGameOver: Int = 0
        private set

    var ultimoModalTransicion: Stage? = null
        private set

    var ultimasEstadisticasPartida: EstadisticasPartida? = null
        private set

    private var marcaTiempoInicioMs: Long = System.currentTimeMillis()

    // 8. Controlador de simulación a 60 FPS desacoplado (SRP / DRY)
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

        // Cada intento de nivel inicia con 3 vidas restablecidas (Issue #18)
        gestorProgreso.cambiarNivelActivo(dificultad)
        gestorProgreso.reiniciarVidas()

        // Ensamblado de layouts
        top = construirBarraNavegacion()
        center = construirContenedorCentral()

        // Inicialización reactiva de datos del problema y renderizado inicial
        actualizarDatosProblemaEnUI()

        // Configuración de eventos de interacción
        configurarEventos()
    }

    /**
     * Construye la barra de herramientas superior ensamblando la navegación, el panel de 3 vidas
     * ([PanelVidasView]), el marcador dinámico de puntos ([MarcadorPuntosView]), el badge de
     * dificultad y las acciones de ayuda.
     */
    private fun construirBarraNavegacion(): HBox =
        HBox(14.0).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("barra-navegacion")
            val espaciador = Region()
            HBox.setHgrow(espaciador, Priority.ALWAYS)
            children.addAll(
                btnVolverMenu,
                espaciador,
                panelVidas,
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
            children.addAll(
                lblTituloMision,
                lblEnunciado,
                lblPista,
                txtRespuesta,
                btnValidar,
                lblResultado,
                btnAvanzarNivel
            )
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
        btnAvanzarNivel.setOnAction { avanzarSiguienteNivel() }

        // Abre el diálogo modal de pistas y notifica al modelo de progreso (Issue #16 & #17)
        btnVerPista.setOnAction {
            gestorProgreso.registrarPistaUsada(problemaActual.id)
            val owner = stagePropietario ?: scene?.window
            VentanaPista.mostrar(owner, problemaActual)
        }
    }

    private fun obtenerTextoBotonSiguienteNivel(): String {
        val siguiente = dificultad.siguienteNivel
        return if (siguiente != null) {
            "🚀 Avanzar a Nivel ${siguiente.nombreVisible}"
        } else {
            "🏆 Volver al Menú Principal"
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
        txtRespuesta.promptText = "Respuesta en ${problemaActual.unidadMedida}"
        txtRespuesta.clear()
        gestorFeedback.limpiar()

        btnAvanzarNivel.text = obtenerTextoBotonSiguienteNivel()
        btnAvanzarNivel.isVisible = false
        btnAvanzarNivel.isManaged = false

        btnSimular.text = if (problemaActual.direccionMovimiento == DireccionMovimiento.ARRIBA) {
            "Simular Disparo"
        } else {
            "Simular Caída"
        }

        val alturaEfectiva = problemaActual.alturaReferenciaSimulacion

        objetoSimulado.reiniciar()
        marcaTiempoInicioMs = System.currentTimeMillis()
        renderizador = SimulacionCanvasView(
            canvas = canvasSimulacion,
            dificultad = difActual,
            alturaMaximaMetros = alturaEfectiva,
            direccion = problemaActual.direccionMovimiento,
            velocidadInicial = problemaActual.velocidadInicial
        )
        controladorSimulacion.configurarParaProblema(problemaActual)
        renderizador.renderizar(objetoSimulado)
    }

    /**
     * Calcula el tiempo transcurrido en segundos desde el inicio del intento actual (Issue #20).
     */
    fun calcularTiempoTranscurridoSegundos(): Double {
        val deltaMs = (System.currentTimeMillis() - marcaTiempoInicioMs).coerceAtLeast(0L)
        return deltaMs / 1000.0
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
            gestorProgreso.reiniciarVidas()
            actualizarDatosProblemaEnUI()
        }
    }

    /**
     * Reinicia automáticamente el intento del nivel actual restableciendo las 3 vidas,
     * limpiando la simulación y el estado del intento tras un Game Over (Issue #18).
     */
    fun reiniciarNivelActual(preservarMensajeGameOver: Boolean = false) {
        detenerSimulacion()
        gestorProgreso.reiniciarEstadoIntentoProblema(problemaActual.id)
        gestorProgreso.reiniciarVidas()
        marcaTiempoInicioMs = System.currentTimeMillis()
        txtRespuesta.clear()
        objetoSimulado.reiniciar()
        renderizador.reiniciarEfectos()
        renderizador.renderizar(objetoSimulado)

        if (!preservarMensajeGameOver) {
            gestorFeedback.limpiar()
        }
    }

    /**
     * Avanza directamente al siguiente nivel desbloqueado en la cadena (Fácil → Medio → Difícil)
     * o regresa al menú si el jugador completó el nivel Difícil.
     *
     * @return `true` si navegó hacia un siguiente nivel, `false` si regresó al menú principal.
     */
    fun avanzarSiguienteNivel(): Boolean {
        detenerSimulacion()
        val siguiente = gestorProgreso.obtenerSiguienteNivel(dificultad)
        return if (siguiente != null && gestorProgreso.esNivelDesbloqueado(siguiente)) {
            navegador.irAJuego(siguiente)
            true
        } else {
            navegador.irAMenu()
            false
        }
    }

    /**
     * Despliega el modal intermedio de transición cuando el jugador supera el nivel actual.
     */
    fun mostrarModalNivelCompletado(): Stage {
        val owner = stagePropietario ?: scene?.window
        val siguiente = gestorProgreso.obtenerSiguienteNivel(dificultad)
        val modal = VentanaTransicionNivel.mostrar(
            propietario = owner,
            dificultadSuperada = dificultad,
            siguienteNivel = siguiente,
            puntajeActual = gestorProgreso.puntajeActual,
            alAvanzarNivel = { nivelDestino ->
                detenerSimulacion()
                navegador.irAJuego(nivelDestino)
            },
            alVolverMenu = {
                detenerSimulacion()
                navegador.irAMenu()
            }
        )
        ultimoModalTransicion = modal
        return modal
    }

    /**
     * Valida la respuesta numérica ingresada por el usuario:
     * - Si es correcta:
     *   1. Registra la victoria y el tiempo en [IGestorProgreso] (desbloqueando automáticamente
     *      el siguiente nivel en la secuencia Fácil → Medio → Difícil).
     *   2. Muestra feedback de éxito, inicia la simulación y notifica el fin del nivel a
     *      [INavegadorEscenas.irANivelCompletado] delegando la construcción de la vista final (SRP / DIP).
     * - Si es incorrecta:
     *   1. Penaliza el puntaje (-10 puntos sin bajar de 0) y descuenta 1 vida en [IGestorProgreso].
     *   2. Si las vidas llegan a 0: registra las estadísticas del intento fallido, reinicia el
     *      estado de las 3 vidas y notifica a [INavegadorEscenas.irAGameOver] para desplegar
     *      la pantalla dedicada de Game Over (SRP / DIP).
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
            // 1. Registra tiempo, logro, puntaje y desbloquea en cadena el siguiente nivel (SRP / OCP)
            val tiempoUsado = calcularTiempoTranscurridoSegundos()
            gestorProgreso.registrarTiempoNivel(tiempoUsado)
            gestorProgreso.registrarVictoria(problemaActual)

            val siguiente = gestorProgreso.obtenerSiguienteNivel(dificultad)
            val estadisticasVictoria = gestorProgreso.obtenerEstadisticasPartida(dificultad, tiempoUsado)
            ultimasEstadisticasPartida = estadisticasVictoria

            val sufijoDesbloqueo = if (siguiente != null) {
                " ¡Nivel ${siguiente.nombreVisible} desbloqueado!"
            } else {
                " ¡Misión final completada!"
            }

            gestorFeedback.mostrarExito(
                "¡Correcto! Respuesta dentro de la tolerancia (±${dificultad.margenErrorTolerable} ${problemaActual.unidadMedida}).$sufijoDesbloqueo"
            )

            // Habilita acción directa de transición y notifica el fin del nivel a través de INavegadorEscenas (Issue #20)
            btnAvanzarNivel.text = obtenerTextoBotonSiguienteNivel()
            btnAvanzarNivel.isVisible = true
            btnAvanzarNivel.isManaged = true

            iniciarOReiniciarSimulacion()
            navegador.irANivelCompletado(dificultad, estadisticasVictoria, siguiente)
            true
        } else {
            // 1. Penalización de puntaje y descuento de 1 vida (Issue #18)
            gestorProgreso.registrarIntentoFallido(problemaActual.id)
            val vidasRestantes = gestorProgreso.perderVida()

            if (vidasRestantes <= 0 || gestorProgreso.esGameOver()) {
                // 2. Game Over (0 vidas): Captura estadísticas, reinicia el progreso bloqueando Medio/Difícil,
                // restablece las 3 vidas y notifica a INavegadorEscenas (Issues #15, #18 & #20)
                val tiempoUsado = calcularTiempoTranscurridoSegundos()
                gestorProgreso.registrarTiempoNivel(tiempoUsado)
                val estadisticasDerrota = gestorProgreso.generarEstadisticasGameOver(dificultad, tiempoUsado)
                ultimasEstadisticasPartida = estadisticasDerrota

                gestorProgreso.registrarGameOver()
                reiniciosPorGameOver += 1
                reiniciarNivelActual(preservarMensajeGameOver = true)
                gestorFeedback.mostrarGameOver(
                    "💀 ¡Game Over / Nivel Fallido! Te quedaste sin vidas. Progreso reiniciado con 3 vidas."
                )
                navegador.irAGameOver(dificultad, estadisticasDerrota)
            } else {
                detenerYReiniciarLienzo()
                gestorFeedback.mostrarErrorPrecision(
                    "¡Intenta de nuevo! Tu respuesta no es precisa. (Vidas restantes: $vidasRestantes)"
                )
            }
            false
        }
    }

    /**
     * Inicia o reinicia la simulación del objeto en movimiento en el Canvas según sus condiciones iniciales.
     */
    fun iniciarOReiniciarSimulacion() {
        val alturaEfectiva = problemaActual.alturaReferenciaSimulacion
        renderizador.reiniciarEfectos()
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
        renderizador.reiniciarEfectos()
        renderizador.renderizar(objetoSimulado)
    }
}
