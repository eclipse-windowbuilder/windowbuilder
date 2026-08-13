/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.rcp.description;

import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;
import org.eclipse.wb.internal.rcp.RcpDescriptionVersionsProviderFactory;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link RcpDescriptionVersionsProviderFactory}.
 *
 * @author scheglov_ke
 */
public class RcpDescriptionVersionsProviderFactoryTest extends RcpModelTest {
	private List<String> m_versions;

	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		m_versions = new ArrayList<>();
		m_versions.addAll(List.of("4.9", "4.8", "4.7", "4.6", "4.5", "4.4", "4.3", "4.2", "3.8", "3.7"));
		if (SWT.getVersion() > 4972) { // For compatibility with 2024-06 release
			m_versions.add(0, "4.10");
		}
		if (SWT.getVersion() > 4973) {
			m_versions.add(0, "4.11");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@DisposeProjectAfter
	@Test
	public void test_notRCP() throws Exception {
		do_projectDispose();
		do_projectCreate();
		parseSource("test", "Test.java", getSource("""
				import javax.swing.*;
				public class Test extends JPanel {
					public Test() {
					}
				}"""));
		// check IDescriptionVersionsProviderFactory
		IDescriptionVersionsProviderFactory providerFactory =
				RcpDescriptionVersionsProviderFactory.INSTANCE;
		IDescriptionVersionsProvider provider =
				providerFactory.getProvider(m_javaProject, m_lastLoader);
		// not RCP project, so RCP factory returns no provider
		assertNull(provider);
		// also no versions
		assertTrue(providerFactory.getVersions(m_javaProject, m_lastLoader).isEmpty(),
				"Component descriptions unsupported for non-RCP classes ");
	}

	@Test
	public void test_getDescriptionVersions() throws Exception {
		parseComposite("""
				// filler filler filler
				public class Test extends Shell {
					public Test() {
					}
				}""");
		// check IDescriptionVersionsProviderFactory
		IDescriptionVersionsProviderFactory providerFactory =
				RcpDescriptionVersionsProviderFactory.INSTANCE;
		IDescriptionVersionsProvider provider =
				providerFactory.getProvider(m_javaProject, m_lastLoader);
		// RCP class: Button
		{
			Class<?> componentClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Button");
			List<String> versions = provider.getVersions(componentClass);
			assertEquals(m_versions, versions);
		}
		// RCP class: TableViewer
		{
			Class<?> componentClass = m_lastLoader.loadClass("org.eclipse.jface.viewers.TableViewer");
			List<String> versions = provider.getVersions(componentClass);
			assertEquals(m_versions, versions);
		}
		// not RCP class
		{
			List<String> versions = provider.getVersions(Object.class);
			assertTrue(versions.isEmpty(), "Component descriptions unsupported for non-RCP classes ");
		}
		// check versions
		assertEquals(m_versions.getFirst(), providerFactory.getVersions(m_javaProject, m_lastLoader).get("rcp_version"));
		assertEquals(m_versions.getFirst(), m_lastState.getVersions().get("rcp_version"));
	}
}