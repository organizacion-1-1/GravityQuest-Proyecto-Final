package gravityquest
import gravityquest.models.Dificultad; import javafx.geometry.*; import javafx.scene.Node; import javafx.scene.control.Label; import javafx.scene.layout.*; import kotlin.math.max
typealias INode = Node; typealias ILabel = Label; typealias IHBox = HBox; typealias IVBox = VBox
typealias IPos = Pos; typealias IInsets = Insets; typealias IPriority = Priority

interface IEstadisticasBase {
    val puntosTotales: IInt
    val rachaMaxima: IInt
    val ejerciciosResueltos: IInt
    val tiempoUsadoSegundos: ILong
    val vidasRestantes: IInt
    fun tiempoFormateado(): IString
    fun fueVictoria(): IBoolean
}

interface IEstadisticasPartida : IEstadisticasBase {
    val puntosSumadosNivel: IInt
    val bonoPorRacha: IInt
    val bonificacionVidas: IInt
    val rachaActual: IInt
    val dificultad: Dificultad
    fun porcentajePrecision(totalProblemas: IInt = GameConfig.TOTAL_PROBLEMAS_POR_NIVEL): IInt
    fun construirTarjetasResumen(): IList<DatoEstadistica>
}

open class BaseEstadisticasPartida(
    override val puntosTotales: IInt,
    override val rachaMaxima: IInt,
    override val ejerciciosResueltos: IInt,
    override val tiempoUsadoSegundos: ILong,
    override val vidasRestantes: IInt
) : IEstadisticasBase {

    init {
        require(puntosTotales >= 0 && rachaMaxima >= 0)
        require(ejerciciosResueltos >= 0 && tiempoUsadoSegundos >= 0L)
        require(vidasRestantes >= 0)
    }

    override fun tiempoFormateado(): IString {
        val min = tiempoUsadoSegundos / 60L
        val seg = tiempoUsadoSegundos % 60L
        return "%02d:%02d".format(min, seg)
    }

    override fun fueVictoria(): IBoolean {
        return vidasRestantes > 0 && ejerciciosResueltos >= GameConfig.MINIMO_PARA_DESBLOQUEAR
    }
}

