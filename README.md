# AlcalaInfection

Simulación de un apocalipsis zombi desarrollada en Java con Apache NetBeans para la práctica de laboratorio PECL 2025. El proyecto combina programación concurrente, una interfaz gráfica Swing y un monitor remoto mediante Java RMI.

## Cómo funciona

Cada humano y cada zombi se ejecuta en su propio hilo. La simulación comienza con un paciente cero y crea humanos de forma escalonada.

Los humanos se preparan en la zona común del refugio y eligen uno de los túneles. Para salir deben formar grupos de tres; el cruce del túnel se realiza de uno en uno y se da prioridad al regreso al refugio. En el exterior exploran y buscan comida. Si regresan sin estar heridos, añaden comida a las reservas. Después descansan y pasan por el comedor, que tiene cinco plazas y consume una unidad de comida por persona. Los humanos heridos realizan un descanso adicional para recuperarse.

Los zombis buscan humanos vivos disponibles en su zona y eligen una víctima al azar. Una víctima solo puede recibir un ataque a la vez. Con la configuración actual, el humano tiene un 66,67 % de probabilidad de defenderse y el zombi un 33,33 % de matarlo. Si el humano muere, se convierte en un zombi en la misma zona. Si sobrevive, queda herido y puede recibir otro ataque mientras siga en el exterior: la defensa no provoca una huida inmediata.

Cuando un zombi no encuentra víctimas disponibles durante un plazo aleatorio de entre 5 y 15 segundos, se desplaza a otra zona. Encontrar una víctima reinicia el plazo aunque el ataque falle. La pausa detiene también este plazo.

## Interfaz y monitor remoto

La ventana principal muestra la distribución de las entidades entre el refugio, los túneles y las zonas exteriores. El monitor RMI se abre automáticamente en la misma máquina y muestra:

- Humanos en el refugio, en espera en los túneles y en las zonas exteriores.
- Zombis en cada zona exterior.
- Los tres zombis con más muertes.
- Un botón para pausar y reanudar la simulación.

El servicio RMI utiliza por defecto el puerto `1099` y el nombre `ServicioApocalipsis`. El cliente incluido conecta a `localhost`; su constructor permite indicar otro host. Al cerrar el monitor, la simulación principal continúa.

Los eventos de creación, movimiento, ataques y comida se registran en `logs/apocalipsis.txt`.

## Configuración

Los parámetros están centralizados en [`Config.java`](src/main/java/user/apocalipsiszombi2025/util/Config.java): población, zonas, túneles, tamaño de grupo, plazas del comedor, comida, tiempos de actividad, probabilidades y conexión RMI.

Por defecto hay 100 humanos, cuatro túneles, cuatro zonas exteriores y 50 unidades iniciales de comida. Cada túnel conduce a la zona exterior del mismo índice; mantén coherentes ambos números si cambias la configuración.

## Abrir y ejecutar

Necesitas Apache NetBeans con soporte Java y Maven, y un JDK compatible con Java 23. JDK 25 permite compilar el proyecto, cuyo `pom.xml` especifica `release 23`.

1. En NetBeans, selecciona **Archivo → Abrir proyecto** y abre la carpeta `AlcalaInfection`, que contiene `pom.xml`.
2. Configura el JDK en las propiedades del proyecto si fuera necesario.
3. Ejecuta **Limpiar y construir** para descargar las dependencias y compilar.
4. Pulsa **F6**. La clase principal es `user.apocalipsiszombi2025.ApocalipsisZombi2025`.

Los formularios `.form` se conservan para editar las ventanas desde el diseñador de NetBeans. La dependencia `AbsoluteLayout` se descarga mediante Maven.

También puedes compilar desde una terminal con Maven instalado:

```bash
mvn clean package
```

La aplicación necesita un entorno gráfico para mostrar las ventanas Swing.
