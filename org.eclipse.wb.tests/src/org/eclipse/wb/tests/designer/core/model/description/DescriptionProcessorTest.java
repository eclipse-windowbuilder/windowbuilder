/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.IDescriptionProcessor;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Test;

/**
 * Tests for loading of {@link IDescriptionProcessor}'s.
 *
 * @author scheglov_ke
 */
public class DescriptionProcessorTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Dynamic extensions
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ComponentDescriptionHelper#getDescriptionProcessors()}.
	 */
	@Test
	public void test_getDescriptionProcessors() throws Exception {
		// dynamic processor is not yet registered, so can not be found
		assertNull(getMyProcessor());
		// add dynamic processor and re-load
		{
			addProcessorExtension(MyDescriptionProcessor.class.getName());
			try {
				IDescriptionProcessor myProcessor = getMyProcessor();
				assertNotNull(myProcessor);
				// do process
				assertEquals(0, MyDescriptionProcessor.m_processCount);
				myProcessor.process(null, null);
				assertEquals(1, MyDescriptionProcessor.m_processCount);
			} finally {
				removeProcessorExtension();
			}
		}
		// dynamic processor is unloaded, so can not be found
		assertNull(getMyProcessor());
	}

	/**
	 * @return the {@link MyDescriptionProcessor} instance loaded by
	 *         {@link ComponentDescriptionHelper}.
	 */
	private IDescriptionProcessor getMyProcessor() {
		String myProcessorName = MyDescriptionProcessor.class.getName();
		IDescriptionProcessor myProcessor = null;
		// check all processors
		for (IDescriptionProcessor processor : ComponentDescriptionHelper.getDescriptionProcessors()) {
			if (myProcessorName.equals(processor.getClass().getName())) {
				myProcessor = processor;
				break;
			}
		}
		//
		return myProcessor;
	}

	/**
	 * Test implementation of {@link IDescriptionProcessor}.
	 *
	 * @author scheglov_ke
	 */
	public static final class MyDescriptionProcessor implements IDescriptionProcessor {
		private static int m_processCount;

		@Override
		public void process(AstEditor editor, ComponentDescription componentDescription)
				throws Exception {
			m_processCount++;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dynamic IDescriptionProcessor extension support
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String POINT_ID = "org.eclipse.wb.core.descriptionProcessors";

	/**
	 * Adds dynamic {@link IDescriptionProcessor} extension.
	 *
	 * @param className
	 *          the name of {@link IDescriptionProcessor} class.
	 */
	private static void addProcessorExtension(String className) throws Exception {
		String contribution = "  <processor class='" + className + "'/>";
		TestUtils.addDynamicExtension(POINT_ID, contribution);
	}

	/**
	 * Removes dynamic {@link IDescriptionProcessor} extension.
	 */
	protected static void removeProcessorExtension() throws Exception {
		TestUtils.removeDynamicExtension(POINT_ID);
	}
}
