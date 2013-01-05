package com.flinty.book.bagheera.model.standard





import com.flinty.book.bagheera.model.IConfigurationManager
import com.flinty.book.bagheera.model.Environment.IDisposable



/**
 * standard implementation for IConfigurationManager
 *
 */
class StandardConfigurationManager implements IConfigurationManager,IDisposable{

	private final File configFolder

	private final Properties props = new Properties()

	StandardConfigurationManager(File folder){
		configFolder = folder
		if(!configFolder.exists() && !configFolder.mkdirs()){
			throw new RuntimeException('не удалось создать директорию ' + configFolder)
		}
		File propsFile = new File(configFolder, 'config.data')
		if(propsFile.exists()){
			propsFile.withReader('utf-8') { props.load(it) }
		}
	}
	@Override
	public void dispose() {
		File propsFile = new File(configFolder, 'config.data')
		propsFile.withWriter('utf-8') { props.store(it, "don't modify") }
	}

	@Override
	public String getStringProperty(String key, String defaultValue) {
		String value = props.getProperty(key)
		return value? value: defaultValue
	}
	@Override
	public void setStringProperty(String key, String value) {
		props.put(key, value)
	}
	@Override
	public int getIntProperty(String key, int defaultValue) {
		String value = props.getProperty(key)
		if(!value){
			return defaultValue
		}
		try{
			return Integer.parseInt(value.trim())
		}catch (Exception e) {
			return defaultValue
		}
	}
	@Override
	public void setIntProperty(String key, int value) {
		props.put(key, Integer.toString(value))
	}
}
