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
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link EditorWarning}.
 *
 * @author scheglov_ke
 */
public class EditorWarningTest extends DesignerTestCase {
	@Test
	public void test() throws Exception {
		String message = "message";
		Exception exception = new Exception();
		EditorWarning warning = new EditorWarning(message, exception);
		assertSame(message, warning.getMessage());
		assertSame(exception, warning.getException());
	}
}
