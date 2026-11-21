# VinzRemote

APK + bridge buat kontrol HP lewat MCP dari mana aja (via cloudflared quick tunnel).

## Arsitektur

```
internet --(trycloudflare.com URL)--> cloudflared --> MCP server (Node, Termux)
                                                              |
                                                     http://127.0.0.1:8765
                                                              |
                                                     VinzRemote APK (AccessibilityService)
                                                              |
                                                     ketik / tap / swipe / buka app di HP
```

cloudflared TIDAK ditaruh di dalam APK -- binary cloudflared itu dibuild buat Linux,
dan versi yang jalan di Android (yang ada di Termux via `pkg install cloudflared`) itu
build khusus Termux. Jadi tunnel tetap jalan dari sisi Termux, APK cuma jadi "tangan"
yang dipanggil lewat loopback localhost -- satu device, jadi gak masalah.

## 1. Setup APK (di HP)

1. Install APK hasil build GitHub Actions (lihat di bawah).
2. Buka app -> tombol "Buka Settings Accessibility" -> aktifin "VinzRemote".
   Ini satu-satunya langkah manual; Android nggak punya API buat enable accessibility
   service secara otomatis tanpa root.
3. Balik ke app -> tombol "Start Server". Notifikasi foreground service bakal muncul,
   artinya server jalan di `127.0.0.1:8765`.

## 2. Setup bridge (di Termux)

1. Copy `vinzremote-bridge.js` ke project MCP server kamu yang udah ada (`api2.vinzgd.space`).
2. Di server utama, import dan daftarkan tools-nya:
   ```js
   const { tools: controlTools } = require("./vinzremote-bridge.js");
   // merge controlTools ke tools list MCP server kamu yang sudah ada
   ```
3. Jalanin MCP server + cloudflared seperti biasa. Tools baru yang muncul:
   `control_health`, `control_type`, `control_tap`, `control_swipe`, `control_key`, `control_launch`.

## 3. Generate URL quick tunnel

```bash
cloudflared tunnel --url http://localhost:<PORT_MCP_SERVER_KAMU>
```

URL `https://xxxx.trycloudflare.com` yang muncul di log itu yang dipakai client MCP
buat connect. Ini URL sementara dan GANTI tiap kali cloudflared di-restart -- kalau
mau URL yang nggak berubah-ubah, pakai named tunnel (`cloudflared tunnel create ...`)
kayak yang udah kamu pake buat `api2.vinzgd.space`, bukan quick tunnel.

## Build APK

Push ke GitHub, Actions (`.github/workflows/build.yml`) otomatis build debug APK dan
upload sebagai artifact "VinzRemote-debug-apk" -- download dari tab Actions run.

## Catatan

- Debug APK aja (belum signed release) -- cukup buat install manual di HP sendiri.
- `/type` pake `ACTION_SET_TEXT`, fallback ke clipboard+paste kalau field-nya nggak
  support set text langsung.
- Semua endpoint lokal, nggak ada auth -- jangan expose port 8765 ke network luar,
  cuma MCP server di Termux yang boleh manggil lewat loopback.
