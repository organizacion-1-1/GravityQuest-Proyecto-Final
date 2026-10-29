<!-- evaluacion:inicio -->
# Evaluación — organizacion-1-1/GravityQuest-Proyecto-Final

> Documento **generado automáticamente** por la cátedra de Programación II (RepoSense + análisis de calidad POO/SOLID). **No editar a mano**: se regenera en cada evaluación.
> Rama evaluada: `main`. Análisis **heurístico** (regex + métricas): es una criba para orientar la revisión, **no una nota**.
> Generado: 2026-10-29.

## 1. Veredicto

| Aspecto | Resultado |
|---|---|
| Índice de calidad POO/SOLID | **1/100** |
| Código (LOC) | 10,376 |
| Archivos de código | 54 |
| Clases (bien usadas) | 164 (32 OK) |
| Código dentro de clases (%POO) | 83% |
| Funciones fuera de clases | 15 |
| Duplicación lógica | 69.0% |
| Señales S/O/L/I/D | 82/2/6/6/53 |
| Tope duro aplicado | `DUP>=50%` → índice ≤ 15 |

> Este informe **describe** lo que el análisis midió con los criterios ya establecidos de la materia. No es una nota.

### Semáforo de aprobación

Criterios de `CRITERIOS-CALIDAD.md`. **Aprobar** se juega con el perfil `estandar`; **destacar**, con el `exigente` (meta de excelencia).

| Semáforo | Resultado |
|---|---|
| Aprobar (`estandar`) | **no cumple** — 0/4 |
| Destacar (`exigente`) | **no cumple** — 0/5 |

**Criterios para aprobar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 85% | 83% | no |
| Duplicación lógica | ≤ 10% | 69.0% | no |
| Funciones fuera de clases | ≤ 1 | 15 | no |
| Señales POO/SOLID críticas | ninguna | Clase demasiado grande (SRP), Baja cohesión (LCOM), Acoplamiento alto (CBO), Switch sobre tipos (OCP), Interfaz con demasiados métodos (ISP), Override vacío / 'no implementado' (LSP) | no |

**Criterios para destacar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 95% | 83% | no |
| Duplicación lógica | ≤ 5% | 69.0% | no |
| Funciones fuera de clases | ≤ 1 | 15 | no |
| Señales POO/SOLID críticas | ninguna | Clase demasiado grande (SRP), Baja cohesión (LCOM), Complejidad alta, Acoplamiento alto (CBO), Switch sobre tipos (OCP), Interfaz con demasiados métodos (ISP), Override vacío / 'no implementado' (LSP) | no |
| Índice de calidad POO/SOLID | ≥ 60 | 1/100 | no |

## 2. Autoría (RepoSense)

| Autor | Commits | Código (LOC) | Tests (LOC) | Docs (LOC) | Días activos | % del código |
|---|---:|---:|---:|---:|---:|---:|
| colombati-creator | 17 | 11,616 | 1,267 | 2 | 12 | 100% |

> _El LOC de autoría lo calcula RepoSense sobre el historial y puede diferir del LOC de código del análisis de calidad (que mira solo el estado de `main`)._

## 3. Aspectos evaluados

Cada aspecto indica **qué mide**, **por qué importa** y **tu resultado**. Cuando el análisis reportó una señal, se agregan preguntas para **investigar la causa** (no hay recetas: entender el porqué es parte de la evaluación).

### 1. Índice de calidad POO/SOLID (indice)

- **Qué mide:** Un resumen numérico (0-100) que combina la riqueza de clases bien usadas con la penalización por señales POO/SOLID y duplicación, y luego aplica topes duros.
- **Por qué importa:** Ordena y prioriza la revisión: un índice bajo indica varias señales que conviene mirar juntas.
- **Tu resultado:** índice **1/100**

### 2. Duplicación lógica (DRY) (duplicacion) — con señales

