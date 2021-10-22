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
package org.eclipse.wb.tests.designer.XWT.model.forms.table;

import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.IPreferenceConstants;
import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Test for {@link TableWrapLayout} and special parameters for grab/alignment.
 *
 * @author scheglov_ke
 */
public class TabelWrapLayoutParametersTest extends XwtModelTest {
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
  // Source
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTestSource_namespaces() {
    return super.getTestSource_namespaces()
        + " xmlns:f='clr-namespace:org.eclipse.ui.forms.widgets'";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Text} widget is marked as required horizontal grab/fill.
   */
  public void test_CREATE_Text() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newText = createObject("org.eclipse.swt.widgets.Text");
    layout.command_CREATE(newText, 0, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Text x:Style='BORDER'>",
        "    <Text.layoutData>",
        "      <f:TableWrapData"
            + " grabHorizontal='true'"
            + " align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
        "    </Text.layoutData>",
        "  </Text>",
        "</Shell>");
  }

  /**
   * Test that horizontal grab/fill {@link Text} can be disabled.
   */
  public void test_CREATE_Text_disabled() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    PreferencesRepairer preferences =
        new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
    try {
      preferences.setValue(IPreferenceConstants.P_ENABLE_GRAB, false);
      ControlInfo newText = createObject("org.eclipse.swt.widgets.Text");
      layout.command_CREATE(newText, 0, false, 0, false);
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<Shell>",
          "  <Shell.layout>",
          "    <f:TableWrapLayout wbp:name='layout'/>",
          "  </Shell.layout>",
          "  <Text x:Style='BORDER'/>",
          "</Shell>");
    } finally {
      preferences.restore();
    }
  }

  /**
   * {@link Table} widget is marked as required horizontal/vertical grab/fill.
   */
  public void test_CREATE_Table() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newTable = createObject("org.eclipse.swt.widgets.Table");
    layout.command_CREATE(newTable, 0, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Table x:Style='BORDER | FULL_SELECTION' linesVisible='true' headerVisible='true'>",
        "    <Table.layoutData>",
        "      <f:TableWrapData"
            + " grabHorizontal='true' align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'"
            + " grabVertical='true' valign='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
        "    </Table.layoutData>",
        "  </Table>",
        "</Shell>");
  }

  /**
   * {@link Label} widget is marked as "right" aligned and next widget is {@link Text}, so when add
   * {@link Label} before {@link Text}, use {@link TableWrapData#RIGHT} alignment.
   */
  public void test_CREATE_LabelBeforeText() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Text/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newLabel = createObject("org.eclipse.swt.widgets.Label");
    layout.command_CREATE(newLabel, 0, true, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label text='New Label'>",
        "    <Label.layoutData>",
        "      <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).RIGHT'/>",
        "    </Label.layoutData>",
        "  </Label>",
        "  <Text/>",
        "</Shell>");
  }

  /**
   * Check that automatic "right alignment" feature for {@link Label} can be disabled.
   */
  public void test_CREATE_LabelBeforeText_disabled() throws Exception {
    parse(
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Text/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    PreferencesRepairer preferences =
        new PreferencesRepairer(ToolkitProvider.DESCRIPTION.getPreferences());
    try {
      preferences.setValue(IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT, false);
      ControlInfo newLabel = createObject("org.eclipse.swt.widgets.Label");
      layout.command_CREATE(newLabel, 0, true, 0, false);
      assertXML(
          "<Shell>",
          "  <Shell.layout>",
          "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
          "  </Shell.layout>",
          "  <Label text='New Label'/>",
          "  <Text/>",
          "</Shell>");
    } finally {
      preferences.restore();
    }
  }

  /**
   * {@link Label} widget is marked as "right" aligned and next widget is {@link Text}, so when add
   * {@link Text} after {@link Label}, use {@link TableWrapData#RIGHT} alignment for {@link Label}.
   */
  public void test_CREATE_TextAfterLabel() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout'/>",
        "  </Shell.layout>",
        "  <Label text='New Label'/>",
        "</Shell>");
    refresh();
    TableWrapLayoutInfo layout = getObjectByName("layout");
    //
    ControlInfo newText = createObject("org.eclipse.swt.widgets.Text");
    layout.command_CREATE(newText, 1, false, 0, false);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <f:TableWrapLayout wbp:name='layout' numColumns='2'/>",
        "  </Shell.layout>",
        "  <Label text='New Label'>",
        "    <Label.layoutData>",
        "      <f:TableWrapData align='(org.eclipse.ui.forms.widgets.TableWrapData).RIGHT'/>",
        "    </Label.layoutData>",
        "  </Label>",
        "  <Text x:Style='BORDER'>",
        "    <Text.layoutData>",
        "      <f:TableWrapData"
            + " grabHorizontal='true' align='(org.eclipse.ui.forms.widgets.TableWrapData).FILL'/>",
        "    </Text.layoutData>",
        "  </Text>",
        "</Shell>");
  }
}