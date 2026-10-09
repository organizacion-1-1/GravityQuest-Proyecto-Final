package gravityquest.models

/**
 * Representa los niveles de dificultad disponibles en GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Modela exclusivamente la graduación del desafío físico y sus atributos intrínsecos
 * (nombre visible, descripción pedagógica y tolerancia numérica permitida), sin acoplarse
 * a la lógica de generación, almacenamiento o componentes de interfaz gráfica.
 *
 * Encapsulamiento:
 * Expone únicamente propiedades inmutables ([val]) y asegura mediante validaciones en [init]
 * la coherencia y robustez de sus datos.
 *
 * @property nombreVisible Nombre amigable para mostrar en interfaces y reportes (ej. "Fácil").
 * @property descripcion Explicación pedagógica de las condiciones físicas del nivel.
 * @property margenErrorTolerable Margen de error o tolerancia numérica absoluta permitida al validar respuestas.
 */
enum class Dificultad(
    val nombreVisible: String,
    val descripcion: String,
    val margenErrorTolerable: Double
) {
    /**
     * Nivel Fácil: Caída libre clásica sin velocidad inicial (v₀ = 0 m/s).
     */
    FACIL(
        nombreVisible = "Fácil",
        descripcion = "Caída libre sin velocidad inicial (v₀ = 0 m/s)",
        margenErrorTolerable = 0.05
    ),

    /**
     * Nivel Medio: Lanzamiento vertical hacia abajo con velocidad inicial (v₀ > 0 m/s).
     */
    MEDIO(
        nombreVisible = "Medio",
        descripcion = "Lanzamiento vertical hacia abajo con velocidad inicial (v₀ > 0 m/s)",
        margenErrorTolerable = 0.05
    ),

    /**
     * Nivel Difícil: Lanzamiento vertical hacia arriba desacelerando por gravedad (v₀ > 0 m/s).
     */
    DIFICIL(
        nombreVisible = "Difícil",
        descripcion = "Lanzamiento vertical hacia arriba desacelerando por gravedad (v₀ > 0 m/s)",
        margenErrorTolerable = 0.05
    );

    init {
        require(nombreVisible.isNotBlank()) { "El nombre visible no puede estar vacío ni contener solo espacios" }
        require(descripcion.isNotBlank()) { "La descripción no puede estar vacía ni contener solo espacios" }
        require(margenErrorTolerable >= 0.0) { "El margen de error tolerable no puede ser negativo: $margenErrorTolerable" }
    }

    companion object {
        /**
         * Alias para soportar la nomenclatura con tilde 'DIFÍCIL' requerida por especificaciones.
         */
        val `DIFÍCIL`: Dificultad get() = DIFICIL

        /**
         * Busca y retorna la [Dificultad] correspondiente a partir de un texto (nombre o identificador),
         * ignorando mayúsculas, minúsculas y espacios periféricos.
         *
         * @param texto Cadena de búsqueda (ej. "Fácil", "facil", "MEDIO", "DIFICIL", "Difícil").
         * @return La [Dificultad] coincidente o `null` si no existe coincidencia.
         */
        fun desdeTexto(texto: String?): Dificultad? {
            if (texto.isNullOrBlank()) return null
            val normalizado = texto.trim().uppercase()
            return entries.firstOrNull {
                it.name == normalizado ||
                it.nombreVisible.uppercase() == normalizado ||
                (it == DIFICIL && normalizado == "DIFÍCIL")
            }
        }
    }
}
