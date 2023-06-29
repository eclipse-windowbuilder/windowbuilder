/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.jface.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.viewers.ColumnLayoutData;

import java.util.List;

/**
 * Model for {@link org.eclipse.jface.layout.AbstractColumnLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface.layout
 */
public abstract class AbstractColumnLayoutInfo extends LayoutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractColumnLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		contributeLayoutDataTypeProperty();
		trackColumnCommands();
		deleteCompositeWhenDeleteManagedControl();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	public void command_CREATE(ControlInfo control) throws Exception {
		command_CREATE(control, null);
	}

	public void command_ADD(ControlInfo control) throws Exception {
		command_MOVE(control, null);
	}

	/**
	 * {@link org.eclipse.jface.layout.AbstractColumnLayout} requires single control, so when no
	 * control, we should delete composite.
	 */
	private void deleteCompositeWhenDeleteManagedControl() {
		addBroadcastListener(new ObjectInfoDelete() {
			@Override
			public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
				if (isActiveOnComposite(parent) && child instanceof ControlInfo) {
					parent.delete();
				}
			}
		});
	}

	/**
	 * Tracks column create/move(in,out) commands and adds/deletes {@link ColumnLayoutDataInfo}'s.
	 */
	private void trackColumnCommands() {
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
				if (isColumn(child) && isOurColumnContainer(parent)) {
					addPixelLayoutData(child);
				}
			}

			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				if (isColumn(child)) {
					if (newParent != oldParent && isOurColumnContainer(oldParent)) {
						removeLayoutData((ItemInfo) child);
					}
				}
			}

			@Override
			public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				if (isColumn(child)) {
					if (newParent != oldParent && isOurColumnContainer(newParent)) {
						addPixelLayoutData(child);
					}
				}
			}

			/**
			 * Adds {@link ColumnPixelDataInfo} to the given column.
			 */
			private void addPixelLayoutData(JavaInfo column) throws Exception {
				// prepare "width" from column property
				Object widthValue = null;
				{
					Property widthProperty = column.getPropertyByTitle("width");
					if (widthProperty != null && widthProperty.isModified()) {
						widthValue = widthProperty.getValue();
						widthProperty.setValue(Property.UNKNOWN_VALUE);
					}
				}
				// set ColumnPixelData
				ColumnLayoutDataInfo layoutData =
						setLayoutData((ItemInfo) column, "org.eclipse.jface.viewers.ColumnPixelData");
				// set "width" to ColumnPixelData
				// check for "null" in case if we just add new column, so setWidth() is not evaluated
				if (widthValue != null) {
					layoutData.getPropertyByTitle("width").setValue(widthValue);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layout data
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ColumnLayoutDataInfo} associated with given column, can not return
	 *         <code>null</code> (because {@link org.eclipse.jface.layout.AbstractColumnLayout}
	 *         requires {@link ColumnLayoutData} for each column).
	 */
	public final ColumnLayoutDataInfo getLayoutData(ItemInfo column) {
		List<ColumnLayoutDataInfo> dataList = column.getChildren(ColumnLayoutDataInfo.class);
		Assert.isTrue(
				dataList.size() <= 1,
				"Zero or one ColumnLayoutData_Info expected in %s, but %s found.",
				column,
				dataList);
		return dataList.isEmpty() ? null : dataList.get(0);
	}

	/**
	 * Sets new {@link ColumnLayoutDataInfo} for column.
	 */
	public final ColumnLayoutDataInfo setLayoutData(ItemInfo column, String layoutDataClassName)
			throws Exception {
		ColumnLayoutDataInfo layoutData =
				(ColumnLayoutDataInfo) JavaInfoUtils.createJavaInfo(
						getEditor(),
						layoutDataClassName,
						new ConstructorCreationSupport());
		setLayoutData(column, layoutData);
		return layoutData;
	}

	/**
	 * Sets new {@link ColumnLayoutDataInfo} for column.
	 */
	private void setLayoutData(ItemInfo column, ColumnLayoutDataInfo newLayoutData) throws Exception {
		removeLayoutData(column);
		// add new setColumnData() invocation
		MethodInvocation invocation;
		Expression layoutDataExpression;
		{
			String invocationSource =
					TemplateUtils.format(
							"{0}.setColumnData({1}, {2})",
							this,
							column,
							newLayoutData.getCreationSupport().add_getSource(null));
			invocation = (MethodInvocation) column.addExpressionStatement(invocationSource);
			layoutDataExpression = DomGenerics.arguments(invocation).get(1);
		}
		// configure CreationSupport
		newLayoutData.getCreationSupport().add_setSourceExpression(layoutDataExpression);
		// use empty variable
		{
			VariableSupport variableSupport =
					new EmptyVariableSupport(newLayoutData, layoutDataExpression);
			newLayoutData.setVariableSupport(variableSupport);
		}
		// use "secondary" association
		newLayoutData.setAssociation(new InvocationSecondaryAssociation(invocation));
		column.addChild(newLayoutData);
		// add related nodes
		addRelatedNodes(invocation);
		column.addRelatedNodes(invocation);
		newLayoutData.addRelatedNodes(invocation);
	}

	/**
	 * Removes existing/old layout data for given column.
	 */
	private void removeLayoutData(ItemInfo column) throws Exception {
		// remove existing setColumnData() invocation
		List<MethodInvocation> invocations =
				getMethodInvocations("setColumnData("
						+ "org.eclipse.swt.widgets.Widget,"
						+ "org.eclipse.jface.viewers.ColumnLayoutData)");
		for (MethodInvocation invocation : invocations) {
			Expression columnExpression = DomGenerics.arguments(invocation).get(0);
			if (column.isRepresentedBy(columnExpression)) {
				getEditor().removeEnclosingStatement(invocation);
			}
		}
		// delete layout data itself
		ColumnLayoutDataInfo oldLayoutData = getLayoutData(column);
		if (oldLayoutData != null) {
			oldLayoutData.delete();
		}
	}

	/**
	 * Adds broadcast that contribute {@link ColumnLayoutDataType_Property} to each column of
	 * table/tree.
	 */
	private void contributeLayoutDataTypeProperty() {
		addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo possibleColumn, List<Property> properties) throws Exception {
				if (isOurColumn(possibleColumn)) {
					// prepare ColumnLayoutDataType_Property (existing or new)
					ColumnLayoutDataType_Property typeProperty;
					{
						Class<ColumnLayoutDataType_Property> key = ColumnLayoutDataType_Property.class;
						typeProperty = (ColumnLayoutDataType_Property) possibleColumn.getArbitraryValue(key);
						if (typeProperty == null) {
							typeProperty = new ColumnLayoutDataType_Property((ItemInfo) possibleColumn);
							possibleColumn.putArbitraryValue(key, typeProperty);
						}
					}
					// add ColumnLayoutDataType_Property
					properties.add(typeProperty);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Checks
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link JavaInfo} is column, by checking parameter
	 *         <code>"isColumn"</code> .
	 */
	private static boolean isColumn(JavaInfo possibleColumn) {
		return JavaInfoUtils.hasTrueParameter(possibleColumn, "isColumn");
	}

	/**
	 * @return <code>true</code> if given {@link JavaInfo} is column on single table/tree child of out
	 *         {@link CompositeInfo}.
	 */
	private boolean isOurColumn(JavaInfo possibleColumn) {
		if (isColumn(possibleColumn)) {
			ObjectInfo possibleColumnContainer = possibleColumn.getParent();
			return isOurColumnContainer(possibleColumnContainer);
		}
		return false;
	}

	/**
	 * @return <code>true</code> if given {@link JavaInfo} is single table/tree child of out
	 *         {@link CompositeInfo}.
	 */
	private boolean isOurColumnContainer(ObjectInfo possibleTableOrTree) {
		ObjectInfo layoutComposite = possibleTableOrTree.getParent();
		return isActiveOnComposite(layoutComposite);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ColumnLayoutData type selection property
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link Property} for selecting {@link ColumnLayoutDataInfo} type to use for column.
	 */
	private final class ColumnLayoutDataType_Property extends Property {
		private static final String PIXELS_TITLE = "ColumnPixelData";
		private static final String WEIGHT_TITLE = "ColumnWeightData";
		private final ItemInfo m_column;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ColumnLayoutDataType_Property(ItemInfo column) {
			super(new StringComboPropertyEditor(PIXELS_TITLE, WEIGHT_TITLE));
			setCategory(PropertyCategory.system(4));
			m_column = column;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String getTitle() {
			return "LayoutDataType";
		}

		@Override
		public boolean isModified() throws Exception {
			return true;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Value
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getValue() throws Exception {
			ColumnLayoutDataInfo layoutData = getLayoutData(m_column);
			if (layoutData instanceof ColumnPixelDataInfo) {
				return PIXELS_TITLE;
			} else {
				return WEIGHT_TITLE;
			}
		}

		@Override
		public void setValue(Object value) throws Exception {
			// prepare class of layout data to set
			final String layoutDataClassName;
			if (value == PIXELS_TITLE) {
				layoutDataClassName = "org.eclipse.jface.viewers.ColumnPixelData";
			} else {
				layoutDataClassName = "org.eclipse.jface.viewers.ColumnWeightData";
			}
			// OK, set layout data
			ExecutionUtils.run(m_column, new RunnableEx() {
				@Override
				public void run() throws Exception {
					setLayoutData(m_column, layoutDataClassName);
				}
			});
		}
	}
}
