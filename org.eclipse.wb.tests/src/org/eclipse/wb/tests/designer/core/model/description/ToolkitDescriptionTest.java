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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@link ToolkitDescription}.
 *
 * @author scheglov_ke
 */
public class ToolkitDescriptionTest extends DesignerTestCase {
	/**
	 * Test for {@link ToolkitDescription}.
	 */
	@Test
	public void test_ToolkitDescription() throws Exception {
		ToolkitDescriptionJava toolkit = org.eclipse.wb.internal.swing.ToolkitProvider.DESCRIPTION;
		assertEquals(
				org.eclipse.wb.internal.swing.preferences.IPreferenceConstants.TOOLKIT_ID,
				toolkit.getId());
		assertEquals("Swing toolkit", toolkit.getName());
		assertEquals(Platform.getBundle("org.eclipse.wb.swing"), toolkit.getBundle());
		assertSame(
				org.eclipse.wb.internal.swing.Activator.getDefault().getPreferenceStore(),
				toolkit.getPreferences());
		{
			GenerationSettings generationSettings = toolkit.getGenerationSettings();
			assertNotNull(generationSettings);
			Assertions.assertThat(generationSettings.getVariables()).isNotEmpty();
		}
	}

	/**
	 * Test for {@link ComponentDescriptionHelper#getToolkitElements()}.
	 */
	@Test
	public void test_getToolkitElements() throws Exception {
		Set<String> toolkitIds = new HashSet<>();
		{
			List<IConfigurationElement> toolkitElements = DescriptionHelper.getToolkitElements();
			for (IConfigurationElement toolkitElement : toolkitElements) {
				toolkitIds.add(toolkitElement.getAttribute("id"));
			}
		}
		assertTrue(toolkitIds.contains(org.eclipse.wb.internal.core.preferences.IPreferenceConstants.TOOLKIT_ID));
		assertTrue(toolkitIds.contains(org.eclipse.wb.internal.swing.preferences.IPreferenceConstants.TOOLKIT_ID));
		assertFalse(toolkitIds.contains("no-such-toolkit-id"));
	}

	/**
	 * Test for {@link ComponentDescriptionHelper#getToolkitElements(String)}.
	 */
	@Test
	public void test_getToolkitElements_forSingleToolkit() throws Exception {
		String toolkitId = org.eclipse.wb.internal.swing.preferences.IPreferenceConstants.TOOLKIT_ID;
		for (IConfigurationElement toolkitElement : DescriptionHelper.getToolkitElements(toolkitId)) {
			assertEquals(toolkitId, ExternalFactoriesHelper.getRequiredAttribute(toolkitElement, "id"));
		}
	}

	/**
	 * Test for {@link ComponentDescriptionHelper#getToolkit(String)}.
	 */
	@Test
	public void test_getToolkit() throws Exception {
		// check for existing toolkit
		assertSame(
				org.eclipse.wb.internal.swing.ToolkitProvider.DESCRIPTION,
				DescriptionHelper.getToolkit(org.eclipse.wb.internal.swing.preferences.IPreferenceConstants.TOOLKIT_ID));
		// check for not existing toolkit
		try {
			DescriptionHelper.getToolkit("no-such-toolkit-id");
			fail();
		} catch (AssertionFailedException e) {
		}
	}

	/**
	 * Test for {@link ComponentDescriptionHelper#getToolkits()}.
	 */
	@Test
	public void test_getToolkits() throws Exception {
		ToolkitDescription[] toolkits = DescriptionHelper.getToolkits();
		Assertions.assertThat(toolkits).contains(
				org.eclipse.wb.internal.core.ToolkitProvider.DESCRIPTION,
				org.eclipse.wb.internal.swing.ToolkitProvider.DESCRIPTION,
				org.eclipse.wb.internal.rcp.ToolkitProvider.DESCRIPTION);
	}
}
