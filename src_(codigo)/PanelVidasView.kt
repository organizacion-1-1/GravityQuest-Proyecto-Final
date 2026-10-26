package gravityquest

import javafx.application.Platform
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.HBox

/**
 * ============================================================================
 * GravityQuest - Componente Visual del Sistema de Vidas (POO, SOLID & DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - [PanelVidasView] es responsable **únicamente** de renderizar visualmente
 *      los 3 íconos de vidas (corazones/íconos neón) alineados horizontalmente
 *      en el panel superior de JavaFX y animar la pérdida o recuperación de vidas.
 *    - No contiene reglas de negocio, conteo ni lógica de reinicio de nivel.
 *
 * 2. Dependency Inversion Principle (DIP) y Patrón Observer:
 *    - Observa de forma reactiva una propiedad [IntegerProperty] ([vidasProperty])
 *      o el contrato [IGestorVidas] / [IGestorProgreso], actualizando los íconos
 *      automáticamente cuando se descuenta o restablece una vida sin acoplamiento directo.
 *
 * 3. Don't Repeat Yourself (DRY):
 *    - Delega la creación de los íconos estilizados a [UIFactory.crearIconoVida]
 *      y las animaciones de parpadeo/desvanecimiento ([javafx.animation.FadeTransition])
 *      a [FxAnimationUtils.animarPerdidaVida].
 */
class PanelVidasView(
    val vidasProperty: IntegerProperty = SimpleIntegerProperty(GestorVidas.MAX_VIDAS_DEFAULT),
    val totalVidas: Int = GestorVidas.MAX_VIDAS_DEFAULT
) : HBox(8.0), IVidasListener {

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

    /** Cantidad de vidas activas actualmente representadas en la vista. */
    var vidasMostradas: Int = vidasProperty.get().coerceIn(0, totalVidas)
        private set

    /** Colección inmutable de los 3 íconos visuales de vida (`.icono-vida`). */
    val iconosVidas: List<Label> = List(totalVidas) {
        UIFactory.crearIconoVida("❤")
    }

    /** Listener reactivo sobre la propiedad JavaFX (`IntegerProperty`). */
    private val propiedadListener = ChangeListener<Number> { _, valorPrevio, valorNuevo ->
        val prev = valorPrevio?.toInt() ?: vidasMostradas
        val nuevo = valorNuevo?.toInt() ?: prev
        procesarCambioReactivo(prev, nuevo)
    }

    init {
        alignment = Pos.CENTER
        styleClass.add("panel-vidas")
        children.addAll(iconosVidas)

        // Sincroniza el estado visual inicial
        sincronizarIconos(vidasMostradas, animarIndicePerdido = null)

        // Suscripción reactiva a IntegerProperty (Observer Pattern / DIP)
        vidasProperty.addListener(propiedadListener)
    }

    /**
     * Callback del contrato [IVidasListener] para compatibilidad directa con el patrón Observer.
     */
    override fun alCambiarVidas(vidasRestantes: Int, indiceVidaPerdida: Int?, esGameOver: Boolean) {
        actualizarVidas(vidasRestantes, indiceVidaPerdida)
    }

    private fun procesarCambioReactivo(vidasAnteriores: Int, vidasNuevas: Int) {
        val indicePerdido = if (vidasNuevas < vidasAnteriores) vidasNuevas else null
        if (Platform.isFxApplicationThread()) {
            actualizarVidas(vidasNuevas, indicePerdido)
        } else {
            // Actualización inmediata del estado y clases CSS para consistencia en pruebas síncronas
            actualizarVidas(vidasNuevas, indicePerdido)
        }
    }

    /**
     * Actualiza la representación visual de los íconos de vidas y ejecuta la animación
     * de desvanecimiento suave (`FadeTransition`) sobre el ícono que acaba de perderse.
     *
     * @param vidasRestantes Cantidad de vidas activas (`0..totalVidas`).
     * @param indiceVidaPerdida Índice opcional del ícono que acaba de perderse para animarlo.
     */
    fun actualizarVidas(vidasRestantes: Int, indiceVidaPerdida: Int? = null) {
        val vidasSeguras = vidasRestantes.coerceIn(0, totalVidas)
        val indiceAnimar = indiceVidaPerdida ?: if (vidasSeguras < vidasMostradas) vidasSeguras else null
        this.vidasMostradas = vidasSeguras
        sincronizarIconos(vidasSeguras, indiceAnimar)
    }

    private fun sincronizarIconos(vidasActivas: Int, animarIndicePerdido: Int?) {
        for (i in iconosVidas.indices) {
            val icono = iconosVidas[i]
            val estaActiva = i < vidasActivas

            if (estaActiva) {
                icono.styleClass.remove("icono-vida-perdida")
                if (!icono.styleClass.contains("icono-vida")) {
                    icono.styleClass.add("icono-vida")
                }
                icono.text = "❤"
                icono.opacity = 1.0
                icono.scaleX = 1.0
                icono.scaleY = 1.0
            } else {
                if (!icono.styleClass.contains("icono-vida")) {
                    icono.styleClass.add("icono-vida")
                }
                if (!icono.styleClass.contains("icono-vida-perdida")) {
                    icono.styleClass.add("icono-vida-perdida")
                }
                icono.text = "♡"
                if (animarIndicePerdido != null && i >= animarIndicePerdido) {
                    icono.opacity = 0.28
                    FxAnimationUtils.animarPerdidaVida(icono, opacidadFinal = 0.28)
                } else {
                    icono.opacity = 0.28
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
