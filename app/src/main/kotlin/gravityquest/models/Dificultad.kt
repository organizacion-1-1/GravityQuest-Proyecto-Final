package gravityquest.models
import gravityquest.*

interface IDificultadVisual {
    val nombreMostrar: String
    val colorHex: String
    val icono: String
    fun subtituloSector(): String
}

interface IDificultadInfo : IDificultadVisual {
    val multiplicadorPuntos: Int
    val ejerciciosRequeridos: Int
    val descripcion: String
    fun siguiente(): Dificultad?
    fun siguienteNivel(): Dificultad?
    fun esNivelValido(): Boolean
}

enum class Dificultad : IDificultadInfo {
    FACIL {
        override val nombreMostrar: String = "F\u00e1cil"
        override val multiplicadorPuntos: Int = 1
        override val colorHex: String = "#00F5D4"
        override val descripcion: String =
            "\u0043a\u00edda libre pura y c\u00e1lculos directos de una sola f\u00f3rmula."
        override val icono: String = "\uD83D\uDEF0"
        override fun subtituloSector(): String = "// \u00d3RBITA BAJA"
    },
    MEDIO {
        override val nombreMostrar: String = "\u004dedio"
        override val multiplicadorPuntos: Int = 2
        override val colorHex: String = "#FFB703"
        override val descripcion: String =
            "\u004canzamientos verticales con velocidad inicial y an\u00e1lisis de alturas."
        override val icono: String = "\u26A1"
        override fun subtituloSector(): String = "// GRAVEDAD VARIABLE"
    },
    DIFICIL {
        override val nombreMostrar: String = "\u0044if\u00edcil"
        override val multiplicadorPuntos: Int = 3
        override val colorHex: String = "#FF007F"
        override val descripcion: String =
            "\u0050roblemas combinados de alta precisi\u00f3n, tiempos de vuelo y velocidades."
        override val icono: String = "\uD83C\uDF00"
        override fun subtituloSector(): String = "// SINGULARITY / CAOS"
    };

    override val ejerciciosRequeridos: Int = GameConfig.MINIMO_PARA_DESBLOQUEAR

    override fun siguiente(): Dificultad? {
        return entries.getOrNull(ordinal + 1)
    }

    override fun siguienteNivel(): Dificultad? {
        return siguiente()
    }

    override fun esNivelValido(): Boolean {
        return multiplicadorPuntos > 0 && ejerciciosRequeridos > 0
    }
}
