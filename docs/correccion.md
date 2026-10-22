# Taller 2. Un robot en la cuadrícula - Informe de corrección

Asignatura de Fundamentos de Programación Funcional y Concurrente.
Documento realizado por estudiantes

## Argumentar la corrección de programas recursivos

Sea $f : A \to B$ una función, y $A$ un conjunto definido recursivamente. Sea $P_f$ un programa recursivo desarrollado
en Scala hecho para calcular $f$. Argumentar $P_f$ es correcto con respecto a su especificación es demostrar el
siguiente teorema:

$$
\forall a \in A : P_f(a) == f(a)
$$

1. **Caso base:** se prueba la igualdad para cada valor básico de $A$.
2. **Casos de inducción** para cada valor construido a partir de otros se supone la hipótesis de inducción (HI), es decir, que $P_f$ es correcto sobre esos otros valores, y se prueba la igualdad.

Notación: $P(x) \rightarrow e$  indica un paso de evaluación del programa según el modelo de sustitución.

**Tipos usados en el taller.**

* $\text{Orden}$ tiene órdenes básicas $\texttt{Avanzar}(n)$ y $\texttt{Girar}(s)$, y órdenes compuestas $\texttt{Repetir}(k, o)$
  y $\texttt{Secuencia}(\text{List}(o_1, \ldots, o_n))$ (la lista puede estar vacía).
* $\texttt{Estado}$ es $(x, y, \mathit{mira})$: posición y dirección.

## Punto 1. `girar`

### Especificación

Las direcciones se numeran en sentido horario con la función $\text{pos}$:

$$\text{pos}(\text{Norte}) = 0 \qquad \text{pos}(\text{Este}) = 1 \qquad \text{pos}(\text{Sur}) = 2 \qquad \text{pos}(\text{Oeste}) = 3$$

Cada sentido de giro suma un desplazamiento $\delta$ a la posición, módulo 4. Girar a la izquierda equivale a retroceder una posición, es decir, a sumar 3:

$$\delta(\text{Derecha}) = 1 \qquad \delta(\text{Izquierda}) = 3$$

Sea $g$ la función que devuelve la dirección resultante de girar:

$$g(d, s) = \text{pos}^{-1}\big((\text{pos}(d) + \delta(s)) \bmod 4\big)$$

Esta especificación coincide con los ejemplos del enunciado: $g(\text{Norte}, \text{Derecha}) = \text{Este}$ y $g(\text{Oeste},\text{Derecha}) = \text{Norte}$.

Sea $P_{\text{girar}}$ el programa en Scala:

```Scala
  def girar(d: Direccion, s: Sentido): Direccion = (d, s) match {
    case (Norte, Derecha) => Este
    case (Norte, Izquierda) => Oeste
    case (Sur, Derecha) => Oeste
    case (Sur, Izquierda) => Este
    case (Este, Derecha) => Sur
    case (Este, Izquierda) => Norte
    case (Oeste, Derecha) => Norte
    case (Oeste, Izquierda) => Sur
  }
```

Se desea demostrar que:

$$\forall d \in \text{Direccion}, \forall s \in \text{Sentido} : P_{\text{girar}}(d,s) = g(d,s)$$

### Método: análisis de casos

$P_{\text{girar}}$ no es recursiva, así que no hay hipótesis de inducción. Como $\text{Direccion}$ tiene 4 valores y $\text{Sentido}$ tiene 2, el conjunto
$\text{Direccion} \times \text{Sentido}$ tiene 8 elementos y se pueden revisar todos.

El `match` ejecuta el primer `case` cuyo patrón coincide con la pareja $(d,s)$. Los 8 patrones son distintos entre sí y cubren las 8 parejas, porque `Direccion` y `Sentido` son `sealed trait`.
Por eso cada pareja activa exactamente un `case`.

### Verificación de las 8 parejas

| $d$   | $s$       | $\text{pos}(d)$ | $(\text{pos}(d) + \delta(s)) \bmod 4$ | $g(d,s)$ | $P_{\text{girar}}(d,s)$ |
|-------|-----------|-----------------|---------------------------------------|----------|-------------------------|
| Norte | Derecha   | 0               | 1                                     | Este     | Este                    |
| Norte | Izquierda | 0               | 3                                     | Oeste    | Oeste                   |
| Este  | Derecha   | 1               | 2                                     | Sur      | Sur                     |
| Este  | Izquierda | 1               | 0                                     | Norte    | Norte                   |
| Sur   | Derecha   | 2               | 3                                     | Oeste    | Oeste                   |
| Sur   | Izquierda | 2               | 1                                     | Este     | Este                    |
| Oeste | Derecha   | 3               | 0                                     | Norte    | Norte                   |
| Oeste | Izquierda | 3               | 2                                     | Sur      | Sur                     |

En las 8 parejas $P_{\text{girar}}(d,s) = g(d,s)$.

### Propiedades que se siguen de la especificación

- **Un giro y su contrario se cancelan.** Con $p = \text{pos}(d)$, girar a la derecha y luego a la izquierda da $(p + 1 + 3) \bmod 4 = p$.
  En el otro orden pasa lo mismo.
- **Cuatro giros al mismo lado vuelven a la dirección inicial.** Como $4\, \delta(s)$ es múltiplo de 4, $(p + 4\,\delta(s)) \bmod 4 = p$.

### Conclusión

Las 8 parejas de $\text{Direccion} \times \text{Sentido}$ cumplen $P_{\text{girar}}(d,s) = g(d,s)$. Por lo tanto, `girar` es correcta respecto a su especificación.

## Punto 2. Aplicación del orden a los estados

### Especificación
La función `ejecutar` recibe el estado actual y una orden. Su objetivo es devolver un nuevo estado después de realizar la instrucción, sin que esta modifique su estado
original.

El estado del robot se ve representado mediante: $e = (x, y, d)$
- $x$ y $y$: Representan su posición
- $d$: Es la dirección hacia la que mira

Sea $E(e, o)$ la función que representa el resultado esperado de ejecutar una orden
- $o$: (Orden) La instrucción que ejecuta el robot
- $e$: Estado actual del robot (posición y dirección)

Para `Avanzar(n)` las coordenadas cambian dependiendo de la dirección:

$E((x, y, d), Avanzar(n)) = $
- $(x, y + n, d)$ si $d = \text{Norte}$
- $(x, y - n, d)$ si $d = \text{Sur}$
- $(x + n, y, d)$ si $d = \text{Este}$
- $(x - n, y, d)$ si $d = \text{Oeste}$

En donde:
- $E$: Es la función que calcula el nuevo estado después de realizar una acción
- $n$: Cantidad de unidades que va a avanzar

Para la orden `Girar(sentido)`, las coordenadas permanecen iguales y únicamente cambian la dirección:
$E((x, y, d), Girar(s)) = (x, y, g(d, s))$

- `s`: Sentido del giro (derecha o izquierda)

La función $g(d, s)$ representa la función `girar` que se explicó en el punto 1. Como tal
permite saber hacia qué dirección queda mirando el robot después de girar a la derecha o a la izquierda

Cuando una orden contiene otras órdenes, el robot las ejecuta una por una, siguiendo el orden establecido.
Esto se expresa en $E$ para `Repetir` y `Secuencia`:

$E(e, \texttt{Repetir}(k, o)) =$
- $e$ si $k \leq 0$
- $E(E(e, o), \texttt{Repetir}(k - 1, o))$ si $k > 0$

$E(e, \texttt{Secuencia}(\text{List}(o_1, \ldots, o_n))) =$
- $e$ si $n = 0$
- $E(E(e, o_1), \texttt{Secuencia}(\text{List}(o_2, \ldots, o_n)))$ si $n \geq 1$

En ambos casos la orden interna se ejecuta sobre el estado que dejó la anterior.

### Algoritmo

Comienza utilizando `o match` para identificar el tipo de orden recibida, según el caso, ejecuta un avance,
realiza un giro, repite una instrucción o recorre una secuencia; permitiendo tratar las 4 alternativas del tipo
`Orden` de manera independiente

```Scala
def ejecutar(e: Estado, o: Orden): Estado = o match {

  case Avanzar(n) =>
    e.mira match {
      case Norte => e.copy(y = e.y + n)
      case Sur   => e.copy(y = e.y - n)
      case Este  => e.copy(x = e.x + n)
      case Oeste => e.copy(x = e.x - n)
    }

  case Girar(sentido) =>
    e.copy(mira = girar(e.mira, sentido))

  case Repetir(veces, orden) =>
    @annotation.tailrec
    def repetir(restantes: Int, actual: Estado): Estado =
      restantes match {
        case r if r <= 0 => actual
        case _           => repetir(restantes - 1, ejecutar(actual, orden))
      }

    repetir(veces, e)

  case Secuencia(ordenes) =>
    @annotation.tailrec
    def recorre(
        pendientes: List[Orden],
        actual: Estado
    ): Estado =
      pendientes match {
        case Nil =>
          actual

        case x :: xs =>
          recorre(xs, ejecutar(actual, x))
      }

    recorre(ordenes, e)
}
```
- `@`: Indica que estamos usando una anotación
- `@annotation.tailrec`: Le indica a Scala donde encontrar esta anotación para evitar el `import`
- `annotation`: Es un paquete de Scala que contiene anotaciones utilizadas para darle indicaciones al compilador
- `tailrec`: Anotación que verifica que una función use recursion de cola, es decir que la llamada recursiva sea
  su última opción.

Sea $P(e, o)$ el resultado que devuelve el programa, porque se quiere demostrar que:
$$\forall e\ \in\ Estado, \forall o\ \in\ Orden : P(e, o) = E(e, o)$$

"Para cualquier estado del robot y cualquier orden, el resultado que produce el
programa debe ser igual al resultado esperado"

- $P(e, o)$: Resultado que produce el programa
- $E(e, o)$: Resultado esperado según las reglas del ejercicio

### Método: Inducción Estructural
Para comprobar que la función `ejecutar` funciona correctamente, se revisan los 4 tipos de órdenes que puede recibir el robot: `Avanzar, Girar, Repetir y Secuencia`.

