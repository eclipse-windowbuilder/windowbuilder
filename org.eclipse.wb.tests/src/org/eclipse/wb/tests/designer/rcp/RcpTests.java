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
package org.eclipse.wb.tests.designer.rcp;

import org.eclipse.wb.tests.designer.databinding.rcp.BindingTests;
import org.eclipse.wb.tests.designer.rcp.description.DescriptionTests;
import org.eclipse.wb.tests.designer.rcp.gef.GefTests;
import org.eclipse.wb.tests.designer.rcp.model.ModelTests;
import org.eclipse.wb.tests.designer.rcp.nebula.NebulaTests;
import org.eclipse.wb.tests.designer.rcp.resource.ResourceTests;
import org.eclipse.wb.tests.designer.rcp.swing2swt.Swing2SwtTests;
import org.eclipse.wb.tests.designer.rcp.wizard.WizardTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * All RCP tests.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
	ModelTests.class,
	DescriptionTests.class,
	ResourceTests.class,
	NebulaTests.class,
	Swing2SwtTests.class,
	GefTests.class,
	BindingTests.class,
	WizardTests.class
})
public class RcpTests {
}
