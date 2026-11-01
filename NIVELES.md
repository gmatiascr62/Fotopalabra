# Cómo agregar imágenes y niveles

Agregar contenido **no requiere tocar código Kotlin**. Solo hay dos cosas:

1. Subir las imágenes a `app/src/main/assets/niveles/`.
2. Agregar (o editar) los datos del nivel en `app/src/main/assets/niveles/niveles.json`.

Al hacer push, GitHub Actions valida los niveles, compila y publica el APK.

---

## 1. Imágenes

Carpeta: `app/src/main/assets/niveles/`

| Nivel con… | Nombres de archivo |
|---|---|
| 1 imagen | `nivel_001.png` |
| 2 imágenes | `nivel_008_a.png`, `nivel_008_b.png` |
| 3 imágenes | `nivel_018_a.png`, `nivel_018_b.png`, `nivel_018_c.png` |

- Formatos: `.png`, `.jpg`, `.jpeg` o `.webp` (webp ocupa menos). Conviene que cada imagen pese menos de 300 KB: si ChatGPT entrega un PNG de varios MB, reducilo o guardalo como JPG.
- Tamaño recomendado: **cuadrada, 768×768 o 1024×1024 px**. La app las reduce al tamaño de la pantalla para ahorrar memoria.
- Si el nivel tiene **una sola imagen**, también puede ser apaisada o vertical (ej. 1408×768): se muestra con su proporción real. Cuando hay varias imágenes se recomienda que sean cuadradas.
- Sin espacios ni tildes en el nombre del archivo.
- La nomenclatura `nivel_XXX` es una convención: lo que manda es el nombre escrito en `niveles.json`.

Mientras una imagen no exista, el juego muestra un **marcador temporal** rayado con el
cartel `IMAGEN PENDIENTE`, el nombre del archivo esperado y la descripción de lo que
debe dibujarse. Apenas subís el archivo con ese nombre, el marcador desaparece.

En **Configuración → Imágenes de niveles → Ver imágenes pendientes** la app lista todas
las imágenes que faltan con su descripción (útil como lista de tareas para ChatGPT).

Para ver un nivel nuevo sin resolver los anteriores, activá **Configuración → Modo prueba →
Desbloquear todos los niveles**. Al apagarlo, el progreso real sigue intacto.

### Imágenes que faltan para los 20 niveles de prueba

| Archivo | Qué dibujar |
|---|---|
| nivel_001.png | Un sol amarillo y brillante con rayos |
| nivel_002.png | Un gato sentado |
| nivel_003.png | Una casa con techo rojo, puerta y ventanas |
| nivel_004.png | Una luna en cuarto creciente en el cielo nocturno |
| nivel_005.png | Un árbol grande con tronco marrón y copa verde |
| nivel_006.png | Una pelota de colores |
| nivel_007.png | Una manzana roja con una hoja |
| nivel_008_a.png / _b.png | Un sol / Un dado de seis caras → SOLDADO |
| nivel_009_a.png / _b.png | Una cara sonriente / Una col (repollo) → CARACOL |
| nivel_010_a.png / _b.png | Un rollo de tela / Una araña → TELARAÑA |
| nivel_011_a.png / _b.png | Un pan / El talón de un pie → PANTALÓN |
| nivel_012_a.png / _b.png | Una cama / Un león → CAMALEÓN |
| nivel_013_a.png / _b.png | Un arco con flecha / El iris de un ojo → ARCOÍRIS |
| nivel_014_a.png / _b.png | Un balón / Un cesto de mimbre → BALONCESTO |
| nivel_015_a.png / _b.png | El mar con olas / Una taza de té → MARTE |
| nivel_016_a.png / _b.png | El símbolo pi (π) / Una rata → PIRATA |
| nivel_017_a.png / _b.png | Nota musical "RE" en un pentagrama / Un mate con bombilla → REMATE |
| nivel_018_a/_b/_c.png | Bufanda / Copo de nieve / Muñeco de nieve → INVIERNO |
| nivel_019_a/_b/_c.png | Pizarrón con tiza / Mochila / Lápiz y cuaderno → ESCUELA |
| nivel_020_a/_b/_c.png | Casco rojo / Manguera / Camión rojo con escalera → BOMBERO |
| ~~nivel_021.jpg~~ ✔ | Ya cargada: una gran hada sobre un pueblo → GRANADA (gran + hada) |
| ~~nivel_022.jpg~~ ✔ | Ya cargada: un limón con antiparras nadando → LIMONADA (limón + nada) |
| ~~nivel_023.jpg~~ ✔ | Ya cargada: una chica tomando una taza de té → TOMATE (toma + té) |
| ~~nivel_024.jpg~~ ✔ | Ya cargada: una flor y una boca mostrando las encías → FLORENCIA (flor + encía) |
| ~~nivel_025.jpg~~ ✔ | Ya cargada: las letras G y A enojadas → MÁLAGA (mala + GA) |

> Consejo para ChatGPT: pedí "ilustración plana, colorida, fondo blanco liso, sin texto,
> estilo juego infantil, formato cuadrado". Mantener el mismo estilo en todas ayuda a que
> el juego se vea uniforme. Evitá que la imagen contenga la palabra escrita.

---

## 2. Datos del nivel (`niveles.json`)

```json
{
  "version": 1,
  "niveles": [
    {
      "id": 21,
      "respuesta": "Girasol",
      "dificultad": "intermedio",
      "imagenes": [
        { "archivo": "nivel_021_a.png", "descripcion": "Una flecha girando en círculo" },
        { "archivo": "nivel_021_b.png", "descripcion": "Un sol" }
      ]
    }
  ]
}
```

| Campo | Obligatorio | Descripción |
|---|---|---|
| `id` | sí | Número único mayor que 0. **Define el orden** de los niveles. No reutilizar ids: el progreso guardado se asocia al id. |
| `respuesta` | sí | La palabra. Puede tener tildes y Ñ (`"Camaleón"`, `"Telaraña"`). Para jugar se usa en mayúsculas y sin tildes (la Ñ se conserva). Espacios y guiones se ignoran. Máximo 16 letras. |
| `dificultad` | no | `facil` (10 monedas), `intermedio` (15) o `dificil` (25). Por defecto `facil`. Resolver sin pistas suma 5 extra. |
| `imagenes` | sí | Una o más imágenes. Cada una puede ser un objeto `{ "archivo", "descripcion" }` o simplemente el nombre: `"nivel_021.png"`. |
| `descripcion` | no | Qué debe mostrar la imagen. Se ve en el marcador temporal y en la lista de pendientes. |
| `fichas` | no | Cantidad total de letras disponibles (entre el largo de la respuesta y 24). Por defecto se calcula según el largo. |

### Cientos o miles de niveles

La app carga **todos** los archivos `.json` de `assets/niveles/`, así que podés dividir
los niveles en varios archivos para que sea más fácil editarlos:

```
assets/niveles/niveles.json          (1–20)
assets/niveles/niveles_021_100.json  (21–100)
assets/niveles/niveles_101_200.json  (101–200)
```

Todos se combinan y se ordenan por `id`. Cada archivo tiene el mismo formato
(`{ "niveles": [ ... ] }`).

### Validación automática

Si un nivel tiene un error (id repetido, respuesta con números, imagen sin nombre,
dificultad desconocida…):

- En GitHub Actions falla el paso **Tests**, con el mensaje del error, y no se genera el APK.
- En el teléfono, ese nivel se omite y el error aparece en **Configuración**.

Podés probar localmente con `./gradlew testDebugUnitTest`.