Primero se analizará `Avanzar y Girar`, porque son órdenes sencillas que el robot ejecuta directamente.
Después se revisará `Repetir y Secuencia`, que contienen otras órdenes.

Para estos casos, partimos de que las órdenes internas $o$ funcionen bien, y se comprueba que $P(e,o)=E(e,o)$ al repetirlas o ejecutarlas una tras otra, el robot llega al estado esperado

### Caso base

#### Caso 1: Avanzar(n)
El programa usa `match` sobre $e.mira$ para determinar qué coordenadas deben modificar,
si el robot mira al norte, devuelve:
- `match`: Sirve para comprobar un valor y decidir qué acción se realiza según el caso
- $e.mira$: Permite consultar la dirección hacia la que se está mirando

```Scala
e.copy(y = e.y + n)
```
- `.copy`: Crea una copia del estado a partir del anterior, cambiando solo los valores que se le indiquen,
  sin modificar el original
- `y = `: Indica que se cambia la coordenada vertical
- `e.y`: Valor actual de la coordenada y
- `+ n`: Suma la cantidad de pasos que se debe avanzar

Ejemplo:
Si tenemos
```Scala
val e = Estado(2, 3, Norte)
val n = 4
```
Al ejecutar $e.copy(y = e.y + n)$ se obtiene:
```Scala
Estado(2, 7, Norte)
```

Por lo tanto: $P((x, y, Norte), Avanzar(n)) =  (x, y + n, Norte)$

Ejemplo:
Si el robot está en (2,3), mirando al norte y avanza 4 unidades entonces:

```Scala
P((2, 3, Norte), Avanzar(4)) =  (2, 7, Norte)
```
Este resultado coincide con la especificación $E$

Los casos `Sur, Este y Oeste` siguen el mismo razonamiento, modificando la coordenada correspondiente.

#### Caso especial: Avanzar con pasos negativos
La función `ejecutar` permite que el robot retroceda cuando recibe un número negativo de pasos.
No es necesario agregar otra condición al código, porque las mismas operaciones de suma y resta permiten calcular
el movimiento.

Cuando el robot está mirando al norte se ejecuta
```Scala
case Norte => e.copy(y = e.y + n)
```
Si $n$ es positivo, la coordenada $y$ aumenta y este avanza al Norte.
Si $n$ es negativo, al sumarlo la coordenada disminuye, haciendo que retroceda.

Ejemplo:
```Scala
val e = Estado(2, 3, Norte)
val n = -2
```
Si lo ejecutamos, se realiza la operación
```Scala
y = 3 + (-2) 
y = 1
```
Dando como resultado:
```Scala
Estado(2, 1, Norte)
```

Retrocediendo 2 unidades, pasando de la posición (2, 3) a (2, 1); sin embargo, continúa mirando al norte.

Ocurriendo lo mismo con las demás direcciones
- Cuando mira al Sur u Oeste, restar con un número negativo hace que la coordenada aumente
- Cuando mira al Este o Norte, sumar un número negativo hace que disminuya la coordenada correspondiente

De esta manera el programa permite avanzar y retroceder utilizando las mismas operaciones sin necesidad
de cambiar la dirección.

#### Caso 2: Girar(Sentido)
El programa ejecuta
```Scala
e.copy(mira = girar(e.mira, sentido))
```
Como `copy` conserva las coordenadas que no se modifican, solamente cambia la dirección,
por lo cual:
$P((x, y, d), Girar(s)) =  (x, y, g (d, s))$
- `Girar(s)`: Orden de girar según el sentido

Ejemplo: Si se está en (4, 7, Norte) y gira a la derecha:
```Scala
P((4, 7, Norte), Girar(Derecha)) =  (4, 7, Este)
```
Observándose que las coordenadas $x = 4$ y $y = 7$ permanecen iguales. Solamente cambia la dirección de
`Norte` a `Este`

Como la corrección de `girar` se demostró en el punto $1$, el resultado coincide con la especificación

### Caso Recursivo: Repetir
La orden `Repetir(veces, orden)` permite ejecutar una misma instrucción varias veces.
Para justificar este caso, partimos en un caso en donde no hay repeticiones y después analizamos que sucede cuando
se agrega una repetición más.
- `repetir(veces, e)`: Inicia la función auxiliar, se ejecuta hasta completar todas las repeticiones
- `veces`: Cuántas repeticiones deben realizarse
- $e$: Estado inicial del robot

Matemáticamente, se podría expresar como: $R(k, e, o)$.
Sea $R$ el estado obtenido después de ejecutar una orden $o$ exactamente $k$ veces, comenzando desde el estado $e$,
donde $k \geq 0$.

$R(0, e, o) = e$ Si no hay repeticiones entonces se conserva el estado

$R(k + 1, e, o) = R(k, E(e, o), o)$ Primero se ejecuta una orden y luego se continúa con las repeticiones pendientes

#### Caso base: Cero o menos repeticiones
Cuando la cantidad de repeticiones es 0 o negativa, el robot no debe hacer ningún movimiento o giro
Para controlar esta situación, la función auxiliar `repetir` utiliza la siguiente condición

```Scala
case r if r <= 0 => actual
```
Este comportamiento se expresa matemáticamente así: $P(e, \texttt{Repetir}(v, o)) = e, v \leq 0$.
- $v$: Cantidad de veces que se repite una orden

- `restantes`: Indica cuántas veces faltan ejecutar la orden

La condición comprueba si todavía queda repeticiones por realizar. Si `restantes` es 0 o negativo, la función devuelve
directamente el estado `actual` sin ejecutar una orden.

Ejemplo:

```Scala
val e = Estado(3, 2, Norte)
val orden = Repetir(0, Avanzar(5)) 
```
Entonces cuando se llama a `ejecutar(e, orden)` la función auxiliar recibe 0 repeticiones
y junto con el estado (3, 2, Norte)
Como se cumple `restantes <= 0`. entonces devuelve el mismo estado

```Scala
Estado(3, 2, Norte)
```
Significando que permanecerá en la misma posición y mirando aún al `Norte`

Y si la cantidad de repeticiones fuera negativa (ejemplo: `Repetir(-2, Avanzar(5))` ), ocurriría lo mismo;
cumpliendo la condición y el robot conservaría el estado.

Evidenciándose que cuando la cantidad de repeticiones es 0 o <0 (menor que cero), el resultado del programa coincide
con lo establecido en la especificación "No se ejecuta ninguna orden y se devuelve el estado recibido".

#### Caso recursivo: $k + 1$ Repeticiones pendientes
Para comprobar que la función `repetir` esté funcionando correctamente, comenzamos suponiendo que puede
realizar correctamente $k$ repeticiones
- $k$: Cantidad de repeticiones

Cuando quedan $k + 1$ repeticiones, primero realiza la orden sobre el estado actual y luego llama a
la función `repetir`, disminuyendo la cantidad de repeticiones pendientes.

Lo anterior se realiza mediante la función:
```Scala
repetir(restantes - 1, ejecutar(actual, orden))
```
- `ejecutar(actual, orden)`: Ejecuta 1 vez la orden y obtiene el estado; el resultado de esta se usa para
  continuar con las otras repeticiones
- `restantes - 1`: Disminuye la cantidad de repeticiones pendientes, porque se ejecutó una de estas.

Ejemplo: Si queremos ejecutar una orden 3 veces
```Scala
val e = Estado(1, 2, Este)
val orden = Repetir(3, Avanzar(2)) 
```
Resultado de las repeticiones

| Repetición | Operación en $x$ | Estado obtenido | Pendientes |
|------------|------------------|-----------------|------------|
| Inicial    | —                | (1, 2, Este)    | 3          |
| Primera    | $1+2=3$          | (3, 2, Este)    | 2          |
| Segunda    | $3+2=5$          | (5, 2, Este)    | 1          |
| Tercera    | $5+2=7$          | (7, 2, Este)    | 0          |

Siendo el resultado final: $Estado(7, 2, Este)$

Y se expresa matemáticamente como: $R(k + 1, e, o) = R(k, E(e, o), o)$

| Repetición      | Operación matemática         | Estado obtenido | Pendientes |
|-----------------|------------------------------|-----------------|------------|
| Inicial         | $R(3,e_0,o)$                 | (1, 2, Este)    | 3          |
| Primera         | $R(3,e_0,o)=R(2,E(e_0,o),o)$ | (3, 2, Este)    | 2          |
| Segunda         | $R(2,e_1,o)=R(1,E(e_1,o),o)$ | (5, 2, Este)    | 1          |
| Tercera         | $R(1,e_2,o)=R(0,E(e_2,o),o)$ | (7, 2, Este)    | 0          |
| Resultado final | $R(0,e_3,o)=e_3$             | (7, 2, Este)    | 0          |

Donde $e_0$ es el estado inicial y $e_1$, $e_2$ y $e_3$ son los estados obtenidos después de cada repetición

Quedando demostrado que `repetir` ejecuta una orden las veces indicadas, utilizando el resultado anterior
para continuar hasta el resultado esperado

### Caso recursivo: Secuencia

#### Caso base: Lista vacía
La orden `Secuencia(ordenes)` permite ejecutar una lista de instrucciones en el orden establecido
Esta comienza llamando a
```Scala
recorre(ordenes, e)
```

Cuando la lista es `Nil`, no quedan órdenes por ejecutar. Por eso, el programa devuelve el estado actual:

```Scala
case Nil => actual
```
Ejemplo: Cuando el robot se encuentre en la posición (2, 3), mirando hacia el `Norte`, y recibe una lista vacía
```Scala
val e = Estado(2, 3, Norte)
val orden = Secuencia(Nil)
ejecutar(e, orden)
```
Como la lista no tiene instrucciones entonces el estado se conserva
```Scala
Estado(2, 3, Norte)
```

Expresándose matemáticamente:
$P(e, Secuencia(Nil)) = e$

Ejemplo:
$P((2, 3, Norte), Secuencia(Nil)) = (2, 3, Norte)$

