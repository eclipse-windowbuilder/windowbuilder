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
package org.eclipse.wb.tests.designer.XML.model.generic;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;

/**
 * Tests for "flow container" support, such as {@link FlowContainer} interface.
 *
 * @author scheglov_ke
 */
public class FlowContainerGefTest extends FlowContainerAbstractGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void prepareFlowPanel() throws Exception {
		FlowContainerModelTest.prepareFlowPanel();
	}
}
