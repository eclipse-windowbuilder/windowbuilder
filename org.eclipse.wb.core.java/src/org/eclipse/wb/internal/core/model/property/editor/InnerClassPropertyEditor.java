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
package org.eclipse.wb.internal.core.model.property.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@link PropertyEditor} that generates/uses instance of anonymous/inner {@link Class} or uses
 * instance of selected external {@link Class}. Source for class generated class is configured as
 * template.
 *
 * <pre><xmp>
	<property id="setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)">
		<editor id="innerClass">
			<parameter name="mode">inner</parameter>
			<parameter name="name">TableLabelProvider</parameter>
			<parameter name="class">org.eclipse.jface.viewers.ITableLabelProvider</parameter>
			<parameter name="source"><![CDATA[
private class ${name} extends org.eclipse.jface.viewers.LabelProvider implements org.eclipse.jface.viewers.ITableLabelProvider {
	public org.eclipse.swt.graphics.Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	public String getColumnText(Object element, int columnIndex) {
		return element.toString();
	}
}
			]]></parameter>
		</editor>
	</property>
 * </xmp></pre>
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class InnerClassPropertyEditor extends TextDialogPropertyEditor
    implements
      IConfigurablePropertyObject {
  private static enum Mode {
    INNER, ANONYMOUS
  }

  private Mode m_mode;
  private String m_baseName;
  private String m_className;
  private String m_source;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    Expression expression = genericProperty.getExpression();
    if (expression instanceof ClassInstanceCreation) {
      ClassInstanceCreation cic = (ClassInstanceCreation) expression;
      if (cic.getAnonymousClassDeclaration() != null) {
        return "<anonymous>";
      }
      return AstNodeUtils.getFullyQualifiedName(expression, false);
    }
    // no class
    return "<double click>";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean activate(PropertyTable propertyTable, Property property, Point location)
      throws Exception {
    // activate using keyboard
    if (location == null) {
      openClass(property);
    }
    // don't activate
    return false;
  }

  @Override
  protected void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    JavaInfo javaInfo = genericProperty.getJavaInfo();
    // prepare scope
    Shell shell = DesignerPlugin.getShell();
    IJavaProject javaProject = javaInfo.getEditor().getJavaProject();
    // open dialog
    IType type = JdtUiUtils.selectType(shell, javaProject);
    if (type != null) {
      if (Flags.isAbstract(type.getFlags())) {
        UiUtils.openError(
            shell,
            ModelMessages.InnerClassPropertyEditor_selectTypeAbstractTitle,
            ModelMessages.InnerClassPropertyEditor_selectTypeAbstractMessage);
        return;
      }
      String source = getCreationSource(javaInfo, type);
      genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
    }
  }

  /**
   * @return the source to use to create {@link Class} instance. Attempt to use shortest
   *         constructor.
   */
  private static String getCreationSource(JavaInfo javaInfo, IType type) {
    String sourceTypeName = type.getFullyQualifiedName().replace('$', '.');
    try {
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(javaInfo);
      Class<?> componentClass = classLoader.loadClass(sourceTypeName);
      Constructor<?> constructor = ReflectionUtils.getShortestConstructor(componentClass);
      return ComponentDescriptionHelper.getDefaultConstructorInvocation(constructor);
    } catch (Throwable e) {
      return "new " + sourceTypeName + "()";
    }
  }

  @Override
  public void doubleClick(Property property, Point location) throws Exception {
    openClass(property);
  }

  /**
   * If there is class, open it, else create new inner class and open.
   */
  private void openClass(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    Expression expression = genericProperty.getExpression();
    if (expression != null) {
      IDesignPageSite site = IDesignPageSite.Helper.getSite(genericProperty.getJavaInfo());
      if (site != null) {
        site.openSourcePosition(expression.getStartPosition());
      }
    } else {
      // generate new class
      switch (m_mode) {
        case ANONYMOUS :
          newClass_ANONYMOUS(genericProperty);
          break;
        case INNER :
          newClass_INNER(genericProperty);
          break;
      }
      openClass(property);
    }
  }

  /**
   * Uses anonymous class as {@link GenericProperty} expression.
   */
  private void newClass_ANONYMOUS(GenericProperty genericProperty) throws Exception {
    JavaInfo javaInfo = genericProperty.getJavaInfo();
    String source = TemplateUtils.evaluate(m_source, javaInfo, ImmutableMap.<String, String>of());
    genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
  }

  /**
   * Adds new inner class.
   */
  private void newClass_INNER(GenericProperty genericProperty) throws Exception {
    JavaInfo javaInfo = genericProperty.getJavaInfo();
    AstEditor editor = javaInfo.getEditor();
    // prepare name of type and source lines
    String newName;
    List<String> newLines;
    {
      newName = editor.getUniqueTypeName(m_baseName);
      String newSource =
          TemplateUtils.evaluate(m_source, javaInfo, ImmutableMap.of("name", newName));
      newLines = ImmutableList.copyOf(StringUtils.split(newSource, "\r\n"));
    }
    // add type
    {
      TypeDeclaration targetType = JavaInfoUtils.getTypeDeclaration(javaInfo);
      editor.addTypeDeclaration(newLines, new BodyDeclarationTarget(targetType, true));
    }
    // set value
    genericProperty.setExpression("new " + newName + "()", Property.UNKNOWN_VALUE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    // mode
    {
      String modeText = (String) parameters.get("mode");
      Assert.isNotNull(modeText, "'mode' attribute required.");
      modeText = modeText.toUpperCase(Locale.ENGLISH);
      m_mode = Mode.valueOf(modeText);
      Assert.isNotNull(
          m_mode,
          "Invalid value for 'mode' attribute. Only 'anonymous' or 'inner' supported.");
    }
    // base class name
    if (m_mode != Mode.ANONYMOUS) {
      m_baseName = (String) parameters.get("name");
      Assert.isNotNull(m_baseName, "'name' attribute required.");
    }
    // class
    {
      m_className = (String) parameters.get("class");
      Assert.isNotNull(m_className, "'class' attribute required.");
    }
    // template source
    {
      m_source = (String) parameters.get("source");
      Assert.isNotNull(m_source, "'source' attribute required.");
      m_source = m_source.trim();
    }
  }
}
