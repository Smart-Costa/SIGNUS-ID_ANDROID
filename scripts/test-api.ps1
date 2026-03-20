param(
  [ValidateSet('dev','prod')][string]$Env = 'dev',
  [string]$Username = 'Wilbert',
  [string]$Password,
  [string]$ActivoId,
  [int]$TimeoutSec = 15
)

if (-not $Password) {
  Write-Host "Falta -Password" -ForegroundColor Yellow
  exit 1
}

if ($Env -eq 'prod') {
  $base = 'http://srvapppro/ApiHH/api'
} else {
  $base = 'http://44.193.11.135:70/ApiHH/Api'
}

$loginUrl = "$base/auth/login"
$loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json

try {
  $loginRes = Invoke-RestMethod -Method Post -Uri $loginUrl -ContentType 'application/json' -Body $loginBody -TimeoutSec $TimeoutSec -ErrorAction Stop
} catch {
  Write-Host "ERROR login: $($_.Exception.Message)" -ForegroundColor Red
  exit 2
}

$token = $null
if ($loginRes) {
  if ($loginRes.PSObject.Properties.Name -contains 'token') { $token = $loginRes.token }
  elseif ($loginRes.PSObject.Properties.Name -contains 'data' -and $loginRes.data -and $loginRes.data.PSObject.Properties.Name -contains 'token') { $token = $loginRes.data.token }
}

if (-not $token) {
  Write-Host "ERROR: token no recibido" -ForegroundColor Red
  $loginRes | ConvertTo-Json -Depth 5 | Write-Host
  exit 3
}

$headers = @{ Authorization = "Bearer $token" }

if ($ActivoId) {
  $testUrl = "$base/Activos/$ActivoId"
} else {
  $testUrl = "$base/ConsolidacionApi/ConsultarNovedades?username=$([Uri]::EscapeDataString($Username))"
}

$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
  $resp = Invoke-RestMethod -Method Get -Uri $testUrl -Headers $headers -TimeoutSec $TimeoutSec -ErrorAction Stop
  $sw.Stop()
  Write-Host "OK $Env $testUrl ($($sw.ElapsedMilliseconds) ms)" -ForegroundColor Green
  if ($resp) { $resp | ConvertTo-Json -Depth 6 | Write-Output }
} catch {
  $sw.Stop()
  Write-Host "ERROR $Env $testUrl ($($sw.ElapsedMilliseconds) ms): $($_.Exception.Message)" -ForegroundColor Red
  exit 4
}

