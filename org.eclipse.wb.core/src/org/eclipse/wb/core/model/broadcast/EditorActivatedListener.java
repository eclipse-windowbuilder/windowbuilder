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
package org.eclipse.wb.core.model.broadcast;

/**
 * This listener is invoked after activating editor.Implementations can specify that some external
 * change was happened (for example modification of used component), so reparse required.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface EditorActivatedListener {
  /**
   * @param request
   *          the request to specify operation that should be performed. Listeners may use
   *          "requestX()" methods to specify operation that should be performed.
   */
  void invoke(EditorActivatedRequest request) throws Exception;
}
