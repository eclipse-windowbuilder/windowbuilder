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
package org.eclipse.wb.internal.dev.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * {@link IProjectNature} for {@link WbBuilder}.
 *
 * @author scheglov_ke
 */
public class WbNature implements IProjectNature {
  public static final String ID = "org.eclipse.wb.dev.wbNature";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Project
  //
  ////////////////////////////////////////////////////////////////////////////
  private IProject m_project;

  public IProject getProject() {
    return m_project;
  }

  public void setProject(IProject project) {
    m_project = project;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configure
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure() throws CoreException {
    IProjectDescription desc = m_project.getDescription();
    ICommand[] commands = desc.getBuildSpec();
    for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(WbBuilder.ID)) {
        return;
      }
    }
    ICommand[] newCommands = new ICommand[commands.length + 1];
    System.arraycopy(commands, 0, newCommands, 0, commands.length);
    ICommand command = desc.newCommand();
    command.setBuilderName(WbBuilder.ID);
    newCommands[newCommands.length - 1] = command;
    desc.setBuildSpec(newCommands);
    m_project.setDescription(desc, null);
  }

  public void deconfigure() throws CoreException {
    IProjectDescription description = getProject().getDescription();
    ICommand[] commands = description.getBuildSpec();
    for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(WbBuilder.ID)) {
        ICommand[] newCommands = new ICommand[commands.length - 1];
        System.arraycopy(commands, 0, newCommands, 0, i);
        System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
        description.setBuildSpec(newCommands);
        return;
      }
    }
  }
}
