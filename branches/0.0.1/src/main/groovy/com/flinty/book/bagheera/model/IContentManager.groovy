package com.flinty.book.bagheera.model



/**
 * it is an interface for MainFrame function: i.e. to show content of different panels
 *
 */
interface IContentManager {

	void updateProvider(IContentProvider provider)

	void repaint()
}
