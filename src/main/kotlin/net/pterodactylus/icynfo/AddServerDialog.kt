package net.pterodactylus.icynfo

import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.Label
import java.awt.Point
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.swing.WindowConstants
import javax.swing.border.EtchedBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class AddServerDialog : JDialog(null as JFrame?) {

	private val enableOkButtonOnHostnameAndUsernameDocumentListener = object : DocumentListener {
		private fun checkInput() {
			okButton.isEnabled = ((hostname.text != "") and (username.text != ""))
		}

		override fun changedUpdate(e: DocumentEvent?) = checkInput()
		override fun insertUpdate(e: DocumentEvent?) = checkInput()
		override fun removeUpdate(e: DocumentEvent?) = checkInput()
	}

	private val hostname: JTextField = JTextField(30).apply { document.addDocumentListener(enableOkButtonOnHostnameAndUsernameDocumentListener) }
	private val username: JTextField = JTextField().apply { document.addDocumentListener(enableOkButtonOnHostnameAndUsernameDocumentListener) }
	private val password = JPasswordField()
	private val okButton = JButton("OK")

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
					addActionListener {
						serverValid = false
						this@AddServerDialog.isVisible = false
					}
				}, BorderLayout.CENTER)
				add(okButton.apply {
					addActionListener {
						serverValid = true
						this@AddServerDialog.isVisible = false
					}
					isEnabled = false
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
