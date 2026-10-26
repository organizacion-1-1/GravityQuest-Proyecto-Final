package gravityquest

import javafx.scene.canvas.GraphicsContext
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
 * GravityQuest - Utilidades Gráficas y Sistema de Efectos Visuales (DRY & SRP)
 * ============================================================================
 *
 * 1. Principio DRY (Don't Repeat Yourself):
 *    - Centraliza en [GraficosUtils] la creación de gradientes espaciales, el trazado
 *      de cuadrículas de medición física, vectores de velocidad con puntas de flecha,
 *      estelas de trayectoria (Trail Effect), sombras proyectadas y renderizado de
 *      cápsulas/sondas con relieve.
 *
 * 2. Principio de Responsabilidad Única (SRP):
 *    - [SistemaParticulas] gestiona el ciclo de vida y cinemática de partículas de
 *      impacto y propulsión sin acoplarse a la lógica de negocio del juego.
 */

/**
 * Representa un punto histórico de la trayectoria para el efecto de estela (Trail Effect).
 */
data class PuntoEstela(
    val x: Double,
    val y: Double,
    val velocidad: Double
)

/**
 * Representa una partícula individual de polvo lunar o destello neón.
 */
data class ParticulaGrafica(
    var x: Double,
    var y: Double,
    var vx: Double,
    var vy: Double,
    var radio: Double,
    var vida: Double,
    val vidaMaxima: Double,
    val color: Color
) {
    val opacidad: Double
        get() = (vida / vidaMaxima).coerceIn(0.0, 1.0)
}

/**
 * Sistema ligero de partículas para efectos de impacto contra el suelo y propulsión de lanzamiento.
 */
class SistemaParticulas(
    private val maxParticulas: Int = 45
) {
    private val _particulas: MutableList<ParticulaGrafica> = mutableListOf()
    val particulasActivas: List<ParticulaGrafica>
        get() = _particulas.toList()

    /**
     * Genera un estallido de partículas (polvo y chispas neón) cuando el objeto impacta el suelo.
     */
    fun emitirImpactoSuelo(centroX: Double, sueloY: Double, magnitudVelocidad: Double = 15.0) {
        val intensidad = (abs(magnitudVelocidad) / 15.0).coerceIn(0.6, 2.2)
        val cantidad = 16
        val colores = listOf(
            Color.web("#38bdf8"),
            Color.web("#4ade80"),
            Color.web("#bae6fd"),
            Color.web("#94a3b8")
        )

        repeat(cantidad) { i ->
            if (_particulas.size >= maxParticulas) {
                _particulas.removeFirst()
            }
            val angulo = -Math.PI * (0.12 + 0.76 * (i.toDouble() / (cantidad - 1).coerceAtLeast(1)))
            val rapidez = (28.0 + Random.nextDouble(0.0, 48.0)) * intensidad
            val vidaTotal = 0.35 + Random.nextDouble(0.0, 0.35)
            _particulas.add(
                ParticulaGrafica(
                    x = centroX + Random.nextDouble(-6.0, 6.0),
                    y = sueloY - 2.0,
                    vx = cos(angulo) * rapidez,
                    vy = sin(angulo) * rapidez,
                    radio = Random.nextDouble(2.0, 4.5),
                    vida = vidaTotal,
                    vidaMaxima = vidaTotal,
                    color = colores[i % colores.size]
                )
            )
        }
    }

    /**
     * Genera destellos de propulsión cuando el objeto es disparado o lanzado inicialmente.
     */
    fun emitirDestelloLanzamiento(centroX: Double, origenY: Double, haciaArriba: Boolean) {
        val cantidad = 12
        val colores = listOf(
            Color.web("#38bdf8"),
            Color.web("#f43f5e"),
            Color.web("#fbbf24"),
            Color.web("#e0f2fe")
        )
        val signoY = if (haciaArriba) 1.0 else -1.0

        repeat(cantidad) { i ->
            if (_particulas.size >= maxParticulas) {
                _particulas.removeFirst()
            }
            val vx = Random.nextDouble(-35.0, 35.0)
            val vy = signoY * Random.nextDouble(18.0, 55.0)
            val vidaTotal = 0.28 + Random.nextDouble(0.0, 0.25)
            _particulas.add(
                ParticulaGrafica(
                    x = centroX + Random.nextDouble(-5.0, 5.0),
                    y = origenY,
                    vx = vx,
                    vy = vy,
                    radio = Random.nextDouble(2.0, 4.0),
                    vida = vidaTotal,
                    vidaMaxima = vidaTotal,
                    color = colores[i % colores.size]
                )
            )
        }
    }

    /**
     * Actualiza la física de todas las partículas activas dado un paso de tiempo `dt` en segundos.
     */
    fun actualizar(dtSegundos: Double) {
        val dt = dtSegundos.coerceIn(0.0, 0.05)
        val iter = _particulas.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 65.0 * dt // Gravedad visual suave sobre las chispas/polvo
            p.vida -= dt
            if (p.vida <= 0.0) {
                iter.remove()
            }
        }
    }

    /** Elimina todas las partículas activas. */
    fun limpiar() {
        _particulas.clear()
    }
}

