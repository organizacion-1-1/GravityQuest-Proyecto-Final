package org.example.Juego
import gravityquest.ControladorSimulacion; import gravityquest.GestorProgreso; import gravityquest.IGestorProgreso
import gravityquest.IMotorSimulacionCanvas; import gravityquest.INavegadorEscenas; import gravityquest.NavegadorEscenas
import gravityquest.models.Dificultad; import gravityquest.models.GeneradorProblemas; import gravityquest.models.IRepositorioProblemas; import gravityquest.models.Problema
import javafx.application.Application; import javafx.stage.Stage
typealias MainApp = GravityQuestApp; typealias Main = GravityQuestApp
internal typealias ctorNivelCaida = NivelCaidaLibre; internal typealias ctorRepoJuego = GeneradorProblemas
internal typealias ctorCalcJuego = CalculadoraFisica; internal typealias ctorGestorJuego = GestorProgreso
internal typealias ctorRenderConsola = RenderizadorConsola; internal typealias ctorControladorSim = ControladorSimulacion
internal typealias ctorNavegadorApp = NavegadorEscenas; internal typealias ctorAppJuego = GravityQuestApp
internal typealias ctorDependenciasApp = DependenciasApp; internal typealias ctorSesionApp = SesionJuegoApp
internal typealias ctorObjSimMain = ObjetoSimulado

internal object ProveedorObjetoNivel {
    fun crearObjeto(problema: Problema, calculadora: ICalculadoraFisica): ctorObjSimMain {
        val dir = DireccionMovimiento.entries.getOrElse(problema.tipo.ordinal) { DireccionMovimiento.CAIDA_LIBRE }
        return ctorObjSimMain(
            alturaInicial = problema.alturaInicial,
            velocidadInicial = problema.velocidadInicial,
            direccion = dir,
            calculadora = calculadora
        )
    }
}

interface INivelEjercicio {
    val problema: Problema
    val calculadora: ICalculadoraFisica
    val nombreModalidad: String
    val formulaPrincipal: String
    fun calcularRespuestaTeorica(): Double
    fun crearObjetoSimulado(): IObjetoSimulado
    fun verificarRespuesta(respuestaUsuario: Double): Boolean
    fun resumenFormativo(): String
}

abstract class NivelEjercicio(
    override val problema: Problema,
    override val calculadora: ICalculadoraFisica = ICalculadoraFisica.porDefecto(),
    override val nombreModalidad: String,
    override val formulaPrincipal: String
) : INivelEjercicio {

    override fun crearObjetoSimulado(): IObjetoSimulado {
        return ProveedorObjetoNivel.crearObjeto(problema, calculadora)
    }

    override fun verificarRespuesta(respuestaUsuario: Double): Boolean {
        return problema.verificarRespuesta(respuestaUsuario)
    }

    override fun resumenFormativo(): String {
        return "[$nombreModalidad] #${problema.id} (${problema.dificultad.nombreMostrar}) -> $formulaPrincipal"
    }
}

class NivelCaidaLibre(
    problema: Problema,
    calculadora: ICalculadoraFisica = ICalculadoraFisica.porDefecto()
) : NivelEjercicio(
    problema = problema,
    calculadora = calculadora,
    nombreModalidad = problema.tipo.etiqueta,
    formulaPrincipal = problema.tipo.formulaReferencia
) {
    override fun calcularRespuestaTeorica(): Double {
        if (problema.tipo.ordinal == 0) return calcularCaidaLibre()
        if (problema.tipo.ordinal == 1) return calcularLanzamientoAbajo()
        return calcularLanzamientoArriba()
    }

    private fun calcularCaidaLibre(): Double {
        if (problema.unidadRespuesta == "s") return calculadora.calcularTiempoCaidaLibre(problema.alturaInicial)
        if (problema.unidadRespuesta == "m/s") return calculadora.calcularVelocidadPorAltura(problema.alturaInicial)
        return problema.respuestaCorrecta
    }

    private fun calcularLanzamientoAbajo(): Double {
        val t = calculadora.calcularTiempoConVelocidadInicial(problema.alturaInicial, problema.velocidadInicial)
        if (problema.unidadRespuesta == "s") return t
        if (problema.unidadRespuesta == "m/s") return calculadora.calcularVelocidadConVelocidadInicial(problema.velocidadInicial, t)
        return problema.respuestaCorrecta
    }

    private fun calcularLanzamientoArriba(): Double {
        if (problema.unidadRespuesta == "m" && problema.alturaInicial > 0.0) {
            return calculadora.calcularAlturaMaximaLanzamientoArriba(problema.velocidadInicial)
        }
        if (problema.unidadRespuesta == "s") return calculadora.calcularTiempoSubida(problema.velocidadInicial)
        return problema.respuestaCorrecta
    }
}

