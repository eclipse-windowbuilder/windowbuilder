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
package org.eclipse.wb.tests.draw2d;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author lobas_av
 */


@RunWith(Suite.class)
@SuiteClasses({
	// Border
	MarginBorderTest.class,
	LineBorderTest.class,
	CompoundBorderTest.class,
	// Figure
	FigureTest.class,
	FigurePaintingTest.class,
	LabelTest.class,
	PolylineTest.class,
	LayerTest.class,
	RootFigureTest.class,
	// events
	FigureEventTest.class,
})
public class Draw2dTests {
}