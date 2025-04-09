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
package org.eclipse.wb.tests.designer.swing.model.layout.gef;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

/**
 * Abstract super class for testing {@link LayoutEditPolicy}.
 *
 * @author scheglov_ke
 */
public abstract class AbstractLayoutPolicyTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Layout testing utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Check for drop given layout on root container.
	 */
	protected final void check_setLayout(String source,
			String layoutClassName,
			String source2,
			int clickOffsetX,
			int clickOffsetY) throws Exception {
		openContainer(source);
		//
		loadCreationTool(layoutClassName);
		canvas.moveTo(m_contentEditPart, clickOffsetX, clickOffsetY);
		canvas.assertFeedbackFigures(1);
		waitEventLoop(10);
		//
		canvas.click();
		canvas.assertNoFeedbackFigures();
		waitEventLoop(10);
		//
		assertEditor(source2);
	}
}
