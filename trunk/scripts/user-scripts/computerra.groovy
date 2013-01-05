//@ description Computerra

showMessage 'готовим книжку - ждите...'
rss('http://feeds.feedburner.com/ct_news?format=xml', 'http://www.computerra.ru/new/logo2.gif'){
	useTimeStamp(true,false)
	updateItem{
		String content = loadAsString it.relatedURL
		if(content){
			String subContent = content.findFirst('<div id="content">(.*)<div id="fin">', '<!-- start -->(.*)<!-- fin -->', '<div id="content">(.*)')
			if(subContent){
				content = subContent
			} else{
				log "subcontent is null, content = $content"
				it.htmlContent = ' '
				return
			}
			content = content.deleteAll('<form.+?/form>','<noscript.+?/noscript>', '<iframe.+?/>')
			//log "content is $content"
			String correctedContent = content
			for(String item: content.getAll('img src="(/upload.*?)"')){
				correctedContent = correctedContent.replace(item, "http://www.computerra.ru$item")
				log "reference to image $item is updated"
			}
			it.htmlContent = correctedContent
		}
	}
}
showMessage 'генерация книжки завершена'

