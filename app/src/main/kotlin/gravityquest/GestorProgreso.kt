package gravityquest
import gravityquest.models.Dificultad; import gravityquest.models.IRepositorioProblemas; import gravityquest.models.Problema
import javafx.beans.property.IntegerProperty; import javafx.beans.property.SimpleIntegerProperty; import kotlin.math.max; import kotlin.math.min
internal typealias ctorControlPuntaje = ControlPuntajeRacha; internal typealias ctorEstadoSesion = EstadoSesionNivel
internal typealias ctorReglaInicial = ReglaDesbloqueoInicial; internal typealias ctorReglaSecuencial = ReglaDesbloqueoSecuencial
internal typealias ctorNucleoProgreso = NucleoProgreso; internal typealias ctorRepoProg = gravityquest.models.GeneradorProblemas
internal typealias ctorVidasProg = GestorVidas; internal typealias ctorEstadisticas = EstadisticasPartida; internal typealias ctorGestorProg = GestorProgreso

interface IPuntajeListener {
    fun alCambiarPuntaje(puntajeTotal: Int, puntosGanados: Int, rachaActual: Int)
}

interface IReglaDesbloqueo {
    fun puedeDesbloquear(aciertosEnNivelPrevio: Int): Boolean
}

class ReglaDesbloqueoInicial : IReglaDesbloqueo {
    override fun puedeDesbloquear(aciertosEnNivelPrevio: Int): Boolean {
        return aciertosEnNivelPrevio >= 0
    }
}

interface IReglaSecuencialInfo : IReglaDesbloqueo {
    val dificultadPrevia: Dificultad
    val minimoRequerido: Int
}

class ReglaDesbloqueoSecuencial(
    override val dificultadPrevia: Dificultad,
    override val minimoRequerido: Int = dificultadPrevia.ejerciciosRequeridos
) : IReglaSecuencialInfo {
    override fun puedeDesbloquear(aciertosEnNivelPrevio: Int): Boolean {
        return aciertosEnNivelPrevio >= minimoRequerido
    }
}

interface IControlPuntajeBase {
    val puntajeTotalProperty: SimpleIntegerProperty
    val rachaActualProperty: SimpleIntegerProperty
    fun obtenerMetrica(indice: Int): Int
    fun limpiarNivel()
    fun reiniciarCompleto()
}

interface IControlPuntajeRacha : IControlPuntajeBase {
    fun registrarAcierto(multiplicador: Int)
    fun registrarFallo()
    fun sumarBonificacion(bono: Int)
    fun modificarListener(listener: IPuntajeListener, agregar: Boolean)
}

class ControlPuntajeRacha : IControlPuntajeRacha {
    override val puntajeTotalProperty: SimpleIntegerProperty = SimpleIntegerProperty(0)
    override val rachaActualProperty: SimpleIntegerProperty = SimpleIntegerProperty(0)
    private val estado: IntArray = IntArray(3)
    private val listeners: MutableList<IPuntajeListener> = mutableListOf()

