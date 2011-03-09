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
package org.eclipse.wb.internal.swing.preferences.event;

import org.eclipse.wb.internal.core.model.property.event.EventsProperty;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.jface.preference.PreferencePage;

/**
 * {@link PreferencePage} for {@link EventsProperty}.
 * 
 * @author scheglov_ke
 * @coverage swing.preferences.ui
 */
public final class EventsPreferencePage
    extends
      org.eclipse.wb.internal.core.preferences.event.EventsPreferencePage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EventsPreferencePage() {
    super(Activator.getDefault().getPreferenceStore());
  }
}
