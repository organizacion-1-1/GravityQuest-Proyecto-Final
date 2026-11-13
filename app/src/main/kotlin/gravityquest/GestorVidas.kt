package gravityquest
import javafx.beans.property.IntegerProperty; import javafx.beans.property.SimpleIntegerProperty
typealias IIntegerProperty = IntegerProperty; typealias ISimpleIntegerProperty = SimpleIntegerProperty
internal typealias ctorSimpleIntProp = SimpleIntegerProperty

interface IVidasListener {
    fun alCambiarVidas(vidasRestantes: IInt, vidasMaximas: IInt, fueDaño: IBoolean)
}

interface IGestorVidasEstado {
    val maxVidas: IInt
    val vidasProperty: IIntegerProperty
    val vidasActuales: IInt
        get() = vidasProperty.get()
    val sinVidas: IBoolean
        get() = vidasActuales <= 0
    val vidasCompletas: IBoolean
        get() = vidasActuales >= maxVidas
    fun calcularBonificacionVidas(): IInt
    fun reiniciarVidas()
}

interface IGestorVidas : IGestorVidasEstado {
    fun perderVida(): IBoolean
    fun recuperarVida(): IBoolean
    fun agregarListener(listener: IVidasListener)
    fun eliminarListener(listener: IVidasListener)
}

class GestorVidas(
    override val maxVidas: IInt = GameConfig.VIDAS_INICIALES
) : IGestorVidas {

    private val _vidasProperty: ISimpleIntegerProperty = ctorSimpleIntProp(maxVidas)
    private val listeners: IMutableList<IVidasListener> = mutableListOf()

    override val vidasProperty: IIntegerProperty
        get() = _vidasProperty

    init {
        require(maxVidas > 0)
    }

    override fun perderVida(): IBoolean {
        val previas = _vidasProperty.get()
        if (previas <= 0) return true
        return actualizarValor(previas - 1, true) <= 0
    }

    override fun recuperarVida(): IBoolean {
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

    override fun calcularBonificacionVidas(): IInt {
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

    private fun actualizarValor(nuevo: IInt, fueDaño: IBoolean): IInt {
        _vidasProperty.set(nuevo)
        listeners.toList().forEach { it.alCambiarVidas(nuevo, maxVidas, fueDaño) }
        return nuevo
    }
}
