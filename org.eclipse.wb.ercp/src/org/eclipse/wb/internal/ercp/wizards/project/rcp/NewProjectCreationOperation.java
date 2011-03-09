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
package org.eclipse.wb.internal.ercp.wizards.project.rcp;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.ercp.wizards.project.AbstractProjectCreationOperation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Implementation of {@link WorkspaceModifyOperation} to create eRCP project.
 * 
 * @author scheglov_ke
 * @coverage ercp.wizards
 */
public final class NewProjectCreationOperation extends AbstractProjectCreationOperation {
  private final boolean m_generateSample;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewProjectCreationOperation(String projectName, boolean generateSample) {
    super(projectName);
    m_generateSample = generateSample;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WorkspaceModifyOperation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
      InterruptedException {
    super.execute(monitor);
    createPDEProject(monitor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PDE project
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createPDEProject(final IProgressMonitor monitor) throws CoreException,
      InvocationTargetException {
    addNature("org.eclipse.pde.PluginNature", monitor);
    // add PDE container
    addClassPathEntry(
        JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")),
        monitor);
    // add template files
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        // fill values map
        String packageName;
        Map<String, String> valueMap = Maps.newTreeMap();
        {
          valueMap.put("projectName", m_projectName);
          {
            String bundleName = m_projectName.replace(' ', '_');
            valueMap.put("bundleName", bundleName + " Plug-in");
            valueMap.put("bundleSymbolicName", bundleName);
          }
          {
            packageName = m_projectName.toLowerCase().replace(' ', '.');
            valueMap.put("packageName", packageName);
          }
        }
        // create MANIFEST.MF file
        {
          IFile manifestFile =
              createTemplateFile("empty/MANIFEST.MF", valueMap, "META-INF", "MANIFEST.MF", monitor);
          scheduleOpen(manifestFile);
        }
        // create plugin.xml
        createTemplateFile("empty/plugin.xml", valueMap, null, "plugin.xml", monitor);
        // create Activator.java
        createTemplateUnit("empty/Activator.jav", valueMap, packageName, "Activator.java", monitor);
        // generate sample elements
        if (m_generateSample) {
          createTemplateFile("sample/plugin.xml", valueMap, null, "plugin.xml", monitor);
          createTemplateUnit(
              "sample/MyViewPart.jav",
              valueMap,
              packageName + ".views",
              "MyViewPart.java",
              monitor);
          createTemplateUnit(
              "sample/PreferencePage_1.jav",
              valueMap,
              packageName + ".preferences",
              "PreferencePage_1.java",
              monitor);
          createTemplateUnit(
              "sample/PreferencePage_2.jav",
              valueMap,
              packageName + ".preferences",
              "PreferencePage_2.java",
              monitor);
        }
      }
    });
  }
}
