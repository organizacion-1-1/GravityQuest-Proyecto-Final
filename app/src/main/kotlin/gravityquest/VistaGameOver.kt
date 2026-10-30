package gravityquest

import gravityquest.models.Dificultad
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label

/**
 * ============================================================================
 * GravityQuest - Pantalla Final de Derrota / Misión Fallida (VistaGameOver)
 * Milestone 3 - Issue #20 (POO, SOLID y DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - Construye y gestiona exclusivamente la pantalla de resumen cuando el jugador
 *      pierde las 3 vidas disponibles en el intento.
 *    - No contiene lógica de negocio ni de cálculo; presenta el Value Object
 *      inmutable [EstadisticasPartida] recibido.
 *
 * 2. Open/Closed Principle (OCP) y Reutilización (DRY):
 *    - Extiende [VistaFinalBase] y reutiliza componentes modulares [TarjetaEstadisticaView]
 *      y controles instanciados mediante [UIFactory].
 *
 * 3. Dependency Inversion Principle (DIP):
 *    - Delega todas las transiciones ("Reintentar Nivel" y "Menú Principal") a la
 *      abstracción [INavegadorEscenas] y el restablecimiento de vidas a [IGestorProgreso].
 */
class VistaGameOver(
    dificultad: Dificultad = Dificultad.FACIL,
    estadisticas: EstadisticasPartida = EstadisticasPartida(vidasRestantes = 0, dificultad = dificultad),
    navegador: INavegadorEscenas,
    gestorProgreso: IGestorProgreso? = null
) : VistaFinalBase(dificultad, estadisticas, navegador, gestorProgreso) {

    companion object {
        const val TEXTO_TITULO_GAMEOVER: String = "GAME OVER - MISION FALLIDA"
    }

    // 1. Título destacado con estética neón/oscura (.titulo-gameover)
    override val lblTitulo: Label = UIFactory.crearTituloFinal(
        texto = TEXTO_TITULO_GAMEOVER,
        claseEstilo = "titulo-gameover"
    )

    override val lblSubtitulo: Label = UIFactory.crearEtiqueta(
        texto = "Has agotado tus 3 vidas en la Misión de Nivel ${dificultad.nombreVisible}. ¡Revisa tus cálculos cinemáticos e inténtalo de nuevo!",
        clasesEstilo = listOf("label", "subtitulo-menu", "subtitulo-final"),
        anchoMax = 520.0,
        wrap = true,
        alineacion = Pos.CENTER
    )

    // 2. Tarjetas modulares e inmutables con las estadísticas del intento (DRY / OCP)
    val tarjetaPuntaje: TarjetaEstadisticaView = UIFactory.crearTarjetaEstadistica(
        titulo = "Puntaje Obtenido",
        valor = "${estadisticas.puntosTotales} PTS",
        icono = "🎯",
        detalle = "Acumulado en misión"
    )

    val tarjetaRachaMaxima: TarjetaEstadisticaView = UIFactory.crearTarjetaEstadistica(
        titulo = "Racha Máxima",
        valor = "${estadisticas.rachaMaxima}",
        icono = "🔥",
        detalle = "Aciertos seguidos"
    )

    val tarjetaEjerciciosCompletados: TarjetaEstadisticaView = UIFactory.crearTarjetaEstadistica(
        titulo = "Ejercicios Completados",
        valor = "${estadisticas.ejerciciosResueltos}",
        icono = "✅",
        detalle = "Resueltos antes de caer"
    )

    val tarjetaTiempoUsado: TarjetaEstadisticaView = UIFactory.crearTarjetaEstadistica(
        titulo = "Tiempo Empleado",
        valor = estadisticas.tiempoFormateado(),
        icono = "⏱",
        detalle = "Duración del intento"
    )

    override val tarjetasEstadisticas: List<TarjetaEstadisticaView> = listOf(
        tarjetaPuntaje,
        tarjetaRachaMaxima,
        tarjetaEjerciciosCompletados,
        tarjetaTiempoUsado
    )

    // 3. Botones principales creados mediante UIFactory con la clase .boton-accion-final (DRY)
    val btnReintentar: Button = UIFactory.crearBotonReintentar("Reintentar Nivel")

    val btnMenuPrincipal: Button = UIFactory.crearBotonMenuPrincipal("Menú Principal")

    /** Alias de compatibilidad para acceso semántico al botón de menú. */
    val btnVolverMenu: Button get() = btnMenuPrincipal

    init {
        styleClass.add("vista-gameover")
        contenedorPrincipal.styleClass.add("pantalla-gameover")

        configurarAcciones()
        inicializarEstructuraVistaFinal(listOf(btnReintentar, btnMenuPrincipal))
    }

    private fun configurarAcciones() {
        btnReintentar.setOnAction {
            reintentarNivel()
        }

        btnMenuPrincipal.setOnAction {
            volverAlMenu()
        }
    }

    /**
     * Reinicia las 3 vidas en el gestor de progreso y redirige al nivel desbloqueado correspondiente
     * (iniciando obligatoriamente en [Dificultad.FACIL] si los niveles superiores quedaron bloqueados tras el Game Over).
     */
    fun reintentarNivel() {
        gestorProgreso?.reiniciarVidas()
        val nivelDestino = if (gestorProgreso != null && !gestorProgreso.esNivelDesbloqueado(dificultad)) {
            Dificultad.FACIL
        } else {
            dificultad
        }
        navegador.irAJuego(nivelDestino)
    }

    /**
     * Regresa al menú principal delegando en [INavegadorEscenas].
     */
    fun volverAlMenu() {
        navegador.irAMenu()
    }

    override fun ejecutarAnimacionEntrada() {
        FxAnimationUtils.animarVibracion(lblTitulo, desplazamiento = 6.0, duracionMs = 55.0)
    }
}
