package com.flinty.book.bagheera.model.standard

import com.flinty.book.bagheera.common.Message
import com.flinty.book.bagheera.common.Message.MessageType
import com.flinty.book.bagheera.model.Environment
import com.flinty.book.bagheera.model.IScript
import com.flinty.book.bagheera.model.IScriptCalback
import com.flinty.book.bagheera.model.IScriptDecorator
import com.flinty.book.bagheera.model.IScriptFactory


/**
 * standard implementation for IScriptFactory
 *
 */
class StandardScriptFactory implements IScriptFactory{

	private final List<IScriptDecorator> decorators = []

	GroovyClassLoader loader = new GroovyClassLoader()

	StandardScriptFactory(File templatesFolder){
		if(!templatesFolder.exists()){
			return
		}
		List files = []
		templatesFolder.listFiles().each{
			if(it.isFile()){
				files<<it
			}
		}
		files.sort{
			String content = it.getText('utf-8')
			if(content.contains('//priority')){
				int n = content.indexOf('//priority')+'//priority'.length()+1
				return Integer.parseInt(content.substring(n, n+1))
			}
			return 100
		}
		files.each{
			Object obj = loader.parseClass(it.getText('utf-8')).newInstance()
			if(obj instanceof IScriptDecorator){
				decorators << obj
			}
		}
	}

	@Override
	public IScript createScript(File scriptFile, Binding binding, IScriptCalback callback) {
		binding.beingInterrupted = false
		binding.isInterrupting = {
			synchronized (binding) {
				return binding.beingInterrupted
			}
		}
		binding.callback = callback
		binding.downloader = new StandardDownloader()
		binding.scriptDataStorage = Environment.scriptDataStorage
		binding.scriptFile = scriptFile

		decorators.each{ it.decorate(binding) }
		return new IScript(){

			void start(){
				try{
					def GroovyShell shell = new GroovyShell(loader, binding)
					shell.evaluate(scriptFile.getText('utf-8'))
				} catch (Throwable t){
					t.printStackTrace()
					def message
					synchronized (binding) {
						message = new Message(type: MessageType.ERROR, message: binding.beingInterrupted? 'Выполнение прервано': "При выполнении произошла ошибка(${t.getMessage()})".toString())
					}
					callback.showMessage(message)
					callback.updateProgress(100, 'Операция прервана')
				} finally{
					callback.setWorkFinished()
				}
			}

			void interrupt(){
				synchronized (binding) {
					binding.beingInterrupted = true
				}
			}
		}
	}
}
