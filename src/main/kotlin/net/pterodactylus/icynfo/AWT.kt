package net.pterodactylus.icynfo

import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.SystemTray
import java.awt.Toolkit

val awtToolkit by lazy { Toolkit.getDefaultToolkit()!! }
val systemTray by lazy { if (SystemTray.isSupported()) SystemTray.getSystemTray() else null }

fun constrain(gridx: Int, gridy: Int, gridwidth: Int = 1, gridheight: Int = 1, weightx: Double = 1.0, weighty: Double = 1.0, anchor: Int = GridBagConstraints.CENTER, fill: Int = GridBagConstraints.NONE, insets: Insets = Insets(0, 0, 0, 0), ipadx: Int = 0, ipady: Int = 0)
		= GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady)
