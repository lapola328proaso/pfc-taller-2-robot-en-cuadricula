# Informe de corrección: Taller 2, el robot en la cuadrícula

Fundamentos de Programación Funcional y Concurrente

En este informe explicamos por qué cada función de RobotCuadricula.scala hace lo que el enunciado pide. Seguimos la guía del docente. Para cada función primero decimos qué debería calcular (la especificación), luego mostramos el programa y por último demostramos que los dos coinciden. Como el tipo Orden está definido de forma recursiva, casi todas las demostraciones son por inducción estructural. Para los recorridos con foldLeft usamos el esquema de estado inicial, transformación, invariante y estado final.

## 0. Notación

Un estado lo escribimos como $(x, y, d)$, donde $d$ es una de las cuatro direcciones: $N$, $E$, $S$, $O$. El robot arranca en $(0, 0, N)$.

Los giros de noventa grados son:

$$
\mathrm{rot}_D:\ N \mapsto E \mapsto S \mapsto O \mapsto N
\qquad\qquad
\mathrm{rot}_I:\ N \mapsto O \mapsto S \mapsto E \mapsto N
$$

Para poder hablar de lo que "hace" una orden, le asociamos a cada orden $o$ una función $E_o$ que lleva un estado en otro. Se define por casos, siguiendo la forma de Orden:

$$
\begin{aligned}
E_{\mathrm{Avanzar}(n)}(x,y,d) &=
\begin{cases}
(x,\ y+n,\ d) & \text{si } d = N\\
(x+n,\ y,\ d) & \text{si } d = E\\
(x,\ y-n,\ d) & \text{si } d = S\\
(x-n,\ y,\ d) & \text{si } d = O
\end{cases}\\[6pt]
E_{\mathrm{Girar}(s)}(x,y,d) &= (x,\ y,\ \mathrm{rot}_s(d))\\[6pt]
E_{\mathrm{Repetir}(k,\,o)} &= (E_o)^k\\[6pt]
E_{\mathrm{Secuencia}([o_1,\ldots,o_m])} &= E_{o_m} \circ \cdots \circ E_{o_1}
\end{aligned}
$$

Aquí $(E_o)^k$ es aplicar $E_o$ seguido $k$ veces: si $k \le 0$ es la identidad, y si $k \ge 1$ es $(E_o)^{k-1} \circ E_o$. En la secuencia, la composición se lee de derecha a izquierda, o sea que primero actúa $o_1$. Si la secuencia es vacía, la composición es la identidad.

Decimos que dos órdenes son **equivalentes**, y lo escribimos $o \equiv o'$, cuando $E_o = E_{o'}$. Dicho en palabras: desde cualquier estado inicial dejan al robot en el mismo estado final.

Hay dos hechos sencillos que vamos a usar varias veces:

- **(O1)** $E_{\mathrm{Avanzar}(0)} = \mathrm{id}$. En los cuatro casos se suma o se resta $0$.
- **(O2)** $E_{\mathrm{Avanzar}(b)} \circ E_{\mathrm{Avanzar}(a)} = E_{\mathrm{Avanzar}(a+b)}$. Avanzar no cambia la dirección, así que los dos avances van hacia el mismo lado y los desplazamientos se suman.

## 1. Punto 1: girar

**Qué debería calcular.** $\mathrm{girar}(d, s) = \mathrm{rot}_s(d)$ para toda dirección $d$ y todo sentido $s$.

**El programa.** girar hace match sobre la pareja (d, s) y tiene ocho casos, uno por cada pareja posible.

**Por qué es correcto.** Las entradas posibles son pocas: cuatro direcciones por dos sentidos. Como Direccion y Sentido son sealed, los ocho casos del match cubren todas las parejas sin repetir ninguna. Basta comparar cada caso con la definición de $\mathrm{rot}$:

