package gravityquest

import gravityquest.models.Dificultad
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import java.util.Locale
import kotlin.math.abs

/**
 * ============================================================================
 * GravityQuest - Motor de Renderizado y Escenarios Polimórficos (SRP, OCP & DRY)
 * ============================================================================
 *
 * 1. Single Responsibility Principle (SRP):
 *    - Encapsula todo el dibujo, interpolación visual y pintado del [Canvas] dentro
 *      de la jerarquía [RenderizadorSimulacion] ([RenderizadorCanvas] / [SimulacionCanvasView]).
 *    - [VistaJuego] solo aloja el nodo [Canvas] en su layout y delega el pintado frame por frame.
 *
 * 2. Open/Closed Principle (OCP) - Fondos y Escenarios Polimórficos:
 *    - Define la abstracción [EscenarioRenderizable] y el enum polimórfico [TipoEscenario]
 *      para que cada nivel ([Dificultad.FACIL], [Dificultad.MEDIO], [Dificultad.DIFICIL])
 *      renderice su propio escenario ilustrado (Acantilado Lunar, Edificio de Pruebas,
 *      Plataforma de Lanzamiento con Cohete) sin estructuras condicionales rígidas.
 *
 * 3. Principio DRY (Don't Repeat Yourself):
 *    - Reutiliza [GraficosUtils] para gradientes, cuadrículas de medición física,
 *      vectores de velocidad dinámicos ($v$), estela de trayectoria y partículas.
 */

/**
 * Contrato polimórfico para renderizar fondos y escenarios temáticos por nivel (OCP).
 */
interface EscenarioRenderizable {
    /** Nombre descriptivo del escenario físico. */
    val nombreEscenario: String

    /**
     * Dibuja el fondo estilizado, iluminación indirecta y estructuras del escenario en el [Canvas].
     */
    fun renderizarFondo(
        gc: GraphicsContext,
        ancho: Double,
        alto: Double,
        inicioY: Double,
        sueloY: Double,
        alturaMaximaMetros: Double
    )
}

/**
 * Implementaciones polimórficas de escenarios para cada dificultad del juego (OCP).
 */
