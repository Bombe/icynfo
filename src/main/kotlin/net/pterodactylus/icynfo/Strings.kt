package net.pterodactylus.icynfo

import java.util.Base64

fun String.toBase64() = Base64.getEncoder().encodeToString(toByteArray())!!
