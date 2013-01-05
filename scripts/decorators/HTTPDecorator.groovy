import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.IScriptDecorator

/**
 * Decorates Binding adding few utility methods retrieving data through HTTP
 *
 */
class HTTPDecorator implements IScriptDecorator{

	private final Log log = LogFactory.getLog('scripts')

	@Override
	public void decorate(Binding binding) {

		binding.loadAsString = { String url ->
			if(!url){
				return null
			}
			return binding.downloader.loadAsString(new URL(url))
		}
		binding.loadAsByteArray = { String url ->
			if(!url){
				return null
			}
			return binding.downloader.load(new URL(url))
		}
		binding.loadAttachment = { String url ->
			if(!url){
				return null
			}
			Map headers = [:]
			byte[] content = binding.downloader.load(new URL(url), headers)
			if(!content){
				return null
			}
			String extension = null
			headers.values().each {
				if(it.contains('/jpeg') || it.contains('/jpg')){
					extension = '.jpeg'
				}
				if(it.contains('/png')){
					extension = '.png'
				}
				if(it.contains('/gif')){
					extension = '.gif'
				}
				if(it.contains('/bmp')){
					extension = '.bmp'
				}
			}
			if(!extension){
				return null
			}
			File file = binding.scriptDataStorage.saveTempData(Utils.cleanupFileName(url)+extension, content)
			return file.getAbsolutePath()
		}
	}
}
