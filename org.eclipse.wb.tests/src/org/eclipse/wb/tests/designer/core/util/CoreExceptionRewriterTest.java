/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.internal.core.editor.errors.CoreExceptionRewriter;
import org.eclipse.wb.internal.core.editor.errors.CoreExceptionRewriter2;
import org.eclipse.wb.internal.core.eval.evaluators.AnonymousEvaluationError;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.os.OSSupportError;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link CoreExceptionRewriter}.
 *
 * @author scheglov_ke
 */
public class CoreExceptionRewriterTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link CoreExceptionRewriter} and {@link OSSupportError}.
	 */
	@Test
	public void test_OSSupportError() throws Exception {
		Throwable e = new OSSupportError("foo");
		DesignerException rewritten = (DesignerException) CoreExceptionRewriter.INSTANCE.rewrite(e);
		assertEquals(OSSupportError.ERROR_CODE, rewritten.getCode());
	}

	/**
	 * Test for {@link CoreExceptionRewriter} and {@link AnonymousEvaluationError}.
	 */
	@Test
	public void test_AnonymousEvaluationError() throws Exception {
		Throwable anError = new AnonymousEvaluationError();
		// rewrite as is
		{
			DesignerException rewritten =
					(DesignerException) CoreExceptionRewriter2.INSTANCE.rewrite(anError);
			assertEquals(ICoreExceptionConstants.EVAL_ANONYMOUS, rewritten.getCode());
		}
		// rewrite when wrapped into Error
		{
			Throwable wrapper = new Error("msg", anError);
			DesignerException rewritten =
					(DesignerException) CoreExceptionRewriter2.INSTANCE.rewrite(wrapper);
			assertEquals(ICoreExceptionConstants.EVAL_ANONYMOUS, rewritten.getCode());
			assertArrayEquals(rewritten.getParameters(), new String[] { "msg" });
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isIncompleteProductException
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean isIncompleteProductException(Throwable e) {
		Throwable rewritten = CoreExceptionRewriter.INSTANCE.rewrite(e);
		if (rewritten instanceof DesignerException designerException) {
			return designerException.getCode() == ICoreExceptionConstants.INCOMPLETE_PRODUCT;
		}
		return false;
	}

	/**
	 * Test for {@link CoreExceptionRewriter} and incomplete installation.
	 */
	@Test
	public void test_isIncompleteProductException_differentException() throws Exception {
		Throwable e = new Exception();
		assertFalse(isIncompleteProductException(e));
	}

	/**
	 * Test for {@link CoreExceptionRewriter} and incomplete installation.
	 * <p>
	 * Support for {@link NoClassDefFoundError}.
	 */
	@Test
	public void test_isIncompleteProductException_NoClassDefFoundError() throws Exception {
		// some other class
		{
			Throwable e = new NoClassDefFoundError("not/designer/Class");
			assertFalse(isIncompleteProductException(e));
		}
		// Designer
		{
			Throwable e = new NoClassDefFoundError("org/eclipse/wb/Foo");
			assertTrue(isIncompleteProductException(e));
			// even wrapped
			e = new Error(e);
			assertTrue(isIncompleteProductException(e));
		}
		// JDT
		{
			Throwable e = new NoClassDefFoundError("org/eclipse/jdt/core/dom/AnonymousTypeDeclaration2");
			assertTrue(isIncompleteProductException(e));
			// even wrapped
			e = new Error(e);
			assertTrue(isIncompleteProductException(e));
		}
	}

	/**
	 * Test for {@link CoreExceptionRewriter} and incomplete installation.
	 * <p>
	 * Support for {@link NoSuchMethodError}.
	 */
	@Test
	public void test_isIncompleteProductException_NoSuchMethodError() throws Exception {
		// some other method
		{
			Throwable e = new NoSuchMethodError("not.designer.Class.method()V");
			assertFalse(isIncompleteProductException(e));
		}
		// Designer
		{
			Throwable e = new NoSuchMethodError("org.eclipse.wb.Foo.bar()V");
			assertTrue(isIncompleteProductException(e));
			// even wrapped
			e = new Error(e);
			assertTrue(isIncompleteProductException(e));
		}
	}
}
