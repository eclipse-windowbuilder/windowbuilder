/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * {@link EntryInfo} that allows user to drop new {@link Composite} with
 * {@link org.eclipse.jface.layout.TableColumnLayout} and {@link Table}.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class TableCompositeEntryInfo extends ToolEntryInfo {
	private static final ImageDescriptor ICON = Activator
			.getImageDescriptor("info/AbstractColumnLayout/TableComposite.gif");
	private final ComponentEntryInfo m_compositeEntry;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableCompositeEntryInfo() {
		setName(PaletteMessages.TableCompositeEntryInfo_name);
		setDescription(PaletteMessages.TableCompositeEntryInfo_description);
		m_compositeEntry = new ComponentEntryInfo();
		m_compositeEntry.setComponentClassName("org.eclipse.swt.widgets.Composite");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		return ICON;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ToolEntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean initialize(IEditPartViewer editPartViewer, JavaInfo rootJavaInfo) {
		return super.initialize(editPartViewer, rootJavaInfo)
				&& ProjectUtils.hasType(m_javaProject, "org.eclipse.jface.layout.TableColumnLayout")
				&& m_compositeEntry.initialize(editPartViewer, rootJavaInfo);
	}

	@Override
	public Tool createTool() throws Exception {
		final CreationTool creationTool = (CreationTool) m_compositeEntry.createTool();
		// configure CreationTool to add also TableColumnLayout and Table
		m_rootJavaInfo.addBroadcastListener(new JavaEventListener() {
			@Override
			public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
				ICreationFactory creationFactory = creationTool.getFactory();
				CompositeInfo composite = (CompositeInfo) creationFactory.getNewObject();
				if (child == composite) {
					composite.removeBroadcastListener(this);
					configureComposite(composite);
				}
			}
		});
		// OK, return CreationTool
		return creationTool;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures {@link CompositeInfo} by creating {@link Table} and
	 * {@link org.eclipse.jface.layout.TableColumnLayout}.
	 */
	private void configureComposite(CompositeInfo composite) throws Exception {
		composite.putArbitraryValue(CompositeInfo.KEY_DONT_INHERIT_LAYOUT, Boolean.TRUE);
		composite.getRootJava().refreshLight();
		// set some known layout, such as RowLayout
		RowLayoutInfo rowLayout;
		{
			rowLayout =
					(RowLayoutInfo) JavaInfoUtils.createJavaInfo(
							m_editor,
							"org.eclipse.swt.layout.RowLayout",
							new ConstructorCreationSupport());
			m_editor.setResolveImports(false);
			composite.setLayout(rowLayout);
			m_editor.setResolveImports(true);
		}
		// create new Table using RowLayout
		{
			ControlInfo table =
					(ControlInfo) JavaInfoUtils.createJavaInfo(
							m_editor,
							"org.eclipse.swt.widgets.Table",
							new ConstructorCreationSupport());
			rowLayout.command_CREATE(table, null);
		}
		// set TableColumnLayout for Composite
		{
			LayoutInfo tableColumnLayout =
					(LayoutInfo) JavaInfoUtils.createJavaInfo(
							m_editor,
							"org.eclipse.jface.layout.TableColumnLayout",
							new ConstructorCreationSupport());
			composite.setLayout(tableColumnLayout);
		}
	}
}
