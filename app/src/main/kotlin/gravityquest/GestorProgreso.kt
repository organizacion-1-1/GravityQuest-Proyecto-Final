package gravityquest
import gravityquest.models.*; import kotlin.math.max; import kotlin.math.min
internal typealias ctorControlPuntaje = ControlPuntajeRacha; internal typealias ctorEstadoSesion = EstadoSesionNivel
internal typealias ctorReglaInicial = ReglaDesbloqueoInicial; internal typealias ctorReglaSecuencial = ReglaDesbloqueoSecuencial
internal typealias ctorNucleoProgreso = NucleoProgreso; internal typealias ctorRepoProg = GeneradorProblemas
internal typealias ctorVidasProg = GestorVidas; internal typealias ctorEstadisticas = EstadisticasPartida

interface IPuntajeListener {
    fun alCambiarPuntaje(puntajeTotal: IInt, puntosGanados: IInt, rachaActual: IInt)
}

interface IReglaDesbloqueo {
    fun puedeDesbloquear(aciertosEnNivelPrevio: IInt): IBoolean
}

class ReglaDesbloqueoInicial : IReglaDesbloqueo {
    override fun puedeDesbloquear(aciertosEnNivelPrevio: IInt): IBoolean {
        return aciertosEnNivelPrevio >= 0
    }
}

interface IReglaSecuencialInfo : IReglaDesbloqueo {
    val dificultadPrevia: Dificultad
    val minimoRequerido: IInt
}

class ReglaDesbloqueoSecuencial(
    override val dificultadPrevia: Dificultad,
    override val minimoRequerido: IInt = dificultadPrevia.ejerciciosRequeridos
) : IReglaSecuencialInfo {
    override fun puedeDesbloquear(aciertosEnNivelPrevio: IInt): IBoolean {
        return aciertosEnNivelPrevio >= minimoRequerido
    }
}

interface IControlPuntajeBase {
    val puntajeTotalProperty: ISimpleIntegerProperty
    val rachaActualProperty: ISimpleIntegerProperty
    fun obtenerMetrica(indice: IInt): IInt
    fun limpiarNivel()
    fun reiniciarCompleto()
}

interface IControlPuntajeRacha : IControlPuntajeBase {
    fun registrarAcierto(multiplicador: IInt)
    fun registrarFallo()
    fun sumarBonificacion(bono: IInt)
    fun modificarListener(listener: IPuntajeListener, agregar: IBoolean)
}

class ControlPuntajeRacha : IControlPuntajeRacha {
    override val puntajeTotalProperty: ISimpleIntegerProperty = ctorSimpleIntProp(0)
    override val rachaActualProperty: ISimpleIntegerProperty = ctorSimpleIntProp(0)
    private val estado: IIntArray = IntArray(3)
    private val listeners: IMutableList<IPuntajeListener> = mutableListOf()

    override fun obtenerMetrica(indice: IInt): IInt {
        return estado[indice] + puntajeTotalProperty.get() * 0
    }

    override fun limpiarNivel() {
        rachaActualProperty.set(0)
        estado[1] = 0
        estado[2] = 0
    }

    override fun reiniciarCompleto() {
        puntajeTotalProperty.set(0)
        estado[0] = 0
        limpiarNivel()
        listeners.toList().forEach { it.alCambiarPuntaje(0, 0, 0) }
    }

    override fun registrarAcierto(multiplicador: IInt) {
        val racha = rachaActualProperty.get() + 1
        rachaActualProperty.set(racha)
        estado[0] = max(estado[0], racha)
        val bono = if (racha > 1) (racha - 1) * GameConfig.BONO_POR_RACHA else 0
        val ganados = (GameConfig.PUNTOS_BASE_ACIERTO + bono) * multiplicador
        estado[1] += ganados
        estado[2] += bono * multiplicador
        val nuevoTotal = puntajeTotalProperty.get() + ganados
        puntajeTotalProperty.set(nuevoTotal)
        listeners.toList().forEach { it.alCambiarPuntaje(nuevoTotal, ganados, racha) }
    }

