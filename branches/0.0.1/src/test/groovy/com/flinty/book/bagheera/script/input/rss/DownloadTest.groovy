

package com.flinty.book.bagheera.script.input.rss

import org.apache.commons.httpclient.Header
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod





class DownloadTest extends GroovyTestCase{

	void testRss(){
		HttpClient client = new HttpClient()
		// establish a connection within 5 seconds
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5000)
		PostMethod postMethod = new PostMethod("http://www.forumhouse.ru/login/login")
		postMethod.setParameter("login", 'nataliana')
		postMethod.setParameter("password", 'chuchito')
		client.executeMethod(postMethod)
		GetMethod method = new GetMethod('http://www.forumhouse.ru/attachments/665634/')
		method.setFollowRedirects(true)
		// execute the method
		client.executeMethod(method)
		byte[] content = method.getResponseBody()
		for(Header header : method.getResponseHeaders()){
			println "${header.getName()}: ${header.getValue()}"
		}
		println "content length is ${content.length}"
		new File('test/image.png').setBytes(content)
	}
}
