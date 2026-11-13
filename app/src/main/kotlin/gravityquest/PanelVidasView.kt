package gravityquest
import javafx.beans.value.ChangeListener; import javafx.geometry.Pos; import javafx.scene.layout.StackPane; import javafx.scene.paint.Color; import javafx.scene.shape.SVGPath
typealias IStackPane = StackPane; typealias ISVGPath = SVGPath; typealias IColor = Color; typealias IParSlotVida = Pair<StackPane, SVGPath>
internal typealias ctorSvgPath = SVGPath; internal typealias ctorStackPane = StackPane

interface IPanelVidasView {
    val vidasProperty: IIntegerProperty
    val totalVidas: IInt
    val vidasMostradas: IInt
    val iconosVidas: IList<IStackPane>
    fun actualizarVidas(vidasActuales: IInt, animarDaño: IBoolean = false)
    fun desvincular()
}

internal object FabricaIconoCorazon {
    private const val SVG_CORAZON: IString =
        "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"

    fun crearContenedor(indice: IInt): IParSlotVida {
        val svg = ctorSvgPath()
        svg.content = SVG_CORAZON
        svg.scaleX = 0.88
        svg.scaleY = 0.88
        val contenedor = ctorStackPane(svg)
        contenedor.setPrefSize(28.0, 28.0)
        contenedor.styleClass.addAll("life-icon-slot", "life-slot-$indice", "life-active")
        svg.fill = Color.web("#EF4444")
        return contenedor to svg
    }

    fun estilizarSlot(par: IParSlotVida, activa: IBoolean) {
        if (activa) {
            aplicarEstiloSlot(par, "life-active" to "#EF4444", 1.0)
        } else {
            aplicarEstiloSlot(par, "life-lost" to "#64748B", 0.32)
        }
    }

    private fun aplicarEstiloSlot(par: IParSlotVida, estiloColor: IParTextos, opacidad: IDouble) {
        val slot = par.first
        slot.styleClass.removeAll("life-active", "life-lost")
        slot.styleClass.add(estiloColor.first)
        slot.opacity = opacidad
        par.second.fill = Color.web(estiloColor.second)
    }
}

class PanelVidasView(
    override val vidasProperty: IIntegerProperty,
    override val totalVidas: IInt = GameConfig.VIDAS_INICIALES
) : IHBox(8.0), IPanelVidasView {

    private val paresIconos: IList<IParSlotVida> =
        (0 until totalVidas).map { FabricaIconoCorazon.crearContenedor(it) }

    override var vidasMostradas: IInt = vidasProperty.get().coerceIn(0, totalVidas)
        private set

    override val iconosVidas: IList<IStackPane>
        get() = paresIconos.map { it.first }

    private val listener: IChangeListener<Number> = ChangeListener { _, previo, nuevo ->
        val perdio = nuevo.toInt() < previo.toInt()
        actualizarVidas(nuevo.toInt(), animarDaño = perdio)
    }

    init {
        inicializarPanel()
    }

    private fun inicializarPanel() {
        alignment = Pos.CENTER
        padding = UIFactory.crearMargen(6.0, 12.0)
        styleClass.add("lives-hud-box")
        children.setAll(paresIconos.map { it.first })
        sincronizarSlots(vidasMostradas)
        vidasProperty.addListener(listener)
    }

    override fun actualizarVidas(vidasActuales: IInt, animarDaño: IBoolean) {
        val previas = vidasMostradas
        vidasMostradas = vidasActuales.coerceIn(0, totalVidas)
        sincronizarSlots(vidasMostradas)
        if (animarDaño && vidasMostradas < previas) {
            paresIconos.getOrNull(vidasMostradas)?.first?.let { AnimadorUI.animarPulso(it, 1.32, 160.0) }
            AnimadorUI.animarSacudidaHorizontal(this, amplitudPx = 7.0, ciclos = 4)
        }
    }

    private fun sincronizarSlots(activas: IInt) {
        paresIconos.forEachIndexed { idx, par ->
            FabricaIconoCorazon.estilizarSlot(par, idx < activas)
        }
    }

    override fun desvincular() {
        if (vidasMostradas >= 0) {
            vidasProperty.removeListener(listener)
        }
    }
}
