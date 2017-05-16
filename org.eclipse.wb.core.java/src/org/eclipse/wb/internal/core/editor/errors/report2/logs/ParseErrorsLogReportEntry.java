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
package org.eclipse.wb.internal.core.editor.errors.report2.logs;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.errors.report2.IReportEntry;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodesCollection;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Adds parser errors/warnings into report.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class ParseErrorsLogReportEntry implements IReportEntry {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  private void writeBadNodes(ZipOutputStream outputStream,
      String title,
      AstEditor editor,
      BadNodesCollection badNodes) throws IOException {
    if (!badNodes.isEmpty()) {
      outputStream.putNextEntry(new ZipEntry("parse-errors/" + title));
      try {
        List<BadNodeInformation> nodes = badNodes.nodes();
        for (BadNodeInformation badNode : nodes) {
          IOUtils2.writeString(outputStream, editor.getSource(badNode.getNode()));
          writeSmallDivider(outputStream);
          IOUtils2.writeString(outputStream, ExceptionUtils.getStackTrace(badNode.getException()));
          writeDivider(outputStream);
        }
      } finally {
        outputStream.closeEntry();
      }
    }
  }

  private void writeWarnings(ZipOutputStream outputStream, List<EditorWarning> warnings)
      throws IOException {
    if (!warnings.isEmpty()) {
      outputStream.putNextEntry(new ZipEntry("parse-errors/warnings.txt"));
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
        outputStream.closeEntry();
      }
    }
  }

  private void writeDivider(OutputStream outputStream) throws IOException {
    IOUtils2.writeString(
        outputStream,
        "\r\n==================================================\r\n\r\n");
  }

  private void writeSmallDivider(OutputStream outputStream) throws IOException {
    IOUtils2.writeString(outputStream, "\r\n----\r\n");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReportEntry
  //
  ////////////////////////////////////////////////////////////////////////////
  public void write(ZipOutputStream zipStream) throws Exception {
    JavaInfo javaInfo = EditorState.getActiveJavaInfo();
    if (javaInfo == null) {
      return;
    }
    AstEditor editor = javaInfo.getEditor();
    EditorState editorState = EditorState.get(editor);
    if (editorState == null) {
      return;
    }
    {
      writeWarnings(zipStream, editorState.getWarnings());
    }
    {
      writeBadNodes(zipStream, "bad-refresh-nodes.txt", editor, editorState.getBadRefreshNodes());
    }
    {
      writeBadNodes(zipStream, "bad-parser-nodes.txt", editor, editorState.getBadParserNodes());
    }
  }
}
