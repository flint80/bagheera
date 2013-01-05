package com.flinty.book.bagheera.model.standard

import groovy.xml.MarkupBuilder

import java.text.DateFormat
import java.text.SimpleDateFormat

import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.Environment
import com.flinty.book.bagheera.model.INmdWrapper
import com.flinty.book.bagheera.script.common.htmlbook.HTMLBook
import com.flinty.book.bagheera.script.common.htmlbook.HTMLSection

import constructor.objects.channel.core.stream.ChannelDataHeaderHelperBean
import constructor.objects.channel.core.stream.ChannelDataHelperBean
import constructor.objects.channel.core.stream.ChannelDataList
import constructor.objects.interpreter.core.data.stream.InterpreterDataHelperBean
import dated.item.atdc.stream.AtdcItemHelperBean



/**
 * Helper class for wrapping NMDParser
 *
 */
class FB2Converter{

	private final static DateFormat df = new SimpleDateFormat('yyyy-MM-dd')


	static String convert(HTMLBook book, INmdWrapper.ProgressCallback callback){
		if(!book){
			return null
		}
		ChannelDataHeaderHelperBean header = new ChannelDataHeaderHelperBean()
		header.coverUrl = book.metadata.coverPageURL? book.metadata.coverPageURL:' '
		header.firstName = book.metadata.author.firstName?book.metadata.author.firstName:' '
		header.lastName = book.metadata.author.lastName? book.metadata.author.lastName: ' '
		header.title = book.metadata.author.lastName? book.metadata.author.lastName: ' '
		header.sourceUrl = ' '
		header.lang = book.metadata.lang?book.metadata.lang:'ru'
		header.setGenres([
			book.metadata.genre?book.metadata.genre:'science'
		])
		List beans = []
		long timestamp = 0
		book.sections.each {
			timestamp = timestamp + addItems(it, beans, timestamp)
		}
		if(beans.isEmpty()){
			return null
		}
		InterpreterDataHelperBean interpreter = new InterpreterDataHelperBean()
		interpreter.setId('simpler_interpreter_adapter')
		interpreter.setList(beans)

		ChannelDataHelperBean channelData = new ChannelDataHelperBean()
		channelData.setHeader(header)
		channelData.setData([interpreter])

		ChannelDataList dataList = new ChannelDataList()
		dataList.setList([channelData])
		String fb2Content = Environment.nmdWrapper.convert(dataList, book.metadata.coverPageURL?book.metadata.coverPageURL:' ', callback)
		if(!fb2Content){
			return null
		}
		Node root = new XmlParser().parseText(fb2Content)
		def ns = new groovy.xml.Namespace('http://www.w3.org/1999/xlink')
		StringWriter sectionWriter = new StringWriter()


		StringWriter resultWriter = new StringWriter()
		resultWriter.write('<?xml version="1.0" encoding="UTF-8"?>\n')
		MarkupBuilder builder = new MarkupBuilder(resultWriter)
		builder.FictionBook('xmlns':'http://www.gribuser.ru/xml/fictionbook/2.0', 'xmlns:xlink':'http://www.w3.org/1999/xlink', 'xmlns:l':'http://www.w3.org/1999/xlink'){
			description(){
				'title-info'(){
					genre('science')
					author{
						'first-name'(book.metadata.author.firstName)
						'last-name'(book.metadata.author.lastName)
					}
					'book-title'(book.metadata.bookTitle)
					mkp.yieldUnescaped '\n'+printDateTag(book.metadata.publishDate)
					coverpage{
						image('l:href':root.description[0].'title-info'[0].coverpage[0].image[0].attribute(ns.href))
					}
					lang(book.metadata.lang)
				}
				'document-info'{
					author{
						'first-name'(book.metadata.author.firstName)
						'last-name'(book.metadata.author.lastName)
					}
					'program-used'('nmd 1.71.7')
					mkp.yieldUnescaped '\n'+printDateTag(book.metadata.publishDate)
					id('nmd ' + System.currentTimeMillis())
					version('1.0')
				}
			}
			def bodyElms = root.body.findAll{it.'@name' != 'notes'}
			if(bodyElms.size() == 1){
				def sectionElms = bodyElms[0].section
				StringBuilder sb = new StringBuilder()
				int sectionIdx = 0
				book.sections.each{sec->
					sectionIdx = sectionIdx + addSection(sec, sectionIdx, sectionElms, sb)
				}
				builder.body(){
					builder.mkp.yieldUnescaped '\n'+sb.toString()
				}
			}

			root.body.findAll{it.'@name' == 'notes'}.each {
				builder.mkp.yieldUnescaped '\n'+Utils.getNodeContent(it)
			}
			root.binary.each {
				builder.mkp.yieldUnescaped Utils.getNodeContent(it)
			}
		}
		return resultWriter.toString()
	}

