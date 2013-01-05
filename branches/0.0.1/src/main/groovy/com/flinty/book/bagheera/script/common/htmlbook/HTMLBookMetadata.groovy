package com.flinty.book.bagheera.script.common.htmlbook


/**
 * Data holder: HTMLBook metadata
 *
 */
class HTMLBookMetadata  implements Serializable{

	String genre = 'science'

	HTMLBookAuthor author = new HTMLBookAuthor()

	String bookTitle

	Date publishDate

	String coverPageURL

	String lang = 'ru'
}
