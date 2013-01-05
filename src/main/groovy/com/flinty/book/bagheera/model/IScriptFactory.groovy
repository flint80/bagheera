package com.flinty.book.bagheera.model



/**
 * implementation is used for creating User script abstraction given a script file
 */
interface IScriptFactory {

	IScript createScript(File scriptFile, Binding binding, IScriptCalback callback)
}
