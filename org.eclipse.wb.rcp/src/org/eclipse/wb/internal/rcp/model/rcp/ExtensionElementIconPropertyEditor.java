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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.SinglePluginFileImagePage;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link PropertyEditor} for "icon" attribute in {@link IPluginElement}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ExtensionElementIconPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ExtensionElementIconPropertyEditor();

  private ExtensionElementIconPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof String) {
      return (String) value;
    } else {
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  protected void openDialog(Property _property) throws Exception {
    ExtensionElementProperty<String> property = (ExtensionElementProperty<String>) _property;
    IJavaProject javaProject = JavaCore.create(property.getProject());
    // create dialog
    ImageDialog imageDialog = new ImageDialog(javaProject);
    // set input for dialog
    {
      String text = getText(property);
      imageDialog.setInput(SinglePluginFileImagePage.ID, text);
    }
    // open dialog
    if (imageDialog.open() == Window.OK) {
      ImageInfo imageInfo = imageDialog.getImageInfo();
      String[] data = (String[]) imageInfo.getData();
      property.setValue(data[1]); // image path
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ImageDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class ImageDialog extends AbstractImageDialog {
    private final IJavaProject m_javaProject;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    protected ImageDialog(IJavaProject javaProject) {
      super(DesignerPlugin.getShell(), Activator.getDefault());
      m_javaProject = javaProject;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Pages
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void addPages(Composite parent) {
      addPage(new SinglePluginFileImagePage(parent, SWT.NONE, this, m_javaProject.getProject()));
    }
  }
}