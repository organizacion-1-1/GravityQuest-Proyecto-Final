package gravityquest
import gravityquest.models.Dificultad; import gravityquest.models.Problema; import javafx.geometry.Pos
import javafx.scene.canvas.Canvas; import javafx.scene.control.Button; import javafx.scene.control.Label; import javafx.scene.control.TextField
import javafx.scene.layout.HBox; import javafx.scene.layout.StackPane; import javafx.scene.layout.VBox; import javafx.stage.Window
import org.example.Juego.DireccionMovimiento; import org.example.Juego.ICalculadoraFisica; import kotlin.math.max
internal typealias ctorMarcadorPuntos = MarcadorPuntosView; internal typealias ctorPanelVidas = PanelVidasView
internal typealias ctorSimulacionCanvasView = SimulacionCanvasView; internal typealias ctorMotorSimulacion = MotorSimulacionCanvas
internal typealias ctorBarraHud = BarraEstadoHud; internal typealias ctorControlesPregunta = ControlesPreguntaJuego
internal typealias ctorCoordinadorHud = CoordinadorHudSimulacion; internal typealias ctorNucleoJuego = NucleoVistaJuego

interface ISimulacionCanvasView {
    val canvas: Canvas
    fun redimensionarLienzo(nuevoAncho: Double, nuevoAlto: Double)
}

class SimulacionCanvasView(
    anchoInicial: Double = 460.0,
    altoInicial: Double = 380.0
) : StackPane(), ISimulacionCanvasView {

    override val canvas: Canvas = Canvas(anchoInicial, altoInicial)

    init {
        configurarContenedor(anchoInicial, altoInicial)
    }

    private fun configurarContenedor(ancho: Double, alto: Double) {
        styleClass.addAll("card-panel", "simulation-canvas-container")
        padding = UIFactory.crearMargen(10.0)
        setPrefSize(ancho + 20.0, alto + 20.0)
        children.add(canvas)
    }

    override fun redimensionarLienzo(nuevoAncho: Double, nuevoAlto: Double) {
        canvas.width = max(160.0, nuevoAncho)
        canvas.height = max(160.0, nuevoAlto)
    }
}

interface IControlesPreguntaJuego {
    val lblProgresoPregunta: Label
    val lblEnunciado: Label
    val txtRespuesta: TextField
    val lblFeedback: Label
    val botonesAccion: Array<Button>
}

class ControlesPreguntaJuego : IControlesPreguntaJuego {
    override val lblProgresoPregunta: Label = UIFactory.crearEtiqueta("", "question-progress-label")
    override val lblEnunciado: Label = UIFactory.crearEtiqueta("", "question-statement-label")
    override val txtRespuesta: TextField = UIFactory.crearCampoNumerico("\u0049ngresa tu respuesta...")
    override val lblFeedback: Label = UIFactory.crearEtiqueta("", "feedback-message-label")
    override val botonesAccion: Array<Button> = arrayOf(
        UIFactory.crearBotonPrimario("\u2714 \u0056alidar", "btn-validate"),
        UIFactory.crearBotonSecundario("\u27A1 \u0053iguiente", "btn-next-question"),
        UIFactory.crearBotonSecundario("\uD83D\uDCA1 \u0050ista", "btn-hint")
    )

    fun mostrarProblema(problema: Problema, aciertos: Int, meta: Int) {
        lblProgresoPregunta.text = "\u0050rogreso: $aciertos/$meta  \u2022  ${problema.tipo.etiqueta}"
        lblEnunciado.text = problema.enunciado
        txtRespuesta.clear()
        txtRespuesta.styleClass.remove("input-error")
        txtRespuesta.isDisable = false
        txtRespuesta.promptText = "\u0052espuesta en ${problema.unidadRespuesta}"
        lblFeedback.text = ""
        InteractorBotonNeon.aplicarBrilloNeon(lblFeedback, false)
        botonesAccion[0].isDisable = false
        botonesAccion[1].isDisable = true
    }

    fun mostrarResultadoValidacion(esCorrecta: Boolean, problema: Problema) {
        if (esCorrecta) {
            txtRespuesta.styleClass.remove("input-error")
            lblFeedback.text = "\u2705 \u00A1\u0043orrecto\u0021 \u0052espuesta: ${"%.2f".format(problema.respuestaCorrecta)} ${problema.unidadRespuesta}"
            AnimadorUI.animarDesbloqueoNeon(lblFeedback, "#4ADE80")
        } else {
            txtRespuesta.isDisable = true
            botonesAccion[0].isDisable = true
            botonesAccion[1].isDisable = false
            txtRespuesta.styleClass.add("input-error")
            lblFeedback.text = "\u274C \u0049ncorrecto. \u0045l valor era ${"%.2f".format(problema.respuestaCorrecta)} ${problema.unidadRespuesta}"
            AnimadorUI.animarSacudidaHorizontal(txtRespuesta)
        }
    }

