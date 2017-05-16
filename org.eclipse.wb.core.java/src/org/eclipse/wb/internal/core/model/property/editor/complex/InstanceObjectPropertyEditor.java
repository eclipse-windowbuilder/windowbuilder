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
package org.eclipse.wb.internal.core.model.property.editor.complex;

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * {@link PropertyEditor} for editing class instance as complex property (like Layouts).
 *
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public final class InstanceObjectPropertyEditor extends TextDialogPropertyEditor
    implements
      IComplexPropertyEditor,
      IConfigurablePropertyObject {
  private static final String INSTANCE_JAVA_INFO_KEY = "Instance JavaInfo";
  private String m_className;
  private String m_sourceNewClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    // by Instance
    JavaInfo instanceInfo = getInstanceInfo(property);
    if (instanceInfo != null) {
      return instanceInfo.getDescription().getComponentClass().getName();
    }
    // by Expression
    Expression expression = getInstanceExpression(property);
    if (expression != null) {
      if (expression instanceof ClassInstanceCreation) {
        ClassInstanceCreation creation = (ClassInstanceCreation) expression;
        if (creation.getAnonymousClassDeclaration() != null) {
          return "<anonymous>";
        }
        return AstNodeUtils.getFullyQualifiedName(expression, false);
      }
      return "<unknown>";
    }
    // by Value
    Object value = property.getValue();
    if (value != null && value != Property.UNKNOWN_VALUE) {
      return value.getClass().getName();
    }
    return null;
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
  public void deactivate(PropertyTable propertyTable, Property property, boolean save) {
    super.deactivate(propertyTable, property, save);
  }

  @Override
  public void doubleClick(Property property, Point location) throws Exception {
    if (!StringUtils.isEmpty(m_sourceNewClass)) {
      openClass(property);
    }
  }

  @Override
  protected void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    // prepare scope
    IJavaSearchScope scope;
    {
      IJavaProject project = genericProperty.getJavaInfo().getEditor().getJavaProject();
      IType classType = project.findType(m_className);
      scope = SearchEngine.createHierarchyScope(classType);
    }
    // prepare dialog
    SelectionDialog dialog;
    {
      Shell shell = DesignerPlugin.getShell();
      ProgressMonitorDialog context = new ProgressMonitorDialog(shell);
      dialog =
          JavaUI.createTypeDialog(
              shell,
              context,
              scope,
              IJavaElementSearchConstants.CONSIDER_CLASSES,
              false);
      dialog.setTitle(ModelMessages.InstanceObjectPropertyEditor_chooseTitle);
      dialog.setMessage(ModelMessages.InstanceObjectPropertyEditor_chooseMessage);
    }
    // open dialog
    if (dialog.open() == Window.OK) {
      IType instanceType = (IType) dialog.getResult()[0];
      String instanceTypeName = instanceType.getFullyQualifiedName();//.replace('$', '.');
      ComponentDescription instanceComponentDescription =
          ComponentDescriptionHelper.getDescription(
              genericProperty.getJavaInfo().getEditor(),
              instanceTypeName);
      CreationDescription creation = instanceComponentDescription.getCreation(null);
      setValueSource(genericProperty, creation.getSource().replace('$', '.'));
    }
  }

  /**
   * If there is class, open it, else create new anonymous class and open.
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
      generateNewClass(genericProperty);
      openClass(property);
    }
  }

  /**
   * Uses anonymous class as {@link GenericProperty} expression.
   */
  private void generateNewClass(GenericProperty property) throws Exception {
    setValueSource(property, m_sourceNewClass);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IComplexPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property[] getProperties(Property property) throws Exception {
    JavaInfo instanceInfo = getInstanceInfo(property);
    if (instanceInfo != null) {
      List<Property> propertyList =
          PropertyUtils.getProperties_excludeByParameter(
              instanceInfo,
              "instanceProperty.exclude-properties");
      Property[] properties = propertyList.toArray(new Property[propertyList.size()]);
      return properties;
    }
    return new Property[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    // class
    {
      m_className = (String) parameters.get("class");
      Assert.isNotNull(m_className, "'class' attribute required.");
    }
    // source
    {
      m_sourceNewClass = (String) parameters.get("source");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Expression getInstanceExpression(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    return genericProperty.getExpression();
  }

  public JavaInfo getInstanceInfo(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    Expression expression = genericProperty.getExpression();
    if (expression != null) {
      JavaInfo instanceInfo = genericProperty.getJavaInfo().getChildRepresentedBy(expression);
      property.putArbitraryValue(INSTANCE_JAVA_INFO_KEY, instanceInfo);
      return instanceInfo;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Processing set new source for {@link GenericProperty} expression.
   */
  private void setValueSource(final GenericProperty property, final String source) throws Exception {
    final JavaInfo javaInfo = property.getJavaInfo();
    ExecutionUtils.run(javaInfo, new RunnableEx() {
      public void run() throws Exception {
        // remove old instance info
        {
          JavaInfo oldInstanceInfo = getInstanceInfo(property);
          if (oldInstanceInfo != null) {
            oldInstanceInfo.delete();
            property.removeArbitraryValue(INSTANCE_JAVA_INFO_KEY);
          }
        }
        // evaluate new expression
        String evaluateSource =
            TemplateUtils.evaluate(source, javaInfo, ImmutableMap.<String, String>of());
        property.setExpression(evaluateSource, Property.UNKNOWN_VALUE);
        // create new instance info
        {
          Expression expression = getInstanceExpression(property);
          Assert.isNotNull(expression, "setting expression failed.");
          if (expression instanceof ClassInstanceCreation) {
            ClassInstanceCreation creation = (ClassInstanceCreation) expression;
            if (creation.getAnonymousClassDeclaration() == null) {
              JavaInfo newInstanceInfo =
                  JavaInfoUtils.createJavaInfo(
                      javaInfo.getEditor(),
                      AstNodeUtils.getFullyQualifiedName(expression, true),
                      new ConstructorCreationSupport(creation));
              newInstanceInfo.bindToExpression(expression);
              newInstanceInfo.setVariableSupport(new EmptyVariableSupport(newInstanceInfo, creation));
              newInstanceInfo.setAssociation(new InvocationChildAssociation((MethodInvocation) creation.getParent()));
              javaInfo.addChild(newInstanceInfo);
            }
          }
        }
      }
    });
  }

  /**
   * Installing listeners for correct processing "Reset to default" action.
   */
  public static void installListenerForProperty(final JavaInfo instanceInfo) {
    instanceInfo.addBroadcastListener(new JavaEventListener() {
      final JavaInfo m_instanceInfo = instanceInfo;

      @Override
      public void propertyValueWasSet(GenericPropertyImpl property) throws Exception {
        if (property.getEditor() instanceof InstanceObjectPropertyEditor
            && m_instanceInfo == property.getArbitraryValue(INSTANCE_JAVA_INFO_KEY)) {
          InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
          if (editor.getInstanceInfo(property) != m_instanceInfo) {
            m_instanceInfo.delete();
            property.removeArbitraryValue(INSTANCE_JAVA_INFO_KEY);
          }
        }
      }
    });
  }
}
