# Phase 5: Ensure data/larion and data/minecraft/worldgen exist.
# 1) Copy world -> larion/worldgen/density_function/overworld
# 2) Copy from source: larion/worldgen/noise, minecraft/worldgen (noise_settings, density_function/overworld redirects, configured_carver)

$ErrorActionPreference = "Stop"
$NewRoot = $PSScriptRoot
$NewData = Join-Path $NewRoot "src\main\resources\data"
$SrcData = Join-Path $NewRoot "..\larion-world-generation-1.20.1\common\src\main\resources\data"

# 1. world -> larion/worldgen/density_function/overworld
$worldSrc = Join-Path $NewData "world"
$larionOverworld = Join-Path $NewData "larion\worldgen\density_function\overworld"
if (Test-Path $worldSrc) {
    Get-ChildItem -Path $worldSrc -Recurse -File -Filter "*.json" | ForEach-Object {
        $rel = $_.FullName.Substring($worldSrc.Length).TrimStart('\')
        $dest = Join-Path $larionOverworld $rel
        $destDir = Split-Path $dest
        if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
        Copy-Item -Path $_.FullName -Destination $dest -Force
    }
    Write-Host "Copied world -> larion/worldgen/density_function/overworld"
}

# 2. From source: larion noise + minecraft worldgen
if (Test-Path $SrcData) {
    $larionNoiseSrc = Join-Path $SrcData "larion\worldgen\noise"
    $larionNoiseDst = Join-Path $NewData "larion\worldgen\noise"
    if (Test-Path $larionNoiseSrc) {
        Copy-Item -Path $larionNoiseSrc -Destination $larionNoiseDst -Recurse -Force
        Write-Host "Copied larion worldgen noise"
    }
    $mcWorldgenSrc = Join-Path $SrcData "minecraft\worldgen"
    $mcWorldgenDst = Join-Path $NewData "minecraft\worldgen"
    if (Test-Path $mcWorldgenSrc) {
        Copy-Item -Path $mcWorldgenSrc -Destination $mcWorldgenDst -Recurse -Force
        Write-Host "Copied minecraft worldgen"
    }
} else {
    Write-Host "Source data not found at $SrcData - only world->larion overworld was copied"
}

# 3. If rworld.json exists (Larion noise_settings), put it in minecraft worldgen
$rworld = Join-Path $NewData "rworld.json"
$noiseSettingsDir = Join-Path $NewData "minecraft\worldgen\noise_settings"
$overworldJson = Join-Path $noiseSettingsDir "overworld.json"
if (Test-Path $rworld) {
    if (-not (Test-Path $noiseSettingsDir)) { New-Item -ItemType Directory -Path $noiseSettingsDir -Force | Out-Null }
    Copy-Item -Path $rworld -Destination $overworldJson -Force
    Write-Host "Copied rworld.json -> minecraft/worldgen/noise_settings/overworld.json"
}

Write-Host "Done."
