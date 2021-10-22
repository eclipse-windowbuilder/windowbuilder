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
package org.eclipse.wb.tests.designer.core.model.variables;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.apache.commons.lang.NotImplementedException;
import org.easymock.EasyMock;

import java.util.List;

/**
 * Test for abstract {@link VariableSupport}.
 *
 * @author scheglov_ke
 */
public class AbstractVariableSupportTest extends AbstractVariableTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create instance of {@link VariableSupport} using CGLib, because {@link VariableSupport} is
   * abstract, and check its methods.
   */
  public void test() throws Exception {
    JavaInfo javaInfo = EasyMock.createMock(JavaInfo.class);
    // create abstract VariableSupport instance
    VariableSupport variableSupport;
    {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(VariableSupport.class);
      enhancer.setCallback(NoOp.INSTANCE);
      variableSupport =
          (VariableSupport) enhancer.create(new Class[]{JavaInfo.class}, new Object[]{javaInfo});
    }
    // JavaInfo-related methods
    {
      assertSame(javaInfo, variableSupport.getJavaInfo());
      assertFalse(variableSupport.isJavaInfo(null));
      assertFalse(variableSupport.isDefault());
    }
    // getComponentName()
    assertEquals("other", variableSupport.getComponentName());
    // addProperties()
    {
      List<Property> properties = Lists.newArrayList();
      variableSupport.addProperties(properties);
      assertTrue(properties.isEmpty());
    }
    // deleteBefore() and deleteAfter() do nothing
    {
      variableSupport.deleteBefore();
      variableSupport.deleteAfter();
    }
    // isValidStatementForChild() returns "true"
    assertTrue(variableSupport.isValidStatementForChild(null));
    // no ensureInstanceReadyAt()
    try {
      variableSupport.ensureInstanceReadyAt(null);
      fail();
    } catch (NotImplementedException e) {
    }
    // no getAssociationTarget()
    try {
      variableSupport.getAssociationTarget(null);
      fail();
    } catch (NotImplementedException e) {
    }
    // no add_*() methods
    {
      try {
        variableSupport.add_getVariableStatementSource(null);
        fail();
      } catch (NotImplementedException e) {
      }
      try {
        variableSupport.add_setVariableStatement(null);
        fail();
      } catch (NotImplementedException e) {
      }
    }
    // no expression
    assertFalse(variableSupport.hasExpression(null));
  }
}
