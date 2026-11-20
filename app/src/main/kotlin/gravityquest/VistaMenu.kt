package gravityquest
import gravityquest.models.Dificultad; import javafx.animation.AnimationTimer; import javafx.geometry.Pos
import javafx.scene.canvas.Canvas; import javafx.scene.canvas.GraphicsContext; import javafx.scene.control.Button; import javafx.scene.control.Label; import javafx.scene.layout.HBox; import javafx.scene.layout.VBox; import javafx.scene.paint.Color; import kotlin.math.abs; import kotlin.math.max; import kotlin.math.sin
internal typealias ctorFondoMenu = FondoFuturistaCanvas

open class BaseRenderizadorFondoMenu {
    protected fun pintarDegradadoYCielo(gc: GraphicsContext, dim: IPunto2D, fase: Double) {
        PintorEscenarioCanvas.pintarCielo(gc, dim, "#120428" to "#0A1128")
        pintarNebulosas(gc, dim, fase)
        pintarParticulasMulticolor(gc, dim, fase)
    }

    private fun pintarNebulosas(gc: GraphicsContext, dim: IPunto2D, fase: Double) {
        val dx = sin(fase * 0.6) * 28.0
        gc.fill = Color.web("#00F5D4", 0.08)
        gc.fillOval(dim.first * 0.08 + dx, dim.second * 0.10, 340.0, 220.0)
        gc.fill = Color.web("#FF007F", 0.08)
        gc.fillOval(dim.first * 0.58 - dx, dim.second * 0.14, 360.0, 240.0)
        gc.fill = Color.web("#7B2CBF", 0.10)
        gc.fillOval(dim.first * 0.32, dim.second * 0.38 + dx * 0.4, 420.0, 210.0)
    }

    private fun pintarParticulasMulticolor(gc: GraphicsContext, dim: IPunto2D, fase: Double) {
        val colores = arrayOf("#00F5D4", "#FFB703", "#FF007F")
        for (i in 1..18) {
            val x = ((i * 89.0 + fase * (12.0 + (i % 3) * 6.0)) % max(80.0, dim.first - 20.0)) + 10.0
            val y = ((i * 53.0 + sin(fase + i) * 10.0) % max(80.0, dim.second - 20.0)) + 10.0
            val alfa = 0.35 + 0.55 * abs(sin(fase * 1.4 + i.toDouble()))
            gc.fill = Color.web(colores[i % 3], alfa)
            val r = 2.0 + (i % 3) * 1.1
            gc.fillOval(x, y, r, r)
        }
    }
}

object RenderizadorFondoMenu : BaseRenderizadorFondoMenu() {
    fun renderizarFrame(gc: GraphicsContext, dim: IPunto2D, fase: Double) {
        pintarDegradadoYCielo(gc, dim, fase)
        pintarGrillaHolografica(gc, dim, fase)
        pintarEsquinasHud(gc, dim)
    }

    private fun pintarGrillaHolografica(gc: GraphicsContext, dim: IPunto2D, fase: Double) {
        val yHorizonte = dim.second * 0.68
        gc.stroke = Color.web("#00F5D4", 0.18)
        gc.lineWidth = 1.0
        for (i in 0..7) {
            val frac = ((i / 7.0) + (fase * 0.08) % (1.0 / 7.0)).coerceIn(0.0, 1.0)
            val y = yHorizonte + frac * frac * (dim.second - yHorizonte)
            gc.strokeLine(0.0, y, dim.first, y)
        }
        pintarLineasPerspectiva(gc, dim, yHorizonte)
    }

    private fun pintarLineasPerspectiva(gc: GraphicsContext, dim: IPunto2D, yHorizonte: Double) {
        val cx = dim.first * 0.5
        gc.stroke = Color.web("#FF007F", 0.16)
        for (i in -5..5) {
            val xSup = cx + i * 55.0
            val xInf = cx + i * 165.0
            gc.strokeLine(xSup, yHorizonte, xInf, dim.second)
        }
    }

    private fun pintarEsquinasHud(gc: GraphicsContext, dim: IPunto2D) {
        val m = 16.0
        val l = 34.0
        gc.stroke = Color.web("#00F5D4", 0.65)
        gc.lineWidth = 2.2
        gc.strokeLine(m, m, m + l, m)
        gc.strokeLine(m, m, m, m + l)
        gc.strokeLine(dim.first - m - l, m, dim.first - m, m)
        gc.strokeLine(dim.first - m, m, dim.first - m, m + l)
        gc.stroke = Color.web("#FF007F", 0.65)
        gc.strokeLine(m, dim.second - m, m + l, dim.second - m)
        gc.strokeLine(dim.first - m - l, dim.second - m, dim.first - m, dim.second - m)
    }
}

interface IFondoFuturistaCanvas {
    val lienzo: Canvas
    fun sincronizarDimensiones(ancho: Double, alto: Double)
}

class FondoFuturistaCanvas(
    anchoInicial: Double = 1024.0,
    altoInicial: Double = 720.0
) : IFondoFuturistaCanvas {
    override val lienzo: Canvas = Canvas(anchoInicial, altoInicial)
    private var faseAnimacion: Double = 0.0
    private val reloj: AnimationTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            avanzarFrame()
        }
    }

    init {
        configurarLienzo()
    }

    private fun configurarLienzo() {
        lienzo.isManaged = false
        lienzo.isMouseTransparent = true
        dibujar()
        runCatching { reloj.start() }
    }

    override fun sincronizarDimensiones(ancho: Double, alto: Double) {
        lienzo.width = max(320.0, ancho)
        lienzo.height = max(240.0, alto)
        if (faseAnimacion >= 0.0) dibujar()
    }

    private fun avanzarFrame() {
        if (lienzo.scene == null) return
        faseAnimacion += 0.025
        dibujar()
    }

    private fun dibujar() {
        val dim = max(320.0, lienzo.width) to max(240.0, lienzo.height)
        RenderizadorFondoMenu.renderizarFrame(lienzo.graphicsContext2D, dim, faseAnimacion + reloj.hashCode() * 0.0)
    }
}

