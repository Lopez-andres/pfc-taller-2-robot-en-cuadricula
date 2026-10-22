package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RobotCuadriculaTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  val cuadrado = Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))

  // Punto 1 -------------------------------------------------------------------

  test("girar: Norte a la derecha da Este") { assert(girar(Norte, Derecha) == Este) }
  test("girar: Norte a la izquierda da Oeste") { assert(girar(Norte, Izquierda) == Oeste) }
  test("girar: Oeste a la derecha da Norte") { assert(girar(Oeste, Derecha) == Norte) }
  test("girar: Sur a la izquierda da Este") { assert(girar(Sur, Izquierda) == Este) }

  test("girar: cuatro giros al mismo lado vuelven al principio") {
    val cuatro = List(Derecha, Derecha, Derecha, Derecha)
      .foldLeft(Norte: Direccion)((d, s) => girar(d, s))
    assert(cuatro == Norte)
  }

  test("girar: un giro y su contrario se cancelan") {
    assert(girar(girar(Este, Derecha), Izquierda) == Este)
  }

  // Punto 2 -------------------------------------------------------------------

  test("ejecutar: avanzar tres al norte") {
    assert(ejecutar(inicial, Avanzar(3)) == Estado(0, 3, Norte))
  }

  test("ejecutar: una secuencia de avance, giro y avance") {
    assert(ejecutar(inicial, Secuencia(List(Avanzar(2), Girar(Derecha), Avanzar(4)))) ==
      Estado(4, 2, Este))
  }

  test("ejecutar: el cuadrado vuelve al punto de partida") {
    assert(ejecutar(inicial, cuadrado) == Estado(0, 0, Norte))
  }

  test("ejecutar: avanzar con paso negativo retrocede") {
    assert(ejecutar(inicial, Avanzar(-2)) == Estado(0, -2, Norte))
  }

  test("ejecutar: repetir cero veces no hace nada") {
    assert(ejecutar(inicial, Repetir(0, Avanzar(5))) == inicial)
  }

  test("ejecutar: la secuencia vacía no hace nada") {
    assert(ejecutar(inicial, Secuencia(Nil)) == inicial)
  }

  test("ejecutar: avanzar al sur disminuye y") {
    assert(ejecutar(Estado(5, 5, Sur), Avanzar(2)) == Estado(5, 3, Sur))
  }

  test("ejecutar: avanzar al oeste disminuye x") {
    assert(ejecutar(Estado(5, 5, Oeste), Avanzar(2)) == Estado(3, 5, Oeste))
  }

  // Punto 3 -------------------------------------------------------------------

  test("cuantasBasicas: una orden básica cuenta uno") {
    assert(cuantasBasicas(Avanzar(3)) == 1)
  }

  test("cuantasBasicas: el cuadrado ejecuta ocho básicas") {
    assert(cuantasBasicas(cuadrado) == 8)
  }

  test("cuantasBasicas: repetir cero veces no ejecuta nada") {
    assert(cuantasBasicas(Repetir(0, cuadrado)) == 0)
  }

  test("cuantasBasicas: la secuencia vacía no ejecuta nada") {
    assert(cuantasBasicas(Secuencia(Nil)) == 0)
  }

  test("profundidad: una orden básica tiene profundidad 1") {
    assert(profundidad(Avanzar(3)) == 1)
  }

  test("profundidad: una secuencia de básicas tiene profundidad 2") {
    assert(profundidad(Secuencia(List(Avanzar(1), Girar(Derecha)))) == 2)
  }

  test("profundidad: el cuadrado tiene profundidad 3") {
    assert(profundidad(cuadrado) == 3)
  }

  test("profundidad: la secuencia vacía tiene profundidad 1") {
    assert(profundidad(Secuencia(Nil)) == 1)
  }

  test("profundidad: manda la rama más honda") {
    assert(profundidad(Secuencia(List(Avanzar(1), cuadrado))) == 4)
  }

  // Punto 4 -------------------------------------------------------------------

  test("componer: deja las dos órdenes en secuencia") {
    assert(componer(Avanzar(1), Girar(Derecha)) ==
      Secuencia(List(Avanzar(1), Girar(Derecha))))
  }

  test("componer: ejecutar la compuesta es ejecutar una y después la otra") {
    assert(ejecutar(inicial, componer(Avanzar(2), Girar(Derecha))) ==
      ejecutar(ejecutar(inicial, Avanzar(2)), Girar(Derecha)))
  }

  test("rutina: currificada, el cuadrado de cuatro vuelve al origen") {
    val cuadradoDe = rutina(1)(Derecha) _
    assert(ejecutar(inicial, cuadradoDe(4)) == Estado(0, 0, Norte))
  }

  test("rutina: con los mismos argumentos construye el cuadrado del enunciado") {
    assert(rutina(1)(Derecha)(4) == cuadrado)
  }

  test("aplicarA: ejecuta la misma orden sobre varios estados") {
    assert(aplicarA(List(inicial, Estado(5, 5, Sur)), Avanzar(2)) ==
      List(Estado(0, 2, Norte), Estado(5, 3, Sur)))
  }

  test("aplicarA: sobre la lista vacía no hay nada que hacer") {
    assert(aplicarA(Nil, Avanzar(2)) == Nil)
  }
  test("aplicarA: Avanza(3) mueve cada estado según hacia dónde mira, y conserva el orden") {
    val estados = List(Estado(0, 0, Este), Estado(2, 2, Oeste), Estado(1, 1, Sur))
    assert(aplicarA(estados, Avanzar(3)) == List(Estado(3, 0, Este), Estado(-1, 2, Oeste), Estado(1, -2, Sur)))
  }
  test("aplicarA: Girar(Izquierda) cambiando solo la dirección de los cuatro estados"){
    val estados = List(Estado(0, 0, Norte), Estado(1, 1, Este), Estado(2, 2, Sur), Estado(3, 3, Oeste))
    assert(aplicarA(estados, Girar(Izquierda)) == List(Estado(0, 0, Oeste), Estado(1, 1, Norte), Estado(2, 2, Este), Estado(3, 3, Sur)))
  }
  test("aplicarA: con un solo estado aplica la orden una vez"){
    assert(aplicarA(List(Estado(2, 3, Oeste)), Girar(Derecha)) == List(Estado(2, 3, Norte)))
  }


  test("transformar: duplicar los avances deja los giros como estaban") {
    val doble = transformar(cuadrado, {
      case Avanzar(n) => Avanzar(2 * n)
      case otra => otra
    })
    assert(doble == Repetir(4, Secuencia(List(Avanzar(2), Girar(Derecha)))))
  }
  test("transformar: intercambiar básicas conserva la forma de Repetir y Secuencia"){
    val intercambia: Orden => Orden ={
      case Avanzar(_) => Girar(Derecha)
      case Girar(_) => Avanzar(1)
      case otra => otra
    }
    assert(transformar(Secuencia(List(Avanzar(5), Repetir(2, Girar(Izquierda)))), intercambia) ==
      Secuencia(List(Girar(Derecha), Repetir(2, Avanzar(1)))))
  }
  test("transformar: con la identidad no cambia nada") {
    assert(transformar(cuadrado, (o: Orden) => o) == cuadrado)
  }

  test("transformar: la función solo se aplica a las órdenes básicas, no a repetir ni a Secuencia"){
    // duplicar cada básica: Avanzar(1) pasa a Secuencia(Avanzar(1), Avanzar(1)), el repetir se queda.
    val duplicar: Orden => Orden = x => Secuencia(List(x, x))
    assert(transformar(Repetir(2, Avanzar(1)), duplicar) == Repetir(2, Secuencia(List(Avanzar(1), Avanzar(1)))))
  }

  test("transformar: duplica los avances cambia el recorrido del robot como se espera"){
    val o = Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(2)))
    val doble = transformar(o, {
      case Avanzar(n) => Avanzar(2 * n)
      case otra => otra
    })
    assert(ejecutar(inicial, o) == Estado(2, 3, Este))
    assert(ejecutar(inicial, doble) == Estado(4, 6, Este))
  }

  test("transformar: una secuencia vacía anidada queda vacía y no se le aplica la función"){
    val duplicar: Orden => Orden = x => Secuencia(List(x, x,))
    assert(transformar(Secuencia(List(Secuencia(Nil), Avanzar(1))), duplicar) ==
      Secuencia(List(Secuencia(Nil), Secuencia(List(Avanzar(1), Avanzar(1))))))
  }

  // Punto 5 -------------------------------------------------------------------

  test("simplificar: dos avances seguidos se juntan") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(3)))) == Avanzar(5))
  }

  test("simplificar: repetir una vez es la orden misma") {
    assert(simplificar(Repetir(1, Avanzar(4))) == Avanzar(4))
  }

  test("simplificar: avanzar cero desaparece de la secuencia") {
    assert(simplificar(Secuencia(List(Avanzar(0), Girar(Derecha)))) == Girar(Derecha))
  }

  test("simplificar: junta solo los avances contiguos") {
    assert(simplificar(Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3)))) ==
      Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(3))))
  }

  test("simplificar: repetir cero veces no hace nada") {
    assert(simplificar(Repetir(0, Avanzar(9))) == Secuencia(Nil))
  }

  test("simplificar: dos avances que se cancelan desaparecen") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(-2)))) == Secuencia(Nil))
  }

  test("simplificar: es idempotente") {
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Secuencia(List(Avanzar(0), Avanzar(4)))),
      Secuencia(List(Secuencia(Nil), Repetir(0, Avanzar(3)))))
    assert(ordenes.forall(o => simplificar(simplificar(o)) == simplificar(o)))
  }

  test("simplificar: no cambia el resultado de ejecutar") {
    val estados = List(inicial, Estado(3, -2, Este), Estado(-1, 4, Sur))
    val ordenes = List(cuadrado,
      Secuencia(List(Avanzar(2), Avanzar(3))),
      Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(3))),
      Repetir(1, Avanzar(4)),
      Repetir(0, Avanzar(9)),
      Secuencia(List(Avanzar(0), Girar(Derecha))),
      Secuencia(List(Avanzar(2), Avanzar(-2))),
      Repetir(3, Secuencia(List(Repetir(1, Avanzar(2)), Girar(Izquierda)))))
    assert(ordenes.forall(o =>
      estados.forall(e => ejecutar(e, o) == ejecutar(e, simplificar(o)))))
  }

  test("simplificar: el anidamiento interior también se reduce") {
    assert(simplificar(Repetir(3, Repetir(1, Avanzar(2)))) == Repetir(3, Avanzar(2)))
  }

  test("simplificar: al quitar órdenes sin valor de varios, tipos, los avances que quedan se unen en cadena"){
    val o = Secuencia(List(Avanzar(1), Repetir(0, Girar(Izquierda)),
      Secuencia(List(Avanzar(0), Secuencia(Nil))),
      Avanzar(2), Repetir(1, Avanzar(3))))
    assert(simplificar(o) == Avanzar(6))
  }

  test("simplificar: los giros nunca se juntan ni se cancelan, aunque sean seguidos u opuestos"){
    val o = Secuencia(List(Girar(Derecha), Repetir(4, Secuencia(List(Repetir(-3, Avanzar(7)), Avanzar(0)))),
      Girar(Izquierda)))
    assert(simplificar(o) == Secuencia(List(Girar(Derecha), Girar(Izquierda))))
  }

  test("simplificar: dentro de un Repetir se simplifica el cuerpo y quitar un Avance(0) junta los avances vecinos"){
    val o = Repetir(5, Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha), Avanzar(0), Avanzar(4), Avanzar(-1))))
    assert(simplificar(o) == Repetir(5, Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(3)))))
  }
  test("simplificar avances que se cancelan (suma 0) desaparecen y también los demás avances vecinos se unen"){
    val o = Secuencia(List(Avanzar(3), Secuencia(List(Avanzar(2), Avanzar(-2))), Girar(Derecha), Avanzar(1), Avanzar(-1)))
    assert(simplificar(Repetir(4, Secuencia(List(Avanzar(3), Avanzar(-3))))) == Secuencia(Nil))

  }

  test("simplificar: un Repetir de veces se conserva y su cuerpo  se simplifica") {
    val o = Secuencia(List(Repetir(2, Secuencia(List(Avanzar(1), Avanzar(1)))), Girar(Derecha)))
    assert(simplificar(o) == Secuencia(List(Repetir(2, Avanzar(2)), Girar(Derecha))))
  }

  test("simplificar: un Repartir en medio separa los avances, no se unen los de afuera"){
    val o = Secuencia(List(Avanzar(1), Repetir(2, Avanzar(1)),Avanzar(1)))
    assert(simplificar(o) == o)
  }


}
