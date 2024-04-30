/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.jface.viewers;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.Activator;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.viewers.TableViewer;

/**
 * {@link PropertyEditor} for installing sorter of {@link TableViewer} by column.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface.viewers
 */
public final class TableViewerColumnSorterPropertyEditor extends TextDisplayPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new TableViewerColumnSorterPropertyEditor();

	private TableViewerColumnSorterPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		if (property.isModified()) {
			return "<exists>";
		}
		return "<double click>";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void doubleClick(Property _property, Point location) throws Exception {
		JavaProperty property = (JavaProperty) _property;
		final JavaInfo javaInfo = property.getJavaInfo();
		ASTNode node = (ASTNode) property.getValue();
		// open in source, if exists
		if (node != null) {
			IDesignPageSite site = IDesignPageSite.Helper.getSite(javaInfo);
			if (site != null) {
				site.openSourcePosition(node.getStartPosition());
			}
			return;
		}
		// no sorter, generate
		AstEditor editor = javaInfo.getEditor();
		ProjectUtils.ensureResourceType(
				editor.getJavaProject(),
				Activator.getDefault().getBundle(),
				"org.eclipse.wb.swt.TableViewerColumnSorter");
		ExecutionUtils.run(javaInfo, new RunnableEx() {
			@Override
			public void run() throws Exception {
				String source =
						CodeUtils.getSource(
								"new org.eclipse.wb.swt.TableViewerColumnSorter("
										+ TemplateUtils.getExpression(javaInfo)
										+ ") {",
										"\t@Override",
										"\tprotected int doCompare(org.eclipse.jface.viewers.Viewer viewer, Object e1, Object e2) {",
										"\t\t// TODO Remove this method, if your getValue(Object) returns Comparable.",
										"\t\t// Typical Comparable are String, Integer, Double, etc.",
										"\t\treturn super.doCompare(viewer, e1, e2);",
										"\t}",
										"\t@Override",
										"\tprotected Object getValue(Object o) {",
										"\t\t// TODO remove this method, if your EditingSupport returns value",
										"\t\treturn super.getValue(o);",
										"\t}",
								"}");
				javaInfo.addExpressionStatement(source);
			}
		});
		// open in source
		doubleClick(_property, location);
	}
}