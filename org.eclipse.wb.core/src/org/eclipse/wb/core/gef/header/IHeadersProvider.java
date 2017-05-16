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
package org.eclipse.wb.core.gef.header;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.gef.graphical.HeaderGraphicalViewer;

import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Provider for headers on {@link HeaderGraphicalViewer}.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public interface IHeadersProvider {
  /**
   * @return the {@link LayoutEditPolicy} for headers container.
   */
  LayoutEditPolicy getContainerLayoutPolicy(boolean horizontal);

  /**
   * @return the {@link List} of header models.
   */
  List<?> getHeaders(boolean horizontal);

  /**
   * @return the {@link EditPart} for given header model.
   */
  EditPart createHeaderEditPart(boolean horizontal, Object model);

  /**
   * Adds menu items into given {@link IMenuManager}.
   */
  void buildContextMenu(IMenuManager manager, boolean horizontal);

  /**
   * Invoked on double click on headers container.
   */
  void handleDoubleClick(boolean horizontal);
}