object FabricaNivelEjercicio {
    fun desdeProblema(
        problema: Problema,
        calculadora: ICalculadoraFisica = ICalculadoraFisica.porDefecto()
    ): NivelEjercicio {
        return ctorNivelCaida(problema, calculadora)
    }
}

interface IRenderizadorVista {
    val ultimoResumenRenderizado: String
    fun configurarMarcador(puntajeTotal: Int, vidasActuales: Int)
    fun renderizar(
        dificultadActiva: Dificultad,
        problemaActual: Problema?,
        objetoSimulado: IObjetoSimulado? = null
    ): String
}

class RenderizadorConsola : IRenderizadorVista {
    override var ultimoResumenRenderizado: String = ""
        private set

    private val marcador: IntArray = intArrayOf(0, 3)

    override fun configurarMarcador(puntajeTotal: Int, vidasActuales: Int) {
        marcador[0] = puntajeTotal
        marcador[1] = vidasActuales
    }

    override fun renderizar(
        dificultadActiva: Dificultad,
        problemaActual: Problema?,
        objetoSimulado: IObjetoSimulado?
    ): String {
        val probTxt = problemaActual?.let { "#${it.id} (${it.incognita})" } ?: "\u0053in problema"
        val objTxt = objetoSimulado?.let {
            "h=${"%.2f".format(it.alturaActual)}m, v=${"%.2f".format(it.velocidadActual)}m/s"
        } ?: "\u0053in simulaci\u00f3n"
        ultimoResumenRenderizado =
            "\u004eivel=${dificultadActiva.nombreMostrar} | \u0050roblema=$probTxt | \u0045stado=$objTxt | " +
            "\u0050untos=${marcador[0]} | \u0056idas=${marcador[1]}"
        return ultimoResumenRenderizado
    }
}

class RenderizadorJavaFx : IRenderizadorVista {
    override var ultimoResumenRenderizado: String = ""
        private set

    private val delegado: IRenderizadorVista = ctorRenderConsola()

    override fun configurarMarcador(puntajeTotal: Int, vidasActuales: Int) {
        if (ultimoResumenRenderizado.length >= 0) {
            delegado.configurarMarcador(puntajeTotal, vidasActuales)
        }
    }

    override fun renderizar(
        dificultadActiva: Dificultad,
        problemaActual: Problema?,
        objetoSimulado: IObjetoSimulado?
    ): String {
        ultimoResumenRenderizado = "[\u004aavaFX] " + delegado.renderizar(dificultadActiva, problemaActual, objetoSimulado)
        return ultimoResumenRenderizado
    }
}

interface IDependenciasApp {
    val repositorio: IRepositorioProblemas
    val calculadora: ICalculadoraFisica
    val gestorProgreso: IGestorProgreso
    val renderizador: IRenderizadorVista
    fun esConfiguracionValida(): Boolean
}

class DependenciasApp(
    override val repositorio: IRepositorioProblemas,
    override val calculadora: ICalculadoraFisica,
    override val gestorProgreso: IGestorProgreso,
    override val renderizador: IRenderizadorVista
) : IDependenciasApp {
    override fun esConfiguracionValida(): Boolean {
        return calculadora.gravedadPorDefecto > 0.0
    }
}

internal object ProveedorServiciosApp {
    fun crearRepo(): IRepositorioProblemas {
        return ctorRepoJuego()
    }