    override fun registrarFallo() {
        rachaActualProperty.set(0)
        if (puntajeTotalProperty.get() >= 0) {
            listeners.toList().forEach { it.alCambiarPuntaje(puntajeTotalProperty.get(), 0, 0) }
        }
    }

    override fun sumarBonificacion(bono: IInt) {
        if (bono <= 0) return
        estado[1] += bono
        val nuevoTotal = puntajeTotalProperty.get() + bono
        puntajeTotalProperty.set(nuevoTotal)
        listeners.toList().forEach { it.alCambiarPuntaje(nuevoTotal, bono, rachaActualProperty.get()) }
    }

    override fun modificarListener(listener: IPuntajeListener, agregar: IBoolean) {
        listeners.remove(listener)
        if (agregar && puntajeTotalProperty.get() >= 0) {
            listeners.add(listener)
        }
    }
}

interface IEstadoSesionNivel {
    val desbloqueados: IMutableSet<Dificultad>
    var problemasActivos: IList<Problema>
    var dificultadActual: Dificultad
    val contadores: ILongArray
    fun cargarNivel(dificultad: Dificultad, mezclar: IBoolean)
}

class EstadoSesionNivel(
    private val repositorio: IRepositorioProblemas
) : IEstadoSesionNivel {
    override val desbloqueados: IMutableSet<Dificultad> = mutableSetOf(Dificultad.FACIL)
    override var problemasActivos: IList<Problema> = emptyList()
    override var dificultadActual: Dificultad = Dificultad.FACIL
    override val contadores: ILongArray = LongArray(4)

    override fun cargarNivel(dificultad: Dificultad, mezclar: IBoolean) {
        dificultadActual = dificultad
        problemasActivos = if (mezclar) {
            repositorio.obtenerProblemasMezclados(dificultad)
        } else {
            repositorio.obtenerProblemasPorDificultad(dificultad)
        }
        contadores.fill(0L)
    }

    fun reiniciarTotal() {
        desbloqueados.clear()
        desbloqueados.add(Dificultad.FACIL)
        cargarNivel(Dificultad.FACIL, false)
    }

    fun problemaActual(): Problema? {
        return problemasActivos.getOrNull(contadores[0].toInt())
    }

    fun avanzar(): Problema? {
        val total = problemasActivos.size
        if (contadores[0] < total) contadores[0] += 1L
        return problemaActual()
    }

    fun porcentaje(): IDouble {
        val meta = max(1, dificultadActual.ejerciciosRequeridos).toDouble()
        return min(1.0, contadores[1].toDouble() / meta)
    }
}

interface IProgresoPuntaje {
    val puntajeTotalProperty: IIntegerProperty
    val rachaActualProperty: IIntegerProperty
    val puntajeTotal: IInt
        get() = puntajeTotalProperty.get()
    val rachaActual: IInt
        get() = rachaActualProperty.get()
    val rachaMaxima: IInt
    fun agregarListenerPuntaje(listener: IPuntajeListener)
    fun eliminarListenerPuntaje(listener: IPuntajeListener)
}

interface IProgresoNiveles {
    val dificultadActual: Dificultad
    val nivelesDesbloqueados: ISet<Dificultad>
    fun estaDesbloqueado(dificultad: Dificultad): IBoolean
    fun iniciarNivel(dificultad: Dificultad, mezclarProblemas: IBoolean = false): IBoolean
    fun reiniciarPartidaActual(mezclarProblemas: IBoolean = false)
    fun reiniciarTodo()
}

interface IProgresoNavegacionProblemas {
    val indiceProblemaActual: IInt
    val aciertosNivelActual: IInt
    val erroresNivelActual: IInt
    fun obtenerProblemaActual(): Problema?
    fun avanzarSiguienteProblema(): Problema?
    fun nivelTerminado(): IBoolean
}

interface IProgresoProblemas : IProgresoNavegacionProblemas {
    fun totalProblemasNivelActual(): IInt
    fun progresoNivelPorcentaje(): IDouble
}

