package com.flinty.book.bagheera.model.standard




import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


/**
 * util class for download data
 *
 */
class StandardDownloader{

	private final Log log = LogFactory.getLog(getClass())

	HttpClient client

	StandardDownloader(){
		client = new HttpClient()
		// establish a connection within 5 seconds
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5000)
	}

	String loadAsString(URL url) throws Exception {
		if (url == null) {
			return null
		}
		HttpMethod method = new GetMethod(url.toString())
		try{
			method.setFollowRedirects(true)
			// execute the method
			client.executeMethod(method)
			return method.getResponseBodyAsString()
		} catch (Throwable e) {
			log.error('unable to load content for ' + url, e)
			return ""
		} finally{
			method.releaseConnection()
		}
	}

	byte[] load(URL url, Map headers = [:]) throws Exception {
		if (url == null) {
			return null
		}
		GetMethod method  = new GetMethod(url.toString())
		try{
			method.setFollowRedirects(true)
			// execute the method
			client.executeMethod(method)
			method.getResponseHeaders().each {
				headers[it.name] = it.value
			}
			return method.getResponseBody()
		} catch (Throwable e) {
			log.error('unable to load content for ' + url, e)
			return new byte[0]
		} finally{
			method.releaseConnection()
		}
	}

	String post(URL url, Map params){
		PostMethod postMethod = new PostMethod(url.toString())
		try{
			params.entrySet().each{
				postMethod.setParameter(it.key, it.value)
			}
			client.executeMethod(postMethod)
			return postMethod.getResponseBodyAsString()
		} finally{
			postMethod.releaseConnection()
		}
	}
}
