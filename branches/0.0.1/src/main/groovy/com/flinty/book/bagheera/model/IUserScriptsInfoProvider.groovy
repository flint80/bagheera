package com.flinty.book.bagheera.model

import com.flinty.book.bagheera.common.FileInfo


/**
 * implementation provides a list of user scripts
 *
 */
interface IUserScriptsInfoProvider {

	List<FileInfo> getFiles()
}
