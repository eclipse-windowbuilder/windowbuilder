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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Collections;
import java.util.List;

/**
 * Components container with type {@link ObserveType#WIDGETS}. Works on <code>Swing</code>
 * components.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model.components
 */
public final class ComponentsObserveTypeContainer extends ObserveTypeContainer {
  private List<IObserveInfo> m_observes = Collections.emptyList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentsObserveTypeContainer() {
    super(ObserveType.WIDGETS, true, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserveInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<IObserveInfo> getObservables() {
    return m_observes;
  }

  @Override
  public void synchronizeObserves(JavaInfo root, AstEditor editor, TypeDeclaration rootNode)
      throws Exception {
    int count = m_observes.size();
    for (int i = 0; i < count; i++) {
      ComponentObserveInfo observe = (ComponentObserveInfo) m_observes.get(i);
      observe.update();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createObservables(JavaInfo root,
      IModelResolver resolver,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    m_observes = Lists.newArrayList();
    m_observes.add(new ComponentObserveInfo(new BeanSupport(), root));
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      ClassInstanceCreation creation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    return null;
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveInfo resolve(Expression expression) throws Exception {
    for (IObserveInfo iobserve : m_observes) {
      ComponentObserveInfo observe = (ComponentObserveInfo) iobserve;
      ObserveInfo resultInfo = observe.resolve(expression);
      if (resultInfo != null) {
        return resultInfo;
      }
    }
    return null;
  }

  public ObserveInfo resolve(JavaInfo javaInfo) throws Exception {
    for (IObserveInfo iobserve : m_observes) {
      ComponentObserveInfo observe = (ComponentObserveInfo) iobserve;
      ObserveInfo resultInfo = observe.resolve(javaInfo);
      if (resultInfo != null) {
        return resultInfo;
      }
    }
    return null;
  }

  public ObserveInfo resolve(String reference) throws Exception {
    for (IObserveInfo iobserve : m_observes) {
      ComponentObserveInfo observe = (ComponentObserveInfo) iobserve;
      ObserveInfo resultInfo = observe.resolve(reference);
      if (resultInfo != null) {
        return resultInfo;
      }
    }
    return null;
  }

  public static ObserveCreationType getCreationType(Class<?> objectType) {
    // JList
    if (javax.swing.JList.class.isAssignableFrom(objectType)) {
      return ObserveCreationType.JListBinding;
    }
    // JTable
    if (javax.swing.JTable.class.isAssignableFrom(objectType)) {
      return ObserveCreationType.JTableBinding;
    }
    // JComboBox
    if (javax.swing.JComboBox.class.isAssignableFrom(objectType)) {
      return ObserveCreationType.JComboBoxBinding;
    }
    // auto
    return ObserveCreationType.AutoBinding;
  }
}