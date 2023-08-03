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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.model.layout.gbl.IPreferenceConstants;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.eclipse.jface.preference.IPreferenceStore;

import org.junit.After;
import org.junit.Before;

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
	@Before
	public void setUp() throws Exception {
		super.setUp();
		configureForTest();
	}

	@Override
	@After
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
