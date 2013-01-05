package com.flinty.book.bagheera.script



import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.flinty.book.bagheera.common.Message
import com.flinty.book.bagheera.model.Environment
import com.flinty.book.bagheera.model.IScript
import com.flinty.book.bagheera.model.IScriptCalback
import com.flinty.book.bagheera.model.Environment.IDisposable
import com.flinty.book.bagheera.model.standard.StandardConfigurationManager
import com.flinty.book.bagheera.model.standard.StandardFileInfoProvider
import com.flinty.book.bagheera.model.standard.StandardNmdWrapper
import com.flinty.book.bagheera.model.standard.StandardScriptDataStorage
import com.flinty.book.bagheera.model.standard.StandardScriptFactory



abstract class ScriptTestBase extends GroovyTestCase{

	protected Log log

	volatile boolean workFinished  = false

	protected void setUp() throws Exception {
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.SimpleLog")
		System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "info")
		System.setProperty("org.apache.commons.logging.simplelog.showlogname", "true")
		System.setProperty("org.apache.commons.logging.simplelog.showShortLogname",
				"true")
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true")
		// per package logging configuration
		System.setProperty("org.apache.commons.logging.simplelog.log.com",
				"debug")
		System.setProperty("org.apache.commons.logging.simplelog.log.scripts",
				"debug")
		log =  LogFactory.getLog(getClass())
		File root = new File('./temp/test/app-home')
		root.deleteDir()
		Environment.configurationManager = new StandardConfigurationManager(new File(root, 'temp/data/'))
		Environment.disposables << Environment.configurationManager
		Environment.userScriptsProvider = new StandardFileInfoProvider(new File('./scripts/user-scripts/'))
		Environment.scriptFactory = new StandardScriptFactory(new File('./scripts/decorators/'))
		Environment.scriptDataStorage = new StandardScriptDataStorage(new File(root, 'temp/scripts/'))
		Environment.nmdWrapper = new StandardNmdWrapper(new File(root, 'temp/nmd/'))
		log.info('initialized')
	}



	protected Binding executeScript(File scriptFile){
		Binding binding = new Binding()
		File resultFile = null
		IScript scr = Environment.scriptFactory.createScript(scriptFile, binding, new IScriptCalback(){
					void updateProgress(int progress, String message){
						log.info(String.format('progress was updated: progress = %s, message = %s', progress, message))
					}

					void showMessage(Message message){
						log.info(String.format('message shown: type = %s, message = %s', message.type, message.message))
					}

					void setOutputFile(File outputFile){
						resultFile = outputFile
						log.info(String.format('output file was set to  %s', outputFile))
					}

					void setWorkFinished(){
						log.info('work was finished')
					}

					Map getParameters(List definitions){
						log.info(String.format('parameters were requested for definitions %s', definitions))
						Map result = [:]
						for(int n = 0; n < definitions.size(); n++){
							Map item = definitions[n]
							Object value = System.getProperty(item['paramName'])
							if('date'.equals(item['type'])){
								value = new SimpleDateFormat('yyyy-MM-dd').parse(value)
							}
							result[item['paramName']] = value? value: item['value']
						}
						return result
					}

					void debug(Binding binding2){
						log.info('debug operation was requested')
					}
				})
		scr.start()
		if(resultFile){
			binding.resultFileContent = resultFile.getText()
		}
		return binding
	}


	protected void tearDown() throws Exception {
		for(IDisposable item : Environment.disposables){
			try{
				item.dispose()
			} catch (Exception e) {
				log.error('unable to dispose item ' + item)
			}
		}
	}

	protected File extractCache(URL zipFile){
		File cacheFolder = new File('./temp/test/app-home/temp/download/cache/')
		if(!cacheFolder.exists()){
			cacheFolder.mkdirs()
		}
		unzip(zipFile, cacheFolder)
		return cacheFolder
	}



	protected void unzip(final URL zipFile, final File folder) throws IOException {
		ZipInputStream zipStrm =
				new ZipInputStream(new FileInputStream(zipFile.getFile()))
		try {
			ZipEntry entry
			while ((entry = zipStrm.getNextEntry()) != null) {
				unpackEntry(zipStrm, entry, folder)
			}
		} finally {
			zipStrm.close()
		}
	}

	private void unpackEntry(final ZipInputStream zipStrm,
	final ZipEntry entry, final File baseFolder) throws IOException {
		String name = entry.getName()
		if (name.endsWith("/")) {
			File folder = new File(baseFolder.getCanonicalPath() + '/' + name)
			if (!folder.exists() && !folder.mkdirs()) {
				throw new IOException("can't create folder " + folder)
			}
			folder.setLastModified(entry.getTime())
			return
		}
		File file = new File(baseFolder.getCanonicalPath() + '/' + name)
		File folder = file.getParentFile()
		if (!folder.exists() && !folder.mkdirs()) {
			throw new IOException("can't create folder " + folder)
		}
		OutputStream strm =
				new BufferedOutputStream(new FileOutputStream(file, false))
		try {
			byte[] buf = new byte[256]
			int len
			while ((len = zipStrm.read(buf)) != -1) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedIOException("thread interrupted") //$NON-NLS-1$
				}
				strm.write(buf, 0, len)
			}
		} finally {
			strm.close()
		}
		file.setLastModified(entry.getTime())
	}

	boolean isEquals(URL expected, String actual){
		String expectedContent = new File(expected.getFile()).getText()
		expectedContent = cleanupForComparison(expectedContent)
		String actualContent = cleanupForComparison(actual)
		boolean result = expectedContent.equals(actualContent)
		if(!result){
			new File('./test/app-home/temp/actual.fb2').setText(actualContent)
			new File('./test/app-home/temp/expected.fb2').setText(expectedContent)
		}
		return result
	}

	private String cleanupForComparison(String data){
		return data.replaceAll('<date value=.*</date>', '').replaceAll('<id>.*</id>', '').replaceAll('href=".*"', '').replaceAll('id=".*"', '').replaceAll("href='.*'", '')
	}
}
