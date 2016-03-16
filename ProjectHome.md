Программа Багира предназначена для создания FB2-книг из различных WEB-источников информации, таких как RSS-ленты и форумы.

http://bagheera.googlecode.com/svn/trunk/help/first_steps/figures/bash_processing.PNG

Работает она, примерно, следующим образом. В какой-то момент программа получает содержимое RSS-ленты и формирует из него книжку в формате FB2. При этом программа запоминает момент генерации книжки. При повторном создании книжки программа использует только те новости, которые опубликованы после запомненного момента времени. После повторной генерации временная метка ленты обновляется. Таким образом, в очередной книжке Вы будете читать только новые новости. Аналогично производится работа и с постами форумов.

Программа позволяет создавать книжки как через графическую оболочку, так и через интерфейс командной строки.

Для задания логики обработки интернет-ресурса необходимо создать специальный Groovy-скрипт. Пример скрипта для обработки ленты сайта Компьютерры приведен ниже:

```

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
			it.htmlContent = content
			log 'modified content of ' + it.relatedURL + ' is ' + content
		}
	}
}
showMessage 'генерация книжки завершена'

```

Несмотря на то что скрипты с инструкциями являются полноценной Groovy программой, их не нужно компилировать: достаточно написать скрипт и подложить его в определенную директорию - остальное Багира сделает сама.

В дистрибутиве программы есть готовые скрипты для обработки RSS-ленты Компьютерры, Bash.org и постов форума `ForumHouse`.

Подробная инструкция по работе с программой доступна в формате [pdf](http://code.google.com/p/bagheera/downloads/detail?name=bagheera.pdf) и [WIKI](http://code.google.com/p/bagheera/wiki/Content).