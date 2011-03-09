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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.base64.Base64;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Attaches the entire project in which error happens.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public class ProjectReportInfo extends FileReportInfo {
  // constant
  private static final long MAX_FILE_SIZE = 350 * 1024; // no more than 350K
  // field
  private final IProject m_project;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ProjectReportInfo(IProject project) {
    m_project = project;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FileReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void encode(OutputStream outStream) throws Exception {
    // prepare project
    m_project.refreshLocal(IResource.DEPTH_INFINITE, null);
    // prepare zip
    final ZipOutputStream zipStream = new ZipOutputStream(new Base64.OutputStream(outStream, false));
    zipStream.setLevel(9);
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
              StringUtils.removeStart(file.getFullPath().toPortableString(), File.separator);
          zipStream.putNextEntry(new ZipEntry(filePath));
          IOUtils.copy(fileStream, zipStream);
          zipStream.closeEntry();
          IOUtils.closeQuietly(fileStream);
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
        return true;
      }
    });
    IOUtils.closeQuietly(zipStream);
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

  @Override
  protected String getFileNameAttribute() {
    return m_project.getName() + ".zip";
  }
}