- **Qué mide:** Porcentaje de bloques de LÓGICA repetidos. Quedan fuera imports/includes, anotaciones, declaraciones de campos y comentarios/Javadoc/docstrings: esos bloques son boilerplate, no lógica.
- **Por qué importa:** Cada copia de lógica se mantiene por separado: un arreglo hay que repetirlo y los defectos se multiplican.
- **Tu resultado:** **69.0%** de duplicación lógica
- **Para investigar la causa:**
    - ¿El trabajo se repartió por pantallas o archivos y se integró copiando y pegando bloques?
    - ¿Qué zonas del sistema cambiaron juntas más de una vez (señal de lógica compartida)?
    - ¿Hubo un lugar común acordado para la lógica repetida antes de empezar, o cada integrante resolvió lo suyo?

### 3. Adopción de POO (% de código en clases) (poo) — sin señales

- **Qué mide:** Qué porción del código vive dentro de clases (no de funciones sueltas).
- **Por qué importa:** El diseño orientado a objetos es el eje de la materia: el comportamiento debería vivir como método de una clase.
- **Tu resultado:** **82.7%** del código dentro de clases

### 4. Funciones fuera de clases (sueltas) — con señales

- **Qué mide:** Funciones de nivel de módulo que no pertenecen a ninguna clase (en esta materia el comportamiento debe ser método de una clase).
- **Por qué importa:** Son la forma más directa de código procedural en un proyecto que debería ser orientado a objetos.
- **Tu resultado:** **15** función(es) fuera de clases
- **Para investigar la causa:**
    - ¿Esa función se dejó suelta por urgencia o por desconocer dónde ubicarla?
    - ¿Qué clase debería ser responsable de ese comportamiento?
    - ¿Había una convención sobre dónde poner cada cosa?

### 5. Clase demasiado grande (SRP) (god) — con señales

- **Qué mide:** Clases con muchas líneas, métodos o campos a la vez.
- **Por qué importa:** Una clase que hace de todo cambia por muchos motivos y cuesta entenderla y probarla.
- **Tu resultado:** **44** clase(s) demasiado grande(s)
- **Para investigar la causa:**
    - ¿La clase creció por acumulación de responsabilidades?
    - ¿Qué partes de la clase se usan siempre juntas y cuáles no?
    - ¿Se podría describir su responsabilidad en una sola frase?

### 6. Baja cohesión (LCOM) (cohesion) — con señales

- **Qué mide:** Métodos y campos de una clase que casi no se relacionan entre sí.
- **Por qué importa:** Una unidad que cambia por motivos distintos y comparte poca información suele esconder más de una responsabilidad.
- **Tu resultado:** **13** clase(s) con baja cohesión
- **Para investigar la causa:**
    - ¿Qué métodos de la clase no comparten estado con el resto?
    - ¿Cambian por motivos distintos según la tarea?
    - ¿Se pensó su responsabilidad antes de escribirla, o se fue agregando?

### 7. Complejidad alta (complejidad) — con señales

- **Qué mide:** Complejidad ciclomática: cantidad de caminos posibles dentro de un método o función (condicionales, bucles, casos).
- **Por qué importa:** Muchos caminos = difícil de seguir y de probar; suelen esconderse casos sin cubrir.
- **Tu resultado:** **11** método(s)/función(es) con complejidad alta
- **Para investigar la causa:**
    - ¿Se agregaron condiciones a medida que aparecían casos nuevos?
    - ¿Se probaron todos los caminos o solo el caso principal?
    - ¿Hay reglas de negocio mezcladas con detalles técnicos?

### 8. Métodos/funciones largos o con muchos parámetros (tamano) — con señales

- **Qué mide:** Tamaño de los métodos/funciones y cantidad de parámetros.
- **Por qué importa:** Una función que hace varias cosas cuesta leerla, nombrarla y reutilizarla.
- **Tu resultado:** **42** método(s)/función(es) largos o con muchos parámetros
- **Para investigar la causa:**
    - ¿La función creció por acumulación de pasos?
    - ¿Agrupa operaciones que podrían describirse por separado?
    - ¿Los parámetros representan una misma idea (un objeto) o cosas sueltas?

### 9. Acoplamiento alto (CBO) (acoplamiento) — con señales

