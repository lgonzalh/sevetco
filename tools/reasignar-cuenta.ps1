# Vuelve a enlazar la ficha de un usuario cuando su cuenta de Firebase cambia de
# identificador (por ejemplo si la borras y la creas otra vez en la consola).
#
# Uso:
#   powershell -File tools\reasignar-cuenta.ps1 -Correo langonzalezh@poligran.edu.co -UidNuevo e6a261jHFmZZiwJNjBfC73GsLNh1
#
# Obtener el identificador nuevo: consola de Firebase > Authentication > Users.

param(
    [Parameter(Mandatory = $true)][string]$Correo,
    [Parameter(Mandatory = $true)][string]$UidNuevo
)

. "$PSScriptRoot\fbadmin.ps1"

$coleccionesConPropietario = @(
    "mascota", "cita", "suscripcion", "trazabilidad_cita",
    "ruta_desplazamiento", "agenda", "pago", "atencion"
)

# 1) Ubicar la ficha actual por el correo
$ficha = $null
$uidViejo = $null
foreach ($d in (Invoke-FbFirestore -Path "/usuario?pageSize=300").documents) {
    if ($d.fields.correo.stringValue -eq $Correo) {
        $ficha = $d
        $uidViejo = ($d.name -replace '.*/', '')
    }
}

if (-not $ficha) {
    Write-Host "No hay ninguna ficha en la coleccion 'usuario' con el correo $Correo."
    return
}
if ($uidViejo -eq $UidNuevo) {
    Write-Host "La ficha ya esta enlazada al identificador $UidNuevo. No hay nada que hacer."
    return
}

Write-Host "Ficha encontrada: usuario/$uidViejo ($Correo, $($ficha.fields.tipo_usuario.stringValue))"

# 2) Copiar la ficha al identificador nuevo y retirar la vieja
Invoke-FbFirestore -Path "/usuario/$UidNuevo" -Method Patch -Body @{ fields = $ficha.fields } | Out-Null
Write-Host "  usuario/$UidNuevo creado"

$proveedorViejo = $null
try { $proveedorViejo = Invoke-FbFirestore -Path "/proveedor/$uidViejo" } catch { }
if ($proveedorViejo) {
    $campos = $proveedorViejo.fields
    $campos.id_usuario = @{ stringValue = $UidNuevo }
    Invoke-FbFirestore -Path "/proveedor/$UidNuevo" -Method Patch -Body @{ fields = $campos } | Out-Null
    Invoke-FbFirestore -Path "/proveedor/$uidViejo" -Method Delete | Out-Null
    Write-Host "  proveedor/$UidNuevo creado"
}

# 3) Actualizar las referencias que quedaron con el identificador viejo
$cambios = 0
foreach ($col in $coleccionesConPropietario) {
    $lista = $null
    try { $lista = Invoke-FbFirestore -Path "/$col`?pageSize=300" } catch { continue }
    foreach ($d in $lista.documents) {
        $id = ($d.name -replace '.*/', '')
        foreach ($campo in @("id_propietario", "id_proveedor")) {
            if ($d.fields.$campo -and $d.fields.$campo.stringValue -eq $uidViejo) {
                Invoke-FbFirestore -Path "/$col/$id`?updateMask.fieldPaths=$campo" -Method Patch `
                    -Body @{ fields = @{ $campo = @{ stringValue = $UidNuevo } } } | Out-Null
                Write-Host "  $col/$id ($campo)"
                $cambios++
            }
        }
    }
}
Invoke-FbFirestore -Path "/usuario/$uidViejo" -Method Delete | Out-Null
Write-Host "Listo. Referencias actualizadas: $cambios"
