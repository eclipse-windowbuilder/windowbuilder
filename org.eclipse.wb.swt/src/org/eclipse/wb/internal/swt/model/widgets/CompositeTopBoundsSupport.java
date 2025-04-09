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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Implementation of {@link TopBoundsSupport} for SWT {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage swt.model.widgets
 */
public abstract class CompositeTopBoundsSupport extends TopBoundsSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CompositeTopBoundsSupport(CompositeInfo composite) {
		super(composite);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void apply() throws Exception {
		// if one these methods is present, we already set size during AST evaluation
		if (hasMethodInvocations(new String[]{
				"setSize(int,int)",
				"setSize(org.eclipse.swt.graphics.Point)",
		"pack()"})) {
			((Composite) m_component.getObject()).layout();
			return;
		}
		// set size from resource properties (or default)
		{
			Dimension size = getResourceSize();
			((Composite) m_component.getObject()).setSize(size.width, size.height);
		}
	}

	@Override
	public void setSize(int width, int height) throws Exception {
		// remember size in resource properties
		setResourceSize(width, height);
		// check for: setSize(org.eclipse.swt.graphics.Point)
		if (setSizePoint("setSize", width, height)) {
			return;
		}
		// prepare source elements
		String widthSource = IntegerConverter.INSTANCE.toJavaSource(m_component, width);
		String heightSource = IntegerConverter.INSTANCE.toJavaSource(m_component, height);
		// check for: setSize(int,int)
		{
			MethodInvocation invocation = m_component.getMethodInvocation("setSize(int,int)");
			if (invocation != null) {
				AstEditor editor = m_component.getEditor();
				List<Expression> arguments = DomGenerics.arguments(invocation);
				editor.replaceExpression(arguments.get(0), widthSource);
				editor.replaceExpression(arguments.get(1), heightSource);
				return;
			}
		}
		// always set size for Shell
		if (m_component instanceof ShellInfo) {
			m_component.addMethodInvocation("setSize(int,int)", widthSource + ", " + heightSource);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Show
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean show() throws Exception {
		Control control = (Control) m_component.getObject();
		show(m_component, control);
		return true;
	}

	/**
	 * Shows given control for testing/preview.
	 */
	public static void show(AbstractComponentInfo component, Control control) throws Exception {
		showBefore();
		try {
			show0(control);
		} finally {
			showAfter();
		}
	}

	/**
	 * Prepares environment for showing control.
	 */
	private static void showBefore() throws Exception {
		Shell eclipseShell = DesignerPlugin.getShell();
		// disable redraw to prevent outstanding paints after preview window closed and disposed
		if (EnvironmentUtils.IS_MAC) {
			eclipseShell.redraw();
			eclipseShell.update();
			eclipseShell.setRedraw(false);
		}
		// disable Shell to prevent its activation before closing preview
		eclipseShell.setEnabled(false);
	}

	/**
	 * Updates environment after showing control.
	 */
	private static void showAfter() throws Exception {
		Shell eclipseShell = DesignerPlugin.getShell();
		if (EnvironmentUtils.IS_MAC) {
			eclipseShell.setRedraw(true);
		}
		eclipseShell.setEnabled(true);
		eclipseShell.forceActive();
	}

	/**
	 * Shows given control for testing/preview, raw.
	 */
	private static void show0(Control control) throws Exception {
		final Shell shell = control.getShell();
		// handle wrapper
		if (control != shell) {
			shell.setText(ModelMessages.CompositeTopBoundsSupport_wrapperShellText);
			shell.setLayout(new FillLayout());
			Rectangle controlBounds = control.getBounds();
			Rectangle shellBounds = shell.computeTrim(0, 0, controlBounds.width, controlBounds.height);
			shell.setSize(shellBounds.width, shellBounds.height);
			shell.layout();
		}
		// close preview by pressing ESC key
		Runnable clearESC = closeOnESC(shell);
		// set location
		{
			Rectangle monitorClientArea = DesignerPlugin.getShell().getMonitor().getClientArea();
			// center on primary Monitor
			int x;
			int y;
			{
				Rectangle shellBounds = shell.getBounds();
				x = monitorClientArea.x + (monitorClientArea.width - shellBounds.width) / 2;
				y = monitorClientArea.y + (monitorClientArea.height - shellBounds.height) / 2;
			}
			// ensure that top-left corner is visible
			x = Math.max(x, monitorClientArea.x + 10);
			y = Math.max(y, monitorClientArea.y + 10);
			// do set location
			shell.setLocation(x, y);
		}
		// show Shell in modal state
		ToolkitSupport.showShell(shell);
		clearESC.run();
	}

	/**
	 * Add the display filter which closes preview by pressing ESC key.
	 */
	private static Runnable closeOnESC(final Shell shell) {
		final Display display = DesignerPlugin.getStandardDisplay();
		final Listener listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.keyCode == SWT.ESC) {
					shell.close();
					event.doit = false;
				}
			}
		};
		// add filter
		display.addFilter(SWT.KeyDown, listener);
		// continuation
		return new Runnable() {
			@Override
			public void run() {
				display.removeFilter(SWT.KeyDown, listener);
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Changes {@link org.eclipse.swt.graphics.Point} argument of given method.
	 *
	 * @return <code>true</code> if method was found and change done.
	 */
	protected final boolean setSizePoint(String methodName, int width, int height) throws Exception {
		MethodInvocation invocation =
				m_component.getMethodInvocation(methodName + "(org.eclipse.swt.graphics.Point)");
		if (invocation != null) {
			AstEditor editor = m_component.getEditor();
			Expression dimensionExpression = (Expression) invocation.arguments().get(0);
			editor.replaceExpression(dimensionExpression, "new org.eclipse.swt.graphics.Point("
					+ width
					+ ", "
					+ height
					+ ")");
			return true;
		}
		// not found
		return false;
	}
}