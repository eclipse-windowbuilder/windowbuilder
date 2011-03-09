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
package org.eclipse.wb.internal.swing.jsr296.model;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.utils.SwingUtils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import javax.swing.JFrame;

/**
 * Model for {@link org.jdesktop.application.FrameView}.
 * 
 * @author scheglov_ke
 * @coverage swing.jsr296
 */
public class FrameViewInfo extends AbstractComponentInfo implements IThisMethodParameterEvaluator {
  private JFrame m_frame;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FrameViewInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IThisMethodParameterEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluateParameter(EvaluationContext context,
      MethodDeclaration methodDeclaration,
      String methodSignature,
      SingleVariableDeclaration parameter,
      int index) throws Exception {
    if (AstNodeUtils.isSuccessorOf(parameter, "org.jdesktop.application.Application")) {
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
      Class<?> applicationClass = classLoader.loadClass("org.jdesktop.application.Application");
      // prepare Application instance (only for Class with callback)
      Enhancer enhancer = new Enhancer();
      enhancer.setClassLoader(classLoader);
      enhancer.setSuperclass(applicationClass);
      enhancer.setCallback(new MethodInterceptor() {
        public Object intercept(Object obj,
            java.lang.reflect.Method method,
            Object[] args,
            MethodProxy proxy) throws Throwable {
          return proxy.invokeSuper(obj, args);
        }
      });
      Object applicationForClass = enhancer.create();
      // create Application instance using Application method, which performs required initializations
      return ReflectionUtils.invokeMethod(
          applicationClass,
          "create(java.lang.Class)",
          applicationForClass.getClass());
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new FrameViewTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canBeRoot() {
    return true;
  }

  @Override
  public Object getComponentObject() {
    return m_frame;
  }

  /**
   * @return the {@link FrameViewInfo}'s {@link JFrame}.
   */
  public JFrame getFrame() {
    return m_frame;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void doRefresh(final RunnableEx runnableEx) throws Exception {
    SwingUtils.runLaterAndWait(runnableEx);
  }

  @Override
  public void refresh_dispose() throws Exception {
    // dispose JFrame
    if (m_frame != null) {
      m_frame.dispose();
      m_frame = null;
    }
    // call "super"
    super.refresh_dispose();
  }

  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    m_frame = (JFrame) ReflectionUtils.invokeMethod(getObject(), "getFrame()");
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    // preferred size, should be here, because "super" applies "top bounds"
    setPreferredSize(CoordinateUtils.get(m_frame.getPreferredSize()));
    // call "super"
    super.refresh_afterCreate();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ComponentInfo.refresh_fetch(this, m_frame, new RunnableEx() {
      public void run() throws Exception {
        FrameViewInfo.super.refresh_fetch();
      }
    });
  }
}
