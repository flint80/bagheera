

package com.flinty.book.bagheera.script.input.rss

import junit.framework.Assert

import com.flinty.book.bagheera.script.ScriptTestBase



class RssTest extends ScriptTestBase{

	@Override
	protected void setUp() throws Exception {
		super.setUp()
		//System.setProperty('baghera.cache.saveTo', new File('./temp/test/app-home/temp/download/save').getAbsolutePath())
		File cacheFolder = extractCache(getClass().getResource('testdata/computerra-rss.zip'))
		System.setProperty('baghera.cache.loadFrom', cacheFolder.getAbsolutePath())
		System.setProperty('timestamp', '2011-01-01')
	}

	void testRss(){
		Binding result = executeScript(new File('./scripts/user-scripts/computerra.groovy'))
		Assert.assertTrue('content is wrong', isEquals(getClass().getResource('testdata/computerra-rss.fb2'), result.resultFileContent))
		//log.debug("content of generated book is \r\n ${result.resultFileContent}")
	}
}
