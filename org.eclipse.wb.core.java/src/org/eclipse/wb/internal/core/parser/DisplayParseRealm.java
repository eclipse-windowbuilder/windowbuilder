/*******************************************************************************
 * Copyright (c) 2026 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.parser;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.swt.SwtCallable;
import org.eclipse.swt.widgets.Display;

/**
 * Default implementation that executes the callable in the SWT UI thread.
 */
public class DisplayParseRealm implements IParseRealm {
	private static IParseRealm INSTANCE = new DisplayParseRealm();

	private DisplayParseRealm() {
	}

	public static IParseRealm getRealm() {
		return INSTANCE;
	}

	@Override
	public <T> T syncCall(SwtCallable<T, Exception> c) throws Exception {
		return DesignerPlugin.getStandardDisplay().syncCall(c);
	}

	@Override
	public boolean isCurrent() {
		return Display.getCurrent() != null;
	}
}
