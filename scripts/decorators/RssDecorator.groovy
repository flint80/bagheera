
import java.text.SimpleDateFormat

import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.INmdWrapper
import com.flinty.book.bagheera.model.IScriptDecorator
import com.flinty.book.bagheera.model.standard.FB2Converter
import com.flinty.book.bagheera.script.common.htmlbook.HTMLBook
import com.flinty.book.bagheera.script.common.htmlbook.HTMLBookMetadata
import com.flinty.book.bagheera.script.common.htmlbook.HTMLSection

/**
 * Decorates Binding adding RSS generator context
 *
 */
class RssDecorator implements IScriptDecorator{

	private final  SimpleDateFormat df = new SimpleDateFormat(
	"EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH)


	private String url

	private String coverPageUrl

	private boolean ignoreTimeStampValue

	private boolean askForPeriod

	private int retrievePeriod = 14

	private int historyLength = 30

	private boolean askForTimeStamp = false

	private boolean confirmTimeStamp = false

	private Date startDateValue = null

	Closure useTimeStamp = {boolean askForTimeStampValue = false,boolean confirmTimeStampValue = false->
		ignoreTimeStampValue = false
		askForTimeStamp = askForTimeStampValue
		confirmTimeStamp = confirmTimeStampValue
	}
	Closure usePeriod = { boolean askForPeriodValue = false, int defaultRetrievePeriod = 14->
		ignoreTimeStampValue = true
		askForPeriod = askForPeriodValue
		retrievePeriod = defaultRetrievePeriod
	}

	Closure updateItem = { updateItemInt = it}

	Closure updateItemInt = {HTMLSection section ->
		//do nothing
	}
	Closure buildFileName = { buildFileNameInt = it}

	Closure buildFileNameInt = {Binding binding ->
		SimpleDateFormat dfInt = new SimpleDateFormat(
				"yyyy-MM-dd", Locale.ENGLISH)
		String fileName = binding.scriptFile.getName()
		int idx = fileName.indexOf('.')
		fileName = fileName.substring(0, idx)
		fileName = fileName + '_'+dfInt.format(startDateValue) + '_'+dfInt.format(new Date())+ '.fb2'
		return fileName
	}


	@Override
	public void decorate(Binding binding) {
		binding.rss = {String url, String coverPageUrl = null, Closure cl = null->
			RssDecorator worker = new RssDecorator()
			worker.url = url
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
		//determining start date
		startDateValue = Utils.clearDate(new Date(new Date().getTime() - 24L * retrievePeriod * 3600 * 1000))
		if(ignoreTimeStampValue){
			if(askForPeriod){
				int defaultValue = retrievePeriod
				Map map = binding.getScriptMetadata.call()
				if(!map){
					map = [:]
				}
				if(map['days'] != null){
					defaultValue = map['days']
				}
				def params = binding.getParameters.call([
					[type: 'int', paramName: 'days', title: 'Кол-во дней', value: defaultValue]
				])
				retrievePeriod = params['days']
				map['days'] = retrievePeriod
				binding.saveScriptMetadata.call(map)
			}
			startDateValue = Utils.clearDate(new Date(new Date().getTime() - 24L * retrievePeriod * 3600 * 1000))
		} else{
			Map map = binding.getScriptMetadata.call()
			if(!map){
				map = [:]
			}
			if(map['timestamp'] != null){
				startDateValue = Utils.clearDate(map['timestamp'])
			}
			if(askForTimeStamp){
				def params = binding.getParameters.call([
					[type: 'date', paramName: 'timestamp', title: 'Начало периода', value: startDateValue]
				])
				startDateValue = params['timestamp']
				if(!startDateValue){
					binding.logError.call('start date is not defined')
					binding.callback.showError 'не задано начала периода'
					return
				}
				startDateValue = Utils.clearDate(startDateValue)
				map['timestamp'] = startDateValue
				binding.saveScriptMetadata.call(map)
			}
		}
		binding.log.call('startDate is ' + startDateValue)
		//checking url
		if(!url){
			binding.logError.call('url is not assigned')
			binding.callback.showError 'не задан URL ленты'
			return
		}
		binding.updateProgress.call(10,'Загружаем ленту')
		//analizing content
		String rssContent = null
		byte[] byteContent = binding.loadAsByteArray.call url
		if(byteContent == null){
			binding.callback.showError 'не удалось загрузить содержимое ленты'
			return
		}
		rssContent = new String(byteContent, 'utf-8')
		int hIdx = rssContent.indexOf('encoding="')
		if(hIdx != -1){
			String encoding = rssContent.substring(hIdx+'encoding="'.length(), rssContent.indexOf('"?'))
			if(!'utf-8'.equals(encoding.toLowerCase())){
				rssContent = new String(byteContent, encoding)
			}
		}

		def node = new XmlParser().parseText(rssContent)
		HTMLBook  book = new HTMLBook()
		book.metadata = new HTMLBookMetadata()
		book.metadata.coverPageURL = coverPageUrl
		book.metadata.bookTitle = node.channel.title.text()
		book.metadata.genre = 'science'
		book.metadata.publishDate = new Date()
		book.metadata.lang = node.channel.language.text()
		book.metadata.author.lastName = node.channel.link.text()

		int startProgressValue = 20
		binding.updateProgress.call(startProgressValue,'Обновляем историю')
		//updating only those items that was not previously loaded
		HTMLBook historyBook = binding.getScriptData.call 'history'
		if(!historyBook){
			historyBook = new HTMLBook()
			historyBook.metadata = book.metadata
		}
		node.channel[0].item.each{sec->
			HTMLSection section = new HTMLSection()
			section.htmlContent = sec.description.text()
			section.sectionDate = parseDate(sec.pubDate.text())
			section.relatedURL = sec.link.text()
			section.sectionTitle = sec.title.text()
			boolean found = false
			book.sections << section
		}
		//updating items
		int endProgressValue = 50
		int currentItem = 0
		int totalItemsCount = book.sections.size()
		book.sections.each{
			updateItemInt.call it
			currentItem++
			binding.updateProgress.call((int) Math.round(startProgressValue + ((double) currentItem)*(endProgressValue - startProgressValue)/totalItemsCount),'Обновляем историю')
		}
		book.sections.each{ it.htmlContent  = Utils.cleanupUrl(it.htmlContent)}
		book.sections.each{HTMLSection sec ->
			HTMLSection sec3 = historyBook.sections.find{HTMLSection sec2 ->
				sec.sectionTitle == sec2.sectionTitle}
			if(sec3){
				sec3.htmlContent = sec.htmlContent
				return
			}
			historyBook.sections << sec
		}
		//removing expired items from history and saving history
		Date expireDate = new Date(new Date().getTime() - historyLength * 24L * 3600 *1000)
		if(expireDate.before(startDateValue)){
			historyBook.sections.removeAll{it.sectionDate.before(expireDate)}
		}
		historyBook.sections.sort {it.sectionDate}
		binding.saveScriptData.call('history',historyBook)
		//preparing result html book
		HTMLBook resultBook = new HTMLBook()
		resultBook.metadata = historyBook.metadata
		Date lastDate = null
		HTMLSection currentDateSection = null
		int count = 0
		historyBook.sections.each {
			if((it.sectionDate != null && it.sectionDate.before(startDateValue)) || count > 10000){
				return
			}
			Date sectDate = Utils.clearDate(it.sectionDate)
			if(lastDate == null || !lastDate.equals(sectDate)){
				lastDate = sectDate
				currentDateSection = new HTMLSection()
				currentDateSection.sectionDate = sectDate
				currentDateSection.sectionTitle = sectDate.toString()
				resultBook.sections << currentDateSection
			}
			currentDateSection.nestedSections << it
			count++
		}
		if(resultBook.sections.isEmpty()){
			binding.updateProgress.call(100,'Генерация книжки закончена')
			binding.logWarning.call('content is empty')
			binding.showError.call('в книгу не попало ни одной статьи, проверьте временную метку')
			return
		}
		binding.updateProgress.call(endProgressValue,'Генерируем книжку')
		//converting to fb2 book
		String content = FB2Converter.convert(resultBook, new INmdWrapper.ProgressCallback(){
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
		binding.save.call(content, buildFileNameInt.call(binding))
		if(!ignoreTimeStampValue && confirmTimeStamp){
			binding.updateProgress.call(99, 'Сохраняем метаданные')
			def params = binding.getParameters.call([
				[type: 'date', paramName: 'timestamp', title: 'Конец периода', value: new Date()]
			])
			Map map = binding.getScriptMetadata.call()
			if(!map){
				map = [:]
			}
			map['timestamp'] = params['timestamp']
			binding.saveScriptMetadata.call(map)
		}
		binding.updateProgress.call(100, 'Создание книжки завершено')
	}



	private Date parseDate(String dateStr){
		if(Utils.isBlank(dateStr)){
			return null
		}
		try{
			return df.parse(dateStr.substring(0, dateStr.lastIndexOf(' ')).trim())
		} catch (Exception e) {
			return null
		}
	}


}
