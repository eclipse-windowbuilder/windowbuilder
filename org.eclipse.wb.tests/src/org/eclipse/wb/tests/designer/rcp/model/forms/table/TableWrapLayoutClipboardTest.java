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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.model.forms.AbstractFormsTest;

/**
 * Test for {@link TableWrapLayoutInfo} and copy/paste.
 *
 * @author scheglov_ke
 */
public class TableWrapLayoutClipboardTest extends AbstractFormsTest {
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
  public void test_simpleSingleControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      {",
            "        TableWrapLayout layout = new TableWrapLayout();",
            "        layout.topMargin = 20;",
            "        composite.setLayout(layout);",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    // prepare memento
    JavaInfoMemento memento;
    {
      CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
      memento = JavaInfoMemento.createMemento(composite);
    }
    // do paste
    CompositeInfo newComposite = (CompositeInfo) memento.create(shell);
    rowLayout.command_CREATE(newComposite, null);
    memento.apply();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      {",
        "        TableWrapLayout layout = new TableWrapLayout();",
        "        layout.topMargin = 20;",
        "        composite.setLayout(layout);",
        "      }",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "      }",
        "    }",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      {",
        "        TableWrapLayout tableWrapLayout = new TableWrapLayout();",
        "        tableWrapLayout.topMargin = 20;",
        "        composite.setLayout(tableWrapLayout);",
        "      }",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_grid2x2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      {",
            "        TableWrapLayout layout = new TableWrapLayout();",
            "        layout.numColumns = 2;",
            "        composite.setLayout(layout);",
            "      }",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "        {",
            "          TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);",
            "          button.setLayoutData(tableWrapData);",
            "        }",
            "      }",
            "      new Label(composite, SWT.NONE);",
            "      new Label(composite, SWT.NONE);",
            "      {",
            "        Button button = new Button(composite, SWT.NONE);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
    // prepare memento
    JavaInfoMemento memento;
    {
      CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
      memento = JavaInfoMemento.createMemento(composite);
    }
    // do paste
    CompositeInfo newComposite = (CompositeInfo) memento.create(shell);
    rowLayout.command_CREATE(newComposite, null);
    memento.apply();
    assertEditor(
        "class Test extends Shell {",
        "  Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      {",
        "        TableWrapLayout layout = new TableWrapLayout();",
        "        layout.numColumns = 2;",
        "        composite.setLayout(layout);",
        "      }",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        button.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));",
        "      }",
        "      new Label(composite, SWT.NONE);",
        "      new Label(composite, SWT.NONE);",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "      }",
        "    }",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "      {",
        "        TableWrapLayout tableWrapLayout = new TableWrapLayout();",
        "        tableWrapLayout.numColumns = 2;",
        "        composite.setLayout(tableWrapLayout);",
        "      }",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "        button.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));",
        "      }",
        "      new Label(composite, SWT.NONE);",
        "      new Label(composite, SWT.NONE);",
        "      {",
        "        Button button = new Button(composite, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}