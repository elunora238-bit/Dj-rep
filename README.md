# DJ MIX PRO — Android starter v0.3

Proyecto Android nativo en Kotlin, en orientación horizontal.

## Incluido en esta versión
- Dos reproductores independientes que pueden reproducir a la vez.
- Carga de audio desde el selector de archivos del dispositivo.
- Play, pausa y volver al inicio por deck.
- Volumen independiente y crossfader con curva equal-power.
- Control PITCH por deck, de 0,50x a 1,50x.
- El control de velocidad conserva el tono nominal (`setPitch(1.0)`), aunque la calidad depende del códec y del dispositivo.
- Ecualización HIGH/MID/LOW inicial mediante el efecto Equalizer del sistema.

## Limitaciones importantes
Esta versión usa `MediaPlayer` y `PlaybackParams`. Es una etapa de prototipo: cambiar la velocidad no equivale a un algoritmo DJ de time-stretch de alta calidad, y no ofrece beatmatching, análisis de BPM, jog wheel preciso, efectos DSP dedicados ni grabación del bus master. Los botones CUE/LOOP/SYNC siguen siendo marcadores de funciones pendientes.

## Próxima etapa
Sustituir MediaPlayer por un motor de audio PCM de baja latencia (por ejemplo, Oboe/AAudio), que permita decodificar, sincronizar y mezclar las dos pistas con control de tempo, procesamiento DSP y grabación del master.

## Abrir
1. Descomprime el ZIP.
2. Abre esta carpeta en Android Studio.
3. Sincroniza Gradle y ejecuta en un dispositivo Android.
4. Requiere JDK 17 y conexión a internet la primera vez.

No se incluye APK compilado y el proyecto no ha sido probado en un dispositivo físico.


## DJ MIX PRO — edición visual PRO
- Interfaz oscura de estilo consola DJ, con dos decks y mixer central.
- Carga de audio local, reproducción/pausa y parada.
- Crossfader con curva equal-power y volumen independiente.
- Pitch y controles de ecualización HIGH/MID/LOW.
- CUE funcional: guarda el punto actual la primera vez y vuelve a él al pulsarlo de nuevo.
- LOOP funcional como repetición de la pista completa.
- SYNC BPM queda indicado como función avanzada pendiente: esta versión no analiza BPM ni hace beatmatching automático.

Esta edición es una compilación de desarrollo. Debe probarse en dispositivos reales antes de distribuirla.
