# Upload Chroot Release Assets to GitHub Releases for W8SOJIB/W8StrykerOSS
param (
    [string]$Token = $env:GH_TOKEN
)

$repo = "W8SOJIB/W8StrykerOSS"
$baseDir = "D:\W8StrykerOSS\W8Chroot"

if (-not $Token) {
    # Check if gh CLI is authenticated
    $ghAuth = & "gh" auth status 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Please login to GitHub CLI by running: gh auth login" -ForegroundColor Yellow
        Write-Host "Or provide a personal access token: .\upload_chroot_releases.ps1 -Token <YOUR_GITHUB_TOKEN>" -ForegroundColor Yellow
        exit 1
    }
} else {
    $env:GH_TOKEN = $Token
}

Write-Host "=== Creating Release 'chroot-650' ===" -ForegroundColor Cyan
& gh release create chroot-650 "$baseDir\chroot-650\chroot64-debian.tar.gz" --repo $repo --title "Debian Trixie Rootfs v2 (v6.5.0+)" --notes "Debian trixie arm64 rootfs for Stryker v6.5.0+"

Write-Host "`n=== Creating Release 'chroot-main' ===" -ForegroundColor Cyan
& gh release create chroot-main "$baseDir\chroot-main\chroot64-debian.tar.gz" "$baseDir\chroot-main\chroot64.tar.gz" "$baseDir\chroot-main\chroot32.tar.gz" --repo $repo --title "Chroot Rootfs Archives" --notes "Debian and Alpine chroot archives"

Write-Host "`n=== Verifying Releases ===" -ForegroundColor Green
& gh release list --repo $repo
