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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

/**
 * Implementation of {@link org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport} for
 * RCP.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class CompositeTopBoundsSupport
    extends
      org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeTopBoundsSupport(CompositeInfo composite) {
    super(composite);
  }
}
