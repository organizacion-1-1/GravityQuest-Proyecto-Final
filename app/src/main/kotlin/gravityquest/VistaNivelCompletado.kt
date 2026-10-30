package gravityquest

import gravityquest.models.Dificultad
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label

/**
 * ============================================================================
 * GravityQuest - Pantalla Final de Victoria ("¡Nivel Completado!")
 * Milestone 3 - Issue #20 (POO, SOLID y DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - Construye y presenta exclusivamente la pantalla triunfal al superar el
 *      desafío físico del nivel activo, mostrando el desglose de recompensas
 *      (puntos sumados, bono por racha y bonificación por vidas conservadas).
 *
 * 2. Open/Closed Principle (OCP) y Reutilización (DRY):
 *    - Extiende [VistaFinalBase] reutilizando el layout base, las tarjetas
 *      inmutables [TarjetaEstadisticaView] y los constructores de [UIFactory].
 *
 * 3. Dependency Inversion Principle (DIP):
 *    - Se comunica con la navegación general delegando las transiciones a
 *      través de la interfaz [INavegadorEscenas] y el desbloqueo progresivo
 *      mediante [IGestorProgreso].
 */
class VistaNivelCompletado(
    dificultadSuperada: Dificultad = Dificultad.FACIL,
    estadisticas: EstadisticasPartida = EstadisticasPartida(dificultad = dificultadSuperada),
    navegador: INavegadorEscenas,
    gestorProgreso: IGestorProgreso? = null,
    val siguienteNivel: Dificultad? = dificultadSuperada.siguienteNivel
) : VistaFinalBase(dificultadSuperada, estadisticas, navegador, gestorProgreso) {

    companion object {
        const val TEXTO_TITULO_VICTORIA: String = "¡NIVEL COMPLETADO!"
    }

    // 1. Título triunfal con estética verde/cian neón (.titulo-victoria)
    override val lblTitulo: Label = UIFactory.crearTituloFinal(
        texto = TEXTO_TITULO_VICTORIA,
        claseEstilo = "titulo-victoria"
    )

    override val lblSubtitulo: Label = UIFactory.crearEtiqueta(
        texto = construirTextoSubtitulo(),
        clasesEstilo = listOf("label", "subtitulo-menu", "subtitulo-final"),
        anchoMax = 540.0,
        wrap = true,
        alineacion = Pos.CENTER
    )

    // 2. Panel de recompensas y estadísticas modulares e inmutables (DRY / OCP)
    val tarjetaPuntosSumados: TarjetaEstadisticaView = UIFactory.crearTarjetaEstadistica(
        titulo = "Puntos Sumados",
        valor = "+${estadisticas.puntosSumadosNivel} PTS",
        icono = "⭐",
        detalle = "Total: ${estadisticas.puntosTotales} PTS"
    )

    val tarjetaBonoRacha: TarjetaEstadisticaView = UIFactory.crearTarjetaEstadistica(
        titulo = "Bono por Racha",
        valor = "+${estadisticas.bonoPorRacha} PTS",
        icono = "🔥",
        detalle = "Racha máx: ${estadisticas.rachaMaxima}"
    )

    val tarjetaBonificacionVidas: TarjetaEstadisticaView = UIFactory.crearTarjetaEstadistica(
        titulo = "Bono por Vidas",
        valor = "+${estadisticas.bonificacionVidas} PTS",
        icono = "❤",
        detalle = "${estadisticas.vidasRestantes} vidas conservadas"
    )

    val tarjetaTiempoUsado: TarjetaEstadisticaView = UIFactory.crearTarjetaEstadistica(
        titulo = "Tiempo Usado",
        valor = estadisticas.tiempoFormateado(),
        icono = "⏱",
        detalle = "Ejercicios: ${estadisticas.ejerciciosResueltos}"
    )

    override val tarjetasEstadisticas: List<TarjetaEstadisticaView> = listOf(
        tarjetaPuntosSumados,
        tarjetaBonoRacha,
        tarjetaBonificacionVidas,
        tarjetaTiempoUsado
    )

    // 3. Botones de acción creados mediante UIFactory con clase .boton-accion-final (DRY)
    val btnSiguienteNivel: Button = UIFactory.crearBotonSiguienteNivel("Siguiente Nivel")

    val btnMenuPrincipal: Button = UIFactory.crearBotonMenuPrincipal("Menú Principal")

    /** Alias de compatibilidad para acceso semántico al botón de menú. */
    val btnVolverMenu: Button get() = btnMenuPrincipal

    init {
        styleClass.add("vista-nivel-completado")
        contenedorPrincipal.styleClass.add("pantalla-victoria")

        configurarAcciones()
        inicializarEstructuraVistaFinal(listOf(btnSiguienteNivel, btnMenuPrincipal))
    }

    private fun construirTextoSubtitulo(): String =
        if (siguienteNivel != null) {
            "¡Excelente cálculo en el Nivel ${dificultad.nombreVisible}! Has desbloqueado el Nivel ${siguienteNivel.nombreVisible}."
        } else {
            "🏆 ¡Misión Maestra Superada! Has completado toda la campaña de GravityQuest (Fácil → Medio → Difícil)."
        }

    private fun configurarAcciones() {
        btnSiguienteNivel.setOnAction {
            avanzarSiguienteNivel()
        }

        btnMenuPrincipal.setOnAction {
            volverAlMenu()
        }
    }

    /**
     * Desbloquea el siguiente nivel de dificultad en [IGestorProgreso] y avanza directamente
     * hacia él a través de [INavegadorEscenas].
     *
     * @return La [Dificultad] destino a la que se avanzó, o `null` si se completó el último nivel y se regresó al menú.
     */
    fun avanzarSiguienteNivel(): Dificultad? {
        val destino = siguienteNivel ?: gestorProgreso?.obtenerSiguienteNivel(dificultad)
        return if (destino != null) {
            gestorProgreso?.desbloquearNivelManualmente(destino)
            gestorProgreso?.reiniciarVidas()
            navegador.irAJuego(destino)
            destino
        } else {
            navegador.irAMenu()
            null
        }
    }

    /**
     * Regresa al menú principal delegando la acción en [INavegadorEscenas].
     */
    fun volverAlMenu() {
        navegador.irAMenu()
    }

    override fun ejecutarAnimacionEntrada() {
        FxAnimationUtils.animarCelebracion(lblTitulo)
    }
}
