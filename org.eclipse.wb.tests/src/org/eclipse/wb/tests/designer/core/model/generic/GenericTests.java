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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for generic simple/flow containers support.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ContainerObjectValidatorsTest.class,
		FlowContainerModelTest.class,
		FlowContainerGefTest.class,
		FlowContainerLayoutGefTest.class,
		FlowContainerGroupGefTest.class,
		SimpleContainerModelTest.class,
		SimpleContainerGefTest.class,
		SimpleContainerLayoutGefTest.class,
		FlipBooleanPropertyGefTest.class,
		DblClickRunScriptEditPolicyTest.class,
		OpenListenerEditPolicyTest.class
})
public class GenericTests {
}
