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
        assertEquals("Por favor, ingresa un número válido.", app.lblResultado.text)
        assertTrue(app.lblResultado.styleClass.contains("resultado-error"), "Debe tener clase CSS 'resultado-error'")
        assertFalse(app.lblResultado.styleClass.contains("resultado-exito"), "No debe tener clase CSS 'resultado-exito'")
    }

    @Test
    fun testValidarRespuestaCorrecta() {
        val app = Main()
        val tiempoEsperado = app.calculadora.calcularTiempo(app.alturaProblema)
        app.txtRespuesta.text = String.format(java.util.Locale.US, "%.2f", tiempoEsperado)
        app.validarRespuesta()
        assertTrue(app.lblResultado.text.startsWith("¡Correcto!"), "Debe indicar resultado correcto con punto")
        assertTrue(app.lblResultado.styleClass.contains("resultado-exito"), "Debe tener clase CSS 'resultado-exito'")
        assertFalse(app.lblResultado.styleClass.contains("resultado-error"), "No debe tener clase CSS 'resultado-error'")

        // Probar también con coma decimal
        app.txtRespuesta.text = "2,02"
        app.validarRespuesta()
        assertTrue(app.lblResultado.text.startsWith("¡Correcto!"), "Debe indicar resultado correcto con coma")
        assertTrue(app.lblResultado.styleClass.contains("resultado-exito"), "Debe tener clase CSS 'resultado-exito'")
    }

    @Test
    fun testValidarRespuestaIncorrecta() {
        val app = Main()
        app.txtRespuesta.text = "99.9"
        app.validarRespuesta()
        assertEquals("Incorrecto. Intenta de nuevo.", app.lblResultado.text)
        assertTrue(app.lblResultado.styleClass.contains("resultado-error"), "Debe tener clase CSS 'resultado-error'")
        assertFalse(app.lblResultado.styleClass.contains("resultado-exito"), "No debe tener clase CSS 'resultado-exito'")
    }
}
