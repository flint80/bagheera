package com.flinty.book.bagheera.model

import java.util.List

import javax.swing.JButton
import javax.swing.JComponent

/**
 * it is an interface for child windows which can be shown inside MainFrame
 *
 */
interface IContentProvider {

	JComponent getContent()

	List<JButton> getToolbarButtons()

	void updateContent()

	boolean isNavigationAllowed()
}
