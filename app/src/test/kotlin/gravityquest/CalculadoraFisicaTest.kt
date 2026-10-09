package gravityquest

import org.junit.jupiter.api.assertThrows
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pruebas unitarias para validar la arquitectura POO y Principios SOLID de [CalculadoraFisica]
 * e [ICalculadoraFisica] para:
 * - Milestone 1: Caída libre (no regresión / OCP).
 * - Milestone 2 (Issue 12): Lanzamiento vertical hacia abajo (a favor de la gravedad).
 * - Milestone 2 (Issue 13): Lanzamiento vertical hacia arriba (en desaceleración contra la gravedad).
 */
class CalculadoraFisicaTest {

    private val calc: CalculadoraFisica = CalculadoraFisica()

    // ========================================================================
    // 1. Milestone 1: Caída Libre (Garantía OCP y Regresión)
    // ========================================================================

    @Test
    fun testConstanteGravedadTerrestre() {
        assertEquals(9.81, calc.g, 0.0001, "La constante g debe ser 9.81 m/s²")
    }

    @Test
    fun testCaidaLibreVelocidadFinalYTiempoValoresValidos() {
        // Altura h = 20.0 m
        val h = 20.0
        val vfEsperado = kotlin.math.sqrt(2.0 * 9.81 * h) // ~19.809 m/s
        val tEsperado = kotlin.math.sqrt(2.0 * h / 9.81)   // ~2.019 s

        assertEquals(vfEsperado, calc.calcularVelocidadFinal(h), 0.001)
        assertEquals(tEsperado, calc.calcularTiempo(h), 0.001)

        // Altura h = 0.0 m
        assertEquals(0.0, calc.calcularVelocidadFinal(0.0), 0.0001)
        assertEquals(0.0, calc.calcularTiempo(0.0), 0.0001)
    }

    @Test
    fun testCaidaLibrePosicionFisica() {
        assertEquals(0.0, calc.calcularPosicionCaida(0.0), 0.0001)
        assertEquals(0.5 * 9.81 * 1.0, calc.calcularPosicionCaida(1.0), 0.001)
        assertEquals(calc.calcularPosicionCaida(2.0), calc.calcularPosicion(2.0), 0.0001)
    }

    @Test
    fun testCaidaLibrePrecondicionesNegativas() {
        assertThrows<IllegalArgumentException> { calc.calcularVelocidadFinal(-5.0) }
        assertThrows<IllegalArgumentException> { calc.calcularTiempo(-1.0) }
        assertThrows<IllegalArgumentException> { calc.calcularPosicionCaida(-2.0) }
    }

    @Test
    fun testValidarResultadoTolerancia() {
        assertTrue(calc.validarResultado(2.02, 2.02, 0.05))
        assertTrue(calc.validarResultado(2.05, 2.02, 0.05))
        assertTrue(calc.validarResultado(1.98, 2.02, 0.05))
        assertFalse(calc.validarResultado(2.15, 2.02, 0.05))

        assertThrows<IllegalArgumentException> {
            calc.validarResultado(2.0, 2.0, -0.01)
        }
    }

    // ========================================================================
    // 2. Milestone 2 (Issue 12): Lanzamiento Vertical Hacia Abajo
    // ========================================================================

    @Test
    fun testLanzamientoAbajoValoresTeoricosValidos() {
        val v0 = 5.0
        val h = 20.0

        // vf = sqrt(v0^2 + 2gh) = sqrt(25 + 392.4) = sqrt(417.4) ≈ 20.43037 m/s
        val vfEsperado = kotlin.math.sqrt((v0 * v0) + (2.0 * 9.81 * h))
        val vfCalculado = calc.calcularVelocidadFinalLanzamientoAbajo(v0, h)
        assertEquals(vfEsperado, vfCalculado, 0.0001)
        assertTrue(calc.validarResultado(vfCalculado, 20.43, margenError = 0.05))

        // t = (-v0 + vf) / g ≈ (-5.0 + 20.43037) / 9.81 ≈ 1.57292 s
        val tEsperado = (-v0 + vfEsperado) / 9.81
        val tCalculado = calc.calcularTiempoLanzamientoAbajo(v0, h)
        assertEquals(tEsperado, tCalculado, 0.0001)
        assertTrue(calc.validarResultado(tCalculado, 1.57, margenError = 0.05))
    }

    @Test
    fun testLanzamientoAbajoConsistenciaOCPConCaidaLibreV0Cero() {
        val h = 20.0
        val vfCaida = calc.calcularVelocidadFinal(h)
        val vfAbajoV0Cero = calc.calcularVelocidadFinalLanzamientoAbajo(0.0, h)
        assertEquals(vfCaida, vfAbajoV0Cero, 1e-9, "Si v0 = 0, vf debe coincidir con caída libre")

        val tCaida = calc.calcularTiempo(h)
        val tAbajoV0Cero = calc.calcularTiempoLanzamientoAbajo(0.0, h)
        assertEquals(tCaida, tAbajoV0Cero, 1e-9, "Si v0 = 0, t debe coincidir con caída libre")
    }

    @Test
    fun testLanzamientoAbajoCasoBordeAlturaCero() {
        val v0 = 12.0
        // Si h = 0, vf = v0 y t = 0
        assertEquals(v0, calc.calcularVelocidadFinalLanzamientoAbajo(v0, 0.0), 1e-9)
        assertEquals(0.0, calc.calcularTiempoLanzamientoAbajo(v0, 0.0), 1e-9)
    }

