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
package org.eclipse.wb.internal.core.editor.errors.report.logs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.errors.report.ErrorReport;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodesCollection;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.eclipse.core.resources.IProject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Adds parser errors/warnings into report.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public class ParseErrorsLogProvider implements IErrorLogsProvider {
  public List<File> getLogFiles(IProject project) throws Exception {
    JavaInfo javaInfo = EditorState.getActiveJavaInfo();
    if (javaInfo == null) {
      return ImmutableList.of();
    }
    AstEditor editor = javaInfo.getEditor();
    EditorState editorState = EditorState.get(editor);
    if (editorState == null) {
      return ImmutableList.of();
    }
    List<File> files = Lists.newArrayList();
    {
      File file = writeWarnings(editorState.getWarnings());
      if (file != null) {
        files.add(file);
      }
    }
    {
      File file = writeBadNodes("bad-refresh-nodes", editor, editorState.getBadRefreshNodes());
      if (file != null) {
        files.add(file);
      }
    }
    {
      File file = writeBadNodes("bad-parser-nodes", editor, editorState.getBadParserNodes());
      if (file != null) {
        files.add(file);
      }
    }
    return files;
  }

  private File writeBadNodes(String title, AstEditor editor, BadNodesCollection badNodes)
      throws IOException {
    if (!badNodes.isEmpty()) {
      File tempFile = getTempFile(title);
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      try {
        List<BadNodeInformation> nodes = badNodes.nodes();
        for (BadNodeInformation badNode : nodes) {
          IOUtils2.writeString(outputStream, editor.getSource(badNode.getNode()));
          writeSmallDivider(outputStream);
          IOUtils2.writeString(outputStream, ExceptionUtils.getStackTrace(badNode.getException()));
          writeDivider(outputStream);
        }
      } finally {
        IOUtils.closeQuietly(outputStream);
      }
      return tempFile;
    }
    return null;
  }

  private File writeWarnings(List<EditorWarning> warnings) throws IOException {
    if (!warnings.isEmpty()) {
      File tempFile = getTempFile("warnings");
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      try {
        for (EditorWarning editorWarning : warnings) {
          IOUtils2.writeString(outputStream, editorWarning.getMessage());
          writeSmallDivider(outputStream);
          IOUtils2.writeString(
              outputStream,
              ExceptionUtils.getStackTrace(editorWarning.getException()));
          writeDivider(outputStream);
        }
      } finally {
        IOUtils.closeQuietly(outputStream);
      }
      return tempFile;
    }
    return null;
  }

  private void writeDivider(FileOutputStream outputStream) throws IOException {
    IOUtils2.writeString(
        outputStream,
        "\r\n==================================================\r\n\r\n");
  }

  private void writeSmallDivider(FileOutputStream outputStream) throws IOException {
    IOUtils2.writeString(outputStream, "\r\n----\r\n");
  }

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTIC")
  private File getTempFile(String title) throws IOException {
    File tempDir = ErrorReport.getReportTemporaryDirectory();
    File warningsDir = new File(tempDir, "warnings");
    FileUtils.deleteDirectory(warningsDir);
    warningsDir.mkdir();
    warningsDir.deleteOnExit();
    File tempFile = new File(warningsDir, title + ".txt");
    tempFile.deleteOnExit();
    return tempFile;
  }
}
