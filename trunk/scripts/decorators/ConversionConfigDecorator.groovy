import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import groovy.json.JsonOutput
import groovy.lang.Binding

import com.flinty.book.bagheera.model.IScriptDecorator

/**
 * Decorates Binding adding possibility to configure Conversion
 *
 */
class ConversionConfigDecorator implements IScriptDecorator{

	private final Log log = LogFactory.getLog('scripts')

	Closure images  ={ Closure cl ->
		cl.delegate = this;
		cl.call()
	}

	Closure grayscale = {boolean value = true ->
		System.setProperty('image.grayscale', Boolean.toString(value))
	}

	@Override
	public void decorate(Binding binding) {
		binding.conversion = {Closure cl ->
			cl.delegate = this
			cl.call()
		}
	}
}