/**
 * Objeto utilitario que centraliza las primitivas de dibujo avanzado en JavaFX Canvas (DRY).
 */
object GraficosUtils {

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
     * Calcula dinámicamente el color del vector de velocidad y estela según la magnitud instantánea (m/s).
     */
    fun obtenerColorPorVelocidad(velocidad: Double): Color {
        val mag = abs(velocidad)
        return when {
            mag < 8.0 -> Color.web("#38bdf8")   // Cian neón (velocidad baja)
            mag < 18.0 -> Color.web("#34d399")  // Esmeralda neón (velocidad media)
            mag < 28.0 -> Color.web("#fbbf24")  // Ámbar energético (velocidad alta)
            else -> Color.web("#f43f5e")        // Magenta/Coral supersónico (velocidad extrema)
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

        gc.font = Font.font("Consolas", FontWeight.NORMAL, 9.0)
        for (i in 1 until divisiones) {
            val fraccion = i.toDouble() / divisiones
            val yLinea = inicioY + fraccion * rangoPixeles
            val metrosMarca = if (direccionAscendente) {
                alturaMaximaMetros * (1.0 - fraccion)
            } else {
                alturaMaximaMetros * fraccion
            }

            // Línea horizontal tenue de la cuadrícula física
            gc.stroke = Color.rgb(51, 65, 85, 0.42)
            gc.lineWidth = 0.8
            gc.strokeLine(28.0, yLinea, ancho - 28.0, yLinea)

            // Marca lateral de regla en metros
            gc.stroke = Color.rgb(56, 189, 248, 0.55)
            gc.lineWidth = 1.2
            gc.strokeLine(22.0, yLinea, 28.0, yLinea)

            gc.fill = Color.rgb(148, 163, 184, 0.78)
            gc.fillText(String.format(Locale.US, "%.0fm", metrosMarca), 4.0, yLinea + 3.0)
        }

        // Eje central de trayectoria
        gc.stroke = Color.rgb(56, 189, 248, 0.22)
        gc.lineWidth = 1.2
        gc.strokeLine(ancho / 2.0, inicioY, ancho / 2.0, sueloY)
    }

    /**
     * Dibuja la línea de estela / trayectoria (Trail Effect) semi-transparente detrás del objeto.
     */
    fun dibujarEstela(
        gc: GraphicsContext,
        puntos: List<PuntoEstela>,
        radioObjeto: Double
    ) {
        if (puntos.size < 2) return

        val total = puntos.size
        for (i in 1 until total) {
            val pAnterior = puntos[i - 1]
            val pActual = puntos[i]
            val progreso = i.toDouble() / total
            val colorBase = obtenerColorPorVelocidad(pActual.velocidad)

            gc.stroke = Color.color(
                colorBase.red,
                colorBase.green,
                colorBase.blue,
                (progreso * 0.55).coerceIn(0.05, 0.65)
            )
            gc.lineWidth = (radioObjeto * 0.55 * progreso).coerceAtLeast(1.5)
            gc.strokeLine(pAnterior.x, pAnterior.y, pActual.x, pActual.y)

            // Nodos luminosos discretos a lo largo de la estela
            if (i % 2 == 0) {
                val r = (radioObjeto * 0.32 * progreso).coerceAtLeast(1.2)
                gc.fill = Color.color(colorBase.red, colorBase.green, colorBase.blue, progreso * 0.35)
                gc.fillOval(pActual.x - r, pActual.y - r, r * 2.0, r * 2.0)
            }
        }
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

        gc.fill = Color.rgb(2, 6, 23, (0.65 * factor).coerceIn(0.1, 0.75))
        gc.fillOval(centroX - anchoSombra / 2.0, sueloY - altoSombra / 2.0, anchoSombra, altoSombra)

        // Halo neón de contacto cuando está muy cerca del suelo
        if (factor > 0.85) {
            gc.stroke = Color.rgb(56, 189, 248, (factor - 0.8) * 2.5)
            gc.lineWidth = 1.2
            gc.strokeOval(centroX - anchoSombra / 2.0, sueloY - altoSombra / 2.0, anchoSombra, altoSombra)
        }
    }

    /**
     * Renderiza el sprite detallado del objeto físico (cápsula/sonda espacial con relieve 3D,
     * anillo energético neón, aletas estabilizadoras y brillo especular).
     */
    fun dibujarSpriteObjeto(
        gc: GraphicsContext,
        centroX: Double,
        centroY: Double,
        radio: Double,
        velocidadY: Double,
        enSuelo: Boolean,
        direccion: DireccionMovimiento
    ) {
        val diametro = radio * 2.0
        val pixelX = centroX - radio
        val pixelY = centroY - radio

        // 1. Halo exterior de energía neón
        val colorHalo = if (enSuelo) {
            Color.rgb(74, 222, 128, 0.28)
        } else {
            val c = obtenerColorPorVelocidad(velocidadY)
            Color.color(c.red, c.green, c.blue, 0.25)
        }
        val radioHalo = radio * 1.45
        gc.fill = colorHalo
        gc.fillOval(centroX - radioHalo, centroY - radioHalo, radioHalo * 2.0, radioHalo * 2.0)

        // 2. Aletas estabilizadoras de la sonda/cápsula
        gc.fill = Color.web("#475569")
        val offsetAleta = if (direccion == DireccionMovimiento.ARRIBA) radio * 0.35 else -radio * 0.35
        gc.fillPolygon(
            doubleArrayOf(centroX - radio * 0.85, centroX - radio * 1.35, centroX - radio * 0.65),
            doubleArrayOf(centroY, centroY + offsetAleta, centroY - offsetAleta * 0.5),
            3
        )
        gc.fillPolygon(
            doubleArrayOf(centroX + radio * 0.85, centroX + radio * 1.35, centroX + radio * 0.65),
            doubleArrayOf(centroY, centroY + offsetAleta, centroY - offsetAleta * 0.5),
            3
        )

        // 3. Cuerpo esférico con relieve 3D mediante RadialGradient
        val gradienteCuerpo = if (enSuelo) {
            crearGradienteRadialEsfera(
                colorBrillo = Color.web("#bbf7d0"),
                colorMedio = Color.web("#22c55e"),
                colorBorde = Color.web("#14532d")
            )
        } else {
            crearGradienteRadialEsfera(
                colorBrillo = Color.web("#e0f2fe"),
                colorMedio = Color.web("#0ea5e9"),
                colorBorde = Color.web("#0c4a6e")
            )
        }
        gc.fill = gradienteCuerpo
        gc.fillOval(pixelX, pixelY, diametro, diametro)

        // 4. Cinturón / anillo ecuatorial metálico de la cápsula
        gc.stroke = if (enSuelo) Color.web("#86efac") else Color.web("#7dd3fc")
        gc.lineWidth = 1.4
        gc.strokeLine(centroX - radio * 0.82, centroY, centroX + radio * 0.82, centroY)

        // 5. Contorno exterior nítido y brillo especular superior
        gc.stroke = Color.web("#f8fafc")
        gc.lineWidth = 1.5
        gc.strokeOval(pixelX, pixelY, diametro, diametro)

        gc.fill = Color.rgb(255, 255, 255, 0.65)
        gc.fillOval(centroX - radio * 0.45, centroY - radio * 0.55, radio * 0.42, radio * 0.28)
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

        // Determina el sentido visual en pantalla:
        // En lanzamiento hacia arriba: velocidadY > 0 sube (hacia Y menor), velocidadY < 0 baja (hacia Y mayor)
        // En movimiento hacia abajo: velocidadY > 0 baja (hacia Y mayor)
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

        // Etiqueta compacta del vector v
        gc.fill = colorVector
        gc.font = Font.font("Consolas", FontWeight.BOLD, 9.5)
        val textoVector = String.format(Locale.US, "v=%.1f", rapidez)
        gc.fillText(textoVector, xOrigen + 5.0, (yOrigen + yDestino) / 2.0 + 3.0)
    }

    /**
     * Traza un segmento con punta de flecha orientada desde `(x1, y1)` hasta `(x2, y2)`.
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

        val xPunta1 = x2 - tamPunta * cos(angulo - anguloApertura)
        val yPunta1 = y2 - tamPunta * sin(angulo - anguloApertura)
        val xPunta2 = x2 - tamPunta * cos(angulo + anguloApertura)
        val yPunta2 = y2 - tamPunta * sin(angulo + anguloApertura)

        gc.fillPolygon(
            doubleArrayOf(x2, xPunta1, xPunta2),
            doubleArrayOf(y2, yPunta1, yPunta2),
            3
        )
    }

    /**
     * Dibuja todas las partículas activas del sistema en el lienzo.
     */
    fun dibujarParticulas(gc: GraphicsContext, particulas: List<ParticulaGrafica>) {
        for (p in particulas) {
            val alpha = p.opacidad
            gc.fill = Color.color(p.color.red, p.color.green, p.color.blue, alpha)
            val r = p.radio * (0.5 + 0.5 * alpha)
            gc.fillOval(p.x - r, p.y - r, r * 2.0, r * 2.0)
        }
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

        gc.fill = Color.rgb(15, 23, 42, 0.82)
        gc.fillRoundRect(xCaja, yCaja, anchoCaja, altoCaja, 6.0, 6.0)
        gc.stroke = Color.rgb(56, 189, 248, 0.45)
        gc.lineWidth = 1.0
        gc.strokeRoundRect(xCaja, yCaja, anchoCaja, altoCaja, 6.0, 6.0)

        gc.fill = Color.web("#e2e8f0")
        gc.font = Font.font("Segoe UI", FontWeight.BOLD, 10.0)
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
