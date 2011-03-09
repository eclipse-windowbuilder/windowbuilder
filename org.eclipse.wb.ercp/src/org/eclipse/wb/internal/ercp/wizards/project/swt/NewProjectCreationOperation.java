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
package org.eclipse.wb.internal.ercp.wizards.project.swt;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.ercp.wizards.project.AbstractProjectCreationOperation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of {@link WorkspaceModifyOperation} to create eSWT project.
 * 
 * @author scheglov_ke
 * @coverage ercp.wizards
 */
public final class NewProjectCreationOperation extends AbstractProjectCreationOperation {
  private final String m_ercpLocation;
  private final boolean m_generateSample;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewProjectCreationOperation(String projectName, String ercpLocation, boolean generateSample) {
    super(projectName);
    m_ercpLocation = ercpLocation;
    m_generateSample = generateSample;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WorkspaceModifyOperation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void execute(final IProgressMonitor monitor) throws CoreException,
      InvocationTargetException, InterruptedException {
    // create Java project
    super.execute(monitor);
    // add template files
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        // add jar's
        for (String absoluteJar : getAbsoluteJars(m_ercpLocation)) {
          addClassPathEntry(JavaCore.newLibraryEntry(new Path(absoluteJar), null, null), monitor);
        }
        // fill values map
        String packageName;
        String typeName;
        Map<String, String> valueMap = Maps.newTreeMap();
        {
          valueMap.put("projectName", m_projectName);
          valueMap.put("packageName", packageName = "com.test");
          valueMap.put("typeName", typeName = "SampleApplication");
        }
        // generate sample elements
        if (m_generateSample) {
          {
            String unitName = typeName + ".java";
            ICompilationUnit unit =
                createTemplateUnit(
                    "sample/Application.jav",
                    valueMap,
                    packageName,
                    unitName,
                    monitor);
            scheduleOpen((IFile) unit.getUnderlyingResource());
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String[] RELATIVE_JARS = {
      "org.eclipse.equinox.common_",
      "org.eclipse.core.runtime_",
      "eswt-converged",
      "org.eclipse.ercp.jface_"};

  /**
   * @param location
   *          the location of eRCP directory.
   * 
   * @return the absolute locations of jar's that should be added to classpath.
   * 
   * @throws FileNotFoundException
   *           if any jar can not be found.
   */
  static String[] getAbsoluteJars(String location) throws FileNotFoundException {
    // validate location
    {
      // no location
      if (location.length() == 0) {
        throw new FileNotFoundException("No eRCP location.");
      }
      // no such directory
      File locationFile = new File(location);
      if (!locationFile.exists() || !locationFile.isDirectory()) {
        throw new FileNotFoundException("Invalid eRCP location, no such directory.");
      }
      // wrong directory
      File startupFile = new File(locationFile, "win32/eRCP/startup.jar");
      if (!startupFile.exists()) {
        throw new FileNotFoundException("eRCP location should be just directory where you extract eRCP."
            + " Designer automatically appends /win32/eRCP/ to check this.");
      }
    }
    // fill absolute jar's
    String[] absoluteJars = new String[RELATIVE_JARS.length];
    Collection<File> files = FileUtils.listFiles(new File(location), new String[]{"jar"}, true);
    for (int i = 0; i < RELATIVE_JARS.length; i++) {
      String jarPrefix = RELATIVE_JARS[i];
      for (File file : files) {
        if (file.getName().startsWith(jarPrefix)) {
          absoluteJars[i] = file.getAbsolutePath();
          break;
        }
      }
      if (absoluteJars[i] == null) {
        throw new FileNotFoundException("Can not find jar file with prefix \"" + jarPrefix + "\".");
      }
    }
    // OK, we have all jar's
    return absoluteJars;
  }
}
