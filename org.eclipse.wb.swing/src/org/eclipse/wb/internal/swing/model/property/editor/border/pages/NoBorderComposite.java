/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.model.property.editor.border.pages;

import org.eclipse.wb.internal.swing.model.property.editor.border.BorderValue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;

import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * Implementation of {@link AbstractBorderComposite} that sets <code>null</code> {@link Border}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class NoBorderComposite extends AbstractBorderComposite {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NoBorderComposite(Composite parent) {
		super(parent, "(no border)");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompletableFuture<Void> setBorderValue(BorderValue borderValue) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be called from the AWT event dispatcher thread");
		if (borderValue.getValue() == null) {
			return CompletableFuture.completedFuture(null);
		}
		return null;
	}

	@Override
	public String getSource() {
		return "null";
	}
}
