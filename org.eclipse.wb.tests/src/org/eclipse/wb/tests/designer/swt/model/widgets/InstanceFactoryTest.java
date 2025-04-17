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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryContainerInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.Test;

import java.util.List;

/**
 * Test for instance factory and eSWT.
 *
 * @author scheglov_ke
 */
public class InstanceFactoryTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// parse
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_parse() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  public Button createButton(Composite parent, String text) {",
						"    Button button = new Button(parent, SWT.NONE);",
						"    button.setText(text);",
						"    return button;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  private InstanceFactory m_factory = new InstanceFactory();",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Button button = m_factory.createButton(this, 'button');",
						"  }",
						"}");
		// check for InstanceFactoryInfo
		{
			InstanceFactoryContainerInfo factoryContainer = InstanceFactoryContainerInfo.get(shell);
			List<InstanceFactoryInfo> factories = factoryContainer.getChildrenFactory();
			assertEquals(1, factories.size());
			assertEquals(
					"test.InstanceFactory",
					factories.get(0).getDescription().getComponentClass().getName());
		}
		// check for Button
		ControlInfo button = shell.getChildrenControls().get(0);
		assertEquals("m_factory.createButton(this, \"button\")", button.getAssociation().getSource());
		assertInstanceOf(InstanceFactoryCreationSupport.class, button.getCreationSupport());
	}
}