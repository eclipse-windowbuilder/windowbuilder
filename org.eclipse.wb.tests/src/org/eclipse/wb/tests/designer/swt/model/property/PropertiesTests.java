/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for SWT properties.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ConvertersTest.class,
		ColorPropertyEditorTestNoManager.class,
		ColorPropertyEditorTestWithManager.class,
		ColorPropertyEditorTestRegistry.class,
		ImagePropertyEditorTestNoManager.class,
		ImagePropertyEditorTestWithManager.class,
		ImagePropertyEditorTestPlugin.class,
		ImageDescriptorPropertyEditorTestNoManager.class,
		ImageDescriptorPropertyEditorTestWithManager.class,
		ImageDescriptorPropertyEditorTestPlugin.class,
		FontPropertyEditorTestNoManager.class,
		FontPropertyEditorTestWithManager.class,
		FontPropertyEditorTestRegistry.class,
		ResourceRegistryTest.class,
		SWTResourceManagerTest.class,
		ResourceManagerTest.class,
		LocalResourceManagerTest.class,
		TabOrderPropertyTest.class
})
public class PropertiesTests {
}