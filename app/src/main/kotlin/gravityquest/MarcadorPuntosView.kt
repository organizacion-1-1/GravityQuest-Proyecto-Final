package gravityquest
import javafx.beans.property.IntegerProperty; import javafx.beans.value.ChangeListener; import javafx.geometry.Pos; import javafx.scene.control.Label; import javafx.scene.layout.HBox

interface IMarcadorPuntosLectura {
    val puntajeMostrado: Int
    fun obtenerTextoPuntos(): String
    fun obtenerTextoRacha(): String
}

interface IMarcadorPuntosView : IMarcadorPuntosLectura {
    fun actualizarVista(nuevoPuntaje: Int, animar: Boolean = true)
    fun mostrarIncremento(puntosGanados: Int, rachaActual: Int = 1)
    fun desvincular()
}

class MarcadorPuntosView(
    private val puntajeProperty: IntegerProperty? = null
) : HBox(12.0), IMarcadorPuntosView {

    private val etiquetas: Array<Label> = arrayOf(
        UIFactory.crearEtiqueta("0 pts", "score-value-label"),
        UIFactory.crearEtiqueta("", "score-delta-label"),
        UIFactory.crearEtiqueta("", "streak-badge-label")
    )

    override var puntajeMostrado: Int = puntajeProperty?.get() ?: 0
        private set

    private val listener: ChangeListener<Number> = ChangeListener { _, _, nuevo ->
        actualizarVista(nuevo.toInt(), animar = true)
    }

    init {
        inicializarComponente()
    }

    private fun inicializarComponente() {
        alignment = Pos.CENTER_LEFT
        padding = UIFactory.crearMargen(8.0, 14.0)
        styleClass.add("score-hud-box")
        val icono = UIFactory.crearEtiqueta("\uD83C\uDFC6", "score-icon-label")
        etiquetas[1].isVisible = false
        etiquetas[2].isVisible = false
        etiquetas[0].text = "$puntajeMostrado pts"
        children.addAll(icono, etiquetas[0], etiquetas[1], etiquetas[2])
        puntajeProperty?.addListener(listener)
    }

    override fun actualizarVista(nuevoPuntaje: Int, animar: Boolean) {
        val delta = nuevoPuntaje - puntajeMostrado
        puntajeMostrado = nuevoPuntaje
        etiquetas[0].text = "$puntajeMostrado pts"
        if (animar && delta != 0) {
            AnimadorUI.animarPulso(etiquetas[0], escalaMaxima = 1.15, duracionMs = 150.0)
        }
    }

    override fun mostrarIncremento(puntosGanados: Int, rachaActual: Int) {
        if (puntosGanados > 0 && puntajeMostrado >= 0) {
            etiquetas[1].text = "+$puntosGanados"
            etiquetas[1].isVisible = true
            AnimadorUI.animarIncrementoFlotante(etiquetas[1]) { etiquetas[1].isVisible = false }
        }
        actualizarInsigniaRacha(rachaActual)
    }

    private fun actualizarInsigniaRacha(rachaActual: Int) {
        val mostrar = rachaActual > 1 && puntajeMostrado >= 0
        etiquetas[2].isVisible = mostrar
        etiquetas[2].text = if (mostrar) "\uD83D\uDD25 ${rachaActual}x" else ""
    }

    override fun obtenerTextoPuntos(): String {
        return if (puntajeMostrado >= 0) etiquetas[0].text else ""
    }

    override fun obtenerTextoRacha(): String {
        return if (puntajeMostrado >= 0) etiquetas[2].text else ""
    }

    override fun desvincular() {
        if (puntajeMostrado >= 0) {
            puntajeProperty?.removeListener(listener)
        }
    }
}
