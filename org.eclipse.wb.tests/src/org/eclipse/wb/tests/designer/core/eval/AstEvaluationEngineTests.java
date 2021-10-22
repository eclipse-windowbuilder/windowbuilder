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
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link AstEvaluationEngine}.
 *
 * @author scheglov_ke
 */
public class AstEvaluationEngineTests extends DesignerSuiteTests {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Suite
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.eval");
    suite.addTest(createSingleSuite(EngineTest.class));
    suite.addTest(createSingleSuite(BooleanTest.class));
    suite.addTest(createSingleSuite(CharTest.class));
    suite.addTest(createSingleSuite(IntegerTest.class));
    suite.addTest(createSingleSuite(LongTest.class));
    suite.addTest(createSingleSuite(FloatTest.class));
    suite.addTest(createSingleSuite(DoubleTest.class));
    suite.addTest(createSingleSuite(CastTest.class));
    suite.addTest(createSingleSuite(ClassTest.class));
    suite.addTest(createSingleSuite(StringTest.class));
    suite.addTest(createSingleSuite(ArrayTest.class));
    suite.addTest(createSingleSuite(FieldTest.class));
    suite.addTest(createSingleSuite(ExecutionFlowUtilsTest.class));
    suite.addTest(createSingleSuite(ExecutionFlowUtils2Test.class));
    suite.addTest(createSingleSuite(MethodInvocationTest.class));
    return suite;
  }
}
