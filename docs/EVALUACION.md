<!-- evaluacion:inicio -->
# Evaluación — organizacion-1-1/GravityQuest-Proyecto-Final

> Documento **generado automáticamente** por la cátedra de Programación II (RepoSense + análisis de calidad POO/SOLID). **No editar a mano**: se regenera en cada evaluación.
> Rama evaluada: `main`. Análisis **heurístico** (regex + métricas): es una criba para orientar la revisión, **no una nota**.
> Generado: 2026-11-06.

## 1. Veredicto

| Aspecto | Resultado |
|---|---|
| Índice de calidad POO/SOLID | **0/100** |
| Código (LOC) | 19,842 |
| Archivos de código | 78 |
| Clases (bien usadas) | 233 (26 OK) |
| Código dentro de clases (%POO) | 99% |
| Funciones fuera de clases | 0 |
| Duplicación lógica | 68.9% |
| Señales S/O/L/I/D | 66/0/3/6/32 |
| Tope duro aplicado | `DUP>=50%` → índice ≤ 15 |

> Este informe **describe** lo que el análisis midió con los criterios ya establecidos de la materia. No es una nota.

### Semáforo de aprobación

Criterios de `CRITERIOS-CALIDAD.md`. **Aprobar** se juega con el perfil `estandar`; **destacar**, con el `exigente` (meta de excelencia).

| Semáforo | Resultado |
|---|---|
| Aprobar (`estandar`) | **no cumple** — 2/4 |
| Destacar (`exigente`) | **no cumple** — 2/5 |

**Criterios para aprobar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 85% | 99% | sí |
| Duplicación lógica | ≤ 10% | 68.9% | no |
| Funciones fuera de clases | ≤ 1 | 0 | sí |
| Señales POO/SOLID críticas | ninguna | Clase demasiado grande (SRP), Baja cohesión (LCOM), Complejidad alta, Acoplamiento alto (CBO), Interfaz con demasiados métodos (ISP), Override vacío / 'no implementado' (LSP) | no |

**Criterios para destacar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 95% | 99% | sí |
| Duplicación lógica | ≤ 5% | 68.9% | no |
| Funciones fuera de clases | ≤ 1 | 0 | sí |
| Señales POO/SOLID críticas | ninguna | Clase demasiado grande (SRP), Baja cohesión (LCOM), Complejidad alta, Acoplamiento alto (CBO), Interfaz con demasiados métodos (ISP), Override vacío / 'no implementado' (LSP) | no |
| Índice de calidad POO/SOLID | ≥ 60 | 0/100 | no |

### Cómo se calcula el %POO (y por qué no sube solo)

`%POO = líneas de lógica dentro de clases ÷ líneas de lógica totales`.

Se cuentan las líneas de **lógica y campos**; no cuentan `package`/`import`/`using`/anotaciones ni comentarios, Javadoc o docstrings. Es una **proporción**: si agregás código nuevo dentro de una clase, el numerador y el denominador crecen juntos y el porcentaje casi no cambia.

Sube cuando el código que hoy está **fuera** de clases pasa a ser método de una clase (o desaparece): funciones sueltas, `main`, constantes de módulo, scripts y lógica de archivos sueltos. También importa que ese trabajo esté **integrado en `main`**: la evaluación siempre mide `main`, así que lo que sigue en otra rama no se refleja todavía.

## 2. Autoría (RepoSense)

| Autor | Commits | Código (LOC) | Tests (LOC) | Docs (LOC) | Días activos | % del código |
|---|---:|---:|---:|---:|---:|---:|
| colombati-creator | 20 | 22,151 | 1,640 | 2 | 13 | 100% |

> _El LOC de autoría lo calcula RepoSense sobre el historial y puede diferir del LOC de código del análisis de calidad (que mira solo el estado de `main`)._

## 3. Fallas detectadas

Reúne lo que el análisis marcó y lo que todavía falta para cada semáforo. Cuando la señal se puede asociar a archivos, se indican para orientar la revisión. No es una nota.

