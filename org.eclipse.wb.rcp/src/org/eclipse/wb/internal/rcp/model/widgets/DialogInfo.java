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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ShellInfo;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Model for {@link Dialog}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class DialogInfo extends AbstractComponentInfo
    implements
      IThisMethodParameterEvaluator {
  private Shell m_shell;
  private ShellInfo m_shellInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    ensureNamesForConstructors();
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void bindComponents(List<JavaInfo> components) throws Exception {
        fetchShell(components);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureNamesForConstructors() {
    for (ConstructorDescription constructor : getDescription().getConstructors()) {
      List<ParameterDescription> parameters = constructor.getParameters();
      for (int i = 0; i < parameters.size(); i++) {
        ParameterDescription parameter = parameters.get(i);
        if (parameter.getName() == null) {
          parameter.setName("arg" + i);
        }
      }
    }
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
    if (ObjectUtils.equals(parameter.getName().getIdentifier(), "style")) {
      return SWT.DIALOG_TRIM;
    }
    return AstEvaluationEngine.UNKNOWN;
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
  protected TopBoundsSupport createTopBoundsSupport() {
    return new DialogTopBoundsSupport(this);
  }

  @Override
  public Object getComponentObject() {
    return m_shell;
  }

  /**
   * @return the {@link DialogInfo}'s Shell.
   */
  Object getShell() {
    return m_shell;
  }

  /**
   * @return the {@link DialogInfo}'s {@link ShellInfo}.
   */
  public ShellInfo getShellInfo() {
    return m_shellInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    if (m_shell != null) {
      m_shell.dispose();
      m_shell = null;
    }
    m_shellInfo = null;
    super.refresh_dispose();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    fetchShell(getChildrenJava());
    super.refresh_afterCreate();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ControlInfo.refresh_fetch(this, new RunnableEx() {
      public void run() throws Exception {
        DialogInfo.super.refresh_fetch();
      }
    });
  }

  /**
   * Fills field {@link #m_shell}.
   */
  private void fetchShell(List<JavaInfo> components) {
    m_shellInfo = GenericsUtils.get(ShellInfo.class, components);
    Assert.isTrueException(m_shellInfo != null, IExceptionConstants.SWT_DIALOG_NO_MAIN_SHELL);
    JavaInfoUtils.setParameter(m_shellInfo, "SWT.isRoot", "true");
    m_shell = (Shell) m_shellInfo.getObject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Class<?> getNotAbstractDialog(ClassLoader classLoader)
      throws ClassNotFoundException {
    try {
      return classLoader.loadClass("org.eclipse.swt.widgets.Dialog_");
    } catch (ClassNotFoundException e) {
      Enhancer enhancer = new Enhancer();
      enhancer.setCallbackType(NoOp.class);
      enhancer.setSuperclass(classLoader.loadClass("org.eclipse.swt.widgets.Dialog"));
      enhancer.setClassLoader(classLoader);
      enhancer.setNamingPolicy(new NamingPolicy() {
        public String getClassName(String prefix, String source, Object key, Predicate names) {
          return prefix + "_";
        }
      });
      return enhancer.createClass();
    }
  }

  public static void contributeExecutionFlow(TypeDeclaration typeDeclaration,
      List<MethodDeclaration> methods) throws Exception {
    MethodDeclaration method = AstNodeUtils.getMethodByName(typeDeclaration, "open");
    Assert.isTrueException(method != null, IExceptionConstants.SWT_DIALOG_NO_OPEN_METHOD);
    methods.add(method);
  }
}
