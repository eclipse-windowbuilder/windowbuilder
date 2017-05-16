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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Preference constants for {@link EventsProperty}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
public interface IPreferenceConstants {
  String BASE = "property.events.";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Other
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Is <code>true</code> if parameters in even handlers should be declared as <code>final</code>.
   */
  String P_FINAL_PARAMETERS = BASE + "finalParameters";
  /**
   * Is <code>true</code> icon of component with events should be decorated.
   */
  String P_DECORATE_ICON = BASE + "decorateIcon";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Stub
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Is <code>true</code> if event handling methods should be generated to call a stub method.
   */
  String P_CREATE_STUB = BASE + "stubCreate";
  /**
   * The template that will be used to generate the name of an stub method.
   */
  String P_STUB_NAME_TEMPLATE = "stubName";
  /**
   * Is <code>true</code> if stub {@link MethodDeclaration} should be removed on {@link JavaInfo}
   * remove.
   */
  String P_DELETE_STUB = BASE + "stubDelete";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Code type
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Style of code for event listener.
   */
  String P_CODE_TYPE = BASE + "codeType";
  /**
   * Generate anonymous class.
   */
  int V_CODE_ANONYMOUS = 0;
  /**
   * Generate inner class.
   */
  int V_CODE_INNER_CLASS = 1;
  /**
   * Implement listener interface by main {@link TypeDeclaration}.
   */
  int V_CODE_INTERFACE = 2;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner class
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Position for new inner class for listener.
   */
  String P_INNER_POSITION = BASE + "innerClassPosition";
  /**
   * First body declaration in class.
   */
  int V_INNER_FIRST = 0;
  /**
   * Last body declaration in class.
   */
  int V_INNER_LAST = 1;
  /**
   * Before first inner listener class, or first in class.
   */
  int V_INNER_BEFORE = 2;
  /**
   * After last inner listener class, or last in class.
   */
  int V_INNER_AFTER = 3;
  /**
   * The template that will be used to generate the name of an stub method.
   */
  String P_INNER_NAME_TEMPLATE = "innerClassName";
}
