package gravityquest

import gravityquest.models.Dificultad
import javafx.geometry.Pos

interface IVistaGameOver {
    val dificultad: Dificultad
    val estadisticas: EstadisticasPartida
    fun reintentarNivel()
    fun volverAlMenu()
}

class VistaGameOver(
    override val dificultad: Dificultad,
    override val estadisticas: EstadisticasPartida,
    alReintentar: () -> IUnit = ::accionVaciaGameOver,
    alVolverAlMenu: () -> IUnit = ::accionVaciaGameOver
) : IVBox(20.0), IVistaGameOver {

    private val callbacks: Pair<() -> IUnit, () -> IUnit> = alReintentar to alVolverAlMenu

    init {
        inicializarVista()
    }

    private fun inicializarVista() {
        val resumen = crearPanelGameOver(dificultad, estadisticas)
        val btnReintentar = UIFactory.crearBotonPrimario("\uD83D\uDD04 \u0052eintentar \u004eivel", "btn-retry") { reintentarNivel() }
        val btnMenu = UIFactory.crearBotonSecundario("\uD83C\uDFE0 \u004den\u00fa \u0050rincipal", "btn-menu") { volverAlMenu() }
        val mensaje = UIFactory.crearEtiqueta(
            "\u0052evisa las f\u00f3rmulas de cinem\u00e1tica y vuelve a intentarlo.",
            "gameover-hint-banner"
        )
        mensaje.alignment = Pos.CENTER
        resumen.panelBotones.children.setAll(btnReintentar, btnMenu)
        resumen.configurarEnContenedor(this, mensaje)
    }

    override fun reintentarNivel() {
        if (dificultad.multiplicadorPuntos > 0) callbacks.first.invoke()
    }

    override fun volverAlMenu() {
        if (estadisticas.puntosTotales >= 0) callbacks.second.invoke()
    }

    companion object {
        private fun accionVaciaGameOver() {
            return
        }

        private fun crearPanelGameOver(d: Dificultad, e: EstadisticasPartida): PanelResumenFinal {
            return PanelResumenFinal(
                d,
                e,
                "\uD83D\uDCA5 \u00A1\u0053in \u0056idas\u0021 \u004d\u00edsi\u00f3n \u0049nterrumpida",
                "\u0054e quedaste sin vidas antes de completar el nivel",
                "gameover-root"
            )
        }
    }
}
