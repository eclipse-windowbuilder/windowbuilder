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
package org.eclipse.wb.internal.core.xml.editor.palette.model;

import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.xml.Messages;
import org.eclipse.wb.internal.core.xml.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.xml.editor.palette.command.Command;
import org.eclipse.wb.internal.core.xml.editor.palette.command.ComponentAddCommand;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import java.text.MessageFormat;
import java.util.List;

/**
 * Implementation of {@link EntryInfo} that allows user select some type and activate
 * {@link ComponentEntryInfo} with this type.
 *
 * @author scheglov_ke
 * @coverage XML.editor.palette
 */
public final class ChooseComponentEntryInfo extends ToolEntryInfo {
  private static final Image ICON = DesignerPlugin.getImage("palette/ChooseComponent.gif");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseComponentEntryInfo() {
    setName(Messages.ChooseComponentEntryInfo_name);
    setDescription(Messages.ChooseComponentEntryInfo_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() {
    return ICON;
  }

  @Override
  public Tool createTool() throws Exception {
    Shell parentShell = IPaletteSite.Helper.getSite(m_rootJavaInfo).getShell();
    String componentClassName = JdtUiUtils.selectTypeName(parentShell, m_javaProject);
    if (componentClassName != null) {
      ComponentEntryInfo componentEntry = new ComponentEntryInfo();
      // configure component entry
      {
        componentEntry.setId("custom_" + System.currentTimeMillis());
        componentEntry.setName(CodeUtils.getShortClass(componentClassName));
        componentEntry.setDescription(MessageFormat.format(
            Messages.ChooseComponentEntryInfo_newDescription,
            componentClassName));
        componentEntry.setComponentClassName(componentClassName);
      }
      // try to initialize
      if (componentEntry.initialize(m_editPartViewer, m_rootJavaInfo)) {
        addChosenComponent(componentEntry);
        return componentEntry.createTool();
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds chosen {@link ComponentEntryInfo} to the palette.
   */
  private void addChosenComponent(ComponentEntryInfo entry) {
    if (DesignerPlugin.getPreferences().getBoolean(IPreferenceConstants.P_COMMON_PALETTE_ADD_CHOSEN)) {
      CategoryInfo category = getCustomCategory();
      Command command =
          new ComponentAddCommand(entry.getId(),
              entry.getName(),
              entry.getDescription(),
              true,
              entry.getClassName(),
              category);
      getSite().addCommand(command);
    }
  }

  /**
   * @return the {@link CategoryInfo} with name "Custom", existing one, or newly added.
   */
  private CategoryInfo getCustomCategory() {
    PaletteInfo palette = getSite().getPalette();
    // try to find "Custom" category
    List<CategoryInfo> categories = palette.getCategories();
    for (CategoryInfo category : categories) {
      if ("Custom".equals(category.getName())) {
        return category;
      }
    }
    // no "Custom" category, add it
    {
      getSite().addCommand(
          new CategoryAddCommand("category_" + System.currentTimeMillis(),
              "Custom",
              Messages.ChooseComponentEntryInfo_customCategoryDescription,
              true,
              true,
              null));
    }
    // now, after command, we should be able to find "Custom"
    return getCustomCategory();
  }
}
