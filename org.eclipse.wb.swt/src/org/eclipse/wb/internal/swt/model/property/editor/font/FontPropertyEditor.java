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
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.swt.model.jface.resource.KeyFieldInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.RegistryContainerInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.ResourceRegistryInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.support.DisplaySupport;
import org.eclipse.wb.internal.swt.support.FontSupport;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;

import java.util.List;

/**
 * {@link PropertyEditor} for {@link org.eclipse.swt.graphic.Font}.
 * 
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class FontPropertyEditor extends TextDialogPropertyEditor
    implements
      IClipboardSourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new FontPropertyEditor();

  private FontPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value != Property.UNKNOWN_VALUE) {
      Expression expression = ((GenericProperty) property).getExpression();
      if (expression instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) expression;
        // JFaceResource.getXXXFont()
        if (isJFaceValue(invocation)) {
          return invocation.getName().getIdentifier() + "()";
        }
        // FontRegistry.get(String key)
        if (isFontRegistryInvocation(invocation)) {
          return getTextForRegistry(property, invocation);
        }
      }
      // default font
      if (value == null) {
        value = DisplaySupport.getSystemFont();
      }
      // use font.toString()
      return getText(value);
    }
    return null;
  }

  private static String getText(Object font) throws Exception {
    Object fontData = FontSupport.getFontData(font);
    StringBuilder buffer = new StringBuilder();
    buffer.append(FontSupport.getFontName(fontData));
    buffer.append(" ");
    buffer.append(FontSupport.getFontSize(fontData));
    {
      String styleText = FontSupport.getFontStyleText(fontData);
      if (styleText.length() != 0) {
        buffer.append(" ");
        buffer.append(styleText);
      }
    }
    return buffer.toString();
  }

  /**
   * @return <code>true</code> if given {@link MethodInvocation} represented method
   *         <code>JFaceResources.getXXXFont()</code>.
   */
  private static boolean isJFaceValue(MethodInvocation invocation) {
    String identifier = invocation.getName().getIdentifier();
    return identifier.startsWith("get")
        && identifier.endsWith("Font")
        && AstNodeUtils.isSuccessorOf(
            invocation.getExpression(),
            "org.eclipse.jface.resource.JFaceResources") && invocation.arguments().isEmpty();
  }

  /**
   * @return <code>true</code> if given {@link Expression} represented method
   *         <code>JFaceResources.getXXXFont()</code>.
   */
  private static boolean isJFaceValue(Expression expression) {
    if (expression instanceof MethodInvocation) {
      return isJFaceValue((MethodInvocation) expression);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    // FondData based FontInfo
    FontInfo fontInfo;
    {
      Object value = property.getValue();
      if (value == Property.UNKNOWN_VALUE) {
        return null;
      }
      fontInfo = new FontInfo(null, value, null, false);
    }
    // JFaceResource.getXXXFont()
    {
      Expression expression = property.getExpression();
      // JFaceResource.getXXXFont()
      if (expression instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) expression;
        String identifier = invocation.getName().getIdentifier();
        if (identifier.startsWith("get")
            && identifier.endsWith("Font")
            && AstNodeUtils.getFullyQualifiedName(invocation.getExpression(), false).equals(
                "org.eclipse.jface.resource.JFaceResources")
            && invocation.arguments().isEmpty()) {
          fontInfo =
              new FontInfo(null, null, "org.eclipse.jface.resource.JFaceResources."
                  + identifier
                  + "()", false);
        }
      }
    }
    // convert FontInfo into source
    return getSource(property, fontInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    FontDialog fontDialog =
        new FontDialog(DesignerPlugin.getShell(), genericProperty.getJavaInfo());
    // set initial value
    {
      Object value = property.getValue();
      if (value != Property.UNKNOWN_VALUE) {
        if (value == null) {
          value = DisplaySupport.getSystemFont();
        }
        // prepare font value
        Object[] registryValue = getRegistryValue(property);
        FontInfo fontInfo;
        if (registryValue == null) {
          fontInfo = new FontInfo(null, value, null, false);
          if (isJFaceValue(genericProperty.getExpression())) {
            fontInfo.setPageId(JFaceFontPage.NAME);
          }
        } else {
          fontInfo = new FontInfo(null, value, (String) registryValue[3], false);
          fontInfo.setData(registryValue);
          fontInfo.setPageId(RegistryFontPage.NAME);
        }
        fontDialog.setFontInfo(fontInfo);
      }
    }
    // open dialog
    if (fontDialog.open() == Window.OK) {
      FontInfo fontInfo = fontDialog.getFontInfo();
      // prepare source
      String source = getSource(genericProperty, fontInfo);
      // set expression
      genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
    }
    fontDialog.disposeFont();
  }

  /**
   * @return the source for given {@link FontInfo}.
   */
  private static String getSource(GenericProperty property, FontInfo fontInfo) throws Exception {
    String source;
    if (fontInfo.getSourceCode() != null) {
      source = fontInfo.getSourceCode();
    } else {
      Object fontData = FontSupport.getFontData(fontInfo.getFont());
      // prepare prefix
      String prefix;
      {
        IPreferenceStore preferences =
            property.getJavaInfo().getDescription().getToolkit().getPreferences();
        if (preferences.getBoolean(IPreferenceConstants.P_USE_RESOURCE_MANAGER)) {
          ManagerUtils.ensure_SWTResourceManager(property.getJavaInfo());
          prefix = "org.eclipse.wb.swt.SWTResourceManager.getFont(";
        } else {
          prefix = "new org.eclipse.swt.graphics.Font(null, ";
        }
      }
      // prepare source
      source =
          prefix
              + "\""
              + FontSupport.getFontName(fontData)
              + "\", "
              + FontSupport.getFontSize(fontData)
              + ", "
              + FontSupport.getFontStyleSource(fontData)
              + ")";
    }
    return source;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FontRegistry
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isFontRegistryInvocation(MethodInvocation invocation) {
    return AstNodeUtils.isMethodInvocation(
        invocation,
        "org.eclipse.jface.resource.FontRegistry",
        new String[]{
            "get(java.lang.String)",
            "getBold(java.lang.String)",
            "getItalic(java.lang.String)"});
  }

  private static String getTextForRegistry(Property property, MethodInvocation invocation)
      throws Exception {
    Object keyArgument = invocation.arguments().get(0);
    if (keyArgument instanceof QualifiedName) {
      // prepare title for registry
      String registryTitle;
      {
        GenericProperty genericProperty = (GenericProperty) property;
        ResourceRegistryInfo registry =
            RegistryContainerInfo.getRegistry(
                genericProperty.getJavaInfo().getRootJava(),
                invocation.getExpression());
        registryTitle = registry.getVariableSupport().getTitle();
      }
      // prepare key
      String key;
      {
        QualifiedName keyQualifiedName = (QualifiedName) keyArgument;
        key = keyQualifiedName.getName().getIdentifier();
      }
      // prepare style prefix
      String prefix = "";
      {
        String signature = AstNodeUtils.getMethodSignature(invocation);
        if (signature.startsWith("getBold")) {
          prefix = "(b)";
        } else if (signature.startsWith("getItalic")) {
          prefix = "(i)";
        }
      }
      //
      return registryTitle + " - " + key + prefix;
    }
    return null;
  }

  /**
   * @return <code>[{@link ResourceRegistryInfo}, {@link KeyFieldInfo}, {none, bold, italic}, source]</code>
   *         if property value sets as "JFace Font Registry" otherwise <code>null</code>.
   */
  private static Object[] getRegistryValue(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    Expression expression = genericProperty.getExpression();
    //
    if (expression instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) expression;
      String signature = AstNodeUtils.getMethodSignature(invocation);
      if (isFontRegistryInvocation(invocation)) {
        Object keyArgument = invocation.arguments().get(0);
        if (keyArgument instanceof QualifiedName) {
          ResourceRegistryInfo registryInfo =
              RegistryContainerInfo.getRegistry(
                  genericProperty.getJavaInfo().getRootJava(),
                  invocation.getExpression());
          // prepare key
          QualifiedName keyQualifiedName = (QualifiedName) keyArgument;
          String keyName = keyQualifiedName.getName().getIdentifier();
          KeyFieldInfo keyFieldInfo = null;
          List<KeyFieldInfo> keyFields = registryInfo.getKeyFields();
          for (KeyFieldInfo info : keyFields) {
            if (keyName.equals(info.keyName)) {
              keyFieldInfo = info;
              break;
            }
          }
          if (keyFieldInfo == null) {
            return null;
          }
          // prepare method type
          int selectionIndex = RegistryFontPage.FONT_STYLE_NONE;
          String methodName = "get";
          if (signature.startsWith("getBold")) {
            selectionIndex = RegistryFontPage.FONT_STYLE_BOLD;
            methodName += "Bold";
          } else if (signature.startsWith("getItalic")) {
            selectionIndex = RegistryFontPage.FONT_STYLE_ITALIC;
            methodName += "Italic";
          }
          // prepare source
          String source =
              TemplateUtils.format(
                  "{0}.{1}({2}.{3})",
                  registryInfo,
                  methodName,
                  registryInfo.getDescription().getComponentClass().getName(),
                  keyQualifiedName.getName().getIdentifier());
          //
          return new Object[]{registryInfo, keyFieldInfo, selectionIndex, source};
        }
      }
    }
    return null;
  }
}