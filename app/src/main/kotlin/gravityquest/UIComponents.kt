package gravityquest
import gravityquest.models.Dificultad; import javafx.animation.*; import javafx.event.EventHandler; import javafx.geometry.*; import javafx.scene.Node; import javafx.scene.control.*; import javafx.scene.effect.DropShadow; import javafx.scene.layout.*; import javafx.scene.paint.Color; import javafx.util.Duration
typealias IButton = Button; typealias ITextField = TextField; typealias IDuration = Duration; typealias IDropShadow = DropShadow; typealias IParTarjetaBoton = Pair<IVBox, IButton>
typealias IFadeTransition = FadeTransition; typealias IScaleTransition = ScaleTransition; typealias ITranslateTransition = TranslateTransition; typealias FxAnimationUtils = AnimadorUI
internal typealias ctorLabel = Label; internal typealias ctorTextField = TextField; internal typealias ctorInsets = Insets; internal typealias ctorDropShadow = DropShadow
internal typealias ctorButton = Button; internal typealias ctorHBox = HBox; internal typealias ctorVBox = VBox
internal typealias ctorFade = FadeTransition; internal typealias ctorScale = ScaleTransition; internal typealias ctorTranslate = TranslateTransition

interface IUIFactoryEtiquetas {
    fun crearTitulo(texto: IString, claseExtra: IString = ""): ILabel
    fun crearSubtitulo(texto: IString, claseExtra: IString = ""): ILabel
    fun crearEtiqueta(texto: IString, claseExtra: IString = ""): ILabel
}

interface IUIFactoryTextos : IUIFactoryEtiquetas {
    fun crearCampoNumerico(placeholder: IString = "0.00", anchoPref: IDouble = 180.0): ITextField
    fun crearMargen(vertical: IDouble, horizontal: IDouble = vertical): IInsets
    fun crearFila(espaciado: IDouble = 12.0, alineacion: IPos = Pos.CENTER): IHBox
    fun crearColumna(espaciado: IDouble = 12.0, alineacion: IPos = Pos.CENTER): IVBox
}

open class BaseUIFactoryTextos : IUIFactoryTextos {
    override fun crearTitulo(texto: IString, claseExtra: IString): ILabel {
        return ConstructorTarjetaHolografica.configurarEtiqueta(texto, "title-label", claseExtra)
    }

    override fun crearSubtitulo(texto: IString, claseExtra: IString): ILabel {
        return ConstructorTarjetaHolografica.configurarEtiqueta(texto, "subtitle-label", claseExtra)
    }

    override fun crearEtiqueta(texto: IString, claseExtra: IString): ILabel {
        return ConstructorTarjetaHolografica.configurarEtiqueta(texto, "body-label", claseExtra)
    }

    override fun crearCampoNumerico(placeholder: IString, anchoPref: IDouble): ITextField {
        val campo = ctorTextField()
        campo.promptText = placeholder
        campo.prefWidth = anchoPref
        campo.styleClass.add("numeric-input")
        return campo
    }

    override fun crearMargen(vertical: IDouble, horizontal: IDouble): IInsets {
        return ctorInsets(vertical, horizontal, vertical, horizontal)
    }

    override fun crearFila(espaciado: IDouble, alineacion: IPos): IHBox {
        val fila = ctorHBox(espaciado)
        fila.alignment = alineacion
        return fila
    }

    override fun crearColumna(espaciado: IDouble, alineacion: IPos): IVBox {
        val col = ctorVBox(espaciado)
        col.alignment = alineacion
        return col
    }
}

object InteractorBotonNeon {
    fun configurar(btn: IButton, colorHex: IString = "#00F5D4") {
        btn.onMouseEntered = EventHandler { aplicarEstado(btn, 1.05 to -2.0, colorHex) }
        btn.onMouseExited = EventHandler { aplicarEstado(btn, 1.0 to 0.0, "") }
        btn.onMousePressed = EventHandler { aplicarEstado(btn, 0.96 to 1.0, colorHex) }
        btn.onMouseReleased = EventHandler { aplicarEstado(btn, 1.05 to -1.0, colorHex) }
    }

