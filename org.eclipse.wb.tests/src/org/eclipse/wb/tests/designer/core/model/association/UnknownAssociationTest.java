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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.UnknownAssociation;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Tests for {@link UnknownAssociation}.
 * 
 * @author scheglov_ke
 */
public class UnknownAssociationTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test() throws Exception {
    UnknownAssociation association = new UnknownAssociation();
    assertFalse(association.canDelete());
    assertNull(association.getStatement());
  }
}
