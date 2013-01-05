

package com.flinty.book.bagheera.common

import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.swing.ImageIcon
import javax.swing.JButton
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature


/**
 * Utility class with few static helper methods
 */
class Utils {

	private final static Log log = LogFactory.getLog(Utils.class)

	private final static Pattern urlPattern = Pattern.compile('http://(.+?)"', Pattern.DOTALL)
	private final static Pattern urlPattern2 = Pattern.compile('.*/([a-z,A-Z,а-я,А-Я,\\-,\\_,0-9]+)\\.[a-z]+', Pattern.DOTALL)


	private final static ObjectMapper mapper = new ObjectMapper()

	static{
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
	}

	/**
	 * checks whether the String is null or empty
	 * @param text String to be checked
	 * @return true if text == null || text.trim().length() == 0
	 */
	static boolean isBlank(String text) {
		return text == null || text.trim().length() == 0
	}

	/**
	 * creates a button
	 * @param ttl button title
	 * @param loc icon location
	 * @param act action for that button
	 * @return JButton
	 */
	static JButton createToolButton(String ttl, String loc, Closure act){
		return new JButton(createToolButtonParams(ttl, loc, act))
	}

	/**
	 * creates a parameters map that can be passed to JButton constructor
	 * @param ttl button title
	 * @param loc icon location
	 * @param act action for that button
	 * @return JButton
	 */
	static Map createToolButtonParams(String title, String iconLocation, Closure action){
		return [text: title, icon: new ImageIcon(Utils.class.getResource(iconLocation)), actionPerformed:action, horizontalTextPosition: 0, verticalTextPosition: 3, focusable: 0]
	}

	/**
	 * Replaces urls form a text with theirs encoded forms 
	 * @param originalContent
	 * @return text with replaced urls
	 */
	static String cleanupUrl(String originalContent){
		if(isBlank(originalContent)){
			return originalContent
		}
		Matcher m = urlPattern.matcher(originalContent)
		Map urls = [:]
		while (m.find()) {
			String url = m.group(1)
			Matcher m2 = urlPattern2.matcher(url)
			if (m2.find()) {
				String part = m2.group(1)
				try {
					String corrected = URLEncoder.encode(part, 'utf-8')
					if (!part.equals(corrected)) {
						urls[url] = url.replace(part, corrected)
					}
				} catch (UnsupportedEncodingException e) {
					log.error('unable to encode url ' + url, e)
					continue
				}
			}
		}
		urls.keySet().each{
			originalContent = originalContent.replace(it,
					urls[it])
		}
		return originalContent
	}

	/**
	 *Cleanup given text to be an appropriate file name 
	 * @param str candidate for a file name
	 * @return String that can be a filename
	 */
	static String cleanupFileName(String str){
		if(!str){
			return ""
		}
		return str.trim().replaceAll(/\p{Punct}|\p{Blank}/, "_")
	}


	/**
	 * Resets given date to the beginning of the day
	 * @param value Date to be reset
	 * @return beginning of the day
	 */
	static Date clearDate(Date value){
		Calendar cal = Calendar.getInstance()
		cal.setTime(value)
		cal.set(Calendar.HOUR_OF_DAY, 0)
		cal.set(Calendar.MINUTE, 0)
		cal.set(Calendar.SECOND, 0)
		cal.set(Calendar.MILLISECOND, 0)
		return cal.getTime()
	}

	/**
	 * Prints xml string representation of the node
	 * @param node Node to be printed
	 */
	static String getNodeContent(Node node){
		def writer = new StringWriter()
		def nodeWriterRoot = new XmlNodePrinter(new PrintWriter(writer))
		nodeWriterRoot.setNamespaceAware(true)
		nodeWriterRoot.print(node)
		return writer.toString()
	}


	static String toJsonString(Object obj){
		if(obj == null){
			return null
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		mapper.writeValue(baos, obj)
		return new String(baos.toByteArray(), "utf-8")
	}
}