- **Qué mide:** Cuánto depende una clase de otros tipos del proyecto.
- **Por qué importa:** Piezas muy enlazadas hacen que un cambio se propague y que sea difícil reemplazar partes.
- **Tu resultado:** **89** clase(s) muy acoplada(s)
- **Para investigar la causa:**
    - ¿La clase conoce detalles internos de muchas otras?
    - ¿Se puede cambiar una parte sin tocar las demás?
    - ¿Los colaboradores llegan como parámetros o se crean por dentro?

### 10. Herencia profunda (DIT) (herencia) — con señales

- **Qué mide:** Profundidad de la cadena de herencia de una clase.
- **Por qué importa:** Jerarquías muy profundas vuelven difícil saber de dónde viene cada comportamiento.
- **Tu resultado:** **6** clase(s) con herencia profunda
- **Para investigar la causa:**
    - ¿La jerarquía creció para reutilizar código o para modelar una relación real 'es-un'?
    - ¿Qué comportamiento cambia entre niveles de la jerarquía?
    - ¿Se evaluó la composición como alternativa?

### 11. Switch sobre tipos (OCP) (ocp) — con señales

- **Qué mide:** Chequeos repetidos de tipo (`instanceof`/`is`/`typeof`) que equivalen a un switch sobre el tipo.
- **Por qué importa:** Cada tipo nuevo obliga a tocar varios lugares, en vez de extender con una implementación nueva.
- **Tu resultado:** **2** clase(s) con switch sobre tipos
- **Para investigar la causa:**
    - ¿Qué operación se resuelve preguntando el tipo?
    - ¿Qué se toca cada vez que aparece un tipo o caso nuevo?
    - ¿El comportamiento podría vivir en la propia jerarquía de tipos?

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
- **Tu resultado:** **6** override(s) vacío(s) o 'no implementado'
- **Para investigar la causa:**
    - ¿Por qué ese método no tiene comportamiento en esa clase?
    - ¿La clase hereda algo que en realidad no le corresponde?
    - ¿Qué contrato se esperaba cumplir al heredar?

### 14. Dependencia de concretos y falta de abstracciones (DIP) (dip) — con señales

- **Qué mide:** Instanciación directa de clases concretas y ausencia de interfaces o clases abstractas pese al acoplamiento.
- **Por qué importa:** Depender de implementaciones concretas dificulta cambiar o probar una parte sin arrastrar las demás.
- **Tu resultado:** **53** dependencia(s) de concretos sin abstracciones
- **Para investigar la causa:**
    - ¿La clase crea por dentro a sus colaboradores en vez de recibirlos?
    - ¿Hay puntos de variación que convendría expresar como abstracción?
    - ¿Cómo se probaría esa clase cambiando una implementación?

### 15. Clase sin métodos (solo datos) (data) — con señales

- **Qué mide:** Clases que solo acumulan campos y no tienen comportamiento.
- **Por qué importa:** Separar datos de comportamiento reparte por el proyecto la lógica que debería estar cerca de esos datos.
- **Tu resultado:** **24** clase(s) sin métodos (solo datos)
- **Para investigar la causa:**
    - ¿Dónde vive el comportamiento que opera sobre esos datos?
    - ¿La clase representa un concepto del dominio o un contenedor técnico?
    - ¿Se evita la lógica de negocio dentro de la clase por comodidad?

### 16. Campos públicos (encapsulación) (encapsulacion) — con señales

- **Qué mide:** Clases que exponen campos sin control de acceso.
- **Por qué importa:** Expone el estado interno y hace que cualquier cambio dependa de quién lo modifica desde afuera.
- **Tu resultado:** **56** clase(s) con campos públicos
- **Para investigar la causa:**
    - ¿Se accede al campo desde otras clases para evitar un método?
    - ¿Qué reglas deberían respetarse al modificar ese estado?
    - ¿Los campos públicos se usan más como configuración que como estado?

### 17. Clase solo con métodos estáticos (utilitaria) (static) — sin señales

- **Qué mide:** Clases donde todos los métodos son estáticos.
- **Por qué importa:** Es comportamiento procedural disfrazado de clase: no se instancia ni participa del modelo de objetos.
- **Tu resultado:** **0** clase(s) solo con métodos estáticos

