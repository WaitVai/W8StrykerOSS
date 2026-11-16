<p align="center">
  <img src="./W8StrykerOSS%20Logo.png" alt="W8StrykerOSS Logo" width="180" style="filter: drop-shadow(0 0 25px rgba(255, 23, 68, 0.6));" />
</p>

<h1 align="center" style="font-weight: 800; font-size: 2.8rem; margin-top: 10px;">
  W8StrykerOSS
</h1>

<p align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&size=24&duration=3000&pause=1000&color=FF1744&center=true&vCenter=true&width=650&lines=W8StrykerOSS+v6.5.1;Advanced+Mobile+Pentest+%26+Security+Suite;Auto+WPS+PBC+%26+Pixie+Dust+Live+Crack;Debian+Trixie+arm64+Root+%26+Rootless+VM;Join+Telegram+Community%3A+t.me%2FW8TeamNew" alt="Typing Banner" />
</p>

<p align="center">
  <a href="https://github.com/WaitVai/W8StrykerOSS">
    <img src="https://visitor-badge.laobi.icu/badge?page_id=WaitVai.W8StrykerOSS&title=REPOSITORY%20VIEWS" height="28" alt="Repository Views" />
  </a>
</p>

<p align="center">
  <a href="https://t.me/W8TeamNew">
    <img src="https://img.shields.io/badge/DOWNLOAD-APK%20v6.5.1-00E676?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" />
  </a>
  <a href="https://t.me/W8TeamNew">
    <img src="https://img.shields.io/badge/TELEGRAM-@W8TeamNew-229ED9?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram Community" />
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/LICENSE-GNU%20GPL%20v3.0-blue?style=for-the-badge" alt="License GPL v3.0" />
  </a>
  <a href="https://github.com/WaitVai/W8StrykerOSS">
    <img src="https://img.shields.io/badge/PLATFORM-Android%20%7C%20ARM64-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android ARM64" />
  </a>
  <a href="https://github.com/WaitVai/W8StrykerOSS">
    <img src="https://img.shields.io/badge/ENGINE-Debian%20Trixie-A81D33?style=for-the-badge&logo=debian&logoColor=white" alt="Debian Trixie Engine" />
  </a>
</p>

<p align="center">
  <a href="https://t.me/W8TeamNew">
    <img src="https://img.shields.io/badge/📥%20GET%20APK%20FROM%20TELEGRAM%20(v6.5.1)-FF1744?style=for-the-badge&logo=telegram&logoColor=white" height="40" alt="Get APK from Telegram" />
  </a>
</p>

---

## 🌟 Overview

**W8StrykerOSS** is an advanced, free, and open-source mobile penetration testing suite built for Android devices. Designed for authorized security assessments, research, and wireless auditing, it turns your smartphone into a comprehensive cybersecurity workstation.

Under the hood, **W8StrykerOSS** runs a full **Debian Trixie (arm64)** native Linux environment under `/data/local/stryker/release` with hardware acceleration. Industry-standard offensive security tools—including **Nmap, Metasploit Framework, Nuclei, Hydra, Hashcat, and SearchSploit**—execute at native performance, managed through an ultra-sleek modern UI.

When running without root access, an integrated **Rootless Linux VM (QEMU / UML)** seamlessly boots the exact same Debian rootfs.

---

## ⚡ What's New in v6.5.1

