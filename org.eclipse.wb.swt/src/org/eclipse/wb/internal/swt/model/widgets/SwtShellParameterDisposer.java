/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Support for disposing {@link Shell} parameters for unknown {@link Composite} parameters, created
 * by {@link SwtMethodParameterEvaluator}.
 *
 * @author scheglov_ke
 * @coverage swt.model.widgets
 */
public final class SwtShellParameterDisposer implements IRootProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IRootProcessor INSTANCE = new SwtShellParameterDisposer();

	private SwtShellParameterDisposer() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IRootProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
		root.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void dispose() throws Exception {
				CompilationUnit unit = root.getEditor().getAstUnit();
				disposeShellParameters(unit);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static void disposeShellParameters(CompilationUnit unit) throws Exception {
		unit.accept(new ASTVisitor() {
			@Override
			public void postVisit(ASTNode node) {
				String key = SwtMethodParameterEvaluator.SHELL_KEY;
				Object shell = node.getProperty(key);
				if (shell instanceof Shell shellObject && !shellObject.isDisposed()) {
					shellObject.dispose();
					node.setProperty(key, null);
				}
			}
		});
	}
}
