package org.jenkinsci.plugins.DependencyCheck.threadfix;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.ClientProtocolException;
import org.jenkinsci.plugins.DependencyCheck.DependencyCheckDescriptor;
import org.jenkinsci.plugins.DependencyCheck.parser.Warning;

/**
 * Main client used to interact with ThreadFix
 * 
 * @author Emilio Escobar <eescobar@gmail.com>
 *
 */
public class ThreadFixClient {
	private DependencyCheckDescriptor descriptor;
	
	public ThreadFixClient(DependencyCheckDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	/**
	 * Does a simple GET to obtain list of teams (if any)
	 * @return true - if response 200 returned by API, false otherwise.
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public void checkConnection() throws ClientProtocolException, IOException, ThreadFixClientException {
		HttpClient httpClient = new HttpClient();
		String url = descriptor.getThreadFixUrl() + "/rest/teams" + "?apiKey=" + descriptor.getThreadFixAPIKey();
		
		GetMethod getRequest = new GetMethod(url);
		getRequest.addRequestHeader("Accept", "application/json");
		
		int statusCode = httpClient.executeMethod(getRequest);
		getRequest.releaseConnection();
		
		if (statusCode != 200) {
			throw new ThreadFixClientException("Error connecting to ThreadFix: Response " + statusCode);
		}
	}
	
	/**
	 * Submits a new entry to ThreadFix for the applicationId provided
	 * @param applicationId - ID of application
	 * @param warning - Entry to submit
	 * @return If successful
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public void submitWarning(String applicationId, Warning warning) throws HttpException, IOException, ThreadFixClientException  {
		HttpClient httpClient = new HttpClient();
		String url = descriptor.getThreadFixUrl() + "/rest/applications/" + applicationId + "/addFinding?apiKey=" +
				descriptor.getThreadFixAPIKey();
		PostMethod postMethod = new PostMethod(url);
		
		NameValuePair isStatic = new NameValuePair("isStatic", "true");
		NameValuePair vulnType = new NameValuePair("vulnType", "OWASP Top Ten 2013 Category A9 - Using Components with Known Vulnerabilities");
		NameValuePair longDescr = new NameValuePair("longDescription", warning.getMessage());
		NameValuePair severity = new NameValuePair("severity", warning.getSeverity());
		NameValuePair filePath = new NameValuePair("filePath", warning.getFileName());
		
		postMethod.setRequestBody(new NameValuePair[] {
				isStatic, vulnType, longDescr, severity, filePath
		});
		
		int statusCode = httpClient.executeMethod(postMethod);
		if (statusCode >= 200 && statusCode <= 300) {
			throw new ThreadFixClientException("Could not submit finding to ThreadFix: " + statusCode);
		}
	}
}
