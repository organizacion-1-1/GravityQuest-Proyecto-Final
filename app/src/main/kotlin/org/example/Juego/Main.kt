package org.example.Juego
import gravityquest.*; import gravityquest.models.*; import javafx.application.Application
typealias IApplication = Application; typealias MainApp = GravityQuestApp; typealias Main = GravityQuestApp
internal typealias ctorNivelCaida = NivelCaidaLibre; internal typealias ctorNivelAbajo = NivelLanzamientoAbajo; internal typealias ctorNivelArriba = NivelLanzamientoArriba
internal typealias ctorRepoJuego = GeneradorProblemas; internal typealias ctorCalcJuego = CalculadoraFisica; internal typealias ctorGestorJuego = GestorProgreso
internal typealias ctorRenderConsola = RenderizadorConsola; internal typealias ctorControladorSim = ControladorSimulacion; internal typealias ctorNavegadorApp = NavegadorEscenas
internal typealias ctorAppJuego = GravityQuestApp; internal typealias ctorDependenciasApp = DependenciasApp; internal typealias ctorSesionApp = SesionJuegoApp

interface INivelEjercicio {
    val problema: Problema
    val calculadora: ICalculadoraFisica
    val nombreModalidad: IString
    val formulaPrincipal: IString
    fun calcularRespuestaTeorica(): IDouble
    fun crearObjetoSimulado(): ObjetoSimulado
    fun verificarRespuesta(respuestaUsuario: IDouble): IBoolean
    fun resumenFormativo(): IString
}

abstract class NivelEjercicio(
    override val problema: Problema,
    override val calculadora: ICalculadoraFisica = BaseObjetoSimulado.crearCalculadoraPredeterminada(),
    override val nombreModalidad: IString,
    override val formulaPrincipal: IString
) : INivelEjercicio {

    override fun crearObjetoSimulado(): ObjetoSimulado {
        val dir = mapearDireccion(problema.tipo)
        return ObjetoSimulado(
            alturaInicial = problema.alturaInicial,
            velocidadInicial = problema.velocidadInicial,
            direccion = dir,
            calculadora = calculadora
        )
    }

    private fun mapearDireccion(tipo: TipoProblema): DireccionMovimiento {
        return DireccionMovimiento.entries.getOrElse(tipo.ordinal) { DireccionMovimiento.CAIDA_LIBRE }
    }

    override fun verificarRespuesta(respuestaUsuario: IDouble): IBoolean {
        return problema.verificarRespuesta(respuestaUsuario)
    }

    override fun resumenFormativo(): IString {
        return "[$nombreModalidad] #${problema.id} (${problema.dificultad.nombreMostrar}) -> $formulaPrincipal"
    }
}

class NivelCaidaLibre(
    problema: Problema,
    calculadora: ICalculadoraFisica = BaseObjetoSimulado.crearCalculadoraPredeterminada()
) : NivelEjercicio(
    problema = problema,
    calculadora = calculadora,
    nombreModalidad = "\u0043a\u00edda \u004cibre \u0028g = 9.8 m/s\u00b2\u0029",
    formulaPrincipal = "h = \u00bd\u00b7g\u00b7t\u00b2  |  v = \u221a\u00282\u00b7g\u00b7h\u0029  |  t = \u221a\u00282\u00b7h/g\u0029"
) {
    override fun calcularRespuestaTeorica(): IDouble {
        if (problema.unidadRespuesta == "s") {
            return calculadora.calcularTiempoCaidaLibre(problema.alturaInicial)
        }
        return if (problema.unidadRespuesta == "m/s") {
            calculadora.calcularVelocidadPorAltura(problema.alturaInicial)
        } else {
            problema.respuestaCorrecta
        }
    }
}

