package com.flinty.book.bagheera.model.standard

import groovy.util.XmlNodePrinter

import java.beans.XMLEncoder
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashMap

import app.VersionInfo
import app.api.NmdApi
import app.controller.NullController
import app.iui.flow.custom.SingleProcessInfo
import app.workingarea.Defaults
import app.workingarea.SettingsManager
import app.workingarea.WorkspaceManager
import app.workingarea.defaults.FileDefaults
import app.workingarea.settings.FileSettingsManager
import app.workingarea.workspace.WorkspaceDirectoriesManager

import com.flinty.book.bagheera.model.INmdWrapper
import com.flinty.book.bagheera.model.INmdWrapper.ProgressCallback
import com.flinty.book.bagheera.script.common.htmlbook.HTMLSection

import constructor.objects.channel.core.stream.ChannelDataList

/**
 * standard implementation for INmdWrapper
 *
 */
class StandardNmdWrapper implements INmdWrapper{

	private File root

	StandardNmdWrapper(File tempDirValue){
		root= tempDirValue
	}

	String convert(ChannelDataList dataList, String coverUrl,final INmdWrapper.ProgressCallback callback){
		if (root.exists() && !root.deleteDir()) {
			throw new Exception('не удалось удалить директорию ' + root)
		}
		if(!root.mkdirs()){
			throw new Exception('не удалось создать директорию ' + root)
		}
		new File(root, 'work/debug/').mkdirs()
		new File(root, 'work/cache/').mkdirs()
		File resourceDir = new File(root, 'work/resources/')
		resourceDir.mkdirs()
		File dummyFile = new File(resourceDir, 'notSupFmt.jpg')
		dummyFile.setBytes(getClass().getClassLoader().getResource('images/notSupFmt.jpg').getBytes())

		File etcDir = new File(root, 'workarea/etc/')
		etcDir.mkdirs()
		File settingsDir = new File(root, 'workarea/settings/')
		settingsDir.mkdirs()
		new File(root, 'workarea/root/').mkdirs()
		new File(root, 'workarea/workspaces/sample/channels/').mkdirs()
		File locatorDir = new File(root, 'workarea/workspaces/sample/locator/')
		locatorDir.mkdirs()
		new File(root, 'workarea/workspaces/sample/modifications/').mkdirs()
		new File(root, 'workarea/workspaces/sample/cloud/').mkdirs()////
		new File(etcDir, 'default.properties').setText(String.format('''
default.storage.root=%s
settings.directory.name=%s
workspaces.directory.name=%s
image.grayscale=%s
default.workspace.name=sample
workspaces.google.reader.directory.name=%s
			''', getFileName(new File(root, 'workarea/root/')),
				getFileName(new File(root, 'workarea/settings/')),
				getFileName(new File(root, 'workarea/workspaces/')),
				System.getProperty('image.grayscale', 'true'),
				getFileName(new File(root, 'workarea/greader/')))
				)
		new File(settingsDir, 'default.properties').setText(String.format('''
resource.cache.root=%s
api.temp.directory=%s
api.resource.dummy=%s
image.grayscale=%s
api.debug.directory=%s
''',getFileName(new File(root, 'work/resources/')), getFileName(new File(root, 'work/cache/')),
				getFileName(dummyFile), System.getProperty('image.grayscale', 'true'), getFileName(new File(root, 'work/debug/'))))
		new File(locatorDir, "ct.xml").setText(String
				.format('<simpler feedUrl="rss" coverUrl="%s" storeDays="100" branch="comp" outName="ct" fromNewToOld="no"></simpler>',coverUrl))
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		XMLEncoder encoder = new XMLEncoder(baos)
		encoder.writeObject(dataList)
		encoder.close()
		new File(root, 'workarea/workspaces/sample/channels/ct.channel.xml').setBytes(baos.toByteArray())
		Defaults defaults = new FileDefaults(
				new File(root, 'workarea/etc/default.properties').getAbsolutePath())
		WorkspaceManager workspaceManager = new WorkspaceDirectoriesManager(
				defaults.getWorkspacesDirectory())
		SettingsManager settingsManager = new FileSettingsManager(
				defaults.getSettingsDirectory(),
				defaults.getDefaultStorageRoot(),
				defaults.getDefaultStoragePeriod())
		VersionInfo versionInfo = new VersionInfo('nmd', '1.7', '1.7')
		NmdApi nmdApi = new NmdApi(defaults, settingsManager, workspaceManager,
				versionInfo)
		try {
			nmdApi.loadSettings(defaults.getDefaultSettingsName())
			nmdApi.loadWorkspace(defaults.getDefaultWorkspaceName())
			nmdApi.updateOutput("ct", 0, new NullController(){
						String lastCode = null
						int startPercent = 0
						public void onProgress(SingleProcessInfo info){
							if(info.isIndeterminate()){
								return
							}
							if(lastCode == null){
								lastCode  = info.getCode()
							} else if(!lastCode.equals(info.getCode())){
								startPercent = 50
							}

							callback.onProgressChanged(startPercent + (int) (50d*info.getCurrent()/info.getTotal()))
						}
					},
					new HashMap<String, String>())
		} catch (Exception e) {
			e.printStackTrace()
		} finally {
			nmdApi.cleanup()
		}
		return new File(root, 'workarea/root/comp/ct.fb2').getText()
	}



	private List getBeans(HTMLSection section){
		List beans = []
	}

	private String printNode(Node node){
		def writer = new StringWriter()
		def nodeWriterRoot = new XmlNodePrinter(new PrintWriter(writer))
		nodeWriterRoot.setNamespaceAware(false)
		nodeWriterRoot.print(node)
		return writer.toString()
	}

	private String getFileName(File file){
		return file.getCanonicalPath().replace('\\', '/')+'/'
	}
}