## 4. Anexo: dónde mirar

Archivos donde el análisis encontró cada señal (sin número de línea, a propósito):

**Clase demasiado grande (SRP) (god)** — 29 archivo(s):
- `app/src/main/kotlin/gravityquest/GestorProgreso.kt`
- `app/src/main/kotlin/gravityquest/NavegadorEscenas.kt`
- `app/src/main/kotlin/gravityquest/VistaJuego.kt`
- `app/src/main/kotlin/gravityquest/VistaMenu.kt`
- `app/src/main/kotlin/gravityquest/models/GeneradorProblemas.kt`
- `app/src/main/kotlin/gravityquest/models/Problema.kt`
- `app/src/main/kotlin/org/example/Juego/Formulas.kt`
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `app/src/test/kotlin/gravityquest/CalculadoraFisicaTest.kt`
- `app/src/test/kotlin/gravityquest/GestorProgresoTest.kt`
- `app/src/test/kotlin/gravityquest/MainTest.kt`
- `app/src/test/kotlin/gravityquest/ModelosTest.kt`
- `app/src/test/kotlin/gravityquest/NavegacionTest.kt`
- `src/Formulas.kt`
- `src/GestorProgreso.kt`
- `src/NavegadorEscenas.kt`
- `src/VistaJuego.kt`
- `src/VistaMenu.kt`
- `src/main.kt`
- `src/models/GeneradorProblemas.kt`
- `src/models/Problema.kt`
- `src_(codigo)/Formulas.kt`
- `src_(codigo)/GestorProgreso.kt`
- `src_(codigo)/NavegadorEscenas.kt`
- `src_(codigo)/VistaJuego.kt`
- `src_(codigo)/VistaMenu.kt`
- `src_(codigo)/main.kt`
- `src_(codigo)/models/GeneradorProblemas.kt`
- `src_(codigo)/models/Problema.kt`

**Baja cohesión (LCOM) (cohesion)** — 13 archivo(s):
- `app/src/main/kotlin/gravityquest/GestorProgreso.kt`
- `app/src/main/kotlin/gravityquest/MarcadorPuntosView.kt`
- `app/src/main/kotlin/gravityquest/VistaJuego.kt`
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `app/src/test/kotlin/gravityquest/NavegacionTest.kt`
- `src/GestorProgreso.kt`
- `src/MarcadorPuntosView.kt`
- `src/VistaJuego.kt`
- `src/main.kt`
- `src_(codigo)/GestorProgreso.kt`
- `src_(codigo)/MarcadorPuntosView.kt`
- `src_(codigo)/VistaJuego.kt`
- `src_(codigo)/main.kt`

**Complejidad alta (complejidad)** — 8 archivo(s):
- `app/src/main/kotlin/gravityquest/VentanaPista.kt`
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `app/src/test/kotlin/gravityquest/GestorProgresoTest.kt`
- `app/src/test/kotlin/gravityquest/NavegacionTest.kt`
- `src/VentanaPista.kt`
- `src/main.kt`
- `src_(codigo)/VentanaPista.kt`
- `src_(codigo)/main.kt`

**Métodos/funciones largos o con muchos parámetros (tamano)** — 15 archivo(s):
- `app/src/main/kotlin/gravityquest/UIComponents.kt`
- `app/src/main/kotlin/gravityquest/VentanaPista.kt`
- `app/src/main/kotlin/gravityquest/models/GeneradorProblemas.kt`
- `app/src/main/kotlin/org/example/Juego/Formulas.kt`
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `src/Formulas.kt`
- `src/UIComponents.kt`
- `src/VentanaPista.kt`
- `src/main.kt`
- `src/models/GeneradorProblemas.kt`
- `src_(codigo)/Formulas.kt`
- `src_(codigo)/UIComponents.kt`
- `src_(codigo)/VentanaPista.kt`
- `src_(codigo)/main.kt`
- `src_(codigo)/models/GeneradorProblemas.kt`

