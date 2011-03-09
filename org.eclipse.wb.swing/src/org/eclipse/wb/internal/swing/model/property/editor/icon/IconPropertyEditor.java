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
package org.eclipse.wb.internal.swing.model.property.editor.icon;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.ClasspathImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.DefaultImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.FileImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.NullImagePage;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import javax.swing.Icon;

/**
 * Implementation of {@link PropertyEditor} for {@link Icon}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class IconPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new IconPropertyEditor();

  private IconPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    Expression expression = genericProperty.getExpression();
    JavaInfo javaInfo = genericProperty.getJavaInfo();
    // check for "null" expression
    if (expression instanceof NullLiteral) {
      return "(null)";
    }
    //
    expression = getFinalExpression(javaInfo, expression);
    if (expression instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) expression;
      // absolute path
      if (AstNodeUtils.isCreation(creation, "javax.swing.ImageIcon", new String[]{
          "<init>(java.lang.String)",
          "<init>(java.lang.String,java.lang.String)"})
          && creation.arguments().get(0) instanceof StringLiteral) {
        String path = ((StringLiteral) creation.arguments().get(0)).getLiteralValue();
        return "File: " + path;
      }
      // URL
      if (AstNodeUtils.isCreation(creation, "javax.swing.ImageIcon", new String[]{
          "<init>(java.net.URL)",
          "<init>(java.net.URL,java.lang.String)"})) {
        Expression urlExpression = DomGenerics.arguments(creation).get(0);
        urlExpression = getFinalExpression(javaInfo, urlExpression);
        if (urlExpression instanceof MethodInvocation) {
          MethodInvocation urlInvocation = (MethodInvocation) urlExpression;
          boolean fromClass =
              AstNodeUtils.isMethodInvocation(
                  urlInvocation,
                  "java.lang.Class",
                  "getResource(java.lang.String)");
          boolean fromClassLoader =
              AstNodeUtils.isMethodInvocation(
                  urlInvocation,
                  "java.lang.ClassLoader",
                  "getResource(java.lang.String)");
          if ((fromClass || fromClassLoader)
              && urlInvocation.arguments().get(0) instanceof StringLiteral) {
            StringLiteral resourceLiteral = (StringLiteral) urlInvocation.arguments().get(0);
            return "Classpath: " + resourceLiteral.getLiteralValue();
          }
        }
      }
    }
    // unknown value
    return null;
  }

  /**
   * @return the final {@link Expression} for given one. This method will traverse
   *         {@link SimpleName}'s until last assignment of "real" {@link Expression} will be found.
   */
  private static Expression getFinalExpression(JavaInfo javaInfo, Expression expression) {
    ExecutionFlowDescription flowDescription =
        JavaInfoUtils.getState(javaInfo).getFlowDescription();
    return ExecutionFlowUtils.getFinalExpression(flowDescription, expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    JavaInfo javaInfo = genericProperty.getJavaInfo();
    AstEditor editor = javaInfo.getEditor();
    IJavaProject javaProject = editor.getJavaProject();
    // create dialog
    ImageDialog imageDialog = new ImageDialog(javaProject);
    // set input for dialog
    {
      String text = getText(property);
      if (text == null) {
        imageDialog.setInput(DefaultImagePage.ID, null);
      } else if (text.equals("(null)")) {
        imageDialog.setInput(NullImagePage.ID, null);
      } else if (text.startsWith("File: ")) {
        String path = text.substring("File: ".length());
        imageDialog.setInput(FileImagePage.ID, path);
      } else if (text.startsWith("Classpath: ")) {
        String path = text.substring("Classpath: ".length());
        imageDialog.setInput(ClasspathImagePage.ID, path);
      }
    }
    // open dialog
    if (imageDialog.open() == Window.OK) {
      ImageInfo imageInfo = imageDialog.getImageInfo();
      // prepare source
      String source = null;
      {
        String pageId = imageInfo.getPageId();
        if (pageId == DefaultImagePage.ID) {
        } else if (pageId == NullImagePage.ID) {
          source = "null";
        } else if (pageId == FileImagePage.ID) {
          String path = (String) imageInfo.getData();
          String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, path);
          source = "new javax.swing.ImageIcon(" + pathSource + ")";
        } else if (pageId == ClasspathImagePage.ID) {
          String path = "/" + imageInfo.getData();
          String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, path);
          source = "new javax.swing.ImageIcon({wbp_classTop}.getResource(" + pathSource + "))";
        }
      }
      // set expression
      genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
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
      addPage(new ClasspathImagePage(parent, SWT.NONE, this, m_javaProject));
      addPage(new FileImagePage(parent, SWT.NONE, this));
      addPage(new NullImagePage(parent, SWT.NONE, this));
      addPage(new DefaultImagePage(parent, SWT.NONE, this));
    }
  }
}
