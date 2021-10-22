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
package org.eclipse.wb.tests.gef;

/**
 * Interface for running UI actions.
 *
 * @author scheglov_ke
 */
public interface UIRunnable {
  /**
   * Executes some actions in {@link UiContext}.
   */
  void run(UiContext context) throws Exception;
}
