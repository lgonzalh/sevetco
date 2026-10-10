"""
SEVETCO - Genera los mockups (wireframes) de la app web y de la app movil.

Salida:
  mockups/export/web/*.png
  mockups/export/mobile/*.png

Las pantallas corresponden a las funcionalidades de los documentos del
proyecto (RF-01 a RF-17) y al plan del proyecto.
"""

import os
from PIL import Image, ImageDraw, ImageFont

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
EXPORT = os.path.join(RAIZ, "mockups", "export")

BORDE = (176, 183, 195)
RELLENO = (242, 244, 247)
RELLENO2 = (250, 251, 253)
AZUL = (31, 56, 100)
GRIS = (120, 128, 140)
NEGRO = (26, 26, 26)
BLANCO = (255, 255, 255)

FUENTE = "arial.ttf"
FUENTE_B = "arialbd.ttf"


def fuente(tamano, negrita=False):
    return ImageFont.truetype(FUENTE_B if negrita else FUENTE, tamano)


def caja(dib, x, y, w, h, relleno=RELLENO, radio=6, borde=BORDE, grosor=2):
    dib.rounded_rectangle([x, y, x + w, y + h], radius=radio, fill=relleno, outline=borde, width=grosor)


def texto(dib, x, y, cadena, tamano=15, negrita=False, color=NEGRO):
    dib.text((x, y), cadena, font=fuente(tamano, negrita), fill=color)


def texto_centrado(dib, x, y, w, cadena, tamano=15, negrita=False, color=NEGRO):
    f = fuente(tamano, negrita)
    ancho = dib.textlength(cadena, font=f)
    dib.text((x + (w - ancho) / 2, y), cadena, font=f, fill=color)


def placeholder(dib, x, y, w, h, etiqueta=""):
    """Zona de imagen: caja con una X en diagonal."""
    caja(dib, x, y, w, h, relleno=RELLENO2, radio=4)
    dib.line([x, y, x + w, y + h], fill=BORDE, width=2)
    dib.line([x + w, y, x, y + h], fill=BORDE, width=2)
    if etiqueta:
        texto_centrado(dib, x, y + h / 2 - 8, w, etiqueta, 12, color=GRIS)


# ---------------------------------------------------------------------------
# Componentes
# ---------------------------------------------------------------------------

def barra_superior(dib, ancho, titulo, usuario="Administrador"):
    dib.rectangle([0, 0, ancho, 58], fill=AZUL)
    texto(dib, 20, 18, "SEVETCO", 20, True, BLANCO)
    texto(dib, 140, 22, titulo, 16, False, (214, 222, 240))
    texto(dib, ancho - 190, 22, usuario, 14, False, (214, 222, 240))


def menu_lateral(dib, alto, items, activo=0, y0=58):
    dib.rectangle([0, y0, 220, alto], fill=(24, 44, 79))
    y = y0 + 18
    for i, item in enumerate(items):
        if i == activo:
            caja(dib, 10, y - 6, 200, 38, relleno=(45, 74, 128), borde=(45, 74, 128))
        texto(dib, 26, y + 4, item, 15, i == activo, BLANCO)
        y += 48


def tabla(dib, x, y, ancho, columnas, filas, alto_fila=38, proporcion=None):
    if proporcion is None:
        proporcion = [1] * len(columnas)
    total = sum(proporcion)
    anchos = [ancho * p / total for p in proporcion]

    caja(dib, x, y, ancho, alto_fila, relleno=RELLENO, radio=4)
    cx = x
    for i, col in enumerate(columnas):
        texto(dib, cx + 10, y + 11, col, 13, True, GRIS)
        cx += anchos[i]

    fy = y + alto_fila
    for fila in filas:
        dib.rectangle([x, fy, x + ancho, fy + alto_fila], fill=BLANCO, outline=BORDE, width=1)
        cx = x
        for i, celda in enumerate(fila):
            texto(dib, cx + 10, fy + 11, str(celda), 13)
            cx += anchos[i]
        fy += alto_fila
    return fy


def boton(dib, x, y, ancho, alto, etiqueta, primario=True):
    caja(dib, x, y, ancho, alto,
         relleno=AZUL if primario else BLANCO,
         borde=AZUL if primario else BORDE, radio=6)
    texto_centrado(dib, x, y + alto / 2 - 9, ancho, etiqueta, 14, True,
                   BLANCO if primario else NEGRO)


def campo(dib, x, y, ancho, etiqueta, valor=""):
    texto(dib, x, y, etiqueta, 13, True, GRIS)
    caja(dib, x, y + 22, ancho, 36, relleno=BLANCO, radio=4)
    if valor:
        texto(dib, x + 10, y + 32, valor, 13, color=GRIS)
    return y + 68