**Acoplamiento alto (CBO) (acoplamiento)** — 47 archivo(s):
- `app/src/main/kotlin/gravityquest/ControladorSimulacion.kt`
- `app/src/main/kotlin/gravityquest/GestorProgreso.kt`
- `app/src/main/kotlin/gravityquest/MarcadorPuntosView.kt`
- `app/src/main/kotlin/gravityquest/NavegadorEscenas.kt`
- `app/src/main/kotlin/gravityquest/PhysicsConstants.kt`
- `app/src/main/kotlin/gravityquest/UIComponents.kt`
- `app/src/main/kotlin/gravityquest/VentanaPista.kt`
- `app/src/main/kotlin/gravityquest/VistaJuego.kt`
- `app/src/main/kotlin/gravityquest/VistaMenu.kt`
- `app/src/main/kotlin/gravityquest/models/Dificultad.kt`
- `app/src/main/kotlin/gravityquest/models/GeneradorProblemas.kt`
- `app/src/main/kotlin/gravityquest/models/Problema.kt`
- `app/src/main/kotlin/org/example/Juego/Formulas.kt`
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `app/src/test/kotlin/gravityquest/CalculadoraFisicaTest.kt`
- `app/src/test/kotlin/gravityquest/GestorProgresoTest.kt`
- `app/src/test/kotlin/gravityquest/MainTest.kt`
- `app/src/test/kotlin/gravityquest/ModelosTest.kt`
- `app/src/test/kotlin/gravityquest/NavegacionTest.kt`
- `src/ControladorSimulacion.kt`
- `src/Formulas.kt`
- `src/GestorProgreso.kt`
- `src/MarcadorPuntosView.kt`
- `src/NavegadorEscenas.kt`
- `src/PhysicsConstants.kt`
- `src/UIComponents.kt`
- `src/VentanaPista.kt`
- `src/VistaJuego.kt`
- `src/VistaMenu.kt`
- `src/main.kt`
- `src/models/Dificultad.kt`
- `src/models/GeneradorProblemas.kt`
- `src/models/Problema.kt`
- `src_(codigo)/ControladorSimulacion.kt`
- `src_(codigo)/Formulas.kt`
- `src_(codigo)/GestorProgreso.kt`
- `src_(codigo)/MarcadorPuntosView.kt`
- `src_(codigo)/NavegadorEscenas.kt`
- `src_(codigo)/PhysicsConstants.kt`
- `src_(codigo)/UIComponents.kt`
- `src_(codigo)/VentanaPista.kt`
- `src_(codigo)/VistaJuego.kt`
- `src_(codigo)/VistaMenu.kt`
- `src_(codigo)/main.kt`
- `src_(codigo)/models/Dificultad.kt`
- `src_(codigo)/models/GeneradorProblemas.kt`
- `src_(codigo)/models/Problema.kt`

**Herencia profunda (DIT) (herencia)** — 3 archivo(s):
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `src/main.kt`
- `src_(codigo)/main.kt`

**Switch sobre tipos (OCP) (ocp)** — 2 archivo(s):
- `app/src/test/kotlin/gravityquest/MainTest.kt`
- `app/src/test/kotlin/gravityquest/NavegacionTest.kt`

**Interfaz con demasiados métodos (ISP) (isp)** — 6 archivo(s):
- `app/src/main/kotlin/gravityquest/GestorProgreso.kt`
- `app/src/main/kotlin/org/example/Juego/Formulas.kt`
- `src/Formulas.kt`
- `src/GestorProgreso.kt`
- `src_(codigo)/Formulas.kt`
- `src_(codigo)/GestorProgreso.kt`

**Override vacío o 'no implementado' (LSP) (lsp)** — 3 archivo(s):
- `app/src/main/kotlin/org/example/Juego/Formulas.kt`
- `src/Formulas.kt`
- `src_(codigo)/Formulas.kt`

