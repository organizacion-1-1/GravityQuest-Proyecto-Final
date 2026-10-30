package gravityquest

import gravityquest.models.Dificultad
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.util.Locale

/**
 * ============================================================================
 * GravityQuest - Modelos de Estadísticas y Componentes de Pantallas Finales
 * Milestone 3 - Issue #20 (POO, SOLID y DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - [EstadisticasPartida] es un objeto de valor (Value Object) estrictamente
 *      inmutable que encapsula las métricas de desempeño de una partida o intento
 *      de nivel (puntos totales, racha máxima, ejercicios resueltos, tiempo usado,
 *      vidas restantes y desglose de bonificaciones).
 *    - [DatoEstadistica] modela la información inmutable de una métrica individual.
 *    - [TarjetaEstadisticaView] encapsula la representación visual modular de una
 *      tarjeta de estadística reutilizable.
 *    - [VistaFinalBase] abstrae la estructura común de las pantallas de cierre
 *      ([VistaGameOver] y [VistaNivelCompletado]).
 *
 * 2. Open/Closed Principle (OCP) y DRY:
 *    - Nuevas métricas o pantallas finales pueden añadirse componiendo instancias
 *      de [TarjetaEstadisticaView] sin duplicar código de layout ni estilos CSS.
 *
 * 3. Dependency Inversion Principle (DIP):
 *    - Las vistas finales dependen únicamente de las abstracciones [INavegadorEscenas]
 *      e [IGestorProgreso].
 */

/**
 * Objeto de valor inmutable (Value Object) que representa las estadísticas y recompensas
 * de una partida o intento de nivel en GravityQuest.
 *
 * @property puntosTotales Puntaje total acumulado por el jugador en la sesión.
 * @property rachaMaxima Racha máxima de ejercicios resueltos de forma consecutiva.
 * @property ejerciciosResueltos Cantidad total de ejercicios completados con éxito.
 * @property tiempoUsadoSegundos Tiempo empleado en el intento o nivel actual (en segundos).
 * @property vidasRestantes Cantidad de vidas conservadas al concluir el nivel (`0..3`).
 * @property puntosSumadosNivel Puntos base obtenidos al resolver el ejercicio del nivel.
 * @property bonoPorRacha Bonificación otorgada por mantener una racha activa de aciertos.
 * @property bonificacionVidas Bonificación otorgada por las vidas conservadas al superar el nivel.
 * @property rachaActual Racha vigente al momento de finalizar el intento.
 * @property dificultad Nivel de dificultad jugado en la partida.
 */
data class EstadisticasPartida(
    val puntosTotales: Int = 0,
    val rachaMaxima: Int = 0,
    val ejerciciosResueltos: Int = 0,
    val tiempoUsadoSegundos: Double = 0.0,
    val vidasRestantes: Int = GestorVidas.MAX_VIDAS_DEFAULT,
    val puntosSumadosNivel: Int = 0,
    val bonoPorRacha: Int = 0,
    val bonificacionVidas: Int = 0,
    val rachaActual: Int = 0,
    val dificultad: Dificultad = Dificultad.FACIL
) {
    init {
        require(puntosTotales >= 0) { "Los puntos totales no pueden ser negativos: $puntosTotales" }
        require(rachaMaxima >= 0) { "La racha máxima no puede ser negativa: $rachaMaxima" }
        require(ejerciciosResueltos >= 0) { "Los ejercicios resueltos no pueden ser negativos: $ejerciciosResueltos" }
        require(tiempoUsadoSegundos >= 0.0 && !tiempoUsadoSegundos.isNaN() && !tiempoUsadoSegundos.isInfinite()) {
            "El tiempo usado debe ser un número finito no negativo: $tiempoUsadoSegundos"
        }
        require(vidasRestantes >= 0) { "Las vidas restantes no pueden ser negativas: $vidasRestantes" }
        require(puntosSumadosNivel >= 0) { "Los puntos sumados del nivel no pueden ser negativos: $puntosSumadosNivel" }
        require(bonoPorRacha >= 0) { "El bono por racha no puede ser negativo: $bonoPorRacha" }
        require(bonificacionVidas >= 0) { "La bonificación por vidas no puede ser negativa: $bonificacionVidas" }
        require(rachaActual >= 0) { "La racha actual no puede ser negativa: $rachaActual" }
    }

    /**
     * Calcula el total de puntos y bonificaciones generados en el nivel completado.
     */
    fun totalRecompensasNivel(): Int = puntosSumadosNivel + bonoPorRacha + bonificacionVidas

    /**
     * Formatea el tiempo empleado en formato legible (`MM:SS` o segundos con decimal).
     */
    fun tiempoFormateado(): String {
        val totalSegundos = tiempoUsadoSegundos.toInt()
        val minutos = totalSegundos / 60
        val segundos = totalSegundos % 60
        return if (minutos > 0) {
            String.format(Locale.US, "%02d:%02d min", minutos, segundos)
        } else {
            String.format(Locale.US, "%.1f s", tiempoUsadoSegundos)
        }
    }

    /**
     * Formatea el puntaje total con relleno de 4 dígitos acorde a la estética arcade espacial.
     */
    fun puntajeTotalFormateado(): String =
        String.format(Locale.US, "%04d PTS", puntosTotales)
}

/**
 * Modelo inmutable para describir el contenido de una tarjeta de estadística individual (DRY / OCP).
 *
 * @property titulo Etiqueta descriptiva superior de la estadística (ej. "PUNTAJE OBTENIDO").
 * @property valor Valor destacado principal (ej. "0250 PTS").
 * @property icono Símbolo o emoji temático espacial/neón.
 * @property detalle Subtexto explicativo opcional para aportar contexto.
 * @property claseEstiloExtra Clase CSS adicional opcional para especializar la tarjeta.
 */
