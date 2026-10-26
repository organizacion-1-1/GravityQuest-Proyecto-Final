package gravityquest

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import kotlin.math.max

/**
 * ============================================================================
 * GravityQuest - Modelo de Estado del Sistema de Vidas (POO, SOLID & DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - [GestorVidas] encapsula exclusivamente las reglas de negocio del sistema
 *      de vidas por intento: conteo inicial (3 vidas), descuento seguro ante
 *      respuestas fallidas, detección de estado de Game Over (0 vidas) y reinicio
 *      del contador.
 *
 * 2. Open/Closed Principle (OCP) y Encapsulamiento Fuerte:
 *    - El estado interno (`private var vidas: Int = 3`) es estrictamente privado.
 *    - Solo puede modificarse mediante los métodos mutadores seguros [perderVida]
 *      y [reiniciarVidas], preservando en todo momento el invariante `0 <= vidas <= maxVidas`.
 *
 * 3. Dependency Inversion Principle (DIP) y Notificación Reactiva:
 *    - Expone una propiedad observable de JavaFX ([vidasProperty] : [IntegerProperty])
 *      y el contrato funcional [IVidasListener] para que componentes visuales como
 *      [PanelVidasView] reaccionen automáticamente sin acoplamiento directo.
 */

/**
 * Contrato de observación (Observer) para reaccionar a cambios en el contador de vidas (DIP).
 */
fun interface IVidasListener {
    /**
     * Se invoca cuando cambia la cantidad de vidas disponibles.
     *
     * @param vidasRestantes Cantidad de vidas activas tras la operación (`0..maxVidas`).
     * @param indiceVidaPerdida Índice base 0 de la vida que se acaba de perder, o `null` si fue un reinicio.
     * @param esGameOver `true` si las vidas llegaron a 0, `false` en caso contrario.
     */
    fun alCambiarVidas(vidasRestantes: Int, indiceVidaPerdida: Int?, esGameOver: Boolean)
}

/**
 * Contrato de abstracción para la gestión de vidas del jugador (DIP).
 */
interface IGestorVidas {
    /** Cantidad máxima de vidas por intento de nivel (por defecto 3). */
    val maxVidas: Int

    /** Cantidad de vidas actualmente disponibles (`0..maxVidas`). */
    val vidasActuales: Int

    /** Propiedad observable de JavaFX para enlace reactivo con la interfaz ([PanelVidasView]). */
    val vidasProperty: IntegerProperty

    /**
     * Descuenta 1 vida al jugador sin bajar nunca de 0.
     *
     * @return Cantidad de vidas restantes luego del descuento.
     */
    fun perderVida(): Int

    /**
     * Restablece el contador de vidas a su valor máximo inicial ([maxVidas]).
     */
    fun reiniciarVidas()

    /**
     * Verifica si el jugador ha perdido todas sus vidas (`vidasActuales == 0`).
     */
    fun esGameOver(): Boolean

    /**
     * Verifica si el jugador aún dispone de al menos una vida activa (`vidasActuales > 0`).
     */
    fun tieneVidas(): Boolean

    /** Suscribe un observador para recibir notificaciones de cambios de vidas. */
    fun agregarListenerVidas(listener: IVidasListener) {}

    /** Elimina un observador previamente registrado. */
    fun removerListenerVidas(listener: IVidasListener) {}
}

/**
 * Implementación del modelo de dominio para el sistema de vidas en GravityQuest.
 */
class GestorVidas(
    override val maxVidas: Int = MAX_VIDAS_DEFAULT
) : IGestorVidas {

    companion object {
        /** Cantidad estándar de vidas al iniciar cada intento de nivel. */
        const val MAX_VIDAS_DEFAULT: Int = 3

        /** Límite inferior de vidas (estado de Game Over). */
        const val MIN_VIDAS: Int = 0
    }

    init {
        require(maxVidas > MIN_VIDAS) {
            "La cantidad máxima de vidas debe ser estrictamente positiva: $maxVidas"
        }
    }

    // Encapsulamiento fuerte requerido: estado interno privado
    private var vidas: Int = maxVidas

    // Propiedad observable de JavaFX para notificación automática (DIP / Observer)
    private val _vidasProperty: SimpleIntegerProperty = SimpleIntegerProperty(maxVidas)
    override val vidasProperty: IntegerProperty
        get() = _vidasProperty

    override val vidasActuales: Int
        get() = vidas

    private val _listeners: MutableList<IVidasListener> = mutableListOf()

    /**
     * Descuenta una vida de forma segura, actualiza la propiedad observable [vidasProperty]
     * y notifica a los observadores registrados.
     *
     * @return El número de vidas restantes (`0..maxVidas`).
     */
    override fun perderVida(): Int {
        if (vidas > MIN_VIDAS) {
            vidas = max(MIN_VIDAS, vidas - 1)
            val indicePerdido = vidas // Índice 0-based del ícono que pierde su estado activo
            _vidasProperty.set(vidas)
            notificarListeners(vidas, indicePerdido, esGameOver())
        }
        return vidas
    }

    /**
     * Restablece las vidas al máximo configurado ([maxVidas]) y notifica a la UI.
     */
    override fun reiniciarVidas() {
        vidas = maxVidas
        _vidasProperty.set(vidas)
        notificarListeners(vidas, null, false)
    }

    override fun esGameOver(): Boolean = vidas <= MIN_VIDAS

    override fun tieneVidas(): Boolean = vidas > MIN_VIDAS

    override fun agregarListenerVidas(listener: IVidasListener) {
        if (!_listeners.contains(listener)) {
            _listeners.add(listener)
        }
    }

    override fun removerListenerVidas(listener: IVidasListener) {
        _listeners.remove(listener)
    }

    private fun notificarListeners(vidasRestantes: Int, indiceVidaPerdida: Int?, gameOver: Boolean) {
        val copia = _listeners.toList()
        for (listener in copia) {
            listener.alCambiarVidas(vidasRestantes, indiceVidaPerdida, gameOver)
        }
    }
}
