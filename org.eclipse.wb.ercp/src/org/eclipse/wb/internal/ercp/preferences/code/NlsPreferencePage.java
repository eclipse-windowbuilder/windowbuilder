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
package org.eclipse.wb.internal.ercp.preferences.code;

import org.eclipse.wb.internal.ercp.ToolkitProvider;

/**
 * NLS preferences for eRCP.
 * 
 * @author scheglov_ke
 * @coverage ercp.preferences.ui
 */
public final class NlsPreferencePage extends org.eclipse.wb.internal.core.nls.NlsPreferencePage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NlsPreferencePage() {
    super(ToolkitProvider.DESCRIPTION);
  }
}
