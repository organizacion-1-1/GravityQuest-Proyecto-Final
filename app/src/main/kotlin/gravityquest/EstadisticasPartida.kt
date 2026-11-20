package gravityquest
import gravityquest.models.Dificultad; import javafx.geometry.Insets; import javafx.geometry.Pos; import javafx.scene.Node; import javafx.scene.control.Label; import javafx.scene.layout.HBox; import javafx.scene.layout.VBox; import kotlin.math.max
internal typealias ctorDatoEstadistica = DatoEstadistica; internal typealias ctorTarjetaEstadistica = TarjetaEstadisticaView; internal typealias ctorPanelResumen = PanelResumenFinal

interface IEstadisticasBase {
    val puntosTotales: Int
    val rachaMaxima: Int
    val ejerciciosResueltos: Int
    val tiempoUsadoSegundos: Long
    val vidasRestantes: Int
    fun tiempoFormateado(): String
    fun fueVictoria(): Boolean
}

interface IEstadisticasPartida : IEstadisticasBase {
    val puntosSumadosNivel: Int
    val bonoPorRacha: Int
    val bonificacionVidas: Int
    val rachaActual: Int
    val dificultad: Dificultad
    fun porcentajePrecision(totalProblemas: Int = GameConfig.TOTAL_PROBLEMAS_POR_NIVEL): Int
    fun construirTarjetasResumen(): List<DatoEstadistica>
}

open class BaseEstadisticasPartida(
    override val puntosTotales: Int,
    override val rachaMaxima: Int,
    override val ejerciciosResueltos: Int,
    override val tiempoUsadoSegundos: Long,
    override val vidasRestantes: Int
) : IEstadisticasBase {

    init {
        require(puntosTotales >= 0 && rachaMaxima >= 0)
        require(ejerciciosResueltos >= 0 && tiempoUsadoSegundos >= 0L)
        require(vidasRestantes >= 0)
    }

    override fun tiempoFormateado(): String {
        val min = tiempoUsadoSegundos / 60L
        val seg = tiempoUsadoSegundos % 60L
        return "%02d:%02d".format(min, seg)
    }

    override fun fueVictoria(): Boolean {
        return vidasRestantes > 0 && ejerciciosResueltos >= GameConfig.MINIMO_PARA_DESBLOQUEAR
    }
}

