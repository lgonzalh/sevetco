# Utilidades para administrar el proyecto Firebase SEVETCO desde PowerShell
# usando las credenciales ya autenticadas de firebase-tools (cuenta propietaria).

$script:FbConfigPath = "C:\Users\lgonz\.config\configstore\firebase-tools.json"

function Get-FbToken {
    param([string]$ProjectId = "sevetco-dd27e")
    $cfg = Get-Content $script:FbConfigPath -Raw | ConvertFrom-Json
    $rt = $cfg.tokens.refresh_token
    $cid = "563584335869-fgrhgmd47bqnekij5i8b5pr03ho849e6.apps.googleusercontent.com"
    $csec = "j9iVZfS8kkCEFUPaAeJV0sAi"
    $body = "grant_type=refresh_token&refresh_token=$rt&client_id=$cid&client_secret=$csec"
    $t = Invoke-RestMethod -Method Post -Uri "https://oauth2.googleapis.com/token" `
        -ContentType "application/x-www-form-urlencoded" -Body $body
    return $t.access_token
}

function Invoke-FbAuth {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [string]$Method = "Get",
        $Body = $null,
        [string]$ProjectId = "sevetco-dd27e"
    )
    $tok = Get-FbToken -ProjectId $ProjectId
    $uri = "https://identitytoolkit.googleapis.com/v1/projects/$ProjectId$Path"
    $args = @{ Method = $Method; Uri = $uri; Headers = @{ Authorization = "Bearer $tok" } }
    if ($Body) {
        $args.ContentType = "application/json"
        $args.Body = ($Body | ConvertTo-Json -Depth 10 -Compress)
    }
    return Invoke-RestMethod @args
}

function Invoke-FbFirestore {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [string]$Method = "Get",
        $Body = $null,
        [string]$ProjectId = "sevetco-dd27e"
    )
    $tok = Get-FbToken -ProjectId $ProjectId
    $uri = "https://firestore.googleapis.com/v1/projects/$ProjectId/databases/(default)/documents$Path"
    $args = @{ Method = $Method; Uri = $uri; Headers = @{ Authorization = "Bearer $tok" } }
    if ($Body) {
        $args.ContentType = "application/json"
        $args.Body = ($Body | ConvertTo-Json -Depth 20 -Compress)
    }
    return Invoke-RestMethod @args
}
