package gravityquest
import gravityquest.models.Dificultad; import gravityquest.models.GeneradorProblemas; import gravityquest.models.IRepositorioProblemas
import javafx.scene.Parent; import javafx.scene.Scene; import javafx.stage.Stage
import org.example.Juego.CalculadoraFisica; import org.example.Juego.ICalculadoraFisica
internal typealias ctorRepoNav = GeneradorProblemas; internal typealias ctorCalcNav = CalculadoraFisica
internal typealias ctorGestorNav = GestorProgreso; internal typealias ctorVistaMenu = VistaMenu; internal typealias ctorVistaJuego = VistaJuego
internal typealias ctorVistaGameOver = VistaGameOver; internal typealias ctorVistaVictoria = VistaNivelCompletado

interface IEscenaConCicloDeVida {
    fun detenerSimulacionActiva()
    fun liberarRecursos()
}

internal object ProveedorDominioNavegador {
    fun crearRepo(): IRepositorioProblemas {
        return ctorRepoNav()
    }

    fun crearCalc(): ICalculadoraFisica {
        return ctorCalcNav()
    }
}

internal object ProveedorGestorNavegador {
    fun crearGestor(r: IRepositorioProblemas): IGestorProgreso {
        return ctorGestorNav(r)
    }
}

interface INavegadorCicloEscena {
    val stage: Stage
    val limpiezasEjecutadas: Int
    fun detenerRecursosEscenaActual()
    fun cambiarEscena(
        raiz: Parent,
        ancho: Double = 1024.0,
        alto: Double = 720.0
    )
}

interface INavegadorDependencias : INavegadorCicloEscena {
    val repositorio: IRepositorioProblemas
    val calculadora: ICalculadoraFisica
}

interface INavegadorBase : INavegadorDependencias {
    val gestorProgreso: IGestorProgreso
}

interface INavegadorEscenas : INavegadorBase {
    fun mostrarMenu()
    fun mostrarJuego(dificultad: Dificultad)
    fun mostrarGameOver(dificultad: Dificultad, estadisticas: IEstadisticasPartida)
    fun mostrarNivelCompletado(
        dificultad: Dificultad,
        estadisticas: IEstadisticasPartida,
        desbloqueoSiguienteNivel: Boolean = false
    )
}

open class BaseNavegadorEscenas(
    override val stage: Stage,
    override val repositorio: IRepositorioProblemas = ProveedorDominioNavegador.crearRepo(),
    override val calculadora: ICalculadoraFisica = ProveedorDominioNavegador.crearCalc()
) : INavegadorDependencias {

    override var limpiezasEjecutadas: Int = 0
        protected set

    override fun detenerRecursosEscenaActual() {
        val actual = stage.scene?.root
        if (actual is IEscenaConCicloDeVida) {
            actual.detenerSimulacionActiva()
            actual.liberarRecursos()
            limpiezasEjecutadas += 1
        }
    }

    override fun cambiarEscena(raiz: Parent, ancho: Double, alto: Double) {
        detenerRecursosEscenaActual()
        val escenaActual = stage.scene
        if (escenaActual == null) {
            val nueva = Scene(raiz, ancho, alto)
            asegurarHojaEstilos(nueva)
            stage.scene = nueva
        } else {
            escenaActual.root = raiz
            asegurarHojaEstilos(escenaActual)
        }
        AnimadorUI.animarEntrada(raiz, duracionMs = 260.0)
    }

    private fun asegurarHojaEstilos(escena: Scene) {
        val url = BaseNavegadorEscenas::class.java.getResource("/assets/style.css")?.toExternalForm()
        if (url != null && !escena.stylesheets.contains(url)) {
            escena.stylesheets.add(url)
        }
    }
}

interface IDelegadoVistasPrincipales : INavegadorEscenas {
    override fun mostrarMenu() {
        val vista = FabricaVistasPrincipales.crearMenu(this)
        stage.title = "\u0047ravity\u0051uest \u2014 \u004den\u00fa \u0050rincipal"
        cambiarEscena(vista)
    }

