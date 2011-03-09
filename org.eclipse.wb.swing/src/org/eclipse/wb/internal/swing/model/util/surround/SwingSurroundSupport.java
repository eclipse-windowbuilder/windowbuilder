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
package org.eclipse.wb.internal.swing.model.util.surround;

import org.eclipse.wb.internal.core.model.util.surround.SurroundSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

/**
 * Helper for surrounding {@link ComponentInfo}'s with some {@link ContainerInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.util
 */
public abstract class SwingSurroundSupport extends SurroundSupport<ContainerInfo, ComponentInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingSurroundSupport(ContainerInfo sourceContainer) {
    super(sourceContainer, ComponentInfo.class);
  }
}
