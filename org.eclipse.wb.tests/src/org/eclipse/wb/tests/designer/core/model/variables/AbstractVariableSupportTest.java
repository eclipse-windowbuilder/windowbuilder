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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

import net.bytebuddy.ByteBuddy;

import static org.mockito.Mockito.mock;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;

import java.util.ArrayList;
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
	 * Create instance of {@link VariableSupport} using ByteBuddy, because {@link VariableSupport} is
	 * abstract, and check its methods.
	 */
	@Test
	public void test() throws Exception {
		JavaInfo javaInfo = mock(JavaInfo.class);
		// create abstract VariableSupport instance
		VariableSupport variableSupport;
		{
			variableSupport = new ByteBuddy() //
					.subclass(VariableSupport.class) //
					.make() //
					.load(getClass().getClassLoader()) //
					.getLoaded() //
					.getConstructor(JavaInfo.class) //
					.newInstance(javaInfo);
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
			List<Property> properties = new ArrayList<>();
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
