# Upload All Chroot & Rootless VM Releases to GitHub Releases for W8SOJIB/W8StrykerOSS
param (
    [string]$Token = $env:GH_TOKEN
)

$repo = "W8SOJIB/W8StrykerOSS"
$chrootBase = "D:\W8StrykerOSS\W8Chroot"
$vmBase = "D:\W8StrykerOSS\W8VM"

if (-not $Token) {
    $ghAuth = & "gh" auth status 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Please login to GitHub CLI by running: gh auth login" -ForegroundColor Yellow
        Write-Host "Or run with a Personal Access Token: .\upload_all_releases.ps1 -Token <GITHUB_TOKEN>" -ForegroundColor Yellow
        exit 1
    }
} else {
    $env:GH_TOKEN = $Token
}

function Upload-Or-Create-Release {
    param(
        [string]$Tag,
        [string]$Title,
        [string]$Notes,
        [string[]]$Files
    )
    Write-Host "`n=== Processing Release: $Tag ===" -ForegroundColor Cyan
    
    # Check if release already exists
    & gh release view $Tag --repo $repo 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Release $Tag exists. Uploading assets..." -ForegroundColor Yellow
        foreach ($file in $Files) {
            if (Test-Path $file) {
                Write-Host "  Uploading $(Split-Path $file -Leaf)..."
                & gh release upload $Tag $file --repo $repo --clobber
            }
        }
    } else {
        Write-Host "Creating new release $Tag with assets..." -ForegroundColor Green
        $validFiles = $Files | Where-Object { Test-Path $_ }
        & gh release create $Tag $validFiles --repo $repo --title $Title --notes $Notes
    }
}

# 1. Chroot Releases
Upload-Or-Create-Release -Tag "chroot-650" `
    -Title "Debian Trixie Rootfs v2 (v6.5.0+)" `
    -Notes "Debian trixie arm64 rootfs for Stryker v6.5.0+" `
    -Files @("$chrootBase\chroot-650\chroot64-debian.tar.gz")

Upload-Or-Create-Release -Tag "chroot-main" `
    -Title "Chroot Rootfs Archives" `
    -Notes "Debian and Alpine chroot archives" `
    -Files @(
        "$chrootBase\chroot-main\chroot64-debian.tar.gz",
        "$chrootBase\chroot-main\chroot64.tar.gz",
        "$chrootBase\chroot-main\chroot32.tar.gz"
    )

# 2. Rootless VM Releases
Upload-Or-Create-Release -Tag "rootless-650" `
    -Title "Rootless VM v2 (Kernel 7.2 UML & QEMU)" `
    -Notes "Kernel 7.2 from UML port tree; unified rootfs for QEMU and UML" `
    -Files @(
        "$vmBase\rootless-650\rootfs.imgz",
        "$vmBase\rootless-650\Image",
        "$vmBase\rootless-650\initrd.img",
        "$vmBase\rootless-650\linux-uml",
        "$vmBase\rootless-650\stub_exe"
    )

Upload-Or-Create-Release -Tag "rootless-main" `
    -Title "Rootless VM Main" `
    -Notes "Rootless QEMU emulator binaries, kernel, and fallback rootfs" `
    -Files @(
        "$vmBase\rootless-main\qemu-system-aarch64",
        "$vmBase\rootless-main\libslirp.so",
        "$vmBase\rootless-main\rootfs.imgz",
        "$vmBase\rootless-main\Image",
        "$vmBase\rootless-main\initrd.img"
    )

Write-Host "`n=== All Releases on $repo ===" -ForegroundColor Green
& gh release list --repo $repo
