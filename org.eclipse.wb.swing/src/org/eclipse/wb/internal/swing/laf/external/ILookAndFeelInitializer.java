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
package org.eclipse.wb.internal.swing.laf.external;

import javax.swing.LookAndFeel;

/**
 * Interface for extension point providing initializing {@link LookAndFeel} external classes.
 * 
 * @author mitin_aa
 * @coverage swing.laf
 */
public interface ILookAndFeelInitializer {
  /**
   * Performs any initializing before using look-n-feel class, i.e., just before
   * javax.swing.UIManager.setLookAndFeel(javax.swing.LookAndFeel).
   * 
   * @throws Exception
   */
  void initialize() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  final ILookAndFeelInitializer DEFAULT = new ILookAndFeelInitializer() {
    public void initialize() throws Exception {
      // do nothing
    }
  };
}
