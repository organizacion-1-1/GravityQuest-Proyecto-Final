package gravityquest.models

import gravityquest.PhysicsConstants

/**
 * Representa los niveles de dificultad disponibles en GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Modela exclusivamente la graduación del desafío físico y sus atributos intrínsecos
 * (nombre visible, descripción pedagógica y tolerancia numérica permitida).
 *
 * Principio DRY:
 * Reutiliza [PhysicsConstants.MARGEN_ERROR_DEFAULT] en lugar de duplicar valores mágicos.
 *
 * Encapsulamiento y POO:
 * Propiedades inmutables ([val]), invariantes asegurados en [init] y métodos de dominio.
 */
enum class Dificultad(
    val nombreVisible: String,
    val descripcion: String,
    val margenErrorTolerable: Double = PhysicsConstants.MARGEN_ERROR_DEFAULT
) {
    /** Nivel Fácil: Caída libre clásica sin velocidad inicial (v₀ = 0 m/s). */
    FACIL(
        nombreVisible = "Fácil",
        descripcion = "Caída libre sin velocidad inicial (v₀ = 0 m/s)"
    ),

    /** Nivel Medio: Lanzamiento vertical hacia abajo con velocidad inicial (v₀ > 0 m/s). */
    MEDIO(
        nombreVisible = "Medio",
        descripcion = "Lanzamiento vertical hacia abajo con velocidad inicial (v₀ > 0 m/s)"
    ),

    /** Nivel Difícil: Lanzamiento vertical hacia arriba desacelerando por gravedad (v₀ > 0 m/s). */
    DIFICIL(
        nombreVisible = "Difícil",
        descripcion = "Lanzamiento vertical hacia arriba desacelerando por gravedad (v₀ > 0 m/s)"
    );

    init {
        require(nombreVisible.isNotBlank()) { "El nombre visible no puede estar vacío ni contener solo espacios" }
        require(descripcion.isNotBlank()) { "La descripción no puede estar vacía ni contener solo espacios" }
        require(margenErrorTolerable >= 0.0) { "El margen de error tolerable no puede ser negativo: $margenErrorTolerable" }
    }

    companion object {
        /** Alias para soportar la nomenclatura con tilde 'DIFÍCIL'. */
        val `DIFÍCIL`: Dificultad get() = DIFICIL

        /**
         * Busca la [Dificultad] correspondiente a partir de un texto normalizado.
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
