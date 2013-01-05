
import java.text.SimpleDateFormat

import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.INmdWrapper
import com.flinty.book.bagheera.model.IScriptDecorator
import com.flinty.book.bagheera.model.standard.FB2Converter
import com.flinty.book.bagheera.script.common.htmlbook.HTMLBook
import com.flinty.book.bagheera.script.common.htmlbook.HTMLBookAuthor
import com.flinty.book.bagheera.script.common.htmlbook.HTMLBookMetadata
import com.flinty.book.bagheera.script.common.htmlbook.HTMLSection

/**
 * Decorates Binding adding Forum generator context
 *
 */
class ForumDecorator implements IScriptDecorator{

	private String url

	private String coverPageUrl

	private String threadTitle

	private boolean useTimeStampValue = false

	private boolean askForTimeStamp = false

	private boolean confirmTimeStamp = false

	private Date startDateValue = null

	private Authorization authorization

	Closure useTimeStamp = {boolean askForTimeStampValue = false,boolean confirmTimeStampValue = false->
		useTimeStampValue = true
		askForTimeStamp = askForTimeStampValue
		confirmTimeStamp = confirmTimeStampValue
	}

	Closure authorize = { Closure cl ->
		authorization = new Authorization()
		cl.delegate = authorization
		cl.call()
	}

	Closure extractMessages = { extractMessagesInt = it}

	Closure extractMessagesInt = {String content ->
		//do nothing
	}
	Closure buildFileName = { buildFileNameInt = it}

	Closure buildFileNameInt = {Binding binding ->
		SimpleDateFormat dfInt = new SimpleDateFormat(
				"yyyy-MM-dd", Locale.ENGLISH)
		return Utils.cleanupFileName(threadTitle)+ '_'+dfInt.format(startDateValue) + '_'+dfInt.format(new Date())+ '.fb2'
	}

	Closure extractTitle = {  extractTitleInt = it}

	Closure extractTitleInt = {String content -> return 'Forum House' }

	Closure extractNextPageUrl = { extractNextPageUrlInt = it}

	Closure extractNextPageUrlInt = {String content, int pageNumber -> return null }

	Closure postProcessMessage = { postProcessMessageInt = it}

	Closure postProcessMessageInt = { ForumMessage message -> 
		//noops
	}

	@Override
	public void decorate(Binding binding) {
		binding.forum = {String coverPageUrl = null, Closure cl ->
			ForumDecorator worker = new ForumDecorator()
			worker.coverPageUrl = coverPageUrl
			if(cl){
				cl.delegate = worker
				cl.call()
			}
			worker.generate(binding)
		}
	}



