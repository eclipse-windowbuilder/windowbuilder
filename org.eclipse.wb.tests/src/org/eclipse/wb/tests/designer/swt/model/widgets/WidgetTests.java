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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for SWT widgets models.
 *
 * @author lobas_av
 */
@RunWith(Suite.class)
@SuiteClasses({
		WidgetTest.class,
		LiveComponentsManagerTest.class,
		DescriptionProcessorTest.class,
		ControlTest.class,
		LiveImagesManagerTest.class,
		ScrollableTest.class,
		CompositeTopBoundsSupportTest.class,
		CompositeTopBoundsTest.class,
		CompositeTest.class,
		TableTest.class,
		TreeTest.class,
		ThisCompositeTest.class,
		ButtonsTest.class,
		StaticFactoryTest.class,
		InstanceFactoryTest.class
})
public class WidgetTests {
}