param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Invoke-Json {
  param(
    [string]$Method,
    [string]$Url,
    [string]$Body = $null
  )

  if ($null -eq $Body) {
    return Invoke-RestMethod -Method $Method -Uri $Url -TimeoutSec 20
  }

  return Invoke-RestMethod -Method $Method -Uri $Url -Body $Body -ContentType "application/json" -TimeoutSec 20
}

Write-Host "[smoke] checking frontend root"
$rootResponse = Invoke-WebRequest -Uri "$BaseUrl/" -UseBasicParsing -TimeoutSec 20
if ($rootResponse.StatusCode -ne 200) {
  throw "frontend root returned status $($rootResponse.StatusCode)"
}

Write-Host "[smoke] checking backend health endpoints through nginx proxy"
$wordHealth = Invoke-Json -Method GET -Url "$BaseUrl/api/word/health"
$quizHealth = Invoke-Json -Method GET -Url "$BaseUrl/api/quiz/health"
if (-not $wordHealth.status.EndsWith("OK")) {
  throw "word health check failed: $($wordHealth | ConvertTo-Json -Compress)"
}
if (-not $quizHealth.status.EndsWith("OK")) {
  throw "quiz health check failed: $($quizHealth | ConvertTo-Json -Compress)"
}

Write-Host "[smoke] checking quiz mode read path"
$modesBefore = Invoke-Json -Method GET -Url "$BaseUrl/api/quiz/modes"
if ($null -eq $modesBefore) {
  throw "cannot read modes list"
}

Write-Host "[smoke] creating temporary mode"
$newMode = Invoke-Json -Method POST -Url "$BaseUrl/api/quiz/modes"
$newModeId = [int]$newMode.id

try {
  Write-Host "[smoke] restarting quiz container to verify persistence"
  docker compose restart service-quiz | Out-Null
  Start-Sleep -Seconds 5

  $modesAfterRestart = Invoke-Json -Method GET -Url "$BaseUrl/api/quiz/modes"
  $found = $false
  foreach ($mode in $modesAfterRestart) {
    if ([int]$mode.id -eq $newModeId) {
      $found = $true
      break
    }
  }

  if (-not $found) {
    throw "mode id $newModeId was not found after quiz restart"
  }

  Write-Host "[smoke] persistence check passed"
}
finally {
  Write-Host "[smoke] cleaning up temporary mode"
  try {
    Invoke-WebRequest -Method DELETE -Uri "$BaseUrl/api/quiz/modes/$newModeId" -UseBasicParsing -TimeoutSec 20 | Out-Null
  }
  catch {
    Write-Warning "cleanup failed for mode id $newModeId: $($_.Exception.Message)"
  }
}

Write-Host "[smoke] all checks passed"