Quedando demostrado que, cuando no hay órdenes por ejecutar, el robot permanece en la misma posición y dirección.

#### Caso recursivo: Lista con elementos
Para mirar si funciona correctamente, se supone que se puede ejecutar una lista de $k$ órdenes
Si tenemos $k + 1$ órdenes, el programa separa la primera instrucción $x$ de las restantes $xs$
```Scala
case x :: xs => recorre(xs, ejecutar(actual, x))
```
- `ejecutar(actual, x)`: Ejecuta la primera orden y obtiene el nuevo estado del robot
- `recorre(xs, ejecutar(actual, x))`: Continúa ejecutando las órdenes restantes usando el nuevo estado

Ejemplo: Si empieza en (2, 3), mirando hacia el `Norte` y recibe 2 instrucciones

```Scala
val e = Estado(2, 3, Norte)
val orden = Secuencia(List(Avanzar(2), Girar(Derecha)))
ejecutar(e, orden)
```

Dando como resultado final $Estado(2, 5, Este)$

Y en expresión matemática es: $E(e, Secuencia([o_1, o_2])) = E(E(e, o_1), o_2)$
- $e$: Estado inicial
- $o_1$: Primera orden
- $o_2$: Segunda orden
- $E(e, o_1)$: Estado obtenido después de ejecutar la primera orden
- $E(E(e, o_1), o_2)$: Estado obtenido después de ejecutar la segunda orden


Resultado de la secuencia:

| Paso            | Operación realizada                         | Estado obtenido | Órdenes pendientes |
|-----------------|---------------------------------------------|-----------------|--------------------|
| Inicial         | $E(e,Secuencia([o_1,o_2]))$                 | (2, 3, Norte)   | 2                  |
| Primera         | $E(e,o_1)=(2,3+2,Norte)$                    | (2, 5, Norte)   | 1                  |
| Segunda         | $E(E(e,o_1),o_2)=(2,5,Este)$                | (2, 5, Este)    | 0                  |
| Resultado final | $E(e,Secuencia([o_1,o_2]))=E(E(e,o_1),o_2)$ | (2, 5, Este)    | 0                  |

Por lo tanto, `Secuencia` ejecuta las instrucciones en el orden indicado, utilizando el resultado para continuar
con la siguiente. Coincidiendo con el comportamiento esperado

### Terminación
La función `ejecutar` termina porque cada vez que se hace una llamada recursiva, se acerca a una condición que permite
detener el proceso llamado como "caso base".

#### En repetir
Como se explicaba anteriormente que la cantidad de repeticiones pendientes disminuye hasta llegar a 0,
teniendo en cuenta que si la cantidad inicial es 0 o negativa, la función termina inmediatamente

Controlándose con:

```Scala
case r if r <= 0 => actual
```
Ejemplo: Supongamos que el robot comienza en (2, 3, Norte) y debe avanzar 2 unidades, repitiendo esa orden 2 veces
```Scala
val estado = Estado(2, 3, Norte)
val orden = Repetir(2, Avanzar(2))
ejecutar(estado, orden) 
```

| Paso            | Instrucción ejecutada | Estado obtenido | Repeticiones pendientes |
|-----------------|-----------------------|-----------------|-------------------------|
| Inicial         | —                     | (2, 3, Norte)   | 2                       |
| Primera         | `Avanzar(2)`          | (2, 5, Norte)   | 1                       |
| Segunda         | `Avanzar(2)`          | (2, 7, Norte)   | 0                       |
| Resultado final | —                     | (2, 7, Norte)   | 0                       |

Las repeticiones pendientes disminuyen así:
$2 \rightarrow 1 \rightarrow 0$

Cuando llegue a 0 devuelve el último estado, y no se ejecuta más, comprobándose que `Repetir` termina cuando completa la
cantidad de repeticiones indicadas.

#### En secuencia
Ejecuta la primera orden de la lista y continúa con las restantes, en cada llamada la lista se hace más
corta hasta llegar a `Nil`

Ejemplo
```Scala
Secuencia(List(Avanzar(2), Girar(Derecha)))
```
Viéndose reflejado como las órdenes disminuyen
```Scala
List(Avanzar(2), Girar(Derecha))
List(Girar(Derecha))
Nil
```

Cuando llega a `Nil` se ejecuta:
```Scala
case Nil => actual
```
Esto indica que ya no quedan más instrucciones y se devuelve el estado actual, además de que `repetir` y `recorre`
usan recursión de cola.
Demostrando que las órdenes, las listas y las repeticiones son finitas, además de que la función
termina después de completar las instrucciones.

### Comprobación del resultado
Para comprobar el funcionamiento de `ejecutar`, se utilizará el siguiente ejemplo:

```Scala
val e = Estado(0, 0, Norte)
val orden = Repetir(2, Secuencia(List(Avanzar(3), Girar(Derecha))))
ejecutar(e, orden)
```
Comienza en la posición (0, 0), mirando hacia el `Norte`, y repite 2 veces una secuencia que avanza 3 unidades
y luego gira a la derecha. Este ejemplo combina `Repetir`, `Secuencia`, `Avanzar` y `Girar`.

Las operaciones realizadas son:

| Repetición | Orden de la secuencia | Operación | Estado obtenido |
|------------|-----------------------|-----------|-----------------|
| Inicial    | —                     | —         | (0, 0, Norte)   |
| Primera    | `Avanzar(3)`          | $0+3=3$ en $y$ | (0, 3, Norte) |
| Primera    | `Girar(Derecha)`      | Norte → Este | (0, 3, Este) |
| Segunda    | `Avanzar(3)`          | $0+3=3$ en $x$ | (3, 3, Este) |
| Segunda    | `Girar(Derecha)`      | Este → Sur   | (3, 3, Sur)  |

En la primera repetición el robot mira al `Norte`, por lo que avanzar cambia $y$; después de girar mira al `Este`,
por lo que en la segunda repetición avanzar cambia $x$.

Por lo tanto, el estado final es:

```Scala
Estado(3, 3, Sur)
```

$P((0, 0, Norte), Repetir(2, Secuencia([Avanzar(3), Girar(Derecha)]))) = (3, 3, Sur)$

El resultado coincide con la especificación $E$: aplicar la secuencia dos veces, cada vez sobre el estado que dejó la
anterior, produce el mismo estado. Demostrando que `ejecutar` combina correctamente las repeticiones y las secuencias.

### Conclusión
Mediante los casos base y los casos de recursión, se explicó por qué la función `ejecutar` funciona correctamente.

Las órdenes `Avanzar` y `Girar` modifican directamente la posición o la dirección del robot.
Por otro lado, `Repetir` y `Secuencia` utilizan recursión para ejecutar las instrucciones en orden correspondiente,
tomando como punto de inicio el estado anterior.

De esto se puede inferir que siempre que los cálculos no superen los límites del tipo $Int$, se cumple
$\forall e \in Estado, \forall o \in Orden: P(e, o) = E (e, o)$
Esta expresión indica que "Para cualquier estado $e$ y orden $o$ válidos, el resultado del programa $P(ejecutar)$
coincide con el comportamiento definido en la especificación $E$"

## Punto 3. `cuantasBasicas` y `profundidad`

### Especificación

$\text{Orden}$ se define recursivamente:

- $\text{Avanzar}(n)$ y $\text{Girar}(s)$ son órdenes (casos básicos).
- Si $o$ es una orden y $v \in \mathbb{Z}$, entonces $\text{Repetir}(v,o)$ es una orden.
- Si $o_1, \ldots, o_n$ son órdenes con $n \geq 0$, entonces $\text{Secuencia}([o_1,\ldots,o_n])$ es una orden.

Sean $B, D: \text{Orden} \to \mathbb{N}$ las funciones que definen qué debe devolver cada programa:

$$B(\text{Avanzar}(n)) = B(\text{Girar}(s)) = 1 \qquad B(\text{Repetir}(v,o)) = \max(v,0)\cdot B(o)$$

$$B(\text{Secuencia}([o_1,\ldots,o_n])) = \sum_{i=1}^n B(o_i)$$

$$D(\text{Avanzar}(n)) = D(\text{Girar}(s)) = 1 \qquad D(\text{Repetir}(v,o)) = 1 + D(o)$$

$$D(\text{Secuencia}([o_1,\ldots,o_n])) = 1 + \max\big(\{0\} \cup \{D(o_i)\}\big)$$

La suma vacía vale 0. Por eso $B(\text{Secuencia}([\,])) = 0$ y $D(\text{Secuencia}([\,])) = 1$.

Sean $P_B$ y $P_D$ los programas en Scala `cuantasBasicas` y `profundidad`:

```Scala
  def cuantasBasicas(o: Orden): Int = o match {
      case Avanzar(_) => 1
      case Girar(_) => 1
      case Repetir(veces, _) if veces <= 0 => 0
      case Repetir(veces, orden) => veces * cuantasBasicas(orden)
      case Secuencia(ordenes) => ordenes match {
        case Nil => 0
        case x :: xs => cuantasBasicas(x) + cuantasBasicas(Secuencia(xs))
      }
  }
  
  def profundidad(o: Orden): Int = o match {
    case Avanzar(_) => 1
    case Girar(_) => 1
    case Repetir(veces, orden) => 1 + profundidad(orden)
    case Secuencia(ordenes) => {
      def maximo(l: List[Orden]): Int = l match {
        case Nil => 0
        case x :: xs => math.max(profundidad(x), maximo(xs))
      }
      1 + maximo(ordenes)
    }
  }
```

Se desea demostrar que, mientras los resultados quepan en un `Int`:

$$\forall o \in \text{Orden} : P_B(o) = B(o) \;\land\; P_D(o) = D(o)$$

### Método

La demostración usa inducción estructural sobre $\text{Orden}$. En el caso $\text{Secuencia}([o_1,\ldots,o_n])$, la hipótesis de inducción
vale para cada $o_i$, porque cada una es parte de la secuencia.

