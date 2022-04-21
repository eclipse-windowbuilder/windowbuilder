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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.SwtMethodParameterEvaluator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * Model for <code>AbstractSplashHandler</code>.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public class AbstractSplashHandlerInfo extends AbstractComponentInfo implements IJavaInfoRendering {
  private Shell m_shell;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSplashHandlerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    JavaInfoUtils.scheduleSpecialRendering(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaInfoRendering
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void render() throws Exception {
    Object window = getObject();
    {
      ASTNode node = getCreationSupport().getNode();
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
      m_shell = (Shell) SwtMethodParameterEvaluator.getDefaultShell(node, classLoader);
      m_shell.setLayout(new FillLayout());
      //m_shell.setVisible(true);
    }
    ReflectionUtils.invokeMethod(window, "init(org.eclipse.swt.widgets.Shell)", m_shell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new AbstractSplashHandlerTopBoundsSupport(this);
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
    return m_shell;
  }

  /**
   * @return the {@link AbstractSplashHandlerInfo}'s Shell.
   */
  Object getShell() {
    return m_shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    // dispose Dialog
    {
      Object object = getObject();
      if (object != null) {
        ReflectionUtils.invokeMethod(object, "dispose()");
        m_shell = null;
      }
    }
    // call "super"
    super.refresh_dispose();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ControlInfo.refresh_fetch(this, new RunnableEx() {
      @Override
      public void run() throws Exception {
        AbstractSplashHandlerInfo.super.refresh_fetch();
      }
    });
  }
}