	private static long addItems(HTMLSection section,List items, long timestamp){
		long delta = 48 * 3600 * 1000L
		long result = delta
		if(!Utils.isBlank(section.htmlContent)){
			AtdcItemHelperBean item = new AtdcItemHelperBean()
			item.setAuthorAvatar('')
			item.setAuthorInfo(section.sectionAuthor?.lastName?section.sectionAuthor.lastName:'')
			item.setDate(new Date(result+ timestamp))
			item.setAuthorNick('')
			item.setBase(section.relatedURL?section.relatedURL:' ')
			item.setContent(section.htmlContent?section.htmlContent:' ')
			item.setTitle(section.sectionTitle?section.sectionTitle:' ')
			item.setUrl(section.relatedURL?section.relatedURL:' ')
			items << item
			result = result+delta
		}
		section.nestedSections.each {
			result = result + addItems(it, items, result+ timestamp)
		}
		return result
	}


	private static int addSection(HTMLSection section, int index, NodeList sectionElms, StringBuilder sb){
		int result = 0
		String sectionContent = null
		if(Utils.isBlank(section.htmlContent) || sectionElms.size() <= index){
			sectionContent = String.format('''
<section xmlns="http://www.gribuser.ru/xml/fictionbook/2.0" id="%s">
<title>
    <p>
      <strong>
        %s
      </strong>
    </p>
</title>
</section>
''', UUID.randomUUID().toString(), section.sectionTitle)
		} else{
			result = 1
			Node sectionNode = sectionElms[index].section[0]
			if(!sectionNode){//only one section in book
				sectionNode = sectionElms[index]
			}
			sectionContent = Utils.getNodeContent(sectionNode)
			if(sectionContent){
				int idx1 = sectionContent.indexOf('<title>')
				int idx2 = sectionContent.indexOf('<empty-line/>')
				if(idx1 != -1 && idx2 != -1){
					StringBuilder sb2 = new StringBuilder(
							String.format('''
<title>
    <p>
      <strong>
        %s
      </strong>
    </p>
</title>
''', section.sectionTitle))
					sectionContent = sectionContent.substring(0, idx1) + sb2.toString() +  sectionContent.substring(idx2)
				}
			}
		}
		if(section.nestedSections.size() == 0){
			sb.append('\n'+sectionContent)
			return result
		}
		StringBuilder sb2 = new StringBuilder()
		int sectionIdx = 0
		section.nestedSections.each{
			result = result + addSection(it, index + result, sectionElms, sb2)
		}
		int idx = sectionContent.lastIndexOf('<')
		sb.append(sectionContent.substring(0, idx -1)+ sb2.toString()+sectionContent.substring(idx))
		return result
	}


	private static String printDateTag(Date date){
		if(!date){
			return '<date/>'
		}
		return String.format('<date value="%s">%1$s</date>', df.format(date))
	}


	private static  String getFileName(File file){
		return file.getCanonicalPath().replace('\\', '/')+'/'
	}
}