**Dependencia de concretos y falta de abstracciones (DIP) (dip)** — 41 archivo(s):
- `app/src/main/kotlin/gravityquest/GestorProgreso.kt`
- `app/src/main/kotlin/gravityquest/MarcadorPuntosView.kt`
- `app/src/main/kotlin/gravityquest/NavegadorEscenas.kt`
- `app/src/main/kotlin/gravityquest/UIComponents.kt`
- `app/src/main/kotlin/gravityquest/VentanaPista.kt`
- `app/src/main/kotlin/gravityquest/VistaJuego.kt`
- `app/src/main/kotlin/gravityquest/VistaMenu.kt`
- `app/src/main/kotlin/gravityquest/models/Dificultad.kt`
- `app/src/main/kotlin/gravityquest/models/GeneradorProblemas.kt`
- `app/src/main/kotlin/gravityquest/models/Problema.kt`
- `app/src/main/kotlin/org/example/Juego/Formulas.kt`
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `app/src/test/kotlin/gravityquest/CalculadoraFisicaTest.kt`
- `app/src/test/kotlin/gravityquest/GestorProgresoTest.kt`
- `app/src/test/kotlin/gravityquest/MainTest.kt`
- `app/src/test/kotlin/gravityquest/ModelosTest.kt`
- `app/src/test/kotlin/gravityquest/NavegacionTest.kt`
- `src/Formulas.kt`
- `src/GestorProgreso.kt`
- `src/MarcadorPuntosView.kt`
- `src/NavegadorEscenas.kt`
- `src/UIComponents.kt`
- `src/VentanaPista.kt`
- `src/VistaJuego.kt`
- `src/VistaMenu.kt`
- `src/main.kt`
- `src/models/Dificultad.kt`
- `src/models/GeneradorProblemas.kt`
- `src/models/Problema.kt`
- `src_(codigo)/Formulas.kt`
- `src_(codigo)/GestorProgreso.kt`
- `src_(codigo)/MarcadorPuntosView.kt`
- `src_(codigo)/NavegadorEscenas.kt`
- `src_(codigo)/UIComponents.kt`
- `src_(codigo)/VentanaPista.kt`
- `src_(codigo)/VistaJuego.kt`
- `src_(codigo)/VistaMenu.kt`
- `src_(codigo)/main.kt`
- `src_(codigo)/models/Dificultad.kt`
- `src_(codigo)/models/GeneradorProblemas.kt`
- `src_(codigo)/models/Problema.kt`

**Clase sin métodos (solo datos) (data)** — 12 archivo(s):
- `app/src/main/kotlin/gravityquest/ControladorSimulacion.kt`
- `app/src/main/kotlin/gravityquest/PhysicsConstants.kt`
- `app/src/main/kotlin/gravityquest/models/Problema.kt`
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `src/ControladorSimulacion.kt`
- `src/PhysicsConstants.kt`
- `src/main.kt`
- `src/models/Problema.kt`
- `src_(codigo)/ControladorSimulacion.kt`
- `src_(codigo)/PhysicsConstants.kt`
- `src_(codigo)/main.kt`
- `src_(codigo)/models/Problema.kt`

**Campos públicos (encapsulación) (encapsulacion)** — 31 archivo(s):
- `app/src/main/kotlin/gravityquest/ControladorSimulacion.kt`
- `app/src/main/kotlin/gravityquest/GestorProgreso.kt`
- `app/src/main/kotlin/gravityquest/MarcadorPuntosView.kt`
- `app/src/main/kotlin/gravityquest/NavegadorEscenas.kt`
- `app/src/main/kotlin/gravityquest/PhysicsConstants.kt`
- `app/src/main/kotlin/gravityquest/VistaJuego.kt`
- `app/src/main/kotlin/gravityquest/VistaMenu.kt`
- `app/src/main/kotlin/gravityquest/models/Dificultad.kt`
- `app/src/main/kotlin/gravityquest/models/Problema.kt`
- `app/src/main/kotlin/org/example/Juego/Main.kt`
- `app/src/test/kotlin/gravityquest/NavegacionTest.kt`
- `src/ControladorSimulacion.kt`
- `src/GestorProgreso.kt`
- `src/MarcadorPuntosView.kt`
- `src/NavegadorEscenas.kt`
- `src/PhysicsConstants.kt`
- `src/VistaJuego.kt`
- `src/VistaMenu.kt`
- `src/main.kt`
- `src/models/Dificultad.kt`
- `src/models/Problema.kt`
- `src_(codigo)/ControladorSimulacion.kt`
- `src_(codigo)/GestorProgreso.kt`
- `src_(codigo)/MarcadorPuntosView.kt`
- `src_(codigo)/NavegadorEscenas.kt`
- `src_(codigo)/PhysicsConstants.kt`
- `src_(codigo)/VistaJuego.kt`
- `src_(codigo)/VistaMenu.kt`
- `src_(codigo)/main.kt`
- `src_(codigo)/models/Dificultad.kt`
- `src_(codigo)/models/Problema.kt`

