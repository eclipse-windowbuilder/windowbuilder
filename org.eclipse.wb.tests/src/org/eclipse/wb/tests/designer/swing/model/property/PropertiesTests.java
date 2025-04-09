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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for Swing properties.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		FontPropertyEditorTest.class,
		IconPropertyEditorTest.class,
		ImagePropertyEditorTest.class,
		BorderPropertyEditorTest.class,
		TabOrderPropertyTest.class,
		TabOrderPropertyValueTest.class,
		BeanPropertyEditorTest.class
})
public class PropertiesTests {
}