    fun crearGestor(r: IRepositorioProblemas = crearRepo()): IGestorProgreso {
        return ctorGestorJuego(r)
    }
}

internal object ProveedorRenderApp {
    fun crearRender(): IRenderizadorVista {
        return ctorRenderConsola()
    }
}

internal object CreadorMotorSesion {
    fun crearMotor(nivel: NivelEjercicio, dificultad: Dificultad): ctorControladorSim {
        val obj = nivel.crearObjetoSimulado()
        val motor = ctorControladorSim(
            alturaInicial = obj.alturaInicial,
            velocidadInicial = obj.velocidadInicial,
            direccion = obj.direccion,
            calculadora = nivel.calculadora
        )
        motor.configurarEscenarioPorDificultad(dificultad)
        return motor
    }
}

internal object LanzadorVentanaApp {
    fun abrir(sesion: ctorSesionApp, stage: Stage) {
        val d = sesion.deps
        val nav = ctorNavegadorApp(stage, d.repositorio, d.calculadora, d.gestorProgreso)
        sesion.asignarNavegador(nav)
        nav.mostrarMenu()
        stage.show()
    }
}

interface ISesionEstadoBase {
    val deps: IDependenciasApp
    val dificultadActiva: Dificultad
    val nivelActual: NivelEjercicio?
}

interface ISesionJuegoApp : ISesionEstadoBase {
    val motorSimulacion: IMotorSimulacionCanvas?
    val navegadorEscenas: INavegadorEscenas?
}

open class BaseSesionJuegoApp(
    override val deps: IDependenciasApp
) : ISesionJuegoApp {
    override var dificultadActiva: Dificultad = Dificultad.FACIL
        protected set
    override var nivelActual: NivelEjercicio? = null
        protected set
    override var motorSimulacion: IMotorSimulacionCanvas? = null
        protected set
    override var navegadorEscenas: INavegadorEscenas? = null
        protected set

    fun asignarNavegador(nav: INavegadorEscenas) {
        navegadorEscenas = nav
    }

    fun detenerComponentes() {
        navegadorEscenas?.detenerRecursosEscenaActual()
        motorSimulacion?.detener()
    }

    fun renderizarResumen(): String {
        deps.renderizador.configurarMarcador(
            deps.gestorProgreso.puntajeTotal,
            deps.gestorProgreso.gestorVidas.vidasActuales
        )
        return deps.renderizador.renderizar(dificultadActiva, nivelActual?.problema, motorSimulacion?.objetoSimulado)
    }
}

class SesionJuegoApp(
    deps: IDependenciasApp
) : BaseSesionJuegoApp(deps) {

    init {
        cambiarDificultad(Dificultad.FACIL)
    }

    fun cambiarDificultad(dificultad: Dificultad): Boolean {
        if (!deps.gestorProgreso.iniciarNivel(dificultad, mezclarProblemas = false)) return false
        dificultadActiva = dificultad
        sincronizarProblema(deps.gestorProgreso.obtenerProblemaActual())
        return true
    }

    fun cargarPorId(id: Int): Problema? {
        val encontrado = deps.repositorio.obtenerProblemaPorId(id) ?: return null
        dificultadActiva = encontrado.dificultad
        sincronizarProblema(encontrado)
        return encontrado
    }

    fun sincronizarProblema(problema: Problema?) {
        if (problema == null) {
            nivelActual = null
            motorSimulacion = null
            return
        }
        val nivel = FabricaNivelEjercicio.desdeProblema(problema, deps.calculadora)
        nivelActual = nivel
        motorSimulacion = CreadorMotorSesion.crearMotor(nivel, dificultadActiva)
    }
}

interface IAppDependencias {
    val sesionApp: ctorSesionApp
    val repositorio: IRepositorioProblemas
        get() = sesionApp.deps.repositorio
    val calculadora: ICalculadoraFisica
        get() = sesionApp.deps.calculadora
}

interface IAppServicios : IAppDependencias {
    val gestorProgreso: IGestorProgreso
        get() = sesionApp.deps.gestorProgreso
    val renderizador: IRenderizadorVista
        get() = sesionApp.deps.renderizador

