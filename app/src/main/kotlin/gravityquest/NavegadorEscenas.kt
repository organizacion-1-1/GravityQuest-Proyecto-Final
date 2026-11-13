package gravityquest
import gravityquest.models.*; import javafx.scene.Parent; import org.example.Juego.*
typealias IParent = Parent; internal typealias ctorRepoNav = GeneradorProblemas; internal typealias ctorCalcNav = CalculadoraFisica
internal typealias ctorGestorNav = GestorProgreso; internal typealias ctorVistaMenu = VistaMenu; internal typealias ctorVistaJuego = VistaJuego
internal typealias ctorVistaGameOver = VistaGameOver; internal typealias ctorVistaVictoria = VistaNivelCompletado

interface IEscenaConCicloDeVida {
    fun detenerSimulacionActiva()
    fun liberarRecursos()
}

interface INavegadorBase {
    val stage: IStage
    val repositorio: IRepositorioProblemas
    val calculadora: ICalculadoraFisica
    val gestorProgreso: IGestorProgreso
    val limpiezasEjecutadas: IInt
    fun detenerRecursosEscenaActual()
    fun cambiarEscena(
        raiz: IParent,
        ancho: IDouble = GameConfig.ANCHO_VENTANA,
        alto: IDouble = GameConfig.ALTO_VENTANA
    )
}

interface INavegadorEscenas : INavegadorBase {
    fun mostrarMenu()
    fun mostrarJuego(dificultad: Dificultad)
    fun mostrarGameOver(dificultad: Dificultad, estadisticas: EstadisticasPartida)
    fun mostrarNivelCompletado(
        dificultad: Dificultad,
        estadisticas: EstadisticasPartida,
        desbloqueoSiguienteNivel: IBoolean = false
    )
}

open class BaseNavegadorEscenas(
    override val stage: IStage,
    override val repositorio: IRepositorioProblemas = ctorRepoNav(),
    override val calculadora: ICalculadoraFisica = ctorCalcNav(),
    override val gestorProgreso: IGestorProgreso = ctorGestorNav(repositorio)
) : INavegadorBase {

    override var limpiezasEjecutadas: IInt = 0
        protected set

    override fun detenerRecursosEscenaActual() {
        val actual = stage.scene?.root
        if (actual is IEscenaConCicloDeVida) {
            actual.detenerSimulacionActiva()
            actual.liberarRecursos()
            limpiezasEjecutadas += 1
        }
    }

    override fun cambiarEscena(raiz: IParent, ancho: IDouble, alto: IDouble) {
        detenerRecursosEscenaActual()
        val escenaActual = stage.scene
        if (escenaActual == null) {
            val nueva = ctorScene(raiz, ancho, alto)
            asegurarHojaEstilos(nueva)
            stage.scene = nueva
        } else {
            escenaActual.root = raiz
            asegurarHojaEstilos(escenaActual)
        }
        FxAnimationUtils.animarEntrada(raiz, duracionMs = 260.0)
    }

    private fun asegurarHojaEstilos(escena: IScene) {
        val url = BaseNavegadorEscenas::class.java.getResource(GameConfig.RUTA_ESTILOS_CSS)?.toExternalForm()
        if (url != null && !escena.stylesheets.contains(url)) {
            escena.stylesheets.add(url)
        }
    }
}

class NavegadorEscenas(
    stage: IStage,
    repositorio: IRepositorioProblemas = ctorRepoNav(),
    calculadora: ICalculadoraFisica = ctorCalcNav(),
    gestorProgreso: IGestorProgreso = ctorGestorNav(repositorio)
) : BaseNavegadorEscenas(stage, repositorio, calculadora, gestorProgreso), INavegadorEscenas {

    override fun mostrarMenu() {
        val vista = FabricaVistasNavegacion.crearMenu(this, gestorProgreso)
        stage.title = "\u0047ravity\u0051uest \u2014 \u004den\u00fa \u0050rincipal"
        cambiarEscena(vista)
    }

    override fun mostrarJuego(dificultad: Dificultad) {
        if (!gestorProgreso.estaDesbloqueado(dificultad)) return
        val vista = FabricaVistasNavegacion.crearJuego(dificultad, this)
        stage.title = "\u0047ravity\u0051uest \u2014 \u004eivel ${dificultad.nombreMostrar}"
        cambiarEscena(vista)
    }

    override fun mostrarGameOver(dificultad: Dificultad, estadisticas: EstadisticasPartida) {
        val vista = FabricaVistasNavegacion.crearGameOver(this, dificultad, estadisticas)
        stage.title = "\u0047ravity\u0051uest \u2014 \u004d\u00edsi\u00f3n \u0046allida"
        cambiarEscena(vista)
    }

    override fun mostrarNivelCompletado(
        dificultad: Dificultad,
        estadisticas: EstadisticasPartida,
        desbloqueoSiguienteNivel: IBoolean
    ) {
        val vista = FabricaVistasNavegacion.crearVictoria(this, dificultad to desbloqueoSiguienteNivel, estadisticas)
        stage.title = "\u0047ravity\u0051uest \u2014 \u004eivel \u0043ompletado"
        cambiarEscena(vista)
    }
}

internal object FabricaVistasNavegacion {
    fun crearMenu(nav: INavegadorEscenas, gestor: IGestorProgreso): VistaMenu {
        return ctorVistaMenu(navegador = nav, gestorProgreso = gestor)
    }

    fun crearJuego(dificultad: Dificultad, nav: INavegadorEscenas): VistaJuego {
        return ctorVistaJuego(
            dificultad = dificultad,
            navegador = nav,
            gestorProgreso = nav.gestorProgreso,
            repositorio = nav.repositorio,
            calculadora = nav.calculadora
        )
    }

    fun crearGameOver(
        nav: INavegadorEscenas,
        dificultad: Dificultad,
        estadisticas: EstadisticasPartida
    ): VistaGameOver {
        return ctorVistaGameOver(
            dificultad = dificultad,
            estadisticas = estadisticas,
            alReintentar = {
                val destino = if (nav.gestorProgreso.estaDesbloqueado(dificultad)) dificultad else Dificultad.FACIL
                nav.gestorProgreso.iniciarNivel(destino, mezclarProblemas = false)
                nav.mostrarJuego(destino)
            },
            alVolverAlMenu = { nav.mostrarMenu() }
        )
    }

    fun crearVictoria(
        nav: INavegadorEscenas,
        parNivel: IParNivelFlag,
        estadisticas: EstadisticasPartida
    ): VistaNivelCompletado {
        val dificultad = parNivel.first
        return ctorVistaVictoria(
            dificultad = dificultad,
            estadisticas = estadisticas,
            desbloqueoSiguienteNivel = parNivel.second,
            alSiguienteNivel = { sig ->
                nav.gestorProgreso.iniciarNivel(sig, mezclarProblemas = false)
                nav.mostrarJuego(sig)
            },
            alReintentar = {
                nav.gestorProgreso.iniciarNivel(dificultad, mezclarProblemas = false)
                nav.mostrarJuego(dificultad)
            },
            alVolverAlMenu = { nav.mostrarMenu() }
        )
    }
}
