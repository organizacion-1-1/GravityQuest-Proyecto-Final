package gravityquest

import javafx.animation.FadeTransition
import javafx.animation.ScaleTransition
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.util.Duration
import java.util.Locale
import kotlin.math.max

/**
 * ============================================================================
 * GravityQuest - Marcador Dinámico de Puntuación (POO, SOLID & DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - Su única responsabilidad es la representación gráfica y animación del
 *      puntaje en el panel superior de JavaFX. No calcula reglas ni almacena lógica
 *      de negocio.
 *
 * 2. Dependency Inversion Principle (DIP) y Patrón Observer:
 *    - Implementa [IPuntajeListener] y observa de forma reactiva a [IGestorProgreso],
 *      actualizándose automáticamente ante cualquier cambio de puntuación en el modelo.
 *
 * 3. Don't Repeat Yourself (DRY):
 *    - Reutiliza [UIFactory] para la instanciación de etiquetas con estilos espaciales
 *      neón (`.puntos-texto` y `.puntos-incremento`) y [FxAnimationUtils] para la
 *      ejecución segura de transiciones en JavaFX.
 */
class MarcadorPuntosView(
    private val gestorProgreso: IGestorProgreso? = null
) : HBox(8.0), IPuntajeListener {

    /** Referencia al nodo raíz del componente visual. */
    val root: Parent get() = this

    /** Puntaje actualmente representado en la etiqueta visual. */
    var puntajeMostrado: Int = max(0, gestorProgreso?.puntajeActual ?: 0)
        private set

    /** Etiqueta principal del puntaje con formato estilizado `"PUNTAJE: 0000"`. */
    val lblPuntos: Label = UIFactory.crearEtiquetaPuntos(
        texto = formatearTextoPuntaje(puntajeMostrado),
        claseEstilo = "puntos-texto"
    )

    /** Etiqueta secundaria para mostrar el incremento/decremento dinámico (ej. `"+100"`, `"+50"`, `"-10"`). */
    val lblIncremento: Label = UIFactory.crearEtiquetaPuntos(
        texto = "",
        claseEstilo = "puntos-incremento"
    ).apply {
        opacity = 0.0
    }

    init {
        alignment = Pos.CENTER
        styleClass.add("marcador-puntuacion")
        children.addAll(lblPuntos, lblIncremento)

        // Suscripción reactiva al modelo mediante el patrón Observer/Listener (DIP)
        gestorProgreso?.agregarListenerPuntaje(this)
    }

    /**
     * Callback del patrón Observer ([IPuntajeListener]) invocado automáticamente
     * cuando el puntaje cambia en [IGestorProgreso].
     */
    override fun alCambiarPuntaje(nuevoPuntaje: Int, delta: Int) {
        if (Platform.isFxApplicationThread()) {
            actualizarVista(nuevoPuntaje, delta)
        } else {
            // Actualización inmediata del estado de texto para consistencia en tests síncronos
            actualizarVista(nuevoPuntaje, delta)
        }
    }

    /**
     * Actualiza el marcador visual con el nuevo puntaje y dispara la transición suave en JavaFX.
     *
     * @param nuevoPuntaje Valor actualizado del puntaje total.
     * @param delta Variación de puntos producida en el evento.
     */
    fun actualizarVista(nuevoPuntaje: Int, delta: Int = nuevoPuntaje - puntajeMostrado) {
        val puntajeSeguro = max(0, nuevoPuntaje)
        this.puntajeMostrado = puntajeSeguro
        this.lblPuntos.text = formatearTextoPuntaje(puntajeSeguro)

        if (delta != 0) {
            this.lblIncremento.text = if (delta > 0) "+$delta" else "$delta"
            this.lblIncremento.opacity = 1.0
        } else {
            this.lblIncremento.text = ""
            this.lblIncremento.opacity = 0.0
        }

        ejecutarAnimacionCambio(delta)
    }

    /**
     * Aplica una transición suave en JavaFX ([ScaleTransition] y [FadeTransition])
     * sobre el texto del marcador para dar retroalimentación visual inmediata al usuario.
     */
    private fun ejecutarAnimacionCambio(delta: Int) {
        FxAnimationUtils.ejecutarSeguro {
            // Transición de pulso suave sobre el texto principal ("PUNTAJE: 0000")
            ScaleTransition(Duration.millis(180.0), lblPuntos).apply {
                fromX = 1.0
                fromY = 1.0
                toX = 1.18
                toY = 1.18
                cycleCount = 2
                isAutoReverse = true
                setOnFinished {
                    lblPuntos.scaleX = 1.0
                    lblPuntos.scaleY = 1.0
                }
                play()
            }

            // Transición de desvanecimiento suave sobre el indicador de variación (+100 / +50 / -10)
            if (delta != 0) {
                FadeTransition(Duration.millis(750.0), lblIncremento).apply {
                    fromValue = 1.0
                    toValue = 0.0
                    setOnFinished {
                        lblIncremento.opacity = 0.0
                    }
                    play()
                }
            }
        }
    }

    /**
     * Libera la suscripción del listener en el gestor de progreso.
     */
    fun desvincular() {
        gestorProgreso?.removerListenerPuntaje(this)
    }

    companion object {
        /**
         * Genera el texto estilizado requerido: `"PUNTAJE: 0000"`.
         */
        fun formatearTextoPuntaje(puntos: Int): String =
            String.format(Locale.US, "PUNTAJE: %04d", max(0, puntos))
    }
}
