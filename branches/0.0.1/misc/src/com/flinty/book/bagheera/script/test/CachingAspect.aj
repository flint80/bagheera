package com.flinty.book.bagheera.script.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import downloader.httpadapter.cache.Cache;


public aspect CachingAspect {

	private final File loadCacheDirectory;

	private final File saveCacheDirectory;

	private final Log log = LogFactory.getLog(getClass());

	private final static String miscDirName = "misc";

	private final Map<String, MiscCachedItem> items = new HashMap<String, MiscCachedItem>();

	public CachingAspect() {
		loadCacheDirectory = prepareDirectory("baghera.cache.loadFrom", false);
		saveCacheDirectory = prepareDirectory("baghera.cache.saveTo", true);
		if (loadCacheDirectory != null) {
			log.debug("trying to initialize cache from " + loadCacheDirectory);
			for (File item : loadCacheDirectory.listFiles()) {
				log.debug("loading cache item from " + item);
				if (!item.isFile()) {
					log.debug("it is not a file");
					continue;
				}
				try {
					FileInputStream fis = new FileInputStream(item);
					try {
						ObjectInputStream ois = new ObjectInputStream(fis);
						MiscCachedItem cachedItem = (MiscCachedItem) ois
								.readObject();
						items.put(cachedItem.getAddress(), cachedItem);
						log.debug(String.format("item %s is put to cache",
								cachedItem));
					} finally {
						fis.close();
					}
				} catch (Exception e) {
					log.error("unable to load cache item", e);
					throw new RuntimeException(e);
				}
			}
		}
		log.info(String
				.format("download trace aspect initialized, loadFrom = %s, saveTo = %s",
						loadCacheDirectory, saveCacheDirectory));
	}

	private File prepareDirectory(String sysPropName, boolean clearFolder) {
		log.debug("preparing directory, sysPropName = " + sysPropName);
		String dirName = System.getProperty(sysPropName, null);
		if (dirName == null) {
			log.debug("system property is absent");
			return null;
		}
		File file = new File(dirName);
		log.debug("file is " + file.getAbsolutePath());
		if (file.exists() && clearFolder) {
			emptyDir(file);
		}
		if (!file.exists() && !file.mkdirs()) {
			throw new RuntimeException("unable to create directory " + file);
		}
		if (clearFolder && !new File(file, "misc/").mkdirs()) {
			throw new RuntimeException("unable to create directory "
					+ new File(file, "misc/"));
		}
		return file;
	}

	private void emptyDir(File file) {
		log.debug("emptying directory " + file);
		for (File item : file.listFiles()) {
			if (item.isFile()) {
				if (!item.delete()) {
					throw new RuntimeException("unable to delete " + file);
				}
				continue;
			}
			emptyDir(item);
		}
		if (!file.delete()) {
			throw new RuntimeException("unable to delete " + file);
		}
	}

	pointcut nmdCacheCreation(Cache cache) : initialization(Cache.new(..)) && this(cache);

	pointcut nmdCachePut(String _address, String _responseAddress,
			String _file, String _charset) : execution(void Cache.put(..)) && args(_address, _responseAddress, _file, _charset) && !within(CachingAspect);

	pointcut nmdCacheGet(String _address) : execution(* Cache.get(..)) && args(_address) && !within(CachingAspect);

	pointcut bagheraLoadAsString(URL url) : execution(* com.flinty.book.bagheera.model.standard.StandardDownloader.loadAsString(..)) && args(url);

	pointcut bagheraLoad(URL url, Map<String, String> headers) : execution(* com.flinty.book.bagheera.model.standard.StandardDownloader.load(..)) && args(url, headers);

	pointcut bagheraPost(URL url, Map<String, String> headers) : execution(* com.flinty.book.bagheera.model.standard.StandardDownloader.post(..)) && args(url, headers);
	
	Object around(String url) : nmdCacheGet(url){
		log.debug("nmdCache getting entry for " + url);
		Object result = proceed(url);
		if (result == null) {
			log.debug("nmdCache entry is absent for " + url);
		}
		return result;
	}

	String around(URL url) : bagheraLoadAsString(url){
		if (url == null) {
			return null;
		}
		log.debug("performing bagheera loadAsString caching for " + url);
		String result = null;
		if (loadCacheDirectory != null) {
			String key = url.toString();
			MiscCachedItem cachedItem = items.get(key);
			if (cachedItem != null) {
				log.debug(String.format("item for %s was found in cache", key));
				try {
					result = new String(getContent(getFile(loadCacheDirectory,
							cachedItem.getFileName())), "utf-8");
				} catch (Exception e) {
					log.error("unable to load item from cache", e);
					throw new RuntimeException(e);
				}
			} else {
				log.error(String.format("item for %s was not found in cache",
						key));
				throw new RuntimeException(String.format(
						"item for %s was not found in cache", key));
			}
		}
		if (result == null) {
			log.debug("delegating to Baghera.LoadAsString" + url);
			result = (String) proceed(url);
			if (result == null) {
				log.warn("no data was loaded from " + url);
				result = "empty";
			}
			if (saveCacheDirectory != null) {
				log.debug("trying to save cached item");
				String fileName = UUID.randomUUID().toString();
				try {
					setContent(getFile(saveCacheDirectory, fileName),
							result.getBytes("utf-8"));
					MiscCachedItem item = new MiscCachedItem(url.toString(),
							null, fileName, "utf-8");
					FileOutputStream fos = new FileOutputStream(new File(
							saveCacheDirectory, fileName));
					try {
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(item);
					} finally {
						fos.close();
					}
					log.debug(String.format("item %s was saved", item));
				} catch (Exception e) {
					log.error("unable to save cached item", e);
					throw new RuntimeException("unable to save cached item ", e);
				}
			}
		}
		return result;
	}

	byte[] around(URL url, Map<String, String> headers) : bagheraLoad(url, headers){
		if (url == null) {
			return null;
		}
		log.debug("performing bagheera load caching for " + url);
		byte[] result = null;
		if (loadCacheDirectory != null) {
			String key = url.toString();
			MiscCachedItem cachedItem = items.get(key);
			if (cachedItem != null) {
				log.debug(String.format("item for %s was found in cache", key));
				try {
					result = getContent(getFile(loadCacheDirectory,
							cachedItem.getFileName()));
					headers.putAll(cachedItem.getHeaders());
					return result;
				} catch (Exception e) {
					log.error("unable to load item from cache", e);
					throw new RuntimeException(e);
				}
			} else {
				log.error(String.format("item for %s was not found in cache",
						key));
				throw new RuntimeException(String.format(
						"item for %s was not found in cache", key));
			}
		}

		if (result == null) {
			log.debug("delegating to Baghera.Load" + url);
			result = (byte[]) proceed(url, headers);
			if (result == null) {
				log.warn("no data was loaded from " + url);
				result = new byte[0];
			}
			if (saveCacheDirectory != null) {
				log.debug("trying to save cached item");
				String fileName = UUID.randomUUID().toString();
				try {
					setContent(getFile(saveCacheDirectory, fileName), result);
					MiscCachedItem item = new MiscCachedItem(url.toString(),
							null, fileName, "utf-8");
					item.getHeaders().putAll(headers);
					FileOutputStream fos = new FileOutputStream(new File(
							saveCacheDirectory, fileName));
					try {
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(item);
					} finally {
						fos.close();
					}
					log.debug(String.format("item %s was saved", item));
				} catch (Exception e) {
					log.error("unable to save cached item", e);
					throw new RuntimeException("unable to save cached item ", e);
				}
			}
		}
		return result;
	}
	
	String around(URL url, Map<String, String> headers) : bagheraPost(url, headers){
		if (url == null) {
			return null;
		}
		log.debug("performing bagheera post caching for " + url);
		String result = null;
		if (loadCacheDirectory != null) {
			String key = url.toString();
			MiscCachedItem cachedItem = items.get(key);
			if (cachedItem != null) {
				log.debug(String.format("item for %s was found in cache", key));
				try {
					byte[] content = getContent(getFile(loadCacheDirectory,
							cachedItem.getFileName()));
					result = content == null || content.length == 0? null: new String(content, "utf-8");
					headers.putAll(cachedItem.getHeaders());
					return result;
				} catch (Exception e) {
					log.error("unable to load item from cache", e);
					throw new RuntimeException(e);
				}
			} else {
				log.error(String.format("item for %s was not found in cache",
						key));
				throw new RuntimeException(String.format(
						"item for %s was not found in cache", key));
			}
		}
		if (result == null) {
			log.debug("delegating to Baghera.post" + url);
			result = (String) proceed(url, headers);
			if (result == null) {
				log.warn("no data was loaded from " + url);
				result = null;
			}
			if (saveCacheDirectory != null) {
				log.debug("trying to save cached item");
				String fileName = UUID.randomUUID().toString();
				try {
					setContent(getFile(saveCacheDirectory, fileName), result == null? new byte[0]: result.getBytes("utf-8"));
					MiscCachedItem item = new MiscCachedItem(url.toString(),
							null, fileName, "utf-8");
					item.getHeaders().putAll(headers);
					FileOutputStream fos = new FileOutputStream(new File(
							saveCacheDirectory, fileName));
					try {
						ObjectOutputStream oos = new ObjectOutputStream(fos);
						oos.writeObject(item);
					} finally {
						fos.close();
					}
					log.debug(String.format("item %s was saved", item));
				} catch (Exception e) {
					log.error("unable to save cached item", e);
					throw new RuntimeException("unable to save cached item ", e);
				}
			}
		}
		return result;
	}

	after(Cache cache) : nmdCacheCreation(cache){
		log.debug("performing cache update");
		if (loadCacheDirectory == null) {
			log.debug("load cache directory is undefined");
			return;
		}
		for (MiscCachedItem cachedItem : items.values()) {
			if (cachedItem.getResponseAddress() == null) {
				continue;
			}
			File file = getFile(loadCacheDirectory, cachedItem.getFileName());
			cache.put(cachedItem.getAddress(), cachedItem.getResponseAddress(),
			file
							.getAbsolutePath(), cachedItem.getCharset());
			log.debug(String.format("address %s is cached: file = %s",
					cachedItem.getAddress(), file));
		}

	}

	after(String _address, String _responseAddress, String _file,
			String _charset) : nmdCachePut(_address, _responseAddress,
					_file, _charset){
		log.debug("adding cache entry");
		if (saveCacheDirectory == null) {
			log.debug("save cache directory is not defined");
			return;
		}
		File source = new File(_file);
		try {
			File target = getFile(saveCacheDirectory, source.getName());
			setContent(target, getContent(source));
			MiscCachedItem item = new MiscCachedItem(_address,
					_responseAddress, source.getName(), _charset);
			File miscItemFile = new File(
					saveCacheDirectory, source.getName());
			FileOutputStream fos = new FileOutputStream(miscItemFile);
			try {
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(item);
			} finally {
				fos.close();
			}
			log.debug(String.format("item %s was saved to file %s", item, miscItemFile));

		} catch (Exception e) {
			log.error("unable to create cache item from " + source.getName(), e);
			throw new RuntimeException("unable to save cached item for "
					+ source.getName(), e);
		}

	}

	private File getFile(File directory, String fileName) {
		return new File(directory,
				String.format("%s/%s", miscDirName, fileName));
	}

	private byte[] getContent(File source) throws Exception {

		FileInputStream fis = new FileInputStream(source);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copyStream(fis, baos);
			return baos.toByteArray();
		} finally {
			fis.close();
		}

	}

	private void copyStream(InputStream fis, OutputStream baos)
			throws Exception {
		byte[] buf = new byte[256];
		int len;
		while ((len = fis.read(buf)) != -1) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedIOException("thread interrupted"); //$NON-NLS-1$
			}
			baos.write(buf, 0, len);
		}
	}

	private void setContent(File target, byte[] content) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(content);
		FileOutputStream baos = new FileOutputStream(target);
		try {
			copyStream(bis, baos);
		} finally {
			baos.flush();
			baos.close();
		}
	}

	static class MiscCachedItem implements Serializable {

		private static final long serialVersionUID = -4366186811109049289L;

		private final String address;

		private final String responseAddress;

		private final String fileName;

		private final String charset;

		private final Map<String, String> headers = new HashMap<String, String>();
		

		MiscCachedItem(String _address, String _responseAddress, String _file,
				String _charset) {
			address = _address;
			responseAddress = _responseAddress;
			fileName = new File(_file).getName();
			charset = _charset;
		}

		public String getAddress() {
			return address;
		}

		public String getCharset() {
			return charset;
		}

		public String getFileName() {
			return fileName;
		}

		public String getResponseAddress() {
			return responseAddress;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}
		
		@Override
		public String toString() {
			return String.format("%s(%s):%s(%s)", address, responseAddress,
					fileName, charset);
		}

	}

}
