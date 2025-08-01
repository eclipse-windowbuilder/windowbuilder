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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.model.layout.gbl.IPreferenceConstants;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.eclipse.jface.preference.IPreferenceStore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.awt.GridBagLayout;

/**
 * Test for {@link GridBagLayout}.
 *
 * @author scheglov_ke
 */
abstract class AbstractGridBagLayoutTest extends AbstractLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		configureForTest();
	}

	@Override
	@AfterEach
	public void tearDown() throws Exception {
		configureDefaults();
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures default preferences for {@link GridBagLayout} in tests.
	 */
	public static void configureForTest() {
		IPreferenceStore preferences = SwingToolkitDescription.INSTANCE.getPreferences();
		preferences.setValue(IPreferenceConstants.P_GBC_LONG, false);
		preferences.setValue(
				IPreferenceConstants.P_CONSTRAINTS_NAME_TEMPLATE,
				SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT);
	}

	/**
	 * Restores default preferences for {@link GridBagLayout}.
	 */
	public static void configureDefaults() {
		IPreferenceStore preferences = SwingToolkitDescription.INSTANCE.getPreferences();
		preferences.setToDefault(IPreferenceConstants.P_CONSTRAINTS_NAME_TEMPLATE);
		preferences.setToDefault(IPreferenceConstants.P_GBC_LONG);
	}
}
