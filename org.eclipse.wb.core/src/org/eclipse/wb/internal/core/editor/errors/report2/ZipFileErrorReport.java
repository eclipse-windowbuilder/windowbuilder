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
package org.eclipse.wb.internal.core.editor.errors.report2;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

/**
 * Class gathering required (and enabled) error report data and then zip it into a single file.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public final class ZipFileErrorReport implements IErrorReport {
  // fields
  private final List<IReportEntry> m_entries = Lists.newArrayList();
  private final IProject m_project;
  private final IReportEntry m_sourceFileReport;
  private final ProjectReportEntry m_projectFileReport;
  private final EnvironmentFileReportInfo m_environmentFileReport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates the {@link ZipFileErrorReport}.
   *
   * @param defaultScreenShot
   *          the {@link Image} of screenshot. May be <code>null</code>.
   * @param project
   *          the IProject instance at which error occurred. Required.
   * @param sourceFileReport
   *          a report entry for current editing source contents. May be <code>null</code>.
   */
  public ZipFileErrorReport(Image defaultScreenShot, IProject project, IReportEntry sourceFileReport) {
    m_defaultScreenShot = defaultScreenShot;
    m_project = project;
    m_sourceFileReport = sourceFileReport;
    // create reports
    m_projectFileReport = new ProjectReportEntry(project);
    m_environmentFileReport = new EnvironmentFileReportInfo(project);
    // setup file attachments
    setupScreenshots();
    setupFiles();
    // add reports
    m_entries.add(m_environmentFileReport);
    m_entries.add(new PreferencesFileReportEntry());
    m_entries.add(new EclipseLogEntryFileReportInfo());
    if (m_sourceFileReport != null) {
      m_entries.add(m_sourceFileReport);
    }
    // search for more
    addExternalReportEntries();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares report as zip file and opens default file-system browser pointing to file location.
   */
  public void reportProblem(IProgressMonitor monitor) throws Exception {
    // create zip.
    String reportFileName = createZipReport(monitor);
    // open
    Program.launch(new Path(reportFileName).removeLastSegments(1).toOSString());
  }

  /**
   * Compress the contents of report into single zip file.
   */
  private String createZipReport(IProgressMonitor monitor) throws Exception {
    monitor.beginTask(Messages.ZipFileErrorReport_taskTitle, m_entries.size());
    // store zip as temp file
    // prepare temp dir and file
    File tempDir = getReportTemporaryDirectory();
    String fileName = "report-" + DateFormatUtils.format(new Date(), "yyyyMMdd-HHmmss") + ".zip";
    File tempFile = new File(tempDir, fileName);
    tempFile.deleteOnExit();
    // create stream
    ZipOutputStream zipStream = null;
    try {
      zipStream = new ZipOutputStream(new FileOutputStream(tempFile));
      zipStream.setLevel(9);
      // compress the files
      for (IReportEntry reportInfo : m_entries) {
        try {
          reportInfo.write(zipStream);
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
      }
    } finally {
      IOUtils.closeQuietly(zipStream);
    }
    return tempFile.getAbsolutePath();
  }

  /**
   * @return the temporary directory for holding report files.
   */
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTIC")
  public static File getReportTemporaryDirectory() {
    String tmpDirPath = System.getProperty("java.io.tmpdir");
    tmpDirPath += File.separator + "wbp-report";
    File tempDir = new File(tmpDirPath);
    tempDir.mkdirs();
    tempDir.deleteOnExit();
    return tempDir;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Screenshots
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, IReportEntry> m_screenshotsMap;
  private final Image m_defaultScreenShot;

  public boolean hasDefaultScreenshot() {
    return m_defaultScreenShot != null;
  }

  public boolean hasScreenshot(String filePath) {
    return m_screenshotsMap.get(filePath) != null;
  }

  public void includeScreenshot(String filePath, boolean include) {
    if (include) {
      addScreenshot(filePath);
    } else {
      IReportEntry removed = m_screenshotsMap.remove(filePath);
      m_entries.remove(removed);
    }
  }

  /**
   * Adds user screenshot to be sent.
   *
   * @param filePath
   *          the full path to a file with screenshot.
   */
  private void addScreenshot(String filePath) {
    if (!hasScreenshot(filePath)) {
      ScreenshotFileReportEntry reportInfo = new ScreenshotFileReportEntry(filePath);
      m_screenshotsMap.put(filePath, reportInfo);
      m_entries.add(reportInfo);
    }
  }

  /**
   * Removes all screenshots from being sent list, adds default screenshot if available.
   */
  public void setupScreenshots() {
    removeEntries(m_screenshotsMap);
    m_screenshotsMap = Maps.newTreeMap();
    if (hasDefaultScreenshot()) {
      addScreenshot("default-screenshot.png");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Files
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, IReportEntry> m_filesMap;

  public boolean hasFile(String filePath) {
    return m_filesMap.get(filePath) != null;
  }

  public void includeFile(String filePath, boolean include) {
    if (include) {
      addFile(filePath);
    } else {
      IReportEntry removed = m_filesMap.remove(filePath);
      m_entries.remove(removed);
    }
  }

  /**
   * Adds a file to be sent.
   *
   * @param filePath
   *          the full path to a file.
   */
  private void addFile(String filePath) {
    if (!hasFile(filePath)) {
      FileReportEntry reportInfo = new FileReportEntry(filePath) {
        @Override
        public String getName() {
          return "files/" + FilenameUtils.getName(getFilePath());
        }
      };
      m_filesMap.put(filePath, reportInfo);
      m_entries.add(reportInfo);
    }
  }

  /**
   * Removes all files from being sent list.
   */
  public void setupFiles() {
    removeEntries(m_filesMap);
    m_filesMap = Maps.newTreeMap();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getComputerInfo() {
    try {
      String contents = IOUtils2.readString(m_environmentFileReport.getContents());
      return "<html><body><pre>" + contents + "</pre></body></html>";
    } catch (Throwable e) {
      // should not happen :)
      return Messages.ZipFileErrorReport_errorMessage;
    }
  }

  public boolean hasSourceFile() {
    return m_sourceFileReport != null && m_entries.indexOf(m_sourceFileReport) != -1;
  }

  /**
   * @return adds extra {@link IReportEntry}s using extension point.
   */
  private void addExternalReportEntries() {
    List<IReportEntriesProvider> providers =
        ExternalFactoriesHelper.getElementsInstances(
            IReportEntriesProvider.class,
            "org.eclipse.wb.core.errorReportEntriesProviders",
            "provider");
    for (IReportEntriesProvider provider : providers) {
      provider.addEntries(this);
    }
  }

  public IProject getProject() {
    return m_project;
  }

  public void addEntry(IReportEntry entry) {
    if (m_entries.indexOf(entry) == -1) {
      m_entries.add(entry);
    }
  }

  public void removeEntry(IReportEntry entry) {
    m_entries.remove(entry);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Various settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets whether or not include compilation unit into this report.
   */
  public void setIncludeSourceFile(boolean include) {
    checkInclude(m_sourceFileReport, include);
  }

  /**
   * Sets whether or not include the entire project into this report.
   */
  public void setIncludeProject(boolean include) {
    checkInclude(m_projectFileReport, include);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  private void removeEntries(Map<String, IReportEntry> map) {
    if (map != null) {
      for (Entry<String, IReportEntry> entry : map.entrySet()) {
        m_entries.remove(entry.getValue());
      }
    }
  }

  private void checkInclude(IReportEntry report, boolean include) {
    if (include) {
      addEntry(report);
    } else {
      removeEntry(report);
    }
  }
}
