package test.rcp.chart.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityView;

import com.opencsv.CSVReader;

import test.rcp.chart.parts.AbsorbanceChart;
import test.rcp.chart.views.AbsorbanceChartView;

public class CSVLauncher implements IEditorLauncher {

	@Override
	public void open(IPath path) {
		File file = path.toFile();
		InputStream contents = null;
		try {
			final EPartService partService = (EPartService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(EPartService.class);

			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file)));

			List<String[]> readAll = reader.readAll();
			reader.close();

			double[] x = new double[readAll.size()];
			double[] y = new double[readAll.size()];
			int index = 0;
			for (String[] d : readAll) {

				if (d.length == 2)// check line is valid
				{
					try {
						double xV = Double.parseDouble(d[0]);
						double yV = Double.parseDouble(d[1]);
						x[index] = xV;
						y[index] = yV;

						index++;
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}

			MPart mPart = partService.findPart("test.rcp.part.chart");
			if (mPart !=null) {
				AbsorbanceChart object = (AbsorbanceChart) mPart.getObject();
				object.loadFromCSV(file.getName(), x, y);
			}
			else{
				
				MPart compatibilityPart = partService.findPart(AbsorbanceChartView.ID);
				if(compatibilityPart!=null)
				{
					partService.showPart(AbsorbanceChartView.ID, PartState.ACTIVATE);
					CompatibilityView view = (CompatibilityView) compatibilityPart.getObject();
					AbsorbanceChartView chartView = (AbsorbanceChartView) view.getView();
					chartView.loadFromCSV(file.getName(), x, y);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (contents != null)
				try {
					contents.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

		}
	}

}
