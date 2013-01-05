


import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.cyberneko.html.parsers.SAXParser

import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.IScriptDecorator

/**
 * Decorates Binding adding few utility methods for working with strings
 *
 */
class StringUtilsDecorator implements IScriptDecorator{

	@Override
	public void decorate(Binding binding) {
		String.metaClass.findFirst  = {String... patterns ->
			if(Utils.isBlank(delegate)){
				return null
			}
			for(String pattern : patterns){
				Pattern p = Pattern.compile(pattern,Pattern.DOTALL)
				Matcher matcher = p.matcher(delegate)
				if(matcher.find()){
					return matcher.group(1)
				}
			}
			return null
		}
		String.metaClass.getAll  = {String... patterns ->
			if(Utils.isBlank(delegate)){
				return []
			}
			List result = []
			for(String pattern : patterns){
				Pattern p = Pattern.compile(pattern,Pattern.DOTALL)
				Matcher matcher = p.matcher(delegate)
				while(matcher.find()){
					result<<matcher.group(1)
				}
			}
			return result
		}
		String.metaClass.getGroups  = {String pattern ->
			if(Utils.isBlank(delegate)){
				return []
			}
			List<String> result = []
			Pattern p = Pattern.compile(pattern,Pattern.DOTALL)
			Matcher matcher = p.matcher(delegate)
			if(matcher.find()){
				for(int n = 0; n < matcher.groupCount(); n++){
					result << matcher.group(n)
				}
			}
			return result
		}

		String.metaClass.trimAll  = {
			->
			if(Utils.isBlank(delegate)){
				return []
			}
			return delegate.replaceAll('\\s','')
		}
		String.metaClass.deleteAll  = {String... patterns ->
			if(Utils.isBlank(delegate)){
				return null
			}
			String result = delegate
			patterns.each {
				Pattern p = Pattern.compile(it,Pattern.DOTALL)
				result = p.matcher(result).replaceAll("")
			}
			return result
		}
		String.metaClass.parseDate = {String pattern ->
			if(Utils.isBlank(delegate)){
				return null
			}
			try{
				return new SimpleDateFormat(pattern).parse(delegate)
			} catch (Exception e) {
				binding.logError.call('unable to parse date', e)
				return null
			}
		}
		String.metaClass.xPath = {Closure path ->
			if(Utils.isBlank(delegate)){
				return []
			}
		try{
			def res = parse(delegate)
			List result = []
			for(Node item : path.call(res)){
				result << Utils.getNodeContent(item)
			}
			return result
		} catch (Exception e) {
			binding.logError.call('unable to parse content', e)
			return []
		}
		}
		String.metaClass.xPath = {String path ->
		if(Utils.isBlank(delegate) || Utils.isBlank(path)){
			return ''
		}
		try{
			Node node = parse(delegate)
				int tagIndex = 0
				for(String item: path.split("/")){
					if(Utils.isBlank(item)){
						continue
					}
					tagIndex++
					if(tagIndex == 1){//HTML element
						continue
					}
					String tagName = item
					int elementIndex = 0
					int brakePosition = item.indexOf('[')
					if(brakePosition != -1){
						tagName = item.substring(0, brakePosition)
						elementIndex = Integer.parseInt(item.substring(brakePosition+1, item.length() -1)) -1 //ONE-base enumeration to zero-based
					}
					int currentElementIndex = 0
					boolean found = false
					for(Object child: node.children()){
						if (child instanceof Node) {
							Node childNode = (Node) child
							if(tagName.equalsIgnoreCase(childNode.name())){
								if(currentElementIndex == elementIndex){
									node = childNode
									found = true
									break
								}
								currentElementIndex++
							}
						}
					}
					if(!found){
						return null
					}
				}
				return node == null? null: Utils.getNodeContent(node)
		} catch (Exception e) {
			binding.logError.call('unable to parse content', e)
			return null
		}
		}
		
		String.metaClass.xFind = {Closure equator ->
			if(Utils.isBlank(delegate)){
				return []
			}
		try{
			List result = []
			for(Node item: findNodes(parse(delegate), equator)){
				result << Utils.getNodeContent(item)
			}
			return result
		} catch (Exception e) {
			binding.logError.call('unable to parse content', e)
			return []
		}
		}
		String.metaClass.xGetNodes = {Closure equator ->
			if(Utils.isBlank(delegate)){
				return []
			}
			try{
			def res = parse(delegate)
			List result = []
					for(Node item: findNodes(res, equator)){
						result << item
					}
			return result
		} catch (Exception e) {
			binding.logError.call('unable to parse content', e)
			return []
		}
		}
		String.metaClass.xParse = {->
		if(Utils.isBlank(delegate)){
			return null
		}
		try{
			return parse(delegate)
		} catch (Exception e) {
			binding.logError.call('unable to parse content', e)
			return null
		}
		}
		
		Node.metaClass.toXML{->
			return Utils.getNodeContent(delegate)
		}
		Node.metaClass.xGet{Closure equator->
			return findNodes(delegate, equator)
		}
		Node.metaClass.xFind{Closure equator->
		def res = findNodes(delegate, equator)
		return res.isEmpty()? null: res.get(0)
		}
		
	}
	private static Node parse(String content){
		SAXParser parser = new SAXParser()
		parser.setProperty('http://cyberneko.org/html/properties/names/elems', 'match')
		parser.setProperty('http://cyberneko.org/html/properties/names/attrs', 'no-change')
		parser.setFeature('http://cyberneko.org/html/features/balance-tags/document-fragment', true)
		return new XmlParser(parser).parseText(content)
	}
	
	private static List<Node> findNodes(Node node, Closure equator){
		List<Node> res = []
		appendNodes(node, res, equator)
		return res
	}
	
	private static void appendNodes(Node node, List<Node> res, Closure equator){
		if(equator.call(node)){
			res << node
			return
		}
		for(Object item : node.children()){
			if(item instanceof Node){
				Node cnode = (Node) item
				appendNodes(cnode, res, equator)
			}

		}
	}
}