| $(d, s)$ | resultado del programa | $\mathrm{rot}_s(d)$ |
|---|---|---|
| $(N, D)$ | $E$ | $E$ |
| $(E, D)$ | $S$ | $S$ |
| $(S, D)$ | $O$ | $O$ |
| $(O, D)$ | $N$ | $N$ |
| $(N, I)$ | $O$ | $O$ |
| $(O, I)$ | $S$ | $S$ |
| $(S, I)$ | $E$ | $E$ |
| $(E, I)$ | $N$ | $N$ |

Las dos columnas coinciden en las ocho filas, así que girar es correcta. $\blacksquare$

## 2. Punto 2: ejecutar

**Qué debería calcular.** Para todo estado $e$ y toda orden $o$:

$$
\mathrm{ejecutar}(e, o) = E_o(e)
$$

**Demostración.** Vamos por inducción estructural sobre $o$. Hay un caso base por cada orden básica y un caso de inducción por cada orden compuesta.

*Caso base, $o = \mathrm{Avanzar}(n)$.* El programa hace match sobre e.mira y cambia $y$ o $x$ según la dirección: suma $n$ a $y$ al norte, suma $n$ a $x$ al este, resta $n$ a $y$ al sur y resta $n$ a $x$ al oeste, sin tocar lo demás. Eso es justamente $E_{\mathrm{Avanzar}(n)}(e)$.

*Caso base, $o = \mathrm{Girar}(s)$.* El programa devuelve e.copy(mira = girar(e.mira, s)). Por el punto 1, girar(e.mira, s) es $\mathrm{rot}_s(d)$, y $x$ e $y$ no cambian. Es $E_{\mathrm{Girar}(s)}(e)$.

