package gravityquest.models

import gravityquest.PhysicsConstants
import gravityquest.IEstrategiaCalculo
import gravityquest.DireccionMovimiento
import gravityquest.CalculosAuxiliares
import kotlin.math.abs

/**
 * Clasificación de los tipos de problemas físicos para escalabilidad y OCP.
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
 * Principio Abierto/Cerrado (OCP) e Inversión de Dependencias (DIP).
 */
interface IProblema {
    val id: Int
    val enunciado: String
    val dificultad: Dificultad
    val valorEsperado: Double
    val unidadMedida: String
    val pista: String
    val tipo: TipoProblema
    val estrategiaCalculo: IEstrategiaCalculo

    /**
     * Valida si un valor provisto coincide con el valor esperado dentro del margen permitido.
     */
    fun validarRespuesta(valorIngresado: Double, margenError: Double? = null): Boolean
}

/**
 * Modelo de datos inmutable que representa un ejercicio físico individual.
 *
 * Principio de Responsabilidad Única (SRP):
 * Encapsula la definición, datos y validación matemática de una consigna física.
 *
 * Principio Abierto/Cerrado (OCP) y Polimorfismo:
 * Delega los cálculos y fórmulas en la estrategia polimórfica [IEstrategiaCalculo].
 *
 * Encapsulamiento y Contratos Seguros:
 * Propiedades inmutables ([val]), precondiciones firmes con [require] y métodos de dominio.
 */
data class Problema(
    override val id: Int,
    override val enunciado: String,
    override val dificultad: Dificultad,
    override val valorEsperado: Double,
    override val unidadMedida: String,
    override val pista: String,
    override val tipo: TipoProblema = dificultad.estrategiaCalculo.tipo,
    val datosAdicionales: Map<String, Any> = emptyMap(),
    override val estrategiaCalculo: IEstrategiaCalculo = if (tipo == dificultad.estrategiaCalculo.tipo) {
        dificultad.estrategiaCalculo
    } else {
        CalculosAuxiliares.resolverEstrategia(tipo)
    }
) : IProblema {

    init {
        require(id > 0) { "El ID del problema debe ser un entero estrictamente positivo (> 0). Valor recibido: $id" }
        require(enunciado.isNotBlank()) { "El enunciado del problema no puede estar vacío ni contener solo espacios" }
        require(unidadMedida.isNotBlank()) { "La unidad de medida no puede estar vacía ni contener solo espacios" }
        require(pista.isNotBlank()) { "La pista no puede estar vacía ni contener solo espacios" }
        require(!valorEsperado.isNaN()) { "El valor esperado no puede ser NaN (Not a Number)" }
        require(!valorEsperado.isInfinite()) { "El valor esperado no puede ser infinito: $valorEsperado" }
        estrategiaCalculo.validarParametros(alturaMetros, velocidadInicial)
    }

    /**
     * Altura inicial del problema en metros para simulación (método de dominio seguro).
     */
    val alturaMetros: Double
        get() {
            val h = (datosAdicionales["alturaMetros"] as? Number)?.toDouble()
            if (h != null) return h
            return if (tipo == TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA && velocidadInicial > 0.0) {
                (velocidadInicial * velocidadInicial) / (2.0 * PhysicsConstants.GRAVEDAD_TIERRA)
            } else {
                PhysicsConstants.ALTURA_DEFAULT
            }
        }

    /**
     * Velocidad inicial del problema en m/s (método de dominio seguro).
     */
    val velocidadInicial: Double
        get() {
            val v = (datosAdicionales["v0"] as? Number)?.toDouble()
            if (v != null) return v
            return when (tipo) {
                TipoProblema.LANZAMIENTO_VERTICAL_ABAJO -> 5.0
                TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA -> 15.0
                else -> 0.0
            }
        }

    /**
     * Dirección cinemática del movimiento físico delegada a la estrategia polimórfica (OCP).
     */
    val direccionMovimiento: DireccionMovimiento
        get() = estrategiaCalculo.direccionMovimiento

    /**
     * Fórmulas teóricas específicas del problema provistas por la estrategia polimórfica (OCP).
     */
    val formulasPistas: String
        get() = estrategiaCalculo.formulasPista

    /**
     * Altura de referencia física representativa para el renderizado del lienzo Canvas.
     */
    val alturaReferenciaSimulacion: Double
        get() = alturaMetros

    /**
     * Valida si una respuesta numérica provista es correcta dentro de la tolerancia.
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

    override fun toString(): String =
        "Problema(id=$id, dificultad=${dificultad.nombreVisible}, tipo=${tipo.nombre}, " +
        "valorEsperado=$valorEsperado $unidadMedida, pista='$pista', enunciado='$enunciado')"
}
