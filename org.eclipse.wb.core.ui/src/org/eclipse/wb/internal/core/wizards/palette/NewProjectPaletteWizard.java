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
package org.eclipse.wb.internal.core.wizards.palette;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.UiMessages;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;

import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;

/**
 * {@link Wizard} that creates custom palette, i.e. "wbp-meta/*.wbp-palette.xml" file.
 *
 * @author scheglov_ke
 * @coverage core.wizards.ui
 */
public final class NewProjectPaletteWizard extends Wizard implements INewWizard {
  private IStructuredSelection m_selection;
  private NewProjectPalettePage m_page;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewProjectPaletteWizard() {
    setWindowTitle(UiMessages.NewProjectPaletteWizard_title);
    setNeedsProgressMonitor(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    {
      m_page = new NewProjectPalettePage();
      m_page.init(m_selection);
      addPage(m_page);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean performFinish() {
    try {
      IProject project = m_page.getJavaProject().getProject();
      ToolkitDescription toolkit = m_page.getToolkit();
      // prepare "wbp-meta" folder
      IFolder metaFolder = project.getFolder("wbp-meta");
      if (!metaFolder.exists()) {
        metaFolder.create(true, true, null);
      }
      // create palette file
      IFile paletteFile;
      {
        paletteFile = metaFolder.getFile(toolkit.getId() + ".wbp-palette.xml");
        String content =
            StringUtils.join(
                new String[]{
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<palette>",
                    "\t<category id=\"someUniqueId\" name=\"Custom category\" description=\"Category added for project "
                        + project.getName()
                        + "\" open=\"true\">",
                    "\t\t<component class=\"javax.swing.JButton\"/>",
                    "\t\t<component class=\"javax.swing.JRadioButton\" name=\"Your name\" description=\"You can write any description here.\"/>",
                    "\t</category>",
                    "</palette>",},
                "\n");
        paletteFile.create(new ByteArrayInputStream(content.getBytes()), true, null);
      }
      // open palette file in editor
      IDE.openEditor(DesignerPlugin.getActivePage(), paletteFile);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWorkbenchWizard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_selection = selection;
  }
}
