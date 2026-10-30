package gravityquest

import javafx.application.Platform
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.SVGPath

/**
 * ============================================================================
 * GravityQuest - Componente Visual del Sistema de Vidas (POO, SOLID & DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - [PanelVidasView] encapsula y aísla completamente la construcción gráfica,
 *      el intercambio vectorial de corazones ([SVGPath]) y las animaciones de
 *      pérdida/recuperación de las 3 vidas en la barra superior de JavaFX.
 *    - [VistaJuego] solo incorpora este nodo personalizado en `BorderPane.top`
 *      sin manipular rutas SVG ni imágenes de corazones directamente.
 *
 * 2. Dependency Inversion Principle (DIP) y Patrón Observer:
 *    - Se vincula de forma reactiva a una propiedad observable de vidas
 *      ([vidasProperty] : [ReadOnlyIntegerProperty]) expuesta por [IGestorProgreso]
 *      o [IGestorVidas], actualizando automáticamente el estado visual de los
 *      corazones sin intervención manual del controlador.
 *
 * 3. Principio DRY (Don't Repeat Yourself):
 *    - Centraliza la generación vectorial en el método privado de fábrica
 *      [crearIconoCorazon] parametrizado por el estado booleano (`esLleno: Boolean`),
 *      evitando cualquier duplicación de código de instanciación o estilización.
 */