- 🛡️ **Brand & Namespace Migration**: Unified package name `com.w8stryker.oss` with modern high-contrast W8 shield branding.
- 📡 **Auto WPS PBC Capture & Connect**: Full push-button Wi-Fi sweeps that disregard PIN lock flags (WPS PIN lock does not affect physical PBC mode), automatically acquiring credentials and connecting.
- ✨ **Auto Pixie Dust Live Crack View**: Real-time dialog displaying cracked networks with **haptic vibration** and **dynamic 4-corner screen glow animations**.
- 🔑 **Streamlined WPS PIN Entry**: Instant PIN input dialog with clear 12-hour timestamp formatting.
- 💬 **Official Telegram Community**: Fast launch prompt connecting users directly to [@W8TeamNew](https://t.me/W8TeamNew).
- 🎨 **Redesigned Launcher & System Icons**: Replaced all legacy icons with custom dark-themed shield assets across notifications and app launchers.

---

## 🚀 Key Modules & Capabilities

| Module | Description | Highlights |
|---|---|---|
| 📡 **Wi-Fi Networks** | Comprehensive wireless scanner & attack engine | Auto WPS PBC Capture, Pixie Dust attack, PIN brute-force, handshake capture & deauth. |
| 🔑 **Handshakes** | Secure on-device handshake storage & analysis | Direct offline cracking via Hashcat, export to OnlineHashCrack, and PCAP sharing. |
| 🛡️ **WhisperPair (BLE)** | Fast Pair Bluetooth exploit suite | CVE-2025-36911 check, RAW/RETROACTIVE/EXTENDED_RESPONSE chains, account-key write & HFP audio tap. |
| 🌐 **Local Network** | Local subnet discovery & service auditing | Deep Nmap host scans, OS fingerprinting, open port inspection, and one-tap exploit execution. |
| ⚡ **HID Attacks** | BadUSB / DuckyScript hardware injection | Full DuckyScript v1+v3 engine, 7 keyboard layouts (US, GB, DE, FR, ES, IT, RU), live terminal output. |
| 🧰 **USB Arsenal** | ConfigFS USB gadget profile manager | Emulate keyboard, mouse, mass storage, RNDIS/ECM/ACM, and mount bootable `.iso`/`.img` disks. |
| 🎯 **Metasploit** | Full-fledged Metasploit Framework console | Embedded console, multi-session management, payload generator, and exploit module browser. |
| 🔍 **Web Scanner** | Web security engine powered by Nuclei | Multi-target concurrent scans with CVE detection and structured vulnerability reporting. |
| 🗺️ **GeoMac** | Visual geolocation mapping | Plot captured access points on interactive OpenStreetMap with KML/CSV exports. |
| 💻 **Integrated Terminal** | Instant access Debian terminal shell | Multi-tab NeoTerm console dropping directly into the arm64 chroot environment. |

---

## 📲 Quick Installation

### Option 1: Direct APK Download (Recommended)
1. Download the latest release APK from our official community: [**W8StrykerOSS.apk (Telegram: @W8TeamNew)**](https://t.me/W8TeamNew)
2. Sideload and install the APK on your Android device (`Android 7.0+`).
3. Launch **W8StrykerOSS**:
   - **Rooted Devices**: Grant root access (`su`) via Magisk / KernelSU / APatch. The initial setup will automatically download and mount the Debian Trixie rootfs (`chroot64-debian.tar.gz`).
   - **Non-Rooted Devices**: Select Rootless VM mode. The embedded QEMU / User-Mode Linux (UML) engine will boot the environment in guest mode.

### Option 2: ADB Install
```bash
adb install -r W8StrykerOSS.apk
```

---

## 🛠️ Hardware Requirements

- **Device Architecture**: `arm64-v8a` (64-bit ARM).
- **Operating System**: Android 7.0 (API 24) to Android 15+.
- **Free Storage**: ~1.5 GB recommended for the full Debian rootfs and security databases.
- **Wireless Adapter (for Monitor Mode / Packet Injection)**:
  - Atheros AR9271 (`ath9k_htc`)
  - Realtek RTL8812AU / RTL8814AU / RTL8821AU
  - Ralink / MediaTek RT3070 / RT5370 / MT7601U
- **Kernel Support (Optional for USB Arsenal & HID Attacks)**:
  - Gadget ConfigFS enabled (`CONFIG_USB_CONFIGFS=y`, `CONFIG_USB_CONFIGFS_F_HID=y`, `CONFIG_USB_CONFIGFS_MASS_STORAGE=y`).

---

## 💻 Building from Source

```bash
# Clone the repository
git clone https://github.com/WaitVai/W8StrykerOSS.git
cd W8StrykerOSS

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

Generated APKs will be located in `app/build/outputs/apk/`.

---

## 💬 Community & Support

Join the official community for updates, announcements, wireless penetration tutorials, and fast support:

<p align="center">
  <a href="https://t.me/W8TeamNew">
    <img src="https://img.shields.io/badge/JOIN%20TELEGRAM%20CHANNEL-@W8TeamNew-229ED9?style=for-the-badge&logo=telegram&logoColor=white" height="42" alt="Join Telegram" />
  </a>
</p>

- **Telegram Channel**: [@W8TeamNew](https://t.me/W8TeamNew)
- **Bug Reports & Issues**: [W8StrykerOSS Issues](https://github.com/WaitVai/W8StrykerOSS/issues)

---

## 💖 Special Thanks & Credits

- Special thanks to [**stryker.zalexdev.com**](https://stryker.zalexdev.com) for the original Stryker foundation, concepts, and research.

---

## ⚖️ Disclaimer & License

> [!WARNING]
> **W8StrykerOSS** is developed exclusively for authorized security auditing, educational purposes, and defensive vulnerability assessments. Utilizing these tools against networks or systems without explicit prior permission is strictly illegal. The developers assume no liability for misuse.

Released under the **GNU General Public License v3.0** ([GPL-3.0](LICENSE)).

<p align="center">
  <sub>Developed &amp; Maintained by <b>W8Team</b> · &copy; 2026 W8StrykerOSS</sub>
</p>