	private void generate(Binding binding){
		binding.updateProgress.call(0,'Собираем метаданные')
		//determining start date and url
		startDateValue = new Date(0)
		Map map = binding.getScriptMetadata.call()
		if(!map){
			map = [:]
		}
		def definitions = []
		if(useTimeStampValue){
			if(map['timestamp'] != null){
				startDateValue = Utils.clearDate(map['timestamp'])
			}
			definitions << [type: 'datetime', paramName: 'timestamp', title: 'Начало периода', value: startDateValue]
		}
		if(map['url'] != null){
			url = map['url']
		}
		definitions << [type: 'text', paramName: 'url', title: 'URL форума', value: url]
		def params = binding.getParameters.call(definitions)
		if(useTimeStampValue){
			startDateValue = params['timestamp']
			if(!startDateValue){
				binding.logError.call('start date is not defined')
				binding.showError.call('не задано начала периода')
				return
			}
		}
		url = params['url']
		if(Utils.isBlank(url)){
			binding.logError.call('url is blank')
			binding.showError.call('не URL форума')
			return
		}
		map['url'] = url
		binding.saveScriptMetadata.call(map)
		//checking url
		if(!url){
			binding.logError.call('url is not assigned')
			binding.showError.call('не задан URL форума')
			return
		}
		binding.showMessage.call('анализируем содержимое страницы 1')
		String content = binding.loadAsString.call url
		if (Utils.isBlank(content)) {
			binding.logError.call('unable to get content of the first page')
			binding.showError.call('не удалось загрузить содержимое первой страницы форума')
			return null
		}
		if(authorization){
			authorization.authorize(binding)
		}
		try{
			HTMLBook  book = new HTMLBook()
			book.metadata = new HTMLBookMetadata()
			book.metadata.coverPageURL = coverPageUrl
			threadTitle = extractTitleInt(content)
			book.metadata.bookTitle = threadTitle
			book.metadata.genre = 'other'
			book.metadata.publishDate = new Date()
			book.metadata.lang = 'ru'
			List messages = []
			messages.addAll(extractMessagesInt(content))
			binding.updateProgress.call(10,'Получаем содержимое форума')
			int n = 1
			url = extractNextPageUrlInt(content, n)
			while (n < 1000 && (url != null)) {
				n++
				binding.showMessage.call('анализируем содержимое страницы ' + n)
				content = binding.loadAsString.call url
				url = extractNextPageUrlInt(content, n)
				if (Utils.isBlank(content)) {
					binding.logError.call('unable to get content of the' +n+ ' page of the forum')
					binding.showError.call('не удалось загрузить содержимое страницы ' + n + ' форума')
					continue
				}
				messages.addAll(extractMessagesInt(content))
			}
			List hMessages = messages
			messages = []
			hMessages.each {
				if(!it.date || !it.date.before(startDateValue)){
					messages << it
				}
			}
			messages.sort {it.date? it.date: new Date()}
			n = 0
			messages.each {
			    postProcessMessageInt(it)
				n++
				binding.updateProgress.call(20 + ((int) (50-20)*n/messages.size()),'Постобработка сообщений')
				}
			Date lastDate = null
			HTMLSection currentDateSection = null
			int count = 0
			SimpleDateFormat sdf = new SimpleDateFormat('dd MMM')
			messages.each {
				if(count > 10000){
					return
				}
				count++
				Date sectDate = Utils.clearDate(it.date)
				if(lastDate == null || !lastDate.equals(sectDate)){
					lastDate = sectDate
					currentDateSection = new HTMLSection()
					currentDateSection.sectionDate = sectDate
					currentDateSection.sectionTitle = sectDate? sdf.format(sectDate):''
					book.sections << currentDateSection
				}
				HTMLSection sect = new HTMLSection()
				sect.sectionAuthor = new HTMLBookAuthor(lastName: it.author)
				sect.sectionDate = it.date
				sect.sectionTitle = it.author + ': '+count
				sect.htmlContent = it.content
				currentDateSection.nestedSections << sect
			}
			if(book.sections.isEmpty()){
				binding.updateProgress.call(100,'Генерация книжки закончена')
				binding.logWarning.call('content is empty')
				binding.showError.call('в книгу не попало ни одного сообщения, проверьте временную метку')
				return
			}
			binding.updateProgress.call(50,'Генерируем книжку')
			//converting to fb2 book
			content = FB2Converter.convert(book, new INmdWrapper.ProgressCallback(){
						void onProgressChanged(int percent){
							binding.updateProgress.call((int) (50 + (100d-50)*percent/100),'Генерируем книжку')
						}
					})
			if(!content){
				binding.logError.call('unable to generate content')
				binding.showError.call('не удалось создать FB2 книгу')
				return
			}
		} finally{
			if(authorization){
				authorization.logout(binding)
			}
		}
		//saving content
		binding.save.call(content, buildFileNameInt.call(binding))
		if(useTimeStampValue && confirmTimeStamp){
			binding.updateProgress.call(99, 'Сохраняем метаданные')
			params = binding.getParameters.call([
				[type: 'datetime', paramName: 'timestamp', title: 'Дата последнего сообщения', value: new Date()]
			])
			map = binding.getScriptMetadata.call()
			if(!map){
				map = [:]
			}
			map['timestamp'] = params['timestamp']
			binding.saveScriptMetadata.call(map)
		} else if(useTimeStampValue){
			map = binding.getScriptMetadata.call()
			map['timestamp'] = new Date()
			binding.saveScriptMetadata.call(map)
		}
		binding.updateProgress.call(100, 'Генерация закончена')

	}


}