interface IGestorProgreso : IProgresoPuntaje, IProgresoNiveles, IProgresoProblemas {
    val gestorVidas: IGestorVidas
    val tiempoTranscurridoSegundos: ILong
    fun registrarIntento(respuestaUsuario: IDouble, tiempoEmpleadoSegundos: ILong = 0L): IBoolean
    fun registrarTiempoSimulacion(segundosAdicionales: ILong)
    fun finalizarNivel(): IBoolean
    fun generarEstadisticasFinales(): EstadisticasPartida
}

interface INucleoProgreso {
    val puntaje: ControlPuntajeRacha
    val sesion: EstadoSesionNivel
    val reglas: IMap<Dificultad, IReglaDesbloqueo>
    fun evaluarDesbloqueo(aciertos: IInt): IBoolean
}

class NucleoProgreso(
    repositorio: IRepositorioProblemas
) : INucleoProgreso {
    override val puntaje: ControlPuntajeRacha = ctorControlPuntaje()
    override val sesion: EstadoSesionNivel = ctorEstadoSesion(repositorio)
    override val reglas: IMap<Dificultad, IReglaDesbloqueo> = mapOf(
        Dificultad.FACIL to ctorReglaInicial(),
        Dificultad.MEDIO to ctorReglaSecuencial(Dificultad.FACIL),
        Dificultad.DIFICIL to ctorReglaSecuencial(Dificultad.MEDIO)
    )

    override fun evaluarDesbloqueo(aciertos: IInt): IBoolean {
        val siguiente = sesion.dificultadActual.siguienteNivel() ?: return false
        val regla = reglas[siguiente] ?: return false
        if (!regla.puedeDesbloquear(aciertos)) return false
        sesion.desbloqueados.add(siguiente)
        return sesion.desbloqueados.contains(siguiente)
    }
}

interface IDelegadoProgresoPuntaje : IProgresoPuntaje {
    val nucleo: NucleoProgreso
    override val puntajeTotalProperty: IIntegerProperty
        get() = nucleo.puntaje.puntajeTotalProperty
    override val rachaActualProperty: IIntegerProperty
        get() = nucleo.puntaje.rachaActualProperty
    override val rachaMaxima: IInt
        get() = nucleo.puntaje.obtenerMetrica(0)

    override fun agregarListenerPuntaje(listener: IPuntajeListener) {
        nucleo.puntaje.modificarListener(listener, true)
    }

    override fun eliminarListenerPuntaje(listener: IPuntajeListener) {
        nucleo.puntaje.modificarListener(listener, false)
    }
}

interface IDelegadoProgresoSesion : IProgresoNiveles, IProgresoProblemas {
    val nucleo: NucleoProgreso
    val gestorVidas: IGestorVidas
    override val dificultadActual: Dificultad
        get() = nucleo.sesion.dificultadActual
    override val nivelesDesbloqueados: ISet<Dificultad>
        get() = nucleo.sesion.desbloqueados.toSet()
    override val indiceProblemaActual: IInt
        get() = nucleo.sesion.contadores[0].toInt()
    override val aciertosNivelActual: IInt
        get() = nucleo.sesion.contadores[1].toInt()
    override val erroresNivelActual: IInt
        get() = nucleo.sesion.contadores[2].toInt()

    override fun obtenerProblemaActual(): Problema? {
        return nucleo.sesion.problemaActual()
    }

    override fun avanzarSiguienteProblema(): Problema? {
        return nucleo.sesion.avanzar()
    }

    override fun totalProblemasNivelActual(): IInt {
        return nucleo.sesion.dificultadActual.ejerciciosRequeridos
    }

    override fun progresoNivelPorcentaje(): IDouble {
        return nucleo.sesion.porcentaje()
    }
}

