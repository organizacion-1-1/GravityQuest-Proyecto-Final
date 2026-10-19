package gravityquest.models

import gravityquest.PhysicsConstants

/**
 * Contrato de abstracción para repositorios y proveedores de problemas físicos.
 *
 * Principio de Inversión de Dependencias (DIP) y Principio Abierto/Cerrado (OCP):
 * Desacopla la obtención y consulta de ejercicios de su almacenamiento concreto.
 */
interface IRepositorioProblemas {
    fun obtenerTodos(): List<Problema>
    fun obtenerPorDificultad(dificultad: Dificultad): List<Problema>
    fun obtenerPorId(id: Int): Problema?
    fun obtenerPorTipo(tipo: TipoProblema): List<Problema>
    fun agregarProblema(problema: Problema)
    fun cantidadTotal(): Int
    fun cantidadPorDificultad(dificultad: Dificultad): Int
    fun obtenerProblemaAleatorio(dificultad: Dificultad? = null): Problema?
}

/**
 * Generador y repositorio en memoria de problemas físicos para GravityQuest.
 *
 * Principio de Responsabilidad Única (SRP):
 * Se encarga exclusivamente de la instanciación inicial, almacenamiento protegido,
 * filtrado y provisión de problemas físicos por nivel de [Dificultad] y tipo cinemático.
 *
 * Encapsulamiento:
 * Protege la colección mutable interna y expone vistas inmutables de sólo lectura.
 */
