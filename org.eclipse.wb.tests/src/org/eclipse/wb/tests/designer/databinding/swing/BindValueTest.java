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
package org.eclipse.wb.tests.designer.databinding.swing;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.bindings.AutoBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.UpdateStrategyInfo;
import org.eclipse.wb.internal.swing.model.component.JPanelInfo;

import java.util.List;

/**
 * @author sablin_aa
 *
 */
public class BindValueTest extends AbstractBindingTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_strategy_policy_variable() throws Exception {
    JPanelInfo shell =
        DatabindingTestUtils.parseTestSource(
            this,
            new String[]{
                "public class Test extends JPanel {",
                "  public static class MyBean {",
                "    protected String name;",
                "    public String getName() {",
                "      return name;",
                "    }",
                "    public void setName(String newName) {",
                "      this.name = newName;",
                "    }",
                "  }",
                "  public static void main(String[] args) {",
                "    JFrame frame = new JFrame();",
                "    frame.getContentPane().add(new Test(), BorderLayout.CENTER);",
                "    frame.setMinimumSize(new Dimension(500, 500));",
                "    frame.setVisible(true);",
                "    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);",
                "  }",
                "  protected MyBean myBean = new MyBean();",
                "  private JLabel label;",
                "  private JTextField textField;",
                "  private JButton button;",
                "  public Test() {",
                "    label = new JLabel();",
                "    add(label);",
                "    textField = new JTextField();",
                "    add(textField);",
                "    button = new JButton();",
                "    add(button);",
                "    initDataBindings();",
                "  }",
                "  AutoBinding.UpdateStrategy m_strategy = AutoBinding.UpdateStrategy.READ_ONCE;",
                "  protected void initDataBindings() {",
                "    {",
                "      BeanProperty<MyBean, String> modelBeanProperty = BeanProperty.create(\"name\");",
                "      BeanProperty<JLabel, String> labelBeanProperty = BeanProperty.create(\"text\");",
                "      AutoBinding.UpdateStrategy strategy = AutoBinding.UpdateStrategy.READ;",
                "      AutoBinding<MyBean, String, JLabel, String> autoBinding = Bindings.createAutoBinding(",
                "        strategy, myBean, modelBeanProperty, label, labelBeanProperty);",
                "      autoBinding.bind();",
                "    }",
                "    {",
                "      BeanProperty<MyBean, String> modelBeanProperty = BeanProperty.create(\"name\");",
                "      BeanProperty<JTextField, String> textFieldBeanProperty = BeanProperty.create(\"text\");",
                "      AutoBinding<MyBean, String, JTextField, String> autoBinding = Bindings.createAutoBinding(",
                "        AutoBinding.UpdateStrategy.READ_WRITE, myBean, modelBeanProperty, textField, textFieldBeanProperty);",
                "      autoBinding.bind();",
                "    }",
                "    {",
                "      BeanProperty<MyBean, String> modelBeanProperty = BeanProperty.create(\"name\");",
                "      BeanProperty<JButton, String> buttonBeanProperty = BeanProperty.create(\"text\");",
                "      AutoBinding<MyBean, String, JButton, String> autoBinding = Bindings.createAutoBinding(",
                "        m_strategy, myBean, modelBeanProperty, button, buttonBeanProperty);",
                "      autoBinding.bind();",
                "    }",
                "  }",
                "}"});
    assertNotNull(shell);
    //
    DatabindingsProvider provider = getDatabindingsProvider();
    List<IBindingInfo> bindings = provider.getBindings();
    //
    assertNotNull(bindings);
    assertEquals(3, bindings.size());
    // label bindings
    {
      assertInstanceOf(AutoBindingInfo.class, bindings.get(0));
      AutoBindingInfo binding = (AutoBindingInfo) bindings.get(0);
      //
      UpdateStrategyInfo strategyInfo = binding.getStrategyInfo();
      assertEquals("READ", strategyInfo.getStrategyValue());
    }
    // text bindings
    {
      assertInstanceOf(AutoBindingInfo.class, bindings.get(1));
      AutoBindingInfo binding = (AutoBindingInfo) bindings.get(1);
      //
      UpdateStrategyInfo strategyInfo = binding.getStrategyInfo();
      assertEquals("READ_WRITE", strategyInfo.getStrategyValue());
    }
    // button bindings
    {
      assertInstanceOf(AutoBindingInfo.class, bindings.get(2));
      AutoBindingInfo binding = (AutoBindingInfo) bindings.get(2);
      //
      UpdateStrategyInfo strategyInfo = binding.getStrategyInfo();
      assertEquals("READ_ONCE", strategyInfo.getStrategyValue());
    }
  }
}