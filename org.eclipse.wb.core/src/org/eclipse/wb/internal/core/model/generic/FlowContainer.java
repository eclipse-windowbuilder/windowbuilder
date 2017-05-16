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
 * Interface of typical flow based container.
 *
 * @author scheglov_ke
 * @coverage core.model.generic
 */
public interface FlowContainer extends AbstractContainer {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this container is horizontal.
   */
  boolean isHorizontal();

  /**
   * @return <code>true</code> if this container has RTL orientation.
   */
  boolean isRtl();

  /**
   * @return <code>true</code> if given existing child of container can be used as reference.
   */
  boolean validateReference(Object reference);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new component on this container.
   */
  void command_CREATE(Object newObject, Object referenceObject) throws Exception;

  /**
   * Moves existing component on this container, internally or from other container.
   */
  void command_MOVE(Object moveObject, Object referenceObject) throws Exception;
}
