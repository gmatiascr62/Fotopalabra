# Fotopalabra — Adiviná la palabra

Juego para Android, completamente en español y sin conexión, donde hay que descubrir la
palabra que representan una o varias imágenes (por ejemplo: **sol + dado = SOLDADO**).

- Kotlin + Jetpack Compose, Android 7.0 (API 24) o superior.
- Sin internet, sin registro, sin APIs pagas. Sin permisos especiales.
- 20 niveles de prueba (7 fáciles, 7 intermedios, 6 difíciles).
- **Para agregar imágenes o niveles, ver [NIVELES.md](NIVELES.md).**

## Cómo se juega

1. Mirá la imagen (o la combinación de imágenes).
2. Tocá las letras para completar los casilleros. Tocá un casillero para devolver su letra.
3. **BORRAR** quita la última letra; mantenelo presionado para borrar todas.
4. Al completar los casilleros se comprueba solo: si es correcto, ganás monedas y se pasa al siguiente nivel.

### Pistas (cuestan monedas)

| Pista | Costo | Efecto |
|---|---|---|
| Pista | 15 | Muestra una frase de ayuda |
| Revelar letra | 25 | Fija una letra correcta (se puede usar varias veces) |
| Quitar letras | 40 | Elimina todas las letras que sobran |

Se empieza con 100 monedas. Cada nivel da 10 / 15 / 25 según la dificultad (+5 si no usaste pistas).
Repetir un nivel ya resuelto no da monedas. Las pistas compradas se conservan aunque cierres la app.

## Arquitectura

```
app/src/main/
├── assets/niveles/          ← imágenes + niveles.json  (contenido, sin código)
├── java/com/fotopalabra/
│   ├── motor/               Motor del juego: estado inmutable, colocar/quitar letras, comprobar,
│   │                        generación determinista de letras, normalización (tildes, Ñ)
│   ├── niveles/             Base de datos de niveles: formato JSON, validación, catálogo
│   ├── pistas/              Sistema de pistas: revelar letra, quitar letras, pista textual
│   ├── monedas/             Sistema de monedas: recompensas y cobro de pistas
│   ├── progreso/            Guardado: progreso, estadísticas, ajustes, reglas de desbloqueo
│   ├── datos/               Adaptadores Android: lectura de assets y SharedPreferences
│   ├── recursos/            Carga de imágenes con reducción de tamaño y caché LRU
│   └── ui/                  Interfaz (Compose): ViewModel, tema, componentes y pantallas
│       └── pantallas/       Inicio, Juego, Victoria, Niveles, Configuración
└── res/                     Icono de la app, tema y textos
```

- `motor`, `niveles`, `pistas`, `monedas` y `progreso` son Kotlin puro (no dependen de Android) y
  están cubiertos por tests unitarios en `app/src/test`.
- `JuegoViewModel` conecta la interfaz con esas piezas y mantiene la navegación.
- El progreso se guarda como un JSON pequeño en `SharedPreferences` en cada cambio,
  así se puede cerrar la app en cualquier momento y continuar donde quedó.
- Las imágenes se decodifican reducidas al tamaño de pantalla y se cachean con un límite
  de memoria; los niveles se leen una vez al iniciar.
- La interfaz se adapta a teléfonos, tablets y orientación horizontal (en pantallas
  anchas, las imágenes van a la izquierda y las letras a la derecha).

## Compilar

**Android Studio:** abrir la carpeta del proyecto y ejecutar la configuración `app`.

**Línea de comandos:**

```bash
./gradlew testDebugUnitTest   # tests del motor y validación de niveles
./gradlew assembleDebug       # APK en app/build/outputs/apk/debug/
```

**GitHub Actions:** cada push ejecuta el workflow `Compilar APK`
(`.github/workflows/android.yml`), que corre los tests, compila y publica el artifact
**Fotopalabra-apk** (pestaña *Actions* → la ejecución → *Artifacts*). Contiene:

- `Fotopalabra-debug.apk`: para probar.
- `Fotopalabra-release.apk`: optimizado y más liviano. Está firmado con la clave de debug
  para que se pueda instalar directamente; antes de publicar en Google Play hay que
  configurar una clave propia.

Para instalar el APK en el teléfono hay que permitir "instalar apps de orígenes desconocidos".
