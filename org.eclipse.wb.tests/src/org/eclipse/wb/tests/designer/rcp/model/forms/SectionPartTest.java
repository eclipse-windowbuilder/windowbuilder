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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.forms.SectionInfo;
import org.eclipse.wb.internal.rcp.model.forms.SectionPartInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.swt.graphics.Font;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link SectionPartInfo}.
 * 
 * @author scheglov_ke
 */
public class SectionPartTest extends AbstractFormsTest {
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
  // Design SectionPart
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_withFormToolkit() throws Exception {
    SectionPartInfo part =
        parseJavaInfo(
            "public class Test extends SectionPart {",
            "  public Test(Composite parent, FormToolkit toolkit, int style) {",
            "    super(parent, toolkit, style);",
            "    createClient(getSection(), toolkit);",
            "  }",
            "  private void createClient(Section section, FormToolkit toolkit) {",
            "    section.setText('New SectionPart');",
            "    Composite container = toolkit.createComposite(section);",
            "    section.setClient(container);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.ui.forms.SectionPart} {this} {}",
        "  {viewer: public org.eclipse.ui.forms.widgets.Section org.eclipse.ui.forms.SectionPart.getSection()} {viewer} {/createClient(getSection(), toolkit)/ /section.setText('New SectionPart')/ /toolkit.createComposite(section)/ /section.setClient(container)/}",
        "    {instance factory: {toolkit} createComposite(org.eclipse.swt.widgets.Composite)} {local-unique: container} {/toolkit.createComposite(section)/ /section.setClient(container)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory container}",
        "    {parameter} {toolkit} {/toolkit.createComposite(section)/ /createClient(getSection(), toolkit)/}");
    SectionInfo section = part.getSection();
    // refresh
    part.refresh();
    assertNoErrors(part);
    assertThat(part.getBounds().width).isEqualTo(600);
    assertThat(part.getBounds().height).isEqualTo(500);
    assertThat(section.getBounds().width).isEqualTo(600);
    assertThat(section.getBounds().height).isEqualTo(500);
  }

  /**
   * We should not be too strict and should allow additional parameters in constructor.
   */
  public void test_additionalConstructorParameter() throws Exception {
    SectionPartInfo part =
        parseJavaInfo(
            "public class Test extends SectionPart {",
            "  public Test(Composite parent, FormToolkit toolkit, int style, int foo) {",
            "    super(parent, toolkit, style);",
            "    createClient(getSection(), toolkit);",
            "  }",
            "  private void createClient(Section section, FormToolkit toolkit) {",
            "    Composite container = toolkit.createComposite(section);",
            "    section.setClient(container);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.ui.forms.SectionPart} {this} {}",
        "  {viewer: public org.eclipse.ui.forms.widgets.Section org.eclipse.ui.forms.SectionPart.getSection()} {viewer} {/createClient(getSection(), toolkit)/ /toolkit.createComposite(section)/ /section.setClient(container)/}",
        "    {instance factory: {toolkit} createComposite(org.eclipse.swt.widgets.Composite)} {local-unique: container} {/toolkit.createComposite(section)/ /section.setClient(container)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory container}",
        "    {parameter} {toolkit} {/toolkit.createComposite(section)/ /createClient(getSection(), toolkit)/}");
    // refresh
    part.refresh();
    assertNoErrors(part);
  }

  /**
   * Test for disposing {@link Font} of {@link SectionInfo} (should not happen).
   */
  public void test_disposing() throws Exception {
    SectionPartInfo part =
        parseJavaInfo(
            "public class Test extends SectionPart {",
            "  public Test(Composite parent, FormToolkit toolkit, int style) {",
            "    super(parent, toolkit, style);",
            "  }",
            "}");
    part.refresh();
    assertNoErrors(part);
    //
    SectionInfo sectionInfo = part.getChildren(SectionInfo.class).get(0);
    Object sectionObject = sectionInfo.getObject();
    // "font" from Section
    {
      Font font = (Font) ReflectionUtils.invokeMethod2(sectionObject, "getFont");
      assertFalse(font.isDisposed());
    }
    // "font" property of Section (default value)
    {
      Property fontProperty = sectionInfo.getPropertyByTitle("font");
      Font font = (Font) fontProperty.getValue();
      assertFalse(font.isDisposed());
    }
  }

