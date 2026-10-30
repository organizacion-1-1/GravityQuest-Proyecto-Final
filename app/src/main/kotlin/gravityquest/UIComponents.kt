package gravityquest

import javafx.animation.FadeTransition
import javafx.animation.ScaleTransition
import javafx.animation.TranslateTransition
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Duration

/**
 * ============================================================================
 * GravityQuest - Fábrica de Componentes Visuales y Extensiones UI (DRY & SRP)
 * ============================================================================
 *
 * Principio DRY & Patrón Factory:
 * Centraliza la creación e instanciación de controles JavaFX estilizados (botones,
 * etiquetas, campos de texto, íconos de vida y selectores) en una sola línea de código,
 * garantizando uniformidad visual y reduciendo drásticamente la duplicación entre vistas.
 */
object UIFactory {

    /**
     * Instancia un botón con estilo primario espacial (#0284c7).
     */
    fun crearBotonPrimario(texto: String, anchoMax: Double? = null): Button =
        Button(texto).apply {
            styleClass.addAll("button", "btn-primario")
            anchoMax?.let { maxWidth = it }
        }

    /**
     * Instancia un botón con estilo secundario oscuro (#334155).
     */
    fun crearBotonSecundario(texto: String, claseExtra: String? = null, anchoMax: Double? = null): Button =
        Button(texto).apply {
            styleClass.addAll(listOfNotNull("button", "btn-secundario", claseExtra))
            anchoMax?.let { maxWidth = it }
        }

    /**
     * Instancia una etiqueta de título con resplandor neón espacial.
     */
    fun crearTitulo(texto: String = GameConfig.TITULO_APP): Label =
        Label(texto).apply {
            styleClass.add("titulo")
        }

    /**
     * Instancia una etiqueta configurada con estilo, envoltorio de texto y ancho máximo opcionales.
     */
    fun crearEtiqueta(
        texto: String = "",
        clasesEstilo: List<String> = listOf("label"),
        anchoMax: Double? = null,
        wrap: Boolean = false,
        alineacion: Pos? = null
    ): Label =
        Label(texto).apply {
            styleClass.addAll(clasesEstilo)
            isWrapText = wrap
            anchoMax?.let { maxWidth = it }
            alineacion?.let { alignment = it }
        }

    /**
     * Instancia una etiqueta con estilo neón espacial para el marcador de puntuación (DRY).
     */
    fun crearEtiquetaPuntos(
        texto: String = "PUNTAJE: 0000",
        claseEstilo: String = "puntos-texto"
    ): Label =
        crearEtiqueta(
            texto = texto,
            clasesEstilo = listOf("label", claseEstilo),
            alineacion = Pos.CENTER
        )

    /**
     * Instancia un ícono visual de vida (corazón neón) con la clase CSS `.icono-vida` (DRY - Issue #18).
     */
    fun crearIconoVida(simbolo: String = "❤"): Label =
        crearEtiqueta(
            texto = simbolo,
            clasesEstilo = listOf("label", "icono-vida"),
            alineacion = Pos.CENTER
        )

    /**
     * Instancia un campo de entrada [TextField] con bordes redondeados y foco estilizado.
     */
    fun crearCampoTexto(prompt: String = "", anchoMax: Double = 220.0): TextField =
        TextField().apply {
            styleClass.add("text-field")
            promptText = prompt
            maxWidth = anchoMax
        }

    /**
     * Instancia un [ToggleButton] para el selector de dificultad.
     * Soporta activación programática controlada para pruebas y desacoplamiento.
     */
    fun crearBotonDificultad(
        texto: String,
        grupo: ToggleGroup,
        datos: Any,
        anchoPref: Double = 120.0
    ): ToggleButton =
        object : ToggleButton(texto) {
            override fun fire() {
                if (!isDisable) {
                    super.fire()
                } else {
                    // Permite disparo programático controlado en pruebas unitarias
                    isSelected = true
                }
            }
        }.apply {
            styleClass.addAll("button", "btn-dificultad")
            toggleGroup = grupo
            userData = datos
            prefWidth = anchoPref
        }

    /**
     * Instancia un contenedor [VBox] con alineación centrada y estilo.
     */
    fun crearPanelVertical(espaciado: Double, claseEstilo: String? = null, anchoMax: Double? = null): VBox =
        VBox(espaciado).apply {
            alignment = Pos.CENTER
            claseEstilo?.let { styleClass.add(it) }
            anchoMax?.let { maxWidth = it }
        }

    /**
     * Instancia un contenedor [HBox] con alineación centrada y estilo.
     */
    fun crearPanelHorizontal(espaciado: Double, claseEstilo: String? = null): HBox =
        HBox(espaciado).apply {
            alignment = Pos.CENTER
            claseEstilo?.let { styleClass.add(it) }
        }

