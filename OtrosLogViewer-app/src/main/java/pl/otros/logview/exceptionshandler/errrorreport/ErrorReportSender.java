package pl.otros.logview.exceptionshandler.errrorreport;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorReportSender {

     private static final Logger LOGGER = LoggerFactory.getLogger(ErrorReportSender.class.getName()) ;


	private static final String ERROR_SEND_URL = "http://otroslogviewer.appspot.com/services/reportError";
    public static final String NULL = "null";
    private String proxy;
	private int proxyPort = 80;
	private String user;
	private String password;

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String sendReport(Map<String, String> values) throws IOException{
		HttpClient httpClient = new HttpClient();

		HostConfiguration hostConfiguration = new HostConfiguration();
		if (!StringUtils.isBlank(proxy)){
			hostConfiguration.setProxy(proxy, proxyPort);
			if (StringUtils.isNotBlank(user) &&StringUtils.isNotBlank(password)){
				httpClient.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user,password));
			}
		}
		httpClient.setHostConfiguration(hostConfiguration);
		PostMethod method = new PostMethod(getSendUrl());

        addHttpPostParams(values, method);

        int executeMethod = httpClient.executeMethod(method);
        LOGGER.info("HTTP result of report send POST: " + executeMethod);
        return IOUtils.toString(method.getResponseBodyAsStream());
	}

    protected void addHttpPostParams(Map<String, String> values, PostMethod method) {
        for (String key:values.keySet()) {
            String parameterValue = values.get(key);
            parameterValue=parameterValue!=null?parameterValue: NULL;
            method.setParameter(key, parameterValue);
        }
    }

    public String getSendUrl(){
        return ERROR_SEND_URL;
    }

}
