# ---------------------------------------------------------------------------
# SEVETCO - Publica los entregables de prueba en la carpeta bin\
#
# Uso:  powershell -ExecutionPolicy Bypass -File tools\publicar.ps1
#
# Deja en bin\ todo lo que el usuario de pruebas necesita:
#   bin\apk\sevetco.apk        aplicacion movil instalable (y en bin\web para
#                              el enlace de descarga de la pagina)
#   bin\web\                   pagina web lista para Live Server
#   bin\db\                    scripts de la base de datos
#   bin\mockups\               mockups exportados
# ---------------------------------------------------------------------------

$raiz = Split-Path -Parent $PSScriptRoot
$bin  = Join-Path $raiz "bin"

Write-Output "Publicando en $bin"

# --- 1. Aplicacion movil ---------------------------------------------------
$apkCompilada = Join-Path $raiz "android\app\build\outputs\apk\debug\app-debug.apk"
$apkBin = Join-Path $bin "apk\sevetco.apk"
New-Item -ItemType Directory -Force -Path (Join-Path $bin "apk") | Out-Null

# Se respeta el APK que este en bin\apk. Solo se reemplaza si hay una
# compilacion de Gradle mas nueva que ese archivo.
$hayBin = Test-Path -LiteralPath $apkBin
$hayCompilada = Test-Path -LiteralPath $apkCompilada
$usarCompilada = $hayCompilada -and (
    -not $hayBin -or
    (Get-Item -LiteralPath $apkCompilada).LastWriteTime -gt
        (Get-Item -LiteralPath $apkBin).LastWriteTime
)

if ($usarCompilada) {
    Copy-Item -LiteralPath $apkCompilada -Destination $apkBin -Force
    Write-Output "  OK  app movil -> bin\apk\sevetco.apk (compilada nueva)"
} elseif ($hayBin) {
    Write-Output "  OK  app movil -> se respeta bin\apk\sevetco.apk"
} else {
    Write-Output "  !!  no se encontro la APK compilada ni bin\apk\sevetco.apk"
}

# --- 2. App web ------------------------------------------------------------
$origenWeb = Join-Path $raiz "web"
$destinoWeb = Join-Path $bin "web"
if (Test-Path -LiteralPath $destinoWeb) {
    Get-ChildItem -LiteralPath $destinoWeb -File -Recurse | Remove-Item -Force
}
New-Item -ItemType Directory -Force -Path $destinoWeb | Out-Null

# copia del codigo de la web
Copy-Item -Path (Join-Path $origenWeb "*") -Destination $destinoWeb -Recurse -Force

# copia de los iconos para que la web de bin sea autocontenida
$destinoIconos = Join-Path $destinoWeb "assets\icons"
New-Item -ItemType Directory -Force -Path $destinoIconos | Out-Null
foreach ($carpeta in @("shared", "web", "mobile")) {
    Copy-Item -Path (Join-Path $raiz "assets\icons\$carpeta") -Destination $destinoIconos -Recurse -Force
}

# la web publicada lee los iconos desde su propia carpeta
$config = Join-Path $destinoWeb "js\firebase-config.js"
if (Test-Path -LiteralPath $config) {
    $texto = Get-Content -LiteralPath $config -Raw
    $texto = $texto -replace "export const ICONOS = '../assets/icons';", "export const ICONOS = './assets/icons';"
    Set-Content -LiteralPath $config -Value $texto -Encoding UTF8
}

$html = Join-Path $destinoWeb "index.html"
if (Test-Path -LiteralPath $html) {
    $texto = Get-Content -LiteralPath $html -Raw
    $texto = $texto -replace '\.\./assets/icons/', './assets/icons/'
    Set-Content -LiteralPath $html -Value $texto -Encoding UTF8
}
Write-Output "  OK  app web -> bin\web\index.html"

# el enlace de descarga de la app movil apunta a este archivo
if (Test-Path -LiteralPath $apkBin) {
    Copy-Item -LiteralPath $apkBin -Destination (Join-Path $destinoWeb "sevetco.apk") -Force
    Write-Output "  OK  descarga de la app -> bin\web\sevetco.apk"
}

# se retiran los nombres anteriores para dejar solo sevetco.apk
foreach ($viejo in @(
        (Join-Path $bin "SEVETCO-debug.apk"),
        (Join-Path $bin "apk\SEVETCO-debug.apk"))) {
    if (Test-Path -LiteralPath $viejo) {
        Remove-Item -LiteralPath $viejo -Force
    }
}

# --- 3. Base de datos (Firebase) -------------------------------------------
$destinoDb = Join-Path $bin "firebase"
New-Item -ItemType Directory -Force -Path $destinoDb | Out-Null
Copy-Item -Path (Join-Path $raiz "firebase\*") -Destination $destinoDb -Recurse -Force
Copy-Item -LiteralPath (Join-Path $raiz "docs\FIREBASE.md") -Destination (Join-Path $destinoDb "LEEME.md") -Force
Write-Output "  OK  base de datos -> bin\firebase\"

# se retira la carpeta de la version anterior con Supabase
$dbAnterior = Join-Path $bin "db"
if (Test-Path -LiteralPath $dbAnterior) {
    Get-ChildItem -LiteralPath $dbAnterior -File -Recurse | Remove-Item -Force
    Remove-Item -LiteralPath $dbAnterior -Recurse -Force
    Write-Output "  OK  se retiro bin\db (era de Supabase)"
}

# --- 4. Mockups ------------------------------------------------------------
$destinoMock = Join-Path $bin "mockups"
New-Item -ItemType Directory -Force -Path $destinoMock | Out-Null
$origenMock = Join-Path $raiz "mockups\export"
if (Test-Path -LiteralPath $origenMock) {
    Copy-Item -Path (Join-Path $origenMock "*") -Destination $destinoMock -Recurse -Force
    Write-Output "  OK  mockups -> bin\mockups\"
}

Write-Output "Listo."

