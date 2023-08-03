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