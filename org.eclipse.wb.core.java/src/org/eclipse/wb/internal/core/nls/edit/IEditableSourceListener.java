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
package org.eclipse.wb.internal.core.nls.edit;

/**
 * Listener for changes in IEditableSource.
 *
 * For example when we externalize new property, we should update composite that displays this
 * source.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public interface IEditableSourceListener {
  /**
   * Sent when new key was added.
   */
  void keyAdded(String key, Object o);

  /**
   * Sent when key was removed.
   */
  void keyRemoved(String key);

  /**
   * Sent when key was renamed.
   */
  void keyRenamed(String oldKey, String newKey);
}
