package gravityquest
import gravityquest.models.*; import javafx.geometry.Pos; import org.example.Juego.*; import kotlin.math.max
internal typealias ctorCanvas = ICanvas; internal typealias ctorMarcadorPuntos = MarcadorPuntosView; internal typealias ctorPanelVidas = PanelVidasView
internal typealias ctorSimulacionCanvasView = SimulacionCanvasView; internal typealias ctorMotorSimulacion = MotorSimulacionCanvas

interface ISimulacionCanvasView {
    val canvas: ICanvas
    fun redimensionarLienzo(nuevoAncho: IDouble, nuevoAlto: IDouble)
}

class SimulacionCanvasView(
    anchoInicial: IDouble = 460.0,
    altoInicial: IDouble = 380.0
) : IStackPane(), ISimulacionCanvasView {

    override val canvas: ICanvas = ctorCanvas(anchoInicial, altoInicial)

    init {
        configurarContenedor(anchoInicial, altoInicial)
    }

    private fun configurarContenedor(ancho: IDouble, alto: IDouble) {
        styleClass.addAll("card-panel", "simulation-canvas-container")
        padding = UIFactory.crearMargen(10.0)
        setPrefSize(ancho + 20.0, alto + 20.0)
        children.add(canvas)
    }

    override fun redimensionarLienzo(nuevoAncho: IDouble, nuevoAlto: IDouble) {
        canvas.width = max(160.0, nuevoAncho)
        canvas.height = max(160.0, nuevoAlto)
    }
}

interface IControlesPreguntaJuego {
    val lblProgresoPregunta: ILabel
    val lblEnunciado: ILabel
    val txtRespuesta: ITextField
    val lblFeedback: ILabel
    val botonesAccion: Array<IButton>
}

class ControlesPreguntaJuego : IControlesPreguntaJuego {
    override val lblProgresoPregunta: ILabel = UIFactory.crearEtiqueta("", "question-progress-label")
    override val lblEnunciado: ILabel = UIFactory.crearEtiqueta("", "question-statement-label")
    override val txtRespuesta: ITextField = UIFactory.crearCampoNumerico("\u0049ngresa tu respuesta...")
    override val lblFeedback: ILabel = UIFactory.crearEtiqueta("", "feedback-message-label")
    override val botonesAccion: Array<IButton> = arrayOf(
        UIFactory.crearBotonPrimario("\u2714 \u0056alidar", "btn-validate"),
        UIFactory.crearBotonSecundario("\u27A1 \u0053iguiente", "btn-next-question"),
        UIFactory.crearBotonSecundario("\uD83D\uDCA1 \u0050ista", "btn-hint")
    )

