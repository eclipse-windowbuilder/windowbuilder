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
package org.eclipse.wb.internal.ercp.gef.part.delegete;

import org.eclipse.wb.internal.ercp.ToolkitProvider;
import org.eclipse.wb.internal.swt.gef.part.ControlEditPart;
import org.eclipse.wb.internal.swt.gef.part.delegate.IControlEditPartDelegate;
import org.eclipse.wb.internal.swt.gef.part.delegate.IControlEditPartDelegateProvider;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

/**
 * Implementation of {@link IControlEditPartDelegateProvider} for eRCP.
 * 
 * @author scheglov_ke
 * @coverage ercp.gef.part
 */
public final class ControlEditPartDelegateProvider implements IControlEditPartDelegateProvider {
  public IControlEditPartDelegate getDelegate(ControlEditPart editPart) {
    if (editPart.getModel() instanceof CompositeInfo) {
      CompositeInfo composite = (CompositeInfo) editPart.getModel();
      if (composite.getDescription().getToolkit() == ToolkitProvider.DESCRIPTION
          && composite.isRoot()) {
        return new TopControlEditPartDelegate(editPart);
      }
    }
    // no delegate
    return null;
  }
}
