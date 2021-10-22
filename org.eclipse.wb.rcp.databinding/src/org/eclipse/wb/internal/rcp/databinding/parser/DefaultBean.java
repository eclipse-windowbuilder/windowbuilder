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
package org.eclipse.wb.internal.rcp.databinding.parser;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Default bean for replace user wrong beans.
 *
 * @author lobas_av
 * @coverage bindings.rcp.parser
 */
public final class DefaultBean {
  public static final DefaultBean INSTANCE = new DefaultBean();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addPropertyChangeListener(PropertyChangeListener listener) {
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simple property.
   */
  public String getFoo() {
    return "";
  }

  /**
   * List property.
   */
  public List<?> getFooList() {
    return Collections.EMPTY_LIST;
  }

  /**
   * Set property.
   */
  public Set<?> getFooSet() {
    return Collections.EMPTY_SET;
  }
}