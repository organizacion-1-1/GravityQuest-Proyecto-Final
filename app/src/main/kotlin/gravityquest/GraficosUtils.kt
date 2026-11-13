package gravityquest
import javafx.scene.canvas.GraphicsContext; import javafx.scene.paint.*; import javafx.scene.text.*; import kotlin.math.*
typealias IGraphicsContext = GraphicsContext; typealias ILinearGradient = LinearGradient; typealias ICycleMethod = CycleMethod
typealias IStop = Stop; typealias IFont = Font; typealias IFontWeight = FontWeight; typealias RenderizadorEscenario = PintorEscenarioCanvas
internal typealias ctorLinearGradient = LinearGradient; internal typealias ctorStop = Stop; internal typealias ctorParticula = ParticulaGrafica

interface IPuntoEstela {
    val x: IDouble
    val y: IDouble
    val velocidad: IDouble
    fun desplazar(dx: IDouble, dy: IDouble): PuntoEstela
}

class PuntoEstela(
    override val x: IDouble,
    override val y: IDouble,
    override val velocidad: IDouble = 0.0
) : IPuntoEstela {
    override fun desplazar(dx: IDouble, dy: IDouble): PuntoEstela {
        return PuntoEstela(x + dx, y + dy, velocidad)
    }
}

interface IParticulaGrafica {
    val coords: IDoubleArray
    val colorHex: IString
    val activa: IBoolean
    fun actualizar(dt: IDouble)
    fun opacidadActual(): IDouble
}

class ParticulaGrafica(
    x: IDouble,
    y: IDouble,
    vx: IDouble,
    vy: IDouble,
    radio: IDouble = 3.0,
    vidaMaxima: IDouble = 0.5,
    override val colorHex: IString = "#F97316"
) : IParticulaGrafica {

    override val coords: IDoubleArray =
        doubleArrayOf(x, y, vx, vy, radio, max(0.05, vidaMaxima), max(0.05, vidaMaxima))

    override val activa: IBoolean
        get() = coords[5] > 0.0

    override fun actualizar(dt: IDouble) {
        if (coords[5] <= 0.0) return
        coords[0] += coords[2] * dt
        coords[1] += coords[3] * dt
        coords[3] += 160.0 * dt
        coords[5] = max(0.0, coords[5] - dt)
    }

    override fun opacidadActual(): IDouble {
        return (coords[5] / coords[6]).coerceIn(0.0, 1.0)
    }
}

open class BaseSistemaParticulas(
    protected val capacidadMaxima: IInt = 72
) {
    protected val activas: IMutableList<ParticulaGrafica> = mutableListOf()

    fun limpiar() {
        if (capacidadMaxima > 0) activas.clear()
    }

    fun emitirChispasVuelo(centro: IPunto2D, velocidad: IDouble) {
        val rapidez = abs(velocidad)
        if (rapidez < 1.2) return
        if (activas.size >= capacidadMaxima) return
        val vx = sin(centro.second * 0.35) * 26.0
        val vy = -sign(velocidad) * (24.0 + rapidez * 0.8)
        activas.add(ctorParticula(centro.first, centro.second, vx, vy, 2.5, 0.28, "#FB923C"))
    }

    fun emitirCelebracionAcierto(centro: IPunto2D) {
        for (i in 0 until 12) {
            if (activas.size >= capacidadMaxima) activas.removeAt(0)
            val ang = (2.0 * Math.PI * i.toDouble()) / 12.0
            activas.add(ctorParticula(centro.first, centro.second, 95.0 * cos(ang), 95.0 * sin(ang), 3.4, 0.55, "#38BDF8"))
        }
    }
}