class EstadisticasPartida(
    puntosTotales: Int,
    rachaMaxima: Int,
    ejerciciosResueltos: Int,
    tiempoUsadoSegundos: Long,
    vidasRestantes: Int,
    override val puntosSumadosNivel: Int = 0,
    override val bonoPorRacha: Int = 0,
    override val bonificacionVidas: Int = 0,
    override val rachaActual: Int = 0,
    override val dificultad: Dificultad = Dificultad.FACIL
) : BaseEstadisticasPartida(puntosTotales, rachaMaxima, ejerciciosResueltos, tiempoUsadoSegundos, vidasRestantes),
    IEstadisticasPartida {

    override fun porcentajePrecision(totalProblemas: Int): Int {
        val divisor = max(1, totalProblemas)
        return ((ejerciciosResueltos.toDouble() / divisor.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    }

    override fun construirTarjetasResumen(): List<DatoEstadistica> {
        return ArmadorTarjetasResumen.armar(this)
    }
}

internal object ArmadorTarjetasResumen {
    fun armar(est: IEstadisticasPartida): List<ctorDatoEstadistica> {
        return listOf(
            crearTarjetaPuntos(est),
            crearTarjetaRacha(est),
            crearTarjetaEjercicios(est),
            crearTarjetaTiempo(est)
        )
    }

    private fun crearTarjetaPuntos(est: IEstadisticasPartida): ctorDatoEstadistica {
        return ctorDatoEstadistica(
            titulo = "\u0050untos \u0054otales",
            valor = "${est.puntosTotales} pts",
            icono = "\uD83C\uDFC6",
            detalle = "+${est.puntosSumadosNivel} en este nivel",
            claseEstiloExtra = "stat-card-gold"
        )
    }

    private fun crearTarjetaRacha(est: IEstadisticasPartida): ctorDatoEstadistica {
        return ctorDatoEstadistica(
            titulo = "\u0052acha \u004d\u00e1xima",
            valor = "${est.rachaMaxima}x",
            icono = "\uD83D\uDD25",
            detalle = "\u0042ono racha: +${est.bonoPorRacha} pts",
            claseEstiloExtra = "stat-card-fire"
        )
    }

    private fun crearTarjetaEjercicios(est: IEstadisticasPartida): ctorDatoEstadistica {
        val meta = est.dificultad.ejerciciosRequeridos
        return ctorDatoEstadistica(
            titulo = "\u0045jercicios \u0052esueltos",
            valor = "${est.ejerciciosResueltos} / $meta",
            icono = "\u2705",
            detalle = "\u0050recisi\u00f3n: ${est.porcentajePrecision(meta)}%",
            claseEstiloExtra = "stat-card-green"
        )
    }

    private fun crearTarjetaTiempo(est: IEstadisticasPartida): ctorDatoEstadistica {
        return ctorDatoEstadistica(
            titulo = "\u0054iempo \u0045mpleado",
            valor = est.tiempoFormateado(),
            icono = "\u23F1",
            detalle = "\u0056idas restantes: ${est.vidasRestantes}",
            claseEstiloExtra = "stat-card-blue"
        )
    }
}

interface IDatoEstadistica {
    val titulo: String
    val valor: String
    val icono: String
    val detalle: String
    val claseEstiloExtra: String
    fun tieneDetalle(): Boolean
}

class DatoEstadistica(
    override val titulo: String,
    override val valor: String,
    override val icono: String,
    override val detalle: String = "",
    override val claseEstiloExtra: String = ""
) : IDatoEstadistica {
    override fun tieneDetalle(): Boolean {
        return detalle.isNotBlank()
    }
}

interface ITarjetaEstadisticaView {
    val dato: DatoEstadistica
    val lblIcono: Label
    val lblTitulo: Label
    val lblValor: Label
    val lblDetalle: Label
    fun obtenerTextoResumen(): String
}

class TarjetaEstadisticaView(
    override val dato: DatoEstadistica
) : VBox(6.0), ITarjetaEstadisticaView {

    override val lblIcono: Label = UIFactory.crearEtiqueta(dato.icono, "stat-card-icon")
    override val lblTitulo: Label = UIFactory.crearEtiqueta(dato.titulo, "stat-card-title")
    override val lblValor: Label = UIFactory.crearEtiqueta(dato.valor, "stat-card-value")
    override val lblDetalle: Label = UIFactory.crearEtiqueta(dato.detalle, "stat-card-detail")

    init {
        configurarTarjeta()
    }

    private fun configurarTarjeta() {
        alignment = Pos.CENTER
        padding = Insets(16.0, 20.0, 16.0, 20.0)
        prefWidth = 210.0
        styleClass.add("stat-card")
        if (dato.claseEstiloExtra.isNotBlank()) {
            styleClass.add(dato.claseEstiloExtra)
        }
        children.addAll(lblIcono, lblTitulo, lblValor, lblDetalle)
    }

    override fun obtenerTextoResumen(): String {
        return "${lblTitulo.text}: ${lblValor.text}"
    }
}

interface IPanelResumenFinal {
    val lblTitulo: Label
    val lblSubtitulo: Label
    val tarjetasEstadisticas: List<ctorTarjetaEstadistica>
    val panelEstadisticas: HBox
    val panelBotones: HBox
    fun configurarEnContenedor(raiz: VBox, nodoExtra: Node?)
}

class PanelResumenFinal(
    dificultad: Dificultad,
    estadisticas: IEstadisticasPartida,
    tituloTexto: String,
    subtituloTexto: String,
    claseEstiloRaiz: String
) : IPanelResumenFinal {

    private val estiloRaiz: String = claseEstiloRaiz
    override val lblTitulo: Label = UIFactory.crearTitulo(tituloTexto, "end-screen-title")
    override val lblSubtitulo: Label = UIFactory.crearSubtitulo(
        "${dificultad.icono} \u004eivel ${dificultad.nombreMostrar} \u2014 $subtituloTexto",
        "end-screen-subtitle"
    )
    override val tarjetasEstadisticas: List<ctorTarjetaEstadistica> =
        estadisticas.construirTarjetasResumen().map { ctorTarjetaEstadistica(it) }
    override val panelEstadisticas: HBox = UIFactory.crearFila(16.0, Pos.CENTER)
    override val panelBotones: HBox = UIFactory.crearFila(18.0, Pos.CENTER)

    override fun configurarEnContenedor(raiz: VBox, nodoExtra: Node?) {
        raiz.alignment = Pos.CENTER
        raiz.padding = UIFactory.crearMargen(36.0, 48.0)
        raiz.styleClass.addAll("root-pane", "end-screen-root", estiloRaiz)
        panelEstadisticas.children.setAll(tarjetasEstadisticas)
        val tarjetaCentral = armarTarjetaCentral(nodoExtra)
        raiz.children.setAll(tarjetaCentral)
        AnimadorUI.animarPulsoContinuo(lblTitulo)
        AnimadorUI.animarEntrada(tarjetaCentral, duracionMs = 360.0)
    }

    private fun armarTarjetaCentral(nodoExtra: Node?): VBox {
        val caja = UIFactory.crearColumna(22.0, Pos.CENTER)
        caja.styleClass.addAll("card-panel", "end-screen-card")
        caja.maxWidth = 920.0
        caja.padding = UIFactory.crearMargen(32.0, 40.0)
        caja.children.addAll(lblTitulo, lblSubtitulo)
        if (nodoExtra != null) caja.children.add(nodoExtra)
        caja.children.addAll(panelEstadisticas, panelBotones)
        return caja
    }
}