Los programas recorren la lista interna con funciones que no son parte de la orden: `cuantasBasicas` llama a $\text{Secuencia}(xs)$, que no es una parte de $\text{Secuencia}(x::xs)$, y `profundidad`
usa `maximo`. Por eso antes se demuestran dos lemas sobre listas, por inducción sobre su longitud $n$.


### Lema 1 (lista en `cuantasBasicas`)

Si $P_B(o_i) = B(o_i)$ para todo $i$, entonces:

$$P_B(\text{Secuencia}([o_1, \ldots,o_n])) = \sum_{i=1}^{n} B(o_i)$$

**Caso base $n=0$.** El programa ejecuta el caso `Nil`:

$$P_B(\text{Secuencia}([\,])) \rightarrow 0$$

que es la suma vacía.

**Caso inductivo $n=k+1$.** Sea la lista $x :: xs$, donde $xs$ tiene $k$ órdenes:

$$P_B(\text{Secuencia}(x::xs)) \rightarrow P_B(x) + P_B(\text{Secuencia}(xs))$$

Por la hipótesis del lema, $P_B(x) = B(x)$. Las órdenes de $xs$ también la cumplen, así que por la hipótesis de inducción
$P_B(\text{Secuencia}(xs))$ es la suma de $B$ sobre $xs$. Por lo tanto, el resultado es $B(x)$ más esa suma, es decir, la suma de $B$ sobre toda la lista.

### Lema 2 (`maximo`)

Si $P_D(o_i) = D(o_i)$ para todo $i$, entonces:

$$\text{maximo}([o_1,\ldots,o_n]) = \max\big(\{0\} \cup \{D(o_i)\}\big)$$

**Caso base $n=0$.** $\text{maximo}([\,]) \rightarrow 0 = \max(\{0\})$.

**Caso inductivo $n=k+1$.** Sea la lista $x :: xs$:

$$\text{maximo}(x::xs) \rightarrow \max\big(P_D(x),\ \text{maximo}(xs)\big)$$

Por la hipótesis del lema, $P_D(x) = D(x)$. Por la hipótesis de inducción, $\text{maximo}(xs) = \max(\{0\} \cup \{D(o_i) : o_i \in xs\})$. El máximo de $D(x)$ con ese
valor es el máximo de $\{0\}$ y de los $D$ de toda la lista.

### Teorema para `cuantasBasicas`

**Casos base.** $P_B(\text{Avanzar}(n)) \rightarrow 1 = B(\text{Avanzar}(n))$. Lo mismo ocurre con $\text{Girar}(s)$.

**Caso $\text{Repetir}(v,o)$.** La hipótesis de inducción es $P_B(o) = B(o)$.

- Si $v \leq 0$, el programa devuelve $0 = \max(v,0)\cdot B(o)$.
- Si $v > 0$, el programa devuelve $v \cdot P_B(o) = v \cdot B(o) = \max(v,0)\cdot B(o)$.

**Caso $\text{Secuencia}([o_1,\ldots,o_n])$.** Cada $o_i$ cumple $P_B(o_i) = B(o_i)$ por la hipótesis de inducción. Por el Lema 1:

$$P_B(\text{Secuencia}([o_1,\ldots,o_n])) = \sum_{i=1}^{n} B(o_i) = B(\text{Secuencia}([o_1,\ldots,o_n]))$$

### Teorema para `profundidad`

**Casos base.** $P_D(\text{Avanzar}(n)) \rightarrow 1 = D(\text{Avanzar}(n))$. Lo mismo ocurre con $\text{Girar}(s)$.

**Caso $\text{Repetir}(v,o)$.** El programa devuelve $1 + P_D(o)$. El valor $v$ no interviene. Por la hipótesis de inducción:

$$1 + P_D(o) = 1 + D(o) = D(\text{Repetir}(v,o))$$

**Caso $\text{Secuencia}([o_1,\ldots,o_n])$.** El programa devuelve $1 + \text{maximo}([o_1,\ldots,o_n])$. Cada $o_i$ cumple $P_D(o_i) = D(o_i)$ por la hipótesis de inducción. Por el lema 2:

$$1 + \text{maximo}([o_1,\ldots,o_n]) = 1 + \max\big(\{0\} \cup \{D(o_i)\}\big) = D(\text{Secuencia}([o_1,\ldots,o_n]))$$

### Terminación

Cada llamada recursiva se hace sobre una parte de la orden o sobre una lista con un elemento menos. Como una orden tiene un número finito de
partes y toda lista es finita, ambos programas terminan.

### Casos límite

- $B(\text{Secuencia}([\,])) = 0$ y $D(\text{Secuencia}([\,])) = 1$.
- $B(\text{Repetir}(v,o)) = 0$ para $v \leq 0$.
- $D$ no depende de $v$.

### Encadenamiento de los llamados
Sea $\text{cuadrado} = \text{Repetir}(4, \text{Secuencia}([\text{Avanzar}(1), \text{Girar}(\text{Derecha})]))$.

$$
\begin{aligned}
P_B(\text{cuadrado}) &\rightarrow 4 \cdot P_B(\text{Secuencia}([\text{Avanzar}(1), \text{Girar}(\text{Derecha})])) \\
&\rightarrow 4 \cdot \big(P_B(\text{Avanzar}(1)) + P_B(\text{Secuencia}([\text{Girar}(\text{Derecha})]))\big) \\
&\rightarrow 4 \cdot \big(1 + (P_B(\text{Girar}(\text{Derecha})) + P_B(\text{Secuencia}([\,])))\big) \\
&\rightarrow 4 \cdot (1 + (1 + 0)) \rightarrow 8
\end{aligned}
$$

$$
\begin{aligned}
P_D(\text{cuadrado}) &\rightarrow 1 + P_D(\text{Secuencia}([\text{Avanzar}(1), \text{Girar}(\text{Derecha})])) \\
&\rightarrow 1 + \big(1 + \text{maximo}([\text{Avanzar}(1), \text{Girar}(\text{Derecha})])\big) \\
&\rightarrow 1 + \big(1 + \max(1, \max(1, 0))\big) \rightarrow 3
\end{aligned}
$$

Ambos valores coinciden con $B(\text{cuadrado}) = 4 \cdot (1+1) = 8$ y $D(\text{cuadrado}) = 1 + (1 + \max(1,1)) = 3$.

### Conclusión

Se demostró, por inducción estructural sobre $\text{Orden}$ y con los Lemas 1 y 2 para las listas de $\text{Secuencia}$, que:

$$\forall o \in \text{Orden} : P_B(o) = B(o) \;\land\; P_D(o) = D(o)$$

Por lo tanto, `cuantasBasicas` y `profundidad` son correctas respecto a su especificación, siempre que el resultado de `cuantasBasicas` esté dentro del rango de un `Int`.

## Punto 4. `componer`, `rutina`, `aplicarA` y `transformar`

### Función `aplicarA`

#### Especificación

Sea $f_{\mathrm{apl}} : \text{List}[\text{Estado}] \times \text{Orden} \to \text{List}[\text{Estado}]$ la función que ejecuta
la misma orden sobre cada estado de la lista: para $L = \text{List}(e_1, \ldots, e_n)$,

$$
f_{\mathrm{apl}}(L, o) = \text{List}(\texttt{ejecutar}(e_1, o),\ \ldots, \ \texttt{ejecutar}(e_n, o))
$$

#### Programa

Y sea $P_{\mathrm{apl}}$ el siguiente programa en Scala:

```scala
def aplicarA(estados: List[Estado], o: Orden): List[Estado] =
    estados match {
      case Nil =>
          Nil
      case cabeza :: cola =>
          ejecutar(cabeza, o) :: aplicarA(cola, o)
}
```
#### Demostración

Vamos a demostrar que:

$$
\forall n \in \mathbb{N},\ \forall o \in \text{Orden},\ \forall e_1, \ldots, e_n \in \text{Estado} : P_{\mathrm{apl}}(\text{List}
(e_1, \ldots, e_n),\ o) == f_{\mathrm{apl}}(\text{List}(e_1, \ldots, e_n),\ o)
$$

La inducción es sobre $n$, la cantidad de estados. La orden $o$ es la misma en todas las llamadas recursivas, así que la
HI vale para ella.

**Caso base.** Si `estados= Nil`, el programa devuelve `Nil`, la lista vacía no contiene estados que deban procesarse, por lo que
el resultado coincide con la especificación.

Por otro lado, $f_{\mathrm{apl}}(\text{Nil}, o) =\text{List}() = \text{Nil}$. Entonces $P_{\mathrm{apl}}(\text{Nil}, o) ==
f_{\mathrm{apl}}(\text{Nil}, o)$.

**Caso inducción.** $n =k + 1$, $k \geq 0$, con $\texttt{cabeza} = e_1$ y $\texttt{cola} = \text{List}(e_2, \ldots, e_{k+1})$,
por el modelo de sustitución.

$$
P_{\mathrm{apl}}(\text{List}(e_1, \ldots, e_{k+1}), o)
\rightarrow \texttt{ejecutar}(e_1, o) :: P_{\mathrm{apl}}(\text{List}(e_2, \ldots, e_{k+1}), o)
$$

Usando la HI, que vale porque la cola tiene $k$ estados:

$$
\rightarrow \texttt{ejecutar}(e_1, o) :: \text{List}(\texttt{ejecutar}(e_2, o), \ldots, \texttt{ejecutar}
(e_{k+1}, o)) = f_{\mathrm{apl}}(\text{List}(e_1, \ldots, e_{k+1}), o)
$$

Concluimos por inducción que `aplicarA` cumple la especificación.



### Función `transformar`

#### Especificación

Sea $T_g : \text{Orden} \to \text{Orden}$ la función que reemplaza cada orden por el resultado de aplicar $g$ (el parámetro `f`
del código) y conserva la estructura de las compuestas:

