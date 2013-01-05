package com.flinty.book.bagheera.script.common.htmlbook

import java.io.Serializable

/**
 * Data holder: section of HTMLBook
 *
 */
class HTMLSection  implements Serializable{

	HTMLBookAuthor sectionAuthor

	String htmlContent

	Date sectionDate

	String sectionTitle

	String relatedURL

	List<HTMLSection> nestedSections   =  []
}
