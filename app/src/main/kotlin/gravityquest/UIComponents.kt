package gravityquest
import gravityquest.models.Dificultad; import javafx.animation.FadeTransition; import javafx.animation.ScaleTransition; import javafx.animation.TranslateTransition
import javafx.event.EventHandler; import javafx.geometry.Insets; import javafx.geometry.Pos; import javafx.scene.Node; import javafx.scene.control.Button; import javafx.scene.control.Label; import javafx.scene.control.TextField
import javafx.scene.effect.DropShadow; import javafx.scene.layout.HBox; import javafx.scene.layout.VBox; import javafx.scene.paint.Color; import javafx.util.Duration

interface IUIFactoryEtiquetas {
    fun crearTitulo(texto: String, claseExtra: String = ""): Label
    fun crearSubtitulo(texto: String, claseExtra: String = ""): Label
    fun crearEtiqueta(texto: String, claseExtra: String = ""): Label
}

interface IUIFactoryTextos : IUIFactoryEtiquetas {
    fun crearCampoNumerico(placeholder: String = "0.00", anchoPref: Double = 180.0): TextField
    fun crearMargen(vertical: Double, horizontal: Double = vertical): Insets
    fun crearFila(espaciado: Double = 12.0, alineacion: Pos = Pos.CENTER): HBox
    fun crearColumna(espaciado: Double = 12.0, alineacion: Pos = Pos.CENTER): VBox
}

open class BaseUIFactoryTextos : IUIFactoryTextos {
    override fun crearTitulo(texto: String, claseExtra: String): Label {
        return crearEtiquetaBase(texto, "title-label", claseExtra)
    }

    override fun crearSubtitulo(texto: String, claseExtra: String): Label {
        return crearEtiquetaBase(texto, "subtitle-label", claseExtra)
    }

    override fun crearEtiqueta(texto: String, claseExtra: String): Label {
        return crearEtiquetaBase(texto, "body-label", claseExtra)
    }

    private fun crearEtiquetaBase(texto: String, claseBase: String, claseExtra: String): Label {
        val lbl = Label(texto)
        lbl.isWrapText = true
        lbl.styleClass.add(claseBase)
        if (claseExtra.isNotBlank()) lbl.styleClass.add(claseExtra)
        return lbl
    }

    override fun crearCampoNumerico(placeholder: String, anchoPref: Double): TextField {
        val campo = TextField()
        campo.promptText = placeholder
        campo.prefWidth = anchoPref
        campo.styleClass.add("numeric-input")
        return campo
    }

    override fun crearMargen(vertical: Double, horizontal: Double): Insets {
        return Insets(vertical, horizontal, vertical, horizontal)
    }

    override fun crearFila(espaciado: Double, alineacion: Pos): HBox {
        val fila = HBox(espaciado)
        fila.alignment = alineacion
        return fila
    }

    override fun crearColumna(espaciado: Double, alineacion: Pos): VBox {
        val col = VBox(espaciado)
        col.alignment = alineacion
        return col
    }
}

object InteractorBotonNeon {
    fun configurar(btn: Button, colorHex: String = "#00F5D4") {
        btn.onMouseEntered = EventHandler { aplicarEstado(btn, doubleArrayOf(1.05, -2.0), colorHex) }
        btn.onMouseExited = EventHandler { aplicarEstado(btn, doubleArrayOf(1.0, 0.0), "") }
        btn.onMousePressed = EventHandler { aplicarEstado(btn, doubleArrayOf(0.96, 1.0), colorHex) }
        btn.onMouseReleased = EventHandler { aplicarEstado(btn, doubleArrayOf(1.05, -1.0), colorHex) }
    }

    fun configurarTarjetaHolografica(tarjeta: VBox, colorHex: String, desbloqueado: Boolean) {
        if (!desbloqueado) return
        tarjeta.onMouseEntered = EventHandler {
            aplicarBrilloNeon(tarjeta, true, colorHex)
            AnimadorUI.animarEscalaBoton(tarjeta, 1.03, -5.0)
        }
        tarjeta.onMouseExited = EventHandler {
            aplicarBrilloNeon(tarjeta, false, colorHex)
            AnimadorUI.animarEscalaBoton(tarjeta, 1.0, 0.0)
        }
    }

