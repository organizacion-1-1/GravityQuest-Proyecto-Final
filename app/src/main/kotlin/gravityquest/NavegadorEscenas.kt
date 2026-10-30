package gravityquest

import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas
import gravityquest.models.Problema
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * ============================================================================
 * GravityQuest - Gestor de Navegación y Gestión de Ciclo de Vida de Escenas
 * (Issue #21 - Milestone 4: Optimización, SOLID, POO y DRY)
 * ============================================================================
 *
 * 1. Principio de Inversión de Dependencias (DIP):
 *    - Las pantallas y vistas no dependen de la implementación concreta del navegador
 *      ni manipulan directamente el [Stage] de JavaFX; dependen exclusivamente de
 *      este contrato [INavegadorEscenas].
 *
 * 2. Principio Abierto/Cerrado (OCP):
 *    - Provee el método extensible [cambiarEscena], permitiendo incorporar nuevas vistas
 *      sin modificar ni acoplar las vistas existentes.
 *
 * 3. Gestión Fina de Recursos y Memoria (Issue #21):
 *    - Garantiza que al cambiar de escena con [cambiarEscena], cualquier vista o motor
 *      activo que implemente [IRecursoLiberable] (como [VistaJuego] y su [MotorSimulacionCanvas])
 *      se detenga explícitamente (`stop()`) y desvincule sus listeners, previniendo
 *      fugas de memoria (*memory leaks*) y consumo fantasma de CPU en segundo plano.
 */
interface INavegadorEscenas {
    /** Navega a la pantalla del menú inicial. */
    fun irAMenu()

    /**
     * Navega a la pantalla de juego cargando la dificultad seleccionada.
     *
     * @param dificultad Dificultad del juego seleccionada por el usuario.
     */
    fun irAJuego(dificultad: Dificultad)

    /**
     * Navega a la pantalla de juego cargando directamente un problema físico concreto.
     *
     * @param problema Instancia del problema a cargar.
     */
    fun irAJuego(problema: Problema) {
        irAJuego(problema.dificultad)
    }

    /**
     * Registra un recurso o vista con ciclo de vida activo ([IRecursoLiberable]) para asegurar
     * su detención automática en el próximo cambio de escena (Issue #21).
     */
    fun registrarRecursoActivo(recurso: IRecursoLiberable) {}

    /**
     * Detiene explícitamente los temporizadores ([javafx.animation.AnimationTimer]) y libera
     * los listeners asociados a la escena actual antes de realizar una transición.
     */
    fun detenerRecursosEscenaActual() {
        (escenaActual as? IRecursoLiberable)?.liberarRecursos()
    }

    /**
     * Navega a la pantalla final de "Game Over" presentando las estadísticas del intento (Issue #20).
     *
     * @param dificultad Nivel de dificultad en el que ocurrió el Game Over.
     * @param estadisticas Objeto de valor inmutable con las estadísticas de la partida.
     */
    fun irAGameOver(
        dificultad: Dificultad,
        estadisticas: EstadisticasPartida = EstadisticasPartida(vidasRestantes = 0, dificultad = dificultad)
    ) {
        detenerRecursosEscenaActual()
        val vista = VistaGameOver(dificultad, estadisticas, this)
        cambiarEscena(vista.root, "${GameConfig.TITULO_APP} - Game Over")
    }

    /**
     * Navega a la pantalla triunfal de "¡Nivel Completado!" presentando recompensas y estadísticas (Issue #20).
     *
     * @param dificultadSuperada Nivel de dificultad superado exitosamente.
     * @param estadisticas Objeto de valor inmutable con las estadísticas y bonificaciones del nivel.
     * @param siguienteNivel Siguiente dificultad desbloqueada, o `null` si se completó el último nivel.
     */
    fun irANivelCompletado(
        dificultadSuperada: Dificultad,
        estadisticas: EstadisticasPartida = EstadisticasPartida(dificultad = dificultadSuperada),
        siguienteNivel: Dificultad? = dificultadSuperada.siguienteNivel
    ) {
        detenerRecursosEscenaActual()
        val vista = VistaNivelCompletado(dificultadSuperada, estadisticas, this, null, siguienteNivel)
        cambiarEscena(vista.root, "${GameConfig.TITULO_APP} - ¡Nivel Completado!")
    }

    /**
     * Cambia la escena activa del Stage por una nueva raíz visual, deteniendo previamente
     * los recursos y temporizadores de la escena saliente.
     *
     * @param nodo Nodo raíz que conformará la nueva vista.
     * @param titulo Título de la ventana para la nueva escena.
     */
    fun cambiarEscena(nodo: Parent, titulo: String = GameConfig.TITULO_APP)

    /** Nodo raíz visual actualmente presentado en pantalla. */
    val escenaActual: Parent? get() = null

    /** Título de la ventana actualmente establecido. */
    val tituloActual: String get() = ""
}

/**
 * Gestor centralizado de escenas para JavaFX en GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Centraliza el control de la ventana ([Stage]), la liberación segura de recursos
 * entre cambios de escena, la inyección cacheada de estilos CSS y la coordinación
 * del estado compartido de progreso entre pantallas.
 */
class NavegadorEscenas(
    val stage: Stage? = null,
    val repositorio: IRepositorioProblemas = GeneradorProblemas(),
    val calculadora: ICalculadoraFisica = CalculadoraFisica(),
    val gestorProgreso: IGestorProgreso = GestorProgreso(repositorio),
    val anchoPredeterminado: Double = GameConfig.ANCHO_VENTANA_DEFAULT,
    val altoPredeterminado: Double = GameConfig.ALTO_VENTANA_DEFAULT
) : INavegadorEscenas {

    /** Ruta del recurso de la hoja de estilos CSS común (referencia a [GameConfig]). */
    val rutaEstilos: String = GameConfig.RUTA_ESTILOS

    /** URL cacheada de la hoja de estilos para evitar búsquedas repetidas de classpath en cada transición. */
    private val cssUrlCacheada: String? by lazy {
        javaClass.getResource(rutaEstilos)?.toExternalForm()
    }

    /** Registro de recursos y motores activos pendientes de liberación al cambiar de escena. */
    private val recursosActivos: MutableSet<IRecursoLiberable> = LinkedHashSet()

    private var _escenaActual: Parent? = null
    override val escenaActual: Parent? get() = _escenaActual

    private var _tituloActual: String = ""
    override val tituloActual: String get() = _tituloActual

    /** Contador diagnóstico de limpiezas de recursos ejecutadas en transiciones de escena. */
    var limpiezasEjecutadas: Int = 0
        private set

    init {
        stage?.setOnHidden {
            detenerRecursosEscenaActual()
        }
    }

    override fun registrarRecursoActivo(recurso: IRecursoLiberable) {
        recursosActivos.add(recurso)
    }

    override fun detenerRecursosEscenaActual() {
        val escenaPrevia = _escenaActual
        if (escenaPrevia is IRecursoLiberable) {
            escenaPrevia.liberarRecursos()
            limpiezasEjecutadas += 1
        }

        if (recursosActivos.isNotEmpty()) {
            val copia = recursosActivos.toList()
            recursosActivos.clear()
            for (recurso in copia) {
                if (recurso !== escenaPrevia) {
                    recurso.liberarRecursos()
                    limpiezasEjecutadas += 1
                }
            }
        }
    }

    override fun irAMenu() {
        detenerRecursosEscenaActual()
        val vistaMenu = VistaMenu(this, gestorProgreso)
        cambiarEscena(vistaMenu.root, "${GameConfig.TITULO_APP} - Menú Principal")
    }

    override fun irAJuego(dificultad: Dificultad) {
        detenerRecursosEscenaActual()
        val problema = repositorio.obtenerPorDificultad(dificultad).firstOrNull()
            ?: repositorio.obtenerProblemaAleatorio(dificultad)
            ?: GeneradorProblemas.crearCatalogoBase().first { it.dificultad == dificultad }
        val vistaJuego = VistaJuego(
            dificultad = dificultad,
            navegador = this,
            repositorio = repositorio,
            calculadora = calculadora,
            gestorProgreso = gestorProgreso,
            stagePropietario = stage,
            problemaInicial = problema
        )
        cambiarEscena(vistaJuego.root, "${GameConfig.TITULO_APP} - Misión: ${dificultad.nombreVisible}")
    }

    override fun irAJuego(problema: Problema) {
        detenerRecursosEscenaActual()
        val vistaJuego = VistaJuego(
            dificultad = problema.dificultad,
            navegador = this,
            repositorio = repositorio,
            calculadora = calculadora,
            gestorProgreso = gestorProgreso,
            stagePropietario = stage,
            problemaInicial = problema
        )
        cambiarEscena(vistaJuego.root, "${GameConfig.TITULO_APP} - Misión: ${problema.dificultad.nombreVisible}")
    }

    override fun irAGameOver(dificultad: Dificultad, estadisticas: EstadisticasPartida) {
        detenerRecursosEscenaActual()
        val vistaGameOver = VistaGameOver(
            dificultad = dificultad,
            estadisticas = estadisticas,
            navegador = this,
            gestorProgreso = gestorProgreso
        )
        cambiarEscena(vistaGameOver.root, "${GameConfig.TITULO_APP} - Game Over")
    }

    override fun irANivelCompletado(
        dificultadSuperada: Dificultad,
        estadisticas: EstadisticasPartida,
        siguienteNivel: Dificultad?
    ) {
        detenerRecursosEscenaActual()
        val vistaVictoria = VistaNivelCompletado(
            dificultadSuperada = dificultadSuperada,
            estadisticas = estadisticas,
            navegador = this,
            gestorProgreso = gestorProgreso,
            siguienteNivel = siguienteNivel
        )
        cambiarEscena(vistaVictoria.root, "${GameConfig.TITULO_APP} - ¡Nivel Completado!")
    }

    override fun cambiarEscena(nodo: Parent, titulo: String) {
        val escenaPrevia = this._escenaActual
        if (escenaPrevia != null && escenaPrevia !== nodo) {
            if (escenaPrevia is IRecursoLiberable) {
                escenaPrevia.liberarRecursos()
                limpiezasEjecutadas += 1
            }
            val otrosRecursos = recursosActivos.filter { it !== nodo && it !== escenaPrevia }
            for (recurso in otrosRecursos) {
                recurso.liberarRecursos()
                limpiezasEjecutadas += 1
            }
            recursosActivos.removeAll { it !== nodo }
        }

        if (nodo is IRecursoLiberable) {
            recursosActivos.add(nodo)
        }

        this._escenaActual = nodo
        this._tituloActual = titulo

        val st = stage ?: return
        val sceneActual = st.scene
        if (sceneActual == null) {
            val nuevaScene = Scene(nodo, anchoPredeterminado, altoPredeterminado)
            aplicarEstilos(nuevaScene)
            st.scene = nuevaScene
        } else {
            sceneActual.root = nodo
            aplicarEstilos(sceneActual)
        }

        st.title = titulo
        if (!st.isShowing) {
            st.show()
        }
    }

    private fun aplicarEstilos(scene: Scene) {
        val cssResource = cssUrlCacheada
        if (cssResource != null && !scene.stylesheets.contains(cssResource)) {
            scene.stylesheets.add(cssResource)
        }
    }
}