| Falla | Tu resultado | Requisito | Archivos |
|---|---|---|---|
| Duplicación lógica | 68.9% | Aprobar: ≤ 10%<br>Destacar: ≤ 5% | `src_(codigo)/NavegadorEscenas.kt`, `src_(codigo)/RenderizadorSimulacion.kt`, `src_(codigo)/UIComponents.kt`, `src_(codigo)/VistaJuego.kt` |
| Señales POO/SOLID críticas | Clase demasiado grande (SRP), Baja cohesión (LCOM), Complejidad alta, Acoplamiento alto (CBO), Interfaz con demasiados métodos (ISP), Override vacío / 'no implementado' (LSP) | Aprobar: ninguna<br>Destacar: ninguna | `app/src/main/kotlin/gravityquest/GestorProgreso.kt`, `app/src/main/kotlin/gravityquest/NavegadorEscenas.kt`, `app/src/main/kotlin/gravityquest/RenderizadorSimulacion.kt`, `app/src/main/kotlin/gravityquest/UIComponents.kt`, `app/src/main/kotlin/gravityquest/VistaJuego.kt`, `app/src/main/kotlin/gravityquest/VistaMenu.kt` (+24 más) |
| Índice de calidad POO/SOLID | 0/100 | Destacar: ≥ 60 | — |
| Métodos/funciones largos o con muchos parámetros | **49** método(s)/función(es) largos o con muchos parámetros | Sin señales | `app/src/main/kotlin/gravityquest/GestorVidas.kt`, `app/src/main/kotlin/gravityquest/MotorSimulacionCanvas.kt`, `app/src/main/kotlin/gravityquest/NavegadorEscenas.kt`, `app/src/main/kotlin/gravityquest/RenderizadorSimulacion.kt`, `app/src/main/kotlin/gravityquest/VentanaPista.kt`, `app/src/main/kotlin/gravityquest/VistaJuego.kt` (+23 más) |
| Herencia profunda (DIT) | **3** clase(s) con herencia profunda | Sin señales | `app/src/main/kotlin/org/example/Juego/Main.kt`, `src/main.kt`, `src_(codigo)/main.kt` |
| Dependencia de concretos y falta de abstracciones (DIP) | **32** dependencia(s) de concretos sin abstracciones | Sin señales | `app/src/main/kotlin/gravityquest/GestorProgreso.kt`, `app/src/main/kotlin/gravityquest/NavegadorEscenas.kt`, `app/src/main/kotlin/gravityquest/RenderizadorSimulacion.kt`, `app/src/main/kotlin/gravityquest/UIComponents.kt`, `app/src/main/kotlin/gravityquest/VistaJuego.kt`, `app/src/main/kotlin/gravityquest/models/GeneradorProblemas.kt` (+20 más) |
| Clase sin métodos (solo datos) | **42** clase(s) sin métodos (solo datos) | Sin señales | `app/src/main/kotlin/gravityquest/EstadisticasPartida.kt`, `app/src/main/kotlin/gravityquest/GraficosUtils.kt`, `app/src/main/kotlin/gravityquest/PhysicsConstants.kt`, `app/src/main/kotlin/gravityquest/RenderizadorSimulacion.kt`, `app/src/main/kotlin/gravityquest/VistaJuego.kt`, `app/src/main/kotlin/gravityquest/models/GeneradorProblemas.kt` (+18 más) |
| Campos públicos (encapsulación) | **98** clase(s) con campos públicos | Sin señales | `app/src/main/kotlin/gravityquest/EstadisticasPartida.kt`, `app/src/main/kotlin/gravityquest/GestorProgreso.kt`, `app/src/main/kotlin/gravityquest/GestorVidas.kt`, `app/src/main/kotlin/gravityquest/GraficosUtils.kt`, `app/src/main/kotlin/gravityquest/MarcadorPuntosView.kt`, `app/src/main/kotlin/gravityquest/MotorSimulacionCanvas.kt` (+46 más) |

## 4. Aspectos evaluados

Cada aspecto indica **qué mide**, **por qué importa** y **tu resultado**. Cuando el análisis reportó una señal, se agregan preguntas para **investigar la causa** (no hay recetas: entender el porqué es parte de la evaluación).

### 1. Índice de calidad POO/SOLID (indice)

- **Qué mide:** Un resumen numérico (0-100) que combina la riqueza de clases bien usadas con la penalización por señales POO/SOLID y duplicación, y luego aplica topes duros.
- **Por qué importa:** Ordena y prioriza la revisión: un índice bajo indica varias señales que conviene mirar juntas.
- **Tu resultado:** índice **0/100**

