package com.flinty.book.bagheera.model


/**
 * IConfigurationManager is used to store config parameters and presets (for example, windows size)
 *
 */
interface IConfigurationManager {

	String getStringProperty(String key, String defaultValue)

	void setStringProperty(String key,String value)

	int getIntProperty(String key, int defaultValue)

	void setIntProperty(String key,int value)
}
