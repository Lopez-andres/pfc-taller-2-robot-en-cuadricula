package taller

/**
 * Taller 2 — órdenes para un robot en una cuadrícula infinita.
 *
 * El tipo `Orden` es recursivo: `Repetir` y `Secuencia` contienen otras
 * órdenes. Toda función que lo recorra lo es también, con un caso por cada
 * alternativa del tipo.
 *
 * Tal como está, el proyecto compila y las pruebas quedan en rojo.
 */
class RobotCuadricula {

  /** El robot empieza en el origen mirando al norte. */
  val inicial = Estado(0, 0, Norte)

  // Punto 1 -------------------------------------------------------------------

  /** La dirección que queda tras girar noventa grados hacia el sentido dado. */
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

  // Punto 2 -------------------------------------------------------------------

  /** El estado que resulta de aplicarle la orden al estado dado. */

  // Recibe un estado y una orden, y devuelve el nuevo estado
  def ejecutar(e: Estado, o: Orden): Estado = o match{
    // Caso 1: Mueve el robot según la dirección en la que mira
    case Avanzar(n) =>
      e.mira match{
        // Si mira al Norte, aumenta la coordenada y
        case Norte => e.copy(y = e.y + n)
        // Si mira al Sur, disminuye la coordenada y
        case Sur => e.copy(y = e.y - n)
        // Si mira al Este, aumenta la coordenada x
        case Este => e.copy(x = e.x + n)
        // Si mira al Oeste, disminuye la coordenada x
        case Oeste => e.copy(x = e.x - n)
      }
    // Caso 2: Cambia la dirección del robot sin moverlo
    case Girar(sentido) => e.copy(mira = girar(e.mira,sentido))
    // Caso 3: Ejecuta la misma orden varias veces
    case Repetir(veces, orden) =>
      @annotation.tailrec
      // Función auxiliar que guarda las repeticiones pendientes
      // y el estado actualizado después de cada ejecución
      def repetir(restantes:Int, actual: Estado): Estado = {
        // Caso base: Si no quedan repeticiones, devuelve el estado actual
        if(restantes <= 0){
          actual
        }
        // Ejecuta la orden, actualiza el estado y resta una repetición
        else{
          repetir(restantes -1, ejecutar(actual, orden))
        }
      }
      // Comienza con las repeticiones indicadas y el estado inicial
      repetir(veces,e)

    // Caso 4: Ejecuta una lista de órdenes en el orden recibido
    case Secuencia(orden) =>
      @annotation.tailrec
      // Recorre las órdenes pendientes y conserva el estado actualizado
      def recorre(pendiente: List[Orden],
                  actual: Estado): Estado = pendiente match {
        // Separa la primera orden (x) del resto de la lista (xs)
        case x :: xs => recorre(xs, ejecutar(actual,x))
        // Caso base: Cuando la lista esta vacía, termina
        case Nil => actual
      }
      // Comienza el recorrido con lalista y el estado inicial
      recorre(orden,e)

  }

  // Punto 3 -------------------------------------------------------------------

  /** Cuántos Avanzar y Girar llega a ejecutar la orden, contando repeticiones. */
  def cuantasBasicas(o: Orden): Int = o match {
      case Avanzar(_) => 1
      case Girar(_) => 1
      case Repetir(veces, orden) => if (veces <= 0) 0 else veces * cuantasBasicas(orden)
      case Secuencia(ordenes) => ordenes match {
        case Nil => 0
        case x :: xs => cuantasBasicas(x) + cuantasBasicas(Secuencia(xs))
      }
  }

  /** Niveles de anidamiento: una básica tiene 1, una compuesta uno más que la más honda. */
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

  // Punto 4 -------------------------------------------------------------------

  /** La secuencia de las dos órdenes. */
  def componer(o1: Orden, o2: Orden): Orden =
    Secuencia(List(o1, o2))

  /** Currificada: fijados los pasos y el giro, queda una función de las veces. */
  def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
    Repetir(veces, componer(Avanzar(pasos), Girar(giro)))

  /** La misma orden ejecutada sobre cada uno de los estados. */
  def aplicarA(estados: List[Estado], o: Orden): List[Estado] =
    estados match {
      case Nil =>
        Nil
      case cabeza :: cola =>
        ejecutar(cabeza, o) :: aplicarA(cola,o)
    }

  /** Reemplaza cada orden básica por f(orden), respetando la estructura de las compuestas. */
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

  // Punto 5 -------------------------------------------------------------------

  /** Una orden equivalente más corta, con las reglas del enunciado. */
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
      def simplificarLista(pendientes: List[Orden]): List[Orden] = {
        pendientes match {
          case Nil =>
            Nil
          case cabeza :: cola =>
            val primera = simplificar(cabeza)
            val resto = simplificarLista(cola)
            primera match {
              case Secuencia(Nil) =>
                resto
              case Avanzar(n) =>
                resto match {
                  case Avanzar(m) :: siguiente =>
                    simplificarLista(Avanzar(n + m) :: siguiente)
                  case _ =>
                    Avanzar(n) :: resto
                }
              case otra =>
                otra :: resto
            }
        }
      }
      val resultado = simplificarLista(ordenes)

      resultado match {
        case Nil =>
          Secuencia(Nil)
        case cabeza :: Nil =>
          cabeza
        case _ =>
          Secuencia(resultado)
      }

    }
  }