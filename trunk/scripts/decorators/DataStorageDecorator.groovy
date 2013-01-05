import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.flinty.book.bagheera.model.IScriptDecorator

/**
 * Decorates Binding adding few utility methods for retrieving and storing script data
 *
 */
class DataStorageDecorator implements IScriptDecorator{

	private final Log log = LogFactory.getLog('scripts')

	@Override
	public void decorate(Binding binding) {

		binding.saveScriptMetadata = {Map metadata ->
			binding.scriptDataStorage.saveMetadata(binding.scriptFile.getName(), metadata)
		}
		binding.getScriptMetadata = {
			return binding.scriptDataStorage.getMetadata(binding.scriptFile.getName())
		}
		binding.saveScriptData = {String dataId, Object data->
			binding.scriptDataStorage.saveData(binding.scriptFile.getName()+ '.'+dataId, data)
		}
		binding.getScriptData = {String dataId->
			return binding.scriptDataStorage.getData(binding.scriptFile.getName()+ '.'+dataId)
		}
	}
}
