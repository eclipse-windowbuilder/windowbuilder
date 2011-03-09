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
package org.eclipse.wb.internal.swt.gef.part.delegate;

import org.eclipse.wb.internal.swt.gef.part.ControlEditPart;

/**
 * Provider for {@link IControlEditPartDelegate} of given {@link ControlEditPart}.
 * 
 * @author scheglov_ke
 * @coverage swt.gef.part
 */
public interface IControlEditPartDelegateProvider {
  /**
   * Returns the {@link IControlEditPartDelegate} for given {@link ControlEditPart}. eRCP and RCP
   * provide different implementations of {@link IControlEditPartDelegate}.
   * 
   * @param editPart
   *          the {@link ControlEditPart} to get provider for.
   * @return the {@link IControlEditPartDelegate} for given {@link ControlEditPart}.
   */
  IControlEditPartDelegate getDelegate(ControlEditPart editPart);
}
