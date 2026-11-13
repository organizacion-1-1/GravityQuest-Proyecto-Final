package gravityquest.models
import gravityquest.*

interface IDificultadVisual {
    val nombreMostrar: IString
    val colorHex: IString
    val icono: IString
    fun subtituloSector(): IString
}

interface IDificultadInfo : IDificultadVisual {
    val multiplicadorPuntos: IInt
    val ejerciciosRequeridos: IInt
    val descripcion: IString
    fun siguiente(): Dificultad?
    fun siguienteNivel(): Dificultad?
    fun esNivelValido(): IBoolean
}

enum class Dificultad : IDificultadInfo {
    FACIL {
        override val nombreMostrar: IString = "F\u00e1cil"
        override val multiplicadorPuntos: IInt = 1
        override val colorHex: IString = "#00F5D4"
        override val descripcion: IString =
            "\u0043a\u00edda libre pura y c\u00e1lculos directos de una sola f\u00f3rmula."
        override val icono: IString = "\uD83D\uDEF0"
        override fun subtituloSector(): IString = "// \u00d3RBITA BAJA"
    },
    MEDIO {
        override val nombreMostrar: IString = "\u004dedio"
        override val multiplicadorPuntos: IInt = 2
        override val colorHex: IString = "#FFB703"
        override val descripcion: IString =
            "\u004canzamientos verticales con velocidad inicial y an\u00e1lisis de alturas."
        override val icono: IString = "\u26A1"
        override fun subtituloSector(): IString = "// GRAVEDAD VARIABLE"
    },
    DIFICIL {
        override val nombreMostrar: IString = "\u0044if\u00edcil"
        override val multiplicadorPuntos: IInt = 3
        override val colorHex: IString = "#FF007F"
        override val descripcion: IString =
            "\u0050roblemas combinados de alta precisi\u00f3n, tiempos de vuelo y velocidades."
        override val icono: IString = "\uD83C\uDF00"
        override fun subtituloSector(): IString = "// SINGULARITY / CAOS"
    };

    override val ejerciciosRequeridos: IInt = GameConfig.MINIMO_PARA_DESBLOQUEAR

    override fun siguiente(): Dificultad? {
        return entries.getOrNull(ordinal + 1)
    }

    override fun siguienteNivel(): Dificultad? {
        return siguiente()
    }

    override fun esNivelValido(): IBoolean {
        return multiplicadorPuntos > 0 && ejerciciosRequeridos > 0
    }
}
