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
package org.eclipse.wb.internal.swing.laf.model;

import org.eclipse.wb.internal.swing.model.ModelMessages;

import javax.swing.LookAndFeel;

/**
 * Used just to indicate separator in list of LAFs.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class SeparatorLafInfo extends LafInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public SeparatorLafInfo(String name) {
    super(name, name, null);
  }

  public SeparatorLafInfo() {
    this("");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public LookAndFeel getLookAndFeelInstance() throws Exception {
    throw new RuntimeException(ModelMessages.SeparatorLafInfo_canNotInstantiate);
  }
}
