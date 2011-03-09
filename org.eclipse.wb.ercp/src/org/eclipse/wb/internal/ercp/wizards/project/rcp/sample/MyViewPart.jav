package ${packageName}.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class MyViewPart extends ViewPart {
	public static final String ID = "${packageName}.views.MyViewPart"; //$NON-NLS-1$
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MyViewPart() {
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// ViewPart
	//
	////////////////////////////////////////////////////////////////////////////
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		{
			Label label = new Label(container, SWT.NONE);
			label.setText("My eRCP ViewPart...");
		}
	}
	public void setFocus() {
		// set the focus
	}
}
