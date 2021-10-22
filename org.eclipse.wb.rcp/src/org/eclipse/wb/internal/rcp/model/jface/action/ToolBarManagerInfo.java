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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

/**
 * Model for {@link IToolBarManager}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ToolBarManagerInfo extends ContributionManagerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolBarManagerInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    {
      ToolBar toolBar = (ToolBar) ReflectionUtils.invokeMethod2(getObject(), "getControl");
      // if no any items, create one
      if (toolBar.getItemCount() == 0) {
        addEmptyAction();
      }
      // OK, remember as component
      setComponentObject(toolBar);
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    // prepare bounds of underlying ToolBar
    ControlInfo.refresh_fetch(this, null);
    // prepare bounds of IContributionItem's
    ToolItem[] toolItems = ((ToolBar) getComponentObject()).getItems();
    for (AbstractComponentInfo contributionItem : getItems()) {
      Object contributionItemObject = contributionItem.getObject();
      for (ToolItem toolItem : toolItems) {
        if (toolItem.getData() == contributionItemObject) {
          Object itemBoundsObject = ReflectionUtils.invokeMethod2(toolItem, "getBounds");
          Rectangle itemBounds = RectangleSupport.getRectangle(itemBoundsObject);
          contributionItem.setModelBounds(itemBounds);
          break;
        }
      }
    }
    // special support for ToolBarContributionItem
    if (getParent() instanceof ContributionItemInfo) {
      ContributionItemInfo parentItem = (ContributionItemInfo) getParent();
      parentItem.setModelBounds(getModelBounds().getCopy());
      parentItem.setBounds(getBounds().getCopy());
      getBounds().setLocation(0, 0);
    }
    // continue
    super.refresh_fetch();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Inserts new "empty" {@link Action}.
   */
  private void addEmptyAction() throws Exception {
    String emptyText = JavaInfoUtils.getParameter(this, "emptyText");
    Assert.isNotNull(
        emptyText,
        "IToolBarManager should have parameter 'emptyText' with text to show when there are no Action's.");
    // prepare Action
    Object action;
    {
      ClassLoader editorLoader = JavaInfoUtils.getClassLoader(this);
      Class<?> actionClass = editorLoader.loadClass("org.eclipse.jface.action.Action");
      Enhancer enhancer = new Enhancer();
      enhancer.setClassLoader(editorLoader);
      enhancer.setSuperclass(actionClass);
      enhancer.setCallback(NoOp.INSTANCE);
      action = enhancer.create(new Class[]{String.class}, new Object[]{emptyText});
    }
    // append Action and update
    ReflectionUtils.invokeMethod(getObject(), "add(org.eclipse.jface.action.IAction)", action);
    ReflectionUtils.invokeMethod2(getObject(), "update", boolean.class, true);
  }
}
