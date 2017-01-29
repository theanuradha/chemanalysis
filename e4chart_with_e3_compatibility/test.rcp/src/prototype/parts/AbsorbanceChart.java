package prototype.parts;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.ISample;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.opencsv.CSVWriter;

import prototype.serial.Serial;

public class AbsorbanceChart {

	private static final int CARRIAGE_RETURN_CODE = 13;

	private final AtomicBoolean onSerialMode = new AtomicBoolean(false);
	private Trace serialTrace;
	private Map<String, Trace> demoTrace = new HashMap<String, Trace>(5);
	private XYGraph xyGraph;
	private Serial serialPort;

	@Inject
	private Shell activeShell;

	@Inject
	EPartService partService;
	@Inject
	MApplication application;
	@Inject
	EModelService modelService;

	public AbsorbanceChart() {
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(5, false));

		new Label(parent, SWT.NONE).setText("Port");

		final  ComboViewer ports = new ComboViewer(parent);
		ports.setContentProvider(ArrayContentProvider.getInstance());
		String[] serialPorts = Serial.list();
		ports.setInput(serialPorts);

		// enable if need to auto load first port automatically
		// if (serialPorts.length > 0) {
		// try {
		// String portName = serialPorts[0];
		// serialPort = new Serial(portName, 57600);
		// ports.setSelection(new StructuredSelection(portName));
		// } catch (Throwable e) {
		// e.printStackTrace();
		// }
		// }
		ports.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection.getFirstElement() != null) {
					onSerialMode.set(false);
					// clean if already have a selected one
					if (serialPort != null)
						serialPort.dispose();

					if (serialTrace != null) {
						xyGraph.removeTrace(serialTrace);
					}
					for (Trace t : demoTrace.values()) {
						xyGraph.removeTrace(t);
					}
					demoTrace.clear();

					String portName = (String) selection.getFirstElement();
					serialPort = new Serial(portName, 57600);
					ports.setSelection(new StructuredSelection(portName));
					onSerialMode.set(true);
				}

			}
		});

		final Button getDataBtn = new Button(parent, SWT.PUSH);
		getDataBtn.setText("Get Data");
		getDataBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleGetData();
			}
		});

		final Button btnExportCsv = new Button(parent, SWT.NONE);
		
		
		final String MAIN_PERSPECTIVE_STACK_ID = "MainPerspectiveStack";

		MPerspectiveStack perspectiveStack = (MPerspectiveStack) modelService.find(MAIN_PERSPECTIVE_STACK_ID,
				application);

		// // Only do this when no other children, or the restored workspace
		// state
		// // will be overwritten.
		// if (perspectiveStack.getChildren().isEmpty()) {
		//
		// // clone each snippet that is a perspective and add the cloned
		// // perspective into the main PerspectiveStack
		// boolean isFirst = true;
		// for (MUIElement snippet : application.getSnippets()) {
		// if (snippet instanceof MPerspective) {
		// MPerspective perspectiveClone = (MPerspective)
		// modelService.cloneSnippet(application,
		// snippet.getElementId(), null);
		// perspectiveStack.getChildren().add(perspectiveClone);
		// if (isFirst) {
		// perspectiveStack.setSelectedElement(perspectiveClone);
		// isFirst = false;
		// }
		// }
		// }
		//
		// }
		MPerspective demoP = null;
		MPerspective chartP = null;
		for (MPerspective perspective : perspectiveStack.getChildren()) {
			if (perspective.getElementId().startsWith("test.rcp.perspective.demo")) {
				demoP = perspective;
				continue;
			} else if (perspective.getElementId().startsWith("test.rcp.perspective")) {
				chartP = perspective;
				continue;
			}

		}

		final MPerspective demo = demoP;
		final MPerspective chart = chartP;
		final Button btnDemoMode = new Button(parent, SWT.TOGGLE);
		btnDemoMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ports.getCombo().setEnabled(!btnDemoMode.getSelection());
				getDataBtn.setEnabled(!btnDemoMode.getSelection());
				btnExportCsv.setEnabled(!btnDemoMode.getSelection());

				if (btnDemoMode.getSelection()) {

					if (demo != null)
						partService.switchPerspective(demo);
					
					partService.showPart("org.eclipse.ui.navigator.ProjectExplorer", PartState.VISIBLE);
					
					
					onSerialMode.set(false);
					if (serialTrace != null) {
						xyGraph.removeTrace(serialTrace);
						serialTrace = null;
					}
					

				} else {
					if (chart != null)
						partService.switchPerspective(chart);
					
					for (Trace t : demoTrace.values()) {
						xyGraph.removeTrace(t);
					}
					demoTrace.clear();
					

				}
			}
		});
		btnDemoMode.setText("Demo Mode [CSV]");

		
		btnExportCsv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleCsvExport();
			}
		});
		btnExportCsv.setText("Export CSV");
		Canvas canvas = new Canvas(parent, SWT.NONE);
		GridData gd_canvas = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_canvas.horizontalSpan = 5;
		canvas.setLayoutData(gd_canvas);
		final LightweightSystem lws = new LightweightSystem(canvas);

		ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph();
		lws.setContents(toolbarArmedXYGraph);

		xyGraph = toolbarArmedXYGraph.getXYGraph();
		xyGraph.setTitle("Absorbance VS Relative Wave Length");
		xyGraph.primaryXAxis.setTitle("Relative Wave Length");
		xyGraph.primaryYAxis.setTitle("Absorbance");
		xyGraph.primaryYAxis.setShowMajorGrid(true);
		xyGraph.setShowLegend(false);
		partService.switchPerspective(chart);

	}

	protected void handleCsvExport() {

		// no export if no data
		if (serialTrace == null) {
			MessageDialog.openInformation(activeShell, "No data", "No data to export");
			return;
		}

		FileDialog dialog = new FileDialog(activeShell, SWT.SAVE);
		dialog.setFilterNames(new String[] { "CSV files" });
		dialog.setFilterExtensions(new String[] { "*.csv" }); // Windows
		String filename = dialog.open();
		if (filename != null) {
			// append csv if needed
			if (!filename.toLowerCase().endsWith(".csv")) {
				filename = filename + ".csv";
			}

			IDataProvider dataProvider = serialTrace.getDataProvider();
			try {
				CSVWriter writer = new CSVWriter(new FileWriter(filename), ',');
				for (int i = 0; i < dataProvider.getSize(); i++) {
					ISample sample = dataProvider.getSample(i);
					String[] entries = new String[] { sample.getXValue() + "", sample.getYValue() + "" };
					writer.writeNext(entries, false);
				}
				writer.close();
			} catch (Exception e) {
				// TODO: use legitimate E4 logger
				e.printStackTrace();
			}
		}
	}

	public void loadFromCSV(String name, double[] x, double[] y) {
		onSerialMode.set(false);
		if (serialTrace != null) {
			xyGraph.removeTrace(serialTrace);
			serialTrace = null;
		}

		if (demoTrace.containsKey(name) && demoTrace.get(name) != null) {

			xyGraph.removeTrace(demoTrace.get(name));
		}

		CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
		traceDataProvider.setBufferSize(x.length);
		traceDataProvider.setCurrentXDataArray(x);
		traceDataProvider.setCurrentYDataArray(y);

		Trace trace = new Trace(name, xyGraph.primaryXAxis, xyGraph.primaryYAxis, traceDataProvider);

		demoTrace.put(name, trace);

		// add the trace to xyGraph
		xyGraph.addTrace(trace);
		xyGraph.performAutoScale();
	}

	protected void handleGetData() {

		for (Trace trace : demoTrace.values()) {
			xyGraph.removeTrace(trace);

		}
		demoTrace.clear();

		if (serialTrace != null) {
			xyGraph.removeTrace(serialTrace);
		}

		if (serialPort == null) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Please select a valid Port.");
			return;
		}

		serialPort.clear(); // Clear any pending serial data

		serialPort.write("50"); // Write out integration time
		serialPort.write(CARRIAGE_RETURN_CODE); // Start transfer

		double[] x = new double[3700];
		double[] y = new double[3700];
		// Loop for 3700 pixels from CCD
		for (int i = 0; i < 3700; i++) {
			x[i] = i;
			y[i] = (getPixel() - 10000) / 10000.0d; // Get and normalize pixel
		}

		CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
		traceDataProvider.setBufferSize(3700);
		traceDataProvider.setCurrentXDataArray(x);
		traceDataProvider.setCurrentYDataArray(y);

		serialTrace = new Trace("Absorbance", xyGraph.primaryXAxis, xyGraph.primaryYAxis, traceDataProvider);

		// add the trace to xyGraph
		xyGraph.addTrace(serialTrace);
		xyGraph.performAutoScale();

	}

	// Get one pixel value from serial port
	public int getPixel() {

		String inStr1 = "0"; // Holds the serial input string

		while (serialPort != null && serialPort.available() < 0) // Wait until
																	// there is
																	// something
																	// in
		// buffer
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		do {
			if (serialPort != null)
				inStr1 = serialPort.readStringUntil(CARRIAGE_RETURN_CODE);
		} while (inStr1 == null);

		return (Integer.parseInt(inStr1.trim()));

	}

	@Focus
	public void setFocus() {
	}

	@PreDestroy
	public void dispose() {
		if (serialPort != null)
			serialPort.dispose();
	}
}