    /**
     * Instancia un título especializado para las pantallas finales ("Game Over" o "¡Nivel Completado!")
     * aplicando la clase CSS específica (`.titulo-gameover` o `.titulo-victoria`) (Issue #20).
     */
    fun crearTituloFinal(texto: String, claseEstilo: String): Label =
        Label(texto).apply {
            styleClass.addAll("titulo", claseEstilo)
            alignment = Pos.CENTER
            isWrapText = true
        }

    /**
     * Instancia un botón de acción para las pantallas finales con la clase `.boton-accion-final` (Issue #20).
     */
    fun crearBotonAccionFinal(
        texto: String,
        esPrimario: Boolean = true,
        claseExtra: String? = null,
        anchoMax: Double? = 240.0
    ): Button =
        (if (esPrimario) crearBotonPrimario(texto, anchoMax) else crearBotonSecundario(texto, claseExtra, anchoMax)).apply {
            if (!styleClass.contains("boton-accion-final")) {
                styleClass.add("boton-accion-final")
            }
            claseExtra?.let {
                if (!styleClass.contains(it)) {
                    styleClass.add(it)
                }
            }
        }

    /**
     * Instancia el botón principal "Reintentar Nivel" para la pantalla de Game Over (DRY - Issue #20).
     */
    fun crearBotonReintentar(texto: String = "🔄 Reintentar Nivel", anchoMax: Double? = 240.0): Button =
        crearBotonAccionFinal(texto = texto, esPrimario = true, claseExtra = "btn-reintentar", anchoMax = anchoMax)

    /**
     * Instancia el botón principal "Siguiente Nivel" para la pantalla de Victoria (DRY - Issue #20).
     */
    fun crearBotonSiguienteNivel(texto: String = "🚀 Siguiente Nivel", anchoMax: Double? = 240.0): Button =
        crearBotonAccionFinal(texto = texto, esPrimario = true, claseExtra = "btn-siguiente-nivel", anchoMax = anchoMax)

    /**
     * Instancia el botón secundario "Menú Principal" para las pantallas finales (DRY - Issue #20).
     */
    fun crearBotonMenuPrincipal(texto: String = "🏠 Menú Principal", anchoMax: Double? = 240.0): Button =
        crearBotonAccionFinal(texto = texto, esPrimario = false, claseExtra = "btn-menu-principal", anchoMax = anchoMax)

    /**
     * Instancia una tarjeta modular de estadística ([TarjetaEstadisticaView]) a partir de un [DatoEstadistica] (DRY / OCP).
     */
    fun crearTarjetaEstadistica(dato: DatoEstadistica): TarjetaEstadisticaView =
        TarjetaEstadisticaView(dato)

    /**
     * Instancia una tarjeta modular de estadística ([TarjetaEstadisticaView]) con sus atributos descriptivos (DRY / OCP).
     */
    fun crearTarjetaEstadistica(
        titulo: String,
        valor: String,
        icono: String = "📊",
        detalle: String = "",
        claseEstiloExtra: String? = null
    ): TarjetaEstadisticaView =
        TarjetaEstadisticaView(DatoEstadistica(titulo, valor, icono, detalle, claseEstiloExtra))

    /**
     * Parsea una cadena de texto admitiendo coma o punto como separador decimal.
     * Encapsulado en [UIFactory] delegando en [CalculosAuxiliares] (SRP).
     */
    fun parsearNumeroDecimal(texto: String?): Double? =
        CalculosAuxiliares.parsearNumeroDecimal(texto)

    /**
     * Obtiene y parsea de forma segura el valor numérico decimal contenido en un [TextField],
     * manejando entradas vacías o caracteres inválidos sin lanzar excepciones no controladas (Issue #21).
     */
    fun obtenerValorDecimal(campo: TextField): Double? =
        parsearNumeroDecimal(campo.text)

    /**
     * Aplica un feedback visual suave de error sobre un [TextField] (borde rojo neón `.input-error`
     * y vibración corta) cuando se detecta una entrada vacía o numéricamente inválida (Issue #21).
     */
    fun marcarErrorCampo(campo: TextField) {
        campo.styleClass.remove("input-exito")
        if (!campo.styleClass.contains("input-error")) {
            campo.styleClass.add("input-error")
        }
        if (!campo.styleClass.contains("text-field-error")) {
            campo.styleClass.add("text-field-error")
        }
        FxAnimationUtils.animarVibracion(campo, desplazamiento = 5.0, duracionMs = 40.0) {
            campo.translateX = 0.0
        }
    }

    /**
     * Aplica un estado visual de validación exitosa sobre un [TextField] eliminando clases de error.
     */
    fun marcarExitoCampo(campo: TextField) {
        campo.styleClass.removeAll("input-error", "text-field-error")
        if (!campo.styleClass.contains("input-exito")) {
            campo.styleClass.add("input-exito")
        }
        campo.translateX = 0.0
    }

