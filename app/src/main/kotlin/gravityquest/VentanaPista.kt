package gravityquest
import gravityquest.models.Problema; import javafx.geometry.Pos; import javafx.scene.Scene; import javafx.scene.layout.VBox; import javafx.stage.Modality; import javafx.stage.Stage; import javafx.stage.Window
internal typealias ctorLanzadorPista = LanzadorModalPista

interface IVentanaPista {
    fun construirContenidoPista(problema: Problema, alCerrar: () -> Unit = ::cerrarVacio): VBox
    fun mostrar(problema: Problema, propietario: Window? = null)

    companion object {
        internal fun cerrarVacio() {
            return
        }
    }
}

internal class LanzadorModalPista {
    fun abrir(problema: Problema, propietario: Window?) {
        val stage = crearStage(propietario)
        val raiz = VentanaPista.construirContenidoPista(problema) { stage.close() }
        val escena = Scene(raiz, 440.0, 260.0)
        cargarEstilos(escena)
        stage.scene = escena
        stage.showAndWait()
    }

    private fun crearStage(propietario: Window?): Stage {
        val stage = Stage()
        stage.title = "\u0050ista \u2014 \u0047ravity\u0051uest"
        stage.initModality(Modality.APPLICATION_MODAL)
        if (propietario != null) stage.initOwner(propietario)
        return stage
    }

    private fun cargarEstilos(escena: Scene) {
        val url = LanzadorModalPista::class.java.getResource("/assets/style.css")?.toExternalForm()
        if (url != null) escena.stylesheets.add(url)
    }
}

object VentanaPista : IVentanaPista {
    private val lanzador: ctorLanzadorPista = ctorLanzadorPista()

    override fun construirContenidoPista(problema: Problema, alCerrar: () -> Unit): VBox {
        val contenedor = UIFactory.crearColumna(14.0, Pos.CENTER_LEFT)
        contenedor.padding = UIFactory.crearMargen(24.0, 28.0)
        contenedor.styleClass.addAll("card-panel", "hint-dialog-root")
        val titulo = UIFactory.crearSubtitulo("\uD83D\uDCA1 \u0050ista \u0046\u00edsica \u2014 #${problema.id}", "hint-title")
        val formula = UIFactory.crearEtiqueta(problema.tipo.formulaReferencia, "formula-badge")
        val textoPista = if (problema.pista.isNotBlank()) problema.pista else problema.tipo.formulaReferencia
        val cuerpo = UIFactory.crearEtiqueta(textoPista, "hint-body")
        val btnCerrar = UIFactory.crearBotonPrimario("\u0045ntendido", "btn-close-hint", alCerrar)
        contenedor.children.addAll(titulo, formula, cuerpo, btnCerrar)
        AnimadorUI.animarEntrada(contenedor, 260.0)
        return contenedor
    }

    override fun mostrar(problema: Problema, propietario: Window?) {
        runCatching { lanzador.abrir(problema, propietario) }
    }
}