    override fun obtenerMetrica(indice: Int): Int {
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

    override fun registrarAcierto(multiplicador: Int) {
        val racha = rachaActualProperty.get() + 1
        rachaActualProperty.set(racha)
        estado[0] = max(estado[0], racha)
        val bono = if (racha > 1) (racha - 1) * 25 else 0
        val ganados = (100 + bono) * multiplicador
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

    override fun sumarBonificacion(bono: Int) {
        if (bono <= 0) return
        estado[1] += bono
        val nuevoTotal = puntajeTotalProperty.get() + bono
        puntajeTotalProperty.set(nuevoTotal)
        listeners.toList().forEach { it.alCambiarPuntaje(nuevoTotal, bono, rachaActualProperty.get()) }
    }

    override fun modificarListener(listener: IPuntajeListener, agregar: Boolean) {
        listeners.remove(listener)
        if (agregar && puntajeTotalProperty.get() >= 0) {
            listeners.add(listener)
        }
    }
}

interface IEstadoSesionNivel {
    val desbloqueados: MutableSet<Dificultad>
    val problemasActivos: List<Problema>
    val dificultadActual: Dificultad
    val contadores: LongArray
    fun cargarNivel(dificultad: Dificultad, mezclar: Boolean)
}

class EstadoSesionNivel(
    private val repositorio: IRepositorioProblemas
) : IEstadoSesionNivel {
    override val desbloqueados: MutableSet<Dificultad> = mutableSetOf(Dificultad.FACIL)
    override var problemasActivos: List<Problema> = emptyList()
        private set
    override var dificultadActual: Dificultad = Dificultad.FACIL
        private set
    override val contadores: LongArray = LongArray(4)

    override fun cargarNivel(dificultad: Dificultad, mezclar: Boolean) {
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

    fun porcentaje(): Double {
        val meta = max(1, dificultadActual.ejerciciosRequeridos).toDouble()
        return min(1.0, contadores[1].toDouble() / meta)
    }
}

interface IProgresoPuntaje {
    val puntajeTotalProperty: IntegerProperty
    val rachaActualProperty: IntegerProperty
    val puntajeTotal: Int
        get() = puntajeTotalProperty.get()
    val rachaActual: Int
        get() = rachaActualProperty.get()
    val rachaMaxima: Int
    fun agregarListenerPuntaje(listener: IPuntajeListener)
    fun eliminarListenerPuntaje(listener: IPuntajeListener)
}

interface IProgresoNiveles {
    val dificultadActual: Dificultad
    val nivelesDesbloqueados: Set<Dificultad>
    fun estaDesbloqueado(dificultad: Dificultad): Boolean
    fun iniciarNivel(dificultad: Dificultad, mezclarProblemas: Boolean = false): Boolean
    fun reiniciarPartidaActual(mezclarProblemas: Boolean = false)
    fun reiniciarTodo()
}

interface IProgresoNavegacionProblemas {
    val indiceProblemaActual: Int
    val aciertosNivelActual: Int
    val erroresNivelActual: Int
    fun obtenerProblemaActual(): Problema?
    fun avanzarSiguienteProblema(): Problema?
    fun nivelTerminado(): Boolean
}

interface IProgresoProblemas : IProgresoNavegacionProblemas {
    fun totalProblemasNivelActual(): Int
    fun progresoNivelPorcentaje(): Double
}

interface IGestorProgreso : IProgresoPuntaje, IProgresoNiveles, IProgresoProblemas {
    val gestorVidas: IGestorVidas
    val tiempoTranscurridoSegundos: Long
    fun registrarIntento(respuestaUsuario: Double, tiempoEmpleadoSegundos: Long = 0L): Boolean
    fun registrarTiempoSimulacion(segundosAdicionales: Long)
    fun finalizarNivel(): Boolean
    fun generarEstadisticasFinales(): ctorEstadisticas
}

internal object FabricaReglasDesbloqueo {
    fun crearMapa(): Map<Dificultad, IReglaDesbloqueo> {
        return mapOf(
            Dificultad.FACIL to ctorReglaInicial(),
            Dificultad.MEDIO to ctorReglaSecuencial(Dificultad.FACIL),
            Dificultad.DIFICIL to ctorReglaSecuencial(Dificultad.MEDIO)
        )
    }
}

interface INucleoProgreso {
    val puntaje: ctorControlPuntaje
    val sesion: ctorEstadoSesion
    fun evaluarDesbloqueo(aciertos: Int): Boolean
}

class NucleoProgreso(
    repositorio: IRepositorioProblemas
) : INucleoProgreso {
    override val puntaje: ctorControlPuntaje = ctorControlPuntaje()
    override val sesion: ctorEstadoSesion = ctorEstadoSesion(repositorio)
    private val reglas = FabricaReglasDesbloqueo.crearMapa()

    override fun evaluarDesbloqueo(aciertos: Int): Boolean {
        val siguiente = sesion.dificultadActual.siguienteNivel() ?: return false
        val regla = reglas[siguiente] ?: return false
        if (!regla.puedeDesbloquear(aciertos)) return false
        sesion.desbloqueados.add(siguiente)
        return sesion.desbloqueados.contains(siguiente)
    }
}

interface IDelegadoProgresoPuntaje : IProgresoPuntaje {
    val nucleo: ctorNucleoProgreso
    override val puntajeTotalProperty: IntegerProperty
        get() = nucleo.puntaje.puntajeTotalProperty
    override val rachaActualProperty: IntegerProperty
        get() = nucleo.puntaje.rachaActualProperty
    override val rachaMaxima: Int
        get() = nucleo.puntaje.obtenerMetrica(0)

    override fun agregarListenerPuntaje(listener: IPuntajeListener) {
        nucleo.puntaje.modificarListener(listener, true)
    }

    override fun eliminarListenerPuntaje(listener: IPuntajeListener) {
        nucleo.puntaje.modificarListener(listener, false)
    }
}

interface IDelegadoProgresoSesion : IProgresoNiveles, IProgresoProblemas {
    val nucleo: ctorNucleoProgreso
    val gestorVidas: IGestorVidas
    override val dificultadActual: Dificultad
        get() = nucleo.sesion.dificultadActual
    override val nivelesDesbloqueados: Set<Dificultad>
        get() = nucleo.sesion.desbloqueados.toSet()
    override val indiceProblemaActual: Int
        get() = nucleo.sesion.contadores[0].toInt()
    override val aciertosNivelActual: Int
        get() = nucleo.sesion.contadores[1].toInt()
    override val erroresNivelActual: Int
        get() = nucleo.sesion.contadores[2].toInt()

    override fun obtenerProblemaActual(): Problema? {
        return nucleo.sesion.problemaActual()
    }

    override fun avanzarSiguienteProblema(): Problema? {
        return nucleo.sesion.avanzar()
    }

    override fun totalProblemasNivelActual(): Int {
        return nucleo.sesion.dificultadActual.ejerciciosRequeridos
    }

    override fun progresoNivelPorcentaje(): Double {
        return nucleo.sesion.porcentaje()
    }
}

open class BaseGestorProgreso(
    repositorio: IRepositorioProblemas,
    override val gestorVidas: IGestorVidas
) : IDelegadoProgresoPuntaje, IDelegadoProgresoSesion {

    override val nucleo: ctorNucleoProgreso = ctorNucleoProgreso(repositorio)

    override fun estaDesbloqueado(dificultad: Dificultad): Boolean {
        return nucleo.sesion.desbloqueados.contains(dificultad)
    }

    override fun iniciarNivel(dificultad: Dificultad, mezclarProblemas: Boolean): Boolean {
        if (!estaDesbloqueado(dificultad)) return false
        nucleo.sesion.cargarNivel(dificultad, mezclarProblemas)
        nucleo.puntaje.limpiarNivel()
        gestorVidas.reiniciarVidas()
        return true
    }

    override fun reiniciarPartidaActual(mezclarProblemas: Boolean) {
        iniciarNivel(nucleo.sesion.dificultadActual, mezclarProblemas)
    }

    override fun reiniciarTodo() {
        nucleo.puntaje.reiniciarCompleto()
        gestorVidas.reiniciarVidas()
        nucleo.sesion.reiniciarTotal()
    }

    override fun nivelTerminado(): Boolean {
        if (gestorVidas.sinVidas) return true
        val metaAlcanzada = aciertosNivelActual >= dificultadActual.ejerciciosRequeridos
        return metaAlcanzada || indiceProblemaActual >= nucleo.sesion.problemasActivos.size
    }
}

internal object ProveedorColaboradoresProgreso {
    fun crearRepo(): IRepositorioProblemas {
        return ctorRepoProg()
    }

    fun crearVidas(): IGestorVidas {
        return ctorVidasProg()
    }
}

internal object FabricaEstadisticasProgreso {
    fun construir(g: IGestorProgreso, n: ctorNucleoProgreso): ctorEstadisticas {
        val bonoVidas = if (g.gestorVidas.sinVidas) 0 else g.gestorVidas.calcularBonificacionVidas()
        return ctorEstadisticas(
            puntosTotales = g.puntajeTotal,
            rachaMaxima = g.rachaMaxima,
            ejerciciosResueltos = g.aciertosNivelActual,
            tiempoUsadoSegundos = g.tiempoTranscurridoSegundos,
            vidasRestantes = g.gestorVidas.vidasActuales,
            puntosSumadosNivel = n.puntaje.obtenerMetrica(1),
            bonoPorRacha = n.puntaje.obtenerMetrica(2),
            bonificacionVidas = bonoVidas,
            rachaActual = g.rachaActual,
            dificultad = g.dificultadActual
        )
    }
}

interface IDelegadoCierreProgreso : IGestorProgreso {
    val nucleo: ctorNucleoProgreso

    override fun finalizarNivel(): Boolean {
        if (gestorVidas.sinVidas) return false
        nucleo.puntaje.sumarBonificacion(gestorVidas.calcularBonificacionVidas())
        return nucleo.evaluarDesbloqueo(max(aciertosNivelActual, dificultadActual.ejerciciosRequeridos))
    }

    override fun generarEstadisticasFinales(): ctorEstadisticas {
        return FabricaEstadisticasProgreso.construir(this, nucleo)
    }
}

class GestorProgreso(
    repositorio: IRepositorioProblemas = ProveedorColaboradoresProgreso.crearRepo(),
    gestorVidas: IGestorVidas = ProveedorColaboradoresProgreso.crearVidas()
) : BaseGestorProgreso(repositorio, gestorVidas), IDelegadoCierreProgreso {

    override val tiempoTranscurridoSegundos: Long
        get() = nucleo.sesion.contadores[3]

    init {
        iniciarNivel(Dificultad.FACIL, mezclarProblemas = false)
    }

    override fun registrarIntento(respuestaUsuario: Double, tiempoEmpleadoSegundos: Long): Boolean {
        val problema = obtenerProblemaActual() ?: return false
        if (gestorVidas.sinVidas) return false
        registrarTiempoSimulacion(tiempoEmpleadoSegundos)
        return procesarVerificacion(problema.verificarRespuesta(respuestaUsuario))
    }

    private fun procesarVerificacion(correcto: Boolean): Boolean {
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

    override fun registrarTiempoSimulacion(segundosAdicionales: Long) {
        if (segundosAdicionales > 0L) {
            nucleo.sesion.contadores[3] += segundosAdicionales
        }
    }
}
