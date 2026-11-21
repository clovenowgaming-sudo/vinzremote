# vinzremote-bridge.js
#
# Tambahan tool buat MCP server Termux kamu (api2.vinzgd.space).
# Ini manggil VinzRemote APK lewat loopback (127.0.0.1:8765) -- APK dan Termux
# jalan di device yang sama jadi gak butuh network/tunnel buat hop ini.
#
# Cara pakai: require/import file ini dari server MCP utama kamu, lalu
# daftarkan function-function di bawah sebagai MCP tools (tools/list + tools/call).

const APK_BASE = "http://127.0.0.1:8765";

async function callApk(path, body) {
  const res = await fetch(APK_BASE + path, {
    method: body ? "POST" : "GET",
    headers: body ? { "Content-Type": "application/json" } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`APK bridge error ${res.status}: ${text}`);
  }
  return res.json();
}

const tools = {
  control_health: {
    description: "Cek apakah VinzRemote APK nyala dan accessibility service aktif",
    params: {},
    run: () => callApk("/health"),
  },
  control_type: {
    description: "Ketik teks ke field yang lagi fokus di layar HP",
    params: { text: "string" },
    run: ({ text }) => callApk("/type", { text }),
  },
  control_tap: {
    description: "Tap di koordinat (x, y) layar HP",
    params: { x: "number", y: "number" },
    run: ({ x, y }) => callApk("/tap", { x, y }),
  },
  control_swipe: {
    description: "Swipe dari (x1,y1) ke (x2,y2), duration ms (default 200)",
    params: { x1: "number", y1: "number", x2: "number", y2: "number", duration: "number?" },
    run: ({ x1, y1, x2, y2, duration }) => callApk("/swipe", { x1, y1, x2, y2, duration }),
  },
  control_key: {
    description: "Tombol global: home | back | recents | notifications | lock | powerdialog",
    params: { key: "string" },
    run: ({ key }) => callApk("/key", { key }),
  },
  control_launch: {
    description: "Buka app lewat package name, contoh com.whatsapp",
    params: { package: "string" },
    run: ({ package: pkg }) => callApk("/launch", { package: pkg }),
  },
};

module.exports = { tools };
