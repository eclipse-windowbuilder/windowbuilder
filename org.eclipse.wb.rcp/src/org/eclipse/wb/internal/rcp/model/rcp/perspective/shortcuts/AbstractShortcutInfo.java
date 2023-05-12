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
package org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.AbstractPartInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.IRenderableInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutAddCreationSupport;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;
import org.eclipse.wb.internal.swt.support.RectangleSupport;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPageLayout;

/**
 * Abstract item for shortcut-like methods in {@link IPageLayout}, such as
 * {@link IPageLayout#addFastView(String)}, {@link IPageLayout#addShowViewShortcut(String)} and
 * {@link IPageLayout#addPerspectiveShortcut(String)}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public abstract class AbstractShortcutInfo extends AbstractComponentInfo implements IRenderableInfo {
  private final AbstractShortcutContainerInfo m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractShortcutInfo(PageLayoutInfo page,
      AbstractShortcutContainerInfo container,
      MethodInvocation invocation) throws Exception {
    super(page.getEditor(), new ComponentDescription(null), new PageLayoutAddCreationSupport(page,
        invocation));
    m_container = container;
    ObjectInfoUtils.setNewId(this);
    getDescription().setToolkit(page.getDescription().getToolkit());
    setAssociation(new InvocationVoidAssociation());
    setVariableSupport(new EmptyPureVariableSupport(this));
    container.addChild(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>ID</code> of this {@link AbstractPartInfo}.
   */
  public final String getId() {
    return (String) getInvocationArgument(0);
  }

  /**
   * @return the underlying {@link MethodInvocation}.
   */
  private MethodInvocation getInvocation() {
    return (MethodInvocation) getCreationSupport().getNode();
  }

  /**
   * @return the value of argument of underlying {@link MethodInvocation}.
   */
  private Object getInvocationArgument(int index) {
    MethodInvocation invocation = getInvocation();
    Expression argument = DomGenerics.arguments(invocation).get(index);
    return JavaInfoEvaluationHelper.getValue(argument);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public Image getIcon() throws Exception {
      return getPresentationIcon();
    }

    @Override
    public String getText() throws Exception {
      return getPresentationText();
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  /**
   * @return the icon to show in component tree.
   */
  protected abstract Image getPresentationIcon() throws Exception;

  /**
   * @return the text to show in component tree.
   */
  protected abstract String getPresentationText() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rendering
  //
  ////////////////////////////////////////////////////////////////////////////
  private ToolItem m_item;

  @Override
  public Object render() throws Exception {
    ToolBar toolBar = m_container.getToolBar();
    m_item = new ToolItem(toolBar, SWT.NONE);
    m_item.setImage(getPresentationIcon());
    m_item.setToolTipText(getPresentationText());
    return m_item;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    {
      Composite composite = m_container.getComposite();
      Rectangle toolBarBounds = CoordinateUtils.getBounds(composite, m_container.getToolBar());
      Rectangle itemBounds = RectangleSupport.getRectangle(m_item.getBounds());
      itemBounds.performTranslate(toolBarBounds.x, toolBarBounds.y);
      setModelBounds(itemBounds);
    }
    super.refresh_fetch();
  }
}
