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
package org.eclipse.wb.tests.designer.databinding.xwt;

import org.eclipse.wb.internal.rcp.databinding.xwt.ui.property.BindingsProperty;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

/**
 * @author sablin_aa
 *
 */
public class DatabindingsProviderTest extends XwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_property() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Button text='Button' wbp:name='component'/>",
        "</Shell>");
    refresh();
    // prepare models
    ControlInfo button = getObjectByName("component");
    BindingsProperty property = (BindingsProperty) button.getPropertyByTitle("bindings");
    assertNotNull(property);
  }

  public void test_property_disabled() throws Exception {
    /*setFileContentSrc(
    	"test/client/MyFormPanel.java",
    	getTestSource(
    		"public class MyFormPanel extends FormPanel {",
    		"  public MyFormPanel() {",
    		"  }",
    		"}"));
    setFileContentSrc(
    	"test/client/MyFormPanel.wbp-component.xml",
    	getSourceDQ(
    		"<?xml version='1.0' encoding='UTF-8'?>",
    		"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
    		"  <parameters>",
    		"    <parameter name='databinding.disable'>true</parameter>",
    		"  </parameters>",
    		"</component>"));
    waitForAutoBuild();*/
    prepareMyComponent(new String[]{""}, new String[]{
        "  <parameters>",
        "    <parameter name='databinding.disable'>true</parameter>",
        "  </parameters>"});
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <t:MyComponent wbp:name='component'/>",
        "</Shell>");
    refresh();
    // prepare models
    ControlInfo component = getObjectByName("component");
    assertNull(component.getPropertyByTitle("bindings"));
  }
}