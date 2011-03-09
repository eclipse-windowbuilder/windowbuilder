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

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.part.Page;

import org.apache.commons.lang.NotImplementedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Model for {@link Page}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PageInfo extends ViewPartLikeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getGUIMethodName() {
    return "createControl";
  }

  @Override
  protected void configureTabItem(CTabItem tabItem) throws Exception {
    tabItem.setImage(getDescription().getIcon());
    tabItem.setText("Page in PageBookView");
  }

  @Override
  protected void applyActionBars() throws Exception {
    ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> pageSiteClass = editorLoader.loadClass("org.eclipse.ui.part.IPageSite");
    // create IPageSite
    final Object pageSite =
        Proxy.newProxyInstance(
            editorLoader,
            new Class<?>[]{pageSiteClass},
            new InvocationHandler() {
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String signature = ReflectionUtils.getMethodSignature(method);
                if (signature.equals("getActionBars()")) {
                  return m_actionBars;
                }
                if (signature.equals("setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)")) {
                  return null;
                }
                throw new NotImplementedException();
              }
            });
    // call init(IPageSite)
    ReflectionUtils.invokeMethod(getObject(), "init(org.eclipse.ui.part.IPageSite)", pageSite);
  }
}
