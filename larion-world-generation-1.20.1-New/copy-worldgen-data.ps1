# Copy worldgen data from larion-world-generation-1.20.1 to this project.
# Run from repo root: .\copy-worldgen-data.ps1
# Source: ..\larion-world-generation-1.20.1\common\src\main\resources\data
# Dest:   .\src\main\resources\data

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$src  = Join-Path $root "..\larion-world-generation-1.20.1\common\src\main\resources\data"
$dst  = Join-Path $root "src\main\resources\data"

if (-not (Test-Path $src)) {
    Write-Error "Source not found: $src"
}

if (-not (Test-Path $dst)) {
    New-Item -ItemType Directory -Path $dst -Force | Out-Null
}

$files = Get-ChildItem -Path $src -Recurse -File
foreach ($f in $files) {
    $rel = $f.FullName.Substring($src.Length).TrimStart('\')
    $target = Join-Path $dst $rel
    $dir = Split-Path $target
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    Copy-Item -Path $f.FullName -Destination $target -Force
}
Write-Host "Copied $($files.Count) files to $dst"