def tarjetas(dib, x, y, ancho, datos, alto=84):
    cantidad = len(datos)
    espacio = 14
    w = (ancho - espacio * (cantidad - 1)) / cantidad
    for i, (valor, etiqueta) in enumerate(datos):
        cx = x + i * (w + espacio)
        caja(dib, cx, y, w, alto, relleno=BLANCO, radio=6)
        texto(dib, cx + 16, y + 16, str(valor), 24, True, AZUL)
        texto(dib, cx + 16, y + 50, etiqueta, 13, color=GRIS)
    return y + alto


def lineas(dib, x, y, ancho, cantidad, alto=12, separacion=24):
    for i in range(cantidad):
        dib.rectangle([x, y + i * separacion, x + ancho * (1 - 0.12 * (i % 3)), y + i * separacion + alto],
                      fill=RELLENO, outline=BORDE)
    return y + cantidad * separacion


# ---------------------------------------------------------------------------
# Pantallas web
# ---------------------------------------------------------------------------

def web(archivo, titulo, menu_activo, contenido):
    ANCHO, ALTO = 1280, 800
    img = Image.new("RGB", (ANCHO, ALTO), BLANCO)
    dib = ImageDraw.Draw(img)

    MENU = ["Panel", "Citas", "Proveedores", "Servicios", "Planes",
            "Tratamientos", "Trazabilidad", "Pagos", "Rutas", "Usuarios"]

    barra_superior(dib, ANCHO, titulo)
    menu_lateral(dib, ALTO, MENU, activo=menu_activo)

    contenido(dib, 250, 88, ANCHO - 300)

    dib.rectangle([0, 0, ANCHO - 1, ALTO - 1], outline=BORDE, width=2)
    img.save(os.path.join(EXPORT, "web", archivo))


