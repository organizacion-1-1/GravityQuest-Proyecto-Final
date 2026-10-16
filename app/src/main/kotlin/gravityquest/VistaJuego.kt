package gravityquest

import javafx.animation.AnimationTimer
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import gravityquest.models.Dificultad
import gravityquest.models.GeneradorProblemas
import gravityquest.models.IRepositorioProblemas
import gravityquest.models.Problema

/**
 * ============================================================================
 * GravityQuest - Pantalla de Juego (VistaJuego)
 * ============================================================================
 *
 * Principio de Responsabilidad Única (SRP):
 * Se encarga exclusivamente de presentar y gestionar la pantalla del problema/ejercicio
 * físico: muestra el enunciado de acuerdo a la dificultad seleccionada, valida la respuesta
 * numérica del usuario con el modelo [Problema], orquesta el feedback visual y sincroniza
 * la simulación cinemática en el lienzo. Delega la navegación al [INavegadorEscenas].
 *
 * Principio de Inversión de Dependencias (DIP):
 * Depende de contratos abstractos ([INavegadorEscenas], [IRepositorioProblemas], [ICalculadoraFisica],
 * [RenderizadorSimulacion], [IGestorFeedback]) permitiendo desacoplamiento total y alta testabilidad.
 *
 * Principio Abierto/Cerrado (OCP):
 * Puede recibir cualquier implementación de repositorio o calculadora física para soportar
 * nuevos desafíos sin modificar la estructura visual ni de interacción.
 *
 * @param dificultad Dificultad seleccionada en el menú inicial.
 * @param navegador Gestor de navegación para retornar al menú o cambiar de pantalla.
 * @param repositorio Repositorio proveedor del catálogo de problemas físicos.
 * @param calculadora Calculadora cinemática y física para la simulación.
 */
