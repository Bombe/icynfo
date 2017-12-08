package net.pterodactylus.icynfo

import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

fun String.toXmlNode() = toByteArray().toXmlNode()
fun ByteArray.toXmlNode() = inputStream().toXmlNode()
fun InputStream.toXmlNode() = parseXml(this)

fun parseXml(input: InputStream): XmlNode? =
		tryOrNull {
			documentBuilderFactory
					.newDocumentBuilder()
					.parse(input)
					.toXmlNode()
		}

private fun <T> tryOrNull(block: () -> T): T? = try {
	block()
} catch (e: Exception) {
	e.printStackTrace()
	null
}

private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }

private fun Document.toXmlNode(): XmlNode =
		documentElement.toXmlNode()

private val Node.children: List<Node> get() = (0 until childNodes.length).map(childNodes::item)

private fun Node.toXmlNode(): XmlNode =
		children
				.map(Node::toXmlNode)
				.let { children ->
					(0 until (attributes?.length ?: 0)).map(attributes::item)
							.map { it.nodeName to it.nodeValue }
							.toMap()
							.let { attributes ->
								XmlNode(nodeName, this.textContent, attributes, children)
							}
				}

class XmlNode(val name: String, val text: String, val attributes: Map<String, String> = emptyMap(), private val children: List<XmlNode> = emptyList()) : Iterable<XmlNode> {

	operator fun get(nodeName: String) =
			children.filter { it.name == nodeName }

	override fun iterator() = children.iterator()

}