*Caso inductivo, $o = \mathrm{Repetir}(k, o')$.* La hipótesis de inducción nos dice que $\mathrm{ejecutar}(e', o') = E_{o'}(e')$ para cualquier estado $e'$. Con eso, probamos por inducción sobre $k$ esta afirmación:

$$
Q(k):\quad \forall e',\quad \mathrm{ejecutar}(e', \mathrm{Repetir}(k, o')) = (E_{o'})^k(e')
$$

- Si $k \le 0$, la guarda de Repetir devuelve $e'$ sin hacer nada, y $(E_{o'})^k$ es la identidad. Vale $Q(k)$. Este es el caso en que la repetición se detiene.
- Si $k \ge 1$, el programa ejecuta una vez la orden y repite $k-1$ veces. Por la hipótesis de inducción, ejecutar una vez da $E_{o'}(e')$. Por $Q(k-1)$, las $k-1$ repeticiones siguientes dan $(E_{o'})^{k-1}(E_{o'}(e')) = (E_{o'})^k(e')$. Vale $Q(k)$.

*Caso inductivo, $o = \mathrm{Secuencia}([o_1, \ldots, o_m])$.* Aquí el programa es un foldLeft sobre la lista, así que lo explicamos con el esquema del recorrido:

- **Estado inicial:** $acc_0 = e$.
- **Transformación:** en el paso $i+1$ se hace $acc_{i+1} = \mathrm{ejecutar}(acc_i, o_{i+1})$.
- **Invariante:** después de procesar $i$ órdenes, con $0 \le i \le m$,

$$
acc_i = (E_{o_i} \circ \cdots \circ E_{o_1})(e)
$$

- **El invariante vale al empezar:** con $i = 0$ la composición está vacía, o sea que es la identidad, y $acc_0 = e$.
- **El invariante se conserva:** supongamos que vale para $i$. Cada $o_{i+1}$ es una parte de $o$, así que la hipótesis de inducción da $\mathrm{ejecutar}(acc_i, o_{i+1}) = E_{o_{i+1}}(acc_i)$, que es $(E_{o_{i+1}} \circ E_{o_i} \circ \cdots \circ E_{o_1})(e)$. Vale para $i+1$.
- **Estado final:** la lista tiene $m$ órdenes, así que el recorrido termina en $m$ pasos con $acc_m = (E_{o_m} \circ \cdots \circ E_{o_1})(e) = E_o(e)$.

Con esto quedan cubiertas las cuatro alternativas de Orden. $\blacksquare$

Además la recursión termina: en Repetir el número $k$ baja de uno en uno hasta llegar a $0$, y en los demás casos las llamadas se hacen sobre órdenes más pequeñas.

## 3. Punto 3: cuantasBasicas y profundidad

### cuantasBasicas

**Qué debería calcular.** La cantidad de órdenes Avanzar y Girar que se llegan a ejecutar, contando las repeticiones. La escribimos $C(o)$:

$$
\begin{aligned}
C(\mathrm{Avanzar}(n)) &= 1\\
C(\mathrm{Girar}(s)) &= 1\\
C(\mathrm{Repetir}(k, o')) &= \max(k, 0) \cdot C(o')\\
C(\mathrm{Secuencia}([o_1,\ldots,o_m])) &= \textstyle\sum_{i=1}^{m} C(o_i)
\end{aligned}
$$

**Demostración** de que $\mathrm{cuantasBasicas}(o) = C(o)$, por inducción estructural:

- Avanzar y Girar: el programa devuelve $1$, igual que la definición.
- $\mathrm{Repetir}(k, o')$: si $k \le 0$ la guarda devuelve $0$, que es $\max(k,0) \cdot C(o')$. Si $k > 0$ el programa devuelve veces por cuantasBasicas(orden), que por la hipótesis de inducción es $k \cdot C(o')$.
- $\mathrm{Secuencia}(os)$: el programa hace map y luego sum. Por la hipótesis de inducción, el map produce la lista $[C(o_1), \ldots, C(o_m)]$, y sum suma sus elementos. La suma de la lista vacía es $0$, que es lo que da la fórmula con $m = 0$. $\blacksquare$

### profundidad

**Qué debería calcular.** Los niveles de anidamiento: una orden básica tiene $1$, y una compuesta tiene uno más que su parte más honda. La escribimos $P(o)$:

$$
\begin{aligned}
P(\mathrm{Avanzar}(n)) &= P(\mathrm{Girar}(s)) = 1\\
P(\mathrm{Repetir}(k, o')) &= 1 + P(o')\\
P(\mathrm{Secuencia}([o_1,\ldots,o_m])) &= 1 + \max\bigl(\{0\} \cup \{P(o_i)\}_{i=1}^{m}\bigr)
\end{aligned}
$$

**Demostración** de que $\mathrm{profundidad}(o) = P(o)$, por inducción estructural:

- Las básicas devuelven $1$. Coincide.
- $\mathrm{Repetir}(k, o')$: el programa devuelve 1 más profundidad(orden), que por hipótesis de inducción es $1 + P(o')$. El valor de $k$ no influye.
- $\mathrm{Secuencia}(os)$: el programa calcula 1 más el máximo de las profundidades de la lista, y lo hace con un foldLeft que empieza en $0$. Por la hipótesis de inducción, el map entrega $[P(o_1), \ldots, P(o_m)]$. El recorrido es:
    - **Estado inicial:** $acc_0 = 0$.
    - **Transformación:** $acc_{i+1} = \max(acc_i, P(o_{i+1}))$.
    - **Invariante:** $acc_i = \max(\{0\} \cup \{P(o_j)\}_{j \le i})$.
    - **Estado final:** $acc_m = \max(\{0\} \cup \{P(o_j)\}_{j \le m})$.

  El invariante vale al empezar, porque $\max(\{0\}) = 0$, y se conserva porque $\max(\max(A), b) = \max(A \cup \{b\})$. Al final el programa suma $1$ y obtiene $P(o)$. Para la secuencia vacía da $1 + 0 = 1$. $\blacksquare$

## 4. Punto 4: componer, rutina, aplicarA y transformar

### componer

**Qué debería calcular.** Componer dos órdenes tiene que equivaler a ejecutar la primera y después la segunda:

$$
E_{\mathrm{componer}(o_1, o_2)} = E_{o_2} \circ E_{o_1}
$$

**Demostración.** componer(o1, o2) es Secuencia(List(o1, o2)). Por la definición de $E$ para una secuencia con $m = 2$, su función es $E_{o_2} \circ E_{o_1}$. Y por el teorema del punto 2, ejecutar calcula exactamente eso:

$$
\mathrm{ejecutar}(e, \mathrm{componer}(o_1, o_2)) = \mathrm{ejecutar}(\mathrm{ejecutar}(e, o_1),\ o_2)
$$

Es decir, ejecutar la orden compuesta es lo mismo que ejecutar sus partes una detrás de la otra. $\blacksquare$

Esto se extiende a secuencias más largas. Si $xs$ e $ys$ son dos listas de órdenes, entonces

$$
E_{\mathrm{Secuencia}(xs \mathbin{+\!\!+} ys)} = E_{\mathrm{Secuencia}(ys)} \circ E_{\mathrm{Secuencia}(xs)}
$$

porque la composición de funciones es asociativa: podemos agrupar primero los factores de $xs$ y después los de $ys$. Así, juntar dos secuencias equivale a ejecutar la primera y luego la segunda.

### rutina

**Qué debería calcular.** rutina(p)(g)(k) debe repetir $k$ veces "avanzar $p$ y girar hacia $g$":

$$
E_{\mathrm{rutina}(p)(g)(k)} = \bigl(E_{\mathrm{Girar}(g)} \circ E_{\mathrm{Avanzar}(p)}\bigr)^k
$$

**Demostración.** El programa devuelve Repetir(k, Secuencia(List(Avanzar(p), Girar(g)))). Por la definición de $E$ para Repetir, su función es la $k$-ésima potencia de la función de la secuencia. Y por la definición para Secuencia con dos órdenes, esa función es $E_{\mathrm{Girar}(g)} \circ E_{\mathrm{Avanzar}(p)}$. Como ejecutar coincide con $E$ (punto 2), queda demostrado. $\blacksquare$

Por lo que vimos en componer, la rutina equivale a repetir $k$ veces componer(Avanzar(p), Girar(g)), o sea a ejecutar sus dos partes, en orden, en cada vuelta. La currificación no cambia nada: rutina(p)(g) es una función que espera las veces, y cuando las recibe devuelve la orden de arriba.

### aplicarA

**Qué debería calcular.** Si la lista de estados es $[e_1, \ldots, e_m]$, el resultado debe ser $[E_o(e_1), \ldots, E_o(e_m)]$.

**Demostración.** El programa es un map que ejecuta la orden sobre cada estado. Un map conserva el orden y el tamaño de la lista, y por el punto 2 ejecutar(e_i, o) vale $E_o(e_i)$. Con la lista vacía devuelve la lista vacía, que es lo que da la fórmula con $m = 0$. $\blacksquare$

### transformar

**Qué debería calcular.** Cambiar cada orden básica $b$ por $f(b)$ y dejar intacta la estructura de las compuestas. Lo escribimos $T(o, f)$:

$$
\begin{aligned}
T(b, f) &= f(b) \qquad (b \text{ es Avanzar o Girar})\\
T(\mathrm{Repetir}(k, o'), f) &= \mathrm{Repetir}(k,\ T(o', f))\\
T(\mathrm{Secuencia}([o_1,\ldots,o_m]), f) &= \mathrm{Secuencia}([T(o_1,f),\ldots,T(o_m,f)])
\end{aligned}
$$

**Demostración** de que transformar(o, f) es igual a $T(o, f)$, por inducción estructural. El match pone primero Repetir y Secuencia, y deja el caso basica para lo que sobre. Como Orden es sealed y solo tiene cuatro alternativas, basica solo puede ser Avanzar o Girar.

- Básicas: el programa devuelve f(basica). Coincide.
- Repetir: devuelve Repetir(veces, transformar(orden, f)), y por hipótesis de inducción eso es $\mathrm{Repetir}(k, T(o', f))$.
- Secuencia: aplica transformar a cada elemento con un map, y por hipótesis de inducción cada uno es $T(o_i, f)$. $\blacksquare$

**Una consecuencia útil.** Si $f$ no cambia el efecto de las órdenes básicas, es decir $E_{f(b)} = E_b$ para toda básica $b$, entonces $T(o, f) \equiv o$. Se prueba por inducción estructural: en las básicas es justo la hipótesis, y en Repetir y Secuencia sale de que $E$ se arma componiendo, o elevando a una potencia, las funciones de las partes, que no cambian.

## 5. Punto 5: simplificar

**Qué debería calcular.** Que simplificar no cambie el resultado de ejecutar. Para toda orden $o$:

$$
E_{\mathrm{simplificar}(o)} = E_o
$$

O sea que, para todo estado $e$, ejecutar(e, o) y ejecutar(e, simplificar(o)) dejan el mismo estado. Por el teorema del punto 2, basta trabajar con $E$.

**Las funciones auxiliares.** simplificar define dos funciones dentro de ella:

- nada(x) dice si una orden no hace nada: es verdadera para Secuencia(Nil) y para Avanzar(0).
- juntar(os) recorre una lista y cambia cada pareja de Avanzar seguidos, Avanzar(a) y Avanzar(b), por un solo Avanzar(a + b).

### Resultados previos

**Lema 1 (nada es correcta).** Si nada(x) es verdadera, entonces $E_x = \mathrm{id}$.

*Demostración.* Si $x$ es Secuencia(Nil), su función es la composición vacía, que es la identidad. Si $x$ es Avanzar(0), es la identidad por (O1). $\blacksquare$

**Lema 2 (quitar lo que no hace nada).** Si a una lista de órdenes le quitamos las que cumplen nada, la secuencia que queda es equivalente a la original.

*Demostración.* En la composición $E_{o_m} \circ \cdots \circ E_{o_1}$, cada factor que cumple nada es la identidad por el Lema 1, y quitar un factor identidad no cambia el resultado. $\blacksquare$

**Lema 3 (juntar conserva el efecto).** Para toda lista $os$, $\mathrm{Secuencia}(\mathrm{juntar}(os)) \equiv \mathrm{Secuencia}(os)$. Además, juntar termina.

*Demostración.* Por inducción sobre la longitud de $os$, con un caso por cada rama del match:

- $os$ vacía: juntar devuelve la misma lista vacía.
- $os = \mathrm{Avanzar}(a) :: \mathrm{Avanzar}(b) :: resto$: el programa llama a juntar con la lista $\mathrm{Avanzar}(a+b) :: resto$, que tiene un elemento menos. Por eso la inducción se puede aplicar, y por eso juntar termina. Por (O2), esa lista es equivalente a $os$, y por la hipótesis de inducción juntar la deja equivalente a sí misma.
- $os = x :: resto$ (cualquier otro caso): juntar devuelve x seguido de juntar(resto). Por la hipótesis de inducción, juntar(resto) es equivalente a $resto$. Como $E_{\mathrm{Secuencia}(x :: zs)} = E_{\mathrm{Secuencia}(zs)} \circ E_x$, cambiar $resto$ por algo equivalente no cambia el resultado. $\blacksquare$

**Lema 4 (cambiar partes por otras equivalentes).** Si $o_i \equiv o'_i$ para cada $i$, entonces $\mathrm{Secuencia}([o_1, \ldots, o_m]) \equiv \mathrm{Secuencia}([o'_1, \ldots, o'_m])$.

*Demostración.* $E$ de una secuencia es la composición de las funciones de sus partes. Si cada $E_{o_i} = E_{o'_i}$, las dos composiciones son iguales. $\blacksquare$

**Lema 5 (secuencias pequeñas).** $E_{\mathrm{Secuencia}(\mathrm{Nil})} = \mathrm{id}$ y $E_{\mathrm{Secuencia}([x])} = E_x$.

*Demostración.* La primera es la composición vacía. La segunda es la composición de un solo factor. $\blacksquare$

### El teorema

**Teorema.** Para toda orden $o$, $\mathrm{simplificar}(o) \equiv o$.

*Demostración.* Por inducción estructural sobre $o$, revisando las reglas en el mismo orden en que aparecen en el match:

1. **Avanzar(0) se vuelve Secuencia(Nil).** Por (O1) $E_{\mathrm{Avanzar}(0)} = \mathrm{id}$, y por el Lema 5 la secuencia vacía también es la identidad.

2. **Repetir con $k \le 0$ se vuelve Secuencia(Nil).** Por definición $E_{\mathrm{Repetir}(k,o')} = (E_{o'})^k = \mathrm{id}$ cuando $k \le 0$, y la secuencia vacía también es la identidad.

3. **Repetir(1, orden) se vuelve simplificar(orden).** $E_{\mathrm{Repetir}(1, o')} = (E_{o'})^1 = E_{o'}$, y por hipótesis de inducción $E_{\mathrm{simplificar}(o')} = E_{o'}$.

4. **Repetir con $k \ge 2$** (los casos $k \le 1$ ya los tomaron las reglas anteriores). Sea $s = \mathrm{simplificar}(o')$. Por hipótesis de inducción, $E_s = E_{o'}$. Hay dos posibilidades:
    - Si nada(s) es verdadera: por el Lema 1 $E_s = \mathrm{id}$, así que $E_{o'} = \mathrm{id}$ y $(E_{o'})^k = \mathrm{id}$. El programa devuelve Secuencia(Nil), que también es la identidad. Coinciden.
    - Si no: el programa devuelve Repetir(k, s), cuya función es $(E_s)^k = (E_{o'})^k$. Coinciden.

5. **Secuencia.** El programa hace cinco cosas, en este orden: simplifica cada elemento, quita los que no hacen nada, aplica juntar, vuelve a quitar los que no hacen nada y mira cuántos elementos quedaron. Cada una conserva el efecto:
    - Simplificar cada elemento: por hipótesis de inducción $\mathrm{simplificar}(o_i) \equiv o_i$, y por el Lema 4 la secuencia no cambia.
    - Quitar lo que no hace nada: Lema 2.
    - juntar: Lema 3.
    - Quitar de nuevo lo que no hace nada: Lema 2. Este segundo filtro hace falta porque juntar puede producir Avanzar(0), por ejemplo con Avanzar(4) seguido de Avanzar(-4).
    - La decisión final: si la lista quedó vacía se devuelve Secuencia(Nil), y si quedó un solo elemento se devuelve ese elemento. Ambos son equivalentes a la secuencia por el Lema 5. Si quedaron dos o más, se devuelve la misma secuencia.

6. **Cualquier otra orden** (Girar y Avanzar(n) con $n \ne 0$): se devuelve igual, así que es equivalente a sí misma. $\blacksquare$

**Terminación.** Todas las llamadas recursivas de simplificar son sobre partes más pequeñas de $o$ (lo que hay dentro de un Repetir o los elementos de una secuencia), y juntar termina por el Lema 3. Por eso simplificar termina con cualquier orden.

## 6. Resumen

| Punto | Función | Qué demostramos | Cómo |
|---|---|---|---|
| 1 | girar | coincide con $\mathrm{rot}_s(d)$ | revisando los 8 casos |
| 2 | ejecutar | $\mathrm{ejecutar}(e,o) = E_o(e)$ | inducción estructural y foldLeft con invariante |
| 3 | cuantasBasicas, profundidad | coinciden con $C(o)$ y $P(o)$ | inducción estructural y foldLeft con invariante |
| 4 | componer | ejecutar la compuesta es ejecutar sus partes | definición de $E$ y punto 2 |
| 4 | rutina | equivale a repetir "avanzar y girar" $k$ veces | definición de $E$ y punto 2 |
| 4 | aplicarA, transformar | coinciden con su especificación | inducción sobre la lista y sobre Orden |
| 5 | simplificar | no cambia el resultado de ejecutar | inducción estructural, regla por regla, con cinco lemas |