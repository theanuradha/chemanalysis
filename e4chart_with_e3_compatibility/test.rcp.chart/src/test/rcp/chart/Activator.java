package test.rcp.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static final String libVersion = "2.8"; // jSSC-2.8.0 Release from
													// 24.01.2014
	private static final String libMinorSuffix = "0"; // since 0.9.0

	// The plug-in ID
	public static final String PLUGIN_ID = "test.rcp.chart"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private int osType;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		String osName = System.getProperty("os.name");
		String architecture = System.getProperty("os.arch");
		String userHome = System.getProperty("user.home");
		String fileSeparator = System.getProperty("file.separator");
		String tmpFolder = System.getProperty("java.io.tmpdir");
		URL resource = context.getBundle().getResource("jSSC-2.8.dll");
		if (resource != null && (osName.startsWith("Win"))) {
			String libFolderPath;
			String libName;
			osName = "windows";

			String libRootFolder = new File(userHome).canWrite() ? userHome : tmpFolder;

			String javaLibPath = System.getProperty("java.library.path");// since
																			// 2.1.0

			if (architecture.equals("i386") || architecture.equals("i686")) {
				architecture = "x86";
			} else if (architecture.equals("amd64") || architecture.equals("universal")) {// os.arch
																							// "universal"
																							// since
																							// 2.6.0
				architecture = "x86_64";
			} else if (architecture.equals("arm")) {// since 2.1.0
				String floatStr = "sf";
				if (javaLibPath.toLowerCase().contains("gnueabihf") || javaLibPath.toLowerCase().contains("armhf")) {
					floatStr = "hf";
				} else {
					try {
						Process readelfProcess = Runtime.getRuntime().exec("readelf -A /proc/self/exe");
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(readelfProcess.getInputStream()));
						String buffer = "";
						while ((buffer = reader.readLine()) != null && !buffer.isEmpty()) {
							if (buffer.toLowerCase().contains("Tag_ABI_VFP_args".toLowerCase())) {
								floatStr = "hf";
								break;
							}
						}
						reader.close();
					} catch (Exception ex) {
						// Do nothing
					}
				}
				architecture = "arm" + floatStr;
			}

			libFolderPath = libRootFolder + fileSeparator + ".jssc" + fileSeparator + osName;
			libName = "jSSC-" + libVersion + "_" + architecture;
			  libName = System.mapLibraryName(libName);
			File dll = new File(libFolderPath, libName);
			if (!dll.exists()) {
				dll.getParentFile().mkdirs();
				try {

					InputStream inputStream = resource.openConnection().getInputStream();
					BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
					FileOutputStream output = new FileOutputStream(dll);
					byte[] buffer = new byte[1024]; // Adjust if you want
					int bytesRead;
					while ((bytesRead = inputStream.read(buffer)) != -1) {

						output.write(buffer, 0, bytesRead);
					}

					in.close();
					output.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
