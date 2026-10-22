
# Taller 2. Un robot en la cuadrícula - Informe de corrección

Asignatura de Fundamentos de Programación Funcional y Concurrente.
Documento realizado por estudiantes

## 1. Argumentar la corrección de programas recursivos

Sea $f : A \to B$ una función, y $A$ un conjunto definido recursivamente. Sea $P_f$ un programa recursivo desarrollado
en Scala hecho para calcular $f$. Argumentar $P_f$ es correcto con respecto a su especificación es demostrar el
siguiente teorema:

$$
\forall a \in A : P_f(a) == f(a)
$$

Como $A$ está definido recursivamente, la demostración usa inducción: se prueba cada valor básico de $A$ y,
para cada valor construido a partir de otros, se supone la hipótesis de inducción (HI), es decir, que
$P_f$ es correcto sobre esos otros valores.

1. **Caso base:** se prueba la igualdad para cada valor básico de $A$.
2. **Casos de inducción** para cada valor construido a partir de otros se supone la hipótesis de inducción (HI), es decir, que $P_f$ es correcto sobre esos otros valores, y se prueba la igualdad.

Notación: $P(x) \rightarrow e$  indica un paso de evaluación del programa según el modelo de sustitución.

**Tipos usados en el taller.**

* $\text{Orden}$ tiene órdenes básicas $\texttt{Avanzar}(n)$ y $\texttt{Girar}(s)$, y órdenes compuestas $\texttt{Repetir}(k, o)$ 
y $\texttt{Secuencia}(\text{List}(o_1, \ldots, o_n))$ (la lista puede estar vacía).
* $\texttt{Estado}$ es $(x, y, \mathit{mira})$: posición y dirección.


## 2. Punto 4. `aplicarA`  y `transformar`

### 2.1 Fución `aplicarA`

#### Especificación

Sea $f_{\mathrm{apl}} : \text{List}[\text{Estado}] \times \text{Orden} \to \text{List}[\text{Estado}]$ la función que ejecuta
la misma orden sobre cada estado de la lista: para $L = \text{List}(e_1, \ldots, e_n)$,

$$
  f_{\mathrm{apl}}(L, o) = \text{List}(\texttt{ejecutar}(e_1, o),\ \ldots, \ \texttt{ejecutar}(e_n, o))
$$

Y sea $P_{\mathrm{apl}}$ el siguiente programa en Scala:

#### Programa

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

vamos a demostrar que:

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

**Caso inducción.** $n =k + 1$, $k \geq 0$, Con $\texttt{cabeza} = e_1$ y $\texttt{cola} = \text{List}(e_2, \ldots, e_{k+1})$,
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



### 2.2 Función `transformar`

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
\forall o \in \text{Orden}, \ \forall g : \text{Orden} \to \text {Orden} : P_{\mathrm{tra}}(o, g) == T_g(o)
$$

