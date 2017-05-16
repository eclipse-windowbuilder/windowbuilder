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
package org.eclipse.wb.internal.core.model.generic;

/**
 * Interface of typical container with single child.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public interface SimpleContainer extends AbstractContainer {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if there are no existing child.
   */
  boolean isEmpty();

  /**
   * @return the existing child, may be <code>null</code>.
   */
  Object getChild();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  void command_CREATE(Object newObject) throws Exception;

  void command_ADD(Object moveObject) throws Exception;
}
