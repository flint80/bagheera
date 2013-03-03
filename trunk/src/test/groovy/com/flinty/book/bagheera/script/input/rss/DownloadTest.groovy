

package com.flinty.book.bagheera.script.input.rss

import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils






class DownloadTest extends GroovyTestCase{

	void testRss(){
		HttpClient client = new DefaultHttpClient()()
		// establish a connection within 5 seconds
		client.getParams().setIntParameter("http.connection.timeout", 5000)
		HttpPost httpPost = new HttpPost("http://www.forumhouse.ru/login/login");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("login", "test"));
		nvps.add(new BasicNameValuePair("password", "test"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		HttpResponse response = client.execute(httpPost);

		try {
			for(Header header : response.getAllHeaders()){
				println "${header.getName()}: ${header.getValue()}"
			}
			HttpEntity entity = response.getEntity();
			byte content = EntityUtils.toByteArray(entity)
			println "content length is ${content.length}"
			new File('test/image.png').setBytes(content)
			EntityUtils.consume(entity);
		} finally {
			httpPost.releaseConnection();
		}
	}
}
