package com.flinty.book.bagheera.model.standard




import java.io.File
import java.util.Map


import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.IScriptDataStorage


/**
 * standard implementation for IScriptDataStorage
 *
 */
class StandardScriptDataStorage implements IScriptDataStorage{

	private final File dataDir

	private final File tempDir

	StandardScriptDataStorage(File dataDirectory){
		dataDir = dataDirectory
		tempDir = new File(dataDir, "temp/")
		if(!dataDir.exists() && !dataDir.mkdirs()){
			throw new RuntimeException('не удалось создать директорию ' + dataDir)
		}
		if(tempDir.exists() && !tempDir.deleteDir()){
			throw new RuntimeException('не удалось удалить директорию ' + tempDir)
		}
		if(!tempDir.exists() && !tempDir.mkdirs()){
			throw new RuntimeException('не удалось создать директорию ' + tempDir)
		}
	}

	@Override
	public Map getMetadata(String scriptFileName) {
		def res = parseAsObject(scriptFileName)
		return res? res: [:]
	}

	@Override
	public void saveMetadata(String scriptFileName, Map metadata) {
		saveAsObject(scriptFileName, metadata)
	}

	@Override
	public Object getData(String scriptFileName) {
		return parseAsObject(scriptFileName)
	}

	@Override
	public void saveData(String scriptFileName, Object data) {
		saveAsObject(scriptFileName, data)
	}




	private Object parseAsObject(String scriptFileName){
		File file = getFile(scriptFileName)
		if(!file.exists()){
			return null
		}
		file.withInputStream {
			return new ObjectInputStream(it).readObject()
		}
	}

	private void saveAsObject(String scriptFileName, Object data){
		File file = getFile(scriptFileName)
		if(data == null){
			if(file.exists() && file.delete()){
				throw new RuntimeException('не удалось удалить файл ' + file)
			}
			return
		}
		file.withOutputStream {
			new ObjectOutputStream(it).writeObject(data)
		}
	}

	File getFile(String scriptFileName){
		return new File(dataDir, Utils.cleanupFileName(scriptFileName)+'.txt')
	}

	File saveTempData(String fileName, byte[] content){
		File result = new File(tempDir, fileName)
		result.setBytes(content)
		return result
	}
}