data class DatoEstadistica(
    val titulo: String,
    val valor: String,
    val icono: String = "📊",
    val detalle: String = "",
    val claseEstiloExtra: String? = null
) {
    init {
        require(titulo.isNotBlank()) { "El título de la tarjeta de estadística no puede estar vacío" }
        require(valor.isNotBlank()) { "El valor de la tarjeta de estadística no puede estar vacío" }
    }
}

/**
 * Componente UI modular, inmutable y reutilizable que representa una tarjeta de estadística
 * individual tanto en la pantalla de victoria como en la de derrota (OCP & DRY).
 */
class TarjetaEstadisticaView(
    val dato: DatoEstadistica
) : VBox(6.0) {

    constructor(
        titulo: String,
        valor: String,
        icono: String = "📊",
        detalle: String = "",
        claseEstiloExtra: String? = null
    ) : this(DatoEstadistica(titulo, valor, icono, detalle, claseEstiloExtra))

    val lblIcono: Label = UIFactory.crearEtiqueta(
        texto = dato.icono,
        clasesEstilo = listOf("label", "tarjeta-estadistica-icono"),
        alineacion = Pos.CENTER
    )

    val lblTitulo: Label = UIFactory.crearEtiqueta(
        texto = dato.titulo.uppercase(),
        clasesEstilo = listOf("label", "tarjeta-estadistica-titulo"),
        alineacion = Pos.CENTER,
        wrap = true
    )

    val lblValor: Label = UIFactory.crearEtiqueta(
        texto = dato.valor,
        clasesEstilo = listOf("label", "tarjeta-estadistica-valor"),
        alineacion = Pos.CENTER
    )

    val lblDetalle: Label = UIFactory.crearEtiqueta(
        texto = dato.detalle,
        clasesEstilo = listOf("label", "tarjeta-estadistica-detalle"),
        alineacion = Pos.CENTER,
        wrap = true
    )

    init {
        alignment = Pos.CENTER
        padding = Insets(14.0, 18.0, 14.0, 18.0)
        prefWidth = 175.0
        minWidth = 155.0
        styleClass.add("tarjeta-estadistica")
        dato.claseEstiloExtra?.let { styleClass.add(it) }

        children.addAll(lblIcono, lblTitulo, lblValor)
        if (dato.detalle.isNotBlank()) {
            children.add(lblDetalle)
        }
    }
}

/**
 * Clase base abstracta para las pantallas finales de resumen ([VistaGameOver] y [VistaNivelCompletado]).
 *
 * Aplica el Patrón Template Method y el principio DRY centralizando la disposición gráfica,
 * el contenedor `.pantalla-final`, la grilla de [TarjetaEstadisticaView] y la barra de acciones,
 * delegando a las subclases únicamente la definición de sus datos y acciones específicas (SRP / OCP).
 */
abstract class VistaFinalBase(
    val dificultad: Dificultad,
    val estadisticas: EstadisticasPartida,
    protected val navegador: INavegadorEscenas,
    protected val gestorProgreso: IGestorProgreso? = null
) : VBox(20.0) {

    /** Nodo raíz de la vista final para su montaje en [INavegadorEscenas]. */
    val root: Parent get() = this

    /** Etiqueta del título principal (configurada con `.titulo-gameover` o `.titulo-victoria`). */
    abstract val lblTitulo: Label

    /** Subtítulo descriptivo del resultado de la misión. */
    abstract val lblSubtitulo: Label

    /** Lista inmutable de tarjetas de estadísticas renderizadas en la vista. */
    abstract val tarjetasEstadisticas: List<TarjetaEstadisticaView>

    /** Contenedor de las tarjetas modulares de estadísticas. */
    val panelEstadisticas: FlowPane = FlowPane(14.0, 14.0).apply {
        alignment = Pos.CENTER
        prefWrapLength = 580.0
        styleClass.add("panel-estadisticas-final")
    }

    /** Contenedor horizontal para los botones de acción principales. */
    val panelBotones: HBox = UIFactory.crearPanelHorizontal(16.0, "panel-botones-final")

    /** Contenedor tipo tarjeta central con la clase CSS `.pantalla-final`. */
    val contenedorPrincipal: VBox = VBox(22.0).apply {
        alignment = Pos.CENTER
        padding = Insets(32.0, 38.0, 32.0, 38.0)
        maxWidth = 660.0
        styleClass.add("pantalla-final")
    }

    /**
     * Ensambla la estructura común de la pantalla final e invoca las animaciones de entrada.
     * Debe llamarse al final del bloque `init` de cada subclase concreta.
     */
    protected fun inicializarEstructuraVistaFinal(botonesAccion: List<Button>) {
        alignment = Pos.CENTER
        padding = Insets(24.0)
        styleClass.addAll("root", "pantalla-final")

        panelEstadisticas.children.setAll(tarjetasEstadisticas)
        panelBotones.children.setAll(botonesAccion)

        contenedorPrincipal.children.setAll(
            lblTitulo,
            lblSubtitulo,
            panelEstadisticas,
            panelBotones
        )

        children.setAll(contenedorPrincipal)
        ejecutarAnimacionEntrada()
    }

    /**
     * Gancho polimórfico (Hook Method) para que las subclases ejecuten efectos visuales al desplegarse.
     */
    protected open fun ejecutarAnimacionEntrada() {
        FxAnimationUtils.animarPulso(lblTitulo, escala = 1.08, duracionMs = 260.0)
    }
}
