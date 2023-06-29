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
package org.eclipse.wb.tests.designer.swing.model.layout;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jface.preference.IPreferenceStore;

import java.awt.LayoutManager;

public class AbstractLayoutTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		configureForTest();
	}

	@Override
	protected void tearDown() throws Exception {
		configureDefaults();
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures default preferences.
	 */
	public static void configureForTest() {
		IPreferenceStore preferences = SwingToolkitDescription.INSTANCE.getPreferences();
		preferences.setValue(
				IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
				SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT);
	}

	/**
	 * Restores default preferences.
	 */
	public static void configureDefaults() {
		IPreferenceStore preferences = SwingToolkitDescription.INSTANCE.getPreferences();
		preferences.setToDefault(IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the {@link LayoutInfo} with given class.
	 */
	protected static void setLayout(ContainerInfo container,
			Class<? extends LayoutManager> layoutClass) throws Exception {
		LayoutInfo newLayout =
				(LayoutInfo) JavaInfoUtils.createJavaInfo(
						container.getEditor(),
						layoutClass,
						new ConstructorCreationSupport());
		container.setLayout(newLayout);
	}
}
