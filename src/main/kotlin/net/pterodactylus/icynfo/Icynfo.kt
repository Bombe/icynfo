package net.pterodactylus.icynfo

import com.fasterxml.jackson.databind.ObjectMapper
import java.awt.TrayIcon
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.SECONDS

fun main(args: Array<String>) {
	System.setProperty("apple.awt.UIElement", "true")
	val systemTray = systemTray ?: throw UnsupportedOperationException("System tray is not supported")
	val image = awtToolkit.getImage(Server::class.java.getResource("/icecast-logo.png"))!!
	TrayIcon(image).let { trayIcon ->
		systemTray.add(trayIcon)
		val icynfo = Icynfo { trayIcon.toolTip = it }.apply {
			startTimer()
		}
		TrayMenu(icynfo, trayIcon, { System.exit(0) })
	}
}

class Icynfo(private val updateTooltip: (String) -> Unit) {

	val currentServers: List<Server> get() = servers
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

	private fun saveServers() =
			ObjectMapper().writeValue(File(System.getProperty("user.home"), ".icynfo"), ObjectMapper().createObjectNode().apply {
				putArray("servers").apply {
					servers.forEach { server ->
						addObject().apply {
							put("hostname", server.hostname)
							put("username", server.username)
							put("password", server.password)
						}
					}
				}
			})

	fun startTimer() {
		currentTask = executor.withFixedDelay(15, SECONDS, action = ::getInfo)
	}

	fun addServer(server: Server) {
		servers += server
		saveServers()
	}

	fun removeServer(server: Server) {
		servers -= server
		saveServers()
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