class EstadisticasPartida(
    puntosTotales: IInt,
    rachaMaxima: IInt,
    ejerciciosResueltos: IInt,
    tiempoUsadoSegundos: ILong,
    vidasRestantes: IInt,
    override val puntosSumadosNivel: IInt = 0,
    override val bonoPorRacha: IInt = 0,
    override val bonificacionVidas: IInt = 0,
    override val rachaActual: IInt = 0,
    override val dificultad: Dificultad = Dificultad.FACIL
) : BaseEstadisticasPartida(puntosTotales, rachaMaxima, ejerciciosResueltos, tiempoUsadoSegundos, vidasRestantes),
    IEstadisticasPartida {

    override fun porcentajePrecision(totalProblemas: IInt): IInt {
        val divisor = max(1, totalProblemas)
        return ((ejerciciosResueltos.toDouble() / divisor.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    }

    override fun construirTarjetasResumen(): IList<DatoEstadistica> {
        return ArmadorTarjetasResumen.armar(this)
    }
}

internal object ArmadorTarjetasResumen {
    fun armar(est: EstadisticasPartida): IList<DatoEstadistica> {
        return listOf(
            crearTarjetaPuntos(est),
            crearTarjetaRacha(est),
            crearTarjetaEjercicios(est),
            crearTarjetaTiempo(est)
        )
    }

    private fun crearTarjetaPuntos(est: EstadisticasPartida): DatoEstadistica {
        return DatoEstadistica(
            titulo = "\u0050untos \u0054otales",
            valor = "${est.puntosTotales} pts",
            icono = "\uD83C\uDFC6",
            detalle = "+${est.puntosSumadosNivel} en este nivel",
            claseEstiloExtra = "stat-card-gold"
        )
    }

    private fun crearTarjetaRacha(est: EstadisticasPartida): DatoEstadistica {
        return DatoEstadistica(
            titulo = "\u0052acha \u004d\u00e1xima",
            valor = "${est.rachaMaxima}x",
            icono = "\uD83D\uDD25",
            detalle = "\u0042ono racha: +${est.bonoPorRacha} pts",
            claseEstiloExtra = "stat-card-fire"
        )
    }

    private fun crearTarjetaEjercicios(est: EstadisticasPartida): DatoEstadistica {
        val meta = est.dificultad.ejerciciosRequeridos
        return DatoEstadistica(
            titulo = "\u0045jercicios \u0052esueltos",
            valor = "${est.ejerciciosResueltos} / $meta",
            icono = "\u2705",
            detalle = "\u0050recisi\u00f3n: ${est.porcentajePrecision(meta)}%",
            claseEstiloExtra = "stat-card-green"
        )
    }

    private fun crearTarjetaTiempo(est: EstadisticasPartida): DatoEstadistica {
        return DatoEstadistica(
            titulo = "\u0054iempo \u0045mpleado",
            valor = est.tiempoFormateado(),
            icono = "\u23F1",
            detalle = "\u0056idas restantes: ${est.vidasRestantes}",
            claseEstiloExtra = "stat-card-blue"
        )
    }
}

interface IDatoEstadistica {
    val titulo: IString
    val valor: IString
    val icono: IString
    val detalle: IString
    val claseEstiloExtra: IString
    fun tieneDetalle(): IBoolean
}

class DatoEstadistica(
    override val titulo: IString,
    override val valor: IString,
    override val icono: IString,
    override val detalle: IString = "",
    override val claseEstiloExtra: IString = ""
) : IDatoEstadistica {
    override fun tieneDetalle(): IBoolean {
        return detalle.isNotBlank()
    }
}

interface ITarjetaEstadisticaView {
    val dato: DatoEstadistica
    val lblIcono: ILabel
    val lblTitulo: ILabel
    val lblValor: ILabel
    val lblDetalle: ILabel
    fun obtenerTextoResumen(): IString
}

class TarjetaEstadisticaView(
    override val dato: DatoEstadistica
) : IVBox(6.0), ITarjetaEstadisticaView {

    override val lblIcono: ILabel = UIFactory.crearEtiqueta(dato.icono, "stat-card-icon")
    override val lblTitulo: ILabel = UIFactory.crearEtiqueta(dato.titulo, "stat-card-title")
    override val lblValor: ILabel = UIFactory.crearEtiqueta(dato.valor, "stat-card-value")
    override val lblDetalle: ILabel = UIFactory.crearEtiqueta(dato.detalle, "stat-card-detail")

    init {
        configurarTarjeta()
    }

    private fun configurarTarjeta() {
        alignment = Pos.CENTER
        padding = UIFactory.crearMargen(16.0, 20.0)
        prefWidth = 210.0
        styleClass.add("stat-card")
        if (dato.claseEstiloExtra.isNotBlank()) {
            styleClass.add(dato.claseEstiloExtra)
        }
        children.addAll(lblIcono, lblTitulo, lblValor, lblDetalle)
    }

    override fun obtenerTextoResumen(): IString {
        return "${lblTitulo.text}: ${lblValor.text}"
    }
}

interface IPanelResumenFinal {
    val lblTitulo: ILabel
    val lblSubtitulo: ILabel
    val tarjetasEstadisticas: IList<TarjetaEstadisticaView>
    val panelEstadisticas: IHBox
    val panelBotones: IHBox
    fun configurarEnContenedor(raiz: IVBox, nodoExtra: INode?)
}

class PanelResumenFinal(
    dificultad: Dificultad,
    estadisticas: EstadisticasPartida,
    tituloTexto: IString,
    subtituloTexto: IString,
    claseEstiloRaiz: IString
) : IPanelResumenFinal {

    private val estiloRaiz: IString = claseEstiloRaiz
    override val lblTitulo: ILabel = UIFactory.crearTitulo(tituloTexto, "end-screen-title")
    override val lblSubtitulo: ILabel = UIFactory.crearSubtitulo(
        "${dificultad.icono} \u004eivel ${dificultad.nombreMostrar} \u2014 $subtituloTexto",
        "end-screen-subtitle"
    )
    override val tarjetasEstadisticas: IList<TarjetaEstadisticaView> =
        estadisticas.construirTarjetasResumen().map { crearVistaTarjeta(it) }
    override val panelEstadisticas: IHBox = UIFactory.crearFila(16.0, Pos.CENTER)
    override val panelBotones: IHBox = UIFactory.crearFila(18.0, Pos.CENTER)

    override fun configurarEnContenedor(raiz: IVBox, nodoExtra: INode?) {
        raiz.alignment = Pos.CENTER
        raiz.padding = UIFactory.crearMargen(36.0, 48.0)
        raiz.styleClass.addAll("root-pane", "end-screen-root", estiloRaiz)
        panelEstadisticas.children.setAll(tarjetasEstadisticas)
        val tarjetaCentral = armarTarjetaCentral(nodoExtra)
        raiz.children.setAll(tarjetaCentral)
        AnimadorUI.animarPulsoContinuo(lblTitulo)
        AnimadorUI.animarEntrada(tarjetaCentral, duracionMs = 360.0)
    }

    private fun armarTarjetaCentral(nodoExtra: INode?): IVBox {
        val caja = UIFactory.crearColumna(22.0, Pos.CENTER)
        caja.styleClass.addAll("card-panel", "end-screen-card")
        caja.maxWidth = 920.0
        caja.padding = UIFactory.crearMargen(32.0, 40.0)
        caja.children.addAll(lblTitulo, lblSubtitulo)
        if (nodoExtra != null) caja.children.add(nodoExtra)
        caja.children.addAll(panelEstadisticas, panelBotones)
        return caja
    }

    companion object {
        private fun crearVistaTarjeta(dato: DatoEstadistica): TarjetaEstadisticaView {
            return TarjetaEstadisticaView(dato)
        }
    }
}
