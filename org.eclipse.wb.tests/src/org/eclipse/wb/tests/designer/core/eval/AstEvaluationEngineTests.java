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
package org.eclipse.wb.tests.designer.core.eval;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.tests.designer.core.eval.other.ArrayTest;
import org.eclipse.wb.tests.designer.core.eval.other.CastTest;
import org.eclipse.wb.tests.designer.core.eval.other.ClassTest;
import org.eclipse.wb.tests.designer.core.eval.other.FieldTest;
import org.eclipse.wb.tests.designer.core.eval.other.StringTest;
import org.eclipse.wb.tests.designer.core.eval.primities.BooleanTest;
import org.eclipse.wb.tests.designer.core.eval.primities.CharTest;
import org.eclipse.wb.tests.designer.core.eval.primities.DoubleTest;
import org.eclipse.wb.tests.designer.core.eval.primities.FloatTest;
import org.eclipse.wb.tests.designer.core.eval.primities.IntegerTest;
import org.eclipse.wb.tests.designer.core.eval.primities.LongTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link AstEvaluationEngine}.
 *
 * @author scheglov_ke
 */

@RunWith(Suite.class)
@SuiteClasses({
	EngineTest.class,
	BooleanTest.class,
	CharTest.class,
	IntegerTest.class,
	LongTest.class,
	FloatTest.class,
	DoubleTest.class,
	CastTest.class,
	ClassTest.class,
	StringTest.class,
	ArrayTest.class,
	FieldTest.class,
	ExecutionFlowUtilsTest.class,
	ExecutionFlowUtils2Test.class,
	MethodInvocationTest.class
})
public class AstEvaluationEngineTests {
}