$$
\begin{aligned}
T_g(\texttt{Avanzar}(n)) &= g(\texttt{Avanzar}(n)) \\
T_g(\texttt{Girar}(s)) &= g(\texttt{Girar}(s)) \\
T_g(\texttt{Repetir}(k, o)) &= \texttt{Repetir}(k, \ T_g(o)) \\
T_g(\texttt{Secuencia}(\text{List}(o_1, \ldots, o_n))) &= \texttt{Secuencia}(\text{List}(T_g(o_1), \ldots, T_g(o_n))).
\end{aligned}
$$

La función no ejecuta las órdenes; construye otra orden con la misma estructura de repeticiones y secuencias, transformando cada
orden básica mediante $g$.


#### Programa

```scala
 def transformar(o: Orden, f: Orden => Orden): Orden =
    o match {
      case Avanzar(_) =>
        f(o)
      case Girar(_) =>
        f(o)
      case Repetir(veces, orden ) =>
        Repetir(veces, transformar(orden, f))

      case Secuencia(ordenes) =>
        def transformarLista(pendientes: List[Orden]): List[Orden] =

          pendientes match {
            case Nil =>
              Nil

            case cabeza :: cola =>
              transformar(cabeza,f) :: transformarLista(cola)

          }
        Secuencia(transformarLista(ordenes))
    }
    
```

#### Demostración

Sea $P_{\mathrm{tra}}$ el programa `transformar`.

Vamos a demostrar que:

$$
\forall o \in \text{Orden}, \ \forall g : \text{Orden} \to \text{Orden} : P_{\mathrm{tra}}(o, g) == T_g(o)
$$

