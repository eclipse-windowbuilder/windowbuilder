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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import com.jgoodies.forms.layout.FormLayout;

import org.osgi.framework.Bundle;

/**
 * Abstract test for {@link FormLayout}.
 * 
 * @author scheglov_ke
 */
public class AbstractFormLayoutTest extends AbstractLayoutTest {
  protected boolean m_useFormsImports = true;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    LafSupport.applySelectedLAF(LafSupport.getDefaultLAF());
  }

  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    do_configureNewProject();
  }

  static void do_configureNewProject() throws Exception {
    Bundle libBundle = Platform.getBundle("org.eclipse.wb.swing.FormLayout.lib");
    String path = FileLocator.toFileURL(libBundle.getEntry("/forms-1.3.0.jar")).getPath();
    m_testProject.addExternalJar(path);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTestSource(String... lines) {
    if (m_useFormsImports) {
      lines =
          CodeUtils.join(new String[]{
              "import com.jgoodies.forms.layout.*;",
              "import com.jgoodies.forms.factories.*;"}, lines);
    }
    return super.getTestSource(lines);
  }
}