    @Test
    fun testLanzamientoAbajoPrecondicionesVelocidadYAlturaNegativas() {
        // Velocidad negativa rechazada
        val exV0 = assertThrows<IllegalArgumentException> {
            calc.calcularVelocidadFinalLanzamientoAbajo(-5.0, 20.0)
        }
        assertTrue(exV0.message!!.contains("velocidad inicial no puede ser negativa", ignoreCase = true))

        assertThrows<IllegalArgumentException> {
            calc.calcularTiempoLanzamientoAbajo(-0.1, 20.0)
        }

        // Altura negativa rechazada
        val exH = assertThrows<IllegalArgumentException> {
            calc.calcularVelocidadFinalLanzamientoAbajo(5.0, -10.0)
        }
        assertTrue(exH.message!!.contains("altura no puede ser negativa", ignoreCase = true))

        assertThrows<IllegalArgumentException> {
            calc.calcularTiempoLanzamientoAbajo(5.0, -0.01)
        }
    }

    // ========================================================================
    // 3. Milestone 2 (Issue 13): Lanzamiento Vertical Hacia Arriba
    // ========================================================================

    @Test
    fun testLanzamientoArribaValoresTeoricosValidos() {
        val v0 = 15.0

        // h_max = v0^2 / (2g) = 225.0 / 19.62 ≈ 11.46789 m
        val hMaxEsperado = (v0 * v0) / (2.0 * 9.81)
        val hMaxCalculado = calc.calcularAlturaMaxima(v0)
        assertEquals(hMaxEsperado, hMaxCalculado, 0.0001)
        assertTrue(calc.validarResultado(hMaxCalculado, 11.47, margenError = 0.05))

        // t_subida = v0 / g = 15.0 / 9.81 ≈ 1.52905 s
        val tSubidaEsperado = v0 / 9.81
        val tSubidaCalculado = calc.calcularTiempoSubida(v0)
        assertEquals(tSubidaEsperado, tSubidaCalculado, 0.0001)
        assertTrue(calc.validarResultado(tSubidaCalculado, 1.53, margenError = 0.05))

        // t_total = 2 * v0 / g = 30.0 / 9.81 ≈ 3.05810 s
        val tTotalEsperado = (2.0 * v0) / 9.81
        val tTotalCalculado = calc.calcularTiempoTotalVuelo(v0)
        assertEquals(tTotalEsperado, tTotalCalculado, 0.0001)
        assertTrue(calc.validarResultado(tTotalCalculado, 3.06, margenError = 0.05))
    }

    @Test
    fun testLanzamientoArribaSimetriaTemporal() {
        val v0 = 28.5
        val tSubida = calc.calcularTiempoSubida(v0)
        val tTotal = calc.calcularTiempoTotalVuelo(v0)
        assertEquals(2.0 * tSubida, tTotal, 1e-9, "El tiempo total debe ser estrictamente el doble del tiempo de subida")
    }

    @Test
    fun testLanzamientoArribaContratosVelocidadInicialNoPositiva() {
        // v0 = 0.0 debe ser rechazada (requiere v0 > 0 estrictamente)
        val exCeroH = assertThrows<IllegalArgumentException> {
            calc.calcularAlturaMaxima(0.0)
        }
        assertTrue(exCeroH.message!!.contains("mayor a cero", ignoreCase = true))

        val exCeroSubida = assertThrows<IllegalArgumentException> {
            calc.calcularTiempoSubida(0.0)
        }
        assertTrue(exCeroSubida.message!!.contains("mayor a cero", ignoreCase = true))

        val exCeroTotal = assertThrows<IllegalArgumentException> {
            calc.calcularTiempoTotalVuelo(0.0)
        }
        assertTrue(exCeroTotal.message!!.contains("mayor a cero", ignoreCase = true))

        // v0 < 0 debe ser rechazada
        assertThrows<IllegalArgumentException> { calc.calcularAlturaMaxima(-15.0) }
        assertThrows<IllegalArgumentException> { calc.calcularTiempoSubida(-10.0) }
        assertThrows<IllegalArgumentException> { calc.calcularTiempoTotalVuelo(-1.0) }
    }

    // ========================================================================
    // 4. Extensión OCP con NivelLanzamientoAbajo y NivelLanzamientoArriba
    // ========================================================================

    @Test
    fun testNivelesExtensiblesOCP() {
        val nivelAbajo = NivelLanzamientoAbajo(velocidadInicial = 5.0, alturaInicialMetros = 20.0)
        assertEquals(2, nivelAbajo.idNivel)
        val respAbajo = nivelAbajo.calcularRespuestaEsperada(calc)
        assertTrue(nivelAbajo.validarRespuesta(calc, respAbajo))
        assertTrue(nivelAbajo.validarRespuesta(calc, 20.43))
        assertFalse(nivelAbajo.validarRespuesta(calc, 25.0))

        val nivelArriba = NivelLanzamientoArriba(velocidadInicial = 15.0)
        assertEquals(3, nivelArriba.idNivel)
        val respArriba = nivelArriba.calcularRespuestaEsperada(calc)
        assertTrue(nivelArriba.validarRespuesta(calc, respArriba))
        assertTrue(nivelArriba.validarRespuesta(calc, 11.47))
        assertFalse(nivelArriba.validarRespuesta(calc, 20.0))
    }

    @Test
    fun testDemostrarMotorFisicoEjecucionExitosa() {
        val resultado = demostrarMotorFisico(calc)
        assertEquals(9.81, resultado.g, 0.0001)
    }
}
