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

import org.eclipse.wb.internal.core.model.generic.SimpleContainer;

/**
 * Tests for "simple container" support, such as {@link SimpleContainer} interface.
 * 
 * @author scheglov_ke
 */
public class SimpleContainerGefTest extends SimpleContainerAbstractGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void prepareSimplePanel() throws Exception {
    SimpleContainerModelTest.prepareSimplePanel();
  }
}