class GeneradorProblemas(
    problemasIniciales: List<Problema> = crearCatalogoBase()
) : IRepositorioProblemas {

    private val problemas: MutableList<Problema> = mutableListOf()

    init {
        for (problema in problemasIniciales) {
            agregarProblema(problema)
        }
    }

    override fun obtenerTodos(): List<Problema> = problemas.toList()

    override fun obtenerPorDificultad(dificultad: Dificultad): List<Problema> =
        problemas.filter { it.dificultad == dificultad }

    override fun obtenerPorId(id: Int): Problema? =
        problemas.firstOrNull { it.id == id }

    override fun obtenerPorTipo(tipo: TipoProblema): List<Problema> =
        problemas.filter { it.tipo == tipo }

    override fun agregarProblema(problema: Problema) {
        require(problemas.none { it.id == problema.id }) {
            "Ya existe un problema registrado con el ID ${problema.id}"
        }
        problemas.add(problema)
    }

    override fun cantidadTotal(): Int = problemas.size

    override fun cantidadPorDificultad(dificultad: Dificultad): Int =
        problemas.count { it.dificultad == dificultad }

    override fun obtenerProblemaAleatorio(dificultad: Dificultad?): Problema? {
        val candidatos = if (dificultad != null) obtenerPorDificultad(dificultad) else problemas
        return candidatos.randomOrNull()
    }

    /**
     * Restablece el repositorio al catálogo predeterminado de GravityQuest.
     */
    fun reiniciarAlCatalogoBase() {
        problemas.clear()
        crearCatalogoBase().forEach { agregarProblema(it) }
    }

    companion object {
        /**
         * Crea y retorna el catálogo base de ejercicios físicos de GravityQuest,
         * utilizando la constante centralizada [PhysicsConstants.GRAVEDAD_TIERRA].
         */
        fun crearCatalogoBase(): List<Problema> {
            val g = PhysicsConstants.GRAVEDAD_TIERRA
            return listOf(
                // ====================================================================
                // NIVEL FÁCIL (Caída libre pura, v₀ = 0 m/s)
                // ====================================================================
                Problema(
                    id = 1,
                    enunciado = "Un objeto cae libremente desde una altura de 20.0 metros sin velocidad inicial. Calcula el tiempo en segundos que tarda en llegar al suelo (g = $g m/s²).",
                    dificultad = Dificultad.FACIL,
                    valorEsperado = 2.02,
                    unidadMedida = "s",
                    pista = "Aplica la fórmula de tiempo de caída: t = √(2h / g).",
                    tipo = TipoProblema.CAIDA_LIBRE,
                    datosAdicionales = mapOf("alturaMetros" to 20.0, "gravedad" to g, "v0" to 0.0)
                ),
                Problema(
                    id = 2,
                    enunciado = "Una cápsula espacial de entrenamiento se suelta en caída libre desde 45.0 metros de altura sobre la plataforma terrestre. Determina cuántos segundos demora en tocar el suelo (g = $g m/s²).",
                    dificultad = Dificultad.FACIL,
                    valorEsperado = 3.03,
                    unidadMedida = "s",
                    pista = "Recuerda que como v₀ = 0, el tiempo depende únicamente de h y g: t = √(2 * 45 / $g).",
                    tipo = TipoProblema.CAIDA_LIBRE,
                    datosAdicionales = mapOf("alturaMetros" to 45.0, "gravedad" to g, "v0" to 0.0)
                ),
                Problema(
                    id = 3,
                    enunciado = "Se deja caer una esfera de acero experimental desde 80.0 metros de altura en reposo. Calcula la magnitud de la velocidad final de impacto con el suelo en m/s (g = $g m/s²).",
                    dificultad = Dificultad.FACIL,
                    valorEsperado = 39.62,
                    unidadMedida = "m/s",
                    pista = "Utiliza la fórmula de velocidad final: v_f = √(2 * g * h).",
                    tipo = TipoProblema.CAIDA_LIBRE,
                    datosAdicionales = mapOf("alturaMetros" to 80.0, "gravedad" to g, "v0" to 0.0)
                ),

                // ====================================================================
                // NIVEL MEDIO (Lanzamiento vertical hacia abajo, v₀ > 0 m/s)
                // ====================================================================
                Problema(
                    id = 4,
                    enunciado = "Un dron de suministros lanza verticalmente hacia abajo un paquete desde una altura de 25.0 metros con una velocidad inicial de 5.0 m/s. Determina su velocidad al impactar contra el suelo en m/s (g = $g m/s²).",
                    dificultad = Dificultad.MEDIO,
                    valorEsperado = 22.70,
                    unidadMedida = "m/s",
                    pista = "Aplica la ecuación independiente del tiempo: v_f² = v₀² + 2gh, de donde v_f = √(5² + 2 * $g * 25).",
                    tipo = TipoProblema.LANZAMIENTO_VERTICAL_ABAJO,
                    datosAdicionales = mapOf("alturaMetros" to 25.0, "v0" to 5.0, "gravedad" to g)
                ),
                Problema(
                    id = 5,
                    enunciado = "Un proyectil de exploración es impulsado hacia abajo desde un risco de 30.0 metros con una velocidad inicial de 4.0 m/s. ¿Cuántos segundos tarda en llegar al suelo? (g = $g m/s²).",
                    dificultad = Dificultad.MEDIO,
                    valorEsperado = 2.10,
                    unidadMedida = "s",
                    pista = "Plantea la ecuación de posición: h = v₀·t + (1/2)g·t² y resuelve la cuadrática positiva para t.",
                    tipo = TipoProblema.LANZAMIENTO_VERTICAL_ABAJO,
                    datosAdicionales = mapOf("alturaMetros" to 30.0, "v0" to 4.0, "gravedad" to g)
                ),

                // ====================================================================
                // NIVEL DIFÍCIL (Lanzamiento vertical hacia arriba, desaceleración por g)
                // ====================================================================
                Problema(
                    id = 6,
                    enunciado = "Un cohete sonda se dispara verticalmente hacia arriba con una velocidad inicial de 20.0 m/s. Calcula el tiempo en segundos que tarda en alcanzar su altura máxima (g = $g m/s²).",
                    dificultad = Dificultad.DIFICIL,
                    valorEsperado = 2.04,
                    unidadMedida = "s",
                    pista = "En el punto más alto la velocidad instantánea se anula (v = 0). Usa t_subida = v₀ / g.",
                    tipo = TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA,
                    datosAdicionales = mapOf("v0" to 20.0, "gravedad" to g)
                ),
                Problema(
                    id = 7,
                    enunciado = "Se dispara verticalmente hacia arriba una bengala de emergencia con velocidad de 25.0 m/s desde el suelo. ¿Cuál es la altura máxima en metros que alcanza antes de comenzar a caer? (g = $g m/s²).",
                    dificultad = Dificultad.DIFICIL,
                    valorEsperado = 31.86,
                    unidadMedida = "m",
                    pista = "En la cúspide v = 0. Despeja h de la fórmula: h_max = v₀² / (2g).",
                    tipo = TipoProblema.LANZAMIENTO_VERTICAL_ARRIBA,
                    datosAdicionales = mapOf("v0" to 25.0, "gravedad" to g)
                ),

                // ====================================================================
                // EXTENSIÓN OCP (Movimiento Rectilíneo Uniforme - MRU)
                // ====================================================================
                Problema(
                    id = 8,
                    enunciado = "Un rover de reconocimiento se desplaza en línea recta sobre una planicie a velocidad constante de 15.0 m/s durante 8.0 segundos. ¿Qué distancia en metros recorre?",
                    dificultad = Dificultad.FACIL,
                    valorEsperado = 120.0,
                    unidadMedida = "m",
                    pista = "En MRU la aceleración es cero. Aplica la relación directa: d = v · t.",
                    tipo = TipoProblema.MRU,
                    datosAdicionales = mapOf("velocidad" to 15.0, "tiempo" to 8.0)
                )
            )
        }
    }
}
