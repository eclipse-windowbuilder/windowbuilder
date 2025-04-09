/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
