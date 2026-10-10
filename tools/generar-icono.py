"""
SEVETCO - Genera el icono de la app Android a partir de assets/sevetco.jpg.

El logo es una ilustracion (perrito y gatito). Para que se vea grande y sin
bordes raros:

  1. Se amplia el lienzo con una copia difuminada de la propia imagen.
  2. Se recorta centrado en los animalitos.
  3. El recorte se usa tal cual como icono.

El lanzador de Android muestra solo el 66% central de la capa de fondo, por eso
el recorte deja margen suficiente alrededor de los animalitos.

Salida:
  android/app/src/main/res/mipmap-*/ic_launcher.png
  android/app/src/main/res/mipmap-*/ic_launcher_round.png
  android/app/src/main/res/mipmap-*/ic_launcher_background.png
  android/app/src/main/res/mipmap-*/ic_launcher_foreground.png
  android/app/src/main/res/drawable/logo_sevetco.png
"""

import os

from PIL import Image, ImageFilter

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FUENTE = os.path.join(RAIZ, "assets", "sevetco.jpg")
RES = os.path.join(RAIZ, "android", "app", "src", "main", "res")

DENSIDADES = {
    "mdpi": 1,
    "hdpi": 1.5,
    "xhdpi": 2,
    "xxhdpi": 3,
    "xxxhdpi": 4,
}


def construir_icono():
    """Devuelve el cuadrado definitivo del icono (los dos animalitos)."""
    imagen = Image.open(FUENTE).convert("RGB")
    ancho, alto = imagen.size

    # Lienzo ampliado con fondo difuminado, para que el recorte del lanzador
    # nunca llegue a mostrar un borde recto.
    lado = int(ancho * 1.22)
    base = imagen.resize((lado, lado), Image.LANCZOS).filter(
        ImageFilter.GaussianBlur(lado * 0.05)
    )
    base.paste(imagen, ((lado - ancho) // 2, (lado - alto) // 2))

    recorte = base.crop(
        (int(0.04 * lado), int(0.06 * lado), int(0.96 * lado), int(0.98 * lado))
    )
    return recorte


def main():
    icono = construir_icono()

    for nombre, factor in DENSIDADES.items():
        carpeta = os.path.join(RES, f"mipmap-{nombre}")
        os.makedirs(carpeta, exist_ok=True)

        lado = int(round(48 * factor))
        completo = icono.resize((lado, lado), Image.LANCZOS)
        completo.save(os.path.join(carpeta, "ic_launcher.png"))
        completo.save(os.path.join(carpeta, "ic_launcher_round.png"))

        lado_fondo = int(round(108 * factor))
        icono.resize((lado_fondo, lado_fondo), Image.LANCZOS).save(
            os.path.join(carpeta, "ic_launcher_background.png")
        )
        Image.new("RGBA", (lado_fondo, lado_fondo), (0, 0, 0, 0)).save(
            os.path.join(carpeta, "ic_launcher_foreground.png")
        )

        print(f"mipmap-{nombre}: {lado}x{lado} y fondo {lado_fondo}x{lado_fondo}")

    dibujable = os.path.join(RES, "drawable")
    os.makedirs(dibujable, exist_ok=True)
    icono.resize((192, 192), Image.LANCZOS).save(
        os.path.join(dibujable, "logo_sevetco.png")
    )
    print("drawable/logo_sevetco.png")


if __name__ == "__main__":
    main()