    fun seleccionarDificultad(dificultad: Dificultad): Boolean {
        return sesionApp.cambiarDificultad(dificultad)
    }

    fun cargarProblemaPorId(id: Int): Problema? {
        return sesionApp.cargarPorId(id)
    }
}

interface IAppEstadoJuego : IAppServicios {
    val dificultadActiva: Dificultad
        get() = sesionApp.dificultadActiva
    val nivelActual: NivelEjercicio?
        get() = sesionApp.nivelActual
    val motorSimulacion: IMotorSimulacionCanvas?
        get() = sesionApp.motorSimulacion
    val navegadorEscenas: INavegadorEscenas?
        get() = sesionApp.navegadorEscenas

    fun responderProblemaActual(respuestaUsuario: Double, tiempoSegundos: Long = 1L): Boolean {
        val exito = sesionApp.deps.gestorProgreso.registrarIntento(respuestaUsuario, tiempoSegundos)
        sesionApp.renderizarResumen()
        return exito
    }

    fun renderizarEstadoActual(): String {
        return sesionApp.renderizarResumen()
    }

    fun abrirVentanaPrincipal(stage: Stage) {
        LanzadorVentanaApp.abrir(sesionApp, stage)
    }
}

class GravityQuestApp(
    repositorio: IRepositorioProblemas = ProveedorServiciosApp.crearRepo(),
    calculadora: ICalculadoraFisica = ICalculadoraFisica.porDefecto(),
    gestorProgreso: IGestorProgreso = ProveedorServiciosApp.crearGestor(repositorio),
    renderizador: IRenderizadorVista = ProveedorRenderApp.crearRender()
) : Application(), IAppEstadoJuego {

    override val sesionApp: ctorSesionApp =
        ctorSesionApp(ctorDependenciasApp(repositorio, calculadora, gestorProgreso, renderizador))

    override fun start(primaryStage: Stage) {
        abrirVentanaPrincipal(primaryStage)
    }

    override fun stop() {
        sesionApp.detenerComponentes()
    }
}

open class BaseDemostracionDominio {
    fun demostrarModelosDominio(
        repositorio: IRepositorioProblemas = ProveedorServiciosApp.crearRepo()
    ): IRepositorioProblemas {
        for (dificultad in Dificultad.entries) {
            repositorio.contarPorDificultad(dificultad)
        }
        return repositorio
    }

    fun demostrarProgresoJugador(
        gestor: IGestorProgreso = ProveedorServiciosApp.crearGestor()
    ): IGestorProgreso {
        gestor.iniciarNivel(Dificultad.FACIL, mezclarProblemas = false)
        val p = gestor.obtenerProblemaActual()
        if (p != null) {
            gestor.registrarIntento(p.respuestaCorrecta, tiempoEmpleadoSegundos = 3L)
        }
        return gestor
    }
}

object DemostracionConsola : BaseDemostracionDominio() {

    fun demostrarNavegacion(): Boolean {
        return Dificultad.FACIL.siguiente() == Dificultad.MEDIO
    }

    fun demostrarMotorFisico(
        calculadora: ICalculadoraFisica = ICalculadoraFisica.porDefecto()
    ): ICalculadoraFisica {
        val motor = ctorControladorSim(alturaInicial = 19.6, calculadora = calculadora)
        motor.iniciar()
        motor.avanzarFrame(0.5)
        motor.pausar()
        return calculadora
    }

    fun ejecutarTodo(args: Array<String> = emptyArray()) {
        if (args.contains("--console") || args.contains("--headless-demo")) {
            correrFlujoConsola()
            return
        }
        runCatching {
            Application.launch(ctorAppJuego::class.java, *args)
        }.onFailure {
            correrFlujoConsola()
        }
    }

    private fun correrFlujoConsola() {
        val repo = demostrarModelosDominio()
        val calc = demostrarMotorFisico()
        demostrarNavegacion()
        val gestor = demostrarProgresoJugador()
        val app = ctorAppJuego(repositorio = repo, calculadora = calc, gestorProgreso = gestor)
        app.renderizarEstadoActual()
    }
}