enum class TipoEscenario(
    override val nombreEscenario: String
) : EscenarioRenderizable {

    /**
     * Escenario Nivel Fácil (Caída Libre): Acantilado Lunar y Torre de Gravedad Cero.
     */
    ACANTILADO_LUNAR("Acantilado Lunar") {
        override fun renderizarFondo(
            gc: GraphicsContext,
            ancho: Double,
            alto: Double,
            inicioY: Double,
            sueloY: Double,
            alturaMaximaMetros: Double
        ) {
            // 1. Gradiente espacial nocturno profundo
            gc.fill = GraficosUtils.crearGradienteVertical("#070b19", "#0f172a", "#1e293b")
            gc.fillRect(0.0, 0.0, ancho, alto)

            // 2. Nebulosa cian tenue e iluminación indirecta
            gc.fill = Color.rgb(56, 189, 248, 0.06)
            gc.fillOval(ancho * 0.15, alto * 0.08, ancho * 0.7, alto * 0.45)

            // 3. Campo de estrellas fijas deterministas
            dibujarEstrellas(gc, ancho, sueloY)

            // 4. Silueta del acantilado lunar lateral izquierdo y brazo de suelta en inicioY
            gc.fill = Color.web("#1e293b")
            gc.fillPolygon(
                doubleArrayOf(2.0, 46.0, 56.0, 38.0, 2.0),
                doubleArrayOf(inicioY - 8.0, inicioY - 8.0, inicioY + 25.0, sueloY, sueloY),
                5
            )
            gc.stroke = Color.web("#334155")
            gc.lineWidth = 1.2
            gc.strokeLine(46.0, inicioY - 4.0, ancho / 2.0, inicioY - 4.0)

            // 5. Superficie lunar inferior bajo sueloY
            gc.fill = Color.web("#0f172a")
            gc.fillRect(2.0, sueloY, ancho - 4.0, (alto - sueloY - 2.0).coerceAtLeast(0.0))
        }
    },

    /**
     * Escenario Nivel Medio (Lanzamiento Vertical Hacia Abajo): Edificio de Pruebas Aeroespaciales.
     */
    EDIFICIO_PRUEBAS("Edificio de Pruebas") {
        override fun renderizarFondo(
            gc: GraphicsContext,
            ancho: Double,
            alto: Double,
            inicioY: Double,
            sueloY: Double,
            alturaMaximaMetros: Double
        ) {
            // 1. Gradiente nocturno tecnológico
            gc.fill = GraficosUtils.crearGradienteVertical("#090d16", "#111827", "#1e293b")
            gc.fillRect(0.0, 0.0, ancho, alto)

            dibujarEstrellas(gc, ancho, sueloY)

            // 2. Silueta del edificio de pruebas a la derecha con ventanas neón iluminadas
            val xEdificio = ancho - 54.0
            val altoEdificio = (sueloY - inicioY + 10.0).coerceAtLeast(20.0)
            gc.fill = Color.rgb(30, 41, 59, 0.85)
            gc.fillRect(xEdificio, inicioY - 10.0, 42.0, altoEdificio)
            gc.stroke = Color.rgb(56, 189, 248, 0.35)
            gc.lineWidth = 1.0
            gc.strokeRect(xEdificio, inicioY - 10.0, 42.0, altoEdificio)

            // Ventanas modulares iluminadas
            gc.fill = Color.rgb(56, 189, 248, 0.25)
            var yVentana = inicioY + 4.0
            while (yVentana < sueloY - 14.0) {
                gc.fillRect(xEdificio + 6.0, yVentana, 10.0, 6.0)
                gc.fillRect(xEdificio + 22.0, yVentana, 10.0, 6.0)
                yVentana += 16.0
            }

            // 3. Plataforma superior de lanzamiento con dron/impulsor
            gc.stroke = Color.web("#38bdf8")
            gc.lineWidth = 1.8
            gc.strokeLine(ancho / 2.0, inicioY - 4.0, xEdificio, inicioY - 4.0)

            // 4. Base amortiguada de recepción
            gc.fill = Color.web("#0f172a")
            gc.fillRect(2.0, sueloY, ancho - 4.0, (alto - sueloY - 2.0).coerceAtLeast(0.0))
        }
    },

    /**
     * Escenario Nivel Difícil (Lanzamiento Vertical Hacia Arriba): Plataforma de Lanzamiento con Cohete.
     */
    PLATAFORMA_COHETE("Plataforma de Lanzamiento") {
        override fun renderizarFondo(
            gc: GraphicsContext,
            ancho: Double,
            alto: Double,
            inicioY: Double,
            sueloY: Double,
            alturaMaximaMetros: Double
        ) {
            // 1. Gradiente orbital (estratosfera oscura arriba, resplandor de plataforma abajo)
            gc.fill = GraficosUtils.crearGradienteVertical("#030712", "#0f172a", "#172554")
            gc.fillRect(0.0, 0.0, ancho, alto)

            dibujarEstrellas(gc, ancho, sueloY)

            // 2. Resplandor indirecto de los reflectores en la base de lanzamiento
            gc.fill = Color.rgb(56, 189, 248, 0.08)
            gc.fillOval(ancho * 0.2, sueloY - 45.0, ancho * 0.6, 70.0)

            // 3. Torre umbilical de lanzamiento junto a la plataforma inferior
            val xTorre = (ancho / 2.0) - 44.0
            val altoTorre = ((sueloY - inicioY) * 0.45).coerceAtLeast(35.0)
            val yCimaTorre = sueloY - altoTorre
            gc.stroke = Color.rgb(100, 116, 139, 0.65)
            gc.lineWidth = 1.4
            gc.strokeRect(xTorre, yCimaTorre, 14.0, altoTorre)
            gc.strokeLine(xTorre, yCimaTorre, xTorre + 14.0, yCimaTorre + altoTorre * 0.5)
            gc.strokeLine(xTorre + 14.0, yCimaTorre, xTorre, yCimaTorre + altoTorre * 0.5)

            // 4. Base de hormigón espacial bajo la plataforma
            gc.fill = Color.web("#0f172a")
            gc.fillRect(2.0, sueloY, ancho - 4.0, (alto - sueloY - 2.0).coerceAtLeast(0.0))
        }
    };

    protected fun dibujarEstrellas(gc: GraphicsContext, ancho: Double, limiteY: Double) {
        val posiciones = listOf(
            0.12 to 0.14, 0.28 to 0.09, 0.44 to 0.18, 0.67 to 0.11,
            0.82 to 0.16, 0.19 to 0.34, 0.74 to 0.31, 0.88 to 0.44,
            0.15 to 0.58, 0.63 to 0.52
        )
        gc.fill = Color.rgb(226, 232, 240, 0.55)
        for ((fx, fy) in posiciones) {
            val sx = ancho * fx
            val sy = (limiteY * fy).coerceAtLeast(10.0)
            gc.fillOval(sx, sy, 1.8, 1.8)
        }
    }

    companion object {
        /**
         * Resuelve polimórficamente el escenario correspondiente a una [Dificultad].
         */
        fun desdeDificultad(dificultad: Dificultad): TipoEscenario = when (dificultad) {
            Dificultad.FACIL -> ACANTILADO_LUNAR
            Dificultad.MEDIO -> EDIFICIO_PRUEBAS
            Dificultad.DIFICIL -> PLATAFORMA_COHETE
        }

        /**
         * Resuelve el escenario según la dirección cinemática y velocidad inicial cuando no se especifica dificultad.
         */
        fun desdeParametros(direccion: DireccionMovimiento, velocidadInicial: Double): TipoEscenario = when {
            direccion == DireccionMovimiento.ARRIBA -> PLATAFORMA_COHETE
            velocidadInicial > 0.0 -> EDIFICIO_PRUEBAS
            else -> ACANTILADO_LUNAR
        }
    }
}

