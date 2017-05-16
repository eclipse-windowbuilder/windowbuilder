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
 * Listener that can be notified that some {@link IMenuObjectInfo} was deleted.
 *
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public interface IMenuObjectListener {
  /**
   * Notifies refresh should be performed.
   */
  void refresh();

  /**
   * Notifies that toolkit object is going to be deleted.
   */
  void deleting(Object toolkitModel);
}
