# ---------------------------------------------------------------------------
# SEVETCO - Publica la app web en Firebase Hosting.
#
# Uso:  powershell -ExecutionPolicy Bypass -File tools\desplegar-web.ps1
#
# Antes conviene ejecutar tools\publicar.ps1 para que bin\web quede al dia.
# La primera vez puede pedir iniciar sesion con: npx firebase-tools login
# ---------------------------------------------------------------------------

$raiz = Split-Path -Parent $PSScriptRoot

Write-Output "Publicando bin\web en Firebase Hosting (sevetco-dd27e)..."

Push-Location $raiz
try {
    npx --yes firebase-tools deploy --only hosting --project sevetco-dd27e
} finally {
    Pop-Location
}

Write-Output ""
Write-Output "Sitio: https://sevetco-dd27e.web.app"

