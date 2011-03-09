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
package org.eclipse.wb.internal.xwt.model.property.editor;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.ClasspathImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.DefaultImagePage;
import org.eclipse.wb.internal.core.xml.Activator;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

/**
 * {@link PropertyEditor} for {link org.eclipse.swt.graphics.Image}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public final class ImagePropertyEditor extends TextDialogPropertyEditor
    implements
      IClipboardSourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ImagePropertyEditor();

  private ImagePropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    return ((GenericProperty) property).getExpression();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    return property.getExpression();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    EditorContext context = genericProperty.getObject().getContext();
    IJavaProject javaProject = context.getJavaProject();
    String packagePath = getContextPackagePath(context);
    // create dialog
    ImageDialog imageDialog = new ImageDialog(javaProject);
    // set input for dialog
    {
      String text = getText(property);
      if (text == null) {
        imageDialog.setInput(DefaultImagePage.ID, null);
      } else {
        if (!text.startsWith("/")) {
          text = packagePath + text;
        }
        imageDialog.setInput(ClasspathImagePage.ID, text);
      }
    }
    // open dialog
    if (imageDialog.open() == Window.OK) {
      ImageInfo imageInfo = imageDialog.getImageInfo();
      // prepare expression
      String expression = null;
      {
        String pageId = imageInfo.getPageId();
        if (pageId == DefaultImagePage.ID) {
        } else {
          expression = "/" + imageInfo.getData();
          expression = StringUtils.removeStart(expression, packagePath);
        }
      }
      // set expression
      genericProperty.setExpression(expression, Property.UNKNOWN_VALUE);
    }
  }

  /**
   * @return the '/' separated path of context {@link IFile}.
   */
  private static String getContextPackagePath(EditorContext context) {
    String packagePath = "/";
    // try to append Java package name
    IContainer packageFolder = context.getFile().getParent();
    IJavaElement javaElement = JavaCore.create(packageFolder);
    if (javaElement instanceof IPackageFragment) {
      IPackageFragment packageFragment = (IPackageFragment) javaElement;
      packagePath += packageFragment.getElementName().replace('.', '/') + "/";
    }
    // done
    return packagePath;
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
      addPage(new ClasspathImagePage(parent, SWT.NONE, this, m_javaProject));
      addPage(new DefaultImagePage(parent, SWT.NONE, this));
    }
  }
}