  public void test_withFormPage() throws Exception {
    parseJavaInfo(
        "public class Test extends SectionPart {",
        "  /**",
        "  * @wbp.parser.constructor",
        "  */",
        "  public Test(FormPage formPage, Composite parent) {",
        "    this(parent, formPage.getManagedForm().getToolkit(), Section.DESCRIPTION);",
        "  }",
        "  public Test(Composite parent, FormToolkit toolkit, int style) {",
        "    super(parent, toolkit, style);",
        "    createClient(getSection(), toolkit);",
        "  }",
        "  private void createClient(Section section, FormToolkit toolkit) {",
        "    Composite container = toolkit.createComposite(section);",
        "    section.setClient(container);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.ui.forms.SectionPart} {this} {}",
        "  {viewer: public org.eclipse.ui.forms.widgets.Section org.eclipse.ui.forms.SectionPart.getSection()} {viewer} {/createClient(getSection(), toolkit)/ /toolkit.createComposite(section)/ /section.setClient(container)/}",
        "    {instance factory: {empty} createComposite(org.eclipse.swt.widgets.Composite)} {local-unique: container} {/toolkit.createComposite(section)/ /section.setClient(container)/}",
        "      {implicit-layout: absolute} {implicit-layout} {}",
        "  {instance factory container}",
        "    {opaque} {empty} {/formPage.getManagedForm().getToolkit()/ /toolkit.createComposite(section)/ /createClient(getSection(), toolkit)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Use SectionPart
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_useSectionPart() throws Exception {
    setFileContentSrc(
        "test/MySectionPart.java",
        getTestSource(
            "public class MySectionPart extends SectionPart {",
            "  public MySectionPart(Composite parent, FormToolkit toolkit, int style) {",
            "    super(parent, toolkit, style);",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    MySectionPart part = new MySectionPart(this, m_toolkit, Section.TITLE_BAR);",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/ /new MySectionPart(this, m_toolkit, Section.TITLE_BAR)/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {viewer: public org.eclipse.ui.forms.widgets.Section org.eclipse.ui.forms.SectionPart.getSection()} {viewer} {}",
        "    {new: test.MySectionPart} {local-unique: part} {/new MySectionPart(this, m_toolkit, Section.TITLE_BAR)/}",
        "  {instance factory container}",
        "    {new: org.eclipse.ui.forms.widgets.FormToolkit} {field-initializer: m_toolkit} {/new FormToolkit(Display.getDefault())/ /new MySectionPart(this, m_toolkit, Section.TITLE_BAR)/}");
    // refresh()
    shell.refresh();
    assertNoErrors(shell);
  }

  public void test_liveImage() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    SectionPartInfo newSectionPart = createJavaInfo("org.eclipse.ui.forms.SectionPart");
    SectionInfo newSection = (SectionInfo) newSectionPart.getWrapper().getWrappedInfo();
    assertNotNull(newSection.getImage());
  }

  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    shell.refresh();
    SectionPartInfo newSectionPart = createJavaInfo("org.eclipse.ui.forms.SectionPart");
    SectionInfo newSection = (SectionInfo) newSectionPart.getWrapper().getWrappedInfo();
    // create
    shell.getLayout().command_CREATE(newSection, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      SectionPart sectionPart = new SectionPart(this, new FormToolkit(Display.getCurrent()), Section.TWISTIE | Section.TITLE_BAR);",
        "      Section section = sectionPart.getSection();",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_withToolkit() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private final FormToolkit formToolkit = new FormToolkit(Display.getCurrent());",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FillLayout())/}",
        "  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
        "  {instance factory container}",
        "    {new: org.eclipse.ui.forms.widgets.FormToolkit} {field-initializer: formToolkit} {/new FormToolkit(Display.getCurrent())/}");
    shell.refresh();
    // create new
    SectionPartInfo newSectionPart = createJavaInfo("org.eclipse.ui.forms.SectionPart");
    SectionInfo newSection = (SectionInfo) newSectionPart.getWrapper().getWrappedInfo();
    // create
    shell.getLayout().command_CREATE(newSection, null);
    assertEditor(
        "public class Test extends Shell {",
        "  private final FormToolkit formToolkit = new FormToolkit(Display.getCurrent());",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      SectionPart sectionPart = new SectionPart(this, formToolkit, Section.TWISTIE | Section.TITLE_BAR);",
        "      Section section = sectionPart.getSection();",
        "      formToolkit.paintBordersFor(section);",
        "    }",
        "  }",
        "}");
  }
}