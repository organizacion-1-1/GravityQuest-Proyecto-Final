package gravityquest

import javafx.application.Application
import javafx.application.Platform
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun testValidarRespuestaEntradaInvalida() {
        val app = Main()
        app.txtRespuesta.text = "no-es-numero"
        app.validarRespuesta()
        assertEquals("Por favor, ingresa un número válido.", app.lblResultado.text)
    }

    @Test
    fun testValidarRespuestaCorrecta() {
        val app = Main()
        val tiempoEsperado = app.calculadora.calcularTiempo(app.alturaProblema)
        app.txtRespuesta.text = String.format(java.util.Locale.US, "%.2f", tiempoEsperado)
        app.validarRespuesta()
        assertTrue(app.lblResultado.text.startsWith("¡Correcto!"), "Debe indicar resultado correcto con punto")

        // Probar también con coma decimal
        app.txtRespuesta.text = "2,02"
        app.validarRespuesta()
        assertTrue(app.lblResultado.text.startsWith("¡Correcto!"), "Debe indicar resultado correcto con coma")
    }

    @Test
    fun testValidarRespuestaIncorrecta() {
        val app = Main()
        app.txtRespuesta.text = "99.9"
        app.validarRespuesta()
        assertEquals("Incorrecto. Intenta de nuevo.", app.lblResultado.text)
    }
}
