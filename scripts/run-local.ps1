<#
.SYNOPSIS
  Roda a aplicacao localmente SEM Docker, carregando as variaveis do .env na sessao.
  Usa o profile h2 (banco em memoria) por padrao, para testar a integracao AWS real
  (S3/SES/SNS/SQS) sem precisar subir PostgreSQL.

.EXAMPLE
  .\scripts\run-local.ps1
  .\scripts\run-local.ps1 -Profile dev      # se tiver um PostgreSQL local rodando
#>
param(
    [string]$Profile = 'h2',
    [string]$EnvFile = ''
)

$ErrorActionPreference = 'Stop'

# Raiz do repositorio = pasta-pai de \scripts
$RepoRoot = Split-Path -Parent $PSScriptRoot
if (-not $EnvFile) { $EnvFile = Join-Path $RepoRoot '.env' }

if (-not (Test-Path $EnvFile)) {
    throw ".env nao encontrado em: $EnvFile"
}

# Carrega o .env na sessao atual (ignora comentarios e linhas vazias)
Write-Host "Carregando variaveis de $EnvFile ..." -ForegroundColor Cyan
$loaded = 0
foreach ($line in Get-Content $EnvFile) {
    $trimmed = $line.Trim()
    if ($trimmed -eq '' -or $trimmed.StartsWith('#')) { continue }
    $idx = $trimmed.IndexOf('=')
    if ($idx -lt 1) { continue }
    $key = $trimmed.Substring(0, $idx).Trim()
    $val = $trimmed.Substring($idx + 1).Trim()
    if ($val.Length -ge 2) {
        $first = $val.Substring(0, 1)
        $last = $val.Substring($val.Length - 1, 1)
        if (($first -eq '"' -and $last -eq '"') -or ($first -eq "'" -and $last -eq "'")) {
            $val = $val.Substring(1, $val.Length - 2)
        }
    }
    Set-Item -Path ("Env:" + $key) -Value $val
    $loaded++
}

# O profile do parametro tem prioridade sobre o que estiver no .env
$env:SPRING_PROFILES_ACTIVE = $Profile
Write-Host "$loaded variavel(is) carregada(s). Profile ativo: $Profile" -ForegroundColor Green
if ($env:APP_AWS_ENABLED -eq 'true') {
    Write-Host "Integracao AWS HABILITADA (region=$($env:APP_AWS_REGION), bucket=$($env:APP_AWS_S3_BUCKET))." -ForegroundColor Green
} else {
    Write-Host "Integracao AWS desabilitada (APP_AWS_ENABLED diferente de true); usando fallback local." -ForegroundColor Yellow
}

# Garante o Maven no PATH
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    $candidates = @(
        "C:\Users\taryj\apache-maven-3.9.6\bin",
        "C:\Users\taryj\AppData\Local\maven-3.9.9\bin"
    )
    foreach ($c in $candidates) {
        $mvnPath = Join-Path $c 'mvn.cmd'
        if (Test-Path $mvnPath) {
            $env:Path = $c + ';' + $env:Path
            break
        }
    }
}
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven (mvn) nao encontrado no PATH. Ajuste a lista de caminhos no script."
}

# Sobe a aplicacao
Write-Host ""
Write-Host "Iniciando a aplicacao (mvn spring-boot:run)..." -ForegroundColor Cyan
Set-Location $RepoRoot
mvn spring-boot:run ("-Dspring-boot.run.profiles=" + $Profile)
