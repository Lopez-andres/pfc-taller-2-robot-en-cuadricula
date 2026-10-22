

## Punto 1. `girar`

### Especificación

Las direcciones se numeran en sentido horario con la función $\text{pos}$:

$$\text{pos}(\text{Norte}) = 0 \quad \text{pos}(\text{Este}) = 1 \qquad \text{pos}(\text{Sur}) = 2 \qquad \text{pos}(\text{Oeste}) = 3$$

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

Él `match` ejecuta el primer `case` cuyo patrón coincide con la pareja $(d,s)$. Los 8 patrones son distintos entre sí y cubren las 8 parejas, porque `Direccion` y `Sentido` son `sealed trait`.
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

Las 8 parejas de $\text{Dirección} \times \text{Sentido}$ cumplen $P_{\text{girar}}(d,s) = g(d,s)$. Por lo tanto, `girar` es correcta respecto a su especificación. 

## Punto 2: Aplicación del orden a los estados

### Especificación 
La función $ejecutar$ recibe el estado actual y una orden. Su objetivo es devolver un nuevo estado después de realizar la instrucción, sin que esta modifique su estado 
original.

El estado del robot se ve representado mediante: $e = (x, y, d)$
- $x$ y $y$: Representa su posición
- $d$: Es la dirección hacia la que mira

Sea $E(e, o)$ la función que representa el resultado esperado de ejecutar una orden
- $o$: (Orden) La instrucción que ejecuta el robot
- $e$: Estado actual del robot (posición y dirección)

Para $Avanzar(n)$ las coordenadas cambian dependiendo de la dirección:

$E((x, y, d), Avanzar(n)) = $
- $(x, y + n, d) si d = Norte$
- $(x, y - n, d) si d = Sur$
- $(x + n, y, d) si d = Este$
- $(x - n, y, d) si d = Oeste$

En donde:
- $E$: Es la función que calcula el nuevo estado después de realizar una acción
- $n$: Cantidad de unidades que va a avanzar

Para la orden $Girar(sentido)$, las coordenadas permanecen iguales y únicamente cambian la dirección:
$E((x, y, d), Girar(s)) = (x, y, g(d, s))$

- $s$: Sentido del giro (derecha o izquierda)

La función $g(d, s)$ representa la función $girar$ que se explicó en el punto 1. Como tal
permite saber hacia que dirección queda mirando el robot después de girar a la derecha o a la izquierda

Cuando una orden contiene otras órdenes, el robot las ejecuta una por una, siguiendo el orden establecido

### Algoritmo

