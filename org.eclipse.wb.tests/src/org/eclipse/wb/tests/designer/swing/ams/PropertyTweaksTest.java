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
package org.eclipse.wb.tests.designer.swing.ams;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * We should tweak properties for AMS components: group them and change {@link PropertyCategory}.
 *
 * @author scheglov_ke
 */
public class PropertyTweaksTest extends SwingGefTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Disabled
	@Test
	public void test_Groups_fromBundle() throws Exception {
		prepareParse_MyButton();
		assertEquals(2, m_propertyTable.forTests_getPropertiesCount());
		// check "AMS" group
		{
			Property propertyAMS = m_propertyTable.forTests_getProperty(0);
			assertEquals("AMS", propertyAMS.getTitle());
			Property[] subProperties = getSubProperties(propertyAMS);
			assertNotNull(PropertyUtils.getByTitle(subProperties, "buttonColor"));
		}
		// check "Other" group
		{
			Property propertyOther = m_propertyTable.forTests_getProperty(1);
			assertEquals("Other", propertyOther.getTitle());
			Property[] subProperties = getSubProperties(propertyOther);
			assertNotNull(PropertyUtils.getByTitle(subProperties, "text"));
		}
	}

	@DisposeProjectAfter
	@Disabled
	@Test
	public void test_Groups_fromJar() throws Exception {
		// add JAR
		{
			String jarPath =
					TestUtils.createTemporaryJar(
							"wbp-meta/AMS.property-tweaks.xml",
							getSource(
									"<groups>",
									"  <group name='ALL'>",
									"    <other-properties/>",
									"  </group>",
									"</groups>"));
			ProjectUtils.addExternalJar(m_javaProject, jarPath, null);
		}
		// parse
		prepareParse_MyButton();
		assertEquals(1, m_propertyTable.forTests_getPropertiesCount());
		// check "ALL" group
		{
			Property propertyALL = m_propertyTable.forTests_getProperty(0);
			assertEquals("ALL", propertyALL.getTitle());
			Property[] subProperties = getSubProperties(propertyALL);
			assertNotNull(PropertyUtils.getByTitle(subProperties, "buttonColor"));
		}
	}

	@Disabled
	@Test
	public void test_categories() throws Exception {
		prepareParse_MyButton();
		// expand "Other" group
		m_propertyTable.forTests_expand(1);
		// "Variable" property should be marked as "advanced-really", not not visible
		assertFalse(hasPropertyWithTitle("Variable"));
		// enabled "advanced" displaying, so show "Variable" property
		m_propertyTable.setShowAdvancedProperties(true);
		assertTrue(hasPropertyWithTitle("Variable"));
	}

	private boolean hasPropertyWithTitle(String title) {
		int count = m_propertyTable.forTests_getPropertiesCount();
		for (int i = 0; i < count; i++) {
			Property property = m_propertyTable.forTests_getProperty(i);
			if (property.getTitle().equals(title)) {
				return true;
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private ComponentInfo prepareParse_MyButton() throws Exception {
		prepare_MyButton();
		ComponentInfo button = parse_MyButton();
		canvas.select(button);
		return button;
	}

	private void prepare_MyButton() throws Exception {
		setFileContentSrc(
				"ams/zpointcs/MyButton.java",
				getSource(
						"package ams.zpointcs;",
						"import java.awt.*;",
						"import javax.swing.*;",
						"public class MyButton extends JButton {",
						"  public void setButtonColor(Color color) {",
						"  }",
						"}"));
		waitForAutoBuild();
	}

	private ComponentInfo parse_MyButton() throws Exception {
		ContainerInfo panel = openContainer("""
				import ams.zpointcs.MyButton;
				public class Test extends JPanel {
					public Test() {
						MyButton button = new MyButton();
						add(button);
					}
				}""");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		return button;
	}
}