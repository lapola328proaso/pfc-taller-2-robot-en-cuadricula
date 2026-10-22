package taller

/**
 * Taller 2 - ordenes para un robot en una cuadricula infinita.
 */
class RobotCuadricula {

  /** El robot empieza en el origen mirando al norte. */
  val inicial = Estado(0, 0, Norte)

  // Punto 1 -------------------------------------------------------------------

  /** La direccion que queda tras girar noventa grados hacia el sentido dado. */
  def girar(d: Direccion, s: Sentido): Direccion = (d, s) match {
    case (Norte, Derecha)   => Este
    case (Este, Derecha)    => Sur
    case (Sur, Derecha)     => Oeste
    case (Oeste, Derecha)   => Norte
    case (Norte, Izquierda) => Oeste
    case (Oeste, Izquierda) => Sur
    case (Sur, Izquierda)   => Este
    case (Este, Izquierda)  => Norte
  }

  // Punto 2 -------------------------------------------------------------------

  /** El estado que resulta de aplicarle la orden al estado dado. */
  def ejecutar(e: Estado, o: Orden): Estado = o match {
    case Avanzar(n) =>
      e.mira match {
        case Norte => e.copy(y = e.y + n)
        case Este  => e.copy(x = e.x + n)
        case Sur   => e.copy(y = e.y - n)
        case Oeste => e.copy(x = e.x - n)
      }
    case Girar(s) => e.copy(mira = girar(e.mira, s))
    case Repetir(veces, _) if veces <= 0 => e
    case Repetir(veces, orden) =>
      ejecutar(ejecutar(e, orden), Repetir(veces - 1, orden))
    case Secuencia(ordenes) =>
      ordenes.foldLeft(e)((acc, x) => ejecutar(acc, x))
  }

  // Punto 3 -------------------------------------------------------------------

  /** Cuantos Avanzar y Girar llega a ejecutar la orden, contando repeticiones. */
  def cuantasBasicas(o: Orden): Int = o match {
    case Avanzar(_) => 1
    case Girar(_)   => 1
    case Repetir(veces, _) if veces <= 0 => 0
    case Repetir(veces, orden) => veces * cuantasBasicas(orden)
    case Secuencia(ordenes) => ordenes.map(x => cuantasBasicas(x)).sum
  }

  /** Niveles de anidamiento: una basica tiene 1, una compuesta uno mas que la mas honda. */
  def profundidad(o: Orden): Int = o match {
    case Avanzar(_) => 1
    case Girar(_)   => 1
    case Repetir(_, orden) => 1 + profundidad(orden)
    case Secuencia(ordenes) =>
      1 + ordenes.map(x => profundidad(x)).foldLeft(0)((a, b) => math.max(a, b))
  }

  // Punto 4 -------------------------------------------------------------------

  /** La secuencia de las dos ordenes. */
  def componer(o1: Orden, o2: Orden): Orden = Secuencia(List(o1, o2))

  /** Currificada: fijados los pasos y el giro, queda una funcion de las veces. */
  def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
    Repetir(veces, Secuencia(List(Avanzar(pasos), Girar(giro))))

  /** La misma orden ejecutada sobre cada uno de los estados. */
  def aplicarA(estados: List[Estado], o: Orden): List[Estado] =
    estados.map(e => ejecutar(e, o))

  /** Reemplaza cada orden basica por f(orden), respetando la estructura de las compuestas. */
  def transformar(o: Orden, f: Orden => Orden): Orden = o match {
    case Repetir(veces, orden) => Repetir(veces, transformar(orden, f))
    case Secuencia(ordenes)    => Secuencia(ordenes.map(x => transformar(x, f)))
    case basica                => f(basica)
  }

  // Punto 5 -------------------------------------------------------------------

  /** Una orden equivalente mas corta, con las reglas del enunciado. */
  def simplificar(o: Orden): Orden = {

    def nada(x: Orden): Boolean = x match {
      case Secuencia(Nil) => true
      case Avanzar(0)     => true
      case _              => false
    }

    def juntar(os: List[Orden]): List[Orden] = os match {
      case Avanzar(a) :: Avanzar(b) :: resto => juntar(Avanzar(a + b) :: resto)
      case x :: resto                        => x :: juntar(resto)
      case Nil                               => Nil
    }

    o match {
      case Avanzar(0) => Secuencia(Nil)
      case Repetir(veces, _) if veces <= 0 => Secuencia(Nil)
      case Repetir(1, orden) => simplificar(orden)
      case Repetir(veces, orden) =>
        simplificar(orden) match {
          case s if nada(s) => Secuencia(Nil)
          case s            => Repetir(veces, s)
        }
      case Secuencia(ordenes) =>
        juntar(ordenes.map(x => simplificar(x)).filterNot(x => nada(x))).filterNot(x => nada(x)) match {
          case Nil      => Secuencia(Nil)
          case x :: Nil => x
          case xs       => Secuencia(xs)
        }
      case otra => otra
    }
  }
}
