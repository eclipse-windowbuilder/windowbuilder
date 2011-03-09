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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.XmlWriter;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;

/**
 * Class gathering required (and enabled) error report data and submits it using
 * {@link IReportSubmitter} instance.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public final class ErrorReport {
  // constants
  private static final String XML_TAG_PRODUCT_REPORT = "product-report";
  private static final String XML_TAG_SUMMARY = "summary";
  private static final String XML_TAG_DESCRIPTION = "description";
  private static final String XML_ATTR_REPLY_TO_NAME = "reply-to-name";
  private static final String XML_ATTR_REPLY_TO = "reply-to";
  private static final String XML_ATTR_PRODUCT = "product";
  // fields
  private IReportSubmitter m_submitter = IReportSubmitter.WEB; // not final!
  private boolean m_includeSourceFile = true;
  private boolean m_includeProject;
  private String m_summary;
  private String m_description;
  // IReportInfos
  private final List<IReportInfo> m_reportInfos = Lists.newArrayList();
  private final ZipMultipleFilesReportInfo m_fileReportInfo;
  private final ScreenshotsReportInfo m_screenshotsReportInfo;
  private final RegistrationReportInfo m_registrationInfo;
  private final ComputerReportInfo m_computerInfo;
  private final String m_productName;
  private final IProject m_project;
  private final SourceInfo m_sourceFile;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates the {@link ErrorReport}.
   * 
   * @param defaultScreenShot
   *          the {@link Image} of screenshot. May be <code>null</code>.
   * @param the
   *          Instantiation's product code related to current report. Required.
   * @param project
   *          the IProject instance at which error occurred. Required.
   */
  public ErrorReport(Image defaultScreenShot,
      String productName,
      IProject project,
      SourceInfo sourceFile) {
    m_productName = productName;
    m_project = project;
    m_sourceFile = sourceFile;
    m_registrationInfo = new RegistrationReportInfo();
    m_reportInfos.add(m_registrationInfo);
    m_computerInfo = new ComputerReportInfo(m_project);
    m_reportInfos.add(m_computerInfo);
    m_reportInfos.add(new PreferencesReportInfo());
    m_fileReportInfo = new ZipMultipleFilesReportInfo();
    m_reportInfos.add(m_fileReportInfo);
    m_screenshotsReportInfo = new ScreenshotsReportInfo();
    m_reportInfos.add(m_screenshotsReportInfo);
    m_screenshotsReportInfo.setDefaultScreenshot(defaultScreenShot);
    m_reportInfos.add(new EclipseLogEntryReportInfo());
    m_reportInfos.add(new LogsReportInfo(m_project));
    // add external providers
    for (IReportInfo reportInfo : getExternalReportInfos()) {
      m_reportInfos.add(reportInfo);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares report as xml file and submits it using chosen way.
   */
  public void reportProblem(IProgressMonitor monitor) throws Exception {
    if (m_includeSourceFile) {
      m_reportInfos.add(new SourceFileReportInfo(m_sourceFile));
    }
    if (m_includeProject) {
      m_reportInfos.add(new ProjectReportInfo(m_project));
    }
    m_submitter.submit(prepareXML(monitor), monitor);
  }

  /**
   * Gathers the data and prepares xml to submit.
   * 
   * @return the path to xml file in temp directory.
   * @throws Exception
   */
  private String prepareXML(IProgressMonitor monitor) throws Exception {
    monitor.beginTask("Preparing report data...", m_reportInfos.size());
    // store xml into temp file
    // prepare temp dir and file
    File tempDir = getReportTemporaryDirectory();
    File tempFile = File.createTempFile("report", ".xml", tempDir);
    tempFile.deleteOnExit();
    // generate report
    XmlWriter writer = new XmlWriter(tempFile.getAbsolutePath());
    try {
      writer.beginTag(XML_TAG_PRODUCT_REPORT);
      writer.writeAttribute(XML_ATTR_PRODUCT, m_productName);
      writer.writeAttribute(XML_ATTR_REPLY_TO, m_registrationInfo.getEmail());
      writer.writeAttribute(XML_ATTR_REPLY_TO_NAME, m_registrationInfo.getName());
      writer.endTag();
      writer.write(XML_TAG_SUMMARY, m_summary);
      writer.write(XML_TAG_DESCRIPTION, m_description);
      for (IReportInfo reportInfo : m_reportInfos) {
        if (reportInfo != null) {
          reportInfo.writeXML(writer);
        }
        monitor.worked(1);
      }
      writer.closeTag();
    } finally {
      monitor.done();
      writer.close();
    }
    return tempFile.getAbsolutePath();
  }

  /**
   * @return the temporary directory for holding report files.
   */
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTIC")
  public static File getReportTemporaryDirectory() {
    String tmpDirPath = System.getProperty("java.io.tmpdir");
    tmpDirPath += File.separator + "wbp-report";
    File tempDir = new File(tmpDirPath);
    tempDir.mkdir();
    tempDir.deleteOnExit();
    return tempDir;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Screenshots
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if report has default screenshot set.
   */
  public boolean hasDefaultScreenshot() {
    return m_screenshotsReportInfo.hasDefaultScreenshot();
  }

  /**
   * Adds user screenshot to be sent.
   * 
   * @param filePath
   *          the full path to file with screenshot.
   */
  public void addScreenshot(String filePath) {
    m_screenshotsReportInfo.addScreenshot(filePath);
  }

  /**
   * @param filePath
   *          the full path to file with screenshot.
   * @return <code>true</code> if this screenshot already added.
   */
  public boolean hasScreenshot(String filePath) {
    return m_screenshotsReportInfo.hasScreenshot(filePath);
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
    m_screenshotsReportInfo.includeScreenshot(filePath, include);
  }

  /**
   * Removes all screenshots from being sent list.
   */
  public void removeAllScreenshots() {
    m_screenshotsReportInfo.removeAllScreenshots();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Various settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link IReportSubmitter} for this report.
   */
  public void setSubmitter(IReportSubmitter submitter) {
    m_submitter = submitter;
  }

  /**
   * Getter for {@link IReportSubmitter}.
   * 
   * @return the currently installed {@link IReportSubmitter}.
   */
  public final IReportSubmitter getSubmitter() {
    return m_submitter;
  }

  /**
   * Sets whether or not include compilation unit into this report.
   */
  public void setIncludeSourceFile(boolean include) {
    m_includeSourceFile = include;
  }

  /**
   * Sets whether or not include the entire project into this report.
   */
  public void setIncludeProject(boolean include) {
    m_includeProject = include;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Files
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds file to be sent.
   * 
   * @param filePath
   *          the full path to file.
   */
  public void addFile(String filePath) {
    m_fileReportInfo.addFile(filePath);
  }

  /**
   * @param filePath
   *          the full path to file.
   * @return <code>true</code> if this file already added.
   */
  public boolean hasFile(String filePath) {
    return m_fileReportInfo.hasFile(filePath);
  }

  /**
   * Includes file defined by <code>filePath</code> to being sent list.
   * 
   * @param filePath
   *          the full path to file.
   * @param include
   *          if <code>true</code> then file would be included to being sent list.
   */
  public void includeFile(String filePath, boolean include) {
    m_fileReportInfo.includeFile(filePath, include);
  }

  /**
   * Removes all file from being sent list.
   */
  public void removeAllFiles() {
    m_fileReportInfo.removeAllFiles();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Problem description
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Set "summary" text for this problem and wraps it to xml tag with CDATA.
   */
  public void setSummary(String summaryText) {
    m_summary = summaryText;
  }

  /**
   * Set "description" text for this problem and wraps it to xml tag with CDATA.
   */
  public void setDescription(String moreInfoText) {
    m_description = moreInfoText;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getRegistrationInfo() {
    return m_registrationInfo.getHTML();
  }

  public String getName() {
    return m_registrationInfo.getName();
  }

  public void setName(String name) {
    m_registrationInfo.setName(name);
  }

  public String getEmail() {
    return m_registrationInfo.getEmail();
  }

  public void setEmail(String email) {
    m_registrationInfo.setEmail(email);
  }

  public String getComputerInfo() {
    return m_computerInfo.getHTML();
  }

  public boolean hasSourceFile() {
    return m_sourceFile != null;
  }

  /**
   * @return the instances of extra {@link IReportInfo}.
   */
  private static List<IReportInfo> getExternalReportInfos() {
    return ExternalFactoriesHelper.getElementsInstances(
        IReportInfo.class,
        "org.eclipse.wb.core.errorReportEntries",
        "entry");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source File Info
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Small class representing the source code being attached.
   */
  public static final class SourceInfo {
    private final String m_name;
    private final String m_content;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SourceInfo(String name, String content) {
      m_name = name;
      m_content = content;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public String getContent() {
      return m_content;
    }

    public String getName() {
      return m_name;
    }

    public boolean isValid() {
      return !StringUtils.isEmpty(m_content) && !StringUtils.isEmpty(m_name);
    }
  }
}
