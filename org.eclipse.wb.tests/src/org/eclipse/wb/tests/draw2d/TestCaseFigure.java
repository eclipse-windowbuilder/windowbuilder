/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.draw2d.Figure;

import java.util.Iterator;

public class TestCaseFigure extends Figure {
	@Override
	public <T extends Object> Iterator<T> getListeners(Class<T> listenerClass) {
		return super.getListeners(listenerClass);
	}

	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}
}