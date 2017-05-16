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
package org.eclipse.wb.internal.core.utils.state;

import org.eclipse.wb.core.model.ObjectInfo;

import java.util.List;

/**
 * Helper for other features.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IOtherHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Context
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the context {@link IJavaProject}.
   */
  Object getJavaProject();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Object} for given model, if it has it. May be <code>null</code> if given
   *         modes has no object, or it is really <code>null</code>.
   */
  Object getObject(ObjectInfo model);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Warnings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link EditorWarning}'s.
   */
  List<EditorWarning> getWarnings();

  /**
   * Adds new {@link EditorWarning}.
   */
  void addWarning(EditorWarning warning);
}
