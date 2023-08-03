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
package org.eclipse.wb.tests.designer.rcp.model.layout.form;

import org.eclipse.wb.tests.designer.rcp.model.layout.form.gef.FormLayoutAlignmentTest;
import org.eclipse.wb.tests.designer.rcp.model.layout.form.gef.FormLayoutMoveTest;

import org.eclipse.swt.layout.FormLayout;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link FormLayout}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 */
@RunWith(Suite.class)
@SuiteClasses({
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