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
package org.eclipse.wb.internal.core.model.menu;

/**
 * Interface for "popup" state of {@link IMenuInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public interface IMenuPopupInfo extends IMenuObjectInfo {
  /**
   * @return the underlying {@link IMenuInfo}.
   */
  IMenuInfo getMenu();
}
