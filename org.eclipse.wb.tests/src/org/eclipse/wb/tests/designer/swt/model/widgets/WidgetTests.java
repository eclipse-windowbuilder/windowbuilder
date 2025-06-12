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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for SWT widgets models.
 *
 * @author lobas_av
 */
@Suite
@SelectClasses({
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