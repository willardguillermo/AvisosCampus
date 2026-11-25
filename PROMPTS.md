# PROMPTS.md · Fase 2 con IA (rama `mejora-ia`)

Herramienta: **Gemini en Android Studio** (modo Agent)
Proyecto: AvisosCampus · Laboratorio 15 · Programación en Móviles

Mejoras elegidas:
1. **Dos canales**: Urgentes (`IMPORTANCE_HIGH`) y Avisos; el recordatorio usa el urgente.
2. **Texto largo** con `BigTextStyle`.
3. **Receptor del cargador** (`ACTION_POWER_CONNECTED` / `ACTION_POWER_DISCONNECTED`) con su propio switch.

## Resumen

| Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Agrega un segundo canal "Urgentes" con IMPORTANCE_HIGH; el ViewModel decide que el recordatorio es urgente y emite un `EventoAviso(aviso, urgente)`; la pantalla pasa `urgente` al Notificador. Prohibido Context en el ViewModel.» | Creó `EventoAviso` en `Modelos.kt`, el canal `CANAL_URGENTE` en `crearCanal`, el parámetro `urgente` en `mostrar()` (canal y prioridad según el valor), cambió `eventos` a `SharedFlow<EventoAviso>` y actualizó el `collect` de la pantalla. | No cambié nada. Revisé el diff: el ViewModel sigue sin `Context`, `NotificationManager` ni `BroadcastReceiver`, y el Repository no se tocó. | Quien decide la **urgencia** es el ViewModel (regla de negocio) y quien elige el **canal** es el Notificador (detalle de Android). Así cada capa hace solo lo suyo. |
| «Agrega `BigTextStyle` cambiando solo `Notificador.kt`: arma un texto largo con texto, origen y hora del aviso; mantén `setContentText` corto y todo lo demás igual.» | Agregó `val textoLargo` dentro de `mostrar()` y `.setStyle(NotificationCompat.BigTextStyle().bigText(textoLargo))` al Builder, conservando canales, prioridad, `PendingIntent` y `notify(aviso.id)`. | No cambié nada. Confirmé en el diff que solo cambió `Notificador.kt`. | Los textos de la guía son cortos y al expandir no se notaba nada; armar el texto largo con datos del propio `Aviso` muestra la mejora **sin cambiar los datos ficticios** de la guía ni otras capas. |
| «Escucha el cargador copiando el patrón del modo avión: `CargadorReceiver` con `ACTION_POWER_CONNECTED`/`DISCONNECTED`, `Origen.CARGADOR`, `escuchandoCargador` y `alCambiarCargador` en el ViewModel, un segundo `DisposableEffect` y su propio switch. Prohibido Context en el ViewModel; quitar el receptor en `onDispose`.» | Creó `receiver/CargadorReceiver.kt`, agregó `CARGADOR("Cargador")` al enum y su color verde en el historial, el estado y las funciones en el ViewModel, y en la pantalla un `DisposableEffect(escuchandoCargador)` con un `IntentFilter` de dos acciones más la fila del switch. También sugirió probar con `adb shell dumpsys battery set ac 1/0`. | No cambié nada. Revisé el diff: el ViewModel sigue sin `Context`, el receptor se quita en `onDispose` y no se tocó el código del modo avión, el Repository ni el Notificador. | Reutilizar el mismo patrón del modo avión hace el código predecible y fácil de explicar. Un solo receptor con un filtro de dos acciones es más simple que dos receptores. |

---

## Detalle de cada prompt

### Mejora 1 · Dos canales

**Fecha:** 2026-11-25

**Prompt:**
```
Proyecto Android "AvisosCampus" (Kotlin + Jetpack Compose + MVVM + Repository), paquete com.willard.avisoscampus.

Quiero agregar un segundo canal de notificación SIN romper la arquitectura:

1. En notificacion/Notificador.kt:
   - Mantén el canal actual CANAL = "avisos" ("Avisos", IMPORTANCE_DEFAULT).
   - Agrega CANAL_URGENTE = "urgentes" con nombre "Urgentes", descripción "Avisos importantes que aparecen en pantalla" e IMPORTANCE_HIGH (notificación emergente).
   - crearCanal(context) debe crear los dos canales.
   - mostrar(context, aviso, urgente: Boolean = false): si urgente es true usa CANAL_URGENTE y PRIORITY_HIGH; si no, CANAL y PRIORITY_DEFAULT. Conserva todo lo demás (tienePermiso, PendingIntent, setAutoCancel, notify con aviso.id).

2. El ViewModel decide qué es urgente: el recordatorio (Origen.RECORDATORIO) es urgente, los demás no.
   - Crea en model/Modelos.kt: data class EventoAviso(val aviso: Aviso, val urgente: Boolean)
   - En viewmodel/AvisosViewModel.kt cambia _eventos y eventos a SharedFlow<EventoAviso>. avisarAhora y alCambiarModoAvion emiten urgente = false; recordarEn emite urgente = true.

3. En ui/PantallaAvisos.kt, el collect de vm.eventos debe llamar Notificador.mostrar(contexto, evento.aviso, evento.urgente) y mantener: si devuelve false, vm.noSePudoMostrar().

Reglas obligatorias:
- PROHIBIDO usar Context, NotificationManager o BroadcastReceiver dentro del ViewModel.
- No cambies el Repository ni HistorialAvisos.
- No borres ni renombres funciones existentes.

Al terminar, dame un resumen corto: qué archivos modificaste, qué cambiaste en cada uno y cómo probarlo.
```

