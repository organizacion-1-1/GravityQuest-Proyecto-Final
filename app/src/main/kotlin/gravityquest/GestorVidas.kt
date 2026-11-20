package gravityquest
import javafx.beans.property.IntegerProperty; import javafx.beans.property.SimpleIntegerProperty

interface IVidasListener {
    fun alCambiarVidas(vidasRestantes: Int, vidasMaximas: Int, fueDaño: Boolean)
}

interface IGestorVidasEstado {
    val maxVidas: Int
    val vidasProperty: IntegerProperty
    val vidasActuales: Int
        get() = vidasProperty.get()
    val sinVidas: Boolean
        get() = vidasActuales <= 0
    val vidasCompletas: Boolean
        get() = vidasActuales >= maxVidas
    fun calcularBonificacionVidas(): Int
    fun reiniciarVidas()
}

interface IGestorVidas : IGestorVidasEstado {
    fun perderVida(): Boolean
    fun recuperarVida(): Boolean
    fun agregarListener(listener: IVidasListener)
    fun eliminarListener(listener: IVidasListener)
}

class GestorVidas(
    override val maxVidas: Int = GameConfig.VIDAS_INICIALES
) : IGestorVidas {

    private val _vidasProperty: SimpleIntegerProperty = SimpleIntegerProperty(maxVidas)
    private val listeners: MutableList<IVidasListener> = mutableListOf()

    override val vidasProperty: IntegerProperty
        get() = _vidasProperty

    init {
        require(maxVidas > 0)
    }

    override fun perderVida(): Boolean {
        val previas = _vidasProperty.get()
        if (previas <= 0) return true
        return actualizarValor(previas - 1, true) <= 0
    }

    override fun recuperarVida(): Boolean {
        val previas = _vidasProperty.get()
        if (previas >= maxVidas) return false
        actualizarValor(previas + 1, false)
        return true
    }

    override fun reiniciarVidas() {
        if (_vidasProperty.get() != maxVidas) {
            actualizarValor(maxVidas, false)
        }
    }

    override fun calcularBonificacionVidas(): Int {
        return _vidasProperty.get() * GameConfig.BONO_POR_VIDA_RESTANTE
    }

    override fun agregarListener(listener: IVidasListener) {
        if (_vidasProperty.get() >= 0 && !listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    override fun eliminarListener(listener: IVidasListener) {
        if (_vidasProperty.get() >= 0) {
            listeners.remove(listener)
        }
    }

    private fun actualizarValor(nuevo: Int, fueDaño: Boolean): Int {
        _vidasProperty.set(nuevo)
        listeners.toList().forEach { it.alCambiarVidas(nuevo, maxVidas, fueDaño) }
        return nuevo
    }
}