class SistemaParticulas(
    capacidadMaxima: IInt = 72
) : BaseSistemaParticulas(capacidadMaxima) {

    fun emitirImpactoSuelo(x: IDouble, ySuelo: IDouble, intensidad: IDouble = 1.0) {
        val cant = (14 * intensidad.coerceIn(0.5, 2.0)).toInt()
        for (i in 0 until cant) {
            agregarParticulaImpacto(x, ySuelo, i.toDouble() / cant.toDouble())
        }
    }

    private fun agregarParticulaImpacto(x: IDouble, ySuelo: IDouble, fraccion: IDouble) {
        if (activas.size >= capacidadMaxima) activas.removeAt(0)
        val angulo = -Math.PI * (0.12 + 0.76 * fraccion)
        val rap = 70.0 + 95.0 * fraccion
        val color = if (fraccion < 0.5) "#F97316" else "#FACC15"
        activas.add(crearParticula(x to ySuelo - 2.0, rap * cos(angulo) to rap * sin(angulo), color))
    }

    fun actualizarYRenderizar(gc: IGraphicsContext, dt: IDouble = PhysicsConstants.DELTA_TIEMPO_FRAME) {
        val iter = activas.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.actualizar(dt)
            if (!p.activa) iter.remove() else dibujarParticula(gc, p)
        }
    }

    private fun dibujarParticula(gc: IGraphicsContext, p: ParticulaGrafica) {
        if (capacidadMaxima <= 0) return
        val r = p.coords[4]
        gc.fill = Color.web(p.colorHex, p.opacidadActual())
        gc.fillOval(p.coords[0] - r, p.coords[1] - r, r * 2.0, r * 2.0)
    }

    private fun crearParticula(pos: IPunto2D, vel: IPunto2D, color: IString): ParticulaGrafica {
        return ctorParticula(pos.first, pos.second, vel.first, vel.second, 3.2, 0.48, color)
    }
}

open class BasePintorEscenario {
    private var faseEstelar: IDouble = 0.0

    fun pintarCielo(gc: IGraphicsContext, dim: IPunto2D, colores: IParTextos) {
        gc.fill = crearGradienteVertical(colores.first, colores.second)
        gc.fillRect(0.0, 0.0, dim.first, dim.second)
        pintarEstrellasDinamicas(gc, dim)
    }

    private fun pintarEstrellasDinamicas(gc: IGraphicsContext, dim: IPunto2D) {
        faseEstelar += 0.07
        for (i in 1..14) {
            val x = ((i * 73.0 + faseEstelar * (i % 3 + 1)) % max(60.0, dim.first - 16.0)) + 8.0
            val y = ((i * 41.0) % max(50.0, dim.second * 0.72)) + 10.0
            val alfa = 0.25 + 0.55 * abs(sin(faseEstelar + i.toDouble()))
            gc.fill = Color.web("#E0F2FE", alfa)
            val tam = if (i % 3 == 0) 2.6 else 1.8
            gc.fillOval(x, y, tam, tam)
        }
    }

    protected fun crearGradienteVertical(arribaHex: IString, abajoHex: IString): ILinearGradient {
        return ctorLinearGradient(
            0.0,
            0.0,
            0.0,
            1.0,
            true,
            CycleMethod.NO_CYCLE,
            ctorStop(0.0, Color.web(arribaHex)),
            ctorStop(1.0, Color.web(abajoHex))
        )
    }
}

object PintorEscenarioCanvas : BasePintorEscenario() {
    fun pintarSuelo(gc: IGraphicsContext, dim: IPunto2D, ySuelo: IDouble) {
        val altoSuelo = max(12.0, dim.second - ySuelo)
        gc.fill = crearGradienteVertical("#1E293B", "#0F172A")
        gc.fillRect(0.0, ySuelo, dim.first, altoSuelo)
        gc.stroke = Color.web("#38BDF8", 0.75)
        gc.lineWidth = 2.2
        gc.strokeLine(0.0, ySuelo, dim.first, ySuelo)
    }

