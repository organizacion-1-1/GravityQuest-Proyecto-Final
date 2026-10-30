package gravityquest.models

import gravityquest.PhysicsConstants
import gravityquest.IEstrategiaCalculo
import gravityquest.EstrategiaCaidaLibre
import gravityquest.EstrategiaLanzamientoAbajo
import gravityquest.EstrategiaLanzamientoArriba

/**
 * Representa los niveles de dificultad disponibles en GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Modela exclusivamente la graduación del desafío físico y sus atributos intrínsecos
 * (nombre visible, descripción pedagógica, tolerancia numérica, estrategia de cálculo
 * y progresión secuencial hacia el siguiente nivel).
 *
 * Principio Abierto/Cerrado (OCP) y Polimorfismo:
 * Cada nivel provee su estrategia de cálculo polimórfica ([estrategiaCalculo]) y su
 * eslabón siguiente en la cadena de desbloqueo ([siguienteNivel]), eliminando
 * condicionales `when` rígidos o dispersos por el sistema.
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
    /** Nivel Fácil: Caída libre clásica sin velocidad inicial (v₀ = 0 m/s). Desbloquea MEDIO. */
    FACIL(
        nombreVisible = "Fácil",
        descripcion = "Caída libre sin velocidad inicial (v₀ = 0 m/s)"
    ) {
        override val estrategiaCalculo: IEstrategiaCalculo get() = EstrategiaCaidaLibre
        override val siguienteNivel: Dificultad? get() = MEDIO
    },

    /** Nivel Medio: Lanzamiento vertical hacia abajo con velocidad inicial (v₀ > 0 m/s). Desbloquea DIFÍCIL. */
    MEDIO(
        nombreVisible = "Medio",
        descripcion = "Lanzamiento vertical hacia abajo con velocidad inicial (v₀ > 0 m/s)"
    ) {
        override val estrategiaCalculo: IEstrategiaCalculo get() = EstrategiaLanzamientoAbajo
        override val siguienteNivel: Dificultad? get() = DIFICIL
    },

    /** Nivel Difícil: Lanzamiento vertical hacia arriba desacelerando por gravedad (v₀ > 0 m/s). Nivel final. */
    DIFICIL(
        nombreVisible = "Difícil",
        descripcion = "Lanzamiento vertical hacia arriba desacelerando por gravedad (v₀ > 0 m/s)"
    ) {
        override val estrategiaCalculo: IEstrategiaCalculo get() = EstrategiaLanzamientoArriba
        override val siguienteNivel: Dificultad? get() = null
    };

    /** Estrategia cinemática polimórfica asignada al nivel (OCP). */
    abstract val estrategiaCalculo: IEstrategiaCalculo

    /** Siguiente nivel en la cadena de progresión secuencial (Fácil → Medio → Difícil). */
    abstract val siguienteNivel: Dificultad?

    /** Retorna el siguiente nivel de la secuencia o `null` si es el nivel final. */
    fun obtenerSiguiente(): Dificultad? = siguienteNivel

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
