package com.flinty.book.bagheera.model

import java.io.File

/**
 * implementation is responsible for storing data which is used/generated in scripts
 *
 */
interface IScriptDataStorage {

	Map getMetadata(String scriptFileName)

	void saveMetadata(String id, Map metadata)

	Object getData(String id)

	void saveData(String id, Object metadata)

	File saveTempData(String fileName, byte[] content)
}