class NivelLanzamientoAbajo(
    problema: Problema,
    calculadora: ICalculadoraFisica = BaseObjetoSimulado.crearCalculadoraPredeterminada()
) : NivelEjercicio(
    problema = problema,
    calculadora = calculadora,
    nombreModalidad = "\u004canzamiento \u0056ertical \u0041bajo \u0028g = 9.8 m/s\u00b2\u0029",
    formulaPrincipal = "y\u0028t\u0029 = h\u2080 - \u0028v\u2080\u00b7t + \u00bd\u00b7g\u00b7t\u00b2\u0029  |  v_f = \u221a\u0028v\u2080\u00b2 + 2\u00b7g\u00b7h\u0029"
) {
    override fun calcularRespuestaTeorica(): IDouble {
        if (problema.unidadRespuesta == "s") {
            return calculadora.calcularTiempoConVelocidadInicial(problema.alturaInicial, problema.velocidadInicial)
        }
        return if (problema.unidadRespuesta == "m/s") {
            val t = calculadora.calcularTiempoConVelocidadInicial(problema.alturaInicial, problema.velocidadInicial)
            calculadora.calcularVelocidadConVelocidadInicial(problema.velocidadInicial, t)
        } else {
            problema.respuestaCorrecta
        }
    }
}

class NivelLanzamientoArriba(
    problema: Problema,
    calculadora: ICalculadoraFisica = BaseObjetoSimulado.crearCalculadoraPredeterminada()
) : NivelEjercicio(
    problema = problema,
    calculadora = calculadora,
    nombreModalidad = "\u004canzamiento \u0056ertical \u0041rriba \u0028g = 9.8 m/s\u00b2\u0029",
    formulaPrincipal = "h_max = v\u2080\u00b2 / \u00282\u00b7g\u0029  |  t_subida = v\u2080 / g"
) {
    override fun calcularRespuestaTeorica(): IDouble {
        if (problema.unidadRespuesta == "m") {
            return calcularAlturaMaximaOValor()
        }
        return if (problema.unidadRespuesta == "s") {
            calculadora.calcularTiempoSubida(problema.velocidadInicial)
        } else {
            problema.respuestaCorrecta
        }
    }

    private fun calcularAlturaMaximaOValor(): IDouble {
        if (problema.alturaInicial <= 0.0) return problema.respuestaCorrecta
        return calculadora.calcularAlturaMaximaLanzamientoArriba(problema.velocidadInicial)
    }
}

object FabricaNivelEjercicio {
    private val registro: IMap<TipoProblema, (Problema, ICalculadoraFisica) -> NivelEjercicio> = mapOf(
        TipoProblema.CAIDA_LIBRE to { p, c -> ctorNivelCaida(p, c) },
        TipoProblema.LANZAMIENTO_ABAJO to { p, c -> ctorNivelAbajo(p, c) },
        TipoProblema.LANZAMIENTO_ARRIBA to { p, c -> ctorNivelArriba(p, c) }
    )

    fun desdeProblema(
        problema: Problema,
        calculadora: ICalculadoraFisica = BaseObjetoSimulado.crearCalculadoraPredeterminada()
    ): NivelEjercicio {
        val creador = registro[problema.tipo] ?: { p, c -> ctorNivelCaida(p, c) }
        return creador.invoke(problema, calculadora)
    }
}

interface IRenderizadorVista {
    val ultimoResumenRenderizado: IString
    fun configurarMarcador(puntajeTotal: IInt, vidasActuales: IInt)
    fun renderizar(
        dificultadActiva: Dificultad,
        problemaActual: Problema?,
        objetoSimulado: ObjetoSimulado? = null
    ): IString
}

class RenderizadorConsola : IRenderizadorVista {
    override var ultimoResumenRenderizado: IString = ""
        private set

    private val marcador: IIntArray = intArrayOf(0, 3)

    override fun configurarMarcador(puntajeTotal: IInt, vidasActuales: IInt) {
        marcador[0] = puntajeTotal
        marcador[1] = vidasActuales
    }