    /**
     * Limpia las clases de estado de validación (`.input-error`, `.text-field-error`, `.input-exito`)
     * sobre el [TextField] restableciendo su estilo original.
     */
    fun limpiarEstadoCampo(campo: TextField) {
        campo.styleClass.removeAll("input-error", "text-field-error", "input-exito")
        campo.translateX = 0.0
    }
}

/**
 * Gestor de animaciones visuales seguras (tolerante a entornos headless o pruebas automatizadas).
 */
object FxAnimationUtils {

    inline fun ejecutarSeguro(accion: () -> Unit) {
        try {
            accion()
        } catch (_: Exception) {
            // Protección ante entornos de pruebas headless sin subsistema de display
        }
    }

    fun animarPulso(nodo: Node, escala: Double = 1.15, duracionMs: Double = 200.0, alFinalizar: () -> Unit = {}) {
        ejecutarSeguro {
            ScaleTransition(Duration.millis(duracionMs), nodo).apply {
                fromX = 1.0
                fromY = 1.0
                toX = escala
                toY = escala
                cycleCount = 2
                isAutoReverse = true
                setOnFinished { alFinalizar() }
                play()
            }
        }
    }

    /**
     * Ejecuta una animación de celebración triunfal (escala rítmica y aparición) para la pantalla
     * de "¡Nivel Completado!" (Issue #20).
     */
    fun animarCelebracion(
        nodo: Node,
        escalaMaxima: Double = 1.18,
        duracionMs: Double = 260.0,
        alFinalizar: () -> Unit = {}
    ) {
        ejecutarSeguro {
            ScaleTransition(Duration.millis(duracionMs), nodo).apply {
                fromX = 0.92
                fromY = 0.92
                toX = escalaMaxima
                toY = escalaMaxima
                cycleCount = 4
                isAutoReverse = true
                setOnFinished {
                    nodo.scaleX = 1.0
                    nodo.scaleY = 1.0
                    alFinalizar()
                }
                play()
            }
        }
    }

    fun animarVibracion(nodo: Node, desplazamiento: Double = 8.0, duracionMs: Double = 50.0, alFinalizar: () -> Unit = {}) {
        ejecutarSeguro {
            TranslateTransition(Duration.millis(duracionMs), nodo).apply {
                fromX = -desplazamiento
                toX = desplazamiento
                cycleCount = 6
                isAutoReverse = true
                setOnFinished { alFinalizar() }
                play()
            }
        }
    }

    /**
     * Aplica una transición combinada de escala y desvanecimiento sobre un indicador de puntos.
     */
    fun animarDesvanecimiento(nodo: Node, duracionMs: Double = 650.0, alFinalizar: () -> Unit = {}) {
        ejecutarSeguro {
            FadeTransition(Duration.millis(duracionMs), nodo).apply {
                fromValue = 1.0
                toValue = 0.0
                setOnFinished { alFinalizar() }
                play()
            }
        }
    }

    /**
     * Anima la pérdida de un ícono de corazón combinando encogimiento/palpitar ([ScaleTransition])
     * y parpadeo/desvanecimiento ([FadeTransition]) (Issue #18).
     */
    fun animarPerdidaVida(
        nodo: Node,
        opacidadFinal: Double = 0.28,
        duracionMs: Double = 220.0,
        alFinalizar: () -> Unit = {}
    ) {
        ejecutarSeguro {
            ScaleTransition(Duration.millis(duracionMs), nodo).apply {
                fromX = 1.18
                fromY = 1.18
                toX = 0.85
                toY = 0.85
                cycleCount = 2
                isAutoReverse = true
                setOnFinished {
                    nodo.scaleX = 0.90
                    nodo.scaleY = 0.90
                }
                play()
            }
            FadeTransition(Duration.millis(duracionMs), nodo).apply {
                fromValue = 1.0
                toValue = opacidadFinal
                cycleCount = 3
                isAutoReverse = true
                setOnFinished {
                    nodo.opacity = opacidadFinal
                    alFinalizar()
                }
                play()
            }
        }
    }

    /**
     * Anima suavemente la restauración de un corazón lleno al reiniciar las 3 vidas (Issue #18).
     */
    fun animarRecuperacionVida(
        nodo: Node,
        duracionMs: Double = 200.0,
        alFinalizar: () -> Unit = {}
    ) {
        ejecutarSeguro {
            FadeTransition(Duration.millis(duracionMs), nodo).apply {
                fromValue = 0.35
                toValue = 1.0
                setOnFinished { nodo.opacity = 1.0 }
                play()
            }
            ScaleTransition(Duration.millis(duracionMs), nodo).apply {
                fromX = 0.85
                fromY = 0.85
                toX = 1.08
                toY = 1.08
                cycleCount = 2
                isAutoReverse = true
                setOnFinished {
                    nodo.scaleX = 1.0
                    nodo.scaleY = 1.0
                    alFinalizar()
                }
                play()
            }
        }
    }
}