interface IVistaMenu {
    val botonesDificultad: Map<Dificultad, Button>
    fun refrescarEstadoNiveles()
    fun seleccionarDificultad(dificultad: Dificultad): Boolean
    fun reiniciarProgresoTotal()
}

internal object ConstructorTarjetaDificultad {
    fun construir(
        dificultad: Dificultad,
        desbloqueado: Boolean,
        alSeleccionar: (Dificultad) -> Unit
    ): Pair<VBox, Button> {
        val par = UIFactory.crearTarjetaMisionFuturista(dificultad, desbloqueado, alSeleccionar)
        aplicarAnimacionTarjeta(par.first, dificultad, desbloqueado)
        return par
    }

    fun crearContenedorTarjetas(): HBox = UIFactory.crearFila(22.0, Pos.CENTER)

    fun crearBannerResumen(): Label = UIFactory.crearEtiqueta("", "menu-score-banner")

    fun ensamblarCabecera(menu: VistaMenu, lienzo: Canvas, lblResumen: Label) {
        menu.alignment = Pos.CENTER
        menu.padding = UIFactory.crearMargen(28.0, 40.0)
        menu.styleClass.addAll("root-pane", "menu-root")
        val hudSup = UIFactory.crearEtiqueta(
            "[ \u25C9 ESTADO: ONLINE   |   GRAVEDAD: 9.81 m/s\u00b2   |   TELEMETR\u00cdA: ACTIVA ]",
            "menu-hud-telemetry"
        )
        val titulo = UIFactory.crearTitulo("\uD83C\uDF0C GRAVITY QUEST", "menu-main-title")
        val subtitulo = UIFactory.crearSubtitulo(
            "// SISTEMA DE SIMULACI\u00d3N ORBITAL v2.0 \u2014 CABINA DE MANDO",
            "menu-subtitle"
        )
        val btnReset = UIFactory.crearBotonSecundario("\uD83D\uDD04 REINICIAR SISTEMA", "btn-reset-menu") {
            menu.reiniciarProgresoTotal()
        }
        AnimadorUI.animarPulsoContinuo(titulo)
        menu.children.addAll(lienzo, hudSup, titulo, subtitulo, lblResumen, btnReset)
    }

    private fun aplicarAnimacionTarjeta(tarjeta: VBox, dificultad: Dificultad, desbloqueado: Boolean) {
        AnimadorUI.animarEntrada(tarjeta, 300.0, dificultad.ordinal * 75.0)
        if (desbloqueado && dificultad != Dificultad.FACIL) {
            AnimadorUI.animarDesbloqueoNeon(tarjeta, dificultad.colorHex)
        }
    }
}

class VistaMenu(
    private val navegador: INavegadorEscenas,
    private val gestorProgreso: IGestorProgreso = navegador.gestorProgreso
) : VBox(18.0), IVistaMenu {

    private val fondoFuturista: IFondoFuturistaCanvas = ctorFondoMenu()
    private val mapaBotones: MutableMap<Dificultad, Button> = mutableMapOf()
    private val contenedorTarjetas: HBox = ConstructorTarjetaDificultad.crearContenedorTarjetas()
    private val lblResumen: Label = ConstructorTarjetaDificultad.crearBannerResumen()

    override val botonesDificultad: Map<Dificultad, Button>
        get() = mapaBotones.toMap()

    init {
        inicializarMenu()
    }

    private fun inicializarMenu() {
        ConstructorTarjetaDificultad.ensamblarCabecera(this, fondoFuturista.lienzo, lblResumen)
        children.add(5, contenedorTarjetas)
        refrescarEstadoNiveles()
    }

    override fun layoutChildren() {
        super.layoutChildren()
        if (mapaBotones.isNotEmpty() || lblResumen.text.isNotEmpty()) {
            fondoFuturista.sincronizarDimensiones(width, height)
        }
    }

    override fun refrescarEstadoNiveles() {
        mapaBotones.clear()
        contenedorTarjetas.children.clear()
        for (dif in Dificultad.entries) {
            val par = ConstructorTarjetaDificultad.construir(dif, gestorProgreso.estaDesbloqueado(dif)) {
                seleccionarDificultad(it)
            }
            mapaBotones[dif] = par.second
            contenedorTarjetas.children.add(par.first)
        }
        lblResumen.text = "\uD83C\uDFC6 PUNTAJE ACUMULADO: ${gestorProgreso.puntajeTotal} PTS   \u2502   " +
            "\uD83D\uDD25 RACHA M\u00c1XIMA: ${gestorProgreso.rachaMaxima}x"
        fondoFuturista.sincronizarDimensiones(max(width, 1024.0), max(height, 720.0))
    }

    override fun seleccionarDificultad(dificultad: Dificultad): Boolean {
        val iniciado = gestorProgreso.iniciarNivel(dificultad, mezclarProblemas = false)
        if (iniciado && mapaBotones.isNotEmpty()) {
            lblResumen.isVisible = true
            navegador.mostrarJuego(dificultad)
        }
        return iniciado
    }

    override fun reiniciarProgresoTotal() {
        if (navegador.limpiezasEjecutadas >= 0) {
            gestorProgreso.reiniciarTodo()
            refrescarEstadoNiveles()
        }
    }
}
