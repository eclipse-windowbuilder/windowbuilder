package ${packageName}.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage_2 extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String ID = "${packageName}.preferences.PreferencePage_2"; //$NON-NLS-1$
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PreferencePage_2() {
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Control
	//
	////////////////////////////////////////////////////////////////////////////
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		{
			Label label = new Label(container, SWT.NONE);
			label.setText("My eRCP PreferencePage 2...");
		}
		return container;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IWorkbenchPreferencePage
	//
	////////////////////////////////////////////////////////////////////////////
	public void init(IWorkbench workbench) {
		// initialize the preference page
	}
}
