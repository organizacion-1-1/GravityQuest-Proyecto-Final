package gravityquest

import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * ============================================================================
 * GravityQuest - Utilidades Gráficas y Sistema de Efectos Visuales Optimizados
 * (Issue #21 - Milestone 4: Optimización de Memoria, Cero GC en handle, DRY & SRP)
 * ============================================================================
 *
 * 1. Gestión Fina de Recursos y Memoria (Object Pooling y Caché de Primitivas):
 *    - Pre-instancia y reutiliza colores ([Color]), fuentes ([Font]), gradientes
 *      ([RadialGradient]) y buffers de coordenadas ([DoubleArray]) para evitar la
 *      creación de objetos de corta vida dentro del ciclo `AnimationTimer.handle(now)`.
 *    - [SistemaParticulas] implementa un pool de objetos ([ParticulaGrafica]) y
 *      expone la lista de partículas sin copiar colecciones en cada frame.
 *
 * 2. Principio DRY (Don't Repeat Yourself):
 *    - Centraliza en [GraficosUtils] la creación de gradientes espaciales, el trazado
 *      de cuadrículas de medición física, vectores de velocidad con puntas de flecha,
 *      estelas de trayectoria (Trail Effect), sombras proyectadas y renderizado del
 *      meteorito con rotación dinámica.
 */

/**
 * Representa un punto histórico reutilizable de la trayectoria para el efecto de estela (Trail Effect).
 * Sus propiedades son mutables para permitir su reciclaje en el pool de [RenderizadorCanvas] sin
 * presionar al Recolector de Basura (Issue #21).
 */
data class PuntoEstela(
    var x: Double,
    var y: Double,
    var velocidad: Double
) {
    fun actualizar(nuevoX: Double, nuevoY: Double, nuevaVelocidad: Double): PuntoEstela {
        this.x = nuevoX
        this.y = nuevoY
        this.velocidad = nuevaVelocidad
        return this
    }
}

/**
 * Representa una partícula individual reutilizable de polvo lunar o destello neón.
 */
data class ParticulaGrafica(
    var x: Double,
    var y: Double,
    var vx: Double,
    var vy: Double,
    var radio: Double,
    var vida: Double,
    var vidaMaxima: Double,
    var color: Color
) {
    val opacidad: Double
        get() = if (vidaMaxima > 0.0) (vida / vidaMaxima).coerceIn(0.0, 1.0) else 0.0

    fun configurar(
        nuevoX: Double,
        nuevoY: Double,
        nuevoVx: Double,
        nuevoVy: Double,
        nuevoRadio: Double,
        nuevaVidaTotal: Double,
        nuevoColor: Color
    ): ParticulaGrafica {
        this.x = nuevoX
        this.y = nuevoY
        this.vx = nuevoVx
        this.vy = nuevoVy
        this.radio = nuevoRadio
        this.vida = nuevaVidaTotal
        this.vidaMaxima = nuevaVidaTotal
        this.color = nuevoColor
        return this
    }
}

/**
 * Sistema ligero de partículas con Object Pooling para efectos de impacto y propulsión
 * sin asignaciones de memoria durante el bucle de animación (Issue #21).
 */
class SistemaParticulas(
    private val maxParticulas: Int = 45
) {
    companion object {
        private val COLORES_IMPACTO: Array<Color> = arrayOf(
            Color.web("#38bdf8"),
            Color.web("#4ade80"),
            Color.web("#bae6fd"),
            Color.web("#94a3b8")
        )

        private val COLORES_LANZAMIENTO: Array<Color> = arrayOf(
            Color.web("#38bdf8"),
            Color.web("#f43f5e"),
            Color.web("#fbbf24"),
            Color.web("#e0f2fe")
        )
    }

    private val _particulas: ArrayList<ParticulaGrafica> = ArrayList(maxParticulas)
    private val _poolInactivas: ArrayDeque<ParticulaGrafica> = ArrayDeque(maxParticulas)

    /** Vista de lectura directa sobre las partículas activas (evita crear copias con `.toList()` por frame). */
    val particulasActivas: List<ParticulaGrafica>
        get() = _particulas

    init {
        repeat(maxParticulas) {
            _poolInactivas.addLast(
                ParticulaGrafica(0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 1.0, COLORES_IMPACTO[0])
            )
        }
    }

    private fun obtenerInstanciaParticula(): ParticulaGrafica =
        if (_poolInactivas.isNotEmpty()) {
            _poolInactivas.removeLast()
        } else if (_particulas.isNotEmpty()) {
            _particulas.removeAt(0)
        } else {
            ParticulaGrafica(0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 1.0, COLORES_IMPACTO[0])
        }

    /**
     * Genera un estallido de partículas (polvo y chispas neón) cuando el objeto impacta el suelo.
     */
    fun emitirImpactoSuelo(centroX: Double, sueloY: Double, magnitudVelocidad: Double = 15.0) {
        val intensidad = (abs(magnitudVelocidad) / 15.0).coerceIn(0.6, 2.2)
        val cantidad = 16

        for (i in 0 until cantidad) {
            if (_particulas.size >= maxParticulas) {
                val reciclada = _particulas.removeAt(0)
                _poolInactivas.addLast(reciclada)
            }
            val angulo = -Math.PI * (0.12 + 0.76 * (i.toDouble() / (cantidad - 1).coerceAtLeast(1)))
            val rapidez = (28.0 + Random.nextDouble(0.0, 48.0)) * intensidad
            val vidaTotal = 0.35 + Random.nextDouble(0.0, 0.35)
            val particula = obtenerInstanciaParticula().configurar(
                nuevoX = centroX + Random.nextDouble(-6.0, 6.0),
                nuevoY = sueloY - 2.0,
                nuevoVx = cos(angulo) * rapidez,
                nuevoVy = sin(angulo) * rapidez,
                nuevoRadio = Random.nextDouble(2.0, 4.5),
                nuevaVidaTotal = vidaTotal,
                nuevoColor = COLORES_IMPACTO[i % COLORES_IMPACTO.size]
            )
            _particulas.add(particula)
        }
    }

    /**
     * Genera destellos de propulsión cuando el objeto es disparado o lanzado inicialmente.
     */
    fun emitirDestelloLanzamiento(centroX: Double, origenY: Double, haciaArriba: Boolean) {
        val cantidad = 12
        val signoY = if (haciaArriba) 1.0 else -1.0

        for (i in 0 until cantidad) {
            if (_particulas.size >= maxParticulas) {
                val reciclada = _particulas.removeAt(0)
                _poolInactivas.addLast(reciclada)
            }
            val vx = Random.nextDouble(-35.0, 35.0)
            val vy = signoY * Random.nextDouble(18.0, 55.0)
            val vidaTotal = 0.28 + Random.nextDouble(0.0, 0.25)
            val particula = obtenerInstanciaParticula().configurar(
                nuevoX = centroX + Random.nextDouble(-5.0, 5.0),
                nuevoY = origenY,
                nuevoVx = vx,
                nuevoVy = vy,
                nuevoRadio = Random.nextDouble(2.0, 4.0),
                nuevaVidaTotal = vidaTotal,
                nuevoColor = COLORES_LANZAMIENTO[i % COLORES_LANZAMIENTO.size]
            )
            _particulas.add(particula)
        }
    }

    /**
     * Actualiza la física de todas las partículas activas dado un paso de tiempo `dt` en segundos,
     * reciclando al pool las partículas expiradas sin generar basura en memoria.
     */
    fun actualizar(dtSegundos: Double) {
        if (_particulas.isEmpty()) return
        val dt = dtSegundos.coerceIn(0.0, 0.08)
        var i = _particulas.size - 1
        while (i >= 0) {
            val p = _particulas[i]
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 65.0 * dt
            p.vida -= dt
            if (p.vida <= 0.0) {
                _particulas.removeAt(i)
                _poolInactivas.addLast(p)
            }
            i--
        }
    }

    /** Recicla todas las partículas activas de vuelta al pool interno. */
    fun limpiar() {
        while (_particulas.isNotEmpty()) {
            _poolInactivas.addLast(_particulas.removeAt(_particulas.size - 1))
        }
    }
}

/**
 * Objeto utilitario que centraliza las primitivas de dibujo avanzado en JavaFX Canvas (DRY).
 * Reutiliza instancias pre-alojadas de colores, fuentes, gradientes y arreglos de vértices (Issue #21).
 */
object GraficosUtils {

    // Caché inmutable de colores frecuentes para evitar instanciación por frame en `handle(now)`
    private val COLOR_VEL_BAJA: Color = Color.web("#38bdf8")
    private val COLOR_VEL_MEDIA: Color = Color.web("#34d399")
    private val COLOR_VEL_ALTA: Color = Color.web("#fbbf24")
    private val COLOR_VEL_EXTREMA: Color = Color.web("#f43f5e")

    private val COLOR_GRID_LINEA: Color = Color.rgb(51, 65, 85, 0.42)
    private val COLOR_GRID_MARCA: Color = Color.rgb(56, 189, 248, 0.55)
    private val COLOR_GRID_TEXTO: Color = Color.rgb(148, 163, 184, 0.78)
    private val COLOR_EJE_CENTRAL: Color = Color.rgb(56, 189, 248, 0.22)

    private val COLOR_NUCLEO_FUEGO: Color = Color.rgb(254, 249, 195, 0.85)
    private val COLOR_HALO_SUELO: Color = Color.rgb(74, 222, 128, 0.25)
    private val COLOR_HALO_AIRE: Color = Color.rgb(249, 115, 22, 0.24)
    private val COLOR_BORDE_METEORITO_SUELO: Color = Color.web("#4ade80")
    private val COLOR_BORDE_METEORITO_AIRE: Color = Color.web("#fb923c")
    private val COLOR_CRATER_INTERNO: Color = Color.rgb(28, 25, 23, 0.65)
    private val COLOR_CRATER_BRILLO: Color = Color.rgb(254, 215, 170, 0.45)

    private val COLOR_TELEMETRIA_FONDO: Color = Color.rgb(15, 23, 42, 0.82)
    private val COLOR_TELEMETRIA_BORDE: Color = Color.rgb(56, 189, 248, 0.45)
    private val COLOR_TELEMETRIA_TEXTO: Color = Color.web("#e2e8f0")

    private val STOP_FUEGO_0 = Stop(0.0, Color.rgb(254, 240, 138, 0.92))
    private val STOP_FUEGO_1 = Stop(0.35, Color.rgb(249, 115, 22, 0.80))
    private val STOP_FUEGO_2 = Stop(0.75, Color.rgb(239, 68, 68, 0.48))
    private val STOP_FUEGO_3 = Stop(1.0, Color.rgb(239, 68, 68, 0.0))
    private val STOPS_ESTELA_FUEGO: List<Stop> = listOf(STOP_FUEGO_0, STOP_FUEGO_1, STOP_FUEGO_2, STOP_FUEGO_3)

    // Gradientes proporcionales cacheados para la estela de fuego (hacia abajo y hacia arriba)
    private val GRADIENTE_FUEGO_POSITIVO: LinearGradient = LinearGradient(
        0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, STOPS_ESTELA_FUEGO
    )
    private val GRADIENTE_FUEGO_NEGATIVO: LinearGradient = LinearGradient(
        0.0, 1.0, 0.0, 0.0, true, CycleMethod.NO_CYCLE, STOPS_ESTELA_FUEGO
    )

    // Gradientes radiales cacheados del cuerpo del meteorito
    private val GRADIENTE_METEORITO_SUELO: RadialGradient = crearGradienteRadialEsfera(
        colorBrillo = Color.web("#fde68a"),
        colorMedio = Color.web("#78716c"),
        colorBorde = Color.web("#292524")
    )
    private val GRADIENTE_METEORITO_AIRE: RadialGradient = crearGradienteRadialEsfera(
        colorBrillo = Color.web("#fdba74"),
        colorMedio = Color.web("#9a3412"),
        colorBorde = Color.web("#431407")
    )

    // Caché de fuentes tipográficas
    private val FONT_CONSOLAS_9: Font = Font.font("Consolas", FontWeight.NORMAL, 9.0)
    private val FONT_CONSOLAS_BOLD_9_5: Font = Font.font("Consolas", FontWeight.BOLD, 9.5)
    private val FONT_SEGOE_BOLD_10: Font = Font.font("Segoe UI", FontWeight.BOLD, 10.0)

    // Geometría precalculada del polígono de 10 vértices del meteorito (evita `cos`/`sin` y `DoubleArray` por frame)
    private val FACTORES_RADIO_METEORITO: DoubleArray =
        doubleArrayOf(1.02, 0.88, 1.06, 0.91, 1.04, 0.86, 0.98, 0.90, 1.05, 0.89)
    private val N_VERTICES_METEORITO: Int = FACTORES_RADIO_METEORITO.size
    private val COS_UNITARIO_METEORITO: DoubleArray = DoubleArray(N_VERTICES_METEORITO) { i ->
        cos((2.0 * Math.PI * i) / N_VERTICES_METEORITO) * FACTORES_RADIO_METEORITO[i]
    }
    private val SIN_UNITARIO_METEORITO: DoubleArray = DoubleArray(N_VERTICES_METEORITO) { i ->
        sin((2.0 * Math.PI * i) / N_VERTICES_METEORITO) * FACTORES_RADIO_METEORITO[i]
    }

    // Buffers reutilizables de coordenadas para polígonos (hilo único de JavaFX Application Thread)
    private val bufferMeteoritoX: DoubleArray = DoubleArray(N_VERTICES_METEORITO)
    private val bufferMeteoritoY: DoubleArray = DoubleArray(N_VERTICES_METEORITO)
    private val bufferTrianguloX: DoubleArray = DoubleArray(3)
    private val bufferTrianguloY: DoubleArray = DoubleArray(3)

    /**
     * Crea un gradiente lineal vertical de 3 paradas cromáticas para fondos con profundidad espacial.
     */
    fun crearGradienteVertical(
        colorSuperior: String,
        colorMedio: String,
        colorInferior: String
    ): LinearGradient =
        LinearGradient(
            0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE,
            Stop(0.0, Color.web(colorSuperior)),
            Stop(0.55, Color.web(colorMedio)),
            Stop(1.0, Color.web(colorInferior))
        )

    /**
     * Crea un gradiente radial con iluminación desplazada para dar volumen 3D al objeto simulado.
     */
    fun crearGradienteRadialEsfera(
        colorBrillo: Color,
        colorMedio: Color,
        colorBorde: Color
    ): RadialGradient =
        RadialGradient(
            -35.0, 0.35, 0.35, 0.35, 0.85, true, CycleMethod.NO_CYCLE,
            Stop(0.0, colorBrillo),
            Stop(0.55, colorMedio),
            Stop(1.0, colorBorde)
        )

    /**
     * Retorna una instancia cacheada de [Color] según la magnitud instantánea de velocidad (m/s),
     * sin instanciar nuevos objetos [Color] durante el bucle de animación (Issue #21).
     */
    fun obtenerColorPorVelocidad(velocidad: Double): Color {
        val mag = abs(velocidad)
        return when {
            mag < 8.0 -> COLOR_VEL_BAJA     // Cian neón (velocidad baja)
            mag < 18.0 -> COLOR_VEL_MEDIA   // Esmeralda neón (velocidad media)
            mag < 28.0 -> COLOR_VEL_ALTA    // Ámbar energético (velocidad alta)
            else -> COLOR_VEL_EXTREMA       // Magenta/Coral supersónico (velocidad extrema)
        }
    }

    /**
     * Dibuja la cuadrícula de medición física (grid con marcas graduadas en metros y eje guía central).
     */
    fun dibujarCuadriculaMedicion(
        gc: GraphicsContext,
        ancho: Double,
        inicioY: Double,
        sueloY: Double,
        alturaMaximaMetros: Double,
        direccionAscendente: Boolean
    ) {
        val divisiones = 4
        val rangoPixeles = (sueloY - inicioY).coerceAtLeast(1.0)

        gc.font = FONT_CONSOLAS_9
        for (i in 1 until divisiones) {
            val fraccion = i.toDouble() / divisiones
            val yLinea = inicioY + fraccion * rangoPixeles
            val metrosMarca = if (direccionAscendente) {
                alturaMaximaMetros * (1.0 - fraccion)
            } else {
                alturaMaximaMetros * fraccion
            }

            // Línea horizontal tenue de la cuadrícula física
            gc.stroke = COLOR_GRID_LINEA
            gc.lineWidth = 0.8
            gc.strokeLine(28.0, yLinea, ancho - 28.0, yLinea)

            // Marca lateral de regla en metros
            gc.stroke = COLOR_GRID_MARCA
            gc.lineWidth = 1.2
            gc.strokeLine(22.0, yLinea, 28.0, yLinea)

            gc.fill = COLOR_GRID_TEXTO
            gc.fillText(String.format(Locale.US, "%.0fm", metrosMarca), 4.0, yLinea + 3.0)
        }

        // Eje central de trayectoria
        gc.stroke = COLOR_EJE_CENTRAL
        gc.lineWidth = 1.2
        gc.strokeLine(ancho / 2.0, inicioY, ancho / 2.0, sueloY)
    }

    /**
     * Dibuja la línea de estela / trayectoria (Trail Effect) semi-transparente detrás del objeto,
     * reutilizando colores cacheados y modulando `globalAlpha` del [GraphicsContext] para evitar
     * instanciar objetos [Color] por cada segmento de estela (Issue #21).
     */
    fun dibujarEstela(
        gc: GraphicsContext,
        puntos: List<PuntoEstela>,
        radioObjeto: Double
    ) {
        if (puntos.size < 2) return

        val alphaOriginal = gc.globalAlpha
        val total = puntos.size
        for (i in 1 until total) {
            val pAnterior = puntos[i - 1]
            val pActual = puntos[i]
            val progreso = i.toDouble() / total
            val colorBase = obtenerColorPorVelocidad(pActual.velocidad)

            gc.globalAlpha = (progreso * 0.55).coerceIn(0.05, 0.65)
            gc.stroke = colorBase
            gc.lineWidth = (radioObjeto * 0.55 * progreso).coerceAtLeast(1.5)
            gc.strokeLine(pAnterior.x, pAnterior.y, pActual.x, pActual.y)

            // Nodos luminosos discretos a lo largo de la estela
            if (i % 2 == 0) {
                val r = (radioObjeto * 0.32 * progreso).coerceAtLeast(1.2)
                gc.globalAlpha = (progreso * 0.35).coerceIn(0.05, 0.5)
                gc.fill = colorBase
                gc.fillOval(pActual.x - r, pActual.y - r, r * 2.0, r * 2.0)
            }
        }
        gc.globalAlpha = alphaOriginal
    }

    /**
     * Dibuja una sombra elíptica proyectada sobre el suelo cuya escala y opacidad dependen de la cercanía.
     */
    fun dibujarSombraProyectada(
        gc: GraphicsContext,
        centroX: Double,
        sueloY: Double,
        radioObjeto: Double,
        proximidadSuelo: Double
    ) {
        val factor = proximidadSuelo.coerceIn(0.15, 1.0)
        val anchoSombra = radioObjeto * 2.4 * (0.5 + 0.5 * factor)
        val altoSombra = radioObjeto * 0.55 * (0.4 + 0.6 * factor)

        val alphaOriginal = gc.globalAlpha
        gc.globalAlpha = (0.65 * factor).coerceIn(0.1, 0.75)
        gc.fill = Color.BLACK
        gc.fillOval(centroX - anchoSombra / 2.0, sueloY - altoSombra / 2.0, anchoSombra, altoSombra)

        // Halo neón de contacto cuando está muy cerca del suelo
        if (factor > 0.85) {
            gc.globalAlpha = ((factor - 0.8) * 2.5).coerceIn(0.05, 0.9)
            gc.stroke = COLOR_VEL_BAJA
            gc.lineWidth = 1.2
            gc.strokeOval(centroX - anchoSombra / 2.0, sueloY - altoSombra / 2.0, anchoSombra, altoSombra)
        }
        gc.globalAlpha = alphaOriginal
    }

    /**
     * Calcula el ángulo de rotación dinámica del meteorito en grados en función del tiempo
     * transcurrido y la velocidad instantánea (DRY).
     */
    fun calcularAnguloRotacionMeteorito(tiempoSegundos: Double, velocidadY: Double): Double {
        if (tiempoSegundos <= 0.0) return 0.0
        val factorRapidez = (1.0 + abs(velocidadY) * 0.08).coerceIn(1.0, 3.5)
        return (tiempoSegundos * 135.0 * factorRapidez) % 360.0
    }

    /**
     * Determina la dirección vertical de la estela de fuego en coordenadas de pantalla (`-1.0` = hacia arriba,
     * `+1.0` = hacia abajo), siempre opuesta al vector instantáneo de velocidad ($v$).
     */
    fun calcularDireccionEstelaFuego(velocidadY: Double, direccion: DireccionMovimiento): Double =
        if (direccion == DireccionMovimiento.ARRIBA) {
            if (velocidadY >= 0.0) 1.0 else -1.0
        } else {
            -1.0
        }

    /**
     * Intenta cargar de forma segura la imagen opcional `/assets/meteorito.png` si existe en el classpath.
     */
    fun cargarSpriteMeteoritoOpcional(rutaRecurso: String = "/assets/meteorito.png"): Image? =
        try {
            val stream = GraficosUtils::class.java.getResourceAsStream(rutaRecurso)
            if (stream != null) Image(stream) else null
        } catch (_: Exception) {
            null
        }

    /**
     * Dibuja la estela ígnea de fuego y plasma detrás del meteorito reutilizando gradientes
     * proporcionales y buffers de vértices pre-alojados (Cero GC en `handle`).
     */
    fun dibujarEstelaFuegoMeteorito(
        gc: GraphicsContext,
        centroX: Double,
        centroY: Double,
        radio: Double,
        velocidadY: Double,
        enSuelo: Boolean,
        direccion: DireccionMovimiento
    ) {
        val rapidez = abs(velocidadY)
        if (enSuelo || rapidez < 0.15) return

        val sentidoColaY = calcularDireccionEstelaFuego(velocidadY, direccion)
        val largoEstela = (radio * 1.6 + rapidez * 1.35).coerceIn(radio * 1.4, radio * 4.2)
        val yPuntaCola = centroY + sentidoColaY * largoEstela
        val yInicioCola = centroY + sentidoColaY * (radio * 0.45)

        // Cono exterior de fuego usando gradiente proporcional cacheado y buffer reutilizable
        gc.fill = if (sentidoColaY >= 0.0) GRADIENTE_FUEGO_POSITIVO else GRADIENTE_FUEGO_NEGATIVO
        bufferTrianguloX[0] = centroX - radio * 0.82
        bufferTrianguloX[1] = centroX + radio * 0.82
        bufferTrianguloX[2] = centroX
        bufferTrianguloY[0] = yInicioCola
        bufferTrianguloY[1] = yInicioCola
        bufferTrianguloY[2] = yPuntaCola
        gc.fillPolygon(bufferTrianguloX, bufferTrianguloY, 3)

        // Núcleo interior incandescente
        val yPuntaNucleo = centroY + sentidoColaY * (largoEstela * 0.58)
        gc.fill = COLOR_NUCLEO_FUEGO
        bufferTrianguloX[0] = centroX - radio * 0.42
        bufferTrianguloX[1] = centroX + radio * 0.42
        bufferTrianguloX[2] = centroX
        bufferTrianguloY[0] = yInicioCola
        bufferTrianguloY[1] = yInicioCola
        bufferTrianguloY[2] = yPuntaNucleo
        gc.fillPolygon(bufferTrianguloX, bufferTrianguloY, 3)
    }

    /**
     * Dibuja el cuerpo detallado del meteorito en el [Canvas] aplicando rotación dinámica,
     * silueta poligonal irregular precalculada y gradientes cacheados.
     */
    fun dibujarCuerpoMeteorito(
        gc: GraphicsContext,
        centroX: Double,
        centroY: Double,
        radio: Double,
        velocidadY: Double,
        tiempo: Double,
        enSuelo: Boolean,
        direccion: DireccionMovimiento,
        imagenSprite: Image? = null
    ) {
        // 1. Estela de fuego orientada según el vector velocidad instantáneo
        dibujarEstelaFuegoMeteorito(gc, centroX, centroY, radio, velocidadY, enSuelo, direccion)

        // 2. Halo térmico exterior alrededor del meteorito
        val radioHalo = radio * 1.48
        gc.fill = if (enSuelo) COLOR_HALO_SUELO else COLOR_HALO_AIRE
        gc.fillOval(centroX - radioHalo, centroY - radioHalo, radioHalo * 2.0, radioHalo * 2.0)

        // 3. Transformación de rotación dinámica sobre el eje del meteorito
        val anguloRotacion = if (enSuelo) 0.0 else calcularAnguloRotacionMeteorito(tiempo, velocidadY)
        gc.save()
        gc.translate(centroX, centroY)
        gc.rotate(anguloRotacion)

        if (imagenSprite != null && !imagenSprite.isError && imagenSprite.width > 0) {
            gc.drawImage(imagenSprite, -radio, -radio, radio * 2.0, radio * 2.0)
        } else {
            for (i in 0 until N_VERTICES_METEORITO) {
                bufferMeteoritoX[i] = COS_UNITARIO_METEORITO[i] * radio
                bufferMeteoritoY[i] = SIN_UNITARIO_METEORITO[i] * radio
            }

            gc.fill = if (enSuelo) GRADIENTE_METEORITO_SUELO else GRADIENTE_METEORITO_AIRE
            gc.fillPolygon(bufferMeteoritoX, bufferMeteoritoY, N_VERTICES_METEORITO)

            gc.stroke = if (enSuelo) COLOR_BORDE_METEORITO_SUELO else COLOR_BORDE_METEORITO_AIRE
            gc.lineWidth = 1.5
            gc.strokePolygon(bufferMeteoritoX, bufferMeteoritoY, N_VERTICES_METEORITO)

            // Cráteres internos tallados sobre la superficie del meteorito
            gc.fill = COLOR_CRATER_INTERNO
            gc.fillOval(-radio * 0.45, -radio * 0.38, radio * 0.38, radio * 0.32)
            gc.fillOval(radio * 0.12, -radio * 0.10, radio * 0.34, radio * 0.30)
            gc.fillOval(-radio * 0.18, radio * 0.18, radio * 0.28, radio * 0.24)

            // Brillo especular sobre el borde del cráter principal
            gc.stroke = COLOR_CRATER_BRILLO
            gc.lineWidth = 0.9
            gc.strokeOval(-radio * 0.45, -radio * 0.38, radio * 0.38, radio * 0.32)
        }

        gc.restore()
    }

    /**
     * Renderiza el sprite detallado del objeto físico delegando al renderizado del meteorito (DRY).
     */
    fun dibujarSpriteObjeto(
        gc: GraphicsContext,
        centroX: Double,
        centroY: Double,
        radio: Double,
        velocidadY: Double,
        enSuelo: Boolean,
        direccion: DireccionMovimiento,
        tiempo: Double = 0.0
    ) {
        dibujarCuerpoMeteorito(
            gc = gc,
            centroX = centroX,
            centroY = centroY,
            radio = radio,
            velocidadY = velocidadY,
            tiempo = tiempo,
            enSuelo = enSuelo,
            direccion = direccion
        )
    }

    /**
     * Dibuja un vector dinámico de velocidad ($v$) sobre el objeto cuyo largo, dirección y color
     * se ajustan en tiempo real a la velocidad instantánea.
     */
    fun dibujarVectorVelocidad(
        gc: GraphicsContext,
        centroX: Double,
        centroY: Double,
        radioObjeto: Double,
        velocidadY: Double,
        direccionConfigurada: DireccionMovimiento
    ) {
        val rapidez = abs(velocidadY)
        if (rapidez < 0.15) return

        val sentidoPantallaY = if (direccionConfigurada == DireccionMovimiento.ARRIBA) {
            if (velocidadY >= 0.0) -1.0 else 1.0
        } else {
            1.0
        }

        val longitudFlecha = (14.0 + rapidez * 1.45).coerceIn(16.0, 58.0)
        val xOrigen = centroX + radioObjeto + 10.0
        val yOrigen = centroY
        val yDestino = (yOrigen + sentidoPantallaY * longitudFlecha).coerceIn(18.0, gc.canvas.height - 18.0)

        val colorVector = obtenerColorPorVelocidad(rapidez)
        dibujarFlecha(gc, xOrigen, yOrigen, xOrigen, yDestino, colorVector, grosor = 2.2)

        gc.fill = colorVector
        gc.font = FONT_CONSOLAS_BOLD_9_5
        val textoVector = String.format(Locale.US, "v=%.1f", rapidez)
        gc.fillText(textoVector, xOrigen + 5.0, (yOrigen + yDestino) / 2.0 + 3.0)
    }

    /**
     * Traza un segmento con punta de flecha orientada desde `(x1, y1)` hasta `(x2, y2)`
     * reutilizando buffers pre-alojados.
     */
    fun dibujarFlecha(
        gc: GraphicsContext,
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        color: Color,
        grosor: Double = 2.0
    ) {
        gc.stroke = color
        gc.fill = color
        gc.lineWidth = grosor
        gc.strokeLine(x1, y1, x2, y2)

        val angulo = atan2(y2 - y1, x2 - x1)
        val tamPunta = 6.5
        val anguloApertura = Math.toRadians(28.0)

        bufferTrianguloX[0] = x2
        bufferTrianguloX[1] = x2 - tamPunta * cos(angulo - anguloApertura)
        bufferTrianguloX[2] = x2 - tamPunta * cos(angulo + anguloApertura)

        bufferTrianguloY[0] = y2
        bufferTrianguloY[1] = y2 - tamPunta * sin(angulo - anguloApertura)
        bufferTrianguloY[2] = y2 - tamPunta * sin(angulo + anguloApertura)

        gc.fillPolygon(bufferTrianguloX, bufferTrianguloY, 3)
    }

    /**
     * Dibuja todas las partículas activas del sistema modulando `globalAlpha` sin instanciar
     * nuevos objetos [Color] por partícula (Issue #21).
     */
    fun dibujarParticulas(gc: GraphicsContext, particulas: List<ParticulaGrafica>) {
        if (particulas.isEmpty()) return
        val alphaOriginal = gc.globalAlpha
        for (i in particulas.indices) {
            val p = particulas[i]
            val alpha = p.opacidad
            if (alpha <= 0.01) continue
            gc.globalAlpha = alpha
            gc.fill = p.color
            val r = p.radio * (0.5 + 0.5 * alpha)
            gc.fillOval(p.x - r, p.y - r, r * 2.0, r * 2.0)
        }
        gc.globalAlpha = alphaOriginal
    }

    /**
     * Dibuja el panel superior de telemetría física en tiempo real sobre el Canvas.
     */
    fun dibujarPanelTelemetria(
        gc: GraphicsContext,
        ancho: Double,
        tiempo: Double,
        posicionY: Double,
        velocidadY: Double,
        simboloAltura: String = "y"
    ) {
        val anchoCaja = 186.0
        val altoCaja = 20.0
        val xCaja = ancho - anchoCaja - 8.0
        val yCaja = 6.0

        gc.fill = COLOR_TELEMETRIA_FONDO
        gc.fillRoundRect(xCaja, yCaja, anchoCaja, altoCaja, 6.0, 6.0)
        gc.stroke = COLOR_TELEMETRIA_BORDE
        gc.lineWidth = 1.0
        gc.strokeRoundRect(xCaja, yCaja, anchoCaja, altoCaja, 6.0, 6.0)

        gc.fill = COLOR_TELEMETRIA_TEXTO
        gc.font = FONT_SEGOE_BOLD_10
        val infoTexto = String.format(
            Locale.US,
            "t: %.2fs | %s: %.1fm | v: %.1fm/s",
            tiempo,
            simboloAltura,
            posicionY,
            velocidadY
        )
        gc.fillText(infoTexto, xCaja + 7.0, yCaja + 13.5)
    }
}
