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
package org.eclipse.wb.internal.core.nls.ui;

import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract superclass for composite's that should be used for adding new string sources.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public abstract class AbstractSourceNewComposite extends Composite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSourceNewComposite(Composite parent, int style) {
    super(parent, style);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties change support
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PropertyChangeSupport m_propertyChangeSupport = new PropertyChangeSupport(this);

  void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertyChangeSupport.addPropertyChangeListener(listener);
  }

  void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertyChangeSupport.removePropertyChangeListener(listener);
  }

  protected final void firePropertyChanged(String property, Object oldValue, Object newValue) {
    m_propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Check
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is returns <code>true</code> if we have enough information for creating new source.
   */
  public abstract IStatus getStatus();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns editable source that will be used later for editing new source.
   *
   * @param o
   *          the parameters created using {@link #createParametersObject()}
   */
  public abstract IEditableSource createEditableSource(Object o) throws Exception;

  /**
   * Returns some object that will be passed later to the static method of source class for adding
   * new source.
   */
  public abstract Object createParametersObject() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Result is piece of Java code that can be displayed to show how this source style looks like.
   */
  public abstract String getSample();
}
