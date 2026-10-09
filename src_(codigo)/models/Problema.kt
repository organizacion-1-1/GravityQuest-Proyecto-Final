package gravityquest.models

import kotlin.math.abs

/**
 * Clasificación de los tipos de problemas físicos para escalabilidad y OCP.
 *
 * Principio Abierto/Cerrado (OCP):
 * Permite categorizar y extender los desafíos físicos soportados (caída libre, lanzamientos verticales,
 * MRU, cinemática general, etc.) sin modificar la estructura del modelo [Problema].
 *
 * @property nombre Nombre legible del tipo de problema físico.
 * @property descripcion Resumen conceptual del movimiento físico.
 */
enum class TipoProblema(
    val nombre: String,
    val descripcion: String
) {
    CAIDA_LIBRE(
        nombre = "Caída Libre",
        descripcion = "Movimiento vertical descendente bajo aceleración gravitatoria sin velocidad inicial (v₀ = 0)"
    ),
    LANZAMIENTO_VERTICAL_ABAJO(
        nombre = "Lanzamiento Vertical Hacia Abajo",
        descripcion = "Movimiento vertical descendente con velocidad inicial en la dirección del movimiento"
    ),
    LANZAMIENTO_VERTICAL_ARRIBA(
        nombre = "Lanzamiento Vertical Hacia Arriba",
        descripcion = "Movimiento vertical ascendente con velocidad inicial desacelerado por la gravedad"
    ),
    MRU(
        nombre = "Movimiento Rectilíneo Uniforme",
        descripcion = "Movimiento rectilíneo con velocidad constante y aceleración nula"
    ),
    MRUV(
        nombre = "Movimiento Rectilíneo Uniformemente Variado",
        descripcion = "Movimiento rectilíneo con aceleración constante"
    ),
    OTRO(
        nombre = "Física General",
        descripcion = "Ejercicio físico cinemático o dinámico general"
    );

    init {
        require(nombre.isNotBlank()) { "El nombre del tipo de problema no puede estar vacío" }
        require(descripcion.isNotBlank()) { "La descripción del tipo de problema no puede estar vacía" }
    }
}

/**
 * Contrato de abstracción para representar un ejercicio o problema físico.
 *
 * Principio Abierto/Cerrado (OCP) e Inversión de Dependencias (DIP):
 * Define los requisitos indispensables de cualquier problema en el dominio de GravityQuest,
 * permitiendo futuras especializaciones sin romper los clientes consumidores.
 */
interface IProblema {
    val id: Int
    val enunciado: String
    val dificultad: Dificultad
    val valorEsperado: Double
    val unidadMedida: String
    val pista: String
    val tipo: TipoProblema

    /**
     * Valida si un valor numérico provisto coincide con el valor esperado dentro del margen de error permitido.
     *
     * @param valorIngresado Número ingresado por el usuario o estudiante.
     * @param margenError Tolerancia personalizada opcional; si no se provee, se toma de [dificultad].
     * @return `true` si la respuesta es admisible, `false` en caso contrario.
     */
    fun validarRespuesta(valorIngresado: Double, margenError: Double? = null): Boolean
}

/**
 * Modelo de datos inmutable que representa un ejercicio físico individual.
 *
 * Principio de Responsabilidad Única (SRP):
 * Encapsula de forma exclusiva la definición, datos requeridos y validación puntual
 * de una consigna física individual, delegando la gestión y filtrado al repositorio/generador.
 *
 * Principio Abierto/Cerrado (OCP):
 * Estructurado con propiedades flexibles ([tipo], [datosAdicionales]) e implementando [IProblema],
 * facilitando la incorporación de nuevas áreas cinemáticas sin alterar la estructura fundamental.
 *
 * Encapsulamiento y Contratos Seguros:
 * Todas las propiedades son inmutables ([val]) y el bloque [init] aplica invariantes estrictos
 * para prevenir estados inconsistentes (enunciados vacíos, identificadores negativos, NaN, etc.).
 *
 * @property id Identificador único y positivo del problema.
 * @property enunciado Consigna o texto descriptivo del ejercicio.
 * @property dificultad Nivel de complejidad asociado ([Dificultad.FACIL], [Dificultad.MEDIO], [Dificultad.DIFICIL]).
 * @property valorEsperado Valor numérico teórico de la respuesta correcta.
 * @property unidadMedida Símbolo de la unidad física de la respuesta (ej. "s", "m/s", "m").
 * @property pista Orientación o ayuda conceptual para guiar la resolución del usuario.
 * @property tipo Categoría física del problema (por defecto [TipoProblema.CAIDA_LIBRE]).
 * @property datosAdicionales Mapa inmutable para parámetros físicos adicionales (ej. masa, ángulo, gravedad).
 */
data class Problema(
    override val id: Int,
    override val enunciado: String,
    override val dificultad: Dificultad,
    override val valorEsperado: Double,
    override val unidadMedida: String,
    override val pista: String,
    override val tipo: TipoProblema = TipoProblema.CAIDA_LIBRE,
    val datosAdicionales: Map<String, Any> = emptyMap()
) : IProblema {

    init {
        require(id > 0) { "El ID del problema debe ser un entero estrictamente positivo (> 0). Valor recibido: $id" }
        require(enunciado.isNotBlank()) { "El enunciado del problema no puede estar vacío ni contener solo espacios" }
        require(unidadMedida.isNotBlank()) { "La unidad de medida no puede estar vacía ni contener solo espacios" }
        require(pista.isNotBlank()) { "La pista no puede estar vacía ni contener solo espacios" }
        require(!valorEsperado.isNaN()) { "El valor esperado no puede ser NaN (Not a Number)" }
        require(!valorEsperado.isInfinite()) { "El valor esperado no puede ser infinito: $valorEsperado" }
    }

    /**
     * Valida si una respuesta numérica provista por el estudiante o usuario es correcta
     * dentro de una tolerancia o margen de error.
     *
     * @param valorIngresado El valor numérico a evaluar.
     * @param margenError Tolerancia personalizada opcional; si es null, utiliza [dificultad.margenErrorTolerable].
     * @return `true` si la diferencia absoluta no excede el margen permitido, `false` en caso contrario.
     */
    override fun validarRespuesta(valorIngresado: Double, margenError: Double?): Boolean {
        val tolerancia = margenError ?: dificultad.margenErrorTolerable
        require(tolerancia >= 0.0) { "El margen de error para la validación no puede ser negativo: $tolerancia" }
        return abs(valorIngresado - valorEsperado) <= tolerancia
    }

    /**
     * Retorna una representación concisa de la solución esperada con su unidad física (ej. "2.02 s").
     */
    fun respuestaFormateada(): String = "$valorEsperado $unidadMedida"

    /**
     * Representación legible del problema con toda su información encapsulada.
     */
    override fun toString(): String {
        return "Problema(id=$id, dificultad=${dificultad.nombreVisible}, tipo=${tipo.nombre}, " +
               "valorEsperado=$valorEsperado $unidadMedida, pista='$pista', enunciado='$enunciado')"
    }
}