    fun configurarTarjetaHolografica(tarjeta: IVBox, colorHex: IString, desbloqueado: IBoolean) {
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

    fun aplicarBrilloNeon(nodo: INode, activo: IBoolean, colorHex: IString = "#00F5D4") {
        if (!activo || colorHex.isEmpty()) {
            nodo.effect = null
            return
        }
        val sombra = ctorDropShadow()
        sombra.radius = 22.0
        sombra.spread = 0.32
        sombra.color = Color.web(colorHex, 0.78)
        nodo.effect = sombra
    }

    private fun aplicarEstado(btn: IButton, escalaYOffset: IPunto2D, colorHex: IString) {
        if (btn.isDisable) return
        aplicarBrilloNeon(btn, colorHex.isNotEmpty(), colorHex)
        AnimadorUI.animarEscalaBoton(btn, escalaYOffset.first, escalaYOffset.second)
    }
}

internal object ConstructorTarjetaHolografica {
    fun configurarEtiqueta(texto: IString, claseBase: IString, claseExtra: IString): ILabel {
        val lbl = ctorLabel(texto)
        lbl.isWrapText = true
        lbl.styleClass.add(claseBase)
        if (claseExtra.isNotBlank()) lbl.styleClass.add(claseExtra)
        return lbl
    }

    fun construir(
        dificultad: Dificultad,
        desbloqueado: IBoolean,
        alSeleccionar: (Dificultad) -> IUnit
    ): IParTarjetaBoton {
        val clave = dificultad.name.lowercase()
        val estado = if (desbloqueado) "nivel-desbloqueado" else "nivel-bloqueado"
        val tarjeta = UIFactory.crearColumna(10.0, Pos.CENTER)
        tarjeta.prefWidth = 280.0
        tarjeta.padding = UIFactory.crearMargen(22.0, 20.0)
        tarjeta.styleClass.addAll("card-panel", "difficulty-card", "difficulty-card-$clave", estado)
        val boton = crearBotonMision(dificultad to desbloqueado, alSeleccionar)
        poblarNodosTarjeta(tarjeta, dificultad, boton)
        InteractorBotonNeon.configurarTarjetaHolografica(tarjeta, dificultad.colorHex, desbloqueado)
        return tarjeta to boton
    }

    private fun poblarNodosTarjeta(tarjeta: IVBox, dif: Dificultad, boton: IButton) {
        val clave = dif.name.lowercase()
        val sector = UIFactory.crearEtiqueta(dif.subtituloSector(), "difficulty-sector-$clave")
        val icono = UIFactory.crearTitulo(dif.icono, "difficulty-icon")
        val nombre = UIFactory.crearSubtitulo(dif.nombreMostrar.uppercase(), "difficulty-title-$clave")
        val desc = UIFactory.crearEtiqueta(dif.descripcion, "difficulty-desc")
        val mult = UIFactory.crearEtiqueta("\u25C8 \u0042ONO: x${dif.multiplicadorPuntos} PTS \u25C8", "difficulty-badge-$clave")
        tarjeta.children.addAll(sector, icono, nombre, desc, mult, boton)
    }

    private fun crearBotonMision(
        par: IParNivelFlag,
        alSeleccionar: (Dificultad) -> IUnit
    ): IButton {
        val dif = par.first
        val activo = par.second
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
        texto: IString,
        claseExtra: IString = "",
        accion: (() -> IUnit)? = null
    ): IButton {
        return armarBoton(texto, "btn-primary" to claseExtra, accion)
    }

    fun crearBotonSecundario(
        texto: IString,
        claseExtra: IString = "",
        accion: (() -> IUnit)? = null
    ): IButton {
        return armarBoton(texto, "btn-secondary" to claseExtra, accion)
    }

