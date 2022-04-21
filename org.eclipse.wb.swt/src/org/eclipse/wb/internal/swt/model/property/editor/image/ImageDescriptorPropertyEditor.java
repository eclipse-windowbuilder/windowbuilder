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
package org.eclipse.wb.internal.swt.model.property.editor.image;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.ClasspathImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.DefaultImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.FileImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.NullImagePage;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.PluginFileImagePage;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.PluginImagesRoot;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link PropertyEditor} for {link org.eclipse.jface.resource.ImageDescriptor}.
 *
 * @author lobas_av
 * @author scheglov_ke
 * @coverage swt.property.editor
 */
public final class ImageDescriptorPropertyEditor extends TextDialogPropertyEditor
    implements
      IClipboardSourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ImageDescriptorPropertyEditor();

  private ImageDescriptorPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    if (property.getValue() != Property.UNKNOWN_VALUE) {
      Expression expression = ((GenericProperty) property).getExpression();
      // check for "null" expression
      if (expression instanceof NullLiteral) {
        return "(null)";
      }
      // ResourceManager.getImageDescriptor(String path)
      if (AstNodeUtils.isMethodInvocation(
          expression,
          "org.eclipse.jface.resource.ImageDescriptor",
          "createFromFile(java.lang.Class,java.lang.String)")) {
        MethodInvocation invocation = (MethodInvocation) expression;
        Expression locationExpression = DomGenerics.arguments(invocation).get(0);
        Expression pathExpression = DomGenerics.arguments(invocation).get(1);
        Object path = JavaInfoEvaluationHelper.getValue(pathExpression);
        if (locationExpression instanceof NullLiteral) {
          return "File: " + path;
        } else {
          return "Classpath: " + path;
        }
      }
      // ResourceManager.getImageDescriptor(String path)
      if (AstNodeUtils.isMethodInvocation(
          expression,
          "org.eclipse.wb.swt.ResourceManager",
          "getImageDescriptor(java.lang.String)")) {
        MethodInvocation invocation = (MethodInvocation) expression;
        Expression stringExpression = DomGenerics.arguments(invocation).get(0);
        return "File: " + JavaInfoEvaluationHelper.getValue(stringExpression);
      }
      // ResourceManager.getImageDescriptor(Class class, String path)
      if (AstNodeUtils.isMethodInvocation(
          expression,
          "org.eclipse.wb.swt.ResourceManager",
          "getImageDescriptor(java.lang.Class,java.lang.String)")) {
        MethodInvocation invocation = (MethodInvocation) expression;
        Expression stringExpression = DomGenerics.arguments(invocation).get(1);
        return "Classpath: " + JavaInfoEvaluationHelper.getValue(stringExpression);
      }
      // ResourceManager.getPluginImageDescriptorXXX
      String[] imageValue = ImageEvaluator.getPluginImageValue(property);
      if (imageValue != null) {
        return "Plugin: " + imageValue[0] + " " + imageValue[1];
      }
    }
    // unknown value
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getClipboardSource(GenericProperty property) throws Exception {
    if (property.getValue() != Property.UNKNOWN_VALUE) {
      Expression expression = property.getExpression();
      // check for "null" expression
      if (expression instanceof NullLiteral) {
        return "null";
      }
      // check for new image from file/classpath
      {
        JavaInfo javaInfo = property.getJavaInfo();
        String[] imageValue = ImageEvaluator.getPluginImageValue(property);
        if (imageValue == null) {
          IPreferenceStore preferences = javaInfo.getDescription().getToolkit().getPreferences();
          boolean useResourceManager =
              preferences.getBoolean(IPreferenceConstants.P_USE_RESOURCE_MANAGER);
          String text = getText(property);
          if (text.startsWith("File: ")) {
            String path = text.substring("File: ".length());
            String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, path);
            if (useResourceManager) {
              return "org.eclipse.wb.swt.ResourceManager.getImageDescriptor(" + pathSource + ")";
            }
            return "org.eclipse.jface.resource.ImageDescriptor.createFromFile(null, "
                + pathSource
                + ")";
          }
          if (text.startsWith("Classpath: ")) {
            String path = text.substring("Classpath: ".length());
            String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, path);
            if (useResourceManager) {
              return "org.eclipse.wb.swt.ResourceManager.getImageDescriptor({wbp_classTop}, "
                  + pathSource
                  + ")";
            }
            return "org.eclipse.jface.resource.ImageDescriptor.createFromFile({wbp_classTop}, "
                + pathSource
                + ")";
          }
        } else {
          String symbolicName = StringConverter.INSTANCE.toJavaSource(javaInfo, imageValue[0]);
          String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, imageValue[1]);
          //
          return "org.eclipse.wb.swt.ResourceManager.getPluginImageDescriptor("
              + symbolicName
              + ", "
              + pathSource
              + ")";
        }
      }
    }
    // unknown image pattern
    return null;
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
    IJavaProject javaProject = javaInfo.getEditor().getJavaProject();
    // create dialog
    ImageDialog imageDialog = new ImageDialog(javaProject);
    // set input for dialog
    {
      String[] imageValue = ImageEvaluator.getPluginImageValue(property);
      if (imageValue == null) {
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
      } else {
        imageDialog.setInput(PluginFileImagePage.ID, imageValue);
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
        } else {
          IPreferenceStore preferences = javaInfo.getDescription().getToolkit().getPreferences();
          boolean useResourceManager =
              preferences.getBoolean(IPreferenceConstants.P_USE_RESOURCE_MANAGER);
          if (useResourceManager) {
            ManagerUtils.ensure_ResourceManager(javaInfo);
          }
          //
          if (pageId == FileImagePage.ID) {
            String path = (String) imageInfo.getData();
            String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, path);
            if (useResourceManager) {
              source = "org.eclipse.wb.swt.ResourceManager.getImageDescriptor(" + pathSource + ")";
            } else {
              source =
                  "org.eclipse.jface.resource.ImageDescriptor.createFromFile(null, "
                      + pathSource
                      + ")";
            }
          } else if (pageId == ClasspathImagePage.ID) {
            String path = "/" + imageInfo.getData();
            String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, path);
            if (useResourceManager) {
              source =
                  "org.eclipse.wb.swt.ResourceManager.getImageDescriptor({wbp_classTop}, "
                      + pathSource
                      + ")";
            } else {
              source =
                  "org.eclipse.jface.resource.ImageDescriptor.createFromFile(null, "
                      + pathSource
                      + ")";
            }
          } else if (pageId == PluginFileImagePage.ID) {
            ManagerUtils.ensure_ResourceManager(javaInfo);
            //
            String[] data = (String[]) imageInfo.getData();
            String symbolicName = StringConverter.INSTANCE.toJavaSource(javaInfo, data[0]);
            String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, data[1]);
            //
            source =
                "org.eclipse.wb.swt.ResourceManager.getPluginImageDescriptor("
                    + symbolicName
                    + ", "
                    + pathSource
                    + ")";
          }
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
      if (PluginImagesRoot.testPluginProject(m_javaProject.getProject())) {
        addPage(PluginFileImagePage.createPage(parent, SWT.NONE, this, m_javaProject.getProject()));
      }
    }
  }
}