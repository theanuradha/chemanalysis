package prototype.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.opencsv.CSVReader;

import prototype.parts.AbsorbanceChart;

public class DumyCSVOpenEditor extends EditorPart {

	public DumyCSVOpenEditor() {
		// use this to mock and load CSV in Chart View. Eclipse not allow to
		// associate files with view only editors. we have to workaround with
		// dummy editor load
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	

	@Override
	public void init(final IEditorSite site, IEditorInput input) throws PartInitException {

		setSite(site);
		setInput(input);
		
	
		final EPartService partService = (EPartService)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(EPartService.class);
		final MPart findPart = partService.findPart("org.eclipse.e4.ui.compatibility.editor");
		findPart.setVisible(false);

		if (input instanceof FileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) input;
			IFile file = fileEditorInput.getFile();
			InputStream contents = null;
			try {
				contents = file.getContents();
				CSVReader reader = new CSVReader(new InputStreamReader(contents));
				
				List<String[]> readAll = reader.readAll();
				reader.close();
				
				double[] x = new double[readAll.size()];
				double[] y = new double[readAll.size()];
				int index = 0;
				for (String[] d : readAll) {
					
					if(d.length==2)//check line is valid
					{
						try
						{
							double xV = Double.parseDouble(d[0]);
							double yV = Double.parseDouble(d[1]);
							x[index] = xV;
							y[index] = yV;
							
							index++;
						}
						catch(NumberFormatException e)
						{
							e.printStackTrace();
						}
					}
				}
				
				
				
				MPart mPart = partService.findPart("test.rcp.part.chart");
				if(mPart !=null)
				{
					AbsorbanceChart object = (AbsorbanceChart) mPart.getObject();
					object.loadFromCSV(file.getName(),x,y);
				}

			} catch (CoreException e) {
				e.printStackTrace();
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

		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
					
					findPart.setVisible(false);
					partService.hidePart(findPart, true);
				
			}
		});
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		new Label(parent, SWT.NONE).setText("CHART");

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
