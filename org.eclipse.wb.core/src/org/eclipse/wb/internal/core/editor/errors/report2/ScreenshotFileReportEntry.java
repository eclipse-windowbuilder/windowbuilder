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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageLoader;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Report entry for writing screenshot files. Does converting from 'bmp' format into 'png' to
 * preserve report size.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class ScreenshotFileReportEntry extends FileReportEntry {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ScreenshotFileReportEntry(String filePath) {
    super(filePath);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractFileReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getName() {
    String filePath = getFilePath();
    String name;
    if (isBmp()) {
      // change file extension
      name = FilenameUtils.getBaseName(filePath) + ".png";
    } else {
      name = FilenameUtils.getName(filePath);
    }
    return "screenshots/" + name;
  }

  @Override
  protected InputStream getContents() throws Exception {
    if (isBmp()) {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      // attempt to convert bmp into png
      ImageLoader imageLoader = new ImageLoader();
      imageLoader.load(getFilePath());
      imageLoader.save(outStream, SWT.IMAGE_PNG);
      return new ByteArrayInputStream(outStream.toByteArray());
    } else {
      return super.getContents();
    }
  }

  private boolean isBmp() {
    return FilenameUtils.getExtension(getFilePath()).equalsIgnoreCase("bmp");
  }
}
