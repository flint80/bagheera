package com.flinty.book.bagheera.model

import com.flinty.book.bagheera.common.Message



/**
 * abstraction on Bagheera API that is available at scripts
 *
 */
interface IScriptCalback {

	void updateProgress(int progress, String message)

	void showMessage(Message message)

	void setOutputFile(File outputFile)

	void setWorkFinished()

	Map getParameters(List definitions)

	void debug(Binding binding)
}
