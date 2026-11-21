package com.vinz.remote

import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject

/**
 * Server lokal di 127.0.0.1:8765. Node MCP di Termux manggil endpoint ini
 * (loopback, satu device, nggak butuh network/tunnel buat hop ini).
 *
 * Endpoint:
 *   GET  /health
 *   POST /type      {"text": "..."}
 *   POST /tap       {"x": 100, "y": 200}
 *   POST /swipe     {"x1":0,"y1":0,"x2":100,"y2":100,"duration":200}
 *   POST /key       {"key": "home|back|recents|notifications|lock|powerdialog"}
 *   POST /launch    {"package": "com.whatsapp"}
 */
class RemoteHttpServer(port: Int) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val svc = ControlAccessibilityService.instance
            ?: return json(503, mapOf("ok" to false, "error" to "accessibility service belum aktif"))

        return try {
            when (session.method) {
                Method.GET -> when (session.uri) {
                    "/health" -> json(200, mapOf("ok" to true))
                    else -> json(404, mapOf("ok" to false, "error" to "not found"))
                }
                Method.POST -> {
                    val body = HashMap<String, String>()
                    session.parseBody(body)
                    val raw = body["postData"] ?: "{}"
                    val j = JSONObject(raw)
                    when (session.uri) {
                        "/type" -> json(200, mapOf("ok" to svc.typeText(j.getString("text"))))
                        "/tap" -> json(
                            200,
                            mapOf("ok" to svc.tap(j.getDouble("x").toFloat(), j.getDouble("y").toFloat()))
                        )
                        "/swipe" -> json(
                            200,
                            mapOf(
                                "ok" to svc.swipe(
                                    j.getDouble("x1").toFloat(), j.getDouble("y1").toFloat(),
                                    j.getDouble("x2").toFloat(), j.getDouble("y2").toFloat(),
                                    j.optLong("duration", 200L)
                                )
                            )
                        )
                        "/key" -> json(200, mapOf("ok" to svc.globalKey(j.getString("key"))))
                        "/launch" -> json(200, mapOf("ok" to svc.launchApp(j.getString("package"))))
                        else -> json(404, mapOf("ok" to false, "error" to "not found"))
                    }
                }
                else -> json(405, mapOf("ok" to false, "error" to "method not allowed"))
            }
        } catch (e: Exception) {
            json(400, mapOf("ok" to false, "error" to (e.message ?: "bad request")))
        }
    }

    private fun json(code: Int, map: Map<String, Any?>): Response {
        val obj = JSONObject(map)
        val status = when (code) {
            200 -> Response.Status.OK
            400 -> Response.Status.BAD_REQUEST
            404 -> Response.Status.NOT_FOUND
            405 -> Response.Status.METHOD_NOT_ALLOWED
            503 -> Response.Status.SERVICE_UNAVAILABLE
            else -> Response.Status.INTERNAL_ERROR
        }
        return newFixedLengthResponse(status, "application/json", obj.toString())
    }
}