Comienza utilizando $o match$ para identificar el tipo de orden recibida, según el caso, ejecuta un avance, 
realiza un giro, repite una instrucción o recorre una secuencia; permitiendo tratar las 4 alternativas del tipo 
$Orden$ de manera independiente

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
      if (restantes <= 0) {
        actual
      } else {
        repetir(restantes - 1, ejecutar(actual, orden))
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
- $@$: Indica que estamos usando una anotación
- $@annotation.tailrec$: Le indica a Scala donde encontrar esta anotación para evitar el $import$
- $annotation$: Es un paquete de Scala que contiene anotaciones utilizadas para darle indicaciones al compilador 
- $tailrec$: Anotación que verifica que una función use recursion de cola, es decir que la llamada recursiva sea 
  su última opción

Sea $P(e, o)$ el resultado que devuelve el programa, porque se quiere demostrar que:
$$\forall e\ \in\ Estado, \forall o\ \in\ Orden : P(e, o) = E(e, o)$$

"Para cualquier estado del robot y cualquier orden, el resultado que produce el 
programa debe deser igual al resultado esperado" 

- $P(e, o)$: Resultado que produce el programa
- $E(e, o)$: Resultado esperado según las reglas del ejercicio

### Método: Inducción Estructural   
Para comprobar que la función $ejecutar$ funciona correctamente, se revisa los 4 tipos de órdenes que
puede recibir el robot: $Avanzar, Girar, Repetir y Secuencia$.

Primero se analizará $Avanzar y Girar$, porque son órdenes sencillas que el robot ejecuta directamente.
Después se revisará $Repetir y Secuencia$, que contiene otras órdenes.

Para estos casos, partimos de que las órdenes internas $o$ funcionen bien $e$, comprobando que $P(e,o)=E(e,o)$ 
al repetirlas o ejecutarlas una tras de la otra el robot llega al estado esperado

### Caso base

#### Caso 1: Avanzar(n)
El programa usa $match$ sobre $e.mira$ para determinar qué coordenadas deben modificar,
si el robot mira al norte, devuelve: 
- $match$: Sirve para comprobar un valor y decidir que acción se realiza segun el caso
- $e.mira$: Permite consultar la dirección hacia la que se está mirando

```Scala
e.copy(y = e.y + n)
```
- $.copy$: Crea una copia del estado a partir del anterior, cambiando solo los valores que se le indiquen, 
    sin modificar el original 
- $y = $: Indica que se cambia la coordenada vertical
- $e.y$: Valor actual de la coordenada y
- $+ n$: Suma la cantidad de pasos que se debe avanzar

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

Los casos $Sur, Este y Oeste$ siguen el mismo razonamiento, modificando la coordenada correspondiente.

#### Caso especial: Avanzar con pasos negativos
La función $ejecutar$ permite que el robot retroceda cuando recibe un número negativo de pasos. 
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
Sí lo ejecutamos, se realia la operación
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
Como $copy$ conserva las coordenadas que no se modifican, solamente cambia la dirección,
por lo cual:
$P((x, y, d), Girar(s)) =  (x, y, g (d, s))$
- $Girar(s)$: Orden de girar según el sentido

Ejemplo: Si se esta en (4, 7, Norte) y gira a la derecha:
```Scala
P((4, 7, Norte), Girar(Derecha)) =  (4, 7, Este)
```
Observándose que las coordenadas $x = 4$ y $y = 7$ permanecen iguales. Solamente cambia la dirección de
$Norte$ a $Este$

Como la corrección de $girar$ se demostró en el punto $1$, el resultado coincide con la especificación

### Caso Recursivo: Repetir
La orden $Repetir(veces, orden)$ permite ejecutar una misma instrucción varias veces.
Para justificar este caso, partimos en un caso en donde no hay repeticiones y después analizamos que sucede cuando
se agrega una repetición más.
- $repetir(veces, e)$: Inicia la función auxiliar, se ejecuta hasta completar tosas las repeticiones
- $veces$: Cuantas repeticiones deben realizarse
- $e$: Estado inicial del robot

Matemáticamente, se podría expresar como: $R(k, e, o)$.
Sea $R$ el estado obtenido después de ejecutar una orden $o$ exactamente $k$ veces, comenzando desde el estado $e$, 
donde $k >= 0$ 

$R(0, e, o) = e$ Si no hay repeticiones entonces se conserva el estado
$R(k + 1, e, o) = R(k, E(e, o), o)$ Primero se ejecuta una orden y luego se continua con las repeticiones pendientes

#### Caso base: Cero o menos repeticiones
Cuando la cantidad de repeticiones es 0 o negativa, el robot no debe de hacer ningún movimiento o giro
Para controlar esta situación, la función auxiliar $repetir$ utiliza la siguiente condición

```Scala
if (restantes <= 0 ){
actual
}
```
Este comportamiento se expresa matemáticamente asi: $P(e, Repetir(v, o)) = e, v <= 0$
- $v$: Cantidad de veces que se repite una orden

- $restantes$: Índica cuantas veces falta ejecutar la orden

La condición comprueba si todavía queda repeticiones por realizar. Si $restantes$ es 0 o negativo, la función devuelve
directamente el estado $actual$ sin ejecutar una orden.

Ejemplo:

```Scala
val e = Estado(3, 2, Norte)
val orden = Repetir(0, Avanzar(5)) 
```
Entonces cuando se llama a $ejecutar(e, orden)$ la función auxiliar recibe 0 repeticiones 
y junto con el estado (3, 2, Norte)
Como se cumple $restantes <= 0$ entonces devuelve el mismo estado 

```Scala
Estado(3, 2, Norte)
```
Significando que permanecerá en la misma posición y mirando aún al $Norte$ 

Y si la cantidad de repeticiones fuera negativa (ejemplo: $Repetir(-2, Avanzar(5))$ ), ocurriría lo mismo;
cumpliendo la condición y el robot conservaría el estado.

Evidenciándose que cuando la cantidad de repeticiones es 0 o <0 (menor que cero), el resultado del programa coincide
con lo establecido en la especificación "No se ejecuta ninguna orden y se devuelve el estado recibido".  

#### Caso recursivo: $k + 1$ Repeticiones pendientes
Para comprobar que la función $repetir$ esté funcionando correctamente, comenzamos suponiendo que puede 
realizar correctamente $k$ repeticiones
-$k$: Cantidad de repeticiones

Cuando queda $k + 1$ repeticiones, primero realiza la orden sobre el estado actual y luego llama a 
la función $repetir$, disminuyendo la cantidad de repeticiones pendiente.

Lo anterior se realiza mediante la función:
```Scala
repetir(restantes - 1, ejecutar(actual, orden))
```
- $ejecutar(actual, orden)$: Ejecuta 1 vez la orden y obtiene el estado; el resultado de esta se usa para 
    continuar con las otras repeticiones
- $restantes - 1$: Disminuye la cantidad de repeticiones pendientes, porque se ejecutó una de estas.

Ejemplo: Sí queremos ejecutar una orden 3 veces
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

| Repetición      | Operación matematica         | Estado obtenido | Pendientes |
|-----------------|------------------------------|-----------------|------------|
| Inicial         | $R(3,e_0,o)$                 | (1, 2, Este)    | 3          |
| Primera         | $R(3,e_0,o)=R(2,E(e_0,o),o)$ | (3, 2, Este)    | 2          |
| Segunda         | $R(2,e_1,o)=R(1,E(e_1,o),o)$ | (5, 2, Este)    | 1          |
| Tercera         | $R(1,e_2,o)=R(0,E(e_2,o),o)$ | (7, 2, Este)    | 0          |
| Resultado final | $R(0,e_3,o)=e_3$             | (7, 2, Este)    | 0          |

Donde $e_0$ es el estado inicial y $e_1$, $e_2$ y $e_3$ son los estados obtenidos después de cada repetición

Quedando demostrado que $repetir$ ejecuta una orden las veces indicadas, utilizando el resultado anterior 
para continuar hasta el resultado esperado

### Caso recursivo: Secuencia

#### Caso base: Lista vacía 
La orden $Secuencia(ordenes)$ permite ejecutar una lista de instrucciones en el orden establecido
Esta comienza llamando a 
```Scala
recorre(ordenes, e)
```

Cuando la lista es $Nil$, no queda órdenes por ejecutar. Por eso, el programa devuelve el estado actual:

```Scala
case Nil => actual
```
Ejemplo: Cuando el robot se encuentre en la posición (2, 3), mirando hacia el $Norte$, y recibe una lista vacía
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
$P((2, 3, Norte), Secuencia(Nil))$
$ = (2, 3, Norte)$

Quedando demostrado que, cuando no hay órdenes por ejecutar, el robot permanece en la misma posición y dirección. 

#### Caso recursivo: Lista con elementos
Para mirar si funciona correctamente, se supone que se puede ejecutar una lista de $k$ órdenes
Si tenemos $k + 1$ órdenes, el programa separa la primera instrucción $x$ de las restantes $xs$
```Scala
case x :: xs => recorre(xs, ejecutar(actual, x))
```
- $ejecutar(actual, x)$: Ejecuta la primera orden y obtiene el nuevo estado del robot
- $recorre(xs, ejecutar(actual, x))$: Continúa ejecutando las órdenes restantes usando el nuevo estado

Ejemplo: Que empiece en (2, 3), mirando hacia el $Norte$ y recibe 2 instrucciones

```Scala
val e = Estado(2, 3, Norte)
val orden = Secuencia(List(Avanzar(2), Girar(Derecha)))
ejecutar(e, orden)
```
Resultados de la secuencia:

| Paso            | Operación realizada        | Estado obtenido | Órdenes pendientes |
|-----------------|----------------------------|-----------------|--------------------|
| Inicial         | —                          | (2, 3, Norte)   | 2                  |
| Primera         | $3+2=5$                    | (2, 5, Norte)   | 1                  |
| Segunda         | $Norte \rightarrow  Este$  | (2, 5, Este)    | 0                  |
| Resultado final | $P(e, Secuencia(Nil)) = e$ | (2, 5, Este)    | 0                  |

Dando como resultado final $Estado(2, 5, Este)$

Y en expresión matematica es: $E(e, Secuencia([o1, o2])) = E(E(e, o1), o2)$
- $e$: Estado inicial
- $o1$: Primera orden
- $o2$: Segunda orden
- $(E(e, o1)$: Estado obtenido después de ejecutar la primera orden
- $E(E(e, o1), o2)$: Estado obtenido después de ejecutar la segunda orden

Resultado de la secuencia:

| Paso            | Operación realizada                         | Estado obtenido | Órdenes pendientes |
|-----------------|---------------------------------------------|-----------------|--------------------|
| Inicial         | $E(e,Secuencia([o_1,o_2]))$                 | (2, 3, Norte)   | 2                  |
| Primera         | $E(e,o_1)=(2,3+2,Norte)$                    | (2, 5, Norte)   | 1                  |
| Segunda         | $E(E(e,o_1),o_2)=(2,5,Este)$                | (2, 5, Este)    | 0                  |
| Resultado final | $E(e,Secuencia([o_1,o_2]))=E(E(e,o_1),o_2)$ | (2, 5, Este)    | 0                  |

Por lo tanto, $Secuencia$ ejecuta las instrucciones en el orden indicado, utilizando el resultado para continuar 
con la siguiente. Coincidiendo con el comportamiento esperado

### Terminación
La función $ejecutar$ termina porque cada vez que se hace una llamada recursiva, se acerca a una condición que permite 
detener el proceso llamado como "caso base".

#### En repetir
Como se explicaba anteriormente que la cantidad de repeticiones pendientes disminuye hasta llegar a 0, 
teniendo en cuenta que si la cantidad inicial es 0 o negativa, la función termina inmediatamente

Controlándose con:

```Scala
if (restantes <= 0 ){
actual
}
```
Ejemplo: Supongamos que el robot comienza en (2, 3, Norte) y debe de avanzar 2 unidades, repitiendo esa orden 2 veces 
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

Cuando llegue a 0 devuelve el último estado, y no se ejecuta más, comprobándose que $Repetir$ termina cuando completa la 
cantidad de repeticiones indicadas.

#### En secuencia
Ejecuta la primera orden de la lista y continua con las restantes, en cada llamada la lista se hace mas
corta hasta llegar a $Nil$

Ejemplo
```Scala
Secuencia(List(Avanzar(2), Girar(Derecha)))
```
Viéndose reflejado como las órdenes disminuye
```Scala
List(Avanzar(2), Girar(Derecha))
List(Girar(Derecha))
Nil
```
Cuando llega a $Nil$ se ejecuta:
```Scala
case Nil => actual
```
Esto indica que ya no quedan más instrucciones y se devuelve el estado actual, además de que $repetir$ y $recorre$ 
usan recursión de cola. 
Demostrando que las ordenes, las listas y las repeticiones son finitas, además de que la función 
termina después de completar las instrucciones.

### Comprobación del resultado
Para comprobar el funcionamiento de $ejecutar$, se utilizará el siguiente ejemplo:

```Scala
val e = Estado(1, 2, Este)
val orden = Repetir(3, Avanzar(2))
ejecutar(e, orden)
```
Comienza en la posición (1, 2), mirando hacia el $Este$ y debe avanzar 2 unidades 3 veces.

Como está mirando al $Este$, cada repetición aumenta la coordenada $x$, mientras que $y$ y la dirección 
permanecen iguales

Ls operaciónes realizadas son:

| Repetición | Operación | Estado obtenido |
|------------|-----------|-----------------|
| Inicial    | —         | (1, 2, Este)    |
| Primera    | $1+2=3$   | (3, 2, Este)    |
| Segunda    | $3+2=5$   | (5, 2, Este)    |
| Tercera    | $5+2=7$   | (7, 2, Este)    |

Matemáticamente, se puede calcular la posición horizontal final sumando los pasos realizados en las 3 repeticiones:
$x_f = 1 + 3(2)$
$1 + 6 = 7$

La coordenada vertical no cambia $y_f = 2$

Por lo tanto, el estado final es:

```Scala
Estado (7, 2, Este)
```

$P((1, 2, Este), Repetir(3, Avanzar(2))) = (7, 2, Este)$

El resultado coincide con la especificación, porque si avanzo 6 unidades sin cambiar la dirección. 
Demostrando que $ejecutar$ realiza correctamente las repeticiones y obtiene el estado esperado.

### Conclusión 
Mediante los casos base y los casos de recursión, se explicó por qué la función $ejecutar$ funciona correctamente.

Las órdenes $Avanzar$ y $Girar$ modifican directamente la posición o la dirección del robot. 
Por otro lado, $Repetir$ y $Secuencia$ utilizan recursión para ejecutar las instrucciones en orden correspondiente,
tomando como punto de inicio el estado anterior.

De esto se puede inferir que siempre que los calculos no superen los límites del tipo $Int$, esta se cumple
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
      case Repetir(veces, orden) => if (veces <= 0) 0 else veces * cuantasBasicas(orden)
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


## Punto 4 Rutinas con funciones de alto orden

### Especificación
En este punto se desarrolló 4 funciones; $componer$, $rutina$, $aplicarA$ y $transformar$.
Cada una te permite realizar una tarea diferente con las órdenes del robot
- $componer$: Une 2 órdenes en una secuencia, respetando el orden en que se reciben
- $rutina$: Crea una orden que permite avanzar, girar y repetir estas acciones en una cantidad determinada de veces
- $aplicarA$: Ejecuta una misma orden sobre cada uno de los estados de una lista y devuelve los resultados
- $transformar$: Cambia las órdenes básicas mediante una función, manteniendo la estructura de las órdenes compuestas

El objetivo es demostrar que cada función realiza correctamente lo que se le pide, y que los resultados obtenidos 
correspondan con los esperados 

### Algoritmo

Las funciones que se analizarán son:

```Scala
def componer(o1: Orden, o2: Orden): Orden =
  Secuencia(List(o1, o2))

def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
  Repetir(veces, componer(Avanzar(pasos), Girar(giro)))

def aplicarA(estados: List[Estado], o: Orden): List[Estado] =
  estados match {
    case Nil =>
      Nil
    case cabeza :: cola =>
      ejecutar(cabeza, o) :: aplicarA(cola, o)
  }

def transformar(o: Orden, f: Orden => Orden): Orden =
  o match {
    case Avanzar(_) =>
      f(o)

    case Girar(_) =>
      f(o)

    case Repetir(veces, orden) =>
      Repetir(veces, transformar(orden, f))

    case Secuencia(ordenes) =>
      def transformarLista(pendientes: List[Orden]): List[Orden] =
        pendientes match {
          case Nil =>
            Nil
          case cabeza :: cola =>
            transformar(cabeza, f) :: transformarLista(cola)
        }

      Secuencia(transformarLista(ordenes))
  }
```
### Método de demostración

Para justificar la corrección de estas funciones se utilizaron 2 métodos: 
- **Demostración directa**: Para $componer$ y $rutina$, porque únicamente construyen órdenes y no 
    realizan llamadas recursivas  
- **Inducción**: Para $aplicarA$ y $transformar$, porque deben procesar estructuras que pueden contener varios elementos 
En cada paso se compara el comportamiento del programa con el resultado que debería obtenerse según la especificación

### Casos de demostración directa

### Caso 1: Función componer

#### Especificación
Recibe 2 órdenes $o_1$ y $o_2$, y debe devolver una $Secuencia$ que contenga ambas instrucciones en ese mismo orden

Sea $C(o_1, o_2)$ el resultado esperado $C(o_1, o_2) = Secuencia([o_1, o_2])$

- $o_1$: Primer orden
- $o_2$: Segundo orden
- $C$: Resultado esperado de $componer$

#### Demostración directa

El código ejecuta: 
```Scala
Secuencia(List(o1, o2)) 
```
- $List(o1, o2)$: Construye una lista en la que o1 ocupa la primera posición y o2 la segunda
- $Secuencia$: Agrupa las 2 órdenes en una instrucción, manteniendo el orden en que fueron recibidas  

Coincidiendo con lo esperado: $P_C(o_1, o_2) = C(o_1, o_2)$
- $P_C$ Representa el resultado que devuelve el programa

Cuando estas secuencias se usan con $ejecutar$, primero se realiza $o_1$ y 
el estado obtenido se usa para ejecutar $o_2$

Esta se expresa matemáticamente como: $E(e, C(o_1, o_2)) = E(E(e, o_1), o_2)$
- $E$: Representa el resultado de $ejecutar$, cuya corrección se demostró en el punto 2

Ejemplo: Consideremos $componer(Girar(Derecha), Avanzar(3))$

Entonces la función construye:

```Scala
Secuencia(List(Girar(Derecha)), Avanzar(3)))
```
Si el robot comienza en Estado(0, 0, Norte), las instrucciones se ejecutan así:

| Paso            | Instrucción      | Estado obtenido |
|-----------------|------------------|-----------------|
| Inicial         | —                | (0, 0, Norte)   |
| Primera         | `Girar(Derecha)` | (0, 0, Este)    |
| Segunda         | `Avanzar(3)`     | (3, 0, Este)    |
| Resultado final | —                | (3, 0, Este)    |

Siendo el resultado final Estado(3, 0, Este), permitiendo comprobar que el orden de las instrucciones se conserva,
también se puede construir una secuencia con una orden vacía: $componer(Secuencia(Nil), Avanzar(4))$

Siendo el resultado: 
```Scala
Secuencia(List(Secuencia(Nil), Avanzar(4)))
```
La orden vacía permanece dentro de la nueva secuencia, porque $componer$ no elimina ni simplifica instrucciones

Cumpliéndose asi la especificación para cualquier par de órdenes

### Caso 2: Función rutina

#### Especificación
Recibe una cantidad de pasos, un sentido de giro y un número de repeticiones; teniendo como objetivo 
construir una orden que permita avanzar, girar y repetir entre ambas instrucciones.

Sea $R(p, s, v)$ el resultado esperado: $R(p, s, v) = Repetir(v,C(Avanzar(p), Girar(s)))$
- $s$: Sentido del giro
- $v$: Número de repeticiones
- $R$: Resultado esperado de la función $rutina$ 
- $p$: Cantidad de pasos que avanza el robot

#### Demostración directa:
El código es:

```Scala
def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
  Repetir(veces, componer(Avanzar(pasos), Girar(giro)))
```
Entonces:

```Scala
// Primero se construye las órdenes
Avanzar(pasos)
Girar(giro)
// Luego la función $componer$ agrupa ambas instrucciones
componer(Avanzar(pasos), Girar(giro))
// Como se demostró anteriormente $componer$ produce
Secuencia(List(Avanzar(pasos), Girar(giro)))
// Finalmente, $Repetir$ recibe esa secuencia y la cantidad de veces solicitada
Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro)))) 
```

Coincidiendo con la especificación $R(p, s, v)$ 

Por lo tanto $P_R(p, s, v) = R(p, s, v)$

El programa construye la orden que se espera, conservando los 3 argumentos recibidos

#### Explicación de la currificación

La función usa 3 listas de parámetros: $rutina(pasos)(giro)(veces)$, permitiendo proporcionar los argumentos por etapas

```Scala
val cuadradoDe = rutina(1)(Derecha) _
```

Con esta expresión se fijan los pasos y el sentido del giro, quedando pendiente la cantidad de repeticiones, 
luego $cuadradoDe(4)$ construye:

```Scala
Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))
```
La orden obtenida coincide con $cuadrado$, que se encuentra definido.

| Repetición | Estado después de avanzar | Estado después de girar |
| ---------- | ------------------------- | ----------------------- |
| Inicial    | —                         | (0, 0, Norte)           |
| Primera    | (0, 1, Norte)             | (0, 1, Este)            |
| Segunda    | (1, 1, Este)              | (1, 1, Sur)             |
| Tercera    | (1, 0, Sur)               | (1, 0, Oeste)           |
| Cuarta     | (0, 0, Oeste)             | (0, 0, Norte)           |

Dando como resultado $Estado(0, 0, Norte)$; por lo tanto, la orden fue construida mediante $rutina$ produciendo 
el recorrido esperado cuando se utiliza con $ejecutar$. 
 
#### Comprobación del resultado

Si se ejecuta:

```Scala
ejecutar(inicial, rutina(1)(Derecha)(4))
```
El robot realiza 4 avances y 4 giros


### Caso 3: Función aplicarA








### Caso 4: Función transformar








### Terminación
Las 4 funciones terminan siempre que la lista y órdenes sean finitas y las funciones que se utilizan también terminen
- $componer$: No utiliza recursión. Construye una lista con 2 órdenes y devuelve una $Secuencia$, 
  por lo que termina directamente. Ejemplo:
  ```Scala
  Secuencia(List(o1, o2))
  ```

- $rutina$: Tampoco usa recursión. Construye las órdenes $Avanzar$ y $Girar$, las une mediante $componer$ y 
  devuelve $Repetir$. Aunque $veces$ sea un número grande, la función solamente construye la orden, 
  sin ejecutarla. Ejemplo:
  ```Scala
  Repetir(veces, componer(Avanzar(pasos), Girar(giro)))
  ```

- $aplicarA$: Utiliza recursión sobre listas. En cada llamada procesa la cola, reduciendo la cantidad de elementos 
  pendientes. Ejemplo:
  ```Scala
  List(e1, e2, e3)
  List(e2, e3)
  List(e3)
  Nil
  ```
  Cuando llega a $Nil$, devuelve una lista vacía y termina. Como la lista disminuye en cada llamada, la recursión
  finaliza

- $transformar$: Usa recursión estructural:
  - $Avanzar y Girar$: Aplica $f$ directamente, sin realizar llamadas recursivas 
  - $Repetir$: Procesa una orden interna más pequeña
  - $Secuencia$: Utiliza $transformarLista$ para asi recorrer las órdenes y reducir la lista en 
    cada llamada hasta llegar a $Nil$
  Ejemplo: 
  ```Scala
  List(o1, o2)
  List(o2)
  Nil
  ``` 
  Por tanto, la función termina porque procesa estructuras cada vez más pequeñas, hasta alcanzar los casos base

###  Conclusión
Mediante la demostración directa y la inducción se justificó la corrección de las 4 funciones. 

$componer$ une 2 órdenes respetando su orden, $rutina$ permite construir una secuencia de avance y giro que se repite 
varias veces, $aplicarA$ usa recursión sobre la lista para ejecutar una misma orden en diferentes estados y 
$transformar$ emplea recursión estructural para modificar las órdenes básicas, conservando la 
estructura de las compuestas

Por tanto, las 4 funciones cumplen sus especificaciones siempre que estas terminen y produzcan los resultados esperados
