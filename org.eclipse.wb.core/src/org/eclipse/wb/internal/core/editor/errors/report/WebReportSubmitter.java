/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
/**
 * Submits the report data directly by web to the bug tracking site.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
package org.eclipse.wb.internal.core.editor.errors.report;

import org.eclipse.core.runtime.IProgressMonitor;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Submits report file directly to the bug tracking site.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
final class WebReportSubmitter implements IReportSubmitter {
  private static final int CHUNK_SIZE = 50 * 1024;
  private static final String[] BASE_URL_DOMAINS = {
      "google.com",
      "appspot.com",
      "instantiations.com"};
  private static final String[] REPORTER_URLS = {
      "http://eclipsetelemetry.google.com/bug",
      "http://eclipsetelemetry.appspot.com/bug",
      "http://reporter.instantiations.com/errortrack/"};

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReportSubmitter
  //
  ////////////////////////////////////////////////////////////////////////////
  public void submit(String xml, IProgressMonitor monitor) throws Exception {
    HttpURLConnection conn = null;
    OutputStream ops = null;
    try {
      File xmlFile = new File(xml);
      int xmlFileLength = (int) xmlFile.length();
      // prepare connection
      URL url = pingServer();
      if (url == null) {
        throw new Exception("Unable to connect to the bug reporting server.");
      }
      conn = (HttpURLConnection) url.openConnection();
      conn.setFixedLengthStreamingMode(xmlFileLength);
      conn.setDoOutput(true);
      conn.setRequestMethod("PUT");
      conn.setRequestProperty("Content-type", "text/xml");
      ops = conn.getOutputStream();
      // do the send operation and maintain progress every 50k of data
      FileInputStream is = new FileInputStream(xmlFile);
      byte[] buffer = new byte[CHUNK_SIZE];
      int chunks = xmlFileLength / CHUNK_SIZE;
      // sending data
      monitor.beginTask("Submitting report...", chunks);
      try {
        int readBytes;
        while ((readBytes = is.read(buffer, 0, buffer.length)) != -1) {
          ops.write(buffer, 0, readBytes);
          ops.flush();
          monitor.worked(1);
        }
      } finally {
        IOUtils.closeQuietly(is);
        monitor.done();
      }
      monitor.beginTask("Waiting for response...", IProgressMonitor.UNKNOWN);
      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream inputStream = conn.getInputStream();
        List<String> lines = IOUtils.readLines(inputStream);
        String response = "";
        if (lines != null && lines.size() > 0) {
          response = lines.get(0);
          if (response.startsWith("Response: 0")) {
            // all fine
            return;
          }
        }
        throw new Exception("Invalid error-tracking server response: " + response);
      } else {
        throw new Exception("Error sending report via Internet: " + responseCode);
      }
    } finally {
      monitor.done();
      // close
      IOUtils.closeQuietly(ops);
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /**
   * Ping the bug reporting server - make sure that we have a network connection and the server is
   * reachable. This method returns the server URL, taking into account any redirects.
   * 
   * @return the URL to use if the server is reachable, null otherwise
   */
  private URL pingServer() {
    for (String url : REPORTER_URLS) {
      try {
        URL respondedUrl = pingUrl(url);
        if (respondedUrl != null) {
          if (verifyUrl(respondedUrl)) {
            return respondedUrl;
          }
        }
      } catch (IOException ioe) {
        // ignore this
      }
    }
    return null;
  }

  private URL pingUrl(String urlText) throws IOException {
    URL url = new URL(urlText);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setInstanceFollowRedirects(true);
    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
      return null;
    }
    connection.disconnect();
    URL redirectedURL = connection.getURL();
    return redirectedURL;
  }

  /**
   * Verify that the URL is in the correct domain - it may have come from a URL redirection.
   */
  private boolean verifyUrl(URL url) {
    String urlHost = url.getHost();
    if (urlHost == null) {
      return false;
    }
    // Make sure the url is in the correct domain - i.e. that it ends with "google.com".
    for (int i = 0; i < BASE_URL_DOMAINS.length; i++) {
      if (urlHost.endsWith(BASE_URL_DOMAINS[i])) {
        return true;
      }
    }
    return false;
  }
}