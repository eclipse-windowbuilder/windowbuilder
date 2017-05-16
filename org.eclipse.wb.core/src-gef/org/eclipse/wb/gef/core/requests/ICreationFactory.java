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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.wb.gef.core.tools.CreationTool;

/**
 * A factory used to create new objects.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface ICreationFactory {
  /**
   * Activates this factory, during {@link CreationTool} activation (including reloading). This
   * allows factory do any operations that are too expensive to perform them in
   * {@link #getNewObject()}.
   *
   * If any exception thrown, then {@link CreationTool} will be unloaded.
   */
  void activate() throws Exception;

  /**
   * @return the new object.
   */
  Object getNewObject();
}