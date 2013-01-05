package com.flinty.book.bagheera.script.input.forum


import junit.framework.Assert

import com.flinty.book.bagheera.script.ScriptTestBase



class ForumTest extends ScriptTestBase{

	@Override
	protected void setUp() throws Exception {
		super.setUp()
		//System.setProperty('baghera.cache.saveTo', new File('./test/app-home/temp/download/save').getAbsolutePath())
		File cacheFolder = extractCache(getClass().getResource('testdata/fh.zip'))
		System.setProperty('baghera.cache.loadFrom', cacheFolder.getAbsolutePath())
		System.setProperty('url', 'http://www.forumhouse.ru/threads/137592/')
	}

	void testRss(){
		Binding result = executeScript(new File('./scripts/user-scripts/fh.groovy'))
		Assert.assertTrue('content is wrong', isEquals(getClass().getResource('testdata/fh.fb2'), result.resultFileContent))
	}
}
