package prototype.parts;

import java.io.FileWriter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.opencsv.CSVWriter;

import prototype.serial.Serial;

public class AbsorbanceChart {

	private static final int CARRIAGE_RETURN_CODE = 13;

	private Trace trace;
	private XYGraph xyGraph;
	private Serial serialPort;

	@Inject
	private Shell activeShell;

	public AbsorbanceChart() {
		String portName = Serial.list()[0];
		serialPort = new Serial(portName, 57600);
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Button getDataBtn = new Button(parent, SWT.PUSH);
		getDataBtn.setText("Get Data");
		getDataBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleGetData();
			}
		});

		Button btnExportCsv = new Button(parent, SWT.NONE);
		btnExportCsv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleCsvExport();
			}
		});
		btnExportCsv.setText("Export CSV");
		Canvas canvas = new Canvas(parent, SWT.NONE);
		GridData gd_canvas = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_canvas.horizontalSpan = 2;
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

	}

	protected void handleCsvExport() {

		// no export if no data
		if (trace == null) {
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

			IDataProvider dataProvider = trace.getDataProvider();
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

	protected void handleGetData() {

		if (trace != null) {
			xyGraph.removeTrace(trace);
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

		trace = new Trace("Absorbance", xyGraph.primaryXAxis, xyGraph.primaryYAxis, traceDataProvider);

		// add the trace to xyGraph
		xyGraph.addTrace(trace);
		xyGraph.performAutoScale();

	}

	// Get one pixel value from serial port
	public int getPixel() {

		String inStr1 = "0"; // Holds the serial input string

		while (serialPort.available() < 0) // Wait until there is something in
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
			inStr1 = serialPort.readStringUntil(CARRIAGE_RETURN_CODE);
		} while (inStr1 == null);

		return (Integer.parseInt(inStr1.trim()));

	}

	@Focus
	public void setFocus() {
	}

	@PreDestroy
	public void dispose() {
		serialPort.dispose();
	}
}