    override fun mostrarJuego(dificultad: Dificultad) {
        if (!gestorProgreso.estaDesbloqueado(dificultad)) return
        val vista = FabricaVistasPrincipales.crearJuego(dificultad, this)
        stage.title = "\u0047ravity\u0051uest \u2014 \u004eivel ${dificultad.nombreMostrar}"
        cambiarEscena(vista)
    }
}

interface IDelegadoVistasNavegacion : IDelegadoVistasPrincipales {
    override fun mostrarGameOver(dificultad: Dificultad, estadisticas: IEstadisticasPartida) {
        val vista = FabricaVistaDerrota.crear(this, dificultad, estadisticas)
        stage.title = "\u0047ravity\u0051uest \u2014 \u004d\u00edsi\u00f3n \u0046allida"
        cambiarEscena(vista)
    }

    override fun mostrarNivelCompletado(
        dificultad: Dificultad,
        estadisticas: IEstadisticasPartida,
        desbloqueoSiguienteNivel: Boolean
    ) {
        val vista = FabricaVistaVictoria.crear(this, estadisticas, desbloqueoSiguienteNivel)
        stage.title = "\u0047ravity\u0051uest \u2014 \u004eivel \u0043ompletado"
        cambiarEscena(vista)
    }
}

class NavegadorEscenas(
    stage: Stage,
    repositorio: IRepositorioProblemas = ProveedorDominioNavegador.crearRepo(),
    calculadora: ICalculadoraFisica = ProveedorDominioNavegador.crearCalc(),
    override val gestorProgreso: IGestorProgreso = ProveedorGestorNavegador.crearGestor(repositorio)
) : BaseNavegadorEscenas(stage, repositorio, calculadora), IDelegadoVistasNavegacion {
    fun estaListo(): Boolean {
        return limpiezasEjecutadas >= 0
    }
}

internal object FabricaVistasPrincipales {
    fun crearMenu(nav: INavegadorEscenas): Parent {
        return ctorVistaMenu(navegador = nav, gestorProgreso = nav.gestorProgreso)
    }

    fun crearJuego(dificultad: Dificultad, nav: INavegadorEscenas): Parent {
        return ctorVistaJuego(
            dificultad = dificultad,
            navegador = nav,
            gestorProgreso = nav.gestorProgreso
        )
    }
}

internal object FabricaVistaDerrota {
    fun crear(
        nav: INavegadorEscenas,
        dificultad: Dificultad,
        estadisticas: IEstadisticasPartida
    ): Parent {
        return ctorVistaGameOver(
            dificultad = dificultad,
            estadisticas = estadisticas,
            alReintentar = { reintentarSegunEstado(nav, dificultad) },
            alVolverAlMenu = { nav.mostrarMenu() }
        )
    }

    private fun reintentarSegunEstado(nav: INavegadorEscenas, dif: Dificultad) {
        val destino = if (nav.gestorProgreso.estaDesbloqueado(dif)) dif else Dificultad.FACIL
        nav.gestorProgreso.iniciarNivel(destino, mezclarProblemas = false)
        nav.mostrarJuego(destino)
    }
}

internal object FabricaVistaVictoria {
    fun crear(
        nav: INavegadorEscenas,
        estadisticas: IEstadisticasPartida,
        desbloqueo: Boolean
    ): Parent {
        val dificultad = estadisticas.dificultad
        return ctorVistaVictoria(
            dificultad = dificultad,
            estadisticas = estadisticas,
            desbloqueoSiguienteNivel = desbloqueo,
            alSiguienteNivel = { sig -> iniciarYMostrar(nav, sig) },
            alReintentar = { iniciarYMostrar(nav, dificultad) },
            alVolverAlMenu = { nav.mostrarMenu() }
        )
    }

    private fun iniciarYMostrar(nav: INavegadorEscenas, dif: Dificultad) {
        nav.gestorProgreso.iniciarNivel(dif, mezclarProblemas = false)
        nav.mostrarJuego(dif)
    }
}
