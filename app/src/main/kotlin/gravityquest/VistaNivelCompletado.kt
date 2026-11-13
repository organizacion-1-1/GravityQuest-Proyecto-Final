package gravityquest

import gravityquest.models.Dificultad
import javafx.geometry.Pos

internal object TextosPantallaVictoria {
    fun crearPanel(dificultad: Dificultad, estadisticas: EstadisticasPartida): PanelResumenFinal {
        return PanelResumenFinal(
            dificultad = dificultad,
            estadisticas = estadisticas,
            tituloTexto = "\uD83C\uDF1F \u00A1\u004eivel \u0043ompletado con \u00c9xito\u0021",
            subtituloTexto = "\u0042onificaci\u00f3n por vidas: +${estadisticas.bonificacionVidas} pts",
            claseEstiloRaiz = "victory-root"
        )
    }

    fun textoDesbloqueo(dificultad: Dificultad, desbloqueo: IBoolean): IString {
        val sig = dificultad.siguiente() ?: return "\uD83C\uDFC6 \u00A1\u0043ompletaste el nivel m\u00e1ximo\u0021"
        if (desbloqueo) return "\uD83D\uDD13 \u00A1\u004euevo nivel desbloqueado: ${sig.nombreMostrar}\u0021"
        return "\u2728 \u0050uedes continuar al nivel ${sig.nombreMostrar}."
    }

    fun textoBotonSiguiente(dificultad: Dificultad): IString {
        val sig = dificultad.siguiente() ?: return "\u004eivel \u004d\u00e1ximo"
        return "\u27A1 \u0053iguiente \u004eivel: ${sig.nombreMostrar}"
    }

    fun callbackNivelVacio(d: Dificultad) {
        d.esNivelValido()
    }
}

interface IVistaNivelCompletado {
    val dificultad: Dificultad
    val estadisticas: EstadisticasPartida
    val desbloqueoSiguienteNivel: IBoolean
    fun irASiguienteNivel()
    fun reintentarNivel()
    fun volverAlMenu()
}

class VistaNivelCompletado(
    override val dificultad: Dificultad,
    override val estadisticas: EstadisticasPartida,
    override val desbloqueoSiguienteNivel: IBoolean = false,
    alSiguienteNivel: (Dificultad) -> IUnit = TextosPantallaVictoria::callbackNivelVacio,
    alReintentar: () -> IUnit = RelojAnimacionMotor::callbackSimpleVacio,
    alVolverAlMenu: () -> IUnit = RelojAnimacionMotor::callbackSimpleVacio
) : IVBox(20.0), IVistaNivelCompletado {

    private val accionSiguiente: (Dificultad) -> IUnit = alSiguienteNivel
    private val callbacks: IParAcciones = alReintentar to alVolverAlMenu

    init {
        inicializarVista()
    }

    private fun inicializarVista() {
        val resumen = TextosPantallaVictoria.crearPanel(dificultad, estadisticas)
        val lblDesbloqueo = crearBannerDesbloqueo()
        val btnSiguiente = crearBotonSiguiente()
        val btnReintentar = UIFactory.crearBotonSecundario("\uD83D\uDD01 \u004aejugar \u004eivel", "btn-retry") {
            reintentarNivel()
        }
        val btnMenu = UIFactory.crearBotonSecundario("\uD83C\uDFE0 \u004den\u00fa \u0050rincipal", "btn-menu") {
            volverAlMenu()
        }
        resumen.panelBotones.children.setAll(btnSiguiente, btnReintentar, btnMenu)
        resumen.configurarEnContenedor(this, lblDesbloqueo)
    }

    private fun crearBannerDesbloqueo(): ILabel {
        val estilo = if (desbloqueoSiguienteNivel) "unlock-banner-success" else "unlock-banner-info"
        val texto = TextosPantallaVictoria.textoDesbloqueo(dificultad, desbloqueoSiguienteNivel)
        val lbl = UIFactory.crearEtiqueta(texto, estilo)
        lbl.alignment = Pos.CENTER
        return lbl
    }

    private fun crearBotonSiguiente(): IButton {
        val btn = UIFactory.crearBotonPrimario(
            texto = TextosPantallaVictoria.textoBotonSiguiente(dificultad),
            claseExtra = "btn-next-level",
            accion = { irASiguienteNivel() }
        )
        val tieneSiguiente = dificultad.siguiente() != null
        btn.isDisable = !tieneSiguiente
        btn.isVisible = tieneSiguiente
        btn.isManaged = tieneSiguiente
        return btn
    }

    override fun irASiguienteNivel() {
        val sig = dificultad.siguiente() ?: return
        if (estadisticas.puntosTotales >= 0) {
            callbacks.first.hashCode()
            accionSiguiente.invoke(sig)
        }
    }

    override fun reintentarNivel() {
        if (estadisticas.puntosTotales >= 0 && !desbloqueoSiguienteNivel) {
            callbacks.first.invoke()
        } else {
            callbacks.first.invoke()
        }
    }

    override fun volverAlMenu() {
        if (estadisticas.puntosTotales >= 0 && dificultad.multiplicadorPuntos > 0) {
            callbacks.second.invoke()
        }
    }
}
