package com.mcmcg.ico.bluefin.service.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;

public class HttpsUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsUtil.class);

    public static String sendPostRequest(String requestUrl, String payload) {
        StringBuffer jsonString = new StringBuffer();
        try {
            URL url = new URL(requestUrl);
            HttpsURLConnection cntn = (HttpsURLConnection) url.openConnection();
            SSLSocketFactory sslSocketFactory = createTrustAllSslSocketFactory("TLSv1.2");
            cntn.setSSLSocketFactory(sslSocketFactory);
            cntn.setDoInput(true);
            cntn.setDoOutput(true);
            cntn.setRequestMethod("POST");

            cntn.setRequestProperty("Accept", "application/json");
            cntn.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter writer = new OutputStreamWriter(cntn.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(cntn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            cntn.disconnect();
        } catch (Exception e) {
            LOGGER.error("Unable to get service response.", e);
            throw new CustomException("Unable to get service response.");
        }
        return jsonString.toString();
    }

    private static SSLSocketFactory createTrustAllSslSocketFactory(final String pProtocol) throws Exception {
        TrustManager[] byPassTrustManagers = new TrustManager[] { new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }, };
        SSLContext sslContext = SSLContext.getInstance(pProtocol);
        sslContext.init(null, byPassTrustManagers, new SecureRandom());
        return sslContext.getSocketFactory();
    }
}