def pantallas_web():
    def login(dib, x, y, w):
        caja(dib, x + w / 2 - 220, 200, 440, 380, relleno=BLANCO, radio=10)
        placeholder(dib, x + w / 2 - 30, 230, 60, 60, "")
        texto_centrado(dib, x + w / 2 - 220, 305, 440, "SEVETCO", 30, True, AZUL)
        texto_centrado(dib, x + w / 2 - 220, 345, 440,
                       "Plataforma Integral de Servicios", 14, color=GRIS)
        texto_centrado(dib, x + w / 2 - 220, 365, 440,
                       "Veterinarios a Domicilio", 14, color=GRIS)
        boton(dib, x + w / 2 - 170, 420, 340, 46, "Continuar con Google")
        texto_centrado(dib, x + w / 2 - 220, 490, 440,
                       "Ingreso para administradores y proveedores", 12, color=GRIS)

    def panel(dib, x, y, w):
        tarjetas(dib, x, y, w, [("24", "Citas registradas"), ("7", "Pendientes"),
                                ("12", "Finalizadas"), ("5", "Proveedores")])
        caja(dib, x, y + 108, w, 300, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 126, "Citas por estado", 16, True, AZUL)
        tabla(dib, x + 18, y + 162, w - 36,
              ["Estado", "Cantidad"],
              [["PENDIENTE", "7"], ["ASIGNADA", "3"], ["EN PROCESO", "2"],
               ["FINALIZADA", "12"], ["CANCELADA", "0"]])

    def usuarios(dib, x, y, w):
        caja(dib, x, y, w, 480, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 18, "Usuarios", 16, True, AZUL)
        campo(dib, x + 18, y + 54, 240, "Buscar")
        tabla(dib, x + 18, y + 140, w - 36,
              ["Nombre", "Correo", "Documento", "Tipo", "Estado", ""],
              [["Ana Perez", "ana@correo.com", "10203040", "PROPIETARIO", "ACTIVO", "Editar"],
               ["Carlos Ruiz", "carlos@vet.com", "77889900", "PROVEEDOR", "ACTIVO", "Editar"],
               ["Diana Lopez", "diana@sevetco.com", "55667788", "ADMIN", "ACTIVO", "Editar"]],
              proporcion=[2, 2, 1.4, 1.4, 1, 1])

    def proveedores(dib, x, y, w):
        boton(dib, x + w - 190, y - 6, 190, 40, "+ Nuevo proveedor")
        caja(dib, x, y + 50, w, 430, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 68, "Proveedores registrados", 16, True, AZUL)
        tabla(dib, x + 18, y + 104, w - 36,
              ["Nombre", "Correo", "Tarjeta profesional", "Especialidad", "Estado", ""],
              [["Carlos Ruiz", "carlos@vet.com", "TP-12345", "Medicina veterinaria", "ACTIVO", "Editar"],
               ["Marta Gomez", "marta@vet.com", "TP-98765", "Estetica canina", "ACTIVO", "Editar"]],
              proporcion=[2, 2, 1.6, 2, 1, 0.8])

    def proveedor_form(dib, x, y, w):
        caja(dib, x, y, 620, 420, relleno=BLANCO, radio=6)
        texto(dib, x + 20, y + 18, "Nuevo proveedor", 17, True, AZUL)
        alto = campo(dib, x + 20, y + 62, 560, "Usuario", "ana@correo.com")
        alto = campo(dib, x + 20, alto, 560, "Tarjeta profesional", "TP-12345")
        alto = campo(dib, x + 20, alto, 560, "Especialidad", "Medicina veterinaria")
        boton(dib, x + 400, alto + 10, 180, 42, "Guardar")
        boton(dib, x + 220, alto + 10, 170, 42, "Cancelar", primario=False)

    def servicios(dib, x, y, w):
        boton(dib, x + w - 180, y - 6, 180, 40, "+ Nuevo servicio")
        caja(dib, x, y + 50, w, 430, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 68, "Portafolio de servicios", 16, True, AZUL)
        tabla(dib, x + 18, y + 104, w - 36,
              ["Servicio", "Precio base", "Duracion", ""],
              [["Consulta veterinaria a domicilio", "$120.000", "60 min", "Editar"],
               ["Vacunacion a domicilio", "$80.000", "30 min", "Editar"],
               ["Estetica canina y felina", "$100.000", "90 min", "Editar"],
               ["Entrenamiento basico", "$150.000", "60 min", "Editar"],
               ["Servicio funerario", "$350.000", "120 min", "Editar"]],
              proporcion=[3, 1.4, 1, 0.8])

    def servicio_form(dib, x, y, w):
        caja(dib, x, y, 620, 380, relleno=BLANCO, radio=6)
        texto(dib, x + 20, y + 18, "Nuevo servicio", 17, True, AZUL)
        alto = campo(dib, x + 20, y + 62, 560, "Nombre del servicio", "Vacunacion a domicilio")
        alto = campo(dib, x + 20, alto, 560, "Precio base (COP)", "80000")
        alto = campo(dib, x + 20, alto, 560, "Duracion (minutos)", "30")
        boton(dib, x + 400, alto + 10, 180, 42, "Guardar")
        boton(dib, x + 220, alto + 10, 170, 42, "Cancelar", primario=False)

    def planes(dib, x, y, w):
        boton(dib, x + w - 160, y - 6, 160, 40, "+ Nuevo plan")
        caja(dib, x, y + 50, w, 430, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 68, "Planes", 16, True, AZUL)
        tabla(dib, x + 18, y + 104, w - 36,
              ["Plan", "Precio", "Vigencia", "Estado", "Servicios incluidos", ""],
              [["Plan Basico", "$250.000", "30 dias", "ACTIVO",
                "Consulta (1), Vacunacion (1)", "Editar"],
               ["Plan Integral", "$480.000", "30 dias", "ACTIVO",
                "Consulta (2), Vacunacion (2), Estetica (1)", "Editar"],
               ["Plan Premium", "$780.000", "30 dias", "ACTIVO",
                "Consulta (4), Vacunacion (4), Estetica (2), Entrenamiento (2)", "Editar"]],
              proporcion=[1.4, 1.2, 1, 0.9, 3.4, 0.8])

    def plan_form(dib, x, y, w):
        caja(dib, x, y, 820, 520, relleno=BLANCO, radio=6)
        texto(dib, x + 20, y + 18, "Nuevo plan", 17, True, AZUL)
        alto = campo(dib, x + 20, y + 62, 360, "Nombre del plan", "Plan Premium")
        campo(dib, x + 420, y + 62, 360, "Precio (COP)", "780000")
        alto = campo(dib, x + 20, alto, 360, "Vigencia (dias)", "30")
        campo(dib, x + 420, y + 130, 360, "Estado", "ACTIVO")
        texto(dib, x + 20, y + 210, "Servicios incluidos", 13, True, GRIS)
        yy = y + 240
        for nombre in ["Consulta veterinaria a domicilio", "Vacunacion a domicilio",
                       "Estetica canina y felina", "Entrenamiento basico"]:
            caja(dib, x + 20, yy, 22, 22, relleno=BLANCO, radio=3)
            texto(dib, x + 54, yy + 2, nombre, 14)
            caja(dib, x + 520, yy, 60, 24, relleno=BLANCO, radio=3)
            texto(dib, x + 540, yy + 4, "1", 13, color=GRIS)
            yy += 40
        boton(dib, x + 620, yy + 10, 180, 42, "Guardar")

    def citas(dib, x, y, w):
        caja(dib, x, y, 300, 40, relleno=BLANCO, radio=4)
        texto(dib, x + 12, y + 11, "Filtrar por estado", 13, color=GRIS)
        caja(dib, x + 320, y, 300, 40, relleno=BLANCO, radio=4)
        texto(dib, x + 332, y + 11, "Filtrar por fecha", 13, color=GRIS)
        caja(dib, x, y + 60, w, 430, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 78, "Citas", 16, True, AZUL)
        tabla(dib, x + 18, y + 114, w - 36,
              ["Fecha", "Propietario", "Mascota", "Servicio", "Responsable", "Estado", ""],
              [["10/10/2026 09:00", "Ana Perez", "Rocky", "Consulta", "Carlos Ruiz", "ASIGNADA", "Ver"],
               ["11/10/2026 14:30", "Ana Perez", "Luna", "Vacunacion", "Sin asignar", "PENDIENTE", "Ver"],
               ["12/10/2026 08:00", "Luis Mora", "Toby", "Estetica", "Marta Gomez", "FINALIZADA", "Ver"]],
              proporcion=[1.6, 1.6, 1.2, 1.4, 1.6, 1.1, 0.6])

    def cita_detalle(dib, x, y, w):
        caja(dib, x, y, 700, 460, relleno=BLANCO, radio=6)
        texto(dib, x + 20, y + 18, "Cita de Rocky", 17, True, AZUL)
        texto(dib, x + 20, y + 52, "Fecha: 10/10/2026 09:00     Servicio: Consulta veterinaria", 14)
        texto(dib, x + 20, y + 76, "Propietario: Ana Perez     Mascota: Rocky (perro)", 14)
        campo(dib, x + 20, y + 120, 640, "Responsable del servicio", "Carlos Ruiz")
        campo(dib, x + 20, y + 190, 640, "Estado de la cita", "ASIGNADA")
        boton(dib, x + 490, y + 270, 170, 42, "Guardar cambios")
        boton(dib, x + 300, y + 270, 170, 42, "Ver trazabilidad", primario=False)

    def trazabilidad(dib, x, y, w):
        caja(dib, x, y, 760, 460, relleno=BLANCO, radio=6)
        texto(dib, x + 20, y + 18, "Trazabilidad de la cita", 17, True, AZUL)
        eventos = [("PENDIENTE", "10/10/2026 08:12", "Ana Perez"),
                   ("ASIGNADA", "10/10/2026 08:40", "Administrador"),
                   ("EN PROCESO", "10/10/2026 09:05", "Carlos Ruiz"),
                   ("FINALIZADA", "10/10/2026 10:10", "Carlos Ruiz")]
        yy = y + 66
        for estado, cuando, quien in eventos:
            caja(dib, x + 24, yy, 190, 32, relleno=RELLENO, radio=16)
            texto(dib, x + 48, yy + 7, estado, 13, True, AZUL)
            texto(dib, x + 240, yy + 7, cuando, 13, color=GRIS)
            texto(dib, x + 400, yy + 7, quien, 13)
            dib.line([x + 40, yy + 32, x + 40, yy + 60], fill=BORDE, width=2)
            yy += 92

    def atenciones(dib, x, y, w):
        caja(dib, x, y, w, 460, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 18, "Historial de tratamientos", 16, True, AZUL)
        tabla(dib, x + 18, y + 54, w - 36,
              ["Fecha", "Mascota", "Diagnostico", "Tratamiento", "Recomendaciones"],
              [["09/10/2026", "Rocky", "Otitis externa", "Limpieza y gotas", "Revisar en 8 dias"],
               ["02/10/2026", "Luna", "Control sano", "Vacuna polivalente", "Proxima dosis en 1 anio"],
               ["28/09/2026", "Toby", "Dermatitis", "Bano medicado", "Evitar shampoo comun"]],
              proporcion=[1.2, 1.2, 2, 2, 2.2])

    def pagos(dib, x, y, w):
        boton(dib, x + w - 180, y - 6, 180, 40, "+ Registrar pago")
        caja(dib, x, y + 50, w, 430, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 68, "Pagos", 16, True, AZUL)
        tabla(dib, x + 18, y + 104, w - 36,
              ["Propietario", "Concepto", "Monto", "Estado", "Referencia"],
              [["Ana Perez", "Suscripcion: Plan Integral", "$480.000", "PAGADO", "REF-0091"],
               ["Luis Mora", "Cita del 12/10/2026", "$100.000", "PENDIENTE", "-"],
               ["Ana Perez", "Cita del 10/10/2026", "$120.000", "PAGADO", "REF-0092"]],
              proporcion=[2, 3, 1.4, 1.2, 1.4])

    def rutas(dib, x, y, w):
        boton(dib, x + w - 190, y - 6, 190, 40, "+ Registrar ruta")
        caja(dib, x, y + 50, w, 300, relleno=BLANCO, radio=6)
        texto(dib, x + 18, y + 68, "Rutas y desplazamientos", 16, True, AZUL)
        tabla(dib, x + 18, y + 104, w - 36,
              ["Cita", "Origen", "Destino", "Distancia", "Estado"],
              [["10/10/2026 - Rocky", "Sede norte", "Calle 45 #12-30 (4.6097, -74.0817)", "6.2 km", "EN RUTA"],
               ["12/10/2026 - Toby", "Sede norte", "Carrera 7 #80-15 (4.6610, -74.0520)", "9.4 km", "COMPLETADA"]],
              proporcion=[1.6, 1.2, 3.2, 1, 1.2])
        caja(dib, x, y + 370, w, 110, relleno=RELLENO2, radio=6)
        texto_centrado(dib, x, y + 410, w, "Mapa del desplazamiento", 14, color=GRIS)

    pantallas = [
        ("01-login.png", "Inicio de sesion", -1, login),
        ("02-panel.png", "Panel", 0, panel),
        ("03-usuarios.png", "Usuarios", 9, usuarios),
        ("04-proveedores.png", "Proveedores", 2, proveedores),
        ("05-proveedor-formulario.png", "Nuevo proveedor", 2, proveedor_form),
        ("06-servicios.png", "Servicios", 3, servicios),
        ("07-servicio-formulario.png", "Nuevo servicio", 3, servicio_form),
        ("08-planes.png", "Planes", 4, planes),
        ("09-plan-formulario.png", "Nuevo plan", 4, plan_form),
        ("10-citas.png", "Citas", 1, citas),
        ("11-cita-detalle.png", "Cita", 1, cita_detalle),
        ("12-trazabilidad.png", "Trazabilidad", 6, trazabilidad),
        ("13-tratamientos.png", "Tratamientos", 5, atenciones),
        ("14-pagos.png", "Pagos", 7, pagos),
        ("15-rutas.png", "Rutas", 8, rutas),
    ]
    for archivo, titulo, activo, contenido in pantallas:
        web(archivo, titulo, activo, contenido)
    return len(pantallas)