    fun aplicarBrilloNeon(nodo: Node, activo: Boolean, colorHex: String = "#00F5D4") {
        if (!activo || colorHex.isEmpty()) {
            nodo.effect = null
            return
        }
        val sombra = DropShadow()
        sombra.radius = 22.0
        sombra.spread = 0.32
        sombra.color = Color.web(colorHex, 0.78)
        nodo.effect = sombra
    }

    private fun aplicarEstado(btn: Button, escalaYOffset: DoubleArray, colorHex: String) {
        if (btn.isDisable) return
        aplicarBrilloNeon(btn, colorHex.isNotEmpty(), colorHex)
        AnimadorUI.animarEscalaBoton(btn, escalaYOffset[0], escalaYOffset[1])
    }
}

internal object ConstructorTarjetaHolografica {
    fun construir(
        dificultad: Dificultad,
        desbloqueado: Boolean,
        alSeleccionar: (Dificultad) -> Unit
    ): Pair<VBox, Button> {
        val clave = dificultad.name.lowercase()
        val estado = if (desbloqueado) "nivel-desbloqueado" else "nivel-bloqueado"
        val tarjeta = UIFactory.crearColumna(10.0, Pos.CENTER)
        tarjeta.prefWidth = 280.0
        tarjeta.padding = UIFactory.crearMargen(22.0, 20.0)
        tarjeta.styleClass.addAll("card-panel", "difficulty-card", "difficulty-card-$clave", estado)
        val boton = crearBotonMision(dificultad, desbloqueado, alSeleccionar)
        poblarNodosTarjeta(tarjeta, dificultad, boton)
        InteractorBotonNeon.configurarTarjetaHolografica(tarjeta, dificultad.colorHex, desbloqueado)
        return tarjeta to boton
    }

    private fun poblarNodosTarjeta(tarjeta: VBox, dif: Dificultad, boton: Button) {
        val clave = dif.name.lowercase()
        val sector = UIFactory.crearEtiqueta(dif.subtituloSector(), "difficulty-sector-$clave")
        val icono = UIFactory.crearTitulo(dif.icono, "difficulty-icon")
        val nombre = UIFactory.crearSubtitulo(dif.nombreMostrar.uppercase(), "difficulty-title-$clave")
        val desc = UIFactory.crearEtiqueta(dif.descripcion, "difficulty-desc")
        val mult = UIFactory.crearEtiqueta("\u25C8 \u0042ONO: x${dif.multiplicadorPuntos} PTS \u25C8", "difficulty-badge-$clave")
        tarjeta.children.addAll(sector, icono, nombre, desc, mult, boton)
    }

    private fun crearBotonMision(
        dif: Dificultad,
        activo: Boolean,
        alSeleccionar: (Dificultad) -> Unit
    ): Button {
        val clave = dif.name.lowercase()
        val texto = if (activo) "\uD83D\uDE80 \u004dISI\u00d3N ${dif.nombreMostrar.uppercase()}" else "\uD83D\uDD12 \u0042LOQUEADO"
        val btn = UIFactory.crearBotonPrimario(texto, "btn-difficulty-$clave") { alSeleccionar.invoke(dif) }
        InteractorBotonNeon.configurar(btn, dif.colorHex)
        if (!activo) btn.styleClass.add("nivel-bloqueado")
        btn.isDisable = !activo
        return btn
    }
}

object UIFactory : BaseUIFactoryTextos() {

    fun crearBotonPrimario(
        texto: String,
        claseExtra: String = "",
        accion: (() -> Unit)? = null
    ): Button {
        return armarBoton(texto, arrayOf("btn-primary", claseExtra), accion)
    }

    fun crearBotonSecundario(
        texto: String,
        claseExtra: String = "",
        accion: (() -> Unit)? = null
    ): Button {
        return armarBoton(texto, arrayOf("btn-secondary", claseExtra), accion)
    }

