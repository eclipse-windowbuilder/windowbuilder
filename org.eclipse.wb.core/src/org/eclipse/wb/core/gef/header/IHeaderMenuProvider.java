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

import org.eclipse.jface.action.IMenuManager;

/**
 * Provider for context menu of header.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public interface IHeaderMenuProvider {
  /**
   * Adds menu items into given {@link IMenuManager}.
   */
  void buildContextMenu(IMenuManager manager);
}
