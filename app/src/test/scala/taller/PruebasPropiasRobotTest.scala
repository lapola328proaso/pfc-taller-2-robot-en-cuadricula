package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PruebasPropiasRobotTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  // ---------- Punto 1: girar ----------

  test("girar: Este a la derecha da Sur") {
    assert(girar(Este, Derecha) == Sur)
  }

  test("girar: Sur a la derecha da Oeste") {
    assert(girar(Sur, Derecha) == Oeste)
  }

  test("girar: Este a la izquierda da Norte") {
    assert(girar(Este, Izquierda) == Norte)
  }

  test("girar: Oeste a la izquierda da Sur") {
    assert(girar(Oeste, Izquierda) == Sur)
  }

  test("girar: izquierda deshace la derecha en cualquier direccion") {
    val todas = List(Norte, Este, Sur, Oeste)
    assert(todas.forall(d => girar(girar(d, Derecha), Izquierda) == d))
  }

  // ---------- Punto 2: ejecutar ----------

  test("ejecutar: repetir con veces negativas no cambia el estado") {
    val e = Estado(3, 3, Oeste)
    assert(ejecutar(e, Repetir(-2, Avanzar(5))) == e)
  }

  test("ejecutar: avanzar hacia el este aumenta x") {
    assert(ejecutar(Estado(2, 3, Este), Avanzar(5)) == Estado(7, 3, Este))
  }

  test("ejecutar: avanzar hacia el sur disminuye y") {
    assert(ejecutar(Estado(1, 1, Sur), Avanzar(4)) == Estado(1, -3, Sur))
  }

  test("ejecutar: secuencia con giros a la izquierda") {
    val o = Secuencia(List(Avanzar(1), Girar(Izquierda), Avanzar(2), Girar(Izquierda), Avanzar(3)))
    assert(ejecutar(inicial, o) == Estado(-2, -2, Sur))
  }

  test("ejecutar: repetir una secuencia tres veces") {
    val o = Repetir(3, Secuencia(List(Avanzar(2), Girar(Izquierda))))
    assert(ejecutar(inicial, o) == Estado(-2, 0, Este))
  }

  // ---------- Punto 3: cuantasBasicas y profundidad ----------

  test("cuantasBasicas: secuencia vacia no tiene ordenes basicas") {
    assert(cuantasBasicas(Secuencia(Nil)) == 0)
  }

  test("cuantasBasicas: repetir multiplica las basicas de la secuencia") {
    val o = Repetir(3, Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha))))
    assert(cuantasBasicas(o) == 9)
  }

  test("cuantasBasicas: repeticiones anidadas se multiplican") {
    assert(cuantasBasicas(Repetir(2, Repetir(3, Avanzar(1)))) == 6)
  }

  test("profundidad: dos repetir anidados dan 3") {
    assert(profundidad(Repetir(2, Repetir(3, Avanzar(1)))) == 3)
  }

  test("profundidad: toma la rama mas honda de la secuencia") {
    val o = Secuencia(List(Avanzar(1), Secuencia(List(Girar(Izquierda), Repetir(2, Avanzar(1))))))
    assert(profundidad(o) == 4)
  }

  // ---------- Punto 4: funciones de alto orden ----------

  test("componer: arma la secuencia de las dos ordenes en ese orden") {
    assert(componer(Avanzar(1), Girar(Izquierda)) == Secuencia(List(Avanzar(1), Girar(Izquierda))))
  }

  test("rutina: fija pasos y giro y espera las veces") {
    val esperada = Repetir(3, Secuencia(List(Avanzar(2), Girar(Izquierda))))
    assert(rutina(2)(Izquierda)(3) == esperada)
  }

  test("rutina: ejecutarla deja al robot donde se espera") {
    assert(ejecutar(inicial, rutina(2)(Derecha)(2)) == Estado(2, 2, Sur))
  }

  test("aplicarA: ejecuta la orden sobre cada estado y respeta la lista vacia") {
    val estados = List(Estado(1, 1, Este), Estado(0, 0, Oeste))
    assert(aplicarA(estados, Avanzar(3)) == List(Estado(4, 1, Este), Estado(-3, 0, Oeste)))
    assert(aplicarA(Nil, Avanzar(3)) == Nil)
  }

  test("transformar: cambia los giros y deja el resto igual") {
    val o = Secuencia(List(Avanzar(1), Repetir(2, Girar(Derecha))))
    val resultado = transformar(o, {
      case Girar(_) => Girar(Izquierda)
      case otra     => otra
    })
    assert(resultado == Secuencia(List(Avanzar(1), Repetir(2, Girar(Izquierda)))))
  }

  // ---------- Punto 5: simplificar ----------

  test("simplificar: avances que se cancelan dejan la secuencia vacia") {
    assert(simplificar(Secuencia(List(Avanzar(4), Avanzar(-4)))) == Secuencia(Nil))
  }

  test("simplificar: secuencia de un solo elemento se vuelve ese elemento") {
    assert(simplificar(Secuencia(List(Girar(Derecha)))) == Girar(Derecha))
  }

  test("simplificar: junta avances aunque uno venga en una secuencia anidada") {
    val o = Secuencia(List(Avanzar(1), Secuencia(List(Avanzar(2)))))
    assert(simplificar(o) == Avanzar(3))
  }

  test("simplificar: simplifica dentro de un repetir") {
    val o = Repetir(3, Secuencia(List(Avanzar(1), Avanzar(1))))
    assert(simplificar(o) == Repetir(3, Avanzar(2)))
  }

  test("simplificar: no cambia el estado final del robot") {
    val o = Secuencia(List(
      Avanzar(2), Avanzar(0), Repetir(1, Girar(Izquierda)), Avanzar(3), Avanzar(-1)
    ))
    assert(simplificar(o) == Secuencia(List(Avanzar(2), Girar(Izquierda), Avanzar(2))))
    assert(ejecutar(inicial, simplificar(o)) == ejecutar(inicial, o))
  }
}