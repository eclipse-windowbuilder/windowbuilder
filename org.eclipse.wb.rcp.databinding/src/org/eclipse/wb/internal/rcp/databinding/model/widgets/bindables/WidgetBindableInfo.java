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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.ISynchronizeProcessor;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.variable.ExposedFieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.WrapperMethodControlVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link BindableInfo} model for <code>SWT</code> widget.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class WidgetBindableInfo extends BindableInfo {
  private JavaInfo m_javaInfo;
  private final WidgetBindableInfo m_parent;
  private final List<WidgetBindableInfo> m_children = Lists.newArrayList();
  private List<WidgetPropertyBindableInfo> m_properties;
  private final IObservePresentation m_presentation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetBindableInfo(JavaInfo javaInfo, DatabindingsProvider provider) throws Exception {
    this(javaInfo, null, provider);
  }

  public WidgetBindableInfo(JavaInfo javaInfo,
      WidgetBindableInfo parent,
      DatabindingsProvider provider) throws Exception {
    super(javaInfo.getDescription().getComponentClass(), new JavaInfoReferenceProvider(javaInfo,
        provider));
    m_javaInfo = javaInfo;
    m_parent = parent;
    m_presentation = new JavaInfoObservePresentation(javaInfo, getReferenceProvider());
    // prepare children
    List<JavaInfo> childrenInfos = SynchronizeManager.getChildren(m_javaInfo, JavaInfo.class);
    for (JavaInfo childInfo : childrenInfos) {
      m_children.add(new WidgetBindableInfo(childInfo, this, provider));
    }
    // prepare properties
    if (getReference() == null) {
      m_properties = Collections.emptyList();
    } else {
      m_properties = PropertiesSupport.getProperties(getClassLoader(), getObjectType());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createBinding(AbstractBindingInfo binding) throws Exception {
    super.createBinding(binding);
    // ensure convert local variable to field
    ensureConvertToField();
  }

  public void ensureConvertToField() throws Exception {
    VariableSupport variableSupport = m_javaInfo.getVariableSupport();
    if (variableSupport.canConvertLocalToField()) {
      variableSupport.convertLocalToField();
    }
    if (variableSupport instanceof ExposedPropertyVariableSupport
        || variableSupport instanceof ExposedFieldVariableSupport) {
      WidgetBindableInfo parent = m_parent;
      if (m_javaInfo instanceof ViewerInfo
          && m_javaInfo.getParentJava().getVariableSupport() instanceof WrapperMethodControlVariableSupport) {
        parent = m_parent.m_parent;
      }
      parent.ensureConvertToField();
    }
    JavaInfoReferenceProvider referenceProvider =
        (JavaInfoReferenceProvider) getReferenceProvider();
    referenceProvider.ensureControllerReference();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfo getJavaInfo() {
    return m_javaInfo;
  }

  private void setJavaInfo(JavaInfo javaInfo) throws Exception {
    // update info
    m_javaInfo = javaInfo;
    // update type
    setObjectType(javaInfo.getDescription().getComponentClass());
    // update reference
    JavaInfoReferenceProvider referenceProvider =
        (JavaInfoReferenceProvider) getReferenceProvider();
    referenceProvider.setJavaInfo(javaInfo);
    // update presentation
    JavaInfoObservePresentation presentation = (JavaInfoObservePresentation) m_presentation;
    presentation.setJavaInfo(javaInfo);
    // update properties
    if (getReference() == null) {
      m_properties = Collections.emptyList();
    } else {
      m_properties = PropertiesSupport.getProperties(getClassLoader(), getObjectType());
    }
  }

  /**
   * Update (reorder, add, remove) children {@link WidgetBindableInfo}.
   */
  public void update(final DatabindingsProvider provider) throws Exception {
    // prepare new javaInfo's
    List<JavaInfo> javaInfos = SynchronizeManager.getChildren(m_javaInfo, JavaInfo.class);
    //
    SynchronizeManager.synchronizeObjects(
        m_children,
        javaInfos,
        new ISynchronizeProcessor<JavaInfo, WidgetBindableInfo>() {
          public boolean handleObject(WidgetBindableInfo object) {
            return true;
          }

          public JavaInfo getKeyObject(WidgetBindableInfo widget) {
            return widget.m_javaInfo;
          }

          public boolean equals(JavaInfo key0, JavaInfo key1) {
            return key0 == key1;
          }

          public WidgetBindableInfo findObject(Map<JavaInfo, WidgetBindableInfo> javaInfoToWidget,
              JavaInfo javaInfo) throws Exception {
            VariableSupport variableSupport = javaInfo.getVariableSupport();
            for (Map.Entry<JavaInfo, WidgetBindableInfo> entry : javaInfoToWidget.entrySet()) {
              if (entry.getKey().getVariableSupport() == variableSupport) {
                WidgetBindableInfo widget = entry.getValue();
                widget.setJavaInfo(javaInfo);
                return widget;
              }
            }
            return null;
          }

          public WidgetBindableInfo createObject(JavaInfo javaInfo) throws Exception {
            return new WidgetBindableInfo(javaInfo, WidgetBindableInfo.this, provider);
          }

          public void update(WidgetBindableInfo widget) throws Exception {
            widget.update(provider);
          }
        });
  }

  /**
   * Helper method for access to editor class loader.
   */
  public ClassLoader getClassLoader() {
    return EditorState.get(m_javaInfo.getEditor()).getEditorLoader();
  }

  /**
   * Access to properties.
   */
  public List<WidgetPropertyBindableInfo> getProperties() {
    return m_properties;
  }

  /**
   * @return {@link WidgetBindableInfo} children that association with given {@link ASTNode} or
   *         <code>null</code>.
   */
  public WidgetBindableInfo resolveReference(ASTNode node) throws Exception {
    if (AstNodeUtils.isVariable(node)) {
      if (AstNodeUtils.getVariableName(node).equals(
          JavaInfoReferenceProvider.getReference(m_javaInfo))) {
        return this;
      }
    } else if (m_javaInfo.isRepresentedBy(node)) {
      return this;
    }
    for (WidgetBindableInfo child : m_children) {
      WidgetBindableInfo result = child.resolveReference(node);
      if (result != null) {
        return result;
      }
    }
    if (m_javaInfo.isRoot()) {
      JavaInfo javaInfo = m_javaInfo.getChildRepresentedBy(node);
      if (javaInfo != null) {
        return resolve(javaInfo);
      }
    }
    return null;
  }

  public WidgetBindableInfo resolve(JavaInfo javaInfo) {
    if (m_javaInfo == javaInfo) {
      return this;
    }
    for (WidgetBindableInfo child : m_children) {
      WidgetBindableInfo result = child.resolve(javaInfo);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * @return {@link WidgetPropertyBindableInfo} property that association with given reference or
   *         <code>null</code>.
   */
  @Override
  public WidgetPropertyBindableInfo resolvePropertyReference(String reference) throws Exception {
    for (WidgetPropertyBindableInfo property : m_properties) {
      if (reference.equals(property.getReference())) {
        return property;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BindableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<BindableInfo> getChildren() {
    return CoreUtils.cast(m_children);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveInfo getParent() {
    return m_parent;
  }

  public List<IObserveInfo> getChildren(ChildrenContext context) {
    if (context == ChildrenContext.ChildrenForMasterTable) {
      return CoreUtils.cast(m_children);
    }
    if (context == ChildrenContext.ChildrenForPropertiesTable) {
      return CoreUtils.cast(m_properties);
    }
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObservePresentation getPresentation() {
    return m_presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return ObserveType.WIDGETS;
  }
}