El tipo $\text{Orden}$, se define recursivamente, así que la demostración es por **inducción estructural** sobre $o$: las 
órdenes básicas son $\texttt{Avanzar}(n)$ y $\texttt{Girar}(s)$; las compuestas son $\texttt{Repetir}(k, o')$ y $\texttt{Secuencia}(\text{List}(o_1, \ldots, o_n))$,
con $o', o_1, \ldots, o_n \in \text{Orden}$ (la lista puede estar vacía).

`Secuencia` no llama a `transformar` sobre la lista, sino a la auxiliar `transformarLista`. Por eso se necesita este lema.

**Lema auxiliar (`transformarLista).`**
Sea $L= \text{List}(o_1, \ldots, o_n)$ tal que $P_{\mathrm{tra}}(o_i, g) == T_g(o_i)$ para todo $i$. 

Entonces:

$$ 
\texttt{transformarLista}(L) == \text{List}(T_g(o_1,\ \ldots,\ T_g(o_n))
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

**Caso `Repetir(k, o')`:** por la Hipótesis, $P_{\mathrm{tra}}(o', g) == T_g(o')$, así que $/texttt{Repetir}(k, P_{\mathrm{trat}}
(o', g)) = \texttt{Repetir}(k, T_g(o')) = T_g(\texttt{Repetir}(k, o'))$.

**Caso `Secuencia`:** por la HI cada $o_i$ cumple la suposición del lema, así que `transformarLista` da $\text{List}(T_g(o_1), 
\ldots, T_g(o_n))$ y el resultado es $T_g(o)$.

Concluimos por inducción estructural que $P_{\mathrm{tra}}(o, g) == T_g(o)$.


## 3. Punto 5. `simplificar`

### 3.1 Especificación y programa

Sea $\mathcal{E}[o] : \text{Estado} \to \text{Estado}$ la función que calcula lo que hace la orden $o$, es decir
$\mathcal{E}[o](e) = \texttt{ejecutar}(e, o)$, Sea $P_{\mathrm{sim}}$ el siguiente programa en Scala, que recibe 
una orden y devuelve otra orden equivalente:

```scala
def simplificar(o: Orden): Orden = o match {

    case Avanzar(0) =>
      Secuencia(Nil)

    case Avanzar(n) =>
      Avanzar(n)

    case Girar(sentido) =>
      Girar(sentido)

    case Repetir(veces, orden) =>
      if (veces <= 0) {
        Secuencia(Nil)
      } else if (veces == 1) {
        simplificar(orden)
      } else {
        val simplificada = simplificar(orden)

        simplificada match {
          case Secuencia(Nil) =>
            Secuencia(Nil)

          case _ =>
            Repetir(veces, simplificada)
        }
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

### 3.2 Semántica de `ejecutar`

Sea $\delta$ el desplazamiento de una unidad según la dirección:
$\delta(\text{Norte}) = (0, 1)$, $\delta(\text{Este}) = (1, 0)$,
$\delta(\text{Sur}) = (0, -1)$ y $\delta(\text{Oeste}) =(-1, 0)$.

Leyendo el código de `ejecutar` se obtienen estas ecuaciones. En ellas $(x, y, \mathit{mira})$ es un estado cualquiera,
como en `Estado(x, y, mira)`: posición $(x, y)$ y dirección $\mathit{mira}$. Además, `0` y `p` son órdenes.

$$
\begin{aligned}
\mathcal{E}[\texttt{Avanzar}(n)](x, y, \mathit{mira}) &= (x + n\,\delta_x(\mathit{mira}),\ y + 
n\,\delta_y(\mathit{mira}),\ \mathit{mira}) \\
\mathcal{E}[\texttt{Girar}(s)](x, y, \mathit{mira}) &= (x, y, \texttt{girar}(\mathit{mira}, s))
\end{aligned}
$$

La función `girar` tiene su propia firma, `girar(d: Dirección, s: Sentido): Dirección`,
con parámetros `d` y `s`. En la ecuación de arriba se llama con la dirección del estado (`mira`) y 
el sentido de la orden `Girar(s)`, igual que hace el código de `ejecutar`: `Estado (e.x, e.y, girar(e.mira, sentido))`.

$$
\mathcal{E}[\texttt{Repetir}(k, o)]= \mathcal{E}[o]^{k}
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

**`Secuencia`.** El auxiliar `ejecutarLista(estado, cabeza:: cola)`, devuelve `ejecutarLista(ejecutar(estado, cabeza), cola)`.

Es decir, ejecutar `cabeza` y sigue con el resto, que es exactamente $\mathcal{E}[\texttt{Secuencia}(\mathit{cola})] \circ \mathcal{E}[\mathit{cabeza}]$.
Con `Nil` no hace nada.

**Por qué vale la ecuación de `Repetir`.** El auxiliar `repetir(restante, estado)` hace:
* si `restantes <= 0`, devuelve `estado`.
* si no, devuelve `repetir(restantes -1, ejecutar(estado, orden))`.

*Afirmación:* `repetir(r, e)` $= \mathcal{E}[o]^{r}(e)$. Se prueba por inducción sobre $r$.

* **Caso base,** $r \leq 0$: devuelve $e$, que es $\mathcal{E}[o]^{r}(e)$ porque  $f^{r} = \mathrm{id}$
si $r \leq 0$.

* **Caso de inducción,** $r > 0$. La hipótesis es que `repetir(r - 1, e')` $=\mathcal{E}[o]^{r-1}(e')$ para todo $e'$. Entonces  
    `repetir(r, e)` $=$ `repetir(r - 1,` $\mathcal{E}[o]{(e)}$`)` $=\mathcal{E}[o]^{r-1}(\mathcal{E}[o](e)) =\mathcal{E}[o]^{r}(e)$.

**Nota sobre el tamaño de los enteros.** Las coordenadas son `Int`de 32 bits. Con valores normales no hay desbordamiento, si algún valor se pasara 
del máximo, el resultado "daría la vuelta", lo cual equivale a hacer las cuentas módulo 2^32. En esa aritmética la suma sigue siendo 
asociativa y conmutativa, así que las igualdades de este informe valen también en ese caso extremo.

### 3.3 Lemas de equivalencia

**Lema A (suma de avances).** $\mathcal{E}[\texttt{Avanzar}(m)] \circ \mathcal{E}[\texttt{Avanzar}(n)] = \mathcal{E}[\texttt{Avanzar}(n + m)]$.

`Avanzar` no cambia $\mathit{mira}$, así que el segundo avance parte de la misma dirección. Desde $(x, y, \mathit{mira})$:
$$
x + n\,\delta_x(\mathit{mira}) + m\,\delta_x(\mathit{mira}) = x + (n + m)\,\delta_x(\mathit{mira})
$$
Lo mismo con $y$. $\square$ Si hubiera un `Girar` en medio no valdría, y por eso el código solo junta avances contiguos.

**Lema Z(órdenes que no hacen nada).**

1. $\mathcal{E}[\texttt{Avanzar}(0)] = \mathrm{id}$, porque suma $0 \cdot\delta =(0,0)$.
2. $\mathcal{E}[\texttt{Secuencia}(\texttt{Nil})] = \mathrm{id}$, por su ecuación.
3. Si $k \leq 0$, $\mathcal{E}[\texttt{Repetir}(k, o)] =\mathcal{E}[o]^{k} = \mathrm{id}$.
4. Si $\mathcal{E}[o] = \mathrm{id}$, entonces $\mathcal{E}[\texttt{Repetir}(k, o)] = \mathrm{id}^{k} = \mathrm{id}$ para todo $k$

**Lema U (secuencia de elementos).** $\mathcal{E}[\texttt{Secuencia}(p :: \texttt{Nil})] =\mathcal{E}[\texttt{Secuencia}(\texttt{Nil})]
\circ \mathcal{E}[p] = \mathrm{id} \circ \mathcal{E}[p] =\mathcal{E}[p]$


### 3.4 Medida y terminación

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
| `Avanzar(0)` | 1 | 1 (`Secuencia(Nil)`) |
| `Avanzar(n)`, `Girar(s)` | 1 | 1 |
| `Repetir(k , o)` con $k \leq 0$ | $1 + t(o)$ | 1 |
| `Repetir(1, o)` | $1 + t(o)$ | $t(\texttt{simplificar}(o)) \leq t(o)$ (hipótesis) |
| `Repetir(k, o)`, $k \geq 2$ | $1 + t(o)$ | 1 , o $1 + t(\texttt{simplificar}(o)) \leq 1 + t(o)$ (hipótesis) |
| Lista, `cabezaSimplificada` es `Secuencia(Nil)` | $t(\text{cabeza}) + T(\text{cola})$ | $T(\text{colaSimplificada}) \leq T(\text{cola})$ (hipótesis)|
| Lista, `cabezaSimplificada` no se junta | $t(\text{cabeza}) + T(\text{cola})$ | $t(\text{cabezaSimplificada}) + T(\text{colaSimplificada}) \leq t(\text{cabeza}) + T(\text{cola})$ (hipótesis) |
| `Secuencia(ps)`, resultado vacío, de un elemento $p$, o varias | $1 + T(ps)$ | 1, o $t(p) \leq T(\text{listaSimplificada}) \leq T(ps)$, o $1 + T(\text{listaSimplificada}) \leq 1 + T(ps)$ (hipótesis) | 

**Caso de juntar avances.** Sea `cabezaSimplificada`$= \texttt{Avanzar}(n)$ y `colaSimplificada` $=\texttt{Avanzar}(m) :: \text{siguiente}$. 
La lista que se pasa a la nueva llamada es $\texttt{Avanzar}(n + m) :: \text{siguiente}$, y su tamaño es:

$$
T(\texttt{Avanzar}(n + m) :: \text{siguiente}) = 1 + T(\text{siguiente}) = T(\text{colaSimplificada}) \leq T(\text{cola}) < T(\text{cabeza} :: \text{cola})
$$

### 3.5 Demostración de equivalencia

Para toda orden $o$ y toda lista $ps$ de órdenes se demuestra:

```math
\mathcal{E}[P_{\mathrm{sim}}(o)] == \mathcal{E}[o]
\qquad \text{y} \qquad
\mathcal{E}[\texttt{Secuencia}(\texttt{simplificarLista}(ps))] == \mathcal{E}[\texttt{Secuencia}(ps)]
```
La segunda afirmación se necesita para el caso de `Secuencia`, y las dos se demuestran juntas por **inducción bien fundada sobre 
$\mu$**: se supone que ambas valen para todo lo de medida menor (HI), lo que justifica cada "por la HI" de abajo.

**Caso base:** $o =\texttt{Avanzar}(0)$

```math
P_{\mathrm{sim}}(\texttt{Avanzar}(0)) \rightarrow \texttt{Secuencia}
(\texttt{Nil})
```

Por el Lema Z, $\mathcal{E}[\texttt{Avanzar}(0)] = \mathrm{id} = \mathcal{E}[\texttt{Secuencia}(\texttt{Nil})]$. 

**Caso base:** $o =\texttt{Avanzar}(n)$ con $n \neq 0$ y $o = \texttt{Girar}(s)$

El programa devuelve la misma orden, así que $\mathcal{E}[P_{\mathrm{sim}}(o)] = \mathcal{E}[o]$.

**Caso de inducción:** $o = \texttt{Repetir}(k, o')$

Como $t(o') < t(\texttt{Repetir}(k, o'))$, la Hipótesis es $\mathcal{E}[P_{\mathrm{sim}}(o')] = \mathcal{E}[o']$. Hay que probar 
$\mathcal{E}[p_{\mathrm{sim}}(\texttt{Repetir}(k, o'))] = \mathcal{E}[o']^{k}$. El programa distingue cuatro casos:

* $k \leq 0$: devuelve `Secuencia(Nil)`. Por el Lema Z, $\mathcal{E}[o']^{k} = \mathrm{id} = \mathcal{E}[\texttt{Secuencia}(\texttt{Nil})]$

* $k = 1$: devuelve $P_{\mathrm{sim}}(o')$. Por la Hipótesis, $\mathcal{E}[P_{\mathrm{sim}}(o')] =\mathcal{E}[o']^{1}$.

* $k \geq 2$ y $P_{\mathrm{sim}}(o') = \texttt{Secuencia}(\texttt{Nil})$: devuelve `Secuencia(Nil)`. Por la HI, $\mathcal{E}[o'] = \mathcal{E}[\texttt{Secuencia}(\texttt{Nil})] =\mathrm{id}$,
y por el Lema Z (numeral 4), $\mathcal{E}[o']^{k} =\mathrm{id}$.

* $k \geq 2$ y $P_{\mathrm{sim}}(o')$ es otra orden: devuelve $\texttt{Repetir}(k, P_{\mathrm{sim}}(o'))$, y

```math
\mathcal{E}[\texttt{Repetir}(k, P_{\mathrm{sim}}(o'))] \mathcal{E}[P_{\mathrm{sim}}(o')]^{k} = \mathcal{E}[o']^{k}
```
Por la ecuación de `Repetir` y la Hipótesis.

**Caso de inducción:** $o = \texttt{Secuencia}(ps)$

Sea $\mathit{listaSimplificada} = \texttt{SimplificarLista}(ps)$. Como $\mu(ps) = (T(ps), 1) < (1 + T(ps), 0) = \mu(o)$, 
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
\mathcal{E}[\texttt{Secuencia}(\mathit{cabezaSimplificada} :: \mathit{colaSimplificada})] = \mathcal{E}[\texttt{Secuencia}(\mathit{colaSimplificada})] = \circ \mathcal{E}[\mathit{cabezaSimplificada}] = 
\mathcal{E}[\texttt{Secuencia}(\mathit{cola})] \circ \mathcal{E}[\mathit{cabeza}]
$$
Los cuatro caminos cubren todas las posibilidades de `cabezaSimplificada` y `colaSimplificada`.

Concluimos por inducción bien fundada, sobre la medida $\mu$ que baja en cada llamada recursiva, que:

$$
\forall o \in \text{Orden}, \ \forall e \in \text{Estado} : \texttt{ejecutar}(e, \texttt{simplificar}(o)) == \texttt{ejecutar}(e, o)
$$

Lo demostrado es que `simplificar` conserva lo que hace la orden. No se afirma que el resultado no pueda
simplificarse más.

## 4. Corrección de programas iterativos
Un programa iterativo se argumenta definiendo un estado de la iteración, su estado inicial, su estado final, una invariante y la transformación
que pasa de un estado al siguiente.
`aplicarA`, `transformar` y `simplificar` son recursivos: procesan una lista o una orden con llamadas sobre la cola o sobre subórdenes, y no hay estado de iteración ni invariante de ciclo. Por eso su corrección
se argumenta con el método recursivo de la sección 1 (inducción estructural o bien fundada)