class VistaJuego(
    val dificultad: Dificultad,
    private val navegador: INavegadorEscenas,
    val repositorio: IRepositorioProblemas = GeneradorProblemas(),
    val calculadora: ICalculadoraFisica = CalculadoraFisica()
) : BorderPane() {

    /** Nodo raíz de la vista de juego (referencia a sí mismo como contenedor). */
    val root: Parent get() = this

    // 1. Problema físico actualmente activo cargado desde el repositorio
    var problemaActual: Problema = cargarProblemaParaDificultad(dificultad)
        private set

    // 2. Altura en metros del ejercicio para configurar el lienzo físico
    val alturaProblema: Double
        get() = (problemaActual.datosAdicionales["alturaMetros"] as? Number)?.toDouble() ?: 20.0

    // 3. Estado físico y renderizado gráfico desacoplados (SRP / DIP)
    val objetoSimulado: ObjetoSimulado = ObjetoSimulado(alturaMaxima = alturaProblema)
    val canvasSimulacion: Canvas = Canvas(320.0, 320.0).apply {
        styleClass.add("canvas-simulacion")
    }
    var renderizador: RenderizadorSimulacion = RenderizadorCanvas(canvasSimulacion, alturaProblema)

    // 4. Componentes UI de navegación superior
    val btnVolverMenu: Button = Button("⬅ Volver al Menú").apply {
        styleClass.addAll("button", "btn-secundario", "btn-volver")
    }

    val lblBadgeDificultad: Label = Label().apply {
        styleClass.add("badge-dificultad")
    }

    val btnOtroProblema: Button = Button("🔄 Otro Ejercicio").apply {
        styleClass.addAll("button", "btn-secundario")
    }

    // 5. Componentes UI del panel de control del problema
    val lblTituloMision: Label = Label().apply {
        styleClass.addAll("label")
        style = "-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 14px;"
    }

    val lblEnunciado: Label = Label().apply {
        styleClass.addAll("label", "enunciado")
        isWrapText = true
        maxWidth = 340.0
    }

    val lblPista: Label = Label().apply {
        styleClass.add("label")
        isWrapText = true
        maxWidth = 340.0
        style = "-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-style: italic;"
    }

    val txtRespuesta: TextField = TextField().apply {
        styleClass.add("text-field")
        maxWidth = 240.0
    }

    val btnValidar: Button = Button("Validar y Simular").apply {
        styleClass.addAll("button", "btn-primario")
        maxWidth = 240.0
    }

    val btnSimular: Button = Button("Simular Caída").apply {
        styleClass.addAll("button", "btn-secundario")
        maxWidth = 200.0
    }

    val lblResultado: Label = Label().apply {
        styleClass.add("label")
        isWrapText = true
        maxWidth = 340.0
        alignment = Pos.CENTER
    }

    // 6. Gestor de retroalimentación visual (animaciones y clases de resultado)
    var gestorFeedback: IGestorFeedback = GestorFeedbackVisual(lblResultado)

    // 7. Control de la simulación gráfica a 60 FPS
    var timerSimulacion: AnimationTimer? = null
    var tiempoInicioNano: Long = 0L
    var simulacionEnCurso: Boolean = false
        private set

    init {
        // Estilos base de la pantalla
        styleClass.add("root")
        padding = Insets(16.0)

        // Configuración de la barra de navegación superior (Header)
        val barraSuperior = HBox(16.0).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("barra-navegacion")
            val espaciador = javafx.scene.layout.Region()
            HBox.setHgrow(espaciador, Priority.ALWAYS)
            children.addAll(btnVolverMenu, espaciador, lblBadgeDificultad, btnOtroProblema)
        }
        top = barraSuperior
        BorderPane.setMargin(barraSuperior, Insets(0.0, 0.0, 16.0, 0.0))

        // Panel de control (Izquierda)
        val panelControl = VBox(14.0).apply {
            alignment = Pos.CENTER
            maxWidth = 360.0
            styleClass.add("panel-control")
            children.addAll(
                lblTituloMision,
                lblEnunciado,
                lblPista,
                txtRespuesta,
                btnValidar,
                lblResultado
            )
        }

        // Panel de simulación (Derecha)
        val panelSimulacion = VBox(14.0).apply {
            alignment = Pos.CENTER
            styleClass.add("panel-simulacion")
            children.addAll(
                canvasSimulacion,
                btnSimular
            )
        }

        // Contenedor principal central (HBox)
        val contenedorCentral = HBox(22.0).apply {
            alignment = Pos.CENTER
            children.addAll(panelControl, panelSimulacion)
        }
        center = contenedorCentral

        // Inicialización de datos del problema y renderizado inicial
        actualizarDatosProblemaEnUI()
        configurarAnimationTimer()
        renderizador.renderizar(objetoSimulado)

        // Configuración de acciones interactivas
        btnVolverMenu.setOnAction {
            detenerSimulacion()
            navegador.irAMenu()
        }

        btnValidar.setOnAction {
            validarRespuesta()
        }

        txtRespuesta.setOnAction {
            validarRespuesta()
        }

        btnSimular.setOnAction {
            iniciarOReiniciarSimulacion()
        }

        btnOtroProblema.setOnAction {
            cargarNuevoProblema()
        }
    }

    /**
     * Carga un problema correspondiente a la dificultad especificada desde el repositorio.
     */
    private fun cargarProblemaParaDificultad(dif: Dificultad): Problema {
        return repositorio.obtenerPorDificultad(dif).randomOrNull()
            ?: repositorio.obtenerPorDificultad(dif).firstOrNull()
            ?: Problema(
                id = 1,
                enunciado = "Un objeto cae libremente desde una altura de 20.0 metros sin velocidad inicial. Calcula el tiempo en segundos (g = 9.81 m/s²).",
                dificultad = dif,
                valorEsperado = 2.02,
                unidadMedida = "s",
                pista = "t = √(2h / g)",
                datosAdicionales = mapOf("alturaMetros" to 20.0)
            )
    }

    /**
     * Actualiza la interfaz con la información del problema actualmente seleccionado.
     */
    private fun actualizarDatosProblemaEnUI() {
        lblBadgeDificultad.text = "Dificultad: ${dificultad.nombreVisible} | ${problemaActual.tipo.nombre}"
        lblTituloMision.text = "MISIÓN #${problemaActual.id} ── ${dificultad.nombreVisible.uppercase()}"
        lblEnunciado.text = problemaActual.enunciado
        lblPista.text = "💡 Pista: ${problemaActual.pista}"
        txtRespuesta.promptText = "Respuesta en ${problemaActual.unidadMedida} (ej. ${problemaActual.valorEsperado})"
        txtRespuesta.clear()
        gestorFeedback.limpiar()

        objetoSimulado.reiniciar()
        renderizador = RenderizadorCanvas(canvasSimulacion, alturaProblema)
        renderizador.renderizar(objetoSimulado)
    }

    /**
     * Carga un nuevo problema aleatorio de la misma dificultad y refresca la pantalla.
     */
    fun cargarNuevoProblema() {
        detenerSimulacion()
        val candidatos = repositorio.obtenerPorDificultad(dificultad)
        val otro = candidatos.filter { it.id != problemaActual.id }.randomOrNull() ?: candidatos.firstOrNull()
        if (otro != null) {
            problemaActual = otro
            actualizarDatosProblemaEnUI()
        }
    }

    /**
     * Valida la respuesta numérica ingresada por el usuario utilizando el modelo [Problema]:
     * - Si es correcta: muestra feedback de éxito y dispara la simulación física animada.
     * - Si es incorrecta o inválida: muestra feedback de error y detiene la animación.
     *
     * @return `true` si la respuesta fue admitida dentro del margen de error, `false` en caso contrario.
     */
    fun validarRespuesta(): Boolean {
        val entrada = txtRespuesta.text.trim().replace(',', '.')
        val valorIngresado = entrada.toDoubleOrNull()

        if (valorIngresado == null) {
            detenerSimulacion()
            objetoSimulado.reiniciar()
            renderizador.renderizar(objetoSimulado)
            gestorFeedback.mostrarErrorFormato("¡Entrada inválida! Por favor ingresa un número.")
            return false
        }

        val esCorrecto = problemaActual.validarRespuesta(valorIngresado)

        if (esCorrecto) {
            gestorFeedback.mostrarExito("¡Correcto! Respuesta dentro de la tolerancia (±${dificultad.margenErrorTolerable} ${problemaActual.unidadMedida}).")
            iniciarOReiniciarSimulacion()
            return true
        } else {
            detenerSimulacion()
            objetoSimulado.reiniciar()
            renderizador.renderizar(objetoSimulado)
            gestorFeedback.mostrarErrorPrecision("¡Intenta de nuevo! Tu respuesta no es precisa.")
            return false
        }
    }

    /**
     * Configura el bucle de animación de 60 FPS con [AnimationTimer] para renderizar la caída.
     */
    private fun configurarAnimationTimer() {
        timerSimulacion = object : AnimationTimer() {
            override fun handle(now: Long) {
                if (tiempoInicioNano == 0L) {
                    tiempoInicioNano = now
                }

                val deltaSegundos = (now - tiempoInicioNano) / 1_000_000_000.0
                val tiempoTotalCaida = calculadora.calcularTiempo(alturaProblema)

                if (deltaSegundos >= tiempoTotalCaida) {
                    val vFinal = calculadora.calcularVelocidadFinal(alturaProblema)
                    objetoSimulado.actualizar(alturaProblema, vFinal, tiempoTotalCaida, haLlegadoAlSuelo = true)
                    renderizador.renderizar(objetoSimulado)
                    detenerSimulacion()
                } else {
                    val posY = calculadora.calcularPosicionCaida(deltaSegundos)
                    val vY = calculadora.g * deltaSegundos
                    objetoSimulado.actualizar(posY, vY, deltaSegundos, haLlegadoAlSuelo = false)
                    renderizador.renderizar(objetoSimulado)
                }
            }
        }
    }

    /**
     * Inicia o reinicia la simulación del objeto en caída en el Canvas.
     */
    fun iniciarOReiniciarSimulacion() {
        detenerSimulacion()
        tiempoInicioNano = 0L
        objetoSimulado.reiniciar()
        renderizador.renderizar(objetoSimulado)
        simulacionEnCurso = true
        timerSimulacion?.start()
    }

    /**
     * Detiene el temporizador de la simulación gráfica.
     */
    fun detenerSimulacion() {
        timerSimulacion?.stop()
        simulacionEnCurso = false
    }
}
