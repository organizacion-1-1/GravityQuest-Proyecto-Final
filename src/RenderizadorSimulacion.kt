package gravityquest

import gravityquest.models.Dificultad
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import java.util.Locale
import kotlin.math.abs

/**
 * ============================================================================
 * GravityQuest - Motor de Renderizado y Escenarios Polimórficos Optimizados
 * (Issue #21 - Milestone 4: Estabilidad en Canvas, Cero GC, SRP, OCP & DRY)
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
 *      renderice su propio escenario ilustrado con gradientes y colores cacheados.
 *
 * 3. Gestión de Memoria y Estabilidad en Canvas (Issue #21):
 *    - Reutiliza un pool de objetos [PuntoEstela], gradientes cacheados ([LinearGradient]),
 *      colores ([Color]) y fuentes ([Font]) para evitar instanciaciones en `handle(now)`.
 *    - Sincroniza la limpieza y repintado del [GraphicsContext] en una sola pasada aislada
 *      (`gc.save()` / `gc.restore()`) previniendo parpadeos (*flickering*).
 *    - Acota estrictamente las coordenadas visuales (`centroX`, `centroY`) dentro de los
 *      bordes del [Canvas] y sobre la línea del suelo (`sueloLineaY`).
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
 * Todos los gradientes, colores y buffers están pre-instanciados para evitar asignaciones por frame.
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
            // 1. Gradiente espacial nocturno profundo cacheado
            gc.fill = GRADIENTE_LUNAR
            gc.fillRect(0.0, 0.0, ancho, alto)

            // 2. Nebulosa cian tenue e iluminación indirecta
            gc.fill = COLOR_NEBULOSA_LUNAR
            gc.fillOval(ancho * 0.15, alto * 0.08, ancho * 0.7, alto * 0.45)

            // 3. Campo de estrellas fijas deterministas
            dibujarEstrellas(gc, ancho, sueloY)

            // 4. Silueta del acantilado lunar lateral izquierdo reutilizando buffers de vértices
            bufferAcantiladoY[0] = inicioY - 8.0
            bufferAcantiladoY[1] = inicioY - 8.0
            bufferAcantiladoY[2] = inicioY + 25.0
            bufferAcantiladoY[3] = sueloY
            bufferAcantiladoY[4] = sueloY
            gc.fill = COLOR_ESTRUCTURA_SLATE
            gc.fillPolygon(BUFFER_ACANTILADO_X, bufferAcantiladoY, 5)

            gc.stroke = COLOR_BORDE_SLATE
            gc.lineWidth = 1.2
            gc.strokeLine(46.0, inicioY - 4.0, ancho / 2.0, inicioY - 4.0)

            // 5. Superficie lunar inferior bajo sueloY
            gc.fill = COLOR_SUELO_OSCURO
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
            // 1. Gradiente nocturno tecnológico cacheado
            gc.fill = GRADIENTE_EDIFICIO
            gc.fillRect(0.0, 0.0, ancho, alto)

            dibujarEstrellas(gc, ancho, sueloY)

            // 2. Silueta del edificio de pruebas a la derecha con ventanas neón iluminadas
            val xEdificio = ancho - 54.0
            val altoEdificio = (sueloY - inicioY + 10.0).coerceAtLeast(20.0)
            gc.fill = COLOR_CUERPO_EDIFICIO
            gc.fillRect(xEdificio, inicioY - 10.0, 42.0, altoEdificio)
            gc.stroke = COLOR_BORDE_EDIFICIO
            gc.lineWidth = 1.0
            gc.strokeRect(xEdificio, inicioY - 10.0, 42.0, altoEdificio)

            // Ventanas modulares iluminadas
            gc.fill = COLOR_VENTANA_EDIFICIO
            var yVentana = inicioY + 4.0
            while (yVentana < sueloY - 14.0) {
                gc.fillRect(xEdificio + 6.0, yVentana, 10.0, 6.0)
                gc.fillRect(xEdificio + 22.0, yVentana, 10.0, 6.0)
                yVentana += 16.0
            }

            // 3. Plataforma superior de lanzamiento con dron/impulsor
            gc.stroke = COLOR_CIAN_NEON
            gc.lineWidth = 1.8
            gc.strokeLine(ancho / 2.0, inicioY - 4.0, xEdificio, inicioY - 4.0)

            // 4. Base amortiguada de recepción
            gc.fill = COLOR_SUELO_OSCURO
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
            // 1. Gradiente orbital cacheado
            gc.fill = GRADIENTE_COHETE
            gc.fillRect(0.0, 0.0, ancho, alto)

            dibujarEstrellas(gc, ancho, sueloY)

            // 2. Resplandor indirecto de los reflectores en la base de lanzamiento
            gc.fill = COLOR_REFLECTOR_COHETE
            gc.fillOval(ancho * 0.2, sueloY - 45.0, ancho * 0.6, 70.0)

            // 3. Torre umbilical de lanzamiento junto a la plataforma inferior
            val xTorre = (ancho / 2.0) - 44.0
            val altoTorre = ((sueloY - inicioY) * 0.45).coerceAtLeast(35.0)
            val yCimaTorre = sueloY - altoTorre
            gc.stroke = COLOR_TORRE_COHETE
            gc.lineWidth = 1.4
            gc.strokeRect(xTorre, yCimaTorre, 14.0, altoTorre)
            gc.strokeLine(xTorre, yCimaTorre, xTorre + 14.0, yCimaTorre + altoTorre * 0.5)
            gc.strokeLine(xTorre + 14.0, yCimaTorre, xTorre, yCimaTorre + altoTorre * 0.5)

            // 4. Base de hormigón espacial bajo la plataforma
            gc.fill = COLOR_SUELO_OSCURO
            gc.fillRect(2.0, sueloY, ancho - 4.0, (alto - sueloY - 2.0).coerceAtLeast(0.0))
        }
    };

    protected fun dibujarEstrellas(gc: GraphicsContext, ancho: Double, limiteY: Double) {
        gc.fill = COLOR_ESTRELLA
        for (i in ESTRELLAS_FX.indices) {
            val sx = ancho * ESTRELLAS_FX[i]
            val sy = (limiteY * ESTRELLAS_FY[i]).coerceAtLeast(10.0)
            gc.fillOval(sx, sy, 1.8, 1.8)
        }
    }

    companion object {
        // Primitivas gráficas cacheadas para cero asignaciones por frame en `handle(now)`
        private val GRADIENTE_LUNAR: LinearGradient =
            GraficosUtils.crearGradienteVertical("#070b19", "#0f172a", "#1e293b")
        private val GRADIENTE_EDIFICIO: LinearGradient =
            GraficosUtils.crearGradienteVertical("#090d16", "#111827", "#1e293b")
        private val GRADIENTE_COHETE: LinearGradient =
            GraficosUtils.crearGradienteVertical("#030712", "#0f172a", "#172554")

        private val COLOR_NEBULOSA_LUNAR: Color = Color.rgb(56, 189, 248, 0.06)
        private val COLOR_ESTRUCTURA_SLATE: Color = Color.web("#1e293b")
        private val COLOR_BORDE_SLATE: Color = Color.web("#334155")
        private val COLOR_SUELO_OSCURO: Color = Color.web("#0f172a")
        private val COLOR_CUERPO_EDIFICIO: Color = Color.rgb(30, 41, 59, 0.85)
        private val COLOR_BORDE_EDIFICIO: Color = Color.rgb(56, 189, 248, 0.35)
        private val COLOR_VENTANA_EDIFICIO: Color = Color.rgb(56, 189, 248, 0.25)
        private val COLOR_CIAN_NEON: Color = Color.web("#38bdf8")
        private val COLOR_REFLECTOR_COHETE: Color = Color.rgb(56, 189, 248, 0.08)
        private val COLOR_TORRE_COHETE: Color = Color.rgb(100, 116, 139, 0.65)
        private val COLOR_ESTRELLA: Color = Color.rgb(226, 232, 240, 0.55)

        private val ESTRELLAS_FX: DoubleArray =
            doubleArrayOf(0.12, 0.28, 0.44, 0.67, 0.82, 0.19, 0.74, 0.88, 0.15, 0.63)
        private val ESTRELLAS_FY: DoubleArray =
            doubleArrayOf(0.14, 0.09, 0.18, 0.11, 0.16, 0.34, 0.31, 0.44, 0.58, 0.52)

        private val BUFFER_ACANTILADO_X: DoubleArray = doubleArrayOf(2.0, 46.0, 56.0, 38.0, 2.0)
        private val bufferAcantiladoY: DoubleArray = DoubleArray(5)

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
 * Contrato modular para el renderizado de entidades físicas participantes en la simulación (OCP & SRP).
 */
