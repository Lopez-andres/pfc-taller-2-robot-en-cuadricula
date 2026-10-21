

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