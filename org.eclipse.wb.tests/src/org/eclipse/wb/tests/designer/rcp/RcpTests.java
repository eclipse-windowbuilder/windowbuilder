/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All RCP tests.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
	ModelTests.class,
	DescriptionTests.class,
	ResourceTests.class,
	NebulaTests.class,
	Swing2SwtTests.class,
	GefTests.class,
	BindingTests.class
})
public class RcpTests {
}
