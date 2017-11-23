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
package org.eclipse.wb.tests.designer.rcp.description;

import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;
import org.eclipse.wb.internal.rcp.RcpDescriptionVersionsProviderFactory;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import java.util.List;

/**
 * Test for {@link RcpDescriptionVersionsProviderFactory}.
 *
 * @author scheglov_ke
 */
public class RcpDescriptionVersionsProviderFactoryTest extends RcpModelTest {
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
  @DisposeProjectAfter
  public void test_notRCP() throws Exception {
    do_projectDispose();
    do_projectCreate();
    parseSource(
        "test",
        "Test.java",
        getSource(
            "import javax.swing.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}"));
    // check IDescriptionVersionsProviderFactory
    IDescriptionVersionsProviderFactory providerFactory =
        RcpDescriptionVersionsProviderFactory.INSTANCE;
    IDescriptionVersionsProvider provider =
        providerFactory.getProvider(m_javaProject, m_lastLoader);
    // not RCP project, so RCP factory returns no provider
    assertNull(provider);
    // also no versions
    assertThat(providerFactory.getVersions(m_javaProject, m_lastLoader)).isEmpty();
  }

  public void test_37() throws Exception {
    parseComposite(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
    // check IDescriptionVersionsProviderFactory
    IDescriptionVersionsProviderFactory providerFactory =
        RcpDescriptionVersionsProviderFactory.INSTANCE;
    IDescriptionVersionsProvider provider =
        providerFactory.getProvider(m_javaProject, m_lastLoader);
    // RCP class: Button
    {
      Class<?> componentClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Button");
      List<String> versions = provider.getVersions(componentClass);
      assertThat(versions).containsExactly("3.7", "3.6", "3.5", "3.4", "3.3", "3.2");
    }
    // RCP class: TableViewer
    {
      Class<?> componentClass = m_lastLoader.loadClass("org.eclipse.jface.viewers.TableViewer");
      List<String> versions = provider.getVersions(componentClass);
      assertThat(versions).containsExactly("3.7", "3.6", "3.5", "3.4", "3.3", "3.2");
    }
    // not RCP class
    {
      List<String> versions = provider.getVersions(Object.class);
      assertThat(versions).isEmpty();
    }
    // check versions
    assertThat(providerFactory.getVersions(m_javaProject, m_lastLoader)).contains(
        entry("rcp_version", "3.7"));
    assertThat(m_lastState.getVersions()).contains(entry("rcp_version", "3.7"));
  }
}