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

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Attaches the entire project in which error happens.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class ProjectReportEntry implements IReportEntry {
  // constant
  private static final long MAX_FILE_SIZE = 350 * 1024; // no more than 350K
  // field
  private final IProject m_project;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ProjectReportEntry(IProject project) {
    m_project = project;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IFileReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public void write(final ZipOutputStream zipStream) throws Exception {
    // prepare project
    m_project.refreshLocal(IResource.DEPTH_INFINITE, null);
    // traverse project files
    m_project.accept(new IResourceVisitor() {
      public boolean visit(IResource resource) throws CoreException {
        try {
          // skip non-local, unresolved files and files with size more than MAX_FILE_SIZE
          long fileSize = getResourceSize(resource);
          if (fileSize == 0 || fileSize > MAX_FILE_SIZE) {
            return true;
          }
          // skip binaries
          String fileExtension = resource.getFileExtension();
          if (fileExtension == null || fileExtension.equalsIgnoreCase("class")) {
            return false;
          }
          // open stream and put it contents to zip
          IFile file = (IFile) resource;
          InputStream fileStream = file.getContents();
          // remove leading slash
          String filePath =
              "project/"
                  + StringUtils.removeStart(file.getFullPath().toPortableString(), File.separator);
          zipStream.putNextEntry(new ZipEntry(filePath));
          try {
            IOUtils.copy(fileStream, zipStream);
          } finally {
            zipStream.closeEntry();
            IOUtils.closeQuietly(fileStream);
          }
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
        return true;
      }
    });
  }

  /**
   * Return the size of the supplied file.
   */
  private static long getResourceSize(IResource resource) {
    if (resource.getType() != IResource.FILE) {
      return 0;
    }
    IFile file = (IFile) resource;
    URI location = file.getLocationURI();
    if (location == null) {
      return 0;
    }
    return new File(location.getSchemeSpecificPart()).length();
  }
}