interface EntidadFisicaRenderizable {
    val nombreEntidad: String

    fun renderizarEntidad(
        gc: GraphicsContext,
        centroX: Double,
        centroY: Double,
        radio: Double,
        objeto: ObjetoSimulado,
        enSueloVisual: Boolean,
        direccion: DireccionMovimiento
    )
}

/**
 * Componente dedicado al renderizado gráfico del meteorito con estela ígnea de fuego,
 * partículas, cráteres poligonales y rotación dinámica (SRP, OCP & DRY).
 */
class MeteoritoRenderizable(
    override val nombreEntidad: String = "Meteorito Espacial",
    val rutaSpriteOpcional: String = "/assets/meteorito.png"
) : EntidadFisicaRenderizable {

    private val spriteCargado by lazy {
        GraficosUtils.cargarSpriteMeteoritoOpcional(rutaSpriteOpcional)
    }

    override fun renderizarEntidad(
        gc: GraphicsContext,
        centroX: Double,
        centroY: Double,
        radio: Double,
        objeto: ObjetoSimulado,
        enSueloVisual: Boolean,
        direccion: DireccionMovimiento
    ) {
        GraficosUtils.dibujarCuerpoMeteorito(
            gc = gc,
            centroX = centroX,
            centroY = centroY,
            radio = radio,
            velocidadY = objeto.velocidadY,
            tiempo = objeto.tiempo,
            enSuelo = enSueloVisual,
            direccion = direccion,
            imagenSprite = spriteCargado
        )
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
 * Implementación optimizada de [RenderizadorSimulacion] sobre un [Canvas] de JavaFX.
 *
 * Garantías de Issue #21:
 * - Cero instanciación de colores, fuentes o puntos de estela en `renderizar(objeto)` (Object Pooling).
 * - Sincronización limpia del [GraphicsContext] sin parpadeos (*flickering*).
 * - Validación estricta de los bordes del [Canvas] y del límite del suelo (`sueloLineaY`).
 */
open class RenderizadorCanvas(
    val canvas: Canvas,
    var alturaMaximaMetros: Double = PhysicsConstants.ALTURA_DEFAULT,
    var direccion: DireccionMovimiento = DireccionMovimiento.ABAJO,
    var velocidadInicial: Double = 0.0,
    var escenario: EscenarioRenderizable = TipoEscenario.desdeParametros(direccion, velocidadInicial),
    var entidadRenderizable: EntidadFisicaRenderizable = MeteoritoRenderizable()
) : RenderizadorSimulacion {

    companion object {
        private val COLOR_BORDE_CANVAS: Color = Color.web("#334155")
        private val COLOR_LINEA_SUELO: Color = Color.web("#38bdf8")
        private val COLOR_LINEA_INICIO: Color = Color.web("#475569")
        private val COLOR_TEXTO_INICIO: Color = Color.web("#94a3b8")
        private val FONT_SEGOE_BOLD_10: Font = Font.font("Segoe UI", FontWeight.BOLD, 10.0)
        private val FONT_SEGOE_NORMAL_10: Font = Font.font("Segoe UI", FontWeight.NORMAL, 10.0)
    }

    private val gc: GraphicsContext = canvas.graphicsContext2D
    val radioObjeto: Double = 12.0
    val diametroObjeto: Double = radioObjeto * 2.0
    val margenSuperior: Double = 35.0
    val margenInferior: Double = 35.0

    // Historial de trayectoria con pool reutilizable de PuntoEstela para evitar GC en `handle(now)`
    private val maxPuntosEstela: Int = 28
    private val puntosEstela: ArrayList<PuntoEstela> = ArrayList(maxPuntosEstela)
    private val poolPuntosEstela: ArrayDeque<PuntoEstela> = ArrayDeque(maxPuntosEstela)

    // Sistema de partículas para impactos y propulsión inicial
    val sistemaParticulas: SistemaParticulas = SistemaParticulas()
    private var impactoEmitido: Boolean = false
    private var lanzamientoEmitido: Boolean = false
    private var ultimoTiempoSimulado: Double = 0.0
    private var pixelYInterpolado: Double? = null

    /** Última coordenada vertical del centro del objeto renderizado dentro del Canvas (para diagnóstico/tests). */
    var ultimoCentroYRenderizado: Double = margenSuperior + radioObjeto
        private set

    init {
        repeat(maxPuntosEstela) {
            poolPuntosEstela.addLast(PuntoEstela(0.0, 0.0, 0.0))
        }
    }

    val escalaPixelesPorMetro: Double
        get() {
            val alturaUtil = (canvas.height - margenSuperior - margenInferior - diametroObjeto).coerceAtLeast(1.0)
            return if (alturaMaximaMetros > 0.0) alturaUtil / alturaMaximaMetros else 1.0
        }

    fun convertirMetroAPixelY(metros: Double): Double {
        val alturaEfectiva = if (alturaMaximaMetros > 0.0) alturaMaximaMetros else PhysicsConstants.ALTURA_DEFAULT
        val metrosAcotados = metros.coerceIn(0.0, alturaEfectiva)
        return margenSuperior + (metrosAcotados * escalaPixelesPorMetro)
    }

    fun configurar(
        altura: Double,
        direccionMov: DireccionMovimiento = DireccionMovimiento.ABAJO,
        v0: Double = 0.0,
        nuevoEscenario: EscenarioRenderizable? = null
    ) {
        this.alturaMaximaMetros = altura.coerceAtLeast(0.0)
        this.direccion = direccionMov
        this.velocidadInicial = v0
        this.escenario = nuevoEscenario ?: TipoEscenario.desdeParametros(direccionMov, v0)
        reiniciarEfectos()
    }

    private fun reciclarEstela() {
        while (puntosEstela.isNotEmpty()) {
            poolPuntosEstela.addLast(puntosEstela.removeAt(puntosEstela.size - 1))
        }
    }

    private fun agregarPuntoEstela(x: Double, y: Double, velocidad: Double) {
        val instancia = if (puntosEstela.size >= maxPuntosEstela) {
            puntosEstela.removeAt(0)
        } else if (poolPuntosEstela.isNotEmpty()) {
            poolPuntosEstela.removeLast()
        } else {
            PuntoEstela(x, y, velocidad)
        }
        instancia.actualizar(x, y, velocidad)
        puntosEstela.add(instancia)
    }

    override fun reiniciarEfectos() {
        reciclarEstela()
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
        val ancho = canvas.width
        val alto = canvas.height
        if (ancho <= 0.0 || alto <= 0.0) return

        // Aísla el estado del GraphicsContext para evitar fugas de transformaciones o alpha (anti-flickering)
        gc.save()
        try {
            limpiar()

            val alturaEfectiva = if (alturaMaximaMetros > 0.0) alturaMaximaMetros else PhysicsConstants.ALTURA_DEFAULT

            // Reinicio automático de estela/partículas cuando el objeto vuelve a t = 0
            if (objeto.tiempo == 0.0 && !objeto.enSuelo) {
                reciclarEstela()
                sistemaParticulas.limpiar()
                impactoEmitido = false
                lanzamientoEmitido = false
                ultimoTiempoSimulado = 0.0
                pixelYInterpolado = null
            }

            // Delta de tiempo real (proveniente de MotorSimulacionCanvas o diferencia temporal)
            val dt = when {
                objeto.ultimoDeltaSegundos > 0.0 -> objeto.ultimoDeltaSegundos.coerceIn(0.001, 0.08)
                objeto.tiempo > ultimoTiempoSimulado -> (objeto.tiempo - ultimoTiempoSimulado).coerceIn(0.004, 0.08)
                else -> 0.016
            }
            ultimoTiempoSimulado = objeto.tiempo

            val inicioY = margenSuperior
            val sueloY = (margenSuperior + (alturaEfectiva * escalaPixelesPorMetro))
                .coerceIn(inicioY, (alto - margenInferior - diametroObjeto).coerceAtLeast(inicioY))
            val inicioLineaY = inicioY + diametroObjeto
            val sueloLineaY = sueloY + diametroObjeto
            val centroX = (ancho / 2.0).coerceIn(radioObjeto + 2.0, (ancho - radioObjeto - 2.0).coerceAtLeast(radioObjeto + 2.0))

            // 1. Renderizado polimórfico del escenario en pasada continua (sin parpadeos)
            escenario.renderizarFondo(gc, ancho, alto, inicioLineaY, sueloLineaY, alturaEfectiva)

            // 2. Cuadrícula física de medición con marcas en metros (DRY)
            val esAscendente = direccion == DireccionMovimiento.ARRIBA
            GraficosUtils.dibujarCuadriculaMedicion(
                gc = gc,
                ancho = ancho,
                inicioY = inicioLineaY,
                sueloY = sueloLineaY,
                alturaMaximaMetros = alturaEfectiva,
                direccionAscendente = esAscendente
            )

            // 3. Borde exterior estilizado del lienzo
            gc.stroke = COLOR_BORDE_CANVAS
            gc.lineWidth = 1.5
            gc.strokeRect(1.0, 1.0, ancho - 2.0, alto - 2.0)

            // 4. Cálculo y validación de límites de la posición vertical dentro del Canvas
            val posicionFisicaAcotada = objeto.posicionY.coerceIn(0.0, alturaEfectiva)
            val pixelYObjetivo = if (esAscendente) {
                sueloY - (posicionFisicaAcotada * escalaPixelesPorMetro)
            } else {
                convertirMetroAPixelY(posicionFisicaAcotada)
            }.coerceIn(inicioY, sueloY)

            val pixelYSuave = if (pixelYInterpolado == null || objeto.tiempo == 0.0 || objeto.enSuelo) {
                pixelYObjetivo
            } else {
                val previo = pixelYInterpolado!!
                val factorSuavizado = (dt * 42.0).coerceIn(0.35, 1.0)
                (previo + (pixelYObjetivo - previo) * factorSuavizado).coerceIn(inicioY, sueloY)
            }
            pixelYInterpolado = pixelYSuave

            // Garantiza que el centro del objeto jamás salga del Canvas ni atraviese sueloLineaY
            val limiteSuperiorCentro = (inicioY + radioObjeto).coerceAtLeast(radioObjeto + 2.0)
            val limiteInferiorCentro = (sueloLineaY - radioObjeto).coerceAtMost(alto - radioObjeto - 2.0)
            val centroY = (pixelYSuave + radioObjeto).coerceIn(
                limiteSuperiorCentro,
                limiteInferiorCentro.coerceAtLeast(limiteSuperiorCentro)
            )
            ultimoCentroYRenderizado = centroY

            // 5. Actualización de estela (con pool) y partículas de lanzamiento/impacto
            if (objeto.tiempo > 0.0) {
                if (!lanzamientoEmitido && (esAscendente || velocidadInicial > 0.0)) {
                    val yOrigenParticulas = if (esAscendente) sueloLineaY else inicioLineaY
                    sistemaParticulas.emitirDestelloLanzamiento(centroX, yOrigenParticulas, haciaArriba = esAscendente)
                    lanzamientoEmitido = true
                }

                if (puntosEstela.isEmpty() || abs(puntosEstela[puntosEstela.size - 1].y - centroY) >= 2.0) {
                    agregarPuntoEstela(centroX, centroY, objeto.velocidadY)
                }
            }

            val enSueloVisual = if (esAscendente) (objeto.enSuelo && objeto.tiempo > 0.0) else objeto.enSuelo
            if (enSueloVisual && !impactoEmitido) {
                sistemaParticulas.emitirImpactoSuelo(centroX, sueloLineaY, objeto.velocidadY)
                impactoEmitido = true
            }
            sistemaParticulas.actualizar(dt)

            // 6. Líneas de referencia superior e inferior (Inicio / Suelo / Altura Máxima)
            dibujarLineasReferencia(gc, ancho, inicioY, inicioLineaY, sueloLineaY, alturaEfectiva, esAscendente)

            // 7. Sombra proyectada en la plataforma inferior
            val distanciaAlSuelo = (sueloLineaY - centroY).coerceAtLeast(0.0)
            val rangoTotal = (sueloLineaY - inicioLineaY).coerceAtLeast(1.0)
            val proximidadSuelo = (1.0 - (distanciaAlSuelo / rangoTotal)).coerceIn(0.15, 1.0)
            GraficosUtils.dibujarSombraProyectada(gc, centroX, sueloLineaY, radioObjeto, proximidadSuelo)

            // 8. Dibujo de estela de trayectoria (Trail Effect) y partículas activas
            GraficosUtils.dibujarEstela(gc, puntosEstela, radioObjeto)
            GraficosUtils.dibujarParticulas(gc, sistemaParticulas.particulasActivas)

            // 9. Dibujo modular de la entidad física y vector ($v$)
            entidadRenderizable.renderizarEntidad(
                gc = gc,
                centroX = centroX,
                centroY = centroY,
                radio = radioObjeto,
                objeto = objeto,
                enSueloVisual = enSueloVisual,
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

            // 10. Panel HUD de telemetría instantánea
            GraficosUtils.dibujarPanelTelemetria(
                gc = gc,
                ancho = ancho,
                tiempo = objeto.tiempo,
                posicionY = posicionFisicaAcotada,
                velocidadY = objeto.velocidadY,
                simboloAltura = if (esAscendente) "h" else "y"
            )
        } finally {
            gc.restore()
        }
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
            gc.stroke = COLOR_LINEA_SUELO
            gc.lineWidth = 2.2
            gc.strokeLine(20.0, sueloLineaY, ancho - 20.0, sueloLineaY)
            gc.fill = COLOR_LINEA_SUELO
            gc.font = FONT_SEGOE_BOLD_10
            gc.fillText("0 m (Suelo / Disparo)", 25.0, sueloLineaY + 16.0)

            gc.stroke = COLOR_LINEA_INICIO
            gc.lineWidth = 1.2
            gc.strokeLine(25.0, inicioLineaY, ancho - 25.0, inicioLineaY)
            gc.fill = COLOR_TEXTO_INICIO
            gc.font = FONT_SEGOE_NORMAL_10
            gc.fillText(String.format(Locale.US, "%.1f m (h_máx)", alturaEfectiva), 25.0, inicioY - 6.0)
        } else {
            gc.stroke = COLOR_LINEA_INICIO
            gc.lineWidth = 1.2
            gc.strokeLine(25.0, inicioLineaY, ancho - 25.0, inicioLineaY)
            gc.fill = COLOR_TEXTO_INICIO
            gc.font = FONT_SEGOE_NORMAL_10
            val etiquetaInicio = if (velocidadInicial > 0.0) {
                String.format(Locale.US, "0 m (Inicio v₀=%.1f m/s)", velocidadInicial)
            } else {
                "0 m (Inicio)"
            }
            gc.fillText(etiquetaInicio, 25.0, inicioY - 6.0)

            gc.stroke = COLOR_LINEA_SUELO
            gc.lineWidth = 2.2
            gc.strokeLine(20.0, sueloLineaY, ancho - 20.0, sueloLineaY)
            gc.fill = COLOR_LINEA_SUELO
            gc.font = FONT_SEGOE_BOLD_10
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
