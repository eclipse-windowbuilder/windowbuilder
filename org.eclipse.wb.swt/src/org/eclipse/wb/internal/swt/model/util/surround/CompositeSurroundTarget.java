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
package org.eclipse.wb.internal.swt.model.util.surround;

import org.eclipse.wb.internal.core.model.util.surround.ISurroundTarget;

import org.eclipse.swt.widgets.Composite;

/**
 * {@link ISurroundTarget} for using {@link Composite} as target container.
 *
 * @author scheglov_ke
 * @coverage swt.model.util
 */
public final class CompositeSurroundTarget extends AbstractCompositeSurroundTarget {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final CompositeSurroundTarget INSTANCE = new CompositeSurroundTarget();

  private CompositeSurroundTarget() {
    super("org.eclipse.swt.widgets.Composite");
  }
}
