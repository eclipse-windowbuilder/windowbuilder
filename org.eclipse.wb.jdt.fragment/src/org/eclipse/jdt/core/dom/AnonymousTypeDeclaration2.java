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
package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * Wrapper of {@link AnonymousClassDeclaration} into {@link TypeDeclaration}.
 *
 * @author scheglov_ke
 */
public final class AnonymousTypeDeclaration2 extends TypeDeclaration {
	public static final String KEY = "AnonymousTypeDeclaration";
	private final AnonymousClassDeclaration m_ACD;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AnonymousTypeDeclaration2(AnonymousClassDeclaration acd) {
		super(acd.getAST());
		m_ACD = acd;
		setSourceRange(m_ACD.getStartPosition(), m_ACD.getLength());
		setParent(m_ACD, m_ACD.getLocationInParent());
		m_ACD.setProperty(KEY, this);
		// copy type binding
		{
			String key = "TYPE_BINDING";
			Object o = m_ACD.getProperty(key);
			setProperty(key, o);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TypeDeclaration
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	void accept0(ASTVisitor visitor) {
		m_ACD.accept0(visitor);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List bodyDeclarations() {
		return m_ACD.bodyDeclarations();
	}

	@Override
	void appendDebugString(StringBuffer buffer) {
		m_ACD.appendDebugString(buffer);
	}

	@Override
	ITypeBinding internalResolveBinding() {
		return m_ACD.resolveBinding();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public AnonymousClassDeclaration getACD() {
		return m_ACD;
	}
}