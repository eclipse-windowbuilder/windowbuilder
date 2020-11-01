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
package org.eclipse.wb.internal.swing.databinding.model.components;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.ISynchronizeProcessor;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.model.presentation.JavaInfoObservePresentation;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.variable.ExposedFieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;

import org.eclipse.jdt.core.dom.Expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link ObserveInfo} model for {@code Swing} components.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.components
 */
public class ComponentObserveInfo extends ObserveInfo {
  private final BeanSupport m_beanSupport;
  private final ObserveInfo m_parent;
  private JavaInfo m_javaInfo;
  private List<ComponentObserveInfo> m_children;
  private List<ObserveInfo> m_properties;
  private final JavaInfoObservePresentation m_presentation;
  private ObserveCreationType m_creationType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentObserveInfo(BeanSupport beanSupport, JavaInfo javaInfo) throws Exception {
    this(beanSupport, null, javaInfo);
  }

  public ComponentObserveInfo(BeanSupport beanSupport, ObserveInfo parent, JavaInfo javaInfo)
      throws Exception {
    super(new ClassGenericType(javaInfo.getDescription().getComponentClass(), null, null),
        new JavaInfoReferenceProvider(javaInfo));
    m_beanSupport = beanSupport;
    m_parent = parent;
    m_javaInfo = javaInfo;
    m_presentation = new JavaInfoObservePresentation(m_javaInfo);
    m_creationType = ComponentsObserveTypeContainer.getCreationType(getObjectClass());
    if (JavaInfoReferenceProvider.getReference(m_javaInfo) == null) {
      m_properties = Collections.emptyList();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  public final ObserveType getType() {
    return ObserveType.WIDGETS;
  }

  @Override
  public ObserveCreationType getCreationType() {
    return m_creationType;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private void setJavaInfo(JavaInfo javaInfo) throws Exception {
    // update info
    m_javaInfo = javaInfo;
    // update type
    setObjectType(new ClassGenericType(javaInfo.getDescription().getComponentClass(), null, null));
    // update reference
    JavaInfoReferenceProvider referenceProvider =
        (JavaInfoReferenceProvider) getReferenceProvider();
    referenceProvider.setJavaInfo(m_javaInfo);
    // update presentation
    m_presentation.setJavaInfo(m_javaInfo);
    // update creation type
    m_creationType = ComponentsObserveTypeContainer.getCreationType(getObjectClass());
    // update properties
    if (JavaInfoReferenceProvider.getReference(m_javaInfo) == null) {
      m_properties = Collections.emptyList();
    } else {
      m_properties = null;
    }
  }

  /**
   * Update (reorder, add, remove) children {@link ComponentObserveInfo}.
   */
  public void update() throws Exception {
    // prepare new javaInfo's
    List<JavaInfo> javaInfos = SynchronizeManager.getChildren(m_javaInfo, JavaInfo.class);
    //
    getChildren(ChildrenContext.ChildrenForMasterTable);
    //
    SynchronizeManager.synchronizeObjects(
        m_children,
        javaInfos,
        new ISynchronizeProcessor<JavaInfo, ComponentObserveInfo>() {
          public boolean handleObject(ComponentObserveInfo object) {
            return true;
          }

          public JavaInfo getKeyObject(ComponentObserveInfo component) {
            return component.m_javaInfo;
          }

          public boolean equals(JavaInfo key0, JavaInfo key1) {
            return key0 == key1;
          }

          public ComponentObserveInfo findObject(
              Map<JavaInfo, ComponentObserveInfo> javaInfoToComponent,
              JavaInfo javaInfo) throws Exception {
            VariableSupport variableSupport = javaInfo.getVariableSupport();
            for (Map.Entry<JavaInfo, ComponentObserveInfo> entry : javaInfoToComponent.entrySet()) {
              if (entry.getKey().getVariableSupport() == variableSupport) {
                ComponentObserveInfo component = entry.getValue();
                component.setJavaInfo(javaInfo);
                return component;
              }
            }
            return null;
          }

          public ComponentObserveInfo createObject(JavaInfo javaInfo) throws Exception {
            return new ComponentObserveInfo(m_beanSupport, ComponentObserveInfo.this, javaInfo);
          }

          public void update(ComponentObserveInfo component) throws Exception {
            component.update();
          }
        });
  }

  @Override
  public void createBinding(BindingInfo binding) throws Exception {
    super.createBinding(binding);
    // ensure convert local variable to field
    ensureConvertToField();
  }

  protected void ensureConvertToField() throws Exception {
    VariableSupport variableSupport = m_javaInfo.getVariableSupport();
    if (variableSupport.canConvertLocalToField()) {
      variableSupport.convertLocalToField();
    }
    if (variableSupport instanceof ExposedPropertyVariableSupport
        || variableSupport instanceof ExposedFieldVariableSupport) {
      ComponentObserveInfo parent = (ComponentObserveInfo) m_parent;
      parent.ensureConvertToField();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public final IObserveInfo getParent() {
    return m_parent;
  }

  public final List<IObserveInfo> getChildren(ChildrenContext context) {
    if (context == ChildrenContext.ChildrenForMasterTable) {
      if (m_children == null) {
        try {
          m_children = new ArrayList<>();
          List<JavaInfo> childrenInfos = SynchronizeManager.getChildren(m_javaInfo, JavaInfo.class);
          for (JavaInfo childInfo : childrenInfos) {
            m_children.add(new ComponentObserveInfo(m_beanSupport, this, childInfo));
          }
        } catch (Throwable e) {
          DesignerPlugin.log(e);
          m_children = Collections.emptyList();
        }
      }
      return CoreUtils.cast(m_children);
    }
    if (context == ChildrenContext.ChildrenForPropertiesTable) {
      if (m_properties == null) {
        m_properties = m_beanSupport.createProperties(this, getObjectType());
      }
      return CoreUtils.cast(m_properties);
    }
    return Collections.emptyList();
  }

  public ComponentObserveInfo resolve(Expression expression) throws Exception {
    if (AstNodeUtils.isVariable(expression)) {
      if (AstNodeUtils.getVariableName(expression).equals(
          JavaInfoReferenceProvider.getReference(m_javaInfo))) {
        return this;
      }
    } else if (m_javaInfo.isRepresentedBy(expression)) {
      return this;
    }
    getChildren(ChildrenContext.ChildrenForMasterTable);
    for (ComponentObserveInfo child : m_children) {
      ComponentObserveInfo resultInfo = child.resolve(expression);
      if (resultInfo != null) {
        return resultInfo;
      }
    }
    return null;
  }

  public ComponentObserveInfo resolve(JavaInfo javaInfo) throws Exception {
    if (m_javaInfo == javaInfo) {
      return this;
    }
    getChildren(ChildrenContext.ChildrenForMasterTable);
    for (ComponentObserveInfo child : m_children) {
      ComponentObserveInfo resultInfo = child.resolve(javaInfo);
      if (resultInfo != null) {
        return resultInfo;
      }
    }
    return null;
  }

  public ComponentObserveInfo resolve(String reference) throws Exception {
    if (reference.equals(getReference())) {
      return this;
    }
    getChildren(ChildrenContext.ChildrenForMasterTable);
    for (ComponentObserveInfo child : m_children) {
      ComponentObserveInfo resultInfo = child.resolve(reference);
      if (resultInfo != null) {
        return resultInfo;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObservePresentation getPresentation() {
    return m_presentation;
  }
}