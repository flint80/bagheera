package com.flinty.book.bagheera.model.standard


import java.util.List

import com.flinty.book.bagheera.common.FileInfo
import com.flinty.book.bagheera.model.IUserScriptsInfoProvider



/**
 * standard implementation for IUserScriptsInfoProvider
 *
 */
class StandardFileInfoProvider implements IUserScriptsInfoProvider{

	final File scriptsDir

	def FileFilter filter

	StandardFileInfoProvider(File dir){
		scriptsDir = dir
		filter = new FileFilter(){
					boolean accept(File file) {
						return file.isFile() && file.absolutePath.endsWith('.groovy')
					}
				}
	}
	@Override
	public List<FileInfo> getFiles() {
		List result = new LinkedList<FileInfo>()
		scriptsDir.listFiles(filter).each{File aFile ->
			def FileInfo item = new FileInfo(file: aFile, title: aFile.name)
			result << item
			String content = aFile.getText("utf-8")
			def matcher = content =~ /@ description (.+?)(\n|$)/
			if(matcher){
				item.title = matcher[0][1].trim()
			}
		}
		result.sort( { FileInfo a, FileInfo b -> a.title.compareTo(b.title) } as Comparator )
		return result
	}
}
