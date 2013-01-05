package com.flinty.book.bagheera.model


/**
 * it is a registry that is globally available
 *
 */
class Environment {


	static IScriptFactory scriptFactory

	static IConfigurationManager configurationManager

	static IUserScriptsInfoProvider userScriptsProvider

	static IContentManager contentManager

	static IScriptDataStorage scriptDataStorage

	static INmdWrapper nmdWrapper

	static List disposables = new LinkedList()

	static interface IDisposable{
		void dispose()
	}
}
