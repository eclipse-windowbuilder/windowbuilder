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
package org.eclipse.wb.internal.swing.model.layout.spring;

import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.swt.custom.CCombo;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SpringLayout;

/**
 * Model for attachment in {@link SpringLayout}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class SpringAttachmentInfo {
	private static final String PUT_CONSTRAINT =
			"putConstraint(java.lang.String,java.awt.Component,int,java.lang.String,java.awt.Component)";

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link SpringAttachmentInfo} for side of component.
	 *
	 * @param side
	 *          one of the {@link PositionConstants#LEFT}, {@link PositionConstants#TOP},
	 *          {@link PositionConstants#RIGHT}, {@link PositionConstants#BOTTOM}.
	 */
	public static SpringAttachmentInfo get(SpringLayoutInfo layout, ComponentInfo component, int side) {
		String key = getAttachmentKey(side);
		SpringAttachmentInfo attachment = (SpringAttachmentInfo) component.getArbitraryValue(key);
		if (attachment == null) {
			attachment = new SpringAttachmentInfo(layout, component, side);
			component.putArbitraryValue(key, attachment);
		}
		return attachment;
	}

	private static String getAttachmentKey(int side) {
		return "SpringLayout_" + side;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final SpringLayoutInfo m_layout;
	private final AstEditor m_editor;
	private final ComponentInfo m_component;
	private final int m_side;
	private final String m_springSide;
	private MethodInvocation m_invocation;
	private int m_offset;
	private ComponentInfo m_anchorComponent;
	private int m_anchorSide;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private SpringAttachmentInfo(SpringLayoutInfo layout, ComponentInfo component, int side) {
		m_layout = layout;
		m_editor = layout.getEditor();
		m_component = component;
		m_side = side;
		m_springSide = getSpringSide(side);
		setInitialState();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialization
	//
	////////////////////////////////////////////////////////////////////////////
	private void setInitialState() {
		m_invocation = getInvocation();
		if (m_invocation != null) {
			List<Expression> arguments = DomGenerics.arguments(m_invocation);
			Expression anchorExpression = arguments.get(4);
			Expression anchorSideExpression = arguments.get(3);
			Expression offsetExpression = arguments.get(2);
			m_anchorComponent =
					(ComponentInfo) m_layout.getContainer().getChildRepresentedBy(anchorExpression);
			{
				String springSide = (String) JavaInfoEvaluationHelper.getValue(anchorSideExpression);
				m_anchorSide = getFrameworkSide(springSide);
			}
			m_offset = (Integer) JavaInfoEvaluationHelper.getValue(offsetExpression);
		}
	}

	private void materialize() {
		if (m_invocation == null) {
			m_offset = 0;
			m_anchorComponent = m_layout.getContainer();
			m_anchorSide = m_side;
		}
	}

	private MethodInvocation getInvocation() {
		List<MethodInvocation> invocations = m_layout.getMethodInvocations(PUT_CONSTRAINT);
		for (MethodInvocation invocation : invocations) {
			List<Expression> arguments = DomGenerics.arguments(invocation);
			Expression componentArgument = arguments.get(1);
			Expression sideArgument = arguments.get(0);
			if (m_component.isRepresentedBy(componentArgument)
					&& m_springSide.equals(JavaInfoEvaluationHelper.getValue(sideArgument))) {
				return invocation;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts {@link PositionConstants} side into {@link SpringLayout} side.
	 */
	public static String getSpringSide(int side) {
		switch (side) {
		case PositionConstants.LEFT :
			return SpringLayout.WEST;
		case PositionConstants.TOP :
			return SpringLayout.NORTH;
		case PositionConstants.RIGHT :
			return SpringLayout.EAST;
		case PositionConstants.BOTTOM :
			return SpringLayout.SOUTH;
		}
		throw new IllegalArgumentException(MessageFormat.format("Invalid side: {0}", side));
	}

	/**
	 * Converts {@link PositionConstants} side into source for {@link SpringLayout} side.
	 */
	public static String getSpringSideSource(int side) {
		String springSideName;
		switch (side) {
		case PositionConstants.LEFT :
			springSideName = "WEST";
			break;
		case PositionConstants.TOP :
			springSideName = "NORTH";
			break;
		case PositionConstants.RIGHT :
			springSideName = "EAST";
			break;
		case PositionConstants.BOTTOM :
			springSideName = "SOUTH";
			break;
		default :
			throw new IllegalArgumentException(MessageFormat.format("Invalid side: {0}", side));
		}
		return "javax.swing.SpringLayout." + springSideName;
	}

	/**
	 * @param side
	 *          the side from {@link SpringLayout}.
	 *
	 * @return the absolute framework side one of the {@link PositionConstants#LEFT},
	 *         {@link PositionConstants#TOP}, {@link PositionConstants#RIGHT},
	 *         {@link PositionConstants#BOTTOM}.
	 */
	public static int getFrameworkSide(String side) {
		if (SpringLayout.WEST.equals(side)) {
			return PositionConstants.LEFT;
		}
		if (SpringLayout.NORTH.equals(side)) {
			return PositionConstants.TOP;
		}
		if (SpringLayout.EAST.equals(side)) {
			return PositionConstants.RIGHT;
		}
		if (SpringLayout.SOUTH.equals(side)) {
			return PositionConstants.BOTTOM;
		}
		throw new IllegalArgumentException(MessageFormat.format("Invalid side: {0}", side));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the side of this attachment.
	 */
	public int getSide() {
		return m_side;
	}

	/**
	 * @return <code>true</code> if this attachment is not represented in source.
	 */
	public boolean isVirtual() {
		return m_invocation == null;
	}

	public ComponentInfo getAnchorComponent() {
		return m_anchorComponent;
	}

	public void setAnchorComponent(ComponentInfo anchorComponent) {
		m_anchorComponent = anchorComponent;
	}

	public int getAnchorSide() {
		return m_anchorSide;
	}

	public void setAnchorSide(int anchorSide) {
		m_anchorSide = anchorSide;
	}

	/**
	 * @return the offset from anchor.
	 */
	public int getOffset() {
		return m_offset;
	}

	public void setOffset(int offset) {
		m_offset = offset;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Write
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Deletes this attachment, so makes it virtual.
	 */
	public void delete() throws Exception {
		if (m_invocation != null) {
			m_editor.removeEnclosingStatement(m_invocation);
			m_invocation = null;
			m_anchorComponent = null;
		}
	}

	/**
	 * Writes changes from fields to AST/source.
	 */
	public void write() throws Exception {
		NodeTarget target = getRequiredNodeTarget();
		String componentSource = m_component.getVariableSupport().getReferenceExpression(target);
		// ensure putConstraint()
		if (m_invocation == null) {
			String arguments =
					StringUtils.join(new String[]{
							getSpringSideSource(m_side),
							componentSource,
							"0",
							"(java.lang.String) null",
					"null"}, ", ");
			StatementTarget statementTarget = target.getStatementTarget();
			m_invocation = m_layout.addMethodInvocation(statementTarget, PUT_CONSTRAINT, arguments);
			m_component.addRelatedNodes(m_invocation);
		}
		// move putConstraint() to target
		{
			Statement invocationStatement = AstNodeUtils.getEnclosingStatement(m_invocation);
			StatementTarget statementTarget = target.getStatementTarget();
			m_editor.moveStatement(invocationStatement, statementTarget);
		}
		// update putConstraint()
		{
			String offsetSource = IntegerConverter.INSTANCE.toJavaSource(m_layout, m_offset);
			String anchorSideSource = getSpringSideSource(m_anchorSide);
			String anchorComponentSource =
					m_anchorComponent.getVariableSupport().getReferenceExpression(target);
			// replace arguments
			List<Expression> arguments = DomGenerics.arguments(m_invocation);
			m_editor.replaceExpression(arguments.get(1), componentSource);
			m_editor.replaceExpression(arguments.get(2), offsetSource);
			m_editor.replaceExpression(arguments.get(3), anchorSideSource);
			m_editor.replaceExpression(arguments.get(4), anchorComponentSource);
			// add related node
			m_component.addRelatedNodes(m_invocation);
			m_anchorComponent.addRelatedNodes(m_invocation);
		}
	}

	/**
	 * Does refreshing after attachment's write().
	 */
	private void writeAndRefresh() throws Exception {
		write();
		ExecutionUtils.refresh(m_component);
	}

	/**
	 * Component or its anchor was moved (in same parent), so this attachment should be updated.
	 */
	public void adjustAfterComponentMove() throws Exception {
		write();
	}

	/**
	 * @return the {@link StatementTarget} based {@link NodeTarget} such that layout, component and
	 *         anchor can be referenced at it.
	 */
	private NodeTarget getRequiredNodeTarget() throws Exception {
		StatementTarget statementTarget =
				JavaInfoUtils.getStatementTarget_whenAllCreated(List.of(
						m_layout,
						m_component,
						m_anchorComponent));
		statementTarget = updateTargetToSortAttachments(statementTarget);
		return new NodeTarget(statementTarget);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Side ordering utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static final int[] sides = {
			PositionConstants.TOP,
			PositionConstants.LEFT,
			PositionConstants.BOTTOM,
			PositionConstants.RIGHT};

	private StatementTarget updateTargetToSortAttachments(StatementTarget target) {
		Statement statement = target.getStatement();
		if (target.isAfter() && statement != null) {
			while (true) {
				Statement nextStatement = getNextStatement_ifBeforeSide(statement);
				if (nextStatement != statement) {
					statement = nextStatement;
				} else {
					break;
				}
			}
		}
		target = new StatementTarget(statement, false);
		return target;
	}

	private Statement getNextStatement_ifBeforeSide(Statement statement) {
		Statement nextStatement = AstNodeUtils.getNextStatement(statement);
		SpringAttachmentInfo attachment = getCorrespondingSide(nextStatement);
		if (attachment != null) {
			if (isBeforeSide(attachment.m_side, m_side)) {
				statement = nextStatement;
			}
		}
		return statement;
	}

	private SpringAttachmentInfo getCorrespondingSide(Statement statement) {
		for (int i = 0; i < sides.length; i++) {
			int side = sides[i];
			SpringAttachmentInfo attachment = m_layout.getAttachment(m_component, side);
			MethodInvocation invocation = attachment.m_invocation;
			if (invocation != null) {
				if (AstNodeUtils.getEnclosingStatement(invocation) == statement) {
					return attachment;
				}
			}
		}
		return null;
	}

	private static boolean isBeforeSide(int side, int nextSide) {
		return getSideOrder(side) - getSideOrder(nextSide) < 0;
	}

	private static int getSideOrder(int side) {
		for (int i = 0; i < sides.length; i++) {
			if (sides[i] == side) {
				return 1 + i;
			}
		}
		return 0;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	protected Property[] getProperties() throws Exception {
		// offset
		Property offsetProperty = new Property(IntegerPropertyEditor.INSTANCE) {
			@Override
			public String getTitle() {
				return "offset";
			}

			@Override
			public boolean isModified() throws Exception {
				return !isVirtual();
			}

			@Override
			public Object getValue() throws Exception {
				if (isVirtual()) {
					return null;
				}
				return getOffset();
			}

			@Override
			public void setValue(Object value) throws Exception {
				if (value instanceof Integer) {
					materialize();
					setOffset((Integer) value);
					writeAndRefresh();
				}
			}
		};
		// anchor component
		Property anchorProperty = new Property(new ComponentEditor()) {
			@Override
			public String getTitle() {
				return "anchor";
			}

			@Override
			public boolean isModified() throws Exception {
				return !isVirtual();
			}

			@Override
			public Object getValue() throws Exception {
				if (isVirtual()) {
					return null;
				}
				return getAnchorComponent();
			}

			@Override
			public void setValue(Object value) throws Exception {
				if (value instanceof ComponentInfo) {
					materialize();
					setAnchorComponent((ComponentInfo) value);
					writeAndRefresh();
				}
			}
		};
		// anchor side
		Property sideProperty;
		{
			StaticFieldPropertyEditor editor = new StaticFieldPropertyEditor();
			String[] fieldDescriptions =
					PlacementUtils.isHorizontalSide(getSide())
					? new String[]{"EAST", "WEST",}
			: new String[]{"NORTH", "SOUTH"};
			editor.configure(SpringLayout.class, fieldDescriptions);
			sideProperty = new Property(editor) {
				@Override
				public String getTitle() {
					return "side";
				}

				@Override
				public boolean isModified() throws Exception {
					return !isVirtual();
				}

				@Override
				public Object getValue() throws Exception {
					if (isVirtual()) {
						return null;
					}
					return getSpringSide(getAnchorSide());
				}

				@Override
				public void setValue(Object value) throws Exception {
					if (value instanceof String) {
						materialize();
						setAnchorSide(getFrameworkSide((String) value));
						writeAndRefresh();
					}
				}
			};
		}
		return new Property[]{offsetProperty, anchorProperty, sideProperty};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		if (isVirtual()) {
			return "<none>";
		} else {
			return ExecutionUtils.runObjectIgnore(() -> "("
					+ getOffset()
					+ ", "
					+ CodeUtils.getShortClass(getSpringSideSource(getAnchorSide()))
					+ ", "
					+ getAnchorComponent().getPresentation().getText()
					+ ")",
					"<exception>");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Inner classes
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * An editor for choosing components in properties.
	 */
	private final class ComponentEditor extends AbstractComboPropertyEditor {
		private final List<ComponentInfo> m_components = new ArrayList<>();

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected String getText(Property property) throws Exception {
			ComponentInfo component = (ComponentInfo) property.getValue();
			if (component != null) {
				return getText(component);
			}
			return null;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Combo
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void addItems(Property property, CCombo combo) throws Exception {
			m_components.clear();
			// parent
			{
				ContainerInfo container = m_layout.getContainer();
				m_components.add(container);
				combo.add(getText(container));
			}
			// siblings
			List<ComponentInfo> components = m_layout.getComponents();
			for (ComponentInfo component : components) {
				if (component != m_component) {
					m_components.add(component);
					combo.add(getText(component));
				}
			}
		}

		@Override
		protected void selectItem(Property property, CCombo combo) throws Exception {
			combo.setText(getText(property));
		}

		@Override
		protected void toPropertyEx(Property property, CCombo combo, int index) throws Exception {
			ComponentInfo component = m_components.get(index);
			property.setValue(component);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Utils
		//
		////////////////////////////////////////////////////////////////////////////
		private String getText(ComponentInfo component) throws Exception {
			return component.getPresentation().getText();
		}
	}
}
