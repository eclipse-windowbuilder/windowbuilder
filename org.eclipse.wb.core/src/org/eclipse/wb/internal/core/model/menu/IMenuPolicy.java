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
package org.eclipse.wb.internal.core.model.menu;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Interface for menu policy: validation and performing operations.
 *
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public interface IMenuPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param newObject
   *          the new object to add.
   *
   * @return <code>true</code> if new object can be added.
   */
  boolean validateCreate(Object newObject);

  /**
   * @param mementoObject
   *          some object that contains information about pasting.
   *
   * @return <code>true</code> paste operation can be performed.
   */
  boolean validatePaste(Object mementoObject);

  /**
   * @param object
   *          the object that should be moved.
   *
   * @return <code>true</code> move operation can be performed.
   */
  boolean validateMove(Object object);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new object.
   *
   * @param newObject
   *          the new object to add.
   * @param nextObject
   *          the item to add new item before.
   */
  void commandCreate(Object newObject, Object nextObject) throws Exception;

  /**
   * Performs paste.
   *
   * @param mementoObject
   *          some object that contains information about pasting.
   * @param nextObject
   *          the existing object to paste object before.
   *
   * @return the {@link List} of pasted objects that should be selected after paste.
   */
  List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception;

  /**
   * Performs move of one "object" before other "object".
   *
   * @param object
   *          the toolkit object that should be moved.
   * @param nextObject
   *          the object to move object before.
   */
  void commandMove(Object object, Object nextObject) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default implementations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuPolicy} that does nothing.
   */
  IMenuPolicy NOOP = new IMenuPolicy() {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean validateCreate(Object newObject) {
      return false;
    }

    public boolean validatePaste(Object mementoObject) {
      return false;
    }

    public boolean validateMove(Object object) {
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Operations
    //
    ////////////////////////////////////////////////////////////////////////////
    public void commandCreate(Object newObject, Object nextObject) throws Exception {
    }

    public void commandMove(Object object, Object nextObject) throws Exception {
    }

    public List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception {
      return ImmutableList.of();
    }
  };
}
