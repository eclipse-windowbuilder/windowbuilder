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
package org.eclipse.wb.internal.css.editors.multi;

import org.eclipse.wb.internal.core.utils.binding.editors.controls.AbstractControlActionsManager;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.StringItemDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringItemDialogField.IStringItemAdapter;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.css.Activator;
import org.eclipse.wb.internal.css.Messages;
import org.eclipse.wb.internal.css.dialogs.style.RulePreviewControl;
import org.eclipse.wb.internal.css.dialogs.style.StyleEditDialog;
import org.eclipse.wb.internal.css.editors.CssConfiguration;
import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssFactory;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;
import org.eclipse.wb.internal.css.parser.CssParser;

import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Composite for editing CSS file.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public class StylesEditComposite extends Composite {
  private IDocument m_document;
  private CssEditContext m_context;
  private final ICommandExceptionHandler m_exceptionHandler;
  private boolean m_modificationProcessing = false;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StylesEditComposite(Composite parent, int style, ICommandExceptionHandler exceptionHandler) {
    super(parent, style);
    m_exceptionHandler = exceptionHandler;
    createContents();
  }

  @Override
  public void dispose() {
    try {
      m_context.disconnect();
    } catch (CoreException e) {
      Activator.getDefault().getLog().log(
          new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "CSS Editor : "
              + e.getMessage(), e) {
            @Override
            public boolean isMultiStatus() {
              return true;
            }
          });
    }
    m_context = null;
    m_document = null;
    super.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setDocument(IDocument document) throws Exception {
    m_document = document;
    m_context = new CssEditContext(document);
    m_rulesViewer.setInput(m_context);
  }

  public IDocument getDocument() {
    return m_document;
  }

  public void setContext(CssEditContext context) {
    m_context = context;
    m_document = context.getDocument();
    m_rulesViewer.setInput(m_context);
  }

  public CssEditContext getContext() {
    return m_context;
  }

  public boolean isModificationProcessing() {
    return m_modificationProcessing;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: Activity
  //
  ////////////////////////////////////////////////////////////////////////////
  public void refreshDesign() {
    // remember selected rule
    String currentSelector = null;
    RuleWrapper wrapper = getSelectedWrapper();
    if (wrapper != null) {
      currentSelector = wrapper.getRule().getSelector().getValue();
    }
    // refresh input
    try {
      setDocument(getDocument());
      // locate rule
      if (currentSelector != null) {
        locateRule(getContext(), currentSelector);
      }
    } catch (Exception ex) {
      processErrorMessage(ex);
    }
  }

  public void disposeDesign() {
    // TODO
  }

  public void onActivate() {
    refreshDesign();
  }

  public void onDeactivate() {
    saveCurrentRule();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: area
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void createContents() {
    GridLayoutFactory.create(this).columns(2);
    createRulesGroup(this);
    createRuleGroup(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: rules list
  //
  ////////////////////////////////////////////////////////////////////////////
  protected ListViewer m_rulesViewer;
  protected StringItemDialogField m_filterField;

  /**
   * Create rule viewer.
   */
  protected void createRulesGroup(Composite parent) {
    final int columns = 3;
    Group rulesGroup = new Group(parent, SWT.NONE);
    rulesGroup.setText(Messages.StylesEditComposite_rulesGroup);
    GridLayoutFactory.create(rulesGroup).columns(columns);
    GridDataFactory.create(rulesGroup).grab().fill();
    // filter
    {
      m_filterField = new StringItemDialogField(new IStringItemAdapter() {
        public void itemPressed(DialogField field) {
          m_filterField.setText("");
        }
      });
      m_filterField.setDialogFieldListener(new IDialogFieldListener() {
        public void dialogFieldChanged(DialogField field) {
          m_rulesViewer.refresh();
        }
      });
      m_filterField.setLabelText(Messages.StylesEditComposite_selectorFilter);
      m_filterField.setItemImage(Activator.getImage("clear.gif"));
      m_filterField.setItemToolTip(Messages.StylesEditComposite_clearFilterToolTip);
      m_filterField.doFillIntoGrid(rulesGroup, columns);
    }
    // viewer
    {
      m_rulesViewer = new ListViewer(rulesGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      GridDataFactory.create(m_rulesViewer.getControl()).spanH(columns).grab().fill().hintC(80, 15);
      // providers
      m_rulesViewer.setContentProvider(getRulesContentProvider());
      m_rulesViewer.setLabelProvider(getRulesLabelProvider());
      m_rulesViewer.addFilter(getRulesFilter());
      // listeners
      m_rulesViewer.addSelectionChangedListener(getRulesSelectionChangedListener());
      m_rulesViewer.addDoubleClickListener(getRulesDoubleClickListener());
    }
    // buttons
    {
      Composite buttonsComposite = createRulesButtonsComposite(rulesGroup);
      GridDataFactory.create(buttonsComposite).spanH(columns).fill();
    }
  }

  /**
   * Rules providers.
   */
  protected IContentProvider getRulesContentProvider() {
    return new DefaultStructuredContextProvider() {
      public Object[] getElements(Object inputElement) {
        CssDocument document = ((CssEditContext) inputElement).getCssDocument();
        List<CssRuleNode> rules = document.getRules();
        RuleWrapper[] wrappers = new RuleWrapper[rules.size()];
        for (int i = 0; i < rules.size(); i++) {
          CssRuleNode rule = rules.get(i);
          wrappers[i] = new RuleWrapper(rule);
          if (m_currentWrapper != null && m_currentWrapper.getRule() == rule) {
            m_currentWrapper = wrappers[i];
          }
        }
        return wrappers;
      }
    };
  }

  protected LabelProvider getRulesLabelProvider() {
    return new LabelProvider() {
      @Override
      public String getText(Object element) {
        CssRuleNode rule = ((RuleWrapper) element).getRule();
        return rule.getSelector().getValue();
      }
    };
  }

  protected ViewerFilter getRulesFilter() {
    return new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        CssRuleNode rule = ((RuleWrapper) element).getRule();
        return rule.getSelector().getValue().toUpperCase().indexOf(
            m_filterField.getText().toUpperCase()) != -1;
      }
    };
  }

  /**
   * Rules listeners.
   */
  protected ISelectionChangedListener getRulesSelectionChangedListener() {
    return new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        showRule(getSelectedWrapper(), true);
      }
    };
  }

  protected IDoubleClickListener getRulesDoubleClickListener() {
    return new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        editSelectedRule();
      }
    };
  }

  /**
   * @return Composite with rule action buttons.
   */
  protected Composite createRulesButtonsComposite(Composite parent) {
    Composite buttonsComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(buttonsComposite).noMargins().columns(5);
    // TODO enable/disable buttons instead of check for "null" 
    // add
    createRulesButton(
        buttonsComposite,
        Messages.StylesEditComposite_addButton,
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            addNewRule();
          }
        });
    // rename
    createRulesButton(
        buttonsComposite,
        Messages.StylesEditComposite_renameButton,
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            renameSelectedRule();
          }
        });
    // edit
    createRulesButton(
        buttonsComposite,
        Messages.StylesEditComposite_editButton,
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            editSelectedRule();
          }
        });
    // remove
    createRulesButton(
        buttonsComposite,
        Messages.StylesEditComposite_removeButton,
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            removeSelectedRule();
          }
        });
    // sort
    createRulesButton(
        buttonsComposite,
        Messages.StylesEditComposite_sortButton,
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            sortRules();
          }
        });
    //
    return buttonsComposite;
  }

  /**
   * @return wrapper for currently selected rule.
   */
  protected RuleWrapper getSelectedWrapper() {
    Object selectedObject = ((IStructuredSelection) m_rulesViewer.getSelection()).getFirstElement();
    return (RuleWrapper) selectedObject;
  }

  public String getSelectedRule() {
    RuleWrapper selectedWrapper = getSelectedWrapper();
    if (selectedWrapper != null) {
      return selectedWrapper.getRule().getSelector().getValue();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: rule viewer
  //
  ////////////////////////////////////////////////////////////////////////////
  protected SourceViewer m_ruleViewer;
  protected RulePreviewControl m_rulePreviewControl;
  protected RuleWrapper m_currentWrapper;
  protected boolean m_currentRuleChanged;

  /**
   * Create rule editor.
   */
  protected void createRuleGroup(Composite parent) {
    Group ruleGroup = new Group(parent, SWT.NONE);
    ruleGroup.setText(Messages.StylesEditComposite_ruleGroup);
    GridLayoutFactory.create(ruleGroup);
    GridDataFactory.create(ruleGroup).spanV(2).grab().fill();
    // editor
    {
      // source viewer
      {
        m_ruleViewer = new SourceViewer(ruleGroup, null, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.create(m_ruleViewer.getTextWidget()).hintC(60, 10).grab().fill();
        m_ruleViewer.configure(new CssConfiguration());
        // hack action handlers
        new StyledTextControlActionsManager(m_ruleViewer.getTextWidget());
        // add modify listener to save rule only if it was changed
        m_ruleViewer.addTextListener(new ITextListener() {
          public void textChanged(TextEvent event) {
            m_currentRuleChanged = true;
            showRulePreview(m_ruleViewer.getTextWidget().getText());
          }
        });
        // create popup menu
        StyledText textWidget = m_ruleViewer.getTextWidget();
        final Menu popupMenu = new Menu(textWidget);
        textWidget.setMenu(popupMenu);
        ISharedImages sharedImages = Activator.getDefault().getWorkbench().getSharedImages();
        {
          MenuItem copyItem = new MenuItem(popupMenu, SWT.NONE);
          copyItem.setText(Messages.StylesEditComposite_copyItem);
          copyItem.setImage(sharedImages.getImage(ISharedImages.IMG_TOOL_COPY));
          copyItem.addSelectionListener(new PopupMenuItemSelectionAdapter(IWorkbenchActionDefinitionIds.COPY));
        }
        {
          MenuItem cutItem = new MenuItem(popupMenu, SWT.NONE);
          cutItem.setText(Messages.StylesEditComposite_cutItem);
          cutItem.setImage(sharedImages.getImage(ISharedImages.IMG_TOOL_CUT));
          cutItem.addSelectionListener(new PopupMenuItemSelectionAdapter(IWorkbenchActionDefinitionIds.CUT));
        }
        {
          MenuItem pasteItem = new MenuItem(popupMenu, SWT.NONE);
          pasteItem.setText(Messages.StylesEditComposite_pasteItem);
          pasteItem.setImage(sharedImages.getImage(ISharedImages.IMG_TOOL_PASTE));
          pasteItem.addSelectionListener(new PopupMenuItemSelectionAdapter(IWorkbenchActionDefinitionIds.PASTE));
        }
        {
          MenuItem selectItem = new MenuItem(popupMenu, SWT.NONE);
          selectItem.setText(Messages.StylesEditComposite_selectAllItem);
          selectItem.addSelectionListener(new PopupMenuItemSelectionAdapter(IWorkbenchActionDefinitionIds.SELECT_ALL));
        }
        {
          MenuItem revertItem = new MenuItem(popupMenu, SWT.NONE);
          revertItem.setText(Messages.StylesEditComposite_revertItem);
          revertItem.setImage(sharedImages.getImage(ISharedImages.IMG_TOOL_UNDO));
          revertItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              showRule(getSelectedWrapper(), false);
            }
          });
        }
      }
      // warning
      {
        Label label = new Label(ruleGroup, SWT.WRAP);
        GridDataFactory.create(label).grabH().hintHC(60).fillH();
        label.setText(Messages.StylesEditComposite_warningEdit);
      }
    }
    // preview
    {
      m_rulePreviewControl = new RulePreviewControl(ruleGroup, SWT.NONE);
      GridDataFactory.create(m_rulePreviewControl).hintVC(15).grab().fill();
    }
  }

  protected void showRule(RuleWrapper wrapper, boolean save) {
    if (save) {
      saveCurrentRule();
    }
    // remember current wrapper and mark it as unchanged
    m_currentWrapper = wrapper;
    m_currentRuleChanged = false;
    // update
    if (wrapper != null) {
      CssRuleNode rule = m_currentWrapper.getRule();
      // show source
      try {
        String ruleSource = getContext().getDocument().get(rule.getOffset(), rule.getLength());
        m_ruleViewer.setDocument(new Document(ruleSource));
        m_ruleViewer.getTextWidget().setEnabled(true);
        // mark rule as unchanged, because setDocument() will fire ITextListener 
        m_currentRuleChanged = false;
      } catch (Throwable ex) {
        processErrorMessage(ex);
      }
    } else {
      m_ruleViewer.setDocument(new Document());
      m_ruleViewer.getTextWidget().setEnabled(false);
    }
  }

  /**
   * If we have currently selected rule and its source was modified, replace current rule with new
   * one, parsed from modified source.
   */
  public void saveCurrentRule() {
    if (m_currentWrapper != null && m_currentRuleChanged) {
      final RuleWrapper currentWrapper = m_currentWrapper;
      final CssRuleNode currentRule = currentWrapper.getRule();
      m_currentWrapper = null;
      try {
        final String ruleText = m_ruleViewer.getTextWidget().getText();
        // apply modifications
        ModificationProcessor processor = new ModificationProcessor() {
          @Override
          protected void execute() throws Exception {
            CssRuleNode newRule = getContext().replaceRule(currentRule, ruleText);
            currentWrapper.setRule(newRule);
          };
        };
        processor.process();
      } catch (Throwable ex) {
        processErrorMessage(ex);
      }
      m_rulesViewer.refresh(currentWrapper);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: Edit actions
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean locateRule(CssEditContext context, String ruleValue) {
    List<CssRuleNode> rules = context.getCssDocument().getRules();
    for (CssRuleNode rule : rules) {
      if (rule.getSelector().getValue().equals(ruleValue)) {
        // select rule
        RuleWrapper wrapper = new RuleWrapper(rule);
        m_rulesViewer.setSelection(new StructuredSelection(wrapper), true);
        return true;
      }
    }
    return false;
  }

  /**
   * Adds new rule with given style name as selector.
   */
  public void addNewRule() {
    String selector =
        editSelector(
            Messages.StylesEditComposite_newRuleTitle,
            Messages.StylesEditComposite_newRuleMessage,
            getDefaultNewSelector());
    if (selector != null) {
      addNewRule(selector);
    }
  }

  public void addNewRule(String selector) {
    final CssRuleNode newRule = CssFactory.newRule(selector);
    try {
      // apply modifications
      ModificationProcessor processor = new ModificationProcessor() {
        @Override
        protected void execute() throws Exception {
          getContext().getCssDocument().addRule(newRule);
        };
      };
      processor.process();
    } catch (Throwable ex) {
      processErrorMessage(ex);
    }
    //
    m_rulesViewer.refresh();
    m_rulesViewer.setSelection(new StructuredSelection(new RuleWrapper(newRule)));
  }

  protected String getDefaultNewSelector() {
    String selectedRule = getSelectedRule();
    if (selectedRule == null) {
      return "";
    } else {
      return selectedRule + "-new";
    }
  }

  /**
   * Change selector name for current rule.
   */
  public void renameSelectedRule() {
    RuleWrapper wrapper = getSelectedWrapper();
    if (wrapper != null) {
      final CssRuleNode rule = wrapper.m_rule;
      final String selector =
          editSelector(
              Messages.StylesEditComposite_changeSelectorTitle,
              Messages.StylesEditComposite_changeSelectorMessage,
              rule.getSelector().getValue());
      if (selector != null) {
        try {
          // apply modifications
          ModificationProcessor processor = new ModificationProcessor() {
            @Override
            protected void execute() {
              rule.getSelector().setValue(selector);
            }
          };
          processor.process();
        } catch (Exception ex) {
          processErrorMessage(ex);
        }
        m_rulesViewer.refresh();
        m_rulesViewer.setSelection(new StructuredSelection(wrapper));
      }
    }
  }

  /**
   * Opens dialog for editing currently selected rule.
   */
  public void editSelectedRule() {
    RuleWrapper wrapper = getSelectedWrapper();
    if (wrapper != null) {
      // save rule to reflect any changes that user made in source editor
      saveCurrentRule();
      final CssRuleNode rule = wrapper.getRule();
      // open dialog to edit rule
      final StyleEditDialog styleEditDialog = getStyleEditDialog(rule);
      if (styleEditDialog.open() != Window.OK) {
        return;
      }
      // update rule to reflect changes
      try {
        ModificationProcessor processor = new ModificationProcessor() {
          @Override
          protected void execute() throws Exception {
            styleEditDialog.updateRule(rule);
          };
        };
        processor.process();
      } catch (Throwable ex) {
        processErrorMessage(ex);
      }
      showRule(wrapper, true);
    }
  }

  protected StyleEditDialog getStyleEditDialog(final CssRuleNode rule) {
    StyleEditDialog styleEditDialog = StyleEditDialog.get(getShell());
    styleEditDialog.setRule(rule);
    return styleEditDialog;
  }

  /**
   * Remove currently selected rule.
   */
  public void removeSelectedRule() {
    RuleWrapper wrapper = getSelectedWrapper();
    if (wrapper != null) {
      final CssRuleNode rule = wrapper.getRule();
      if (MessageDialog.openConfirm(
          getShell(),
          Messages.StylesEditComposite_removeRuleTitle,
          MessageFormat.format(
              Messages.StylesEditComposite_removeRuleMessage,
              rule.getSelector().getValue()))) {
        try {
          // apply modifications
          ModificationProcessor processor = new ModificationProcessor() {
            @Override
            protected void execute() {
              getContext().getCssDocument().removeRule(rule);
            }
          };
          processor.process();
        } catch (Exception ex) {
          processErrorMessage(ex);
        }
        m_rulesViewer.refresh();
      }
    }
  }

  /**
   * Sort rules in file.
   */
  public void sortRules() {
    try {
      // apply modifications
      ModificationProcessor processor = new ModificationProcessor() {
        @Override
        protected void execute() throws Exception {
          getContext().sortRules(new Comparator<CssRuleNode>() {
            public int compare(CssRuleNode rule_1, CssRuleNode rule_2) {
              return rule_1.getSelector().getValue().compareTo(rule_2.getSelector().getValue());
            }
          });
        }
      };
      processor.process();
    } catch (Throwable ex) {
      processErrorMessage(ex);
    }
    m_rulesViewer.refresh();
  }

  /**
   * @return new value of selector, does not allows empty of existing value.
   */
  protected String editSelector(String dialogTitle, String dialogMessage, String initialSelector) {
    InputDialog inputDialog =
        new InputDialog(getShell(),
            dialogTitle,
            dialogMessage,
            initialSelector,
            new IInputValidator() {
              public String isValid(String newText) {
                // check for empty selector
                if (newText.length() == 0) {
                  return Messages.StylesEditComposite_validateEmptySelector;
                }
                // check for unique selector
                CssEditContext context = getContext();
                List<CssRuleNode> rules = context.getCssDocument().getRules();
                for (CssRuleNode rule : rules) {
                  if (rule.getSelector().getValue().equals(newText)) {
                    return MessageFormat.format(
                        Messages.StylesEditComposite_validateSelectorExists,
                        newText);
                  }
                }
                // valid
                return null;
              }
            });
    if (inputDialog.open() == Window.OK) {
      return inputDialog.getValue();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  private Timer m_timer;

  protected void showRulePreview(final String ruleText) {
    if (m_timer != null) {
      m_timer.cancel();
    }
    m_timer = new Timer();
    m_timer.schedule(new TimerTask() {
      @Override
      public void run() {
        m_timer = null;
        try {
          CssParser parser = new CssParser(new StringReader(ruleText));
          CssDocument document = parser.parse();
          final List<CssRuleNode> rules = document.getRules();
          if (rules.size() == 1) {
            Display.getDefault().syncExec(new Runnable() {
              public void run() {
                m_rulePreviewControl.showRule(rules.get(0));
              }
            });
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    }, 300);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link Button} with given title and {@link SelectionListener}.
   */
  protected static void createRulesButton(Composite parent,
      String text,
      SelectionListener selectionListener) {
    Button button = new Button(parent, SWT.NONE);
    GridDataFactory.create(button).hintHC(15);
    button.setText(text);
    button.addSelectionListener(selectionListener);
  }

  /**
   * Handle processor for designer errors.
   */
  protected void processErrorMessage(Throwable e) {
    if (m_exceptionHandler == null) {
      e.printStackTrace();
    } else {
      m_exceptionHandler.handleException(e);
    }
  }

  /**
   * Wrapper for {@link CssRuleNode}. We use it as element for rules viewer because we need to
   * replace {@link CssRuleNode} when users changes its text, but there is not way to replacing item
   * in {@link ListViewer}.
   */
  protected static final class RuleWrapper {
    CssRuleNode m_rule;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public RuleWrapper(CssRuleNode rule) {
      checkObject(rule);
      m_rule = rule;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public CssRuleNode getRule() {
      return m_rule;
    }

    public void setRule(CssRuleNode rule) {
      m_rule = rule;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Object
    //
    ////////////////////////////////////////////////////////////////////////////
    private void checkObject(Object o) {
      if (o == null) {
        throw new IllegalArgumentException("Object can not be null");
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof RuleWrapper) {
        return ((RuleWrapper) obj).m_rule.equals(m_rule);
      }
      if (obj instanceof CssRuleNode) {
        return obj.equals(m_rule);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return m_rule.hashCode();
    }
  }
  /**
   * Default content provider.
   */
  protected abstract static class DefaultStructuredContextProvider
      implements
        IStructuredContentProvider {
    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
  /**
   * {@link ICommandExceptionHandler} allows centralized exceptions handling for all executable
   * {@link Command}'s.
   */
  public interface ICommandExceptionHandler {
    /**
     * Notifies that exception was happened.
     */
    void handleException(Throwable exception);
  }
  /**
   * Processor for inform external utilities about designer modifications in progress.
   */
  protected abstract class ModificationProcessor {
    protected abstract void execute() throws Exception;

    public final void process() throws Exception {
      try {
        m_modificationProcessing = true;
        execute();
      } finally {
        m_modificationProcessing = false;
      }
    }
  }
  /**
   * Listener for processing standard edit actions from popup menu.
   */
  class PopupMenuItemSelectionAdapter extends SelectionAdapter {
    private final String m_commandId;

    PopupMenuItemSelectionAdapter(final String commandId) {
      m_commandId = commandId;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      Event event = new Event();
      event.widget = e.widget;
      event.item = e.item;
      event.x = e.x;
      event.y = e.y;
      event.width = e.width;
      event.height = e.height;
      event.detail = e.detail;
      event.stateMask = e.stateMask;
      event.text = e.text;
      event.doit = e.doit;
      try {
        AbstractControlActionsManager.getHandlerService().executeCommand(getCommandId(), event);
      } catch (Throwable exception) {
        processErrorMessage(exception);
      }
    }

    public String getCommandId() {
      return m_commandId;
    }
  };
}
