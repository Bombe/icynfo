package net.pterodactylus.icynfo

import java.net.HttpURLConnection
import java.net.URL

data class Server(val hostname: String, val username: String, val password: String) {

	fun getInfo() = try {
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

}