class PanelVidasView(
    val vidasProperty: ReadOnlyIntegerProperty = SimpleIntegerProperty(GestorVidas.MAX_VIDAS_DEFAULT),
    val totalVidas: Int = GestorVidas.MAX_VIDAS_DEFAULT
) : HBox(8.0), IVidasListener {

    companion object {
        /** Ruta vectorial SVG de alta precisión para el ícono de corazón espacial. */
        const val SVG_PATH_CORAZON: String =
            "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 " +
                "2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09" +
                "C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5" +
                "c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
    }

    /** Constructor secundario desacoplado que recibe el contrato [IGestorVidas] (DIP). */
    constructor(gestorVidas: IGestorVidas) : this(
        vidasProperty = gestorVidas.vidasProperty,
        totalVidas = gestorVidas.maxVidas
    )

    /** Constructor secundario desacoplado que recibe el contrato [IGestorProgreso] (DIP). */
    constructor(gestorProgreso: IGestorProgreso) : this(
        vidasProperty = gestorProgreso.vidasProperty,
        totalVidas = gestorProgreso.gestorVidas.maxVidas
    )

    /** Referencia al nodo raíz del componente visual. */
    val root: Parent get() = this

    /** Cantidad de vidas activas actualmente representadas en la vista (`0..totalVidas`). */
    var vidasMostradas: Int = vidasProperty.get().coerceIn(0, totalVidas)
        private set

    /** Colección inmutable de los nodos contenedores de cada corazón alineados horizontalmente. */
    val iconosVidas: List<Label> = List(totalVidas) { indice ->
        val esLlenoInicial = indice < vidasMostradas
        UIFactory.crearIconoVida("").apply {
            graphic = crearIconoCorazon(esLleno = esLlenoInicial)
        }
    }

    /** Acceso directo a los vectores [SVGPath] renderizados dentro de cada posición de vida. */
    val corazonesSvg: List<SVGPath>
        get() = iconosVidas.mapNotNull { it.graphic as? SVGPath }

    /** Listener reactivo suscrito a la propiedad observable `vidasProperty`. */
    private val propiedadListener = ChangeListener<Number> { _, valorPrevio, valorNuevo ->
        val prev = valorPrevio?.toInt() ?: vidasMostradas
        val nuevo = valorNuevo?.toInt() ?: prev
        procesarCambioReactivo(prev, nuevo)
    }

    init {
        alignment = Pos.CENTER
        styleClass.add("panel-vidas")
        children.addAll(iconosVidas)

        // Sincroniza el estado visual inicial sin disparar transición de pérdida
        sincronizarIconos(vidasMostradas, animarIndicePerdido = null, animarReinicio = false)

        // Suscripción reactiva a ReadOnlyIntegerProperty (Observer Pattern / DIP)
        vidasProperty.addListener(propiedadListener)
    }

    /**
     * Método privado de fábrica (DRY) que genera e configura el ícono vectorial [SVGPath]
     * del corazón según su estado booleano (`esLleno: Boolean`).
     *
     * @param esLleno `true` para corazón activo con brillo neón (`.corazon-lleno`),
     *                `false` para corazón perdido en silueta gris/contorno (`.corazon-vacio`).
     */
    private fun crearIconoCorazon(esLleno: Boolean): SVGPath =
        SVGPath().apply {
            content = SVG_PATH_CORAZON
            scaleX = 0.78
            scaleY = 0.78
            if (esLleno) {
                fill = Color.web("#fb7185")
                stroke = Color.web("#f43f5e")
                strokeWidth = 1.2
                styleClass.addAll("corazon-lleno", "icono-vida")
            } else {
                fill = Color.rgb(71, 85, 105, 0.22)
                stroke = Color.web("#64748b")
                strokeWidth = 1.5
                styleClass.addAll("corazon-vacio", "icono-vida", "icono-vida-perdida")
            }
        }

    /**
     * Callback del contrato [IVidasListener] para compatibilidad directa con el patrón Observer.
     */
    override fun alCambiarVidas(vidasRestantes: Int, indiceVidaPerdida: Int?, esGameOver: Boolean) {
        actualizarVidas(vidasRestantes, indiceVidaPerdida)
    }

    private fun procesarCambioReactivo(vidasAnteriores: Int, vidasNuevas: Int) {
        val indicePerdido = if (vidasNuevas < vidasAnteriores) vidasNuevas else null
        val esReinicio = vidasNuevas > vidasAnteriores
        if (Platform.isFxApplicationThread()) {
            actualizarVidas(vidasNuevas, indicePerdido, esReinicio)
        } else {
            actualizarVidas(vidasNuevas, indicePerdido, esReinicio)
        }
    }

    /**
     * Actualiza la representación visual de los 3 corazones y ejecuta la animación
     * correspondiente (encogimiento/parpadeo al perder vida o restauración suave al reiniciar).
     */
    fun actualizarVidas(
        vidasRestantes: Int,
        indiceVidaPerdida: Int? = null,
        animarReinicio: Boolean = false
    ) {
        val vidasSeguras = vidasRestantes.coerceIn(0, totalVidas)
        val indiceAnimar = indiceVidaPerdida ?: if (vidasSeguras < vidasMostradas) vidasSeguras else null
        val restaurandoVidas = animarReinicio || (vidasSeguras > vidasMostradas)
        this.vidasMostradas = vidasSeguras
        sincronizarIconos(vidasSeguras, indiceAnimar, restaurandoVidas)
    }

    private fun sincronizarIconos(
        vidasActivas: Int,
        animarIndicePerdido: Int?,
        animarReinicio: Boolean
    ) {
        for (i in iconosVidas.indices) {
            val contenedor = iconosVidas[i]
            val estaLleno = i < vidasActivas

            // Intercambia el ícono vectorial usando el método privado de fábrica (DRY)
            val corazonSvg = crearIconoCorazon(esLleno = estaLleno)
            contenedor.graphic = corazonSvg
            contenedor.text = ""

            if (estaLleno) {
                contenedor.styleClass.removeAll("icono-vida-perdida", "corazon-vacio")
                if (!contenedor.styleClass.contains("icono-vida")) {
                    contenedor.styleClass.add("icono-vida")
                }
                if (!contenedor.styleClass.contains("corazon-lleno")) {
                    contenedor.styleClass.add("corazon-lleno")
                }
                contenedor.opacity = 1.0
                contenedor.scaleX = 1.0
                contenedor.scaleY = 1.0
                if (animarReinicio) {
                    FxAnimationUtils.animarRecuperacionVida(contenedor)
                }
            } else {
                contenedor.styleClass.remove("corazon-lleno")
                if (!contenedor.styleClass.contains("icono-vida")) {
                    contenedor.styleClass.add("icono-vida")
                }
                if (!contenedor.styleClass.contains("icono-vida-perdida")) {
                    contenedor.styleClass.add("icono-vida-perdida")
                }
                if (!contenedor.styleClass.contains("corazon-vacio")) {
                    contenedor.styleClass.add("corazon-vacio")
                }
                if (animarIndicePerdido != null && i >= animarIndicePerdido) {
                    contenedor.opacity = 0.28
                    FxAnimationUtils.animarPerdidaVida(contenedor, opacidadFinal = 0.28)
                } else {
                    contenedor.opacity = 0.28
                }
            }
        }
    }

    /**
     * Libera la suscripción a la propiedad observable cuando la vista se destruye.
     */
    fun desvincular() {
        vidasProperty.removeListener(propiedadListener)
    }
}
