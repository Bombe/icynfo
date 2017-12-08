package net.pterodactylus.icynfo

import com.fasterxml.jackson.databind.ObjectMapper
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

fun main(args: Array<String>) {
	System.setProperty("apple.awt.UIElement", "true")
	val systemTray = systemTray ?: throw UnsupportedOperationException("System tray is not supported")
	val image = awtToolkit.getImage(Server::class.java.getResource("/icecast-logo.png"))!!
	TrayIcon(image).let { trayIcon ->
		systemTray.add(trayIcon)
		Icynfo { trayIcon.toolTip = it }
				.startTimer()
	}
}

private val awtToolkit by lazy { Toolkit.getDefaultToolkit()!! }
private val systemTray by lazy { if (SystemTray.isSupported()) SystemTray.getSystemTray() else null }

class Icynfo(private val updateTooltip: (String) -> Unit) {

	private val servers by lazy { readServers() }
	private val executor = Executors.newSingleThreadScheduledExecutor()!!
	private lateinit var currentTask: ScheduledFuture<*>

	private fun readServers(): MutableList<Server> =
			try {
				ObjectMapper().readTree(File(System.getProperty("user.home"), ".icynfo"))
						.let { config ->
							config["servers"].map {
								Server(it["hostname"].asText(), it["username"].asText(), it["password"].asText())
							}.toMutableList()
						}
			} catch (_: Exception) {
				mutableListOf()
			}

	fun startTimer() {
		currentTask = executor.withFixedDelay(15, SECONDS, action = ::getInfo)
	}

	private fun getInfo() {
		updateTooltip(servers
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
		)
	}

}

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

private fun ScheduledExecutorService.withFixedDelay(delay: Long, unit: TimeUnit, initialDelay: Long = 0, action: () -> Unit) =
		scheduleWithFixedDelay(action, initialDelay, delay, unit)!!
