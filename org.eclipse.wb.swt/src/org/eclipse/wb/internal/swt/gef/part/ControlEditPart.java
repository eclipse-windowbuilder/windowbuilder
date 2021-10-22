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
package org.eclipse.wb.internal.swt.gef.part;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.policy.TabOrderContainerEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.swt.gef.part.delegate.IControlEditPartDelegate;
import org.eclipse.wb.internal.swt.gef.part.delegate.IControlEditPartDelegateProvider;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import java.util.List;

/**
 * {@link EditPart} for {@link ControlInfo}.
 *
 * @author lobas_av
 * @coverage swt.gef.part
 */
public class ControlEditPart extends AbstractComponentEditPart {
  private final IControlEditPartDelegate m_delegate;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ControlEditPart(ControlInfo control) {
    super(control);
    m_delegate = createDelegate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public EditPart getTargetEditPart(Request request) {
    if (TabOrderContainerEditPolicy.TAB_ORDER_REQUEST == request) {
      return this;
    }
    return super.getTargetEditPart(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delegating
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    if (m_delegate != null) {
      return m_delegate.createFigure();
    } else {
      return super.createFigure();
    }
  }

  @Override
  protected void refreshVisuals() {
    if (m_delegate != null) {
      m_delegate.refreshVisuals();
    } else {
      super.refreshVisuals();
    }
  }

  @Override
  public void addNotify() {
    if (m_delegate != null) {
      m_delegate.addNotify();
    }
    super.addNotify();
  }

  @Override
  public void removeNotify() {
    if (m_delegate != null) {
      m_delegate.removeNotify();
    }
    super.removeNotify();
  }

  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    if (m_delegate != null) {
      m_delegate.refreshEditPolicies();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delegate
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of registered {@link IControlEditPartDelegateProvider}'s.
   */
  private static List<IControlEditPartDelegateProvider> getDelegateProviders() {
    return ExternalFactoriesHelper.getElementsInstances(
        IControlEditPartDelegateProvider.class,
        "org.eclipse.wb.swt.controlEditPartDelegateProviders",
        "provider");
  }

  /**
   * @return the {@link IControlEditPartDelegate} for this {@link ControlEditPart}, may return
   *         <code>null</code> if no delegate found.
   */
  private IControlEditPartDelegate createDelegate() {
    for (IControlEditPartDelegateProvider provider : getDelegateProviders()) {
      IControlEditPartDelegate delegate = provider.getDelegate(this);
      if (delegate != null) {
        return delegate;
      }
    }
    // no delegate
    return null;
  }
}