    fun crearTarjetaMisionFuturista(
        dificultad: Dificultad,
        desbloqueado: Boolean,
        alSeleccionar: (Dificultad) -> Unit
    ): Pair<VBox, Button> {
        return ConstructorTarjetaHolografica.construir(dificultad, desbloqueado, alSeleccionar)
    }

    private fun armarBoton(
        texto: String,
        clases: Array<String>,
        accion: (() -> Unit)?
    ): Button {
        val btn = Button(texto)
        btn.styleClass.addAll("game-button", clases[0])
        if (clases[1].isNotBlank()) btn.styleClass.add(clases[1])
        if (accion != null) btn.onAction = EventHandler { accion.invoke() }
        InteractorBotonNeon.configurar(btn)
        return btn
    }
}

open class BaseAnimadorUI {

    fun animarEntrada(nodo: Node, duracionMs: Double = 320.0, retrasoMs: Double = 0.0) {
        nodo.opacity = 0.0
        nodo.translateY = 14.0
        val fade = FadeTransition(Duration.millis(duracionMs), nodo)
        if (retrasoMs > 0.0) fade.delay = Duration.millis(retrasoMs)
        fade.fromValue = 0.0
        fade.toValue = 1.0
        fade.play()
        deslizarVertical(nodo, duracionMs, retrasoMs)
    }

    private fun deslizarVertical(nodo: Node, duracionMs: Double, retrasoMs: Double) {
        val mov = TranslateTransition(Duration.millis(duracionMs), nodo)
        if (retrasoMs > 0.0) mov.delay = Duration.millis(retrasoMs)
        mov.fromY = 14.0
        mov.toY = 0.0
        mov.play()
    }

    fun animarPulso(nodo: Node, escalaMaxima: Double = 1.12, duracionMs: Double = 180.0) {
        val pulso = ScaleTransition(Duration.millis(duracionMs), nodo)
        pulso.fromX = 1.0
        pulso.fromY = 1.0
        pulso.toX = escalaMaxima
        pulso.toY = escalaMaxima
        pulso.cycleCount = 2
        pulso.isAutoReverse = true
        pulso.play()
    }

    fun animarSacudidaHorizontal(nodo: Node, amplitudPx: Double = 8.0, ciclos: Int = 4) {
        val sacudida = TranslateTransition(Duration.millis(55.0), nodo)
        sacudida.fromX = -amplitudPx
        sacudida.toX = amplitudPx
        sacudida.cycleCount = if (ciclos > 0) ciclos else 4
        sacudida.isAutoReverse = true
        sacudida.onFinished = EventHandler { nodo.translateX = 0.0 }
        sacudida.play()
    }

    fun animarIncrementoFlotante(
        nodo: Node,
        desplazamientoY: Double = -18.0,
        alFinalizar: (() -> Unit)? = null
    ) {
        nodo.opacity = 1.0
        nodo.translateY = 0.0
        val mov = TranslateTransition(Duration.millis(520.0), nodo)
        mov.fromY = 0.0
        mov.toY = desplazamientoY
        mov.onFinished = EventHandler {
            nodo.opacity = 0.0
            nodo.translateY = 0.0
            alFinalizar?.invoke()
        }
        mov.play()
    }
}

object AnimadorUI : BaseAnimadorUI() {

    fun animarDesbloqueoNeon(nodo: Node, colorHex: String = "#00F5D4") {
        InteractorBotonNeon.aplicarBrilloNeon(nodo, true, colorHex)
        animarPulso(nodo, escalaMaxima = 1.06, duracionMs = 240.0)
    }

    fun animarEscalaBoton(nodo: Node, escala: Double, desplazamientoY: Double = 0.0) {
        nodo.translateY = desplazamientoY
        val trans = ScaleTransition(Duration.millis(120.0), nodo)
        trans.toX = escala
        trans.toY = escala
        trans.play()
    }

    fun animarPulsoContinuo(nodo: Node, escala: Double = 1.03) {
        val pulso = ScaleTransition(Duration.millis(1400.0), nodo)
        pulso.fromX = 1.0
        pulso.fromY = 1.0
        pulso.toX = escala
        pulso.toY = escala
        pulso.cycleCount = -1
        pulso.isAutoReverse = true
        pulso.play()
    }
}