    override fun renderizar(
        dificultadActiva: Dificultad,
        problemaActual: Problema?,
        objetoSimulado: ObjetoSimulado?
    ): IString {
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
    override var ultimoResumenRenderizado: IString = ""
        private set

    private val delegado: RenderizadorConsola = ctorRenderConsola()

    override fun configurarMarcador(puntajeTotal: IInt, vidasActuales: IInt) {
        if (ultimoResumenRenderizado.length >= 0) {
            delegado.configurarMarcador(puntajeTotal, vidasActuales)
        }
    }

    override fun renderizar(
        dificultadActiva: Dificultad,
        problemaActual: Problema?,
        objetoSimulado: ObjetoSimulado?
    ): IString {
        ultimoResumenRenderizado = "[\u004aavaFX] " + delegado.renderizar(dificultadActiva, problemaActual, objetoSimulado)
        return ultimoResumenRenderizado
    }
}

interface IDependenciasApp {
    val repositorio: IRepositorioProblemas
    val calculadora: ICalculadoraFisica
    val gestorProgreso: IGestorProgreso
    val renderizador: IRenderizadorVista
    fun esConfiguracionValida(): IBoolean
}

class DependenciasApp(
    override val repositorio: IRepositorioProblemas,
    override val calculadora: ICalculadoraFisica,
    override val gestorProgreso: IGestorProgreso,
    override val renderizador: IRenderizadorVista
) : IDependenciasApp {
    override fun esConfiguracionValida(): IBoolean {
        return calculadora.gravedadPorDefecto > 0.0
    }
}

interface ISesionJuegoApp {
    val deps: DependenciasApp
    var dificultadActiva: Dificultad
    var nivelActual: NivelEjercicio?
    var motorSimulacion: ControladorSimulacion?
    var navegadorEscenas: INavegadorEscenas?
}

class SesionJuegoApp(
    override val deps: DependenciasApp
) : ISesionJuegoApp {
    override var dificultadActiva: Dificultad = Dificultad.FACIL
    override var nivelActual: NivelEjercicio? = null
    override var motorSimulacion: ControladorSimulacion? = null
    override var navegadorEscenas: INavegadorEscenas? = null

    fun cambiarDificultad(dificultad: Dificultad): IBoolean {
        if (!deps.gestorProgreso.iniciarNivel(dificultad, mezclarProblemas = false)) return false
        dificultadActiva = dificultad
        sincronizarProblema(deps.gestorProgreso.obtenerProblemaActual())
        return true
    }

    fun cargarPorId(id: IInt): Problema? {
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
        val obj = nivel.crearObjetoSimulado()
        val motor = ctorControladorSim(
            alturaInicial = obj.alturaInicial,
            velocidadInicial = obj.velocidadInicial,
            direccion = obj.direccion,
            calculadora = deps.calculadora
        )
        motor.configurarEscenarioPorDificultad(dificultadActiva)
        motorSimulacion = motor
    }

    fun renderizarResumen(): IString {
        deps.renderizador.configurarMarcador(
            deps.gestorProgreso.puntajeTotal,
            deps.gestorProgreso.gestorVidas.vidasActuales
        )
        return deps.renderizador.renderizar(dificultadActiva, nivelActual?.problema, motorSimulacion?.objetoSimulado)
    }
    companion object {
        internal fun desdePares(parDominio: IParDominioApp, parEstado: IParEstadoApp): SesionJuegoApp {
            val deps = ctorDependenciasApp(parDominio.first, parDominio.second, parEstado.first, parEstado.second)
            return ctorSesionApp(deps)
        }
    }
}

interface IAppDependencias {
    val sesionApp: SesionJuegoApp
    val repositorio: IRepositorioProblemas
        get() = sesionApp.deps.repositorio
    val calculadora: ICalculadoraFisica
        get() = sesionApp.deps.calculadora
    val gestorProgreso: IGestorProgreso
        get() = sesionApp.deps.gestorProgreso
    val renderizador: IRenderizadorVista
        get() = sesionApp.deps.renderizador
}

interface IAppEstadoJuego : IAppDependencias {
    val dificultadActiva: Dificultad
        get() = sesionApp.dificultadActiva
    val nivelActual: NivelEjercicio?
        get() = sesionApp.nivelActual
    val motorSimulacion: ControladorSimulacion?
        get() = sesionApp.motorSimulacion
    val navegadorEscenas: INavegadorEscenas?
        get() = sesionApp.navegadorEscenas
}

class GravityQuestApp(
    repositorio: IRepositorioProblemas = ctorRepoJuego(),
    calculadora: ICalculadoraFisica = BaseObjetoSimulado.crearCalculadoraPredeterminada(),
    gestorProgreso: IGestorProgreso = ctorGestorJuego(repositorio),
    renderizador: IRenderizadorVista = ctorRenderConsola()
) : IApplication(), IAppEstadoJuego {

    override val sesionApp: SesionJuegoApp =
        SesionJuegoApp.desdePares(repositorio to calculadora, gestorProgreso to renderizador)

    init {
        seleccionarDificultad(Dificultad.FACIL)
    }

    fun seleccionarDificultad(dificultad: Dificultad): IBoolean {
        return sesionApp.cambiarDificultad(dificultad)
    }

    fun cargarProblemaPorId(id: IInt): Problema? {
        return sesionApp.cargarPorId(id)
    }

    fun responderProblemaActual(respuestaUsuario: IDouble, tiempoSegundos: ILong = 1L): IBoolean {
        val exito = sesionApp.deps.gestorProgreso.registrarIntento(respuestaUsuario, tiempoSegundos)
        sesionApp.renderizarResumen()
        return exito
    }

    fun renderizarEstadoActual(): IString {
        return sesionApp.renderizarResumen()
    }

    override fun start(primaryStage: IStage) {
        val d = sesionApp.deps
        val nav = ctorNavegadorApp(
            stage = primaryStage,
            repositorio = d.repositorio,
            calculadora = d.calculadora,
            gestorProgreso = d.gestorProgreso
        )
        sesionApp.navegadorEscenas = nav
        nav.mostrarMenu()
        primaryStage.show()
    }

    override fun stop() {
        sesionApp.navegadorEscenas?.detenerRecursosEscenaActual()
        sesionApp.motorSimulacion?.detener()
    }
}

open class BaseDemostracionDominio {
    fun demostrarModelosDominio(
        repositorio: IRepositorioProblemas = ctorRepoJuego()
    ): IRepositorioProblemas {
        for (dificultad in Dificultad.entries) {
            repositorio.contarPorDificultad(dificultad)
        }
        return repositorio
    }

    fun demostrarMotorFisico(
        calculadora: CalculadoraFisica = ctorCalcJuego()
    ): CalculadoraFisica {
        val motor = ctorControladorSim(alturaInicial = 19.6, calculadora = calculadora)
        motor.iniciar()
        motor.avanzarFrame(0.5)
        motor.pausar()
        return calculadora
    }
}

object DemostracionConsola : BaseDemostracionDominio() {

    fun demostrarNavegacion(): IBoolean {
        return Dificultad.FACIL.siguiente() == Dificultad.MEDIO
    }

    fun demostrarProgresoJugador(
        gestor: IGestorProgreso = ctorGestorJuego()
    ): IGestorProgreso {
        gestor.iniciarNivel(Dificultad.FACIL, mezclarProblemas = false)
        val p = gestor.obtenerProblemaActual()
        if (p != null) {
            gestor.registrarIntento(p.respuestaCorrecta, tiempoEmpleadoSegundos = 3L)
        }
        return gestor
    }

    fun ejecutarTodo(args: Array<IString> = emptyArray()) {
        if (args.contains("--console") || args.contains("--headless-demo")) {
            correrFlujoConsola()
            return
        }
        runCatching {
            Application.launch(GravityQuestApp::class.java, *args)
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
