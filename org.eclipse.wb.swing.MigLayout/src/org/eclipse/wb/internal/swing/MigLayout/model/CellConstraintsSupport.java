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
package org.eclipse.wb.internal.swing.MigLayout.model;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.EnumCustomPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swing.MigLayout.model.ui.CellEditDialog;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

import net.miginfocom.layout.CC;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.DimConstraint;
import net.miginfocom.layout.IDEUtil;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import java.awt.Component;
import java.awt.Container;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model for constraints on {@link MigLayout}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public final class CellConstraintsSupport {
	private final CellConstraintsSupport m_this = this;
	private final MigLayoutInfo m_layout;
	private final ComponentInfo m_component;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constraints values
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_dirty;
	private int x;
	private int y;
	private int width;
	private int height;
	private CC cc;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	CellConstraintsSupport(MigLayoutInfo layoutInfo, ComponentInfo componentInfo) {
		m_layout = layoutInfo;
		m_component = componentInfo;
		// fetch values
		Container container = m_layout.getContainer().getContainer();
		if (m_layout.getObject() != null
				&& m_component.getComponent() != null
				&& m_component.getComponent().getParent() == container) {
			MigLayout layout = (MigLayout) layoutInfo.getObject();
			Component component = componentInfo.getComponent();
			// prepare CC
			{
				Object constraints = layout.getComponentConstraints(component);
				if (constraints instanceof CC) {
					cc = (CC) constraints;
				} else if (constraints instanceof String) {
					cc = ConstraintParser.parseComponentConstraint((String) constraints);
				} else {
					cc = new CC();
				}
			}
			// prepare cell
			{
				int[] gridPosition = IDEUtil.getGridPositions(container).get(component);
				if (gridPosition != null) {
					x = gridPosition[0];
					y = gridPosition[1];
					width = gridPosition[2];
					height = gridPosition[3];
					// tweaks for span
					if (width == 30000 - x) {
						int columnCount = IDEUtil.getColumnSizes(container)[0].length;
						width = columnCount - x;
					}
				} else {
					x = 0;
					y = 0;
					width = 0;
					height = 0;
				}
			}
		} else {
			cc = new CC();
			x = 0;
			y = 0;
			width = 1;
			height = 1;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ComponentInfo} which uses this {@link CellConstraintsSupport}.
	 */
	public ComponentInfo getComponent() {
		return m_component;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * AFAIK in {@link MigLayout} using explicit <code>cell</code> is not best usage pattern,
	 * components usually are just added consecutive, may be with <code>skip</code> and
	 * <code>wrap</code> tags. But when we perform some grid-based operations, we should make bounds
	 * explicit, to avoid analyzing all components, how change in one will change all other.
	 */
	public void makeExplicitCell() {
		// ignore docked
		if (getDockSide() != null) {
			return;
		}
		// operations
		if (cc.getSplit() != 1) {
			cc.setSplit(1);
			m_dirty = true;
		}
		if (cc.isWrap()) {
			cc.setWrap(false);
			m_dirty = true;
		}
		if (cc.isNewline()) {
			cc.setNewline(false);
			m_dirty = true;
		}
		if (cc.getSkip() != 0) {
			cc.setSkip(0);
			m_dirty = true;
		}
		// bounds
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
	}

	/**
	 * @return the <code>X</code> location in grid.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Sets the <code>X</code> location in grid.
	 */
	public void setX(int x) {
		this.x = x;
		if (cc.getCellX() != x) {
			cc.setCellX(x);
			m_dirty = true;
		}
	}

	/**
	 * Changes <b>X</b> on given delta.
	 */
	public void updateX(int delta) {
		setX(getX() + delta);
	}

	/**
	 * @return the <code>Y</code> location in grid.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Sets the <code>Y</code> location in grid.
	 */
	public void setY(int y) {
		this.y = y;
		if (cc.getCellY() != y) {
			cc.setCellY(y);
			m_dirty = true;
		}
	}

	/**
	 * Changes <b>Y</b> on given delta.
	 */
	public void updateY(int delta) {
		setY(getY() + delta);
	}

	/**
	 * @return the <code>width</code> in cells.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the <code>width</code> in cells.
	 */
	public void setWidth(int width) {
		this.width = width;
		if (cc.getSpanX() != width) {
			cc.setSpanX(width);
			m_dirty = true;
		}
	}

	/**
	 * Changes <b>width</b> on given delta.
	 */
	public void updateWidth(int delta) {
		setWidth(getWidth() + delta);
	}

	/**
	 * @return the <code>height</code> in cells.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the <code>height</code> in cells.
	 */
	public void setHeight(int height) {
		this.height = height;
		if (cc.getSpanY() != height) {
			cc.setSpanY(height);
			m_dirty = true;
		}
	}

	/**
	 * Changes <b>height</b> on given delta.
	 */
	public void updateHeight(int delta) {
		setHeight(getHeight() + delta);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the horizontal alignment.
	 */
	public MigColumnInfo.Alignment getHorizontalAlignment() {
		DimConstraint constraint = cc.getDimConstraint(true);
		return MigColumnInfo.getAlignment(constraint, false, false);
	}

	/**
	 * Sets the horizontal alignment.
	 */
	public void setHorizontalAlignment(MigColumnInfo.Alignment alignment) {
		DimConstraint constraint = cc.getDimConstraint(true);
		MigColumnInfo.setAlignment(constraint, alignment, false);
		m_dirty = true;
	}

	/**
	 * @return the vertical alignment.
	 */
	public MigRowInfo.Alignment getVerticalAlignment() {
		DimConstraint constraint = cc.getDimConstraint(false);
		return MigRowInfo.getAlignment(constraint, false, false);
	}

	/**
	 * Sets the vertical alignment.
	 */
	public void setVerticalAlignment(MigRowInfo.Alignment alignment) {
		DimConstraint constraint = cc.getDimConstraint(false);
		MigRowInfo.setAlignment(constraint, alignment, false);
		m_dirty = true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dock
	//
	////////////////////////////////////////////////////////////////////////////
	public enum DockSide {
		NORTH, WEST, SOUTH, EAST
	}

	/**
	 * @return the {@link DockSide} for docked component, may be <code>null</code> if component is not
	 *         docked.
	 */
	public DockSide getDockSide() {
		switch (cc.getDockSide()) {
		case 0 :
			return DockSide.NORTH;
		case 1 :
			return DockSide.WEST;
		case 2 :
			return DockSide.SOUTH;
		case 3 :
			return DockSide.EAST;
		}
		return null;
	}

	/**
	 * Sets {@link DockSide} as enum.
	 *
	 * @param side
	 *          the {@link DockSide}, may be <code>null</code> if docking should be removed.
	 */
	public void setDockSide(DockSide side) {
		m_dirty = true;
		// no dock
		if (side == null) {
			cc.setDockSide(-1);
			return;
		}
		// some dock side
		switch (side) {
		case NORTH :
			cc.setDockSide(0);
			break;
		case WEST :
			cc.setDockSide(1);
			break;
		case SOUTH :
			cc.setDockSide(2);
			break;
		case EAST :
			cc.setDockSide(3);
			break;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Split
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this is first component in splitted cell and this cell has
	 *         horizontal flow. Note: only for first component in splitted cell.
	 */
	public boolean isHorizontalSplit() {
		Boolean flowX = cc.getFlowX();
		if (flowX != null) {
			return flowX.booleanValue();
		} else {
			return m_layout.getLC().isFlowX();
		}
	}

	/**
	 * Specifies if split of this this component should be: horizontal, vertical or none.
	 *
	 * @param horizontal
	 *          is <code>true</code>, <code>false</code> or <code>null</code>.
	 */
	public void setHorizontalSplit(Boolean horizontal) {
		cc.setFlowX(horizontal);
		m_dirty = true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// String
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the string presentation of this {@link CC}.
	 */
	public final String getString() {
		return IDEUtil.getConstraintString(cc, false);
	}

	/**
	 * Sets the string for this {@link CC}.
	 */
	public final void setString(String s) {
		s = s.toLowerCase().trim();
		cc = ConstraintParser.parseComponentConstraint(s);
		m_dirty = true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Write
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Writes current values as constraints in component association using
	 * {@link Container#add(Component, Object)}.
	 */
	public void write() throws Exception {
		// check if write required
		if (m_dirty) {
			write0();
			m_dirty = false;
		}
	}

	/**
	 * Implementation of {@link #write()}.
	 */
	public void write0() throws Exception {
		String constraintsString = IDEUtil.getConstraintString(cc, false);
		constraintsString = cleanUpSource(constraintsString);
		// update association constraints
		if (m_component.getAssociation() instanceof InvocationChildAssociation) {
			InvocationChildAssociation association =
					(InvocationChildAssociation) m_component.getAssociation();
			MethodInvocation invocation = association.getInvocation();
			String signature = AstNodeUtils.getMethodSignature(invocation);
			String constraintsSource = StringConverter.INSTANCE.toJavaSource(null, constraintsString);
			if (signature.equals("add(java.awt.Component,java.lang.Object)")) {
				Expression constraintsExpression = (Expression) invocation.arguments().get(1);
				m_layout.getEditor().replaceExpression(constraintsExpression, constraintsSource);
			} else if (signature.equals("add(java.awt.Component)")) {
				m_layout.getEditor().addInvocationArgument(invocation, 1, constraintsSource);
			}
		}
	}

	/**
	 * Currently {@link IDEUtil} generates sometimes not ideal source. I've asked Mikael to fix this,
	 * but right we clean up several cases manually.
	 */
	private static String cleanUpSource(String source) {
		source = cleanUpSource_secondGap(source, "gapx", "gapright ");
		source = cleanUpSource_secondGap(source, "gapy", "gapbottom ");
		// bug in 3.7.4
		if (source.contains("hideMode")) {
			source = StringUtils.replace(source, "hideMode", "hidemode");
		}
		// done
		return source;
	}

	/**
	 * "gapx null value" -> "gapright value"
	 */
	private static String cleanUpSource_secondGap(String source, String sourceName, String targetName) {
		Pattern pattern = Pattern.compile(sourceName + " null (\\w+)(\\,|\\z)");
		Matcher matcher = pattern.matcher(source);
		if (matcher.find()) {
			String actualGap = matcher.group(1);
			String optionalComma = matcher.group().endsWith(",") ? "," : "";
			source =
					source.substring(0, matcher.start())
					+ targetName
					+ actualGap
					+ optionalComma
					+ source.substring(matcher.end());
		}
		return source;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	private ComplexProperty m_complexProperty;

	/**
	 * @return the {@link Property} with given title.
	 */
	public Property getPropertyByTitle(String title) throws Exception {
		for (Property property : getCellProperty().getProperties()) {
			if (property.getTitle().equals(title)) {
				return property;
			}
		}
		return null;
	}

	/**
	 * @return the {@link ComplexProperty} for this {@link CellConstraintsSupport}.
	 */
	public ComplexProperty getCellProperty() throws Exception {
		if (m_complexProperty == null) {
			m_complexProperty = new ComplexProperty("Constraints", null);
			m_complexProperty.setCategory(PropertyCategory.system(6));
			m_complexProperty.setEditorPresentation(new ButtonPropertyEditorPresentation() {
				@Override
				protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
					new CellEditDialog(propertyTable.getControl().getShell(), m_layout, m_this).open();
				}
			});
			// grid properties
			Property xProperty = new IntegerCellProperty("grid x", "getX()", "setX(int)") {
				@Override
				public boolean isModified() throws Exception {
					return true;
				}

				@Override
				protected String validate(int value) {
					int columns = m_layout.getColumns().size();
					if (0 <= value && value + width <= columns) {
						return null;
					}
					return MessageFormat.format(
							ModelMessages.CellConstraintsSupport_gridX_outOfRange,
							value,
							(columns - width));
				}
			};
			Property wProperty = new IntegerCellProperty("grid width", "getWidth()", "setWidth(int)") {
				@Override
				public boolean isModified() throws Exception {
					return width != 1;
				}

				@Override
				protected Object getDefaultValue() {
					return 1;
				}

				@Override
				protected String validate(int value) {
					int columns = m_layout.getColumns().size();
					if (1 <= value && x + value <= columns) {
						return null;
					}
					return MessageFormat.format(
							ModelMessages.CellConstraintsSupport_gridWidth_outOfRange,
							value,
							(columns - x));
				}
			};
			Property yProperty = new IntegerCellProperty("grid y", "getY()", "setY(int)") {
				@Override
				public boolean isModified() throws Exception {
					return true;
				}

				@Override
				protected String validate(int value) {
					int rows = m_layout.getRows().size();
					if (0 <= value && value + height <= rows) {
						return null;
					}
					return MessageFormat.format(
							ModelMessages.CellConstraintsSupport_gridY_outOfRange,
							value,
							(rows - y - 1));
				}
			};
			Property hProperty = new IntegerCellProperty("grid height", "getHeight()", "setHeight(int)") {
				@Override
				public boolean isModified() throws Exception {
					return height != 1;
				}

				@Override
				protected Object getDefaultValue() {
					return 1;
				}

				@Override
				protected String validate(int value) {
					int rows = m_layout.getRows().size();
					if (1 <= value && y + value <= rows) {
						return null;
					}
					return MessageFormat.format(
							ModelMessages.CellConstraintsSupport_gridHeight_outOfRange,
							value,
							(rows - y));
				}
			};
			// set sub-properties
			m_complexProperty.setProperties(new Property[]{
					xProperty,
					yProperty,
					wProperty,
					hProperty,
					getHorizontalAlignmentProperty(),
					getVerticalAlignmentProperty()});
		}
		//
		m_complexProperty.setText(IDEUtil.getConstraintString(cc, false));
		return m_complexProperty;
	}

	private Property getHorizontalAlignmentProperty() throws Exception {
		return new AlignmentCellProperty<>("h alignment",
				"getHorizontalAlignment()",
				"setHorizontalAlignment(org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo.Alignment)",
				MigColumnInfo.ALIGNMENTS_TO_SET,
				MigColumnInfo.Alignment.DEFAULT);
	}

	private Property getVerticalAlignmentProperty() throws Exception {
		return new AlignmentCellProperty<>("v alignment",
				"getVerticalAlignment()",
				"setVerticalAlignment(org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo.Alignment)",
				MigRowInfo.ALIGNMENTS_TO_SET,
				MigRowInfo.Alignment.DEFAULT);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractCellProperty
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Abstract implementation of {@link Property} for {@link CellConstraintsSupport}.
	 *
	 * @author scheglov_ke
	 */
	private abstract class AbstractCellProperty extends Property {
		private final String m_title;
		protected final String m_getterSignature;
		protected final String m_setterSignature;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AbstractCellProperty(String title,
				String getterSignature,
				String setterSignature,
				PropertyEditor propertyEditor) throws Exception {
			super(propertyEditor);
			m_title = title;
			m_getterSignature = getterSignature;
			m_setterSignature = setterSignature;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Property
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public final String getTitle() {
			return m_title;
		}

		@Override
		public final void setValue(Object value) throws Exception {
			// try to replace unknown value with some default value
			if (value == UNKNOWN_VALUE) {
				value = getDefaultValue();
			}
			// set known value
			if (value != UNKNOWN_VALUE) {
				// validate
				{
					String errorMessage = validate(value);
					if (errorMessage != null) {
						UiUtils.openWarning(DesignerPlugin.getShell(), getTitle(), errorMessage);
						return;
					}
				}
				// do modification
				doSetValue(value);
			}
		}

		private void doSetValue(final Object value) {
			ExecutionUtils.run(m_layout, new RunnableEx() {
				@Override
				public void run() throws Exception {
					ReflectionUtils.invokeMethod(m_this, m_setterSignature, value);
					write();
				}
			});
		}

		/**
		 * @return the default property value.
		 */
		protected Object getDefaultValue() {
			return UNKNOWN_VALUE;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Value
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getValue() throws Exception {
			return ReflectionUtils.invokeMethod(m_this, m_getterSignature);
		}

		/**
		 * @return <code>null</code> if given value is valid and can be set, or return some
		 *         {@link String} with error message.
		 */
		protected abstract String validate(Object value) throws Exception;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IntegerCellProperty
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link Property} for integer fields of {@link CellConstraintsSupport}.
	 *
	 * @author scheglov_ke
	 */
	private abstract class IntegerCellProperty extends AbstractCellProperty {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public IntegerCellProperty(String title, String getterSignature, String setterSignature)
				throws Exception {
			super(title, getterSignature, setterSignature, IntegerPropertyEditor.INSTANCE);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Value
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected final String validate(Object value) throws Exception {
			if (!(value instanceof Integer)) {
				return ModelMessages.CellConstraintsSupport_integerValueExpected;
			}
			int intValue = ((Integer) value).intValue();
			return validate(intValue);
		}

		/**
		 * @return <code>true</code> if given value is valid for this property. For example we should
		 *         not allow to set width outside of number of existing columns.
		 */
		protected abstract String validate(int value);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// AlignmentCellProperty
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link Property} for alignment fields of {@link CellConstraintsSupport}.
	 *
	 * @author scheglov_ke
	 */
	private final class AlignmentCellProperty<A extends Enum<?>> extends AbstractCellProperty {
		private final A m_defaultValue;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AlignmentCellProperty(String title,
				String getterSignature,
				String setterSignature,
				A[] alignments,
				A defaultValue) throws Exception {
			super(title, getterSignature, setterSignature, new EnumCustomPropertyEditor());
			EnumCustomPropertyEditor editor = (EnumCustomPropertyEditor) getEditor();
			editor.configure(alignments);
			m_defaultValue = defaultValue;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Value
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public final boolean isModified() throws Exception {
			return getValue() != m_defaultValue;
		}

		@Override
		protected Object getDefaultValue() {
			return m_defaultValue;
		}

		@Override
		protected String validate(Object value) throws Exception {
			return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the small {@link ImageDescriptor} that represents horizontal/vertical
	 *         alignment.
	 */
	public ImageDescriptor getSmallAlignmentImageDescriptor(boolean horizontal) {
		if (horizontal) {
			return getHorizontalAlignment().getSmallImageDescriptor();
		} else {
			return getVerticalAlignment().getSmallImageDescriptor();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds items to the context {@link IMenuManager}.
	 */
	public void addContextMenu(IMenuManager manager) throws Exception {
		manager.appendToGroup(IContextMenuConstants.GROUP_TOP, new Action("Constraints...") {
			@Override
			public void run() {
				new CellEditDialog(DesignerPlugin.getShell(), m_layout, m_this).open();
			}
		});
		// horizontal
		{
			IMenuManager manager2 =
					new MenuManager(ModelMessages.CellConstraintsSupport_horizontalAlignment);
			manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
			fillHorizontalAlignmentMenu(manager2);
		}
		// vertical
		{
			IMenuManager manager2 =
					new MenuManager(ModelMessages.CellConstraintsSupport_verticalAlignment);
			manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
			fillVerticalAlignmentMenu(manager2);
		}
	}

	/**
	 * Adds the horizontal alignment {@link Action}'s.
	 */
	public void fillHorizontalAlignmentMenu(IMenuManager manager) {
		manager.add(new SetHorizontalAlignmentAction(ModelMessages.CellConstraintsSupport_horizontalAlignment_default,
				MigColumnInfo.Alignment.DEFAULT));
		manager.add(new SetHorizontalAlignmentAction(ModelMessages.CellConstraintsSupport_horizontalAlignment_left,
				MigColumnInfo.Alignment.LEFT));
		manager.add(new SetHorizontalAlignmentAction(ModelMessages.CellConstraintsSupport_horizontalAlignment_center,
				MigColumnInfo.Alignment.CENTER));
		manager.add(new SetHorizontalAlignmentAction(ModelMessages.CellConstraintsSupport_horizontalAlignment_right,
				MigColumnInfo.Alignment.RIGHT));
		manager.add(new SetHorizontalAlignmentAction(ModelMessages.CellConstraintsSupport_horizontalAlignment_fill,
				MigColumnInfo.Alignment.FILL));
		manager.add(new SetHorizontalAlignmentAction(ModelMessages.CellConstraintsSupport_horizontalAlignment_leading,
				MigColumnInfo.Alignment.LEADING));
		manager.add(new SetHorizontalAlignmentAction(ModelMessages.CellConstraintsSupport_horizontalAlignment_trailing,
				MigColumnInfo.Alignment.TRAILING));
	}

	/**
	 * Adds the vertical alignment {@link Action}'s.
	 */
	public void fillVerticalAlignmentMenu(IMenuManager manager2) {
		manager2.add(new SetVerticalAlignmentAction(ModelMessages.CellConstraintsSupport_verticalAlignment_default,
				MigRowInfo.Alignment.DEFAULT));
		manager2.add(new SetVerticalAlignmentAction(ModelMessages.CellConstraintsSupport_verticalAlignment_top,
				MigRowInfo.Alignment.TOP));
		manager2.add(new SetVerticalAlignmentAction(ModelMessages.CellConstraintsSupport_verticalAlignment_center,
				MigRowInfo.Alignment.CENTER));
		manager2.add(new SetVerticalAlignmentAction(ModelMessages.CellConstraintsSupport_verticalAlignment_bottom,
				MigRowInfo.Alignment.BOTTOM));
		manager2.add(new SetVerticalAlignmentAction(ModelMessages.CellConstraintsSupport_verticalAlignment_fill,
				MigRowInfo.Alignment.FILL));
		manager2.add(new SetVerticalAlignmentAction(ModelMessages.CellConstraintsSupport_verticalAlignment_baseline,
				MigRowInfo.Alignment.BASELINE));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SetHorizontalAlignmentAction
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link Action} for modifying horizontal alignment.
	 */
	private class SetHorizontalAlignmentAction extends ObjectInfoAction {
		private final MigColumnInfo.Alignment m_alignment;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public SetHorizontalAlignmentAction(String text, MigColumnInfo.Alignment alignment) {
			super(m_layout, text, AS_RADIO_BUTTON);
			m_alignment = alignment;
			setImageDescriptor(alignment.getMenuImageDescriptor());
			// set check for current alignment
			setChecked(getHorizontalAlignment() == m_alignment);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Run
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void runEx() throws Exception {
			setHorizontalAlignment(m_alignment);
			write();
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// SetVerticalAlignmentAction
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link Action} for modifying vertical alignment.
	 */
	private class SetVerticalAlignmentAction extends ObjectInfoAction {
		private final MigRowInfo.Alignment m_alignment;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public SetVerticalAlignmentAction(String text, MigRowInfo.Alignment alignment) {
			super(m_layout, text, AS_RADIO_BUTTON);
			m_alignment = alignment;
			setImageDescriptor(alignment.getMenuImageDescriptor());
			// set check for current alignment
			setChecked(getVerticalAlignment() == m_alignment);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Run
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void runEx() throws Exception {
			setVerticalAlignment(m_alignment);
			write();
		}
	}
}
