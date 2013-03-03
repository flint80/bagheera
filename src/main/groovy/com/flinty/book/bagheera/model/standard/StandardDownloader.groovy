package com.flinty.book.bagheera.model.standard





import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils


/**
 * util class for download data
 *
 */
class StandardDownloader{

	private final Log log = LogFactory.getLog(getClass())

	HttpClient client

	StandardDownloader(){
		client = new DefaultHttpClient()
		// establish a connection within 5 seconds
		client.getParams().setIntParameter("http.connection.timeout", 5000)
	}

	String loadAsString(URL url) throws Exception {
		if (!url) {
			return null
		}
		HttpGet httpGet = new HttpGet(url.toURI());
		HttpResponse response = client.execute(httpGet);

		try {
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity)
			EntityUtils.consume(entity);
			return result
		} finally {
			httpGet.releaseConnection();
		}
	}

	byte[] load(URL url, Map headers = [:]) throws Exception {
		if (!url) {
			return null
		}
		HttpGet httpGet = new HttpGet(url.toURI());
		HttpResponse response = client.execute(httpGet);

		try {
			for(Header header : response.getAllHeaders()){
				headers[header.name] = header.value
			}
			HttpEntity entity = response.getEntity();
			byte[] result = EntityUtils.toByteArray(entity)
			EntityUtils.consume(entity);
			return result
		} finally {
			httpGet.releaseConnection();
		}
	}

	String post(URL url, Map params){
		if(!url){
			return null
		}
		HttpPost httpPost = new HttpPost(url.toURI());
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		params.entrySet().each{
			nvps.add(new BasicNameValuePair(it.key, it.value));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		HttpResponse response = client.execute(httpPost);
		try {
			HttpEntity entity = response.getEntity();
			byte[] result = EntityUtils.toString()
			EntityUtils.consume(entity);
			return result
		} finally {
			httpPost.releaseConnection();
		}
	}

	void dispose(){
		client.getConnectionManager().shutdown()
	}
}