### 2. Duplicación lógica (DRY) (duplicacion) — con señales

- **Qué mide:** Porcentaje de bloques de LÓGICA repetidos. Quedan fuera imports/includes, anotaciones, declaraciones de campos y comentarios/Javadoc/docstrings: esos bloques son boilerplate, no lógica.
- **Por qué importa:** Cada copia de lógica se mantiene por separado: un arreglo hay que repetirlo y los defectos se multiplican.
- **Tu resultado:** **68.9%** de duplicación lógica
- **Para investigar la causa:**
    - ¿El trabajo se repartió por pantallas o archivos y se integró copiando y pegando bloques?
    - ¿Qué zonas del sistema cambiaron juntas más de una vez (señal de lógica compartida)?
    - ¿Hubo un lugar común acordado para la lógica repetida antes de empezar, o cada integrante resolvió lo suyo?

### 3. Adopción de POO (% de código en clases) (poo) — sin señales

- **Qué mide:** Proporción de las **líneas de lógica** que viven dentro de una clase. Quedan fuera `package`/`import`/`using`/anotaciones y comentarios/Javadoc/docstrings: no son código que pueda estar dentro de una clase y, si contaran, el porcentaje quedaría topado por el boilerplate de cada archivo.
- **Por qué importa:** El diseño orientado a objetos es el eje de la materia: el comportamiento debería vivir como método de una clase.
- **Tu resultado:** **98.9%** del código dentro de clases
- **Cómo leerlo:** Es una **proporción**, no un acumulado: agregar código dentro de una clase no la sube por sí solo (numerador y denominador crecen juntos). Sube cuando el código que está **fuera** de clases (funciones sueltas, `main`, constantes de módulo, scripts) pasa a ser método de una clase o deja de existir. La medición se hace sobre `main`: el trabajo que todavía vive en otra rama no se refleja hasta que se integra.

### 4. Funciones fuera de clases (sueltas) — sin señales

- **Qué mide:** Funciones de nivel de módulo que no pertenecen a ninguna clase (en esta materia el comportamiento debe ser método de una clase).
- **Por qué importa:** Son la forma más directa de código procedural en un proyecto que debería ser orientado a objetos.
- **Tu resultado:** **0** función(es) fuera de clases

### 5. Clase demasiado grande (SRP) (god) — con señales

- **Qué mide:** Clases con demasiadas líneas o métodos de LÓGICA. No cuentan los getters/setters triviales ni la cantidad de campos por sí sola.
- **Por qué importa:** Una clase que hace de todo cambia por muchos motivos y cuesta entenderla y probarla.
- **Tu resultado:** **9** clase(s) demasiado grande(s)
- **Para investigar la causa:**
    - ¿La clase creció por acumulación de responsabilidades?
    - ¿Qué partes de la clase se usan siempre juntas y cuáles no?
    - ¿Se podría describir su responsabilidad en una sola frase?

### 6. Baja cohesión (LCOM) (cohesion) — con señales

- **Qué mide:** Métodos y campos de una clase que casi no se relacionan entre sí.
- **Por qué importa:** Una unidad que cambia por motivos distintos y comparte poca información suele esconder más de una responsabilidad.
- **Tu resultado:** **18** clase(s) con baja cohesión
- **Para investigar la causa:**
    - ¿Qué métodos de la clase no comparten estado con el resto?
    - ¿Cambian por motivos distintos según la tarea?
    - ¿Se pensó su responsabilidad antes de escribirla, o se fue agregando?

### 7. Complejidad alta (complejidad) — con señales

- **Qué mide:** Complejidad ciclomática: cantidad de caminos posibles dentro de un método o función (condicionales, bucles, casos).
- **Por qué importa:** Muchos caminos = difícil de seguir y de probar; suelen esconderse casos sin cubrir.
- **Tu resultado:** **3** método(s)/función(es) con complejidad alta
- **Para investigar la causa:**
    - ¿Se agregaron condiciones a medida que aparecían casos nuevos?
    - ¿Se probaron todos los caminos o solo el caso principal?
    - ¿Hay reglas de negocio mezcladas con detalles técnicos?

### 8. Métodos/funciones largos o con muchos parámetros (tamano) — con señales

