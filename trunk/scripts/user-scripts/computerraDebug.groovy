//@ description Computerra (Debug)
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
			it.htmlContent = content
			binding.item = it
			debug()
		}
	}
}
showMessage 'генерация книжки завершена'

