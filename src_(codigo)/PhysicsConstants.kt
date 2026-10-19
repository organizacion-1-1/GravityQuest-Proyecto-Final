package gravityquest

/**
 * ============================================================================
 * GravityQuest - Constantes y Configuración Global (POO / DRY)
 * ============================================================================
 *
 * Centraliza las constantes físicas universales y configuraciones del juego
 * para erradicar valores mágicos duplicados (Hardcoding) a lo largo del sistema.
 */

/**
 * Constantes cinemáticas y físicas fundamentales.
 *
 * Principio DRY:
 * Reemplaza valores repetidos como g = 9.81 y tolerancias numéricas por
 * referencias inmutables centralizadas.
 */
object PhysicsConstants {
    /** Aceleración de la gravedad terrestre estándar en m/s². */
    const val GRAVEDAD_TIERRA: Double = 9.81

    /** Margen de error o tolerancia numérica predeterminada (±0.05). */
    const val MARGEN_ERROR_DEFAULT: Double = 0.05

    /** Altura de caída predeterminada en metros para ejercicios base. */
    const val ALTURA_DEFAULT: Double = 20.0
}

/**
 * Parámetros globales de interfaz gráfica, dimensiones y rutas de recursos.
 *
 * Principio de Responsabilidad Única (SRP):
 * Centraliza las directivas de configuración visual y paths de assets de la aplicación.
 */
object GameConfig {
    /** Nombre oficial del juego y título de ventana base. */
    const val TITULO_APP: String = "GravityQuest"

    /** Subtítulo descriptivo para pantallas de inicio y cabeceras. */
    const val SUBTITULO_APP: String = "Misiones de Cinemática y Física Gravitatoria"

    /** Ruta del recurso de la hoja de estilos espacial en el classpath. */
    const val RUTA_ESTILOS: String = "/assets/style.css"

    /** Ancho predeterminado de la ventana en píxeles. */
    const val ANCHO_VENTANA_DEFAULT: Double = 840.0

    /** Alto predeterminado de la ventana en píxeles. */
    const val ALTO_VENTANA_DEFAULT: Double = 560.0

    /** Ancho predeterminado del lienzo [javafx.scene.canvas.Canvas] en píxeles. */
    const val ANCHO_CANVAS_DEFAULT: Double = 320.0

    /** Alto predeterminado del lienzo [javafx.scene.canvas.Canvas] en píxeles. */
    const val ALTO_CANVAS_DEFAULT: Double = 320.0
}
