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
package org.eclipse.wb.internal.css.model.root;

/**
 * Model change events are fired by the model when it is changed from the last clean state. Model
 * change listeners can use these events to update accordingly.
 * 
 * @author scheglov_ke
 * @coverage CSS.model
 */
public class ModelChangedEvent {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Static fields
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Indicates a change where one or more objects are added to the model.
   */
  public static final int INSERT = 1;
  /**
   * Indicates a change where one or more objects are removed from the model.
   */
  public static final int REMOVE = 2;
  /**
   * indicates that a model object's property has been changed.
   */
  public static final int CHANGE = 3;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final int m_type;
  private final Object m_object;
  private final Object m_newValue;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModelChangedEvent(Object object, String changedProperty, Object newValue) {
    m_type = ModelChangedEvent.CHANGE;
    m_object = object;
    m_newValue = newValue;
  }

  public ModelChangedEvent(int type, Object object) {
    m_type = type;
    m_object = object;
    m_newValue = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the type of change that occurred in the model (one of {@link #INSERT}, {@link #REMOVE},
   *         {@link #CHANGE}.
   */
  public int getChangeType() {
    return m_type;
  }

  /**
   * @return the model object that is affected by the change.
   */
  public Object getChangedObject() {
    return m_object;
  }

  /**
   * When model change is of type {@link #CHANGE}, this method is used to obtain the new value of
   * the property (after the change).
   * 
   * @return the new value of the changed property.
   */
  public Object getNewValue() {
    return m_newValue;
  }
}