**Qué respondió Gemini (resumen):**
- `model/Modelos.kt`: agregó `data class EventoAviso(val aviso: Aviso, val urgente: Boolean)`.
- `notificacion/Notificador.kt`: constante `CANAL_URGENTE = "urgentes"`; `crearCanal()` crea los dos canales; `mostrar()` recibe `urgente` y elige canal (`CANAL_URGENTE` / `CANAL`) y prioridad (`PRIORITY_HIGH` / `PRIORITY_DEFAULT`).
- `viewmodel/AvisosViewModel.kt`: `eventos` pasa a `SharedFlow<EventoAviso>`; solo `recordarEn()` emite `urgente = true`.
- `ui/PantallaAvisos.kt`: el `collect` llama a `Notificador.mostrar(contexto, evento.aviso, evento.urgente)` y conserva `vm.noSePudoMostrar()`.

**Qué se usó / qué se cambió:** se usó tal cual. Antes del commit revisé el diff línea por línea:
- El ViewModel no importa nada de Android (solo `ViewModel` y `viewModelScope`).
- El Repository y `HistorialAvisos` quedaron sin cambios.
- Se mantuvo el `PendingIntent` inmutable y el `notify` con `aviso.id`.

**Qué aprendimos:**
- La importancia de un canal **no se puede cambiar** después de crearlo; por eso el urgente es un canal nuevo (`urgentes`) y no una modificación de `avisos`.
- `IMPORTANCE_HIGH` (canal, Android 8+) y `PRIORITY_HIGH` (notificación, Android 7 o menos) se ponen juntos para que la notificación sea emergente en todas las versiones.
- Envolver el aviso en un `EventoAviso` deja que el ViewModel pase una decisión (urgente o no) sin tocar Android.

### Mejora 2 · Texto largo con BigTextStyle

**Fecha:** 2026-11-25

**Prompt:**
```
Proyecto Android "AvisosCampus" (Kotlin + Jetpack Compose + MVVM + Repository), paquete com.willard.avisoscampus.

Quiero que la notificación muestre un texto largo al expandirla usando NotificationCompat.BigTextStyle, cambiando SOLO notificacion/Notificador.kt:

1. Dentro de mostrar(context, aviso, urgente), construye un texto largo con los datos del aviso, por ejemplo:
   "${aviso.texto}\n\nOrigen: ${aviso.origen.texto} · Hora: ${aviso.hora}\nToca para abrir AvisosCampus y revisar el historial."
2. Agrega al Builder:
   .setStyle(NotificationCompat.BigTextStyle().bigText(textoLargo))
3. Mantén setContentText(aviso.texto) para que la notificación contraída siga mostrando el texto corto.
4. Conserva todo lo demás tal cual: los dos canales (CANAL y CANAL_URGENTE), la prioridad según urgente, tienePermiso, el PendingIntent con FLAG_IMMUTABLE, setAutoCancel(true) y notify con aviso.id.

Reglas obligatorias:
- No modifiques ningún otro archivo (ni ViewModel, ni Repository, ni pantallas).
- No uses Context fuera del Notificador.

Al terminar, dame un resumen corto: qué cambiaste y cómo probarlo.
```

**Qué respondió Gemini (resumen):**
- `notificacion/Notificador.kt`: dentro de `mostrar()` creó `textoLargo` con el texto, el origen, la hora y una línea final, y agregó `.setStyle(NotificationCompat.BigTextStyle().bigText(textoLargo))` al Builder.
- Conservó `setContentText(aviso.texto)` para la vista contraída, los dos canales, la prioridad, el `PendingIntent`, `setAutoCancel(true)` y `notify(aviso.id)`.

**Qué se usó / qué se cambió:** se usó tal cual. El diff confirma que solo cambió `Notificador.kt` (2 líneas nuevas).

**Qué aprendimos:**
- Una notificación tiene dos vistas: **contraída**, que usa `setContentText`, y **expandida**, que usa el estilo (`BigTextStyle`). Por eso se dejan las dos.
- Es un cambio de presentación, así que vive solo en el Notificador: el ViewModel y el Repository no necesitan saber cómo se dibuja la notificación.