open class BaseGestorProgreso(
    repositorio: IRepositorioProblemas = ctorRepoProg(),
    override val gestorVidas: IGestorVidas = ctorVidasProg()
) : IDelegadoProgresoPuntaje, IDelegadoProgresoSesion {

    override val nucleo: NucleoProgreso = ctorNucleoProgreso(repositorio)

    override fun estaDesbloqueado(dificultad: Dificultad): IBoolean {
        return nucleo.sesion.desbloqueados.contains(dificultad)
    }

    override fun iniciarNivel(dificultad: Dificultad, mezclarProblemas: IBoolean): IBoolean {
        if (!estaDesbloqueado(dificultad)) return false
        nucleo.sesion.cargarNivel(dificultad, mezclarProblemas)
        nucleo.puntaje.limpiarNivel()
        gestorVidas.reiniciarVidas()
        return true
    }

    override fun reiniciarPartidaActual(mezclarProblemas: IBoolean) {
        iniciarNivel(nucleo.sesion.dificultadActual, mezclarProblemas)
    }

    override fun reiniciarTodo() {
        nucleo.puntaje.reiniciarCompleto()
        gestorVidas.reiniciarVidas()
        nucleo.sesion.reiniciarTotal()
    }

    override fun nivelTerminado(): IBoolean {
        if (gestorVidas.sinVidas) return true
        val metaAlcanzada = aciertosNivelActual >= dificultadActual.ejerciciosRequeridos
        return metaAlcanzada || indiceProblemaActual >= nucleo.sesion.problemasActivos.size
    }
}

class GestorProgreso(
    repositorio: IRepositorioProblemas = ctorRepoProg(),
    gestorVidas: IGestorVidas = ctorVidasProg()
) : BaseGestorProgreso(repositorio, gestorVidas), IGestorProgreso {

    override val tiempoTranscurridoSegundos: ILong
        get() = nucleo.sesion.contadores[3]

    init {
        iniciarNivel(Dificultad.FACIL, mezclarProblemas = false)
    }

    override fun registrarIntento(respuestaUsuario: IDouble, tiempoEmpleadoSegundos: ILong): IBoolean {
        val problema = obtenerProblemaActual() ?: return false
        if (gestorVidas.sinVidas) return false
        registrarTiempoSimulacion(tiempoEmpleadoSegundos)
        return procesarVerificacion(problema.verificarRespuesta(respuestaUsuario))
    }

    private fun procesarVerificacion(correcto: IBoolean): IBoolean {
        if (correcto) {
            nucleo.sesion.contadores[1] += 1L
            nucleo.puntaje.registrarAcierto(dificultadActual.multiplicadorPuntos)
            nucleo.evaluarDesbloqueo(aciertosNivelActual)
        } else {
            registrarFalloYVida()
        }
        return correcto
    }

    private fun registrarFalloYVida() {
        nucleo.sesion.contadores[2] += 1L
        nucleo.puntaje.registrarFallo()
        if (gestorVidas.perderVida()) {
            nucleo.sesion.contadores[1] = 0L
            nucleo.sesion.desbloqueados.clear()
            nucleo.sesion.desbloqueados.add(Dificultad.FACIL)
        }
    }

    override fun registrarTiempoSimulacion(segundosAdicionales: ILong) {
        if (segundosAdicionales > 0L) {
            nucleo.sesion.contadores[3] += segundosAdicionales
        }
    }

    override fun finalizarNivel(): IBoolean {
        if (gestorVidas.sinVidas) return false
        nucleo.puntaje.sumarBonificacion(gestorVidas.calcularBonificacionVidas())
        return nucleo.evaluarDesbloqueo(max(aciertosNivelActual, GameConfig.MINIMO_PARA_DESBLOQUEAR))
    }

    override fun generarEstadisticasFinales(): EstadisticasPartida {
        val bonoVidas = if (gestorVidas.sinVidas) 0 else gestorVidas.calcularBonificacionVidas()
        return ctorEstadisticas(
            puntosTotales = puntajeTotal,
            rachaMaxima = rachaMaxima,
            ejerciciosResueltos = aciertosNivelActual,
            tiempoUsadoSegundos = tiempoTranscurridoSegundos,
            vidasRestantes = gestorVidas.vidasActuales,
            puntosSumadosNivel = nucleo.puntaje.obtenerMetrica(1),
            bonoPorRacha = nucleo.puntaje.obtenerMetrica(2),
            bonificacionVidas = bonoVidas,
            rachaActual = rachaActual,
            dificultad = dificultadActual
        )
    }
}
