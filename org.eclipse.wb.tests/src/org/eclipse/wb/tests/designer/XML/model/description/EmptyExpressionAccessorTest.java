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
package org.eclipse.wb.tests.designer.XML.model.description;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.property.accessor.EmptyExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;

/**
 * Test for {@link EmptyExpressionAccessor}.
 * 
 * @author scheglov_ke
 */
public class EmptyExpressionAccessorTest extends AbstractCoreTest {
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
  // Types
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    ExpressionAccessor accessor = EmptyExpressionAccessor.INSTANCE;
    assertSame(Property.UNKNOWN_VALUE, accessor.getValue(null));
    assertSame(Property.UNKNOWN_VALUE, accessor.getDefaultValue(null));
  }
}