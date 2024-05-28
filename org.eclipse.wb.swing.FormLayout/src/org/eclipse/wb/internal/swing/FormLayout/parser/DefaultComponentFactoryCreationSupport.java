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
package org.eclipse.wb.internal.swing.FormLayout.parser;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ILiveCreationSupport;
import org.eclipse.wb.internal.core.model.creation.OpaqueCreationSupport;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.FactoryAccessor;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * {@link CreationSupport} for JGoodies <code>DefaultComponentFactory</code>.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class DefaultComponentFactoryCreationSupport extends OpaqueCreationSupport
implements
ILiveCreationSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public DefaultComponentFactoryCreationSupport(Expression expression) {
		super(expression);
	}

	public DefaultComponentFactoryCreationSupport(String source) {
		super("com.jgoodies.forms.factories.DefaultComponentFactory.getInstance()." + source);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ILiveCreationSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CreationSupport getLiveComponentCreation() {
		String source = add_getSource(null);
		return new OpaqueCreationSupport(source);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Accessors
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addAccessors(GenericPropertyDescription propertyDescription,
			List<ExpressionAccessor> accessors) {
		if (propertyDescription.getId().equals("setText(java.lang.String)")) {
			String signature = getSignature();
			if (signature.equals("createLabel(java.lang.String)")
					|| signature.equals("createTitle(java.lang.String)")) {
				String defaultSource = StringConverter.INSTANCE.toJavaSource(m_javaInfo, "");
				ExpressionAccessor accessor = new FactoryAccessor(0, defaultSource);
				accessors.add(accessor);
			}
		}
	}

	private String getSignature() {
		MethodInvocation invocation = (MethodInvocation) getNode();
		return AstNodeUtils.getMethodSignature(invocation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Permissions
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canReorder() {
		return true;
	}

	@Override
	public boolean canReparent() {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void delete() throws Exception {
		JavaInfoUtils.deleteJavaInfo(m_javaInfo, true);
	}
}
