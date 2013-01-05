package com.flinty.book.bagheera.main

import groovy.io.GroovyPrintStream

import java.awt.EventQueue
import java.text.DateFormat
import java.text.SimpleDateFormat

import javax.swing.UIManager

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.log4j.PropertyConfigurator

import com.flinty.book.bagheera.common.Message
import com.flinty.book.bagheera.gui.MainFrame
import com.flinty.book.bagheera.gui.ScriptsPanelProvider
import com.flinty.book.bagheera.gui.SplashWindow
import com.flinty.book.bagheera.model.Environment
import com.flinty.book.bagheera.model.IScript
import com.flinty.book.bagheera.model.IScriptCalback
import com.flinty.book.bagheera.model.Environment.IDisposable
import com.flinty.book.bagheera.model.standard.StandardConfigurationManager
import com.flinty.book.bagheera.model.standard.StandardFileInfoProvider
import com.flinty.book.bagheera.model.standard.StandardNmdWrapper
import com.flinty.book.bagheera.model.standard.StandardScriptDataStorage
import com.flinty.book.bagheera.model.standard.StandardScriptFactory
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel



/**
 * Main class for the application
 * Depending on the input arguments it launches an application either in GUI on CLI mode 
 *
 */
class Bagheera {
	private static Log log = LogFactory.getLog(Bagheera.class)

	public static void main(String[] args) {
		PropertyConfigurator.configure('config/log4j.properties')
		System.addShutdownHook {
			for(IDisposable item : Environment.disposables){
				try{
					item.dispose()
				} catch (Exception e) {
					log.error('unable to dispose item ' + item)
				}
			}
			log.info('shutted down')
		}
		if(args.length == 0){
			launchGUI()
		} else {
			launchCLI(args)
		}
	}
	private static void launchCLI(String[] args){
		try {
			System.setOut(new GroovyPrintStream(System.out, true, System.getProperty('console.encoding','Cp866')) )
		}
		catch(UnsupportedEncodingException e) {
			System.out.println('Unable to setup console codepage: ' + e)
		}
		try {
			System.setErr(new GroovyPrintStream(System.err, true, System.getProperty('console.encoding','Cp866')) )
		}
		catch(UnsupportedEncodingException e) {
			System.out.println('Unable to setup console codepage: ' + e)
		}
		log.debug('starting in CLI mode')
		String scriptName = args[0]
		if(!scriptName){
			System.err.println('не задано название скрипта')
			System.exit(1)
		}
		if(!scriptName.endsWith('.groovy')){
			scriptName = scriptName + '.groovy'
		}
		File file = new File('./scripts/user-scripts/', scriptName)
		if(!file.exists()){
			System.err.println("файл $file не существует")
			System.exit(1)
		}
		log.debug("using script file $file")
		Map params = [:]
		for(int n = 1; n<args.length; n++){
			def parts = args[n] =~ '([a-z,A-Z]+)=(.+)'
			if(parts){
				if(parts[0][1]){
					params[parts[0][1]] = parts[0][2]
				}
			}
		}
		log.debug("params was parsed $params")
		Environment.configurationManager = new StandardConfigurationManager(new File('./temp/data/'))
		Environment.disposables << Environment.configurationManager
		Environment.scriptFactory = new StandardScriptFactory(new File('./scripts/decorators/'))
		Environment.scriptDataStorage = new StandardScriptDataStorage(new File('./temp/scripts/'))
		Environment.nmdWrapper = new StandardNmdWrapper(new File('./temp/fb2/'))
		DateFormat dtf = new SimpleDateFormat('yyyy-MM-dd-HH-mm')
		DateFormat df = new SimpleDateFormat('yyyy-MM-dd')
		def binding = new Binding()
		IScript scr = Environment.scriptFactory.createScript(file, binding, new IScriptCalback(){
					void updateProgress(int progress, String message){
						log.info("progress was updated: progress = ${progress}, message = ${progress}")
					}

					void showMessage(Message message){
						log.info("message shown: type = ${message.type}, message = ${message.message}")
						System.out.println("${message.type}: ${message.message}")
					}

					void setOutputFile(File outputFile){
						log.info("output file was set to  ${outputFile}")
						System.out.println("Сообщение: книжка сохранена в ${outputFile.getAbsolutePath()}")
					}

					void setWorkFinished(){
						log.info('work was finished')
					}

					Map getParameters(List definitions){
						Map result = [:]
						for(int n = 0; n < definitions.size(); n++){
							Map item = definitions[n]
							log.debug("request for ${item['paramName']}: type is ${item['type']}, defaultValue is ${item['value']}")
							String customValue = params[item['paramName']]
							if(customValue){
								String type = item['type']
								def value = null
								if('int'.equals(type)){
									value = Integer.parseInt(customValue)
								} else if('text'.equals(type)){
									value = customValue
								} else if('date'.equals(type)){
									value = df.parse(customValue)
								} else if('datetime'.equals(type)){
									value = dtf.parse(customValue)
								} else{
									throw new Exception("неподдерживаемый тип данных '${type}'")
								}
								result[item['paramName']] = value
								log.debug("${item['paramName']} was updated with custom value $value")
							} else{
								result[item['paramName']] = item['value']
							}
							return result
						}
					}
					void debug(Binding binding2){
						log.info('debug operation was requested')
					}
				})
		try{
			scr.start()
		} catch (Exception e) {
			log.error('unable to execute script', e)
			System.err.println('При выполнении скрипта возникла ошибка ' + e)
			System.exit(1)
		}
	}
	private static void launchGUI(){
		SplashWindow.splash(Bagheera.class.getClassLoader().getResource('images/splash.gif'))
		UIManager.setLookAndFeel(new NimbusLookAndFeel())
		Environment.configurationManager = new StandardConfigurationManager(new File('./temp/data/'))
		Environment.disposables << Environment.configurationManager
		Environment.userScriptsProvider = new StandardFileInfoProvider(new File('./scripts/user-scripts/'))
		Environment.scriptFactory = new StandardScriptFactory(new File('./scripts/decorators/'))
		Environment.scriptDataStorage = new StandardScriptDataStorage(new File('./temp/scripts/'))
		Environment.nmdWrapper = new StandardNmdWrapper(new File('./temp/fb2/'))
		MainFrame frame = new MainFrame()
		Environment.contentManager = frame.createContentManager()

		Environment.contentManager.updateProvider(ScriptsPanelProvider.getInstance())
		EventQueue.invokeLater(new Runnable(){
					void run(){
						SplashWindow.disposeSplash()
						frame.frame.setVisible(true)
					}
				})
	}
}