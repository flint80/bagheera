

package com.flinty.book.bagheera.script.input.rss

import org.cyberneko.html.parsers.SAXParser

import com.flinty.book.bagheera.common.Utils






class XPathTest extends GroovyTestCase{

	void testXPath(){
		String.metaClass.xPath = {Closure path ->
		try{
			SAXParser parser = new SAXParser()
			parser.setProperty('http://cyberneko.org/html/properties/names/elems', 'match')
			//parser.setProperty('http://cyberneko.org/html/properties/names/attrs ', 'no-change')
			def res = new XmlParser(parser).parseText(delegate)
			List result = []
			for(Node item : path.call(res)){
				result << Utils.getNodeContent(item)
			}
			return result
		} catch (Exception e) {
			return []
		}
		}
		
		String.metaClass.xFind = {Closure equator ->
		try{
			SAXParser parser = new SAXParser()
			parser.setProperty('http://cyberneko.org/html/properties/names/elems', 'match')
			//parser.setProperty('http://cyberneko.org/html/properties/names/attrs ', 'no-change')
			def res = new XmlParser(parser).parseText(delegate)
			List result = []
			for(Node item: findNodes(res, equator)){
				result << Utils.getNodeContent(item)
			}
			return result
		} catch (Exception e) {
			return []
		}
		}
		String.metaClass.xPath = {String path ->
			if(Utils.isBlank(delegate) || Utils.isBlank(path)){
				return ''
			}
			try{
				SAXParser parser = new SAXParser()
				parser.setProperty('http://cyberneko.org/html/properties/names/elems', 'match')
				//parser.setProperty('http://cyberneko.org/html/properties/names/attrs ', 'no-change')
				Node node = new XmlParser(parser).parseText(delegate)
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
				return null
			}
			}
	String content = new File(getClass().getResource("testdata/page.html").getFile()).getText()
	println content.xPath("/HTML/BODY/DIV[2]/DIV[2]/DIV/DIV/FORM/OL/LI/DIV[2]")
		
		
//		Node.metaClass.toXML{->
//			return Utils.getNodeContent(delegate)
//		}
//		Node.metaClass.xGet{Closure equator->
//			return findNodes(delegate, equator)
//		}
//		String content = new File(getClass().getResource("testdata/page.html").getFile()).getText()
//		DOMParser domParser = new DOMParser()
//		domParser.setProperty('http://cyberneko.org/html/properties/names/elems', 'true')
//		domParser.parse(new InputSource(new StringReader(content)))
//		def doc     = domParser.getDocument()
//		//def expr    = XPathFactory.newInstance().newXPath().compile("/body/div[2]/div[2]/div/div/form/ol/li/div[2]")
//		def expr    = XPathFactory.newInstance().newXPath().compile("/HTML/BODY/DIV[2]/DIV[2]/DIV/DIV/FORM/OL/LI/DIV[2]")
//		def nodes = expr.evaluate(doc, XPathConstants.NODE)
//		println Utils.getNodeContent(nodes)
//		// Set up the output transformer
//		TransformerFactory transfac = TransformerFactory.newInstance()
//		Transformer trans = transfac.newTransformer()
//		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
//		trans.setOutputProperty(OutputKeys.INDENT, "yes")
//	  
//		// Print the DOM node
//	  
//		StringWriter sw = new StringWriter()
//		StreamResult result = new StreamResult(sw)
//		DOMSource source = new DOMSource(nodes)
//		trans.transform(source, result)
//		String xmlString = sw.toString()
//		println xmlString	
		//println Utils.getNodeContent(nodes)
		
		
		
		
		
//		def bodyNode = res.BODY[0]
//		bodyNode.replaceNode{a('href':'http://test.jpg')}
	//	println(Utils.getNodeContent(res))
				
		//def res = new XmlParser(new SAXParser()).parseText(content)
				
//		List<String> posts  = content.xFind{it.name() == 'li' && it.attribute('id')?.contains('post-')}
//		for(String post : posts){
//			for(String item: post.xFind{it.name()=='blockquote'}){
//					println "article $item"
//				}
//			}

			//		for(Object item: res.BODY.DIV.DIV.DIV.DIV.FORM.OL.LI){
			//			println item
			//		}
			//		for(Object item: res.getByName('BODY/DIV/DIV/DIV/DIV/FORM/OL/LI/DIV/DIV/ARTICLE/BLOCKQUOTE')){
			//			println item
			//		}

			//println res
			//def items = res.depthFirst().BLOCKQUOTE.findAll{it.getA == 'messageText ugc baseHtml'}
			//		NodeList items = findNodes(res){it.name().toUpperCase()=='BLOCKQUOTE' && it.attribute('class') == 'messageText ugc baseHtml'}
			//		int n = 0
			//		items.each {Object item->
			//			n++
			//			Node node = (Node) item
			//			println "itemx ${n}, name ${node.name()}, path = ${printPath(node)}"
			//			//println node.name()
			//			printNode(node)
			//		}
		}

		private List<Node> findNodes(Node node, Closure equator){
			List<Node> res = []
			appendNodes(node, res, equator)
			return res
		}

		private void appendNodes(Node node, List<Node> res, Closure equator){
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

		private String printPath(Node node){
			String result = node.name()
			if(node.parent() != null && node.parent() != node){
				result = printPath(node.parent())+'/'+result
			}
			return result
		}

		private void printNode(Node node){
			println(Utils.getNodeContent(node))
		}
	}
