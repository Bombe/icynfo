package net.pterodactylus.icynfo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Test

/**
 * Unit tests for XML functionality.
 */
class XmlTest {

	@Test
	fun `parser returns null for empty input stream`() {
		assertThat(parseXml("".toByteArray().inputStream()), nullValue())
	}

	private fun String.readXml() =
			XmlTest::class.java.getResourceAsStream(this)!!

	@Test
	fun `xml document with only root node can be parsed`() {
	    assertThat(parseXml("xml-root-node-only.xml".readXml()), notNullValue())
	}

	@Test
	fun `xml document with only root node has correct root node name`() {
		assertThat(parseXml("xml-root-node-only.xml".readXml())?.name, equalTo("root"))
	}

	@Test
	fun `xml document with only root node has correct attributes`() {
		assertThat(parseXml("xml-root-node-only.xml".readXml())?.attributes, equalTo(mapOf("foo" to "bar")))
	}

	@Test
	fun `xml document with only root node has correct text`() {
		assertThat(parseXml("xml-root-node-only.xml".readXml())?.text, equalTo("\n\ttext\n"))
	}


}