## 5. Evolución por commit

Métricas calculadas sobre el estado de `main` en **cada commit** (16 commits con trabajo), del primero al último. La calidad usa el perfil `exigente`; el aporte de código lo mide RepoSense. El índice puede **bajar** al crecer el proyecto: se acumulan señales.

```mermaid
xychart-beta
    title "Índice POO/SOLID a lo largo de los commits"
    x-axis ["052883a", "f27feb0", "52dd827", "9a37451", "6f47bc8", "9f0b68b", "42dbe3e", "6cb23a6", "f662fcc", "967a698", "8501235", "62f0ff0", "aaddf8e", "652075e", "1e2658f", "a3a2de3"]
    y-axis "Índice" 0 --> 100
    line [0, 0, 38, 38, 36, 36, 7, 7, 6, 4, 4, 2, 2, 1, 1, 1]
```

| # | Fecha | Commit | Autor | LOC | Índice | %POO | Duplic. | Sueltas |
|---|---|---|---|---:|---:|---:|---:|---:|
| 1 | 2026-08-21 | `052883a` | ciro | 0 | 0 | 0% | 0.0% | 0 |
| 2 | 2026-08-24 | `f27feb0` | dqmdz-etec | 0 | 0 | 0% | 0.0% | 0 |
| 3 | 2026-08-28 | `52dd827` | ciro | 76 | 38 | 16% | 0.0% | 1 |
| 4 | 2026-08-28 | `9a37451` | ciro | 76 | 38 | 16% | 0.0% | 1 |
| 5 | 2026-09-04 | `6f47bc8` | ciro | 135 | 36 | 22% | 0.0% | 2 |
| 6 | 2026-09-07 | `9f0b68b` | ciro | 159 | 36 | 26% | 0.0% | 2 |
| 7 | 2026-10-02 | `42dbe3e` | ciro | 326 | 7 | 32% | 53.5% | 3 |
| 8 | 2026-10-05 | `6cb23a6` | ciro | 467 | 7 | 63% | 38.8% | 3 |
| 9 | 2026-10-05 | `f662fcc` | ciro | 523 | 6 | 66% | 40.9% | 3 |
| 10 | 2026-10-05 | `967a698` | ciro | 1,336 | 4 | 78% | 64.4% | 3 |
| 11 | 2026-10-09 | `8501235` | ciro | 1,640 | 4 | 76% | 62.1% | 1 |
| 12 | 2026-10-09 | `62f0ff0` | ciro | 3,354 | 2 | 78% | 67.9% | 3 |
| 13 | 2026-10-09 | `aaddf8e` | ciro | 4,142 | 2 | 73% | 65.1% | 5 |
| 14 | 2026-10-16 | `652075e` | ciro | 6,182 | 1 | 75% | 59.9% | 7 |
| 15 | 2026-10-19 | `1e2658f` | ciro | 7,932 | 1 | 76% | 67.9% | 28 |
| 16 | 2026-10-26 | `a3a2de3` | ciro | 10,376 | 1 | 83% | 69.0% | 15 |

---
_Informe generado por la cátedra. Los criterios y umbrales de medición corresponden al perfil `exigente` de `calidad.py`; el semáforo de aprobación, a `CRITERIOS-CALIDAD.md` (ver `README.md`). Ante dudas, revisá el archivo señalado._
<!-- evaluacion:fin -->
