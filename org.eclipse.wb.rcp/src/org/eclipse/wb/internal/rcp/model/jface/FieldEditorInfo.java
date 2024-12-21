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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.model.rcp.WorkbenchPartLikeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Model for {@link FieldEditor}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class FieldEditorInfo extends AbstractComponentInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FieldEditorInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// support for copy/paste
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void clipboardCopy_Argument(JavaInfo javaInfo,
					ParameterDescription parameter,
					Expression argument,
					String[] source) throws Exception {
				if (javaInfo instanceof FieldEditorInfo && isParameterComposite(parameter)) {
					source[0] = "%parentComposite%";
				}
			}
		});
		// remember Control's of FieldEditor
		addBroadcastListener(new EvaluationEventListener() {
			private final List<Control> m_beforeControls = new ArrayList<>();

			@Override
			public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
				if (node == getCreationSupport().getNode()) {
					m_beforeControls.clear();
					appendControls(getShell(), m_beforeControls);
				}
			}

			@Override
			public void evaluateAfter(EvaluationContext context, ASTNode node) throws Exception {
				if (getRoot() instanceof PreferencePageInfo || getRoot() instanceof WorkbenchPartLikeInfo) {
					ASTNode creationNode = getCreationSupport().getNode();
					Statement creationStatement = AstNodeUtils.getEnclosingStatement(creationNode);
					if (node == creationStatement) {
						appendControls(getShell(), m_controls);
						m_controls.removeAll(m_beforeControls);
					}
				}
			}

			/**
			 * @return the top level {@link Shell}
			 */
			private Shell getShell() {
				ObjectInfo root = EditorState.getActiveJavaInfo().getRoot();
				if (root instanceof PreferencePageInfo) {
					return ((PreferencePageInfo) root).getShell();
				} else if (root instanceof WorkbenchPartLikeInfo) {
					return ((WorkbenchPartLikeInfo) root).getShell();
				}
				return null;
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createExposedChildren() throws Exception {
		super.createExposedChildren();
		String exposedMethodsString = JavaInfoUtils.getParameter(this, "FieldEditor.exposeMethods");
		if (exposedMethodsString != null) {
			for (String exposedMethodName : StringUtils.split(exposedMethodsString)) {
				Method exposeMethod =
						ReflectionUtils.getMethodBySignature(
								getDescription().getComponentClass(),
								exposedMethodName + "(org.eclipse.swt.widgets.Composite)");
				Assert.isNotNull(
						exposeMethod,
						"Unable to find expose method %s(Composite) for %s.",
						exposedMethodName,
						getDescription().getComponentClass());
				// create sub-component
				ControlInfo subComponent =
						(ControlInfo) JavaInfoUtils.createJavaInfo(
								getEditor(),
								exposeMethod.getReturnType(),
								new FieldEditorSubComponentCreationSupport(this, exposeMethod));
				{
					VariableSupport variableSupport =
							new FieldEditorSubComponentVariableSupport(subComponent, this, exposeMethod);
					subComponent.setVariableSupport(variableSupport);
				}
				subComponent.setAssociation(new ImplicitObjectAssociation(this));
				// should be initialized
				Assert.isNotNull(
						subComponent.getObject(),
						"Sub-component %s is not initialized in %s.",
						exposedMethodName,
						this);
				// add sub-component
				addChild(subComponent);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link ParameterDescription} is "parentComposite" that
	 *         should be passed into each {@link FieldEditor} constructor.
	 */
	static boolean isParameterComposite(ParameterDescription parameter) {
		return parameter.hasTrueTag("parentComposite");
	}

	/**
	 * @return the {@link ControlInfo} children (exposed).
	 */
	public List<ControlInfo> getChildControls() {
		return getChildren(ControlInfo.class);
	}

	@Override
	public Object getComponentObject() {
		if (!m_controls.isEmpty()) {
			if (m_controls.get(0) instanceof Composite) {
				return m_controls.get(0);
			} else {
				return ((AbstractComponentInfo) getParent()).getComponentObject();
			}
		}
		return super.getComponentObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public List<ObjectInfo> getChildrenGraphical() throws Exception {
			return Collections.emptyList();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<Control> m_controls = new ArrayList<>();

	/**
	 * Appends direct/indirect children of given {@link Control}.
	 */
	private static void appendControls(Control control, List<Control> controls) throws Exception {
		if (control == null) {
			return;
		}
		controls.add(control);
		if (control instanceof Composite composite) {
			for (Control child : composite.getChildren()) {
				appendControls(child, controls);
			}
		}
	}

	@Override
	public void refresh_dispose() throws Exception {
		m_controls.clear();
		super.refresh_dispose();
	}

	@Override
	protected void refresh_fetch() throws Exception {
		// prepare enclosing bounds for FieldEditor Control's
		Assert.isLegal(!m_controls.isEmpty());
		if (m_controls.get(0) instanceof Composite composite) {
			ControlInfo.refresh_fetch(this, composite, null);
		} else {
			// keep only "top level" Control's, remove any children
			for (Iterator<Control> I = m_controls.iterator(); I.hasNext();) {
				Control control = I.next();
				do {
					control = control.getParent();
					if (m_controls.contains(control)) {
						I.remove();
						break;
					}
				} while (control != null);
			}
			// prepare "bounds" as intersection of all Control's
			Rectangle bounds = new Rectangle();
			for (Control control : m_controls) {
				Rectangle controlBounds = new Rectangle(control.getBounds());
				if (bounds.isEmpty()) {
					bounds = controlBounds;
				} else {
					bounds.union(controlBounds);
				}
			}
			// model bounds
			setModelBounds(bounds.getCopy());
			// convert into "shot"
			{
				Control control = m_controls.get(0);
				Control parentControl = (Control) ((AbstractComponentInfo) getParent()).getComponentObject();
				Point controlLocation = CoordinateUtils.getDisplayLocation(control, bounds.x, bounds.y);
				Point parentLocation = CoordinateUtils.getDisplayLocation(parentControl);
				bounds.x = controlLocation.x - parentLocation.x;
				bounds.y = controlLocation.y - parentLocation.y;
			}
			// remember
			setBounds(bounds);
		}
		// process children
		super.refresh_fetch();
	}
}
