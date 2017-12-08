package net.pterodactylus.icynfo

import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import kotlin.concurrent.timer

fun main(args: Array<String>) {
	System.setProperty("apple.awt.UIElement", "true")
	val systemTray = systemTray ?: throw UnsupportedOperationException("System tray is not supported")
	val image = awtToolkit.getImage(Server::class.java.getResource("/icecast-logo.png"))!!
	trayIcon = TrayIcon(image)
	systemTray.add(trayIcon)
	timer("Update Icynfo Status", period = 15000) {
		trayIcon.toolTip = servers
				.map { it to it.getInfo() }
				.map { (server, infoXml) ->
					if (infoXml == null) {
						"${server.hostname}: offline"
					} else {
						infoXml["source"].let { sources ->
							when (sources.size) {
								0 -> "${server.hostname}: no sources"
								1 -> "${server.hostname}: ${sources.first()["listeners"].first().text} listeners"
								else -> sources.joinToString("\n", "Host ${server.hostname}:\n") { "  Source ${it.attributes["mount"]}: ${it["listeners"].first().text} listeners" }
							}
						}
					}
				}
				.joinToString("\n")
	}
}

private val awtToolkit by lazy { Toolkit.getDefaultToolkit()!! }
private val systemTray by lazy { if (SystemTray.isSupported()) SystemTray.getSystemTray() else null }

private val servers = mutableListOf<Server>(
)

private lateinit var trayIcon: TrayIcon

class Server(val hostname: String, val username: String, val password: String)

private fun Server.getInfo() = try {
	(URL("https://$hostname/admin/stats")
			.openConnection() as HttpURLConnection)
			.apply {
				addRequestProperty("Authorization", "Basic ${(username + ":" + password).toBase64()}")
			}
			.inputStream
			.toXmlNode()
} catch (_: Exception) {
	null
}

private fun String.toBase64() = Base64.getEncoder().encodeToString(toByteArray())