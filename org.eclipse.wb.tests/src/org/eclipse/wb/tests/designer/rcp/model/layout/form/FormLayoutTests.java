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
package org.eclipse.wb.tests.designer.rcp.model.layout.form;

import org.eclipse.wb.tests.designer.rcp.model.layout.form.gef.FormLayoutAlignmentTest;
import org.eclipse.wb.tests.designer.rcp.model.layout.form.gef.FormLayoutMoveTest;

import org.eclipse.swt.layout.FormLayout;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link FormLayout}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 */
@Suite
@SelectClasses({
		FormLayoutMoveSingleResizableTest.class,
		FormLayoutMoveSingleWithSingleSideTest.class,
		FormLayoutMoveSingleWithBothSidesTest.class,
		FormLayoutAlignmentDetectionTest.class,
		FormLayoutModelsTest.class,
		FormLayoutMoveTest.class,
		FormLayoutAlignmentTest.class
})
public class FormLayoutTests {
}