/**
 * Contrato de abstracción para el renderizado visual de la simulación física (SRP & DIP).
 */
interface RenderizadorSimulacion {
    fun renderizar(objeto: ObjetoSimulado)
    fun limpiar()
    fun reiniciarEfectos() {}
}

/**
 * Implementación avanzada de [RenderizadorSimulacion] sobre un [Canvas] de JavaFX.
 *
 * Incluye:
 * - Escenarios polimórficos ([EscenarioRenderizable]) con gradientes e iluminación.
 * - Cuadrícula física de medición en metros.
 * - Interpolación suave de movimiento a 60 FPS.
 * - Línea de estela / trayectoria en tiempo real ([PuntoEstela]).
 * - Sistema de partículas al lanzar e impactar ([SistemaParticulas]).
 * - Sombra proyectada dinámica y sprite de cápsula con relieve 3D.
 * - Vector de velocidad ($v$) interactivo con flecha y código de color dinámico.
 */
open class RenderizadorCanvas(
    val canvas: Canvas,
    var alturaMaximaMetros: Double = PhysicsConstants.ALTURA_DEFAULT,
    var direccion: DireccionMovimiento = DireccionMovimiento.ABAJO,
    var velocidadInicial: Double = 0.0,
    var escenario: EscenarioRenderizable = TipoEscenario.desdeParametros(direccion, velocidadInicial)
) : RenderizadorSimulacion {

    private val gc: GraphicsContext = canvas.graphicsContext2D
    val radioObjeto: Double = 12.0
    val diametroObjeto: Double = radioObjeto * 2.0
    val margenSuperior: Double = 35.0
    val margenInferior: Double = 35.0

    // Historial de trayectoria para el efecto de estela (Trail Effect)
    private val puntosEstela: ArrayDeque<PuntoEstela> = ArrayDeque()
    private val maxPuntosEstela: Int = 28

    // Sistema de partículas para impactos y propulsión inicial
    val sistemaParticulas: SistemaParticulas = SistemaParticulas()
    private var impactoEmitido: Boolean = false
    private var lanzamientoEmitido: Boolean = false
    private var ultimoTiempoSimulado: Double = 0.0
    private var pixelYInterpolado: Double? = null

    val escalaPixelesPorMetro: Double
        get() {
            val alturaUtil = canvas.height - margenSuperior - margenInferior - diametroObjeto
            return if (alturaMaximaMetros > 0.0) alturaUtil / alturaMaximaMetros else 1.0
        }

    fun convertirMetroAPixelY(metros: Double): Double =
        margenSuperior + (metros * escalaPixelesPorMetro)

    fun configurar(
        altura: Double,
        direccionMov: DireccionMovimiento = DireccionMovimiento.ABAJO,
        v0: Double = 0.0,
        nuevoEscenario: EscenarioRenderizable? = null
    ) {
        this.alturaMaximaMetros = altura
        this.direccion = direccionMov
        this.velocidadInicial = v0
        this.escenario = nuevoEscenario ?: TipoEscenario.desdeParametros(direccionMov, v0)
        reiniciarEfectos()
    }

    override fun reiniciarEfectos() {
        puntosEstela.clear()
        sistemaParticulas.limpiar()
        impactoEmitido = false
        lanzamientoEmitido = false
        ultimoTiempoSimulado = 0.0
        pixelYInterpolado = null
    }

    override fun limpiar() {
        gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
    }

    override fun renderizar(objeto: ObjetoSimulado) {
        // 1. Limpieza óptima del contexto gráfico para 60 FPS estables
        limpiar()

        val ancho = canvas.width
        val alto = canvas.height
        val alturaEfectiva = if (alturaMaximaMetros > 0.0) alturaMaximaMetros else PhysicsConstants.ALTURA_DEFAULT

        // Reinicio automático de estela/partículas cuando el objeto vuelve a t = 0
        if (objeto.tiempo == 0.0 && !objeto.enSuelo) {
            puntosEstela.clear()
            sistemaParticulas.limpiar()
            impactoEmitido = false
            lanzamientoEmitido = false
            ultimoTiempoSimulado = 0.0
            pixelYInterpolado = null
        }

        val dt = if (objeto.tiempo > ultimoTiempoSimulado) {
            (objeto.tiempo - ultimoTiempoSimulado).coerceIn(0.008, 0.05)
        } else {
            0.016
        }
        ultimoTiempoSimulado = objeto.tiempo

        val inicioY = margenSuperior
        val sueloY = margenSuperior + (alturaEfectiva * escalaPixelesPorMetro)
        val inicioLineaY = inicioY + diametroObjeto
        val sueloLineaY = sueloY + diametroObjeto
        val centroX = ancho / 2.0

        // 2. Renderizado polimórfico del escenario según el nivel/dificultad (OCP)
        escenario.renderizarFondo(gc, ancho, alto, inicioLineaY, sueloLineaY, alturaEfectiva)

        // 3. Cuadrícula física de medición con marcas en metros (DRY)
        val esAscendente = direccion == DireccionMovimiento.ARRIBA
        GraficosUtils.dibujarCuadriculaMedicion(
            gc = gc,
            ancho = ancho,
            inicioY = inicioLineaY,
            sueloY = sueloLineaY,
            alturaMaximaMetros = alturaEfectiva,
            direccionAscendente = esAscendente
        )

        // 4. Borde exterior estilizado del lienzo
        gc.stroke = Color.web("#334155")
        gc.lineWidth = 1.5
        gc.strokeRect(1.0, 1.0, ancho - 2.0, alto - 2.0)

        // 5. Cálculo e interpolación suave de la posición vertical entre frames
        val pixelYObjetivo = if (esAscendente) {
            sueloY - (objeto.posicionY * escalaPixelesPorMetro)
        } else {
            convertirMetroAPixelY(objeto.posicionY)
        }

        val pixelYSuave = if (pixelYInterpolado == null || objeto.tiempo == 0.0 || objeto.enSuelo) {
            pixelYObjetivo
        } else {
            val previo = pixelYInterpolado!!
            previo + (pixelYObjetivo - previo) * 0.65
        }
        pixelYInterpolado = pixelYSuave
        val centroY = pixelYSuave + radioObjeto

        // 6. Actualización de estela y partículas de lanzamiento/impacto
        if (objeto.tiempo > 0.0) {
            if (!lanzamientoEmitido && (esAscendente || velocidadInicial > 0.0)) {
                val yOrigenParticulas = if (esAscendente) sueloLineaY else inicioLineaY
                sistemaParticulas.emitirDestelloLanzamiento(centroX, yOrigenParticulas, haciaArriba = esAscendente)
                lanzamientoEmitido = true
            }

            if (puntosEstela.isEmpty() || abs(puntosEstela.last().y - centroY) >= 2.0) {
                if (puntosEstela.size >= maxPuntosEstela) {
                    puntosEstela.removeFirst()
                }
                puntosEstela.addLast(PuntoEstela(centroX, centroY, objeto.velocidadY))
            }
        }

        val enSueloVisual = if (esAscendente) (objeto.enSuelo && objeto.tiempo > 0.0) else objeto.enSuelo
        if (enSueloVisual && !impactoEmitido) {
            sistemaParticulas.emitirImpactoSuelo(centroX, sueloLineaY, objeto.velocidadY)
            impactoEmitido = true
        }
        sistemaParticulas.actualizar(dt)

        // 7. Líneas de referencia superior e inferior (Inicio / Suelo / Altura Máxima)
        dibujarLineasReferencia(gc, ancho, inicioY, inicioLineaY, sueloLineaY, alturaEfectiva, esAscendente)

        // 8. Sombra proyectada en la plataforma inferior
        val distanciaAlSuelo = (sueloLineaY - centroY).coerceAtLeast(0.0)
        val rangoTotal = (sueloLineaY - inicioLineaY).coerceAtLeast(1.0)
        val proximidadSuelo = (1.0 - (distanciaAlSuelo / rangoTotal)).coerceIn(0.15, 1.0)
        GraficosUtils.dibujarSombraProyectada(gc, centroX, sueloLineaY, radioObjeto, proximidadSuelo)

        // 9. Dibujo de estela de trayectoria (Trail Effect) y partículas activas
        GraficosUtils.dibujarEstela(gc, puntosEstela, radioObjeto)
        GraficosUtils.dibujarParticulas(gc, sistemaParticulas.particulasActivas)

        // 10. Dibujo del sprite detallado con relieve 3D y vector de velocidad dinámico ($v$)
        GraficosUtils.dibujarSpriteObjeto(
            gc = gc,
            centroX = centroX,
            centroY = centroY,
            radio = radioObjeto,
            velocidadY = objeto.velocidadY,
            enSuelo = enSueloVisual,
            direccion = direccion
        )

        GraficosUtils.dibujarVectorVelocidad(
            gc = gc,
            centroX = centroX,
            centroY = centroY,
            radioObjeto = radioObjeto,
            velocidadY = objeto.velocidadY,
            direccionConfigurada = direccion
        )

        // 11. Panel HUD de telemetría instantánea
        GraficosUtils.dibujarPanelTelemetria(
            gc = gc,
            ancho = ancho,
            tiempo = objeto.tiempo,
            posicionY = objeto.posicionY,
            velocidadY = objeto.velocidadY,
            simboloAltura = if (esAscendente) "h" else "y"
        )
    }

    private fun dibujarLineasReferencia(
        gc: GraphicsContext,
        ancho: Double,
        inicioY: Double,
        inicioLineaY: Double,
        sueloLineaY: Double,
        alturaEfectiva: Double,
        esAscendente: Boolean
    ) {
        if (esAscendente) {
            // Suelo / Plataforma de disparo abajo
            gc.stroke = Color.web("#38bdf8")
            gc.lineWidth = 2.2
            gc.strokeLine(20.0, sueloLineaY, ancho - 20.0, sueloLineaY)
            gc.fill = Color.web("#38bdf8")
            gc.font = Font.font("Segoe UI", FontWeight.BOLD, 10.0)
            gc.fillText("0 m (Suelo / Disparo)", 25.0, sueloLineaY + 16.0)

            // Cúspide h_máx arriba
            gc.stroke = Color.web("#475569")
            gc.lineWidth = 1.2
            gc.strokeLine(25.0, inicioLineaY, ancho - 25.0, inicioLineaY)
            gc.fill = Color.web("#94a3b8")
            gc.font = Font.font("Segoe UI", FontWeight.NORMAL, 10.0)
            gc.fillText(String.format(Locale.US, "%.1f m (h_máx)", alturaEfectiva), 25.0, inicioY - 6.0)
        } else {
            // Plataforma superior de inicio
            gc.stroke = Color.web("#475569")
            gc.lineWidth = 1.2
            gc.strokeLine(25.0, inicioLineaY, ancho - 25.0, inicioLineaY)
            gc.fill = Color.web("#94a3b8")
            gc.font = Font.font("Segoe UI", FontWeight.NORMAL, 10.0)
            val etiquetaInicio = if (velocidadInicial > 0.0) {
                String.format(Locale.US, "0 m (Inicio v₀=%.1f m/s)", velocidadInicial)
            } else {
                "0 m (Inicio)"
            }
            gc.fillText(etiquetaInicio, 25.0, inicioY - 6.0)

            // Suelo de impacto abajo
            gc.stroke = Color.web("#38bdf8")
            gc.lineWidth = 2.2
            gc.strokeLine(20.0, sueloLineaY, ancho - 20.0, sueloLineaY)
            gc.fill = Color.web("#38bdf8")
            gc.font = Font.font("Segoe UI", FontWeight.BOLD, 10.0)
            gc.fillText(String.format(Locale.US, "%.1f m (Suelo)", alturaEfectiva), 25.0, sueloLineaY + 16.0)
        }
    }
}

/**
 * Especialización de [RenderizadorCanvas] para vinculación directa por dificultad de nivel (SRP / OCP).
 */
class SimulacionCanvasView(
    canvas: Canvas,
    dificultad: Dificultad = Dificultad.FACIL,
    alturaMaximaMetros: Double = PhysicsConstants.ALTURA_DEFAULT,
    direccion: DireccionMovimiento = dificultad.estrategiaCalculo.direccionMovimiento,
    velocidadInicial: Double = 0.0
) : RenderizadorCanvas(
    canvas = canvas,
    alturaMaximaMetros = alturaMaximaMetros,
    direccion = direccion,
    velocidadInicial = velocidadInicial,
    escenario = TipoEscenario.desdeDificultad(dificultad)
)