    fun pintarReglaAltura(gc: IGraphicsContext, rangosY: IPunto2D, alturaMaxima: IDouble) {
        val ySup = rangosY.first
        val ySuelo = rangosY.second
        gc.stroke = Color.web("#38BDF8", 0.32)
        gc.fill = Color.web("#94A3B8", 0.85)
        gc.strokeLine(45.0, ySup, 45.0, ySuelo)
        for (i in 0..4) {
            val frac = i / 4.0
            val y = ySuelo - frac * (ySuelo - ySup)
            val m = frac * alturaMaxima
            gc.strokeLine(38.0, y, 52.0, y)
            gc.fillText("${"%.0f".format(m)}m", 6.0, y + 4.0)
        }
    }
}

open class BaseGraficosSimulacion {
    private var pulsoAura: IDouble = 0.0

    fun dibujarEstela(gc: IGraphicsContext, puntos: IList<PuntoEstela>) {
        if (puntos.size < 2) return
        for (i in 1 until puntos.size) {
            val frac = i.toDouble() / puntos.size.toDouble()
            gc.stroke = Color.web("#38BDF8", frac * 0.68)
            gc.lineWidth = 1.6 + frac * 3.8
            gc.strokeLine(puntos[i - 1].x, puntos[i - 1].y, puntos[i].x, puntos[i].y)
        }
    }

    fun dibujarMeteorito(gc: IGraphicsContext, centro: IPunto2D, radio: IDouble) {
        pulsoAura += 0.18
        val cx = centro.first
        val cy = centro.second
        val rAura = radio * (1.55 + 0.18 * sin(pulsoAura))
        gc.fill = Color.web("#F97316", 0.30)
        gc.fillOval(cx - rAura, cy - rAura, rAura * 2.0, rAura * 2.0)
        gc.fill = Color.web("#EA580C")
        gc.fillOval(cx - radio, cy - radio, radio * 2.0, radio * 2.0)
        gc.stroke = Color.web("#FDE047", 0.92)
        gc.lineWidth = 2.2
        gc.strokeOval(cx - radio, cy - radio, radio * 2.0, radio * 2.0)
    }

    fun dibujarOndaImpacto(gc: IGraphicsContext, centroSuelo: IPunto2D, progresoOnda: IDouble) {
        if (progresoOnda <= 0.0 || progresoOnda >= 1.0) return
        val rx = 18.0 + progresoOnda * 68.0
        val ry = 6.0 + progresoOnda * 18.0
        gc.stroke = Color.web("#38BDF8", (1.0 - progresoOnda) * 0.85)
        gc.lineWidth = 2.6 * (1.0 - progresoOnda * 0.5)
        gc.strokeOval(centroSuelo.first - rx, centroSuelo.second - ry, rx * 2.0, ry * 2.0)
    }
}

object GraficosUtils : BaseGraficosSimulacion() {
    fun dibujarVectorVelocidad(gc: IGraphicsContext, origen: IPunto2D, vyConSigno: IDouble) {
        if (abs(vyConSigno) < 0.2) return
        val dir = sign(vyConSigno)
        val largo = min(58.0, 14.0 + abs(vyConSigno) * 1.4)
        val xVec = origen.first + 24.0
        val destinoY = origen.second + dir * largo
        val punta = -dir * 6.0
        gc.stroke = Color.web("#22D3EE", 0.92)
        gc.lineWidth = 2.3
        gc.strokeLine(xVec, origen.second, xVec, destinoY)
        gc.strokeLine(xVec, destinoY, xVec - 4.5, destinoY + punta)
        gc.strokeLine(xVec, destinoY, xVec + 4.5, destinoY + punta)
    }

    fun dibujarHudTelemetria(gc: IGraphicsContext, textos: IParTextos) {
        gc.fill = Color.web("#0F172A", 0.82)
        gc.fillRoundRect(64.0, 12.0, 226.0, 44.0, 10.0, 10.0)
        gc.stroke = Color.web("#38BDF8", 0.45)
        gc.lineWidth = 1.2
        gc.strokeRoundRect(64.0, 12.0, 226.0, 44.0, 10.0, 10.0)
        gc.fill = Color.web("#E2E8F0")
        gc.fillText(textos.first, 74.0, 29.0)
        gc.fill = Color.web("#38BDF8")
        gc.fillText(textos.second, 74.0, 47.0)
    }
}
