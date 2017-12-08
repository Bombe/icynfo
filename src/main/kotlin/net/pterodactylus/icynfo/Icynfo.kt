package net.pterodactylus.icynfo

import com.fasterxml.jackson.databind.ObjectMapper
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.Label
import java.awt.MenuItem
import java.awt.Point
import java.awt.PopupMenu
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
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.swing.WindowConstants
import javax.swing.border.EtchedBorder

fun main(args: Array<String>) {
	System.setProperty("apple.awt.UIElement", "true")
	val systemTray = systemTray ?: throw UnsupportedOperationException("System tray is not supported")
	val image = awtToolkit.getImage(Server::class.java.getResource("/icecast-logo.png"))!!
	TrayIcon(image).let { trayIcon ->
		systemTray.add(trayIcon)
		val icynfo = Icynfo { trayIcon.toolTip = it }.apply {
			startTimer()
		}
		trayIcon.popupMenu = createPopupMenu(icynfo)
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

private fun createPopupMenu(icynfo: Icynfo) =
		PopupMenu().apply {
			add(MenuItem("Add Server").apply {
				addActionListener { addServer(icynfo) }
			})
			addSeparator()
			add(MenuItem("Quit").apply {
				addActionListener { quit() }
			})
		}

private fun quit() {
	System.exit(0)
}

private fun addServer(icynfo: Icynfo) {
	AddServerDialog().getNewServer()
			?.let(icynfo::addServer)
}

private fun constrain(gridx: Int, gridy: Int, gridwidth: Int = 1, gridheight: Int = 1, weightx: Double = 1.0, weighty: Double = 1.0, anchor: Int = GridBagConstraints.CENTER, fill: Int = GridBagConstraints.NONE, insets: Insets = Insets(0, 0, 0, 0), ipadx: Int = 0, ipady: Int = 0)
		= GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady)

class AddServerDialog : JDialog(null as JFrame?) {

	private val hostname = JTextField(30)
	private val username = JTextField()
	private val password = JPasswordField()
	@Volatile
	private var serverValid = false

	init {
		isModal = true
		isAlwaysOnTop = true
		layout = BorderLayout()
		title = "Icynfo – Add Server"
		add(JPanel(GridBagLayout()).apply {
			border = BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(12, 12, 12, 12),
					BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)
			)
			add(Label("Hostname"), constrain(0, 0, weightx = 0.0, anchor = GridBagConstraints.LINE_START, insets = Insets(12, 12, 6, 6)))
			add(hostname, constrain(1, 0, anchor = GridBagConstraints.LINE_START, fill = GridBagConstraints.HORIZONTAL, insets = Insets(12, 6, 6, 12)))
			add(Label("Username"), constrain(0, 1, weightx = 0.0, anchor = GridBagConstraints.LINE_START, insets = Insets(6, 12, 6, 6)))
			add(username, constrain(1, 1, anchor = GridBagConstraints.LINE_START, fill = GridBagConstraints.HORIZONTAL, insets = Insets(6, 6, 6, 12)))
			add(Label("Password"), constrain(0, 2, weightx = 0.0, anchor = GridBagConstraints.LINE_START, insets = Insets(6, 12, 12, 6)))
			add(password, constrain(1, 2, anchor = GridBagConstraints.LINE_START, fill = GridBagConstraints.HORIZONTAL, insets = Insets(6, 6, 12, 12)))
		}, BorderLayout.CENTER)
		add(JPanel(BorderLayout()).apply {
			border = BorderFactory.createEmptyBorder(0, 12, 12, 12)
			add(JPanel(BorderLayout()).apply {
				add(JButton("Cancel").apply {
					serverValid = false
					this@AddServerDialog.isVisible = false
				}, BorderLayout.CENTER)
				add(JButton("OK").apply {
					addActionListener {
						serverValid = (hostname.text.trim() != "") and (username.text.trim() != "")
						this@AddServerDialog.isVisible = false
					}
				}, BorderLayout.LINE_END)
			}, BorderLayout.LINE_END)
		}, BorderLayout.PAGE_END)
		defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
		isResizable = false
		pack()
		location = Point((awtToolkit.screenSize.width - width) / 2, (awtToolkit.screenSize.height - height) / 2)
	}

	fun getNewServer(): Server? {
		isVisible = true
		return if (serverValid) Server(hostname.text.trim(), username.text.trim(), password.password.joinToString("")) else null
	}

}
