<#
  update-ostinato.ps1 - make libs/baritone-unoptimized-fabric-1.16.1.jar match Ostinato origin/1.16.1.

  Fetches origin/1.16.1 in the Ostinato-1.16.1 clone. If its commit differs from the stamp in
  libs/ostinato-1.16.1.commit, builds that commit in a throwaway worktree and installs the
  pre-proguard jar (build/libs/baritone-unoptimized-fabric-*.jar) into libs/.

  Gotchas (verified 2026-09-27):
   - Ostinato uses Gradle 4.9, which needs JDK 8. ~/.gradle/gradle.properties pins
     org.gradle.java.home to JDK 21 for TenorClef, so it is overridden on the command line.
   - The :proguard step produces the unoptimized jar TenorClef uses; :createDist fails and is skipped.
   - The jar file is named ...-1.16.5.jar but fabric.mod.json targets minecraft 1.16.1.
#>
[CmdletBinding()]
param(
  [string]$Repo = 'C:\Users\redfa\Documents\MinecraftDev\altoclef',
  [string]$OstinatoClone = 'C:\Users\redfa\Documents\MinecraftDev\Ostinato-1.16.1',
  [string]$Jdk8 = 'C:\Users\redfa\agent-tools\jdk8',
  [string]$Branch = 'origin/1.16.1',
  [switch]$Force
)
$ErrorActionPreference = 'Stop'
$lib = Join-Path $Repo 'libs\baritone-unoptimized-fabric-1.16.1.jar'
$stamp = Join-Path $Repo 'libs\ostinato-1.16.1.commit'

& git -C $OstinatoClone fetch -q origin
if ($LASTEXITCODE -ne 0) { Write-Host "ostinato: fetch failed - keeping current jar" -ForegroundColor Yellow; exit 0 }
$want = (& git -C $OstinatoClone rev-parse --short $Branch).Trim()
$have = if (Test-Path $stamp) { (Get-Content $stamp -Raw).Trim() } else { '' }
if (-not $Force -and $want -eq $have) { Write-Host "ostinato: up to date ($want)" -ForegroundColor DarkGray; exit 0 }

Write-Host "ostinato: building $Branch $want (was '$have')" -ForegroundColor Cyan
$wt = 'C:\ob116'   # short path; the first test build under %TEMP% hung
if (Test-Path $wt) { & git -C $OstinatoClone worktree remove --force $wt 2>$null; Remove-Item -Recurse -Force $wt -EA SilentlyContinue }
& git -C $OstinatoClone worktree add -q --detach $wt $want
if ($LASTEXITCODE -ne 0) { Write-Host "ostinato: worktree failed - keeping current jar" -ForegroundColor Yellow; exit 0 }
try {
  $log = Join-Path $Repo "logs\ostinato-build-$want.log"
  $oldJH = $env:JAVA_HOME; $oldTO = $env:JAVA_TOOL_OPTIONS
  $env:JAVA_HOME = $Jdk8; Remove-Item Env:JAVA_TOOL_OPTIONS -EA SilentlyContinue
  # Detached with redirected streams: Gradle 4.9 hung (idle JVM at compileApiJava) when it inherited a pipe as stdin.
  $nul = Join-Path $env:TEMP 'ostinato-empty-stdin.txt'; Set-Content $nul '' -Encoding ascii
  Start-Process -FilePath (Join-Path $wt 'gradlew.bat') -WorkingDirectory $wt -NoNewWindow -Wait `
    -ArgumentList "-Dorg.gradle.java.home=$($Jdk8 -replace '\\','/')", '-Dorg.gradle.jvmargs=-Xmx3G', '--console=plain', 'build', 'proguard', '-x', 'test', '-x', 'createDist' `
    -RedirectStandardOutput $log -RedirectStandardError "$log.err" -RedirectStandardInput $nul
  $env:JAVA_HOME = $oldJH; if ($oldTO) { $env:JAVA_TOOL_OPTIONS = $oldTO }
  $jar = Get-ChildItem (Join-Path $wt 'build\libs') -Filter 'baritone-unoptimized-fabric-*.jar' -EA SilentlyContinue | Select-Object -First 1
  if (-not $jar) { Write-Host "ostinato: build failed (see $log) - keeping current jar" -ForegroundColor Red; exit 0 }
  Copy-Item $jar.FullName $lib -Force
  (Get-Item $lib).LastWriteTime = Get-Date   # build.gradle picks the newest matching jar
  Set-Content -Path $stamp -Value $want -Encoding ascii
  Write-Host "ostinato: installed $want -> $lib" -ForegroundColor Green
} finally {
  & git -C $OstinatoClone worktree remove --force $wt 2>$null
}
