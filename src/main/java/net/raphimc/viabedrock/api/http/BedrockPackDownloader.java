/*
 * This file is part of ViaBedrock - https://github.com/RaphiMC/ViaBedrock
 * Copyright (C) 2023-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.viabedrock.api.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BedrockPackDownloader {

    private static final int TIMEOUT = 6000;

    private final URL url;

    public BedrockPackDownloader(final URL url) {
        this.url = url;
    }

    public int getContentLength() {
        try {
            final HttpURLConnection connection = this.createConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            this.checkResponseCode(connection);
            if (connection.getContentLength() < 0) {
                throw new IOException("Content-Length is not set");
            }
            return connection.getContentLength();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] download() {
        try {
            final HttpURLConnection connection = this.createConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            this.checkResponseCode(connection);
            return connection.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpURLConnection createConnection() throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT * 2);
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("User-Agent", "libhttpclient/1.0.0.0");
        connection.setRequestProperty("Cache-Control", "no-cache");
        return connection;
    }

    private void checkResponseCode(final HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() / 100 != 2) {
            throw new IOException("HTTP response code: " + connection.getResponseCode());
        }

    }

}
