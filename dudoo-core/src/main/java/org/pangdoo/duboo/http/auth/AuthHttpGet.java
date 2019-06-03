package org.pangdoo.duboo.http.auth;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.pangdoo.duboo.exception.NullException;
import org.pangdoo.duboo.http.HttpRequest;
import org.pangdoo.duboo.url.Url;
import org.pangdoo.duboo.url.WebUrl;

public class AuthHttpGet extends HttpRequest {
	
	public AuthHttpGet(String host, int port, Credentials credentials) {
		this(host, port, credentials, null);
	}

	public AuthHttpGet(String host, int port, Credentials credentials, WebUrl webUrl) {
		super(webUrl);
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(host, port), credentials);
		setCredsProvider(credsProvider);
	}

	@Override
	public HttpUriRequest request() {
		if (this.webUrl == null) {
			throw new NullException("URL is null.");
		}
		Url url = this.webUrl.getUrl();
		if (url == null) {
			throw new NullException("URL is null.");
		}
		RequestBuilder builder = RequestBuilder.get(url.toString());
		Map<String, String> header = getHeaders();
		if (header != null && !header.isEmpty()) {
			Iterator<String> headerIterator = header.keySet()
					.iterator();
			while (headerIterator.hasNext()) {
				String name = headerIterator.next();
				builder.addHeader(name, header.get(name));
			}
		}
		builder.setCharset(Charset.forName(getCharset()));
		return builder.build();
	}

}