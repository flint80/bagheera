//@ description Computerra

showMessage 'готовим книжку - ждите...'
rss('http://feeds.feedburner.com/ct_news?format=xml', 'http://img15.nnm.ru/9/7/3/b/8/4bf03e580673df17eefa3211426.jpg'){
	usePeriod(false,7)
	updateItem{
		showMessage "обрабатываем пост ${it.sectionTitle}(url: ${it.relatedURL})"
		log "$it.sectionTitle : related url is $it.relatedURL"
		String content = loadAsString it.relatedURL
		if(content){
			String subContent = content.findFirst('<div id="content">(.*)<div id="fin">', '<!-- start -->(.*)<!-- fin -->', '<div id="content">(.*)')
			if(subContent){
				content = subContent
			} else{
				log "subcontent is null, content = $content"
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
			log "corrected content is $correctedContent"
		} else{
			log "content is empty"
			//log "raw content is " + new java.net.URL("$it.relatedURL").getText()
		}
	}
}
showMessage 'генерация книжки завершена'