    fun mostrarProblema(problema: Problema, aciertos: IInt, meta: IInt) {
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

    fun mostrarResultadoValidacion(esCorrecta: IBoolean, problema: Problema) {
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
}

internal typealias ctorControlesPregunta = ControlesPreguntaJuego
internal typealias ctorCoordinadorHud = CoordinadorHudSimulacion
internal typealias ctorNucleoJuego = NucleoVistaJuego

interface ICoordinadorHudSimulacion {
    val gestorProgreso: IGestorProgreso
    val marcadorPuntos: ctorMarcadorPuntos
    val panelVidas: ctorPanelVidas
    val canvasView: ctorSimulacionCanvasView
    val motor: ctorMotorSimulacion
}

class CoordinadorHudSimulacion(
    override val gestorProgreso: IGestorProgreso,
    calculadora: ICalculadoraFisica
) : ICoordinadorHudSimulacion {
    override val marcadorPuntos: ctorMarcadorPuntos = ctorMarcadorPuntos(gestorProgreso.puntajeTotalProperty)
    override val panelVidas: ctorPanelVidas =
        ctorPanelVidas(gestorProgreso.gestorVidas.vidasProperty, gestorProgreso.gestorVidas.maxVidas)
    override val canvasView: ctorSimulacionCanvasView = ctorSimulacionCanvasView(460.0, 360.0)
    override val motor: ctorMotorSimulacion = ctorMotorSimulacion(canvas = canvasView.canvas, calculadora = calculadora)
    private val listenerPuntaje: IPuntajeListener = object : IPuntajeListener {
        override fun alCambiarPuntaje(puntajeTotal: IInt, puntosGanados: IInt, rachaActual: IInt) {
            if (puntosGanados > 0) {
                marcadorPuntos.mostrarIncremento(puntosGanados, rachaActual)
            }
        }
    }

    init {
        gestorProgreso.agregarListenerPuntaje(listenerPuntaje)
    }

    fun sincronizarConProblema(problema: Problema, dificultad: Dificultad) {
        val dir = mapearDireccion(problema.tipo)
        motor.configurarEscenarioPorDificultad(dificultad)
        motor.configurarCondiciones(problema.alturaInicial, problema.velocidadInicial, dir)
    }

    fun liberar() {
        motor.detener()
        marcadorPuntos.desvincular()
        panelVidas.desvincular()
        gestorProgreso.eliminarListenerPuntaje(listenerPuntaje)
    }

    private fun mapearDireccion(tipo: TipoProblema): DireccionMovimiento {
        return DireccionMovimiento.entries.getOrElse(tipo.ordinal) { DireccionMovimiento.CAIDA_LIBRE }
    }
}

interface INucleoVistaJuego {
    val dificultad: Dificultad
    val navegador: INavegadorEscenas
    val gestorProgreso: IGestorProgreso
    var problemaActual: Problema?
}

interface INucleoVistaJuegoInterno : INucleoVistaJuego {
    val controles: ControlesPreguntaJuego
    val hud: CoordinadorHudSimulacion
}

class NucleoVistaJuego(
    override val dificultad: Dificultad,
    override val navegador: INavegadorEscenas,
    override val gestorProgreso: IGestorProgreso
) : INucleoVistaJuegoInterno {
    override val controles: ControlesPreguntaJuego = ctorControlesPregunta()
    override val hud: CoordinadorHudSimulacion = ctorCoordinadorHud(gestorProgreso, navegador.calculadora)
    override var problemaActual: Problema? = null
    private var respondidoActual: IBoolean = false

    fun cargarPreguntaActual() {
        val prob = gestorProgreso.obtenerProblemaActual()
        problemaActual = prob
        respondidoActual = false
        if (prob == null) {
            evaluarCierreDeNivel()
            return
        }
        controles.mostrarProblema(prob, gestorProgreso.aciertosNivelActual, gestorProgreso.totalProblemasNivelActual())
        hud.sincronizarConProblema(prob, dificultad)
    }

    fun procesarRespuestaUsuario(): IBoolean {
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

    private fun registrarIntentoValido(prob: Problema, valor: IDouble): IBoolean {
        respondidoActual = true
        val tSim = hud.motor.objetoSimulado.tiempoTranscurrido.toLong().coerceAtLeast(1L)
        val acierto = gestorProgreso.registrarIntento(valor, tSim)
        if (gestorProgreso.nivelTerminado()) {
            controles.mostrarProblema(prob, gestorProgreso.aciertosNivelActual, gestorProgreso.totalProblemasNivelActual())
            controles.mostrarResultadoValidacion(acierto, prob)
            evaluarCierreDeNivel()
            return acierto
        }
        procesarContinuacionIntento(acierto, prob)
        return acierto
    }

    private fun procesarContinuacionIntento(acierto: IBoolean, prob: Problema) {
        if (acierto) {
            gestorProgreso.avanzarSiguienteProblema()
            cargarPreguntaActual()
        }
        controles.mostrarResultadoValidacion(acierto, prob)
        hud.motor.iniciar()
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

    private fun evaluarCierreDeNivel() {
        hud.motor.detener()
        if (gestorProgreso.gestorVidas.sinVidas) {
            navegador.mostrarGameOver(dificultad, gestorProgreso.generarEstadisticasFinales())
        } else {
            val desbloqueo = gestorProgreso.finalizarNivel()
            navegador.mostrarNivelCompletado(dificultad, gestorProgreso.generarEstadisticasFinales(), desbloqueo)
        }
    }
}

interface IVistaJuegoControles {
    val nucleo: NucleoVistaJuego
    val lblEnunciado: ILabel
        get() = nucleo.controles.lblEnunciado
    val lblProgresoPregunta: ILabel
        get() = nucleo.controles.lblProgresoPregunta
    val txtRespuesta: ITextField
        get() = nucleo.controles.txtRespuesta
    val lblFeedback: ILabel
        get() = nucleo.controles.lblFeedback
}

interface IVistaJuegoSimulacion : IEscenaConCicloDeVida {
    val nucleo: NucleoVistaJuego
    val problemaActual: Problema?
        get() = nucleo.problemaActual
    val simulacionEnCurso: IBoolean
        get() = nucleo.hud.motor.enEjecucion
    val motorSimulacion: MotorSimulacionCanvas
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
    fun ensamblar(vista: VistaJuego, nucleo: NucleoVistaJuego) {
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

    private fun construirBarraSuperior(vista: VistaJuego, nucleo: NucleoVistaJuego): IHBox {
        val barra = UIFactory.crearFila(16.0, Pos.CENTER_LEFT)
        barra.styleClass.add("game-top-bar")
        val btnMenu = UIFactory.crearBotonSecundario("\u2B05 \u004den\u00fa") { vista.volverAlMenu() }
        val tituloNivel = UIFactory.crearSubtitulo(
            "${nucleo.dificultad.icono} \u004eivel ${nucleo.dificultad.nombreMostrar}",
            "game-level-badge"
        )
        barra.children.addAll(btnMenu, tituloNivel, nucleo.hud.marcadorPuntos, nucleo.hud.panelVidas)
        return barra
    }

    private fun construirColumnaSimulacion(vista: VistaJuego, nucleo: NucleoVistaJuego): IVBox {
        val col = UIFactory.crearColumna(10.0, Pos.CENTER)
        val btnPlay = UIFactory.crearBotonPrimario("\u25B6 \u0053imular") { vista.iniciarSimulacion() }
        val btnPause = UIFactory.crearBotonSecundario("\u23F8 \u0050ausar") { vista.pausarSimulacion() }
        val btnReset = UIFactory.crearBotonSecundario("\uD83D\uDD04 \u0052einiciar") { vista.reiniciarSimulacion() }
        val controlesSim = UIFactory.crearFila(10.0, Pos.CENTER)
        controlesSim.children.addAll(btnPlay, btnPause, btnReset)
        col.children.addAll(nucleo.hud.canvasView, controlesSim)
        return col
    }

    private fun construirColumnaPregunta(vista: VistaJuego, c: ControlesPreguntaJuego): IVBox {
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
        AnimadorUI.animarEntrada(panel, 280.0, 50.0)
        return panel
    }
}

class VistaJuego(
    dificultad: Dificultad,
    navegador: INavegadorEscenas,
    gestorProgreso: IGestorProgreso = navegador.gestorProgreso,
    repositorio: IRepositorioProblemas = navegador.repositorio,
    calculadora: ICalculadoraFisica = navegador.calculadora
) : IVBox(14.0), IVistaJuegoControles, IVistaJuegoSimulacion {

    override val nucleo: NucleoVistaJuego = NucleoVistaJuego(dificultad, navegador, gestorProgreso)

    init {
        if (repositorio.contarPorDificultad(dificultad) >= 0 && calculadora.gravedadPorDefecto > 0.0) {
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

    fun validarRespuestaUsuario(): IBoolean {
        return nucleo.procesarRespuestaUsuario()
    }

    fun avanzarSiguientePregunta() {
        nucleo.avanzarPregunta()
    }

    fun abrirVentanaPista() {
        val prob = nucleo.problemaActual ?: return
        VentanaPista.mostrar(prob, scene?.window)
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
