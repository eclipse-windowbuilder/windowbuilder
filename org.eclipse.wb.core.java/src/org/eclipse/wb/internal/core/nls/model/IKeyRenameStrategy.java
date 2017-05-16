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
package org.eclipse.wb.internal.core.nls.model;


/**
 * This interface helps in renaming keys for externalized properties, when name of component is
 * changing.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface IKeyRenameStrategy {
  /**
   * @return the new key. If it is same as any of existing keys, including returning "oldKey", then
   *         no rename will be performed.
   */
  String getNewKey(String oldName, String newName, String oldKey);
}
