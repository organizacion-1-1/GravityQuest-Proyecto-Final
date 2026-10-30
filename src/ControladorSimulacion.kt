package gravityquest

/**
 * ============================================================================
 * GravityQuest - Adaptador Compatible del Controlador de Simulación (SRP, LSP & DRY)
 * ============================================================================
 *
 * Extiende de [MotorSimulacionCanvas] respetando el Principio de Sustitución de Liskov (LSP)
 * y el Principio DRY para mantener compatibilidad total con módulos que referencian
 * [ControladorSimulacion] mientras delega toda la gestión del [javafx.animation.AnimationTimer]
 * y el cálculo de $\Delta t$ en [MotorSimulacionCanvas] (Issue #21).
 */
class ControladorSimulacion(
    objetoSimulado: ObjetoSimulado,
    calculadora: ICalculadoraFisica = CalculadoraFisica(),
    alturaMetros: Double = PhysicsConstants.ALTURA_DEFAULT,
    velocidadInicial: Double = 0.0,
    direccion: DireccionMovimiento = DireccionMovimiento.ABAJO,
    estrategia: IEstrategiaCalculo = EstrategiaCaidaLibre,
    alActualizar: (ObjetoSimulado) -> Unit = {}
) : MotorSimulacionCanvas(
    objetoSimulado = objetoSimulado,
    calculadora = calculadora,
    alturaMetrosInicial = alturaMetros,
    velocidadInicialParam = velocidadInicial,
    direccionInicial = direccion,
    estrategiaInicial = estrategia,
    alActualizar = alActualizar
)
