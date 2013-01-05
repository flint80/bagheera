import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.flinty.book.bagheera.common.Message
import com.flinty.book.bagheera.model.IScriptDecorator

/**
 * Decorates Binding adding few utility methods for communication with user
 *
 */
class CommunicationsDecorator implements IScriptDecorator{

	private final Log log = LogFactory.getLog('scripts')

	@Override
	public void decorate(Binding binding) {

		binding.showMessage = {String msg->
			binding.callback.showMessage(new Message(type: Message.MessageType.MESSAGE, message: msg))
		}
		binding.showWarning = {String msg->
			binding.callback.showMessage(new Message(type: Message.MessageType.WARNING, message: msg))
		}
		binding.showError = {String msg->
			binding.callback.showMessage(new Message(type: Message.MessageType.ERROR, message: msg))
		}
		File outputDirectory = new File('./output/')
		if(!outputDirectory.exists() && !outputDirectory.mkdirs()){
			throw new Exception('не удалось создать папку ' + outputDirectory)
		}
		binding.save = {String content, String fileName->
			if(!content || !fileName){
				return
			}
			File file = new File(outputDirectory, fileName)
			file.setText(content, 'utf-8')
			binding.callback.setOutputFile(file)
		}
		binding.getParameters = {List defs ->
			binding.callback.getParameters(defs)
		}
		binding.updateProgress = {int progress, String message->
			binding.callback.updateProgress(progress, message)
		}
		binding.debug = {
			->
			binding.callback.debug(binding)
		}
	}
}
