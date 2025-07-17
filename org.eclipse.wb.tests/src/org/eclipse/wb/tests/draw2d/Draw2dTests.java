/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author lobas_av
 */


@Suite
@SelectClasses({
	// Border
	MarginBorderTest.class,
	LineBorderTest.class,
	CompoundBorderTest.class,
	// Figure
	FigureTest.class,
	FigurePaintingTest.class,
	PolylineTest.class,
	LayerTest.class,
	RootFigureTest.class,
	// events
	FigureEventTest.class,
})
public class Draw2dTests {
}