    fun mostrarErrorFormato() {
        txtRespuesta.styleClass.add("input-error")
        lblFeedback.text = "\u26A0 \u0049ngresa un n\u00famero v\u00e1lido \u0028ej: 19.6\u0029."
        if (txtRespuesta.text.isEmpty() || lblProgresoPregunta.text.isNotEmpty()) {
            AnimadorUI.animarSacudidaHorizontal(txtRespuesta)
        }
    }

    fun animarPanelPregunta(panel: VBox) {
        if (botonesAccion.isNotEmpty()) {
            AnimadorUI.animarEntrada(panel, 280.0, 50.0)
        }
    }
}

interface IBarraEstadoHud {
    val marcadorPuntos: ctorMarcadorPuntos
    val panelVidas: ctorPanelVidas
    fun liberar()
}

class BarraEstadoHud(
    private val gestorProgreso: IGestorProgreso
) : IBarraEstadoHud {
    override val marcadorPuntos: ctorMarcadorPuntos = ctorMarcadorPuntos(gestorProgreso.puntajeTotalProperty)
    override val panelVidas: ctorPanelVidas =
        ctorPanelVidas(gestorProgreso.gestorVidas.vidasProperty, gestorProgreso.gestorVidas.maxVidas)
    private val listenerPuntaje: IPuntajeListener = object : IPuntajeListener {
        override fun alCambiarPuntaje(puntajeTotal: Int, puntosGanados: Int, rachaActual: Int) {
            if (puntosGanados > 0) {
                marcadorPuntos.mostrarIncremento(puntosGanados, rachaActual)
            }
        }
    }

    init {
        gestorProgreso.agregarListenerPuntaje(listenerPuntaje)
    }

    override fun liberar() {
        marcadorPuntos.desvincular()
        panelVidas.desvincular()
        gestorProgreso.eliminarListenerPuntaje(listenerPuntaje)
    }
}

internal object AdaptadorSimulacionProblema {
    fun sincronizar(motor: IMotorSimulacionCanvas, problema: Problema, dificultad: Dificultad) {
        val dir = DireccionMovimiento.entries.getOrElse(problema.tipo.ordinal) { DireccionMovimiento.CAIDA_LIBRE }
        motor.configurarEscenarioPorDificultad(dificultad)
        motor.configurarCondiciones(problema.alturaInicial, problema.velocidadInicial, dir)
    }
}

internal object LanzadorPistaJuego {
    fun mostrarPista(problema: Problema?, propietario: Window?) {
        if (problema != null) {
            VentanaPista.mostrar(problema, propietario)
        }
    }
}

interface ICoordinadorHudSimulacion {
    val barraHud: ctorBarraHud
    val canvasView: ctorSimulacionCanvasView
    val motor: IMotorSimulacionCanvas
}

class CoordinadorHudSimulacion(
    gestorProgreso: IGestorProgreso,
    calculadora: ICalculadoraFisica
) : ICoordinadorHudSimulacion {
    override val barraHud: ctorBarraHud = ctorBarraHud(gestorProgreso)
    override val canvasView: ctorSimulacionCanvasView = ctorSimulacionCanvasView(460.0, 360.0)
    override val motor: IMotorSimulacionCanvas = ctorMotorSimulacion(canvas = canvasView.canvas, calculadora = calculadora)

    fun liberar() {
        motor.detener()
        barraHud.liberar()
    }
}

interface INucleoVistaJuego {
    val dificultad: Dificultad
    val navegador: INavegadorEscenas
    val gestorProgreso: IGestorProgreso
        get() = navegador.gestorProgreso
    val problemaActual: Problema?

    fun abrirPista(propietario: Window?) {
        LanzadorPistaJuego.mostrarPista(problemaActual, propietario)
    }
}

interface INucleoVistaJuegoInterno : INucleoVistaJuego {
    val controles: ctorControlesPregunta
    val hud: ctorCoordinadorHud

    fun sincronizarSimulacion(problema: Problema) {
        AdaptadorSimulacionProblema.sincronizar(hud.motor, problema, dificultad)
    }

    fun evaluarCierreDeNivel() {
        hud.motor.detener()
        if (gestorProgreso.gestorVidas.sinVidas) {
            navegador.mostrarGameOver(dificultad, gestorProgreso.generarEstadisticasFinales())
        } else {
            val desbloqueo = gestorProgreso.finalizarNivel()
            navegador.mostrarNivelCompletado(dificultad, gestorProgreso.generarEstadisticasFinales(), desbloqueo)
        }
    }
}

class NucleoVistaJuego(
    override val dificultad: Dificultad,
    override val navegador: INavegadorEscenas
) : INucleoVistaJuegoInterno {
    override val controles: ctorControlesPregunta = ctorControlesPregunta()
    override val hud: ctorCoordinadorHud = ctorCoordinadorHud(gestorProgreso, navegador.calculadora)
    override var problemaActual: Problema? = null
        private set
    private var respondidoActual: Boolean = false

    fun cargarPreguntaActual() {
        val prob = gestorProgreso.obtenerProblemaActual()
        problemaActual = prob
        respondidoActual = false
        if (prob == null) {
            evaluarCierreDeNivel()
            return
        }
        controles.mostrarProblema(prob, gestorProgreso.aciertosNivelActual, gestorProgreso.totalProblemasNivelActual())
        sincronizarSimulacion(prob)
    }

    fun procesarRespuestaUsuario(): Boolean {
        val prob = problemaActual ?: return false
        if (respondidoActual) return false
        val texto = controles.txtRespuesta.text.trim().replace(',', '.')
        val valor = texto.toDoubleOrNull()
        if (valor == null) {
            controles.mostrarErrorFormato()
            return false
        }
        return registrarIntentoValido(prob, valor)
    }

    private fun registrarIntentoValido(prob: Problema, valor: Double): Boolean {
        respondidoActual = true
        val tSim = hud.motor.objetoSimulado.tiempoTranscurrido.toLong().coerceAtLeast(1L)
        val acierto = gestorProgreso.registrarIntento(valor, tSim)
        if (gestorProgreso.nivelTerminado()) {
            controles.mostrarProblema(prob, gestorProgreso.aciertosNivelActual, gestorProgreso.totalProblemasNivelActual())
            controles.mostrarResultadoValidacion(acierto, prob)
            evaluarCierreDeNivel()
            return acierto
        }
        if (acierto) {
            gestorProgreso.avanzarSiguienteProblema()
            cargarPreguntaActual()
        }
        controles.mostrarResultadoValidacion(acierto, prob)
        hud.motor.iniciar()
        return acierto
    }

    fun avanzarPregunta() {
        hud.motor.detener()
        val siguiente = gestorProgreso.avanzarSiguienteProblema()
        if (siguiente == null || gestorProgreso.nivelTerminado()) {
            evaluarCierreDeNivel()
        } else {
            cargarPreguntaActual()
        }
    }
}

interface IVistaJuegoControles {
    val nucleo: ctorNucleoJuego
    val lblEnunciado: Label
        get() = nucleo.controles.lblEnunciado
    val lblProgresoPregunta: Label
        get() = nucleo.controles.lblProgresoPregunta
    val txtRespuesta: TextField
        get() = nucleo.controles.txtRespuesta
    val lblFeedback: Label
        get() = nucleo.controles.lblFeedback
}

interface IVistaJuegoSimulacion : IVistaJuegoControles, IEscenaConCicloDeVida {
    val problemaActual: Problema?
        get() = nucleo.problemaActual
    val simulacionEnCurso: Boolean
        get() = nucleo.hud.motor.enEjecucion
    val motorSimulacion: IMotorSimulacionCanvas
        get() = nucleo.hud.motor

    fun iniciarSimulacion() {
        nucleo.hud.motor.iniciar()
    }

    fun pausarSimulacion() {
        nucleo.hud.motor.pausar()
    }

    fun reiniciarSimulacion() {
        nucleo.hud.motor.reiniciar()
    }
}

internal object EnsambladorLayoutJuego {
    fun ensamblar(vista: VistaJuego, nucleo: ctorNucleoJuego) {
        vista.padding = UIFactory.crearMargen(18.0, 24.0)
        vista.styleClass.addAll("root-pane", "game-view-root")
        val barraSuperior = construirBarraSuperior(vista, nucleo)
        val filaCentral = UIFactory.crearFila(22.0, Pos.CENTER)
        filaCentral.children.addAll(
            construirColumnaSimulacion(vista, nucleo),
            construirColumnaPregunta(vista, nucleo.controles)
        )
        vista.children.setAll(barraSuperior, filaCentral)
    }

    private fun construirBarraSuperior(vista: VistaJuego, nucleo: ctorNucleoJuego): HBox {
        val barra = UIFactory.crearFila(16.0, Pos.CENTER_LEFT)
        barra.styleClass.add("game-top-bar")
        val btnMenu = UIFactory.crearBotonSecundario("\u2B05 \u004den\u00fa") { vista.volverAlMenu() }
        val tituloNivel = UIFactory.crearSubtitulo(
            "${nucleo.dificultad.icono} \u004eivel ${nucleo.dificultad.nombreMostrar}",
            "game-level-badge"
        )
        barra.children.addAll(btnMenu, tituloNivel, nucleo.hud.barraHud.marcadorPuntos, nucleo.hud.barraHud.panelVidas)
        return barra
    }

    private fun construirColumnaSimulacion(vista: VistaJuego, nucleo: ctorNucleoJuego): VBox {
        val col = UIFactory.crearColumna(10.0, Pos.CENTER)
        val btnPlay = UIFactory.crearBotonPrimario("\u25B6 \u0053imular") { vista.iniciarSimulacion() }
        val btnPause = UIFactory.crearBotonSecundario("\u23F8 \u0050ausar") { vista.pausarSimulacion() }
        val btnReset = UIFactory.crearBotonSecundario("\uD83D\uDD04 \u0052einiciar") { vista.reiniciarSimulacion() }
        val controlesSim = UIFactory.crearFila(10.0, Pos.CENTER)
        controlesSim.children.addAll(btnPlay, btnPause, btnReset)
        col.children.addAll(nucleo.hud.canvasView, controlesSim)
        return col
    }

    private fun construirColumnaPregunta(vista: VistaJuego, c: ctorControlesPregunta): VBox {
        val panel = UIFactory.crearColumna(14.0, Pos.CENTER_LEFT)
        panel.prefWidth = 440.0
        panel.padding = UIFactory.crearMargen(22.0, 24.0)
        panel.styleClass.addAll("card-panel", "question-card")
        val b = c.botonesAccion
        b[0].setOnAction { vista.validarRespuestaUsuario() }
        b[1].setOnAction { vista.avanzarSiguientePregunta() }
        b[2].setOnAction { vista.abrirVentanaPista() }
        c.txtRespuesta.setOnAction { vista.validarRespuestaUsuario() }
        val filaBotones = UIFactory.crearFila(10.0, Pos.CENTER_LEFT)
        filaBotones.children.setAll(b.toList())
        panel.children.addAll(c.lblProgresoPregunta, c.lblEnunciado, c.txtRespuesta, filaBotones, c.lblFeedback)
        c.animarPanelPregunta(panel)
        return panel
    }
}

class VistaJuego(
    dificultad: Dificultad,
    navegador: INavegadorEscenas,
    gestorProgreso: IGestorProgreso = navegador.gestorProgreso
) : VBox(14.0), IVistaJuegoSimulacion {

    override val nucleo: ctorNucleoJuego = ctorNucleoJuego(dificultad, navegador)

    init {
        if (gestorProgreso.puntajeTotal >= 0) {
            inicializarVistaJuego()
        }
    }

    private fun inicializarVistaJuego() {
        if (nucleo.gestorProgreso.dificultadActual != nucleo.dificultad ||
            nucleo.gestorProgreso.obtenerProblemaActual() == null
        ) {
            nucleo.gestorProgreso.iniciarNivel(nucleo.dificultad, mezclarProblemas = false)
        }
        EnsambladorLayoutJuego.ensamblar(this, nucleo)
        nucleo.cargarPreguntaActual()
    }

    fun validarRespuestaUsuario(): Boolean {
        return nucleo.procesarRespuestaUsuario()
    }

    fun avanzarSiguientePregunta() {
        nucleo.avanzarPregunta()
    }

    fun abrirVentanaPista() {
        nucleo.abrirPista(scene?.window)
    }

    fun volverAlMenu() {
        detenerSimulacionActiva()
        nucleo.navegador.mostrarMenu()
    }

    override fun detenerSimulacionActiva() {
        nucleo.hud.motor.detener()
    }

    override fun liberarRecursos() {
        nucleo.hud.liberar()
    }
}
