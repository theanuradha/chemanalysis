package test.rcp.chart.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;

import test.rcp.chart.parts.AbsorbanceChart;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class AbsorbanceChartView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "test.rcp.chart.views.AbsorbanceChartView";

	private final AbsorbanceChart chartPart;

	public AbsorbanceChartView() {
		chartPart = new AbsorbanceChart();
	}

	public void createPartControl(Composite parent) {
		chartPart.createComposite(parent);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		chartPart.setFocus();
	}
	
	@Override
	public void dispose() {
		chartPart.dispose();
	}

	public void loadFromCSV(String name, double[] x, double[] y) {
		chartPart.loadFromCSV(name, x, y);
		
	}
}