# ---------------------------------------------------------------------------
# Pantallas movil
# ---------------------------------------------------------------------------

def movil(archivo, dibujar):
    ANCHO, ALTO = 400, 800
    img = Image.new("RGB", (ANCHO, ALTO), BLANCO)
    dib = ImageDraw.Draw(img)
    dib.rectangle([0, 0, ANCHO, 26], fill=(240, 242, 245))
    texto(dib, 16, 6, "9:55", 12)
    dib.rectangle([ANCHO - 46, 8, ANCHO - 16, 18], fill=(200, 205, 212))
    dibujar(dib, ANCHO, ALTO)
    dib.rectangle([0, 0, ANCHO - 1, ALTO - 1], outline=BORDE, width=2)
    img.save(os.path.join(EXPORT, "mobile", archivo))


def cabecera_movil(dib, ancho, titulo, con_volver=False):
    dib.rectangle([0, 26, ancho, 84], fill=AZUL)
    x = 14 if con_volver else 0
    if con_volver:
        texto(dib, 14, 44, "<", 22, True, BLANCO)
    if con_volver:
        texto_centrado(dib, 0, 46, ancho, titulo, 17, True, BLANCO)
    else:
        texto(dib, 16, 46, titulo, 17, True, BLANCO)


def barra_inferior(dib, ancho, alto, activo):
    dib.rectangle([0, alto - 76, ancho, alto], fill=BLANCO, outline=BORDE, width=2)
    items = ["Inicio", "Mascotas", "Servicios", "Planes", "Citas"]
    for i, item in enumerate(items):
        cx = i * (ancho / len(items))
        color = AZUL if i == activo else GRIS
        dib.rectangle([cx + ancho / len(items) / 2 - 11, alto - 62,
                       cx + ancho / len(items) / 2 + 11, alto - 40], fill=color)
        texto_centrado(dib, cx, alto - 34, ancho / len(items), item, 11, i == activo,
                       AZUL if i == activo else GRIS)