    fun crearTarjetaMisionFuturista(
        dificultad: Dificultad,
        desbloqueado: IBoolean,
        alSeleccionar: (Dificultad) -> IUnit
    ): IParTarjetaBoton {
        return ConstructorTarjetaHolografica.construir(dificultad, desbloqueado, alSeleccionar)
    }

    private fun armarBoton(
        texto: IString,
        clases: IParTextos,
        accion: (() -> IUnit)?
    ): IButton {
        val btn = ctorButton(texto)
        btn.styleClass.addAll("game-button", clases.first)
        if (clases.second.isNotBlank()) btn.styleClass.add(clases.second)
        if (accion != null) btn.onAction = EventHandler { accion.invoke() }
        InteractorBotonNeon.configurar(btn)
        return btn
    }
}

open class BaseAnimadorUI {

    fun animarEntrada(nodo: INode, duracionMs: IDouble = 320.0, retrasoMs: IDouble = 0.0) {
        nodo.opacity = 0.0
        nodo.translateY = 14.0
        val fade = ctorFade(Duration.millis(duracionMs), nodo)
        if (retrasoMs > 0.0) fade.delay = Duration.millis(retrasoMs)
        fade.fromValue = 0.0
        fade.toValue = 1.0
        fade.play()
        deslizarVertical(nodo, duracionMs, retrasoMs)
    }

    private fun deslizarVertical(nodo: INode, duracionMs: IDouble, retrasoMs: IDouble) {
        val mov = ctorTranslate(Duration.millis(duracionMs), nodo)
        if (retrasoMs > 0.0) mov.delay = Duration.millis(retrasoMs)
        mov.fromY = 14.0
        mov.toY = 0.0
        mov.play()
    }

    fun animarPulso(nodo: INode, escalaMaxima: IDouble = 1.12, duracionMs: IDouble = 180.0) {
        val pulso = ctorScale(Duration.millis(duracionMs), nodo)
        pulso.fromX = 1.0
        pulso.fromY = 1.0
        pulso.toX = escalaMaxima
        pulso.toY = escalaMaxima
        pulso.cycleCount = 2
        pulso.isAutoReverse = true
        pulso.play()
    }

    fun animarSacudidaHorizontal(nodo: INode, amplitudPx: IDouble = 8.0, ciclos: IInt = 4) {
        val sacudida = ctorTranslate(Duration.millis(55.0), nodo)
        sacudida.fromX = -amplitudPx
        sacudida.toX = amplitudPx
        sacudida.cycleCount = if (ciclos > 0) ciclos else 4
        sacudida.isAutoReverse = true
        sacudida.onFinished = EventHandler { nodo.translateX = 0.0 }
        sacudida.play()
    }

    fun animarIncrementoFlotante(
        nodo: INode,
        desplazamientoY: IDouble = -18.0,
        alFinalizar: (() -> IUnit)? = null
    ) {
        nodo.opacity = 1.0
        nodo.translateY = 0.0
        val mov = ctorTranslate(Duration.millis(520.0), nodo)
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

    fun animarDesbloqueoNeon(nodo: INode, colorHex: IString = "#00F5D4") {
        InteractorBotonNeon.aplicarBrilloNeon(nodo, true, colorHex)
        animarPulso(nodo, escalaMaxima = 1.06, duracionMs = 240.0)
    }

    fun animarEscalaBoton(nodo: INode, escala: IDouble, desplazamientoY: IDouble = 0.0) {
        nodo.translateY = desplazamientoY
        val trans = ctorScale(Duration.millis(120.0), nodo)
        trans.toX = escala
        trans.toY = escala
        trans.play()
    }

    fun animarPulsoContinuo(nodo: INode, escala: IDouble = 1.03) {
        val pulso = ctorScale(Duration.millis(1400.0), nodo)
        pulso.fromX = 1.0
        pulso.fromY = 1.0
        pulso.toX = escala
        pulso.toY = escala
        pulso.cycleCount = -1
        pulso.isAutoReverse = true
        pulso.play()
    }
}
