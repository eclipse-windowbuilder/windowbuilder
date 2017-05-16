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
package org.eclipse.wb.internal.core.utils;

import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Compact {@link ToStringStyle} - with short class name and field names.
 *
 * @author scheglov_ke
 */
public class CompactToStringStyle extends ToStringStyle {
  private static final long serialVersionUID = 0L;
  public static final ToStringStyle INSTANCE = new CompactToStringStyle();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private CompactToStringStyle() {
    setUseClassName(true);
    setUseShortClassName(true);
    setUseIdentityHashCode(false);
    setUseFieldNames(true);
    setContentStart("{");
    setContentEnd("}");
  }
}