def pantallas_movil():
    def splash(dib, ancho, alto):
        dib.rectangle([0, 26, ancho, alto], fill=AZUL)
        dib.ellipse([ancho / 2 - 40, 240, ancho / 2 + 40, 320], fill=BLANCO)
        texto_centrado(dib, 0, 350, ancho, "SEVETCO", 30, True, BLANCO)
        texto_centrado(dib, 0, 392, ancho, "Servicios veterinarios a domicilio", 13, color=(214, 222, 240))
        boton(dib, 60, 520, 280, 48, "Comenzar")
        texto_centrado(dib, 0, 600, ancho, "Consulta - Vacunacion - Estetica", 12, color=(180, 195, 225))
        texto_centrado(dib, 0, 620, ancho, "Entrenamiento - Servicio funerario", 12, color=(180, 195, 225))

    def login(dib, ancho, alto):
        dib.rectangle([0, 26, ancho, alto], fill=AZUL)
        caja(dib, 30, 180, ancho - 60, 400, relleno=BLANCO, radio=12)
        dib.ellipse([ancho / 2 - 32, 220, ancho / 2 + 32, 284], fill=RELLENO, outline=BORDE, width=2)
        texto_centrado(dib, 30, 300, ancho - 60, "SEVETCO", 26, True, AZUL)
        texto_centrado(dib, 30, 336, ancho - 60, "Servicios veterinarios a domicilio", 12, color=GRIS)
        boton(dib, 60, 400, ancho - 120, 46, "Continuar con Google")
        texto_centrado(dib, 30, 470, ancho - 60, "Ingreso de propietarios de mascotas", 11, color=GRIS)

    def registro(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Completar perfil")
        y = 110
        y = campo(dib, 24, y, ancho - 48, "Nombre completo", "Ana Perez")
        y = campo(dib, 24, y, ancho - 48, "Numero de documento", "1020304050")
        y = campo(dib, 24, y, ancho - 48, "Telefono", "300 123 4567")
        campo(dib, 24, y, ancho - 48, "Correo", "ana@correo.com")
        boton(dib, 24, 560, ancho - 48, 46, "Guardar")

    def inicio(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Inicio")
        texto(dib, 20, 104, "Hola, Ana", 20, True, AZUL)
        texto(dib, 20, 132, "Tus servicios a domicilio", 13, color=GRIS)
        boton(dib, 20, 162, ancho - 40, 44, "+ Agendar cita")
        boton(dib, 20, 214, ancho - 40, 44, "Historial de tratamientos", primario=False)
        texto(dib, 20, 286, "Proximas citas", 15, True, AZUL)
        yy = 314
        for mascota, servicio, cuando, estado in [
            ("Rocky", "Consulta veterinaria", "10/10/2026 09:00", "ASIGNADA"),
            ("Luna", "Vacunacion", "11/10/2026 14:30", "PENDIENTE"),
        ]:
            caja(dib, 20, yy, ancho - 40, 92, relleno=BLANCO, radio=8)
            texto(dib, 36, yy + 14, cuando, 14, True)
            texto(dib, 36, yy + 38, "Mascota: " + mascota, 13)
            texto(dib, 36, yy + 58, "Servicio: " + servicio, 13)
            texto(dib, ancho - 150, yy + 14, estado, 12, True, GRIS)
            yy += 104
        barra_inferior(dib, ancho, alto, 0)

    def mascotas(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Mis mascotas")
        yy = 106
        for nombre, detalle in [("Rocky", "Perro - Labrador - 3 anios"),
                                ("Luna", "Gato - Siames - 1 anio")]:
            caja(dib, 20, yy, ancho - 40, 84, relleno=BLANCO, radio=8)
            dib.ellipse([36, yy + 18, 84, yy + 66], fill=RELLENO, outline=BORDE, width=2)
            texto(dib, 100, yy + 20, nombre, 16, True)
            texto(dib, 100, yy + 46, detalle, 13, color=GRIS)
            texto(dib, ancho - 84, yy + 30, "Editar", 13, color=AZUL)
            yy += 98
        dib.ellipse([ancho - 84, alto - 164, ancho - 24, alto - 104], fill=AZUL)
        texto_centrado(dib, ancho - 84, alto - 142, 60, "+", 28, True, BLANCO)
        barra_inferior(dib, ancho, alto, 1)

    def mascota_form(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Nueva mascota", con_volver=True)
        y = 110
        y = campo(dib, 24, y, ancho - 48, "Nombre", "Rocky")
        y = campo(dib, 24, y, ancho - 48, "Especie", "Perro")
        y = campo(dib, 24, y, ancho - 48, "Raza", "Labrador")
        campo(dib, 24, y, ancho - 48, "Fecha de nacimiento", "12/03/2023")
        boton(dib, 24, 520, ancho - 48, 46, "Guardar mascota")

    def detalle_mascota(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Rocky", con_volver=True)
        dib.ellipse([ancho / 2 - 56, 110, ancho / 2 + 56, 222], fill=RELLENO, outline=BORDE, width=2)
        texto_centrado(dib, 0, 240, ancho, "Rocky", 22, True, AZUL)
        texto_centrado(dib, 0, 272, ancho, "Perro - Labrador - 3 anios", 13, color=GRIS)
        caja(dib, 20, 312, ancho - 40, 210, relleno=BLANCO, radio=8)
        texto(dib, 36, 330, "Datos", 15, True, AZUL)
        filas = [("Especie", "Perro"), ("Raza", "Labrador"),
                 ("Nacimiento", "12/03/2023"), ("Peso", "24 kg"), ("Sexo", "Macho")]
        yy = 362
        for k, v in filas:
            texto(dib, 36, yy, k, 13, color=GRIS)
            texto(dib, 180, yy, v, 13)
            yy += 30
        boton(dib, 24, 548, ancho - 48, 46, "Ver historial de tratamientos", primario=False)

    def servicios(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Servicios")
        yy = 106
        for nombre, precio, duracion in [
            ("Consulta veterinaria", "$120.000", "60 min"),
            ("Vacunacion", "$80.000", "30 min"),
            ("Estetica", "$100.000", "90 min"),
            ("Entrenamiento", "$150.000", "60 min"),
            ("Servicio funerario", "$350.000", "120 min"),
        ]:
            caja(dib, 20, yy, ancho - 40, 78, relleno=BLANCO, radio=8)
            dib.ellipse([36, yy + 18, 78, yy + 60], fill=RELLENO, outline=BORDE, width=2)
            texto(dib, 94, yy + 18, nombre, 15, True)
            texto(dib, 94, yy + 42, precio + " - " + duracion, 13, color=GRIS)
            texto(dib, ancho - 74, yy + 28, ">", 18, True, GRIS)
            yy += 90
        barra_inferior(dib, ancho, alto, 2)

    def detalle_servicio(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Consulta veterinaria", con_volver=True)
        placeholder(dib, 20, 106, ancho - 40, 180, "Imagen del servicio")
        texto(dib, 24, 306, "Consulta veterinaria a domicilio", 18, True, AZUL)
        texto(dib, 24, 338, "$120.000  -  60 minutos", 15)
        texto(dib, 24, 376, "Descripcion del servicio", 14, True, GRIS)
        lineas(dib, 24, 402, ancho - 48, 3)
        boton(dib, 24, 500, ancho - 48, 46, "Seleccionar servicio")

    def planes(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Planes disponibles")
        yy = 106
        for nombre, precio, detalle in [
            ("Plan Basico", "$250.000", "Consulta (1) - Vacunacion (1)"),
            ("Plan Integral", "$480.000", "Consulta (2) - Vacunacion (2) - Estetica (1)"),
            ("Plan Premium", "$780.000", "Consulta (4) - Vacunacion (4) - Estetica (2)"),
        ]:
            caja(dib, 20, yy, ancho - 40, 110, relleno=BLANCO, radio=8)
            texto(dib, 36, yy + 16, nombre, 16, True, AZUL)
            texto(dib, 36, yy + 42, precio + " / 30 dias", 14)
            texto(dib, 36, yy + 68, detalle, 12, color=GRIS)
            texto(dib, ancho - 90, yy + 80, "Ver mas", 13, color=AZUL)
            yy += 122
        barra_inferior(dib, ancho, alto, 3)

    def detalle_plan(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Plan Integral", con_volver=True)
        texto(dib, 24, 110, "Plan Integral", 20, True, AZUL)
        texto(dib, 24, 142, "$480.000 - vigencia 30 dias", 15)
        texto(dib, 24, 186, "Servicios incluidos", 14, True, GRIS)
        yy = 214
        for nombre, cantidad in [("Consulta veterinaria", 2), ("Vacunacion", 2), ("Estetica", 1)]:
            caja(dib, 24, yy, ancho - 48, 54, relleno=BLANCO, radio=6)
            texto(dib, 38, yy + 18, nombre, 14)
            texto(dib, ancho - 90, yy + 18, "x" + str(cantidad), 14, True, AZUL)
            yy += 66
        boton(dib, 24, 470, ancho - 48, 46, "Adquirir plan")

    def agendar_1(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Agendar cita (1/3)", con_volver=True)
        texto(dib, 24, 104, "Paso 1 de 3", 12, color=GRIS)
        dib.rectangle([24, 126, ancho - 24, 132], fill=BORDE)
        dib.rectangle([24, 126, (ancho - 24) * 0.33 + 24, 132], fill=AZUL)
        texto(dib, 24, 152, "Selecciona la mascota", 15, True, AZUL)
        yy = 182
        for nombre in ["Rocky - Perro", "Luna - Gato"]:
            caja(dib, 24, yy, ancho - 48, 56, relleno=BLANCO, radio=8)
            texto(dib, 40, yy + 18, nombre, 14)
            yy += 68
        texto(dib, 24, yy + 14, "Selecciona el servicio", 15, True, AZUL)
        yy += 44
        for nombre in ["Consulta veterinaria", "Vacunacion", "Estetica"]:
            caja(dib, 24, yy, ancho - 48, 56, relleno=BLANCO, radio=8)
            texto(dib, 40, yy + 18, nombre, 14)
            yy += 68
        boton(dib, 24, alto - 150, ancho - 48, 46, "Continuar")

    def agendar_2(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Agendar cita (2/3)", con_volver=True)
        texto(dib, 24, 104, "Paso 2 de 3", 12, color=GRIS)
        dib.rectangle([24, 126, ancho - 24, 132], fill=BORDE)
        dib.rectangle([24, 126, (ancho - 24) * 0.66 + 24, 132], fill=AZUL)
        texto(dib, 24, 154, "Fecha y hora", 15, True, AZUL)
        caja(dib, 24, 184, ancho - 48, 300, relleno=BLANCO, radio=8)
        texto_centrado(dib, 24, 200, ancho - 48, "Octubre 2026", 15, True, AZUL)
        dib.line([24, 228, ancho - 24, 228], fill=BORDE, width=2)
        dias = ["L", "M", "M", "J", "V", "S", "D"]
        for i, d in enumerate(dias):
            texto(dib, 40 + i * 46, 240, d, 12, True, GRIS)
        for fila in range(5):
            for col in range(7):
                cx = 40 + col * 46
                cy = 274 + fila * 42
                dib.rectangle([cx - 8, cy - 6, cx + 22, cy + 24], fill=RELLENO, outline=BORDE)
        caja(dib, 24, 504, ancho - 48, 52, relleno=BLANCO, radio=8)
        texto(dib, 40, 520, "Hora: 09:00", 14)
        boton(dib, 24, alto - 150, ancho - 48, 46, "Continuar")

    def agendar_3(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Agendar cita (3/3)", con_volver=True)
        texto(dib, 24, 104, "Paso 3 de 3", 12, color=GRIS)
        dib.rectangle([24, 126, ancho - 24, 132], fill=AZUL)
        texto(dib, 24, 154, "Direccion del servicio", 15, True, AZUL)
        y = campo(dib, 24, 184, ancho - 48, "Direccion del domicilio", "Calle 45 #12-30")
        boton(dib, 24, y + 6, ancho - 48, 46, "Usar mi ubicacion", primario=False)
        texto(dib, 24, y + 70, "Coordenadas: 4.609700, -74.081700", 12, color=GRIS)
        caja(dib, 24, y + 100, ancho - 48, 170, relleno=RELLENO2, radio=8)
        texto_centrado(dib, 24, y + 176, ancho - 48, "Mapa", 14, color=GRIS)
        boton(dib, 24, alto - 150, ancho - 48, 46, "Agendar cita")

    def mis_citas(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Mis citas")
        yy = 106
        tarjetas_cita = [
            ("10/10/2026 09:00", "Rocky", "Consulta veterinaria", "Carlos Ruiz", "ASIGNADA"),
            ("11/10/2026 14:30", "Luna", "Vacunacion", "Por asignar", "PENDIENTE"),
            ("12/10/2026 08:00", "Toby", "Estetica", "Marta Gomez", "FINALIZADA"),
        ]
        for cuando, mascota, servicio, responsable, estado in tarjetas_cita:
            caja(dib, 20, yy, ancho - 40, 132, relleno=BLANCO, radio=8)
            texto(dib, 36, yy + 14, cuando, 14, True)
            texto(dib, ancho - 130, yy + 14, estado, 12, True, GRIS)
            texto(dib, 36, yy + 42, "Mascota: " + mascota, 13)
            texto(dib, 36, yy + 64, "Servicio: " + servicio, 13)
            texto(dib, 36, yy + 86, "Responsable: " + responsable, 13, color=GRIS)
            yy += 146
        barra_inferior(dib, ancho, alto, 4)

    def detalle_cita(dib, ancho, alto):
        cabecera_movil(dib, ancho, "Detalle de la cita", con_volver=True)
        texto(dib, 24, 108, "10/10/2026 09:00", 18, True, AZUL)
        texto(dib, 24, 140, "Estado: ASIGNADA", 14)
        caja(dib, 20, 176, ancho - 40, 200, relleno=BLANCO, radio=8)
        filas = [("Mascota", "Rocky (perro, labrador)"), ("Servicio", "Consulta veterinaria a domicilio"),
                 ("Responsable", "Carlos Ruiz"), ("Duracion", "60 minutos"), ("Valor", "$120.000")]
        yy = 196
        for k, v in filas:
            texto(dib, 36, yy, k, 13, color=GRIS)
            texto(dib, 140, yy, v, 13)
            yy += 34
        caja(dib, 20, 396, ancho - 40, 130, relleno=RELLENO2, radio=8)
        texto_centrado(dib, 20, 448, ancho - 40, "Ubicacion del servicio", 13, color=GRIS)
        boton(dib, 24, 556, ancho - 48, 46, "Ver historial de la mascota", primario=False)

    pantallas = [
        ("01-splash.png", splash),
        ("02-login.png", login),
        ("03-registro-perfil.png", registro),
        ("04-inicio.png", inicio),
        ("05-mis-mascotas.png", mascotas),
        ("06-mascota-formulario.png", mascota_form),
        ("07-mascota-detalle.png", detalle_mascota),
        ("08-servicios.png", servicios),
        ("09-servicio-detalle.png", detalle_servicio),
        ("10-planes.png", planes),
        ("11-plan-detalle.png", detalle_plan),
        ("12-agendar-paso-1.png", agendar_1),
        ("13-agendar-paso-2.png", agendar_2),
        ("14-agendar-paso-3.png", agendar_3),
        ("15-mis-citas.png", mis_citas),
        ("16-cita-detalle.png", detalle_cita),
    ]
    for archivo, dibujar in pantallas:
        movil(archivo, dibujar)
    return len(pantallas)


def main():
    os.makedirs(os.path.join(EXPORT, "web"), exist_ok=True)
    os.makedirs(os.path.join(EXPORT, "mobile"), exist_ok=True)
    total_web = pantallas_web()
    total_movil = pantallas_movil()
    print(f"Web: {total_web} pantallas")
    print(f"Movil: {total_movil} pantallas")
    print(f"Salida: {EXPORT}")


if __name__ == "__main__":
    main()

