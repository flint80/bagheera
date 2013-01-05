import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import groovy.json.JsonOutput
import groovy.lang.Binding

import com.flinty.book.bagheera.model.IScriptDecorator

/**
 * Decorates Binding adding few logging methods
 *
 */
class LoggerDecorator implements IScriptDecorator{

	private final Log log = LogFactory.getLog('scripts')

	@Override
	public void decorate(Binding binding) {
		binding.logError = {Object message, Throwable t = null ->
			if(t){
				log.error(message.toString(), t)
				return
			}
			log.error(message.toString())
		}
		binding.logWarning = {Object message, Throwable t = null ->
			if(t){
				log.warn(message.toString(), t)
				return
			}
			log.warn(message.toString())
		}
		binding.logDebug = {Object message ->
			log.debug(message.toString())
		}
		binding.log = {Object message ->
			log.debug(message.toString())
		}
		binding.logTrace = {String message, Object obj ->
			String trace = JsonOutput.toJson(obj)
			try{
				trace = JsonOutput.prettyPrint(trace)
			} catch (Exception e) {
			}
			log.trace(message? message + '\n' + trace: 'Trace of ' + obj + '\n' + trace )
		}
	}
}