El tipo $\text{Orden}$, se define recursivamente, así que la demostración es por **inducción estructural** sobre $o$: las
órdenes básicas son $\texttt{Avanzar}(n)$ y $\texttt{Girar}(s)$; las compuestas son $\texttt{Repetir}(k, o')$ y $\texttt{Secuencia}(\text{List}(o_1, \ldots, o_n))$,
con $o', o_1, \ldots, o_n \in \text{Orden}$ (la lista puede estar vacía).

`Secuencia` no llama a `transformar` sobre la lista, sino a la auxiliar `transformarLista`. Por eso se necesita este lema.

**Lema auxiliar (`transformarLista`).**
Sea $L= \text{List}(o_1, \ldots, o_n)$ tal que $P_{\mathrm{tra}}(o_i, g) == T_g(o_i)$ para todo $i$.

Entonces:

$$
\texttt{transformarLista}(L) == \text{List}(T_g(o_1),\ \ldots,\ T_g(o_n))
$$

Se prueba con inducción sobre $n$.

*Caso base:* $n = 0$, es decir, $L= \text{Nil}$, `transformarLista(Nil)` devuelve $\text{Nil}$, que es la lista $\text{List}()$
de cero elementos. Se cumple el lema.

*Caso de inducción:* $n = k +1$, $k \geq 0$. Con $\texttt{cabeza} = o_1$ y $\texttt{cola} = \text{List}(o_2, \ldots, o_{k+1})$:

$$
\texttt{transformarLista}(L) \rightarrow P_{\mathrm{tra}}(o_1, g) :: \texttt{transformarLista}(\text{List}(o_2, \ldots, o_{k+1}))
$$

Por la suposición del lema y la Hipótesis, esto es $T_g(o_1) :: \text{List}(T_g(o_2), \ldots, T_g(o_{k+1}))$, que es el lado derecho.
Concluimos por inducción que el lema vale.

**Caso base:**  `Avanzar(n)` y `Girar(s)`: el programa devuelve `f(o)`, que es $T_g(o)$.

**Caso `Repetir(k, o')`:** por la Hipótesis, $P_{\mathrm{tra}}(o', g) == T_g(o')$, así que $\texttt{Repetir}(k, P_{\mathrm{tra}}
(o', g)) = \texttt{Repetir}(k, T_g(o')) = T_g(\texttt{Repetir}(k, o'))$.

**Caso `Secuencia`:** por la HI cada $o_i$ cumple la suposición del lema, así que `transformarLista` da $\text{List}(T_g(o_1),
\ldots, T_g(o_n))$ y el resultado es $T_g(o)$.

Concluimos por inducción estructural que $P_{\mathrm{tra}}(o, g) == T_g(o)$.

### Componer y rutina

#### Especificación
En este punto se desarrollaron 4 funciones; `componer`, `rutina`, `aplicarA` y `transformar`.
Cada una permite realizar una tarea diferente con las órdenes del robot
- `componer`: Une 2 órdenes en una secuencia, respetando el orden en que se reciben
- `rutina`: Crea una orden que permite avanzar, girar y repetir estas acciones en una cantidad determinada de veces
- `aplicarA`: Ejecuta una misma orden sobre cada uno de los estados de una lista y devuelve los resultados
- `transformar`: Cambia las órdenes básicas mediante una función, manteniendo la estructura de las órdenes compuestas

El objetivo es demostrar que cada función realiza correctamente lo que se le pide, y que los resultados obtenidos
correspondan con los esperados

#### Algoritmo

Las funciones que se analizarán en esta sección son:

```Scala
def componer(o1: Orden, o2: Orden): Orden =
  Secuencia(List(o1, o2))

def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
  Repetir(veces, componer(Avanzar(pasos), Girar(giro)))
```

El código de `aplicarA` y `transformar` se presentó y se demostró en las secciones anteriores de este punto
(*Función `aplicarA`* y *Función `transformar`*), por lo que no se repite aquí.

#### Método de demostración

Para justificar la corrección de estas funciones se utilizaron 2 métodos:
- **Demostración directa**: Para `componer` y `rutina`, porque únicamente construyen órdenes y no
  realizan llamadas recursivas
- **Inducción**: Para `aplicarA` y `transformar`, porque deben procesar estructuras que pueden contener varios elementos
  En cada paso se compara el comportamiento del programa con el resultado que debería obtenerse según la especificación

#### Casos de demostración directa

#### Caso 1: Función componer

#### Especificación
Recibe 2 órdenes $o_1$ y $o_2$, y debe devolver una `Secuencia` que contenga ambas instrucciones en ese mismo orden

Sea $C(o_1, o_2)$ el resultado esperado $C(o_1, o_2) = Secuencia([o_1, o_2])$

- $o_1$: Primera orden
- $o_2$: Segunda orden
- $C$: Resultado esperado de `componer`

#### Demostración directa

El código ejecuta:
```Scala
Secuencia(List(o1, o2)) 
```
- $List(o1, o2)$: Construye una lista en la que o1 ocupa la primera posición y o2 la segunda
- `Secuencia`: Agrupa las 2 órdenes en una instrucción, manteniendo el orden en que fueron recibidas

Coincidiendo con lo esperado: $P_C(o_1, o_2) = C(o_1, o_2)$
- $P_C$ Representa el resultado que devuelve el programa

Cuando estas secuencias se usan con `ejecutar`, primero se realiza $o_1$ y
el estado obtenido se usa para ejecutar $o_2$

Esta se expresa matemáticamente como: $E(e, C(o_1, o_2)) = E(E(e, o_1), o_2)$
- $E$: Representa el resultado de `ejecutar`, cuya corrección se demostró en el punto 2

Para derivarlo, se evalúa `ejecutar` sobre `componer(o1, o2)`. Con el modelo de sustitución, `componer(o1, o2)`
produce `Secuencia(List(o1, o2))`, y `ejecutar` llama a `recorre(ordenes, e)` (ver el punto 2):

$$
\begin{aligned}
\texttt{ejecutar}(e, \texttt{Secuencia}(\text{List}(o_1, o_2)))
&\rightarrow \texttt{recorre}(\text{List}(o_1, o_2),\ e) \\
&\rightarrow \texttt{recorre}(\text{List}(o_2),\ \texttt{ejecutar}(e, o_1)) \\
&\rightarrow \texttt{recorre}(\text{Nil},\ \texttt{ejecutar}(\texttt{ejecutar}(e, o_1), o_2)) \\
&\rightarrow \texttt{ejecutar}(\texttt{ejecutar}(e, o_1), o_2)
\end{aligned}
$$

Cada paso aplica una vez el caso `x :: xs` de `recorre` (con $x = o_1$ y luego $x = o_2$), y el último aplica el caso
`Nil`, que devuelve el estado acumulado. Como `ejecutar` es correcta, es decir `ejecutar(e, o)` $= E(e, o)$, se obtiene:

$$
E(e, C(o_1, o_2)) = E(E(e, o_1), o_2)
$$

Ejemplo: Consideremos `componer(Girar(Derecha), Avanzar(3))`

Entonces la función construye:

```Scala
Secuencia(List(Girar(Derecha), Avanzar(3)))
```
Si el robot comienza en Estado(0, 0, Norte), las instrucciones se ejecutan así:

| Paso            | Instrucción      | Estado obtenido |
|-----------------|------------------|-----------------|
| Inicial         | —                | (0, 0, Norte)   |
| Primera         | `Girar(Derecha)` | (0, 0, Este)    |
| Segunda         | `Avanzar(3)`     | (3, 0, Este)    |
| Resultado final | —                | (3, 0, Este)    |

Siendo el resultado final Estado(3, 0, Este), permitiendo comprobar que el orden de las instrucciones se conserva,
también se puede construir una secuencia con una orden vacía: `componer(Secuencia(Nil), Avanzar(4))`

Siendo el resultado:
```Scala
Secuencia(List(Secuencia(Nil), Avanzar(4)))
```
La orden vacía permanece dentro de la nueva secuencia, porque `componer` no elimina ni simplifica instrucciones

Cumpliéndose así la especificación para cualquier par de órdenes

#### Caso 2: Función rutina

#### Especificación
Recibe una cantidad de pasos, un sentido de giro y un número de repeticiones; teniendo como objetivo
construir una orden que permita avanzar, girar y repetir entre ambas instrucciones.

Sea $U(p, s, v)$ el resultado esperado: $U(p, s, v) = Repetir(v,C(Avanzar(p), Girar(s)))$
- $s$: Sentido del giro
- $v$: Número de repeticiones
- $U$: Resultado esperado de la función `rutina`
- $p$: Cantidad de pasos que avanza el robot

#### Demostración directa:
El código es:

```Scala
def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
  Repetir(veces, componer(Avanzar(pasos), Girar(giro)))
```
Entonces:

```Scala
// Primero se construyen las órdenes
Avanzar(pasos)
Girar(giro)
// Luego la función componer agrupa ambas instrucciones
componer(Avanzar(pasos), Girar(giro))
// Como se demostró anteriormente componer produce
Secuencia(List(Avanzar(pasos), Girar(giro)))
// Finalmente, Repetir recibe esa secuencia y la cantidad de veces solicitada
Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro)))) 
```

Coincidiendo con la especificación $U(p, s, v)$

Por lo tanto $P_R(p, s, v) = U(p, s, v)$

El programa construye la orden que se espera, conservando los 3 argumentos recibidos

#### Derivación general

La igualdad anterior es sobre la *orden construida*. Para ver su efecto al ejecutarla, se evalúa `ejecutar` sobre
`rutina(p)(s)(v)` con argumentos arbitrarios. Sea $b = C(\texttt{Avanzar}(p), \texttt{Girar}(s)) =
\texttt{Secuencia}([\texttt{Avanzar}(p), \texttt{Girar}(s)])$ el cuerpo que construye `componer`. Como
`rutina(p)(s)(v)` produce `Repetir(v, b)`, `ejecutar` llama a la auxiliar `repetir(veces, e)` del punto 2, cuyos
casos se distinguen con guardas:

$$
\begin{aligned}
\texttt{ejecutar}(e, \texttt{Repetir}(v, b))
&\rightarrow \texttt{repetir}(v,\ e) \\
&\rightarrow \texttt{repetir}(v - 1,\ \texttt{ejecutar}(e, b)) \\
&\rightarrow \texttt{repetir}(v - 2,\ \texttt{ejecutar}^{2}(e, b)) \\
&\ \ \vdots \\
&\rightarrow \texttt{repetir}(0,\ \texttt{ejecutar}^{v}(e, b)) \\
&\rightarrow \texttt{ejecutar}^{v}(e, b)
\end{aligned}
$$

Cada uno de los primeros $v$ pasos aplica el caso general `case _`, que resta una repetición y acumula el estado; el
último aplica el caso con guarda `case r if r <= 0`, que devuelve el estado acumulado. Aquí $\texttt{ejecutar}^{k}(e, b)$
denota ejecutar el cuerpo $b$ un total de $k$ veces sobre $e$. Por la derivación de `componer`, cada aplicación cumple
$E(e, b) = E(E(e, \texttt{Avanzar}(p)), \texttt{Girar}(s))$, de modo que para cualquier $p$, $s$ y $v \geq 0$ la rutina
avanza $p$ pasos y gira en el sentido $s$ exactamente $v$ veces seguidas, como exige $U(p, s, v)$.

#### Explicación de la currificación

La función usa 3 listas de parámetros: `rutina(pasos)(giro)(veces)`, permitiendo proporcionar los argumentos por etapas

```Scala
val cuadradoDe = rutina(1)(Derecha) _
```

Con esta expresión se fijan los pasos y el sentido del giro, quedando pendiente la cantidad de repeticiones,
luego `cuadradoDe(4)` construye:

```Scala
Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))
```
La orden obtenida coincide con `cuadrado`, que se encuentra definido.

| Repetición | Estado después de avanzar | Estado después de girar |
| ---------- | ------------------------- | ----------------------- |
| Inicial    | —                         | (0, 0, Norte)           |
| Primera    | (0, 1, Norte)             | (0, 1, Este)            |
| Segunda    | (1, 1, Este)              | (1, 1, Sur)             |
| Tercera    | (1, 0, Sur)               | (1, 0, Oeste)           |
| Cuarta     | (0, 0, Oeste)             | (0, 0, Norte)           |

Dando como resultado `Estado(0, 0, Norte)`; por lo tanto, la orden fue construida mediante `rutina` produciendo
el recorrido esperado cuando se utiliza con `ejecutar`.

#### Comprobación del resultado

Si se ejecuta:

```Scala
val inicial = Estado(0, 0, Norte)
ejecutar(inicial, rutina(1)(Derecha)(4))
```
Por la demostración directa, `rutina(1)(Derecha)(4)` construye
`Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))`, es decir, la orden `cuadrado`. El robot realiza 4 avances y
4 giros, y la tabla anterior muestra el estado después de cada paso:

```Scala
Estado(0, 0, Norte)
```

Es el mismo estado inicial, porque el recorrido forma un cuadrado de lado 1 y el robot termina de nuevo en (0, 0)
mirando al `Norte`. Este resultado coincide con $E$: $E(\text{inicial}, U(1, \text{Derecha}, 4)) = (0, 0, \text{Norte})$.

### Terminación
Las 4 funciones terminan siempre que la lista y órdenes sean finitas y las funciones que se utilizan también terminen
- `componer`: No utiliza recursión. Construye una lista con 2 órdenes y devuelve una `Secuencia`,
  por lo que termina directamente. Ejemplo:
  ```Scala
  Secuencia(List(o1, o2))
  ```

- `rutina`: Tampoco usa recursión. Construye las órdenes `Avanzar` y `Girar`, las une mediante `componer` y
  devuelve `Repetir`. Aunque `veces` sea un número grande, la función solamente construye la orden,
  sin ejecutarla. Ejemplo:
  ```Scala
  Repetir(veces, componer(Avanzar(pasos), Girar(giro)))
  ```

La terminación de `aplicarA` y `transformar`, que sí son recursivas, se explica en sus secciones anteriores: en ambas
la recursión se hace sobre una estructura finita (una lista o una orden) que se reduce en cada llamada hasta llegar a
un caso base.

### Conclusión
Mediante la demostración directa y la inducción se justificó la corrección de las 4 funciones.

`componer` une 2 órdenes respetando su orden, `rutina` permite construir una secuencia de avance y giro que se repite
varias veces, `aplicarA` usa recursión sobre la lista para ejecutar una misma orden en diferentes estados y
`transformar` emplea recursión estructural para modificar las órdenes básicas, conservando la
estructura de las compuestas

Por tanto, las 4 funciones cumplen sus especificaciones: `componer` y `rutina` se justifican mediante demostración directa,
mientras que `aplicarA` y `transformar` se justifica por inducción, Además, todas terminan, ya que operan sobre estructura finita.

## Punto 5. `simplificar`

### Especificación y programa

Sea $\mathcal{E}[o] : \text{Estado} \to \text{Estado}$ la función que calcula lo que hace la orden $o$, es decir
$\mathcal{E}[o](e) = \texttt{ejecutar}(e, o)$. Sea $P_{\mathrm{sim}}$ el siguiente programa en Scala, que recibe
una orden y devuelve otra orden equivalente:

```scala
def simplificar(o: Orden): Orden = o match {

    case Avanzar(0) =>
      Secuencia(Nil)

    case Avanzar(n) =>
      Avanzar(n)

    case Girar(sentido) =>
      Girar(sentido)

    case Repetir(veces, _) if veces <= 0 =>
      Secuencia(Nil)

    case Repetir(veces, orden) if veces == 1 =>
      simplificar(orden)

    case Repetir(veces, orden) =>
      val simplificada = simplificar(orden)

      simplificada match {
        case Secuencia(Nil) =>
          Secuencia(Nil)

        case _ =>
          Repetir(veces, simplificada)
      }
    case Secuencia(ordenes) =>

      def simplificarLista(pendientes: List[Orden]): List[Orden] =

        pendientes match {

          case Nil =>
            Nil

          case cabeza :: cola =>
            val cabezaSimplificada = simplificar(cabeza)
            val colaSimplificada = simplificarLista(cola)

            cabezaSimplificada match {

              case Secuencia(Nil) =>
                colaSimplificada

              case Avanzar(n) =>
                colaSimplificada  match {

                  case Avanzar(m) :: siguiente =>
                    simplificarLista(Avanzar(n + m) :: siguiente)
                  case _ =>
                    Avanzar(n) :: colaSimplificada
                }
              case otra =>
                otra :: colaSimplificada
            }
        }
      val listaSimplificada = simplificarLista(ordenes)

        listaSimplificada match {
        case Nil =>
          Secuencia(Nil)

        case cabeza :: Nil =>
          cabeza

        case _ =>
          Secuencia(listaSimplificada)
      }

    }
    
```

Vamos a demostrar que $P_{\mathrm{sim}}(o)$ hace lo mismo que $o$, desde cualquier estado:


$$
\boxed{\forall o \in \text{Orden}, \ \forall e \in \text{Estado}:\ \texttt{ejecutar}(e, \texttt{simplificar}(o))
== \texttt{ejecutar}(e, o)}
$$

También está este más corto, $\mathcal{E}[P_{\mathrm{sim}}(o)] = \mathcal{E}[o]$.
El tipo $\text{Orden}$ tiene como órdenes básicas $\texttt{Avanzar}(n)$ y $\texttt{Girar}(s)$ para todo sentido $s$, y
órdenes compuestas: $\texttt{Repetir}(k, o)$ y $\texttt{Secuencia}(\texttt{List}(o_1, \ldots, o_n))$ con $o_1, \ldots, o_n \in \text{Orden}$ (incluida la lista vacía).

La inducción no es estructural simple: al juntar dos avances, `simplificarLista` se llama sobre una lista nueva que no es
una subestructura. Por eso la inducción es **bien fundada**, sobre una medida que baja en cada llamada.

### Semántica de `ejecutar`

Sea $\delta$ el desplazamiento de una unidad según la dirección:
$\delta(\text{Norte}) = (0, 1)$, $\delta(\text{Este}) = (1, 0)$,
$\delta(\text{Sur}) = (0, -1)$ y $\delta(\text{Oeste}) =(-1, 0)$.

Leyendo el código de `ejecutar` se obtienen estas ecuaciones. En ellas $(x, y, \mathit{mira})$ es un estado cualquiera,
como en `Estado(x, y, mira)`: posición $(x, y)$ y dirección $\mathit{mira}$.

$$
\begin{aligned}
\mathcal{E}[\texttt{Avanzar}(n)](x, y, \mathit{mira}) &= (x + n\,\delta_x(\mathit{mira}),\ y +
n\,\delta_y(\mathit{mira}),\ \mathit{mira}) \\
\mathcal{E}[\texttt{Girar}(s)](x, y, \mathit{mira}) &= (x, y, \texttt{girar}(\mathit{mira}, s))
\end{aligned}
$$

La función `girar` tiene su propia firma, `girar(d: Direccion, s: Sentido): Direccion`,
con parámetros `d` y `s`. En la ecuación de arriba se llama con la dirección del estado (`mira`) y
el sentido de la orden `Girar(s)`, igual que hace el código de `ejecutar`: `Estado(e.x, e.y, girar(e.mira, sentido))`.

$$
\mathcal{E}[\texttt{Repetir}(k, o)] = \mathcal{E}[o]^{k}
$$

$$
\mathcal{E}[\texttt{Secuencia}(\texttt{Nil})] =\mathrm{id}
$$

$$
\mathcal{E}[\texttt{Secuencia}(p :: ps)] = \mathcal{E}[\texttt{Secuencia}(ps)] \circ \mathcal{E}[p]
$$

Donde $f^{k}$ es aplicar $f$ $k$ veces, y $f^{k} = \mathrm{id}$ si $k \leq 0$ (no es una inversa: una repetición con cero
o menos veces simplemente no hace nada).
Una composición $g \circ f$ significa "primero $f$, después $g$".

**`Secuencia`.** El auxiliar `recorre(pendientes, actual)`, devuelve `recorre(xs, ejecutar(actual, x))`.

Es decir, `x y xs`, que es exactamente $\mathcal{E}[\texttt{Secuencia}(\mathit{cola})] \circ \mathcal{E}[\mathit{cabeza}]$.
Con `Nil` no hace nada.

**Por qué vale la ecuación de `Repetir`.** El auxiliar `repetir(restantes, actual)` hace:
* si `restantes <= 0`, devuelve `actual`.
* si no, devuelve `repetir(restantes - 1, ejecutar(actual, orden))`.

*Afirmación:* `repetir(r, e)` $= \mathcal{E}[o]^{r}(e)$. Se prueba por inducción sobre $r$.

* **Caso base,** $r \leq 0$: devuelve $e$, que es $\mathcal{E}[o]^{r}(e)$ porque  $f^{r} = \mathrm{id}$
  si $r \leq 0$.

* **Caso de inducción,** $r > 0$. La hipótesis es que `repetir(r - 1, e')` $=\mathcal{E}[o]^{r-1}(e')$ para todo $e'$. Entonces  
  `repetir(r, e)` $=$ `repetir(r - 1,` $\mathcal{E}[o]{(e)}$`)` $=\mathcal{E}[o]^{r-1}(\mathcal{E}[o](e)) =\mathcal{E}[o]^{r}(e)$.

**Nota sobre el tamaño de los enteros.** Las coordenadas son `Int` de 32 bits. Con valores normales no hay desbordamiento; Si algún valor se pasara
del máximo, el resultado "daría la vuelta", lo cual equivale a hacer las cuentas módulo $2^{32}$. En esa aritmética la suma sigue siendo
asociativa y conmutativa, así que las igualdades de este informe valen también en ese caso extremo.

### Lemas de equivalencia

**Lema A (suma de avances).** $\mathcal{E}[\texttt{Avanzar}(m)] \circ \mathcal{E}[\texttt{Avanzar}(n)] = \mathcal{E}[\texttt{Avanzar}(n + m)]$.

`Avanzar` no cambia $\mathit{mira}$, así que el segundo avance parte de la misma dirección. Desde $(x, y, \mathit{mira})$:
$$
x + n\,\delta_x(\mathit{mira}) + m\,\delta_x(\mathit{mira}) = x + (n + m)\,\delta_x(\mathit{mira})
$$
Lo mismo con $y$. $\square$ Si hubiera un `Girar` en medio no valdría, y por eso el código solo junta avances contiguos.

**Lema Z (órdenes que no hacen nada).**

1. $\mathcal{E}[\texttt{Avanzar}(0)] = \mathrm{id}$, porque suma $0 \cdot\delta =(0,0)$.
2. $\mathcal{E}[\texttt{Secuencia}(\texttt{Nil})] = \mathrm{id}$, por su ecuación.
3. Si $k \leq 0$, $\mathcal{E}[\texttt{Repetir}(k, o)] =\mathcal{E}[o]^{k} = \mathrm{id}$.
4. Si $\mathcal{E}[o] = \mathrm{id}$, entonces $\mathcal{E}[\texttt{Repetir}(k, o)] = \mathrm{id}^{k} = \mathrm{id}$ para todo $k$

**Lema U (secuencia de elementos).** $\mathcal{E}[\texttt{Secuencia}(p :: \texttt{Nil})] =\mathcal{E}[\texttt{Secuencia}(\texttt{Nil})]
\circ \mathcal{E}[p] = \mathrm{id} \circ \mathcal{E}[p] =\mathcal{E}[p]$


### Medida y terminación

El **tamaño** de una orden es $t(\texttt{Avanzar}(n)) = t(\texttt{Girar}(s)) = 1$, $t(\texttt{Repetir}(k, o)) = 1 + t(o)$ y
$t(\texttt{Secuencia}(ps)) = 1 + T(ps)$. El de una lista es la suma de los tamaños de sus elementos: $T(\texttt{Nil}) = 0$ y $T(p :: ps) = t(p) + T(ps)$.
Toda orden tiene un tamaño al menos 1.

Observación: **el tamaño de una lista de un solo elemento es igual al de ese elemento**, $T(\texttt{List}(p)) = t(p)$. Por eso
`simplificarLista` llamando a `simplificar(cabeza)` no baja el tamaño cuando la lista tiene un solo elemento, y el tamaño por
sí solo no basta para la inducción.

Se usa un par ordenado de forma lexicográfica (primero se compara el primer número, y solo si empatan, el segundo):

$$
\mu(o) =(t(o), 0)\quad \text{para una orden } o, \qquad \mu(ps) = (T(ps), 1) \quad \text{para una lista }ps
$$

El segundo número da prioridad a la lista sobre las órdenes cuando el tamaño empata. Como el primer número es un entero positivo
(o cero para `Nil`) y el segundo toma solo los valores 0 y 1, no hay cadenas infinitas decrecientes: la medida es **bien fundada**.


| Llamada | Desde | Hacia | Por qué baja $\mu$ |
|---|---|---|---|
| `simplificar(orden)` en `Repetir(k, orden)` | $(1 + t(\text{orden}), 0)$ | $(t(\text{orden}), 0)$ | el primer número baja en 1 |
| `simplificarLista(ordenes)` en `Secuencia(ordenes)` | $(1 + T(\text{ordenes}), 0)$ | $(T(\text{ordenes}), 1)$ | el primer número baja en 1 |
| `simplificar(cabeza)` en `simplificarLista(cabeza :: cola)` | $(T(\text{cabeza} :: \text{cola}), 1)$ | $(t(\text{cabeza}), 0)$ | $t(\text{cabeza}) \leq T(\text{cabeza} :: \text{cola})$; si empatan (la cola está vacía), el segundo número baja de 1 a 0 |
| `simplificarLista(cola)` | $(T(\text{cabeza} :: \text{cola}), 1)$ | $(T(\text{cola}), 1)$ | $T(\text{cola}) < T(\text{cabeza} :: \text{cola})$ , porque $t(\text{cabeza}) \geq 1$ |
| `simplificarLista(Avanzar(n + m) :: siguiente)` | $(T(\text{cabeza} :: \text{cola}), 1)$ | $(T(\text{Avanzar}(n+m) :: \text{siguiente}), 1)$ | $T(\text{Avanzar}(n+m) :: \text{siguiente}) = T(\text{colaSimplificada}) \leq T(\text{cola}) < T(\text{cabeza} :: \text{cola})$; la desigualdad central es el Lema T aplicado a `cola`, que tiene medida menor |

**Lema T (el tamaño no crece).**
$t(\texttt{simplificar}(o)) \leq t(o)$ y $T(\texttt{simplificarLista}(ps)) \leq T(ps)$.

Se prueba por inducción bien fundada sobre $\mu$, con la HI sobre las llamadas de medida menor (por lo anterior). No hay circularidad: el lema T solo usa la HI sobre medidas menores.


| Caso | Tamaño de la entrada | Tamaño de la salida |
|---|---|---|
| `Avanzar(0)` | $1$ | $1$ (`Secuencia(Nil)`) |
| `Avanzar(n)`, `Girar(s)` | $1$ | $1$ |
| `Repetir(k, o)` con $k \leq 0$ | $1 + t(o)$ | $1$ |
| `Repetir(1, o)` | $1 + t(o)$ | $t(\texttt{simplificar}(o)) \leq t(o)$ (hipótesis) |
| `Repetir(k, o)`, $k \geq 2$ | $1 + t(o)$ | $1$ si el resultado es `Secuencia(Nil)`; si no, $1 + t(\texttt{simplificar}(o)) \leq 1 + t(o)$ (hipótesis) |
| Lista, `cabezaSimplificada` es `Secuencia(Nil)` | $t(\text{cabeza}) + T(\text{cola})$ | $T(\text{colaSimplificada}) \leq T(\text{cola})$ (hipótesis)|
| Lista, `cabezaSimplificada` no se junta | $t(\text{cabeza}) + T(\text{cola})$ | $t(\text{cabezaSimplificada}) + T(\text{colaSimplificada}) \leq t(\text{cabeza}) + T(\text{cola})$ (hipótesis) |
| `Secuencia(ps)` | $1 + T(ps)$ | $1$ si el resultado es vacío;  $t(p) \leq T(\text{listaSimplificada}) \leq T(ps)$, si es un solo elemento $p$;  $1 + T(\text{listaSimplificada}) \leq 1 + T(ps)$  si son varios (hipótesis) | 

**Caso de juntar avances.** Sea `cabezaSimplificada`$= \texttt{Avanzar}(n)$ y `colaSimplificada` $=\texttt{Avanzar}(m) :: \text{siguiente}$.
La lista que se pasa a la nueva llamada es $\texttt{Avanzar}(n + m) :: \text{siguiente}$, y su tamaño es:

$$
T(\texttt{Avanzar}(n + m) :: \text{siguiente}) = 1 + T(\text{siguiente}) = T(\text{colaSimplificada}) \leq T(\text{cola}) < T(\text{cabeza} :: \text{cola})
$$

### Demostración de equivalencia

Para toda orden $o$ y toda lista $ps$ de órdenes se demuestra:

$$
\mathcal{E}[P_{\mathrm{sim}}(o)] == \mathcal{E}[o]
\qquad \text{y} \qquad
\mathcal{E}[\texttt{Secuencia}(\texttt{simplificarLista}(ps))] == \mathcal{E}[\texttt{Secuencia}(ps)]
$$
La segunda afirmación se necesita para el caso de `Secuencia`, y las dos se demuestran juntas por **inducción bien fundada sobre
$\mu$**: se supone que ambas valen para todo lo de medida menor (HI), lo que justifica cada "por la HI" de abajo.

**Caso base:** $o =\texttt{Avanzar}(0)$

$$
P_{\mathrm{sim}}(\texttt{Avanzar}(0)) \rightarrow \texttt{Secuencia}
(\texttt{Nil})
$$

Por el Lema Z, $\mathcal{E}[\texttt{Avanzar}(0)] = \mathrm{id} = \mathcal{E}[\texttt{Secuencia}(\texttt{Nil})]$.

**Caso base:** $o =\texttt{Avanzar}(n)$ con $n \neq 0$ y $o = \texttt{Girar}(s)$

El programa devuelve la misma orden, así que $\mathcal{E}[P_{\mathrm{sim}}(o)] = \mathcal{E}[o]$.

**Caso de inducción:** $o = \texttt{Repetir}(k, o')$

Como $t(o') < t(\texttt{Repetir}(k, o'))$, la Hipótesis es $\mathcal{E}[P_{\mathrm{sim}}(o')] = \mathcal{E}[o']$. Hay que probar
$\mathcal{E}[P_{\mathrm{sim}}(\texttt{Repetir}(k, o'))] = \mathcal{E}[o']^{k}$. El programa distingue cuatro casos:

* $k \leq 0$: devuelve `Secuencia(Nil)`. Por el Lema Z, $\mathcal{E}[o']^{k} = \mathrm{id} = \mathcal{E}[\texttt{Secuencia}(\texttt{Nil})]$

* $k = 1$: devuelve $P_{\mathrm{sim}}(o')$. Por la Hipótesis, $\mathcal{E}[P_{\mathrm{sim}}(o')] =\mathcal{E}[o']^{1}$.

* $k \geq 2$ y $P_{\mathrm{sim}}(o') = \texttt{Secuencia}(\texttt{Nil})$: devuelve `Secuencia(Nil)`. Por la HI, $\mathcal{E}[o'] = \mathcal{E}[\texttt{Secuencia}(\texttt{Nil})] =\mathrm{id}$,
  y por el Lema Z (numeral 4), $\mathcal{E}[o']^{k} =\mathrm{id}$.

* $k \geq 2$ y $P_{\mathrm{sim}}(o')$ es otra orden: devuelve $\texttt{Repetir}(k, P_{\mathrm{sim}}(o'))$, y

$$
\mathcal{E}[\texttt{Repetir}(k, P_{\mathrm{sim}}(o'))] = \mathcal{E}[P_{\mathrm{sim}}(o')]^{k} = \mathcal{E}[o']^{k}
$$

Por la ecuación de `Repetir` y la Hipótesis.

**Caso de inducción:** $o = \texttt{Secuencia}(ps)$

Sea $\mathit{listaSimplificada} = \texttt{simplificarLista}(ps)$. Como $\mu(ps) = (T(ps), 1) < (1 + T(ps), 0) = \mu(o)$, 
la segunda afirmación es Hipótesis y da $\mathcal{E}[\texttt{Secuencia}(\mathit{listaSimplificada})] = \mathcal{E}[\texttt{Secuencia}(ps)]$. 

El programa mira $\mathit{listaSimplificada}$:

* $\texttt{Nil}$ o dos o más elementos: devuelve `Secuencia(listaSimplificada)` y por la HI hace lo mismo que $\texttt{Secuencia}(ps)$.

* $p :: \texttt{Nil}$ devuelve $p$. Por el Lema U, $\mathcal{E}[p] = \mathcal{E}[\texttt{Secuencia}(p :: \texttt{Nil})] = \mathcal{E}[\texttt{Secuencia}(\mathit{listaSimplificada})]$,  
que por la HI es $\mathcal{E}[\texttt{Secuencia}(ps)]$

**Afirmación para listas:** $\mathcal{E}[\texttt{Secuencia}(\texttt{simplificarLista}(ps))] == \mathcal{E}[\texttt{Secuencia}(ps)]$

*Caso base:* $ps = \texttt{Nil}$. Devuelve `Nil`, la misma lista.

*Caso de inducción:* $ps =\mathit{cabeza} :: \mathit{cola}$, Sean $\mathit{cabezaSimplificada} = P_{\mathrm{sim}}(\mathit{cabeza})$ y $\mathit{colaSimplificada} = \texttt{simplificarLista}(\mathit{cola})$.
Ambas llamadas tienen medida menor (tabla de medida).

Así que por la Hipótesis se tienen dos hechos:

$$ 
\text{Hipótesis 1: } \mathcal{E}[\mathit{cabezaSimplificada}] =
\mathcal{E}[\mathit{cabeza}] \qquad \text{Hipótesis 2: }
\mathcal{E}[\texttt{Secuencia}(\mathit{colaSimplificada})] =
\mathcal{E}[\texttt{Secuencia}(\mathit{cola})]
$$

El programa tiene cuatro caminos según `cabezaSimplificada` y `colaSimplificada`:

* $\mathit{cabezaSimplificada} = \texttt{Secuencia}(\texttt{Nil})$: devuelve `colaSimplificada`. Por Hipótesis 1 y el Lema Z,
$\mathcal{E}[\mathit{cabeza}] = \mathrm{id}$, así que $\mathcal{E}[\texttt{Secuencia}(\mathit{cola})] \circ \mathcal{E}[\mathit{cabeza}] =
\mathcal{E}[\texttt{Secuencia}(\mathit{cola})]$, que por Hipótesis 2 es $\mathcal{E}[\texttt{Secuencia}(\mathit{colaSimplificada})]$

* $\mathit{cabezaSimplificada} = \texttt{Avanzar}(n)$ y $\mathit{colaSimplificada} = \texttt{Avanzar}(m) :: \mathit{siguiente}$: devuelve `simplificarLista(Avanzar(n + m) :: siguiente)`.
Esta lista tiene medida menor que $ps$ (última fila de la tabla), así que por la hipótesis su resultado equivale a ella:

$$
\begin{aligned}
\mathcal{E}[\texttt{Secuencia}(\texttt{Avanzar}(n+m) :: \mathit{siguiente})]
 &= \mathcal{E}[\texttt{Secuencia}(\mathit{siguiente})]\circ \mathcal{E}[\texttt{Avanzar}(n+m)] \\
 &= \mathcal{E}[\texttt{Secuencia}(\mathit{siguiente})]\circ \mathcal{E}[\texttt{Avanzar}(m)] \circ \mathcal{E}[\texttt{Avanzar}(n)] \\
 &= \mathcal{E}[\texttt{Secuencia}(\mathit{colaSimplificada})] \circ \mathcal{E}[\mathit{cabezaSimplificada}] \\
 &= \mathcal{E}[\texttt{Secuencia}(\mathit{cola})] \circ \mathcal{E}[\mathit{cabeza}] 
 \end{aligned} 
$$
La primera igualdad es la ecuación de `Secuencia`; la segunda, el Lema A; la tercera, ecuación de `Secuencia` leída al revés;
la cuarta, las Hipótesis.

* $\mathit{cabezaSimplificada} =\texttt{Avanzar}(n)$ y $ \mathit{colaSimplificada}$ no empieza con `Avanzar`: devuelve `Avanzar(n) :: colaSimplificada`.
* Cualquier otra `cabezaSimplificada` : devuelve `cabezaSimplificada :: colaSimplificada`.

En los dos últimos, por la ecuación de `Secuencia` y HI:

$$
\mathcal{E}[\texttt{Secuencia}(\mathit{cabezaSimplificada} :: \mathit{colaSimplificada})] =
\mathcal{E}[\texttt{Secuencia}(\mathit{colaSimplificada})] \circ \mathcal{E}[\mathit{cabezaSimplificada}] =
\mathcal{E}[\texttt{Secuencia}(\mathit{cola})] \circ \mathcal{E}[\mathit{cabeza}]
$$
Los cuatro caminos cubren todas las posibilidades de `cabezaSimplificada` y `colaSimplificada`.

Concluimos por inducción bien fundada, sobre la medida $\mu$ que baja en cada llamada recursiva, que:

$$
\forall o \in \text{Orden}, \ \forall e \in \text{Estado} : \texttt{ejecutar}(e, \texttt{simplificar}(o)) == \texttt{ejecutar}(e, o)
$$

Lo demostrado es que `simplificar` conserva lo que hace la orden. No se afirma que el resultado no pueda
simplificarse más.

## Corrección de programas iterativos
Un programa iterativo se argumenta definiendo un estado de la iteración, su estado inicial, su estado final, una invariante y la transformación
que pasa de un estado al siguiente.
`aplicarA`, `transformar` y `simplificar` son recursivos: procesan una lista o una orden con llamadas sobre la cola o sobre subórdenes, y no hay estado de iteración ni invariante de ciclo. Por eso su corrección
se argumenta con el método recursivo de la sección Argumentar la corrección de programas recursivos.