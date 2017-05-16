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

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.osgi.framework.BundleException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Adds the last Eclipse log entry into report.
 *
 * Working with IStatus mostly copied from the Eclipse code.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class EclipseLogEntryFileReportInfo extends FileReportEntry {
  // constants
  protected static final String ENTRY = "!ENTRY"; //$NON-NLS-1$
  protected static final String SUBENTRY = "!SUBENTRY"; //$NON-NLS-1$
  protected static final String MESSAGE = "!MESSAGE"; //$NON-NLS-1$
  protected static final String STACK = "!STACK"; //$NON-NLS-1$
  // fields
  private Writer m_writer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EclipseLogEntryFileReportInfo() {
    super("last-log-message.txt");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FileReportEntry
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected InputStream getContents() throws Exception {
    IStatus lastStatus = DesignerPlugin.getLastStatus();
    if (lastStatus != null) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      m_writer = new OutputStreamWriter(outputStream);
      try {
        writeLog(0, getLog(lastStatus));
      } finally {
        m_writer.flush();
        IOUtils.closeQuietly(m_writer);
      }
      if (outputStream.size() > 0) {
        return new ByteArrayInputStream(outputStream.toByteArray());
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Working with IStatus
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Writes the log entry to the log using the specified depth. A depth value of 0 idicates that the
   * log entry is the root entry. Any value greater than 0 indicates a sub-entry.
   *
   * @param depth
   *          the depth of th entry
   * @param entry
   *          the entry to log
   * @throws IOException
   *           if any error occurs writing to the log
   */
  private void writeLog(int depth, FrameworkLogEntry entry) throws IOException {
    writeEntry(depth, entry);
    writeMessage(entry);
    writeStack(entry);
    FrameworkLogEntry[] children = entry.getChildren();
    if (children != null) {
      for (int i = 0; i < children.length; i++) {
        writeLog(depth + 1, children[i]);
      }
    }
  }

  /**
   * Writes the ENTRY or SUBENTRY header for an entry. A depth value of 0 indicates that the log
   * entry is the root entry. Any value greater than 0 indicates a sub-entry.
   *
   * @param depth
   *          the depth of th entry
   * @param entry
   *          the entry to write the header for
   * @throws IOException
   *           if any error occurs writing to the log
   */
  private void writeEntry(int depth, FrameworkLogEntry entry) throws IOException {
    if (depth == 0) {
      writeln(); // write a blank line before all !ENTRY tags bug #64406
      write(ENTRY);
    } else {
      write(SUBENTRY);
      writeSpace();
      write(Integer.toString(depth));
    }
    writeSpace();
    write(entry.getEntry());
    writeSpace();
    write(Integer.toString(entry.getSeverity()));
    writeSpace();
    write(Integer.toString(entry.getBundleCode()));
    writeln();
  }

  /**
   * Writes the MESSAGE header to the log for the given entry.
   *
   * @param entry
   *          the entry to write the message for
   * @throws IOException
   *           if any error occurs writing to the log
   */
  private void writeMessage(FrameworkLogEntry entry) throws IOException {
    write(MESSAGE);
    writeSpace();
    writeln(entry.getMessage());
  }

  /**
   * Writes the STACK header to the log for the given entry.
   *
   * @param entry
   *          the entry to write the stacktrace for
   * @throws IOException
   *           if any error occurs writing to the log
   */
  private void writeStack(FrameworkLogEntry entry) throws IOException {
    Throwable t = entry.getThrowable();
    if (t != null) {
      String stack = getStackTrace(t);
      write(STACK);
      writeSpace();
      write(Integer.toString(entry.getStackCode()));
      writeln();
      write(stack);
    }
  }

  /**
   * Writes the given message to the log.
   *
   * @param message
   *          the message
   * @throws IOException
   *           if any error occurs writing to the log
   */
  private void write(String message) throws IOException {
    if (message != null) {
      m_writer.write(message);
    }
  }

  /**
   * Writes the given message to the log and a newline.
   *
   * @param s
   *          the message
   * @throws IOException
   *           if any error occurs writing to the log
   */
  private void writeln(String s) throws IOException {
    write(s);
    writeln();
  }

  /**
   * Writes a newline log.
   *
   * @throws IOException
   *           if any error occurs writing to the log
   */
  private void writeln() throws IOException {
    write("\n");
  }

  /**
   * Writes a space to the log.
   *
   * @throws IOException
   *           if any error occurs writing to the log
   */
  private void writeSpace() throws IOException {
    write(" "); //$NON-NLS-1$
  }

  private FrameworkLogEntry getLog(IStatus status) {
    Throwable t = status.getException();
    List<FrameworkLogEntry> childlist = Lists.newArrayList();
    int stackCode = t instanceof CoreException ? 1 : 0;
    // ensure a substatus inside a CoreException is properly logged
    if (stackCode == 1) {
      IStatus coreStatus = ((CoreException) t).getStatus();
      if (coreStatus != null) {
        childlist.add(getLog(coreStatus));
      }
    }
    if (status.isMultiStatus()) {
      IStatus[] children = status.getChildren();
      for (int i = 0; i < children.length; i++) {
        childlist.add(getLog(children[i]));
      }
    }
    FrameworkLogEntry[] children =
        childlist.size() == 0 ? null : childlist.toArray(new FrameworkLogEntry[childlist.size()]);
    return new FrameworkLogEntry(status.getPlugin(),
        status.getSeverity(),
        status.getCode(),
        status.getMessage(),
        stackCode,
        t,
        children);
  }

  /**
   * Returns a stacktrace string using the correct format for the log
   *
   * @param t
   *          the Throwable to get the stacktrace for
   * @return a stacktrace string
   */
  private String getStackTrace(Throwable t) {
    if (t == null) {
      return null;
    }
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    // ensure the root exception is fully logged
    Throwable root = getRoot(t);
    if (root != null) {
      pw.println("Root exception:"); //$NON-NLS-1$
      root.printStackTrace(pw);
    }
    return sw.toString();
  }

  private Throwable getRoot(Throwable t) {
    Throwable root = null;
    if (t instanceof BundleException) {
      root = ((BundleException) t).getNestedException();
    }
    if (t instanceof InvocationTargetException) {
      root = ((InvocationTargetException) t).getTargetException();
    }
    // skip inner InvocationTargetExceptions and BundleExceptions
    if (root instanceof InvocationTargetException || root instanceof BundleException) {
      Throwable deeplyNested = getRoot(root);
      if (deeplyNested != null) {
        // if we have something more specific, use it, otherwise keep what we have
        root = deeplyNested;
      }
    }
    return root;
  }
}
