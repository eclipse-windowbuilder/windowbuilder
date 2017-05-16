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
package org.eclipse.wb.internal.core.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Maps given Eclipse boolean preference to System.property value.
 *
 * @author mitin_aa
 */
public final class PreferenceToSystemForwarder {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PreferenceToSystemForwarder(IPreferenceStore preferenceStore,
      final String preference,
      final String property) {
    preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(preference)) {
          updateProperty(property, event.getNewValue());
        }
      }
    });
    updateProperty(property, preferenceStore.getBoolean(preference));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  private void updateProperty(String property, Object newValue) {
    System.setProperty(property, newValue.toString());
  }
}
