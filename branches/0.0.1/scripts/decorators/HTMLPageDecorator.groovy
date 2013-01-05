
import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.INmdWrapper
import com.flinty.book.bagheera.model.IScriptDecorator
import com.flinty.book.bagheera.model.standard.FB2Converter
import com.flinty.book.bagheera.script.common.htmlbook.HTMLBook
import com.flinty.book.bagheera.script.common.htmlbook.HTMLBookMetadata
import com.flinty.book.bagheera.script.common.htmlbook.HTMLSection

/**
 * Decorates Binding adding HTML context
 *
 */
class HTMLPageDecorator implements IScriptDecorator{

	Closure updateContent = { updateContentInt = it}
	
	Closure updateContentInt = {HTMLSection section ->
			//do nothing
    }


	@Override
	public void decorate(Binding binding) {
		binding.html = { Closure cl = null ->
			HTMLPageDecorator worker = new HTMLPageDecorator()
			if(cl){
				cl.delegate = worker
				cl.call()
			}
			worker.generate(binding)
		}
	}



	private void generate(Binding binding){
		binding.updateProgress.call(0,'Запрашиваем данные')
		def params = binding.getScriptMetadata.call()
		params = binding.getParameters.call([
			[type: 'text', paramName: 'url', title: 'Адрес', value: params['url']],
			[type: 'content', paramName: 'content', title: 'Содержимое HTML']
		])
		String content = params['content']
		//checking url
		if(!content){
			binding.logError.call('content is not assigned')
			binding.callback.showError 'не задано содержимое страницы'
			return
		}
		String url = params['url']
				//checking url
				if(!content){
					binding.logError.call('url is not assigned')
					binding.callback.showError 'не задан адрес страницы'
					return
				}
		binding.saveScriptMetadata.call(params)
		//EXTRACTING TITLE
		String contentTitle = content.findFirst('<title>(.*)</title>')
		//UPDATING IMAGES URLS
		String correctedUrl = url.endsWith('/')? url.substring(0, url.length()-1): url
		correctedUrl = correctedUrl.startsWith('http://')? correctedUrl: ('http://'+correctedUrl)
		try{
		for(Node node: content.xGetNodes{it.name() == 'img' && it.attribute('src')!= null&& !it.attribute('src').contains('http:')}){
			String imageURL = node.attribute('src')
			String fileName = binding.loadAttachment.call("$correctedUrl/$imageURL".toString())
			if(!fileName){
				URL urlObj = new URL(content)
				String host = urlObj.getHost().endsWith('/')? urlObj.getHost().substring(0, content.length()-1): urlObj.getHost()
				fileName = binding.loadAttachment.call("http://$host/$imageURL".toString())
			}
			if(fileName){
				node.replaceNode{a{img('src': fileName)}}
			} 
		}} catch (Exception e){
		binding.logError.call('unable to update image references', e)
	}
		
		//CLEANUP CONTENT
	    content = content.deleteAll('<noscript.+?/noscript>', '<iframe.+?/>')
		
		String title = url
		
		HTMLBook  book = new HTMLBook()
		book.metadata = new HTMLBookMetadata()
	    book.metadata.bookTitle = contentTitle? contentTitle: title
	    book.metadata.coverPageURL = ' '
		book.metadata.genre = 'science'
		
		book.metadata.publishDate = new Date()
		book.metadata.lang = 'ru'
		book.metadata.author.lastName = url
		
		
		HTMLSection section = new HTMLSection()
		section.htmlContent = content
		section.sectionDate = new Date()
		section.relatedURL = url
		section.sectionTitle = title
		
		//CUSTOM UPDATE CONTENT
		updateContentInt(section)
		
		book.sections << section
		
		//converting to fb2 book
		int endProgressValue = 50
		binding.updateProgress.call(endProgressValue,'Генерируем книжку')
		content = FB2Converter.convert(book, new INmdWrapper.ProgressCallback(){
					void onProgressChanged(int percent){
						binding.updateProgress.call((int) (endProgressValue + (100d-endProgressValue)*percent/100),'Генерируем книжку')
					}
				})
		if(!content){
			binding.logError.call('unable to generate content')
			binding.showError.call('не удалось создать FB2 книгу')
			return
		}
		//saving content
		binding.save.call(content, Utils.cleanupFileName(title)+'.fb2')
		binding.updateProgress.call(100, 'Создание книжки завершено')
	}
}
