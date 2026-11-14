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

  if ($Method -in @("GET", "DELETE", "HEAD", "OPTIONS") -or $null -eq $Body) {
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
$leaderboardHealth = Invoke-Json -Method GET -Url "$BaseUrl/api/leaderboard/health"
$wordHealthText = [string]$wordHealth
$quizHealthText = [string]$quizHealth
$leaderboardHealthText = [string]$leaderboardHealth
if (-not $wordHealthText.Contains("OK")) {
  throw "word health check failed: $wordHealthText"
}
if (-not $quizHealthText.Contains("OK")) {
  throw "quiz health check failed: $quizHealthText"
}
if (-not $leaderboardHealthText.Contains("OK")) {
  throw "leaderboard health check failed: $leaderboardHealthText"
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

  Write-Host "[smoke] checking quiz stop/details flow"
  $quizId = Invoke-Json -Method POST -Url "$BaseUrl/api/quiz/quiz/start?size=3&mode=0&lang=pl"
  $summary = Invoke-Json -Method GET -Url "$BaseUrl/api/quiz/quiz/$quizId/stop/details"
  if ($null -eq $summary -or -not $summary.PSObject.Properties.Name.Contains("scorePercent") -or
      -not $summary.PSObject.Properties.Name.Contains("correctAnswers") -or
      -not $summary.PSObject.Properties.Name.Contains("totalQuestions") -or
      -not $summary.PSObject.Properties.Name.Contains("mode")) {
    throw "quiz stop/details response shape is invalid"
  }

  Write-Host "[smoke] quiz stop/details check passed"

  Write-Host "[smoke] checking leaderboard submit/read flow"
  $entryA = @{ game = "quiz"; mode = 0; username = "smokeA"; scorePercent = 0.8; totalQuestions = 10; correctAnswers = 8 }
  $entryB = @{ game = "quiz"; mode = 0; username = "smokeB"; scorePercent = 0.9; totalQuestions = 5; correctAnswers = 4 }
  $entryC = @{ game = "quiz"; mode = 0; username = "smokeC"; scorePercent = 0.9; totalQuestions = 10; correctAnswers = 9 }
  Invoke-Json -Method POST -Url "$BaseUrl/api/leaderboard/leaderboard/entries" -Body ($entryA | ConvertTo-Json -Compress) | Out-Null
  Invoke-Json -Method POST -Url "$BaseUrl/api/leaderboard/leaderboard/entries" -Body ($entryB | ConvertTo-Json -Compress) | Out-Null
  Invoke-Json -Method POST -Url "$BaseUrl/api/leaderboard/leaderboard/entries" -Body ($entryC | ConvertTo-Json -Compress) | Out-Null

  $topEntries = Invoke-Json -Method GET -Url "$BaseUrl/api/leaderboard/leaderboard/entries/quiz/0?limit=10"
  if ($null -eq $topEntries -or $topEntries.Count -lt 3) {
    throw "leaderboard flow failed: not enough entries in top list"
  }

  $firstThree = @($topEntries | Select-Object -First 3 | ForEach-Object { $_.username })
  if ($firstThree[0] -ne "smokeC" -or $firstThree[1] -ne "smokeB" -or $firstThree[2] -ne "smokeA") {
    throw "leaderboard ranking order mismatch: $($firstThree -join ', ')"
  }

  Write-Host "[smoke] leaderboard ranking check passed"
}
finally {
  Write-Host "[smoke] cleaning up temporary mode"
  try {
    Invoke-WebRequest -Method DELETE -Uri "$BaseUrl/api/quiz/modes/$newModeId" -UseBasicParsing -TimeoutSec 20 | Out-Null
  }
  catch {
    Write-Warning "cleanup failed for mode id ${newModeId}: $($_.Exception.Message)"
  }
}

Write-Host "[smoke] all checks passed"
