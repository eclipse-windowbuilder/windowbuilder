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
package org.eclipse.wb.internal.swing;

import org.eclipse.wb.internal.core.model.description.IToolkitProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;

/**
 * Implementation of {@link IToolkitProvider} for Swing.
 *
 * @author scheglov_ke
 * @coverage swing
 */
public final class ToolkitProvider implements IToolkitProvider {
  public static final ToolkitDescriptionJava DESCRIPTION = SwingToolkitDescription.INSTANCE;
  static {
    ((SwingToolkitDescription) DESCRIPTION).initialize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IToolkitProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolkitDescription getDescription() {
    return DESCRIPTION;
  }
}
