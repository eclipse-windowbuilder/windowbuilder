/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.forms.layout.column;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGenerator;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swt.model.layout.GenericFlowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.collections.bidimap.UnmodifiableBidiMap;

import java.util.List;

/**
 * Model for {@link ColumnLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public class ColumnLayoutInfo extends GenericFlowLayoutInfo
implements
IColumnLayoutInfo<ControlInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		new ColumnLayoutAssistant(this);
		new ColumnLayoutSelectionActionsSupport<>(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void onSet() throws Exception {
		super.onSet();
		// restore general layout datas
		for (ControlInfo control : getComposite().getChildrenControls()) {
			LayoutDataInfo layoutData = getLayoutData(control);
			if (layoutData != null) {
				restoreLayoutData(control, layoutData);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Components/constraints
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Object getDefaultVirtualDataObject() throws Exception {
		ClassLoader editorLoader = GlobalState.getClassLoader();
		return editorLoader.loadClass("org.eclipse.ui.forms.widgets.ColumnLayoutData").newInstance();
	}

	@Override
	public IColumnLayoutDataInfo getColumnData2(ControlInfo control) {
		return getColumnData(control);
	}

	/**
	 * @return {@link ColumnLayoutDataInfo} associated with given {@link ControlInfo}.
	 */
	public static ColumnLayoutDataInfo getColumnData(ControlInfo control) {
		return (ColumnLayoutDataInfo) getLayoutData(control);
	}

	//////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void clipboardCopy_addControlCommands(ControlInfo control,
			List<ClipboardCommand> commands) throws Exception {
		// command for adding child
		commands.add(new LayoutClipboardCommand<ColumnLayoutInfo>(control) {
			private static final long serialVersionUID = 0L;

			@Override
			protected void add(ColumnLayoutInfo layout, ControlInfo control) throws Exception {
				layout.command_CREATE(control, null);
			}
		});
		// command for ColumnLayoutData
		ColumnLayoutDataInfo columnDataInfo = getColumnData(control);
		if (!(columnDataInfo.getCreationSupport() instanceof IImplicitCreationSupport)) {
			final int index = getComposite().getChildrenControls().indexOf(control);
			final JavaInfoMemento dataMemento = JavaInfoMemento.createMemento(columnDataInfo);
			commands.add(new ComponentClipboardCommand<CompositeInfo>() {
				private static final long serialVersionUID = 0L;

				@Override
				public void execute(CompositeInfo composite) throws Exception {
					ControlInfo control = composite.getChildrenControls().get(index);
					// remove existing (virtual) LayoutData
					control.removeChild(getLayoutData(control));
					// add new LayoutData
					JavaInfo columnData = dataMemento.create(composite);
					JavaInfoUtils.add(
							columnData,
							new EmptyInvocationVariableSupport(columnData, "%parent%.setLayoutData(%child%)", 0),
							BlockStatementGenerator.INSTANCE,
							AssociationObjects.invocationChildNull(),
							control,
							null);
					// apply properties
					dataMemento.apply();
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Manage general layout data.
	//
	////////////////////////////////////////////////////////////////////////////
	public static final BidiMap m_horizontalAlignmentMap;
	static {
		BidiMap horizontalAlignmentMap = new DualHashBidiMap();
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.LEFT, ColumnLayoutData.LEFT);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.CENTER, ColumnLayoutData.CENTER);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.RIGHT, ColumnLayoutData.RIGHT);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.FILL, ColumnLayoutData.FILL);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.NONE, 0);
		m_horizontalAlignmentMap = UnmodifiableBidiMap.decorate(horizontalAlignmentMap);
	}

	@Override
	protected void storeLayoutData(ControlInfo control, LayoutDataInfo layoutData) throws Exception {
		GeneralLayoutData generalLayoutData = new GeneralLayoutData();
		ColumnLayoutDataInfo columnLayoutData = (ColumnLayoutDataInfo) layoutData;
		generalLayoutData.horizontalAlignment =
				GeneralLayoutData.getGeneralValue(
						m_horizontalAlignmentMap,
						(Integer) GeneralLayoutData.getLayoutPropertyValue(
								columnLayoutData,
								"horizontalAlignment"));
		generalLayoutData.putToInfo(control);
	}

	protected void restoreLayoutData(ControlInfo control, LayoutDataInfo layoutData) throws Exception {
		ColumnLayoutDataInfo columnLayoutData = (ColumnLayoutDataInfo) layoutData;
		GeneralLayoutData generalLayoutData = GeneralLayoutData.getFromInfoEx(control);
		Integer horizontalAlignmentValue =
				GeneralLayoutData.getRealValue(
						m_horizontalAlignmentMap,
						generalLayoutData.horizontalAlignment);
		if (horizontalAlignmentValue != null) {
			columnLayoutData.setHorizontalAlignment(horizontalAlignmentValue);
		}
	}
}
