
# Eclipse plugin for ChemAnalysis

## Installing from https://github.com/theanuradha/chemanalysis-updatesite/raw/master

1.  Start with a clean install of Eclipse Mars.
    
    <http://www.eclipse.org/downloads/>
    
2.  Use `Help > Install New Software ... > paste url [https://github.com/theanuradha/chemanalysis-updatesite/raw/master] and hit Enter key

    ##note: For Dawn IDE Use following update site [https://github.com/theanuradha/chemanalysis-updatesite/raw/dawn]
    
3.  Select "ChemAnalysis Tools" install the 
    plugin.
    
4.  Restart Eclipse.
    
5.  Go to perspective and select "ChemAnalysis Workbench" 


## Buiulding From Source: Pre-Requirements: 

1. Eclipse for RCP and RAP Developers 4.5- https://eclipse.org/downloads/packages/release/Mars/2 
2. Install Nebula Visualization Widgets - Use `Help > Install New Software ... > paste url [http://download.eclipse.org/nebula/releases/1.0.0] and hit Enter key.
 Select "Nebula Visualization Widgets" install the plugin.
 
3. Import all projects under '*e4chart_with_e3_compatibility*' folder. 

4. Right click on Project  "test.rcp.chart" ->"Run as Eclipse application" it will run in Eclipse IDE mode with 'ChemAnalysis Workbench' perspective . 

5. Open file 'test.rcp.product' under project 'test.rcp' and use Run action to E4 Application mode.


