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
package org.eclipse.wb.internal.rcp.databinding.preferences;

/**
 * Contains various preference constants.
 *
 * @author lobas_av
 * @coverage bindings.rcp.preferences
 */
public interface IPreferenceConstants {
	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * When <code>true</code>, code generator add <code>initDataBindings()</code> invocation to GUI
	 * creation method.
	 */
	String ADD_INVOKE_INITDB_TO_GUI = "ADD_INVOKE_INITDB_TO_GUI";
	/**
	 * When <code>true</code>, code generator assign return value of <code>initDataBindings()</code>
	 * invocation to field.
	 */
	String ADD_INITDB_TO_FIELD = "ADD_INITDB_TO_FIELD";
	/**
	 * When <code>true</code>, code generator add <code>initDataBindings()</code> invocation to
	 * <code>org.eclipse.swt.widgets.Composite</code> constructor.
	 */
	String ADD_INVOKE_INITDB_TO_COMPOSITE_CONSTRUCTOR = "ADD_INVOKE_INITDB_TO_COMPOSITE_CONSTRUCTOR";
	/**
	 * Code generator generate <code>initDataBindings()</code> with access:
	 * <code>public/protected/private/package private</code>.
	 */
	String INITDB_GENERATE_ACCESS = "INITDB_GENERATE_ACCESS";
	/**
	 * When {@code true}, code generator wrap body of <code>initDataBindings()</code> into try/catch
	 * block.
	 */
	String INITDB_TRY_CATCH = "INITDB_TRY_CATCH";
	/**
	 * When {@code true}, code generator work over new model.
	 */
	String GENERATE_CODE_FOR_VERSION_1_3 = "GENERATE_CODE_FOR_VERSION_1_3";
	/**
	 * When {@code true}, code generator create code for JFace viewers over ViewerSupport.
	 */
	String USE_VIEWER_SUPPORT = "USE_VIEWER_SUPPORT";
	/**
	 * Default value for new value strategy object.
	 */
	String UPDATE_VALUE_STRATEGY_DEFAULT = "UPDATE_VALUE_STRATEGY_DEFAULT";
	/**
	 * Default value for new list strategy object.
	 */
	String UPDATE_LIST_STRATEGY_DEFAULT = "UPDATE_LIST_STRATEGY_DEFAULT";
	/**
	 * Default value for new set strategy object.
	 */
	String UPDATE_SET_STRATEGY_DEFAULT = "UPDATE_SET_STRATEGY_DEFAULT";
	////////////////////////////////////////////////////////////////////////////
	//
	// Access values
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Public access (java keyword "public").
	 */
	int PUBLIC_ACCESS = 0;
	/**
	 * Protected access (java keyword "protected").
	 */
	int PROTECTED_ACCESS = 1;
	/**
	 * Private access (java keyword "private").
	 */
	int PRIVATE_ACCESS = 2;
	/**
	 * Package private access.
	 */
	int PACKAGE_PRIVATE_ACCESS = 3;
}