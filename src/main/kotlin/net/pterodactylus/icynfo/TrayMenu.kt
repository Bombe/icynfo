package net.pterodactylus.icynfo

import java.awt.Menu
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.TrayIcon
import javax.swing.JOptionPane

class TrayMenu(private val icynfo: Icynfo, trayIcon: TrayIcon, private val quit: () -> Unit) {

	private val deleteMenu = Menu("Delete Server")

	init {
		trayIcon.popupMenu = createPopupMenu()
		rebuildDeleteMenu()
	}

	private fun createPopupMenu() =
			PopupMenu().apply {
				add(MenuItem("Add Server").apply {
					addActionListener { addServer() }
				})
				add(deleteMenu)
				addSeparator()
				add(MenuItem("Quit").apply {
					addActionListener { quit() }
				})
			}

	private fun addServer() {
		AddServerDialog().getNewServer()
				?.let(icynfo::addServer)
				?.also { rebuildDeleteMenu() }
	}

	private fun rebuildDeleteMenu() {
		deleteMenu.removeAll()
		icynfo.currentServers.forEach { server ->
			deleteMenu.add(MenuItem(server.hostname)).run {
				addActionListener {
					if (reallyDelete(server)) {
						icynfo.removeServer(server)
						rebuildDeleteMenu()
					}
				}
			}
		}
	}

	private fun reallyDelete(server: Server) =
			JOptionPane.showConfirmDialog(null, arrayOf("Really delete this server?", "${server.username} @ ${server.hostname}"), "Really delete server?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION

}
