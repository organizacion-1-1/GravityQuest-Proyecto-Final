package gravityquest

import javafx.application.Application
import javafx.application.Platform
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class MainTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupJavaFx() {
            try {
                Platform.startup {}
            } catch (_: IllegalStateException) {
                // Platform already started
            }
        }
    }

    @Test
    fun testMainInheritsFromApplication() {
        val app = Main()
        assertTrue(app is Application, "Main debe heredar de javafx.application.Application")
    }

    @Test
    fun testCalculadoraInitialization() {
        val app = Main()
        assertEquals(9.81, app.calculadora.g, "La calculadora debe inicializarse con g = 9.81")
        assertEquals(20.0, app.alturaProblema, "La altura del problema debe ser 20.0 m")
    }

    @Test
    fun testUIComponentsInitialization() {
        val app = Main()
        assertTrue(app.lblInstruccion.text.contains("20m"), "El label de instrucción debe mencionar la altura de 20m")
        assertTrue(app.lblInstruccion.text.contains("tiempo de caída"), "El label debe preguntar por el tiempo de caída")
        assertEquals("Validar Respuesta", app.btnValidar.text, "El texto del botón debe ser 'Validar Respuesta'")
    }

    @Test
    fun testUIComponentsStyleClasses() {
        val app = Main()
        assertTrue(app.lblTitulo.styleClass.contains("titulo"), "El título debe tener la clase CSS 'titulo'")
        assertTrue(app.lblInstruccion.styleClass.contains("label"), "La instrucción debe tener la clase CSS 'label'")
        assertTrue(app.txtRespuesta.styleClass.contains("text-field"), "El campo de texto debe tener la clase CSS 'text-field'")
        assertTrue(app.btnValidar.styleClass.contains("button"), "El botón debe tener la clase CSS 'button'")
    }

    @Test
    fun testStylesheetResourceExists() {
        val resource = Main::class.java.getResource("/assets/style.css")
        assertNotNull(resource, "El archivo de recursos /assets/style.css debe existir en el classpath")
    }

    @Test
    fun testValidarRespuestaEntradaInvalida() {
        val app = Main()
        app.txtRespuesta.text = "no-es-numero"
        app.validarRespuesta()
        assertEquals("¡Entrada inválida! Por favor ingresa un número.", app.lblResultado.text)
        assertTrue(app.lblResultado.styleClass.contains("resultado-error"), "Debe tener clase CSS 'resultado-error'")
        assertFalse(app.lblResultado.styleClass.contains("resultado-exito"), "No debe tener clase CSS 'resultado-exito'")
    }

    @Test
    fun testValidarRespuestaCorrecta() {
        val app = Main()
        val tiempoEsperado = app.calculadora.calcularTiempo(app.alturaProblema)
        app.txtRespuesta.text = String.format(java.util.Locale.US, "%.2f", tiempoEsperado)
        app.validarRespuesta()
        assertEquals("¡Correcto! Respuesta dentro del margen de error.", app.lblResultado.text)
        assertTrue(app.lblResultado.styleClass.contains("resultado-exito"), "Debe tener clase CSS 'resultado-exito'")
        assertFalse(app.lblResultado.styleClass.contains("resultado-error"), "No debe tener clase CSS 'resultado-error'")

        // Probar también con coma decimal
        app.txtRespuesta.text = "2,02"
        app.validarRespuesta()
        assertEquals("¡Correcto! Respuesta dentro del margen de error.", app.lblResultado.text)
        assertTrue(app.lblResultado.styleClass.contains("resultado-exito"), "Debe tener clase CSS 'resultado-exito'")
    }

    @Test
    fun testValidarRespuestaIncorrecta() {
        val app = Main()
        app.txtRespuesta.text = "99.9"
        app.validarRespuesta()
        assertEquals("¡Intenta de nuevo! Tu respuesta no es precisa.", app.lblResultado.text)
        assertTrue(app.lblResultado.styleClass.contains("resultado-error"), "Debe tener clase CSS 'resultado-error'")
        assertFalse(app.lblResultado.styleClass.contains("resultado-exito"), "No debe tener clase CSS 'resultado-exito'")
    }

    @Test
    fun testCanvasAndSimulationButtonInitialization() {
        val app = Main()
        assertEquals(300.0, app.canvasSimulacion.width, "El Canvas debe tener 300px de ancho")
        assertEquals(300.0, app.canvasSimulacion.height, "El Canvas debe tener 300px de alto")
        assertTrue(app.canvasSimulacion.styleClass.contains("canvas-simulacion"), "El Canvas debe tener clase CSS 'canvas-simulacion'")
        assertEquals("Simular Caída", app.btnSimular.text, "El botón debe tener el texto 'Simular Caída'")
        assertTrue(app.btnSimular.styleClass.contains("button"), "El botón debe tener la clase CSS 'button'")
    }

    @Test
    fun testObjetoSimuladoEncapsulamiento() {
        val objeto = ObjetoSimulado(alturaMaxima = 20.0)
        assertEquals(0.0, objeto.posicionY)
        assertEquals(0.0, objeto.velocidadY)
        assertEquals(0.0, objeto.tiempo)
        assertFalse(objeto.enSuelo)

        objeto.actualizar(posicion = 10.0, velocidad = 14.0, tiempoTranscurrido = 1.42, haLlegadoAlSuelo = false)
        assertEquals(10.0, objeto.posicionY)
        assertEquals(14.0, objeto.velocidadY)
        assertEquals(1.42, objeto.tiempo)
        assertFalse(objeto.enSuelo)

        objeto.actualizar(posicion = 25.0, velocidad = 19.8, tiempoTranscurrido = 2.02, haLlegadoAlSuelo = true)
        assertEquals(20.0, objeto.posicionY, "La posición no debe superar la altura máxima (20.0)")
        assertTrue(objeto.enSuelo)

        objeto.reiniciar()
        assertEquals(0.0, objeto.posicionY)
        assertEquals(0.0, objeto.velocidadY)
        assertEquals(0.0, objeto.tiempo)
        assertFalse(objeto.enSuelo)
    }

    @Test
    fun testCalculadoraFisicaPosicionCaida() {
        val calc = CalculadoraFisica()
        // y = 1/2 * g * t^2 -> t = 0 -> y = 0
        assertEquals(0.0, calc.calcularPosicionCaida(0.0))
        // t = 1.0 -> y = 0.5 * 9.81 * 1 = 4.905
        assertEquals(4.905, calc.calcularPosicionCaida(1.0), 0.001)
        // t = 2.0 -> y = 0.5 * 9.81 * 4 = 19.62
        assertEquals(19.62, calc.calcularPosicionCaida(2.0), 0.001)
        // calcularPosicion es alias de calcularPosicionCaida
        assertEquals(calc.calcularPosicionCaida(2.0), calc.calcularPosicion(2.0))
    }

    @Test
    fun testRenderizadorCanvasConversionCoordenadas() {
        val app = Main()
        val renderizador = RenderizadorCanvas(app.canvasSimulacion, app.alturaProblema)
        val pixelYInicio = renderizador.convertirMetroAPixelY(0.0)
        val pixelYFin = renderizador.convertirMetroAPixelY(app.alturaProblema)

        assertEquals(renderizador.margenSuperior, pixelYInicio, "En y=0m, pixelY debe ser el margen superior")
        assertTrue(pixelYFin > pixelYInicio, "El pixel final debe ser mayor que el pixel inicial (caída hacia abajo)")
    }
}
