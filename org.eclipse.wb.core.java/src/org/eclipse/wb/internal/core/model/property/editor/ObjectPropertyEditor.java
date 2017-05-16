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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.model.util.ObjectsTreeContentProvider;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import java.lang.reflect.Method;
import java.util.List;

/**
 * {@link PropertyEditor} for selecting model of {@link Object}, for example in
 * {@link javax.swing.JLabel#setLabelFor(java.awt.Component)}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class ObjectPropertyEditor extends TextDialogPropertyEditor
    implements
      IComplexPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ObjectPropertyEditor();

  private ObjectPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfo getValueComponent(Property property) throws Exception {
    Object value = property.getValue();
    GenericProperty genericProperty = (GenericProperty) property;
    return genericProperty.getJavaInfo().getRootJava().getChildByObject(value);
  }

  @Override
  protected String getText(Property property) throws Exception {
    JavaInfo component = getValueComponent(property);
    if (component != null) {
      return ObjectsLabelProvider.INSTANCE.getText(component);
    }
    // unknown value
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property_) throws Exception {
    GenericProperty property = (GenericProperty) property_;
    // prepare dialog
    ElementTreeSelectionDialog selectionDialog;
    {
      final JavaInfo thisComponent = property.getJavaInfo();
      final Class<?> propertyType = property.getType();
      // providers
      ISelectionStatusValidator validator = new ISelectionStatusValidator() {
        public IStatus validate(Object[] selection) {
          if (selection.length == 1) {
            if (isValidComponent(propertyType, selection[0])) {
              return StatusUtils.OK_STATUS;
            }
          }
          return StatusUtils.ERROR_STATUS;
        }
      };
      ITreeContentProvider contentProvider = createContentProvider(propertyType);
      // create dialog
      selectionDialog =
          new ElementTreeSelectionDialog(DesignerPlugin.getShell(), ObjectsLabelProvider.INSTANCE,
              contentProvider) {
            @Override
            public void create() {
              super.create();
              getTreeViewer().expandAll();
            }
          };
      selectionDialog.setAllowMultiple(false);
      selectionDialog.setTitle(property_.getTitle());
      selectionDialog.setMessage(ModelMessages.ObjectPropertyEditor_chooseMessage);
      selectionDialog.setValidator(validator);
      // set input
      selectionDialog.setInput(new Object[]{thisComponent.getRoot()});
      // set initial selection
      {
        JavaInfo component = getValueComponent(property);
        selectionDialog.setInitialSelection(component);
      }
    }
    // open dialog
    if (selectionDialog.open() == Window.OK) {
      JavaInfo component = (JavaInfo) selectionDialog.getFirstResult();
      setComponent(property, component);
    }
  }

  private ITreeContentProvider createContentProvider(final Class<?> propertyType) {
    final ITreeContentProvider[] contentProvider = new ITreeContentProvider[1];
    contentProvider[0] = new ObjectsTreeContentProvider(new Predicate<ObjectInfo>() {
      public boolean apply(ObjectInfo t) {
        return isValidComponent(propertyType, t) || hasValidComponents(t);
      }

      private boolean hasValidComponents(ObjectInfo t) {
        return contentProvider[0].getChildren(t).length != 0;
      }
    });
    return contentProvider[0];
  }

  private boolean isValidComponent(Class<?> propertyType, Object element) {
    if (element instanceof JavaInfo) {
      JavaInfo component = (JavaInfo) element;
      Class<?> componentClass = component.getDescription().getComponentClass();
      return componentClass != null && propertyType.isAssignableFrom(componentClass);
    }
    return false;
  }

  /**
   * Sets new {@link JavaInfo} value.
   *
   * @param component
   *          new {@link JavaInfo}, or <code>null</code>, if property should be removed.
   */
  public void setComponent(final GenericProperty property, final JavaInfo component)
      throws Exception {
    ExecutionUtils.run(property.getJavaInfo(), new RunnableEx() {
      public void run() throws Exception {
        setComponent0(property, component);
      }
    });
  }

  /**
   * Implementation for {@link #setComponent(GenericProperty, JavaInfo)}.
   */
  private void setComponent0(GenericProperty property, JavaInfo component) throws Exception {
    JavaInfo thisComponent = property.getJavaInfo();
    // remove existing invocation
    property.setValue(Property.UNKNOWN_VALUE);
    // set, if not null
    if (component != null) {
      Method setter = getSetter(property);
      // may be setter
      if (setter != null) {
        StatementTarget target = null;
        // try get special target
        if (thisComponent instanceof IObjectPropertyProcessor) {
          IObjectPropertyProcessor objectPropertyProcessor =
              (IObjectPropertyProcessor) thisComponent;
          target = objectPropertyProcessor.getObjectPropertyStatementTarget(property, component);
        }
        // if no special, use default
        if (target == null) {
          if (component.getVariableSupport() instanceof LazyVariableSupport) {
            // "lazy" component is always accessible
            property.setExpression(TemplateUtils.getExpression(component), component);
            return;
          }
          List<JavaInfo> allComponents = ImmutableList.of(thisComponent, component);
          target = JavaInfoUtils.getStatementTarget_whenAllCreated(allComponents);
        }
        String source =
            TemplateUtils.format("{0}.{1}({2})", thisComponent, setter.getName(), component);
        Expression expression = thisComponent.addExpressionStatement(target, source);
        component.addRelatedNodes(expression);
        return;
      }
      // should be creation
      property.setExpression(TemplateUtils.getExpression(component), component);
    }
  }

  /**
   * @return the setter {@link Method}, or <code>null</code> if given {@link Property} is not based
   *         on known setter.
   */
  private static Method getSetter(Property property) throws Exception {
    if (property instanceof GenericPropertyImpl) {
      GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
      List<ExpressionAccessor> accessors = genericProperty.getAccessors();
      for (ExpressionAccessor accessor : accessors) {
        if (accessor instanceof SetterAccessor) {
          return ((SetterAccessor) accessor).getSetter();
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property[] getProperties(Property property) throws Exception {
    JavaInfo component = getValueComponent(property);
    if (component != null) {
      return component.getProperties();
    }
    return new Property[0];
  }
}
