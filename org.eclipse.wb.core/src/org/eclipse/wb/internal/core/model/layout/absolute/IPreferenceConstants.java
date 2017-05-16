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
package org.eclipse.wb.internal.core.model.layout.absolute;

/**
 * Preference constants for absolute-based layouts. Descendants may extend this interface.
 * Preference initializers are concrete for every layout support.
 *
 * FIXME use small, nice looking names for preferences, see for example
 * {@link org.eclipse.wb.internal.core.model.property.event.IPreferenceConstants} or even
 * IAntUIPreferenceConstants.
 *
 * @author mitin_aa
 * @coverage core.model.layout.absolute
 */
public interface IPreferenceConstants {
  ////////////////////////////////////////////////////////////////////////////
  //
  // editing mode
  //
  ////////////////////////////////////////////////////////////////////////////
  String P_USE_FREE_MODE = "P_USE_FREE_MODE";
  String P_USE_GRID = "P_USE_GRID";
  String P_CREATION_FLOW = "P_CREATION_FLOW";
  ////////////////////////////////////////////////////////////////////////////
  //
  // grid options
  //
  ////////////////////////////////////////////////////////////////////////////
  String P_DISPLAY_GRID = "P_DISPLAY_GRID";
  String P_GRID_STEP_X = "P_GRID_STEP_X";
  String P_GRID_STEP_Y = "P_GRID_STEP_Y";
  ////////////////////////////////////////////////////////////////////////////
  //
  // component gaps
  //
  ////////////////////////////////////////////////////////////////////////////
  String P_COMPONENT_GAP_LEFT = "P_COMPONENT_GAP_LEFT";
  String P_COMPONENT_GAP_RIGHT = "P_COMPONENT_GAP_RIGHT";
  String P_COMPONENT_GAP_TOP = "P_COMPONENT_GAP_TOP";
  String P_COMPONENT_GAP_BOTTOM = "P_COMPONENT_GAP_BOTTOM";
  ////////////////////////////////////////////////////////////////////////////
  //
  // container gaps
  //
  ////////////////////////////////////////////////////////////////////////////
  String P_CONTAINER_GAP_LEFT = "P_CONTAINER_GAP_LEFT";
  String P_CONTAINER_GAP_RIGHT = "P_CONTAINER_GAP_RIGHT";
  String P_CONTAINER_GAP_TOP = "P_CONTAINER_GAP_TOP";
  String P_CONTAINER_GAP_BOTTOM = "P_CONTAINER_GAP_BOTTOM";
  ////////////////////////////////////////////////////////////////////////////
  //
  // misc
  //
  ////////////////////////////////////////////////////////////////////////////
  String P_DISPLAY_LOCATION_SIZE_HINTS = "P_DISPLAY_LOCATION_SIZE_HINTS";
  String P_AUTOSIZE_ON_PROPERTY_CHANGE = "absoluteLayout.autoSize.onTextImage";
}
