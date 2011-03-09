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
package org.eclipse.wb.internal.core.editor.errors.report;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.XmlWriter;
import org.eclipse.wb.internal.core.utils.base64.Base64;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Provides support for adding screenshots into report.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public final class ScreenshotsReportInfo implements IReportInfo {
  // fields
  private Map<String, Boolean> m_screenshotsMap = Maps.newTreeMap();
  private Image m_defaultScreenshot;

  /**
   * 
   * Sets default screenshot to send. May be <code>null</code>.
   * 
   * @param screenshot
   *          the {@link Image} of screenshot.
   */
  public void setDefaultScreenshot(Image screenshot) {
    m_defaultScreenshot = screenshot;
  }

  /**
   * @return <code>true</code> if report has default screenshot set.
   */
  public boolean hasDefaultScreenshot() {
    return m_defaultScreenshot != null;
  }

  /**
   * Adds user screenshot to be sent.
   * 
   * @param filePath
   *          the full path to file with screenshot.
   */
  public void addScreenshot(String filePath) {
    m_screenshotsMap.put(filePath, true);
  }

  /**
   * @param filePath
   *          the full path to file with screenshot.
   * @return <code>true</code> if this screenshot already added.
   */
  public boolean hasScreenshot(String filePath) {
    return m_screenshotsMap.get(filePath) != null;
  }

  /**
   * Includes screenshot defined by <code>filePath</code> to being sent list.
   * 
   * @param filePath
   *          the full path to file with screenshot.
   * @param include
   *          if <code>true</code> then screenshot would be included to being sent.
   */
  public void includeScreenshot(String filePath, boolean include) {
    m_screenshotsMap.put(filePath, include);
  }

  /**
   * Removes all screenshots from being sent list.
   */
  public void removeAllScreenshots() {
    m_screenshotsMap = Maps.newTreeMap();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReportInfo 
  //
  ////////////////////////////////////////////////////////////////////////////
  public void writeXML(XmlWriter xmlWriter) throws Exception {
    if (hasDefaultScreenshot()) {
      new ScreenshotImageReportInfo().writeXML(xmlWriter);
    }
    ReportInfoUtils.checkFiles(m_screenshotsMap);
    if (m_screenshotsMap.isEmpty() || ReportInfoUtils.isAllFilesExcluded(m_screenshotsMap)) {
      // nothing to encode
      return;
    }
    for (final String filename : m_screenshotsMap.keySet()) {
      if (!m_screenshotsMap.get(filename)) {
        continue;
      }
      new ScreenshotFileReportInfo(filename).writeXML(xmlWriter);
    }
  }

  public String getHTML() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner classes 
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates xml part of default image.
   */
  private final class ScreenshotImageReportInfo extends FileReportInfo {
    @Override
    protected void encode(OutputStream outStream) throws Exception {
      // store image to stream
      ImageLoader loader = new ImageLoader();
      loader.data = new ImageData[]{m_defaultScreenshot.getImageData()};
      loader.save(new Base64.OutputStream(outStream), SWT.IMAGE_PNG);
    }

    @Override
    protected String getFileNameAttribute() {
      return "wbp-error-screenshot.png";
    }
  }
  /**
   * Creates XML part for one of every added image.
   */
  private static class ScreenshotFileReportInfo extends FileReportInfo {
    private String m_filePath;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private ScreenshotFileReportInfo(String filepath) {
      m_filePath = filepath;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // FileReportInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void encode(OutputStream outStream) throws Exception {
      if (FilenameUtils.getExtension(m_filePath).equalsIgnoreCase("bmp")) {
        // attempt to convert bmp into png
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.load(m_filePath);
        imageLoader.save(new Base64.OutputStream(outStream), SWT.IMAGE_PNG);
        // change file extension
        String fullPath = FilenameUtils.getFullPath(m_filePath);
        if (!fullPath.endsWith(File.separator)) {
          fullPath += File.separator;
        }
        m_filePath = fullPath + FilenameUtils.getBaseName(m_filePath) + ".png";
      } else {
        FileInputStream sourceStream = new FileInputStream(m_filePath);
        IOUtils.copy(sourceStream, new Base64.OutputStream(outStream));
        IOUtils.closeQuietly(sourceStream);
      }
    }

    @Override
    protected String getFileNameAttribute() {
      return FilenameUtils.getName(m_filePath);
    }
  }
}