- **Qué mide:** Tamaño de los métodos/funciones y cantidad de parámetros.
- **Por qué importa:** Una función que hace varias cosas cuesta leerla, nombrarla y reutilizarla.
- **Tu resultado:** **49** método(s)/función(es) largos o con muchos parámetros
- **Para investigar la causa:**
    - ¿La función creció por acumulación de pasos?
    - ¿Agrupa operaciones que podrían describirse por separado?
    - ¿Los parámetros representan una misma idea (un objeto) o cosas sueltas?

### 9. Acoplamiento alto (CBO) (acoplamiento) — con señales

- **Qué mide:** Cuánto depende una clase de otros tipos del proyecto. No cuentan los tipos de la plataforma (biblioteca estándar, DOM, motor Godot) ni los métodos/propiedades propias.
- **Por qué importa:** Piezas muy enlazadas hacen que un cambio se propague y que sea difícil reemplazar partes.
- **Tu resultado:** **18** clase(s) muy acoplada(s)
- **Para investigar la causa:**
    - ¿La clase conoce detalles internos de muchas otras?
    - ¿Se puede cambiar una parte sin tocar las demás?
    - ¿Los colaboradores llegan como parámetros o se crean por dentro?

### 10. Herencia profunda (DIT) (herencia) — con señales

- **Qué mide:** Profundidad de la cadena de herencia de una clase.
- **Por qué importa:** Jerarquías muy profundas vuelven difícil saber de dónde viene cada comportamiento.
- **Tu resultado:** **3** clase(s) con herencia profunda
- **Para investigar la causa:**
    - ¿La jerarquía creció para reutilizar código o para modelar una relación real 'es-un'?
    - ¿Qué comportamiento cambia entre niveles de la jerarquía?
    - ¿Se evaluó la composición como alternativa?

### 11. Switch sobre tipos (OCP) (ocp) — sin señales

- **Qué mide:** Cadenas de dispatch por tipo (`else if ... is` / `else if ... instanceof`, `switch` sobre tipo o `typeof`). Un `is Tipo variable` aislado (pattern matching / cast seguro) NO cuenta como switch.
- **Por qué importa:** Cada tipo nuevo obliga a tocar varios lugares, en vez de extender con una implementación nueva.
- **Tu resultado:** **0** clase(s) con switch sobre tipos

### 12. Interfaz con demasiados métodos (ISP) (isp) — con señales

- **Qué mide:** Interfaces o clases abstractas con muchos métodos declarados.
- **Por qué importa:** Obliga a implementar operaciones que no se usan y acopla a los clientes a métodos que no necesitan.
- **Tu resultado:** **6** interfaz(es) con demasiados métodos
- **Para investigar la causa:**
    - ¿Qué subconjunto de métodos usa realmente cada implementación?
    - ¿Se agruparon métodos de necesidades distintas en la misma interfaz?
    - ¿Hay implementaciones que dejan métodos sin uso?

### 13. Override vacío o 'no implementado' (LSP) (lsp) — con señales

- **Qué mide:** Métodos sobrescritos que quedan vacíos o lanzan 'no implementado'.
- **Por qué importa:** Suele indicar que la jerarquía no representa bien el problema y rompe la sustitución entre clases.
- **Tu resultado:** **3** override(s) vacío(s) o 'no implementado'
- **Para investigar la causa:**
    - ¿Por qué ese método no tiene comportamiento en esa clase?
    - ¿La clase hereda algo que en realidad no le corresponde?
    - ¿Qué contrato se esperaba cumplir al heredar?

### 14. Dependencia de concretos y falta de abstracciones (DIP) (dip) — con señales

- **Qué mide:** Instanciación directa de clases **propias** concretas y ausencia de interfaces o clases abstractas pese al acoplamiento. No cuentan los structs ni los nodos/tipos del motor.
- **Por qué importa:** Depender de implementaciones concretas dificulta cambiar o probar una parte sin arrastrar las demás.
- **Tu resultado:** **32** dependencia(s) de concretos sin abstracciones
- **Para investigar la causa:**
    - ¿La clase crea por dentro a sus colaboradores en vez de recibirlos?
    - ¿Hay puntos de variación que convendría expresar como abstracción?
    - ¿Cómo se probaría esa clase cambiando una implementación?

### 15. Clase sin métodos (solo datos) (data) — con señales

- **Qué mide:** Clases que solo acumulan campos y no tienen comportamiento.
- **Por qué importa:** Separar datos de comportamiento reparte por el proyecto la lógica que debería estar cerca de esos datos.
- **Tu resultado:** **42** clase(s) sin métodos (solo datos)
- **Para investigar la causa:**
    - ¿Dónde vive el comportamiento que opera sobre esos datos?
    - ¿La clase representa un concepto del dominio o un contenedor técnico?
    - ¿Se evita la lógica de negocio dentro de la clase por comodidad?

### 16. Campos públicos (encapsulación) (encapsulacion) — con señales

- **Qué mide:** Clases que exponen campos sin control de acceso.
- **Por qué importa:** Expone el estado interno y hace que cualquier cambio dependa de quién lo modifica desde afuera.
- **Tu resultado:** **98** clase(s) con campos públicos
- **Para investigar la causa:**
    - ¿Se accede al campo desde otras clases para evitar un método?
    - ¿Qué reglas deberían respetarse al modificar ese estado?
    - ¿Los campos públicos se usan más como configuración que como estado?

### 17. Clase solo con métodos estáticos (utilitaria) (static) — sin señales

- **Qué mide:** Clases donde todos los métodos son estáticos.
- **Por qué importa:** Es comportamiento procedural disfrazado de clase: no se instancia ni participa del modelo de objetos.
- **Tu resultado:** **0** clase(s) solo con métodos estáticos

## 5. Evolución por commit

Métricas calculadas sobre el estado de `main` en **cada commit** (19 commits con trabajo), del primero al último. La calidad usa el perfil `exigente`; el aporte de código lo mide RepoSense. El índice puede **bajar** al crecer el proyecto: se acumulan señales.

```mermaid
xychart-beta
    title "Índice POO/SOLID a lo largo de los commits"
    x-axis ["052883a", "f27feb0", "52dd827", "9a37451", "6f47bc8", "9f0b68b", "42dbe3e", "6cb23a6", "f662fcc", "967a698", "8501235", "62f0ff0", "aaddf8e", "652075e", "1e2658f", "a3a2de3", "471f067", "f8ab0c3", "61da5f8"]
    y-axis "Índice" 0 --> 100
    line [0, 0, 38, 38, 44, 44, 8, 8, 7, 3, 4, 3, 3, 2, 2, 1, 1, 1, 1]
```

| # | Fecha | Commit | Autor | LOC | Índice | %POO | Duplic. | Sueltas |
|---|---|---|---|---:|---:|---:|---:|---:|
| 1 | 2026-08-21 | `052883a` | ciro | 0 | 0 | 0% | 0.0% | 0 |
| 2 | 2026-08-24 | `f27feb0` | dqmdz-etec | 0 | 0 | 0% | 0.0% | 0 |
| 3 | 2026-08-28 | `52dd827` | ciro | 76 | 38 | 26% | 0.0% | 1 |
| 4 | 2026-08-28 | `9a37451` | ciro | 76 | 38 | 26% | 0.0% | 1 |
| 5 | 2026-09-04 | `6f47bc8` | ciro | 135 | 44 | 30% | 0.0% | 1 |
| 6 | 2026-09-07 | `9f0b68b` | ciro | 159 | 44 | 29% | 0.0% | 1 |
| 7 | 2026-10-02 | `42dbe3e` | ciro | 326 | 8 | 25% | 53.5% | 1 |
| 8 | 2026-10-05 | `6cb23a6` | ciro | 467 | 8 | 78% | 38.8% | 1 |
| 9 | 2026-10-05 | `f662fcc` | ciro | 523 | 7 | 82% | 40.9% | 1 |
| 10 | 2026-10-05 | `967a698` | ciro | 1,336 | 3 | 94% | 63.9% | 1 |
| 11 | 2026-10-09 | `8501235` | ciro | 1,640 | 4 | 96% | 61.7% | 1 |
| 12 | 2026-10-09 | `62f0ff0` | ciro | 3,354 | 3 | 96% | 66.5% | 1 |
| 13 | 2026-10-09 | `aaddf8e` | ciro | 4,142 | 3 | 88% | 64.0% | 3 |
| 14 | 2026-10-16 | `652075e` | ciro | 6,182 | 2 | 92% | 59.2% | 5 |
| 15 | 2026-10-19 | `1e2658f` | ciro | 7,932 | 2 | 88% | 66.8% | 13 |
| 16 | 2026-10-26 | `a3a2de3` | ciro | 10,376 | 1 | 99% | 67.4% | 0 |
| 17 | 2026-10-30 | `471f067` | ciro | 14,694 | 1 | 99% | 69.8% | 0 |
| 18 | 2026-10-30 | `f8ab0c3` | ciro | 17,870 | 1 | 99% | 68.8% | 0 |
| 19 | 2026-10-30 | `61da5f8` | ciro | 19,842 | 1 | 99% | 68.9% | 0 |

<!-- actividad-clases:inicio -->
## Actividad en días de clase

> Período **2026-08-14 → 2026-10-29** · **17 clases** (lunes, viernes) · calendario oficial de la materia.
>
> `Commits` = commits de trabajo (no-merge); `Merges` = merges de PR (también son push). Los días sin actividad dicen **sin actividad**. El cuadro principal usa sólo las fechas de clase del calendario oficial.
>
> **Aportes fuera del calendario:** cada alumno tiene un cuadro aparte con las fechas no lectivas con actividad, desde **2026-08-14** hasta **2026-11-06** (ventana de aportes). Esas fechas no forman parte del cuadro principal, pero se muestran para no perder trabajo hecho en fines de semana, feriados o después del período.
>
> Se recorre `git log --all` sobre un mirror (todas las ramas y `refs/pull/*`), con deduplicación por commit. Fecha = fecha de autor en UTC-3. Los commits locales no pusheados no se ven. Si el trabajo fue en pareja y el commit es de otra persona, figura a nombre del autor del commit; para reflejarlo, usar `Co-authored-by` o una nota manual.
>
> Sección informativa: no modifica el semáforo de aprobación.

### colombati-creator

**12/17 clases con actividad** · 19 commits · 14 merges

| Fecha | Marca | Repositorios | Commits / merges |
|---|:--:|---|--:|
| 2026-08-14 | No | sin actividad | 0 / 0 |
| 2026-08-21 | **Sí** | GravityQuest-Proyecto-Final | 1 / 0 |
| 2026-08-24 | No | sin actividad | 0 / 0 |
| 2026-08-28 | **Sí** | GravityQuest-Proyecto-Final | 2 / 2 |
| 2026-08-31 | **Sí** | GravityQuest-Proyecto-Final | 1 / 0 |
| 2026-09-04 | **Sí** | GravityQuest-Proyecto-Final | 2 / 1 |
| 2026-09-07 | **Sí** | GravityQuest-Proyecto-Final | 1 / 1 |
| 2026-09-14 | No | sin actividad | 0 / 0 |
| 2026-09-25 | No | sin actividad | 0 / 0 |
| 2026-09-28 | No | sin actividad | 0 / 0 |
| 2026-10-02 | **Sí** | GravityQuest-Proyecto-Final | 2 / 1 |
| 2026-10-05 | **Sí** | GravityQuest-Proyecto-Final | 2 / 3 |
| 2026-10-09 | **Sí** | GravityQuest-Proyecto-Final | 3 / 3 |
| 2026-10-16 | **Sí** | GravityQuest-Proyecto-Final | 1 / 1 |
| 2026-10-19 | **Sí** | GravityQuest-Proyecto-Final | 1 / 1 |
| 2026-10-23 | **Sí** | GravityQuest-Proyecto-Final | 1 / 0 |
| 2026-10-26 | **Sí** | GravityQuest-Proyecto-Final | 2 / 1 |

#### Aportes fuera del calendario de clases — colombati-creator

**colombati-creator — 1 fecha fuera del calendario** · 2 commits · 3 merges

| Alumno | Fecha | Repositorios | Commits / merges |
|---|---|---|---|
| colombati-creator (`colombati-creator`) | 2026-10-30 | GravityQuest-Proyecto-Final | 2 / 3 |

<!-- actividad-clases:fin -->

---
_Informe generado por la cátedra. Los criterios y umbrales de medición corresponden al perfil `exigente` de `calidad.py`; el semáforo de aprobación, a `CRITERIOS-CALIDAD.md` (ver `README.md`). Ante dudas, revisá el archivo señalado._
<!-- evaluacion:fin -->
