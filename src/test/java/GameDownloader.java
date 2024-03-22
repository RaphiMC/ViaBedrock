/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2024 RK_01/RaphiMC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.w3c.dom.Document;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

public class GameDownloader {

    private static final String GAME_PACKAGE_ID = "383fa522-5568-48d1-94fa-dd44b31f02b3"; // 1.19.83.1
    private static final String URL = "https://fe3.delivery.mp.microsoft.com/ClientWebService/client.asmx/secured";
    private static final String DOWNLOAD_URL_XPATH = "//e:Body/cws:GetExtendedUpdateInfo2Response/cws:GetExtendedUpdateInfo2Result/cws:FileLocations/cws:FileLocation[starts-with(cws:Url, 'http://tlu.dl.delivery.mp.microsoft.com')]/cws:Url";

    private int progress;

    public File download() throws Exception {
        this.progress = 0;
        final Instant createdAt = Instant.now();
        final Instant expiresAt = createdAt.plus(5, ChronoUnit.MINUTES);

        final String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\">" +
                "   <s:Header>" +
                "      <a:Action s:mustUnderstand=\"1\">http://www.microsoft.com/SoftwareDistribution/Server/ClientWebService/GetExtendedUpdateInfo2</a:Action>" +
                "      <a:MessageID>urn:uuid:5754a03d-d8d5-489f-b24d-efc31b3fd32d</a:MessageID>" +
                "      <a:To s:mustUnderstand=\"1\">" + URL + "</a:To>" +
                "      <o:Security xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" s:mustUnderstand=\"1\">" +
                "         <Timestamp xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" +
                "            <Created>" + createdAt + "</Created>" +
                "            <Expires>" + expiresAt + "</Expires>" +
                "         </Timestamp>" +
                "         <wuws:WindowsUpdateTicketsToken xmlns:wuws=\"http://schemas.microsoft.com/msus/2014/10/WindowsUpdateAuthorization\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:id=\"ClientMSA\">" +
                "            <TicketType Name=\"AAD\" Version=\"1.0\" Policy=\"MBI_SSL\" />" +
                "         </wuws:WindowsUpdateTicketsToken>" +
                "      </o:Security>" +
                "   </s:Header>" +
                "   <s:Body>" +
                "      <GetExtendedUpdateInfo2 xmlns=\"http://www.microsoft.com/SoftwareDistribution/Server/ClientWebService\">" +
                "         <updateIDs>" +
                "            <UpdateIdentity>" +
                "               <UpdateID>" + GAME_PACKAGE_ID + "</UpdateID>" +
                "               <RevisionNumber>1</RevisionNumber>" +
                "            </UpdateIdentity>" +
                "         </updateIDs>" +
                "         <infoTypes>" +
                "            <XmlUpdateFragmentType>FileUrl</XmlUpdateFragmentType>" +
                "         </infoTypes>" +
                "      </GetExtendedUpdateInfo2>" +
                "   </s:Body>" +
                "</s:Envelope>";

        final HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();

        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        }, new SecureRandom());
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
        connection.setRequestProperty("Content-Length", String.valueOf(payload.length()));
        connection.getOutputStream().write(payload.getBytes());

        if (connection.getResponseCode() / 100 != 2) {
            throw new IOException("Bad response code: " + connection.getResponseCode());
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(connection.getInputStream());

        final XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceResolver());
        final String downloadUrl = (String) xPath.compile(DOWNLOAD_URL_XPATH).evaluate(doc, XPathConstants.STRING);

        final File tempFile = File.createTempFile(GAME_PACKAGE_ID, ".zip");
        tempFile.deleteOnExit();

        final URL url = new URL(downloadUrl);
        final URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        final int contentLength = urlConnection.getContentLength();
        try (final InputStream inputStream = urlConnection.getInputStream();
             final OutputStream outputStream = Files.newOutputStream(tempFile.toPath())) {
            final byte[] buffer = new byte[8192];
            int bytesRead;
            int totalBytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                this.progress = (int) ((totalBytesRead / (double) contentLength) * 100);
            }
        }

        return tempFile;
    }

    public int getProgress() {
        return this.progress;
    }

    private static class NamespaceResolver implements NamespaceContext {

        @Override
        public String getNamespaceURI(final String prefix) {
            if ("e".equals(prefix)) {
                return "http://www.w3.org/2003/05/soap-envelope";
            } else if ("cws".equals(prefix)) {
                return "http://www.microsoft.com/SoftwareDistribution/Server/ClientWebService";
            }
            return null;
        }

        @Override
        public String getPrefix(final String namespaceURI) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<String> getPrefixes(final String namespaceURI) {
            throw new UnsupportedOperationException();
        }

    }

}
