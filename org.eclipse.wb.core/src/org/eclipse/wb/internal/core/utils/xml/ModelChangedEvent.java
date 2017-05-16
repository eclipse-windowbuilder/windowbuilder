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
package org.eclipse.wb.internal.core.utils.xml;

/**
 * Model change events are fired by the model when it is changed. Model change listeners can use
 * these events to update accordingly.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
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
   * Indicates a change where objects was moved into new parent.
   */
  public static final int MOVE = 2;
  /**
   * Indicates a change where one or more objects are removed from the model.
   */
  public static final int REMOVE = 3;
  /**
   * Indicates that a model object's property has been changed.
   */
  public static final int CHANGE = 4;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final int m_type;
  private final Object m_object;
  private final Object m_oldValue;
  private final Object m_newValue;
  private final String m_property;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModelChangedEvent(int type,
      Object object,
      String changedProperty,
      Object oldValue,
      Object newValue) {
    m_type = type;
    m_object = object;
    m_property = changedProperty;
    m_oldValue = oldValue;
    m_newValue = newValue;
  }

  public ModelChangedEvent(int type, Object object, String changedProperty) {
    this(type, object, changedProperty, null, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the type of change that occurred in the model (one of <code>INSERT</code>,
   *         <code>REMOVE</code>, <code>CHANGE</code> or <code>WORLD_CHANGED </code>).
   */
  public int getChangeType() {
    return m_type;
  }

  /**
   * @return an model object that is affected by the change.
   */
  public Object getChangedObject() {
    return m_object;
  }

  /**
   * @return property that has been changed in the model object, or <code>null</code> if type is not
   *         CHANGE or if more than one property has been changed.
   */
  public String getChangedProperty() {
    return m_property;
  }

  /**
   * When model change is of type <code>CHANGE</code>, this method is used to obtain the old value
   * of the property (before the change).
   *
   * @return the old value of the changed property
   */
  public Object getOldValue() {
    return m_oldValue;
  }

  /**
   * When model change is of type <code>CHANGE</code>, this method is used to obtain the new value
   * of the property (after the change).
   *
   * @return the new value of the changed property.
   */
  public Object getNewValue() {
    return m_newValue;
  }
}
