package gravityquest
import gravityquest.models.Dificultad; import org.example.Juego.*; import org.junit.jupiter.api.Assertions.*; import org.junit.jupiter.api.Test
internal typealias ctorCalcTest = CalculadoraFisica; internal typealias ctorObjTest = ObjetoSimulado; internal typealias ctorCtrlTest = ControladorSimulacion
internal typealias ctorEstrategiaMRU = EstrategiaMRU; internal typealias ctorEstrategiaCaida = EstrategiaCaidaLibre
internal typealias ctorEstrategiaTiro = EstrategiaTiroVertical; internal typealias ctorEstrategiaGen = EstrategiaGenerica

class CalculadoraCaidaLibreTest {

    private val calc: CalculadoraFisica = ctorCalcTest()

    @Test
    fun testCalculosCaidaLibreBasicos() {
        assertEquals(19.6, calc.calcularAlturaCaidaLibre(2.0), 1e-3)
        assertEquals(19.6, calc.calcularVelocidadFinalCaidaLibre(2.0), 1e-3)
        assertEquals(19.6, calc.calcularVelocidadPorAltura(19.6), 1e-3)
        assertEquals(2.0, calc.calcularTiempoCaidaLibre(19.6), 1e-3)
    }

    @Test
    fun testCalculosLanzamientoVertical() {
        assertEquals(15.4, calc.calcularPosicionVertical(45.0, 5.0, 2.0), 1e-3)
        assertEquals(24.6, calc.calcularVelocidadConVelocidadInicial(5.0, 2.0), 1e-3)
        assertEquals(2.0, calc.calcularTiempoConVelocidadInicial(29.6, 5.0), 1e-3)
    }

    @Test
    fun testCalculosLanzamientoHaciaArriba() {
        assertEquals(19.6, calc.calcularAlturaMaximaLanzamientoArriba(19.6), 1e-3)
        assertEquals(2.0, calc.calcularTiempoSubida(19.6), 1e-3)
        assertEquals(29.6, calc.calcularPosicionLanzamientoArriba(10.0, 19.6, 2.0), 1e-3)
    }

    @Test
    fun testValidacionParametrosInvalidos() {
        assertThrows(IllegalArgumentException::class.java) {
            calc.calcularAlturaCaidaLibre(-1.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            calc.calcularTiempoCaidaLibre(-5.0)
        }
    }

    @Test
    fun testVerificacionConTolerancia() {
        assertTrue(calc.verificarRespuesta(19.8, 20.0))
        assertFalse(calc.verificarRespuesta(25.0, 20.0))
        assertFalse(calc.verificarRespuesta(Double.NaN, 20.0))
    }
}

class EstrategiasYObjetoSimuladoTest {

    @Test
    fun testPolimorfismoEstrategiasCinematica() {
        val mru = ctorEstrategiaMRU()
        val caida = ctorEstrategiaCaida()
        val tiro = ctorEstrategiaTiro()
        val gen = ctorEstrategiaGen()
        assertEquals(10.0, mru.calcularPosicion(20.0, 5.0, 2.0), 1e-3)
        assertEquals(0.4, caida.calcularPosicion(20.0, 0.0, 2.0), 1e-3)
        assertEquals(20.4, tiro.calcularPosicion(0.0, 20.0, 2.0), 1e-3)
        assertEquals(15.0, gen.calcularPosicion(15.0, 3.0, 1.0), 1e-3)
    }

    @Test
    fun testEvolucionObjetoSimuladoYReinicio() {
        val obj = ctorObjTest(alturaInicial = 19.6, velocidadInicial = 0.0)
        assertFalse(obj.enSuelo)
        obj.actualizar(1.0)
        assertEquals(14.7, obj.alturaActual, 1e-3)
        assertEquals(9.8, obj.velocidadActual, 1e-3)
        obj.actualizar(1.5)
        assertTrue(obj.enSuelo)
        assertEquals(0.0, obj.alturaActual, 1e-3)
        obj.reiniciar()
        assertFalse(obj.enSuelo)
        assertEquals(19.6, obj.alturaActual, 1e-3)
    }

    @Test
    fun testControladorYMotorSimulacion() {
        var impactos = 0
        val ctrl = ctorCtrlTest(alturaInicial = 4.9, alImpactar = { impactos += 1 })
        ctrl.configurarEscenarioPorDificultad(Dificultad.DIFICIL)
        assertEquals(TipoEscenario.TORMENTA_GRAVITACIONAL, ctrl.escenarioActual)
        ctrl.iniciar()
        assertTrue(ctrl.enEjecucion)
        ctrl.avanzarFrame(1.2)
        assertFalse(ctrl.enEjecucion)
        assertEquals(1, impactos)
        ctrl.reiniciar(nuevaAltura = 45.0, nuevaVelocidad = 5.0, nuevaDireccion = DireccionMovimiento.LANZAMIENTO_ABAJO)
        assertEquals(45.0, ctrl.objetoSimulado.alturaInicial, 1e-3)
    }
}
