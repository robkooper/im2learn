package edu.illinois.ncsa.isda.im2learn.ext.hyperspectral;
/*
 * RankBandDialog.java
 *
 */



/**
 *
 * @author Peter Bajcsy
 * @version 1.0
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.ext.misc.PlotComponent;



public class RankBandDialog extends Im2LearnFrame implements Im2LearnMenu {

	//private ImageFrame preview = null;
	private ImagePanel imagepanel = null;

	private JLabel _labelRankType = new JLabel("RankOrderType");
	private JComboBox _choiceRankType = new JComboBox();

	private JButton _cboxOrder = new JButton("Order");
	private JButton _cboxOrderAll = new JButton("OrderAll");
	private JButton _cboxShowOrder = new JButton("ShowOrder");
	private JButton _cboxSave = new JButton("Save");


//	private Button _cboxStats = new Button("Stats");
//	private Button _cboxShowStats = new Button("ShowStats");

	private JButton _cboxDone = new JButton("Done");

	private JLabel _labelResults = new JLabel("Results");

	// TextARea(num of of rows, num of col)
	private JTextArea _fieldResults = new JTextArea(15,40);
	private String _fieldString = new String();

	private String _title = "RankBand";
	private String _message = "RankBand Computation";
	private boolean _isModal = false;//true;
	private RankBand [] _myRankBands;
	private static final int _numMethods = 6;// number of currently supported methods
	private boolean _flagAllMethods = false;

	//private Dialog myd = null;
	//private PlotDisplay myPlot=null;
	private PlotComponent myPlot=null;
	JFrame frm = new JFrame();

	private int _band = 0;
	private int _bestBandNum = 3;
	private int _rankOrderType = 1;
	private String nameChoice1 = "1 Band (Entropy)";
	private String nameChoice2 = "1 Band (Contrast)";
	private String nameChoice3 = "2 Bands (1st deriv=0)";
	private String nameChoice4 = "3 Bands (2nd deriv=0)";
	private String nameChoice5 = "2 Bands (Ratio)";
	private String nameChoice6 = "2 Bands (Correlation)";

	private String _rankOrderTypeString = nameChoice1;

	private ImageObject _imgObject;
	private static Log logger = LogFactory.getLog(RankBandDialog.class);


	public RankBandDialog(){
		super("Rank Bands of Image");
		setResizable(true);
		createGUI();

	}

	// populate the choice menu
	public void showing(){
		
		_imgObject = imagepanel.getImageObject();
		if(_imgObject == null){
			System.out.println("Error: no image to process !");
			return;
		}
		System.out.println("Test: original ImageObject");
		_imgObject.toString();
		
		DefaultComboBoxModel model = (DefaultComboBoxModel)_choiceRankType.getModel();
		model.removeAllElements();
		
		model.addElement(nameChoice1);
		model.addElement(nameChoice2);
		if(_imgObject.getNumBands() > 1){
			model.addElement(nameChoice3);
		}
		//_choiceRankType.add("2 Bands (1st deriv=0)");
		if(_imgObject.getNumBands() > 2){
			model.addElement(nameChoice4);
		}

		if(_imgObject.getNumBands() > 1){
			model.addElement(nameChoice5);
			model.addElement(nameChoice6);
		}

		_choiceRankType.setSelectedIndex(0);
	}

	/**
	 * Create the GUI.
	 */
	protected void createGUI() {

		// preview frame
/*		preview = new ImageFrame("Preview RankBand");
		preview.getImagePanel().addMenu(new SelectBandDialog());
		preview.setSize(640, 480);
*/
//		public void RankBandDialog(Frame myFrame, String title, boolean isModal){

		

		_myRankBands = new RankBand[_numMethods];
		for(int i=0;i<_numMethods;i++){
			_myRankBands[i] = new RankBand();
		}
		//System.out.println("Test: MinDataVal="+_histDialog[0].GetMinDataVal());


		//RankBandDialog(myFrame,_title, _isModal);


		//myd = new Dialog(myFrame,title,isModal);
		//myPlot = new PlotComponent();
		//myPlot.frame = null;

		JPanel donePanel = new JPanel();
		JPanel resultsPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JPanel typePanel = new JPanel();
		
		//typePanel.setLayout(new GridLayout(2,1) );
		typePanel.add(_labelRankType);
		typePanel.add(_choiceRankType);
		//_choiceRankType.setSelectedItem(_rankOrderTypeString);

		//resultsPanel.setLayout(new GridLayout(2,1) );
		resultsPanel.add(_labelResults);

		_fieldResults.setEditable(false);
		_fieldResults.setBackground(Color.lightGray);
		resultsPanel.add(_fieldResults);

		buttonPanel.setLayout(new GridLayout(1,4) );
		buttonPanel.add(_cboxOrder);
		buttonPanel.add(_cboxOrderAll);
		buttonPanel.add(_cboxShowOrder);
		buttonPanel.add(_cboxSave);


		donePanel.add(_cboxDone);

		JPanel testPanel = new JPanel();
		testPanel.setLayout(new GridLayout(2,1) );
		testPanel.add(typePanel);
		testPanel.add(buttonPanel);




		/// choice implementation
		_choiceRankType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				_rankOrderType = _choiceRankType.getSelectedIndex();
				if (_rankOrderType == -1) {
					return;
				}
				_rankOrderTypeString = (String)_choiceRankType.getSelectedItem();
				if (_rankOrderTypeString == null) {
					return;
				}
				if(_rankOrderTypeString.equalsIgnoreCase(nameChoice1) ){
					_rankOrderType = 1;
				}
				if(_rankOrderTypeString.equalsIgnoreCase(nameChoice2) ){
					_rankOrderType = 2;
				}
				if(_rankOrderTypeString.equalsIgnoreCase(nameChoice3) ){
					_rankOrderType = 3;
				}
				if(_rankOrderTypeString.equalsIgnoreCase(nameChoice4) ){
					_rankOrderType = 4;
				}
				if(_rankOrderTypeString.equalsIgnoreCase(nameChoice5) ){
					_rankOrderType = 5;
				}
				if(_rankOrderTypeString.equalsIgnoreCase(nameChoice6) ){
					_rankOrderType = 6;
				}

			}
		});

		_cboxOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


				_fieldString = "Computing Order";
				_fieldResults.setText(_fieldString);

				_flagAllMethods = false;

				switch(_rankOrderType){
				case 1:
					_myRankBands[0].RankBasedEntropy(_imgObject);
					break;
				case 2:
					try{
						_myRankBands[0].RankBasedSharpness(_imgObject);
					}catch(Exception exc){
						logger.error("method sharpness failed");
					}
					break;
				case 3:
					_myRankBands[0].RankBased1stDeriv(_imgObject);
					break;
				case 4:
					_myRankBands[0].RankBased2ndDeriv(_imgObject);
					break;
				case 5:
					_myRankBands[0].RankBasedRatio(_imgObject);
					break;
				case 6:
					_myRankBands[0].RankBasedCorrelation(_imgObject);
					break;
				default:
					System.out.println("Error: invalid choice");
				break;
				}
				_fieldString = "Finished Computing Order";
				_fieldResults.setText(_fieldString);

			}
		});
		//_cboxOrder.setEnabled(false);

		_cboxOrderAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//////////////////////////////////////////////////////
				// this method is activated to produce results from all methods
				// and show them next to each other
				//test
				//_imgObject. PrintImageObject();
				if(_imgObject.getNumBands() <= 2){
					_fieldString = "Error: at least 3 bands are required";
					_fieldResults.setText(_fieldString);
					return;
				}

				_fieldString = "Computing Order: All Methods";
				_fieldResults.setText(_fieldString);

				_flagAllMethods = true;
				_myRankBands[0].RankBasedEntropy(_imgObject);
				try{
					_myRankBands[1].RankBasedSharpness(_imgObject);
				}catch(Exception exc){
					logger.error("method sharpness failed");
				}
				_myRankBands[2].RankBased1stDeriv(_imgObject);
				_myRankBands[3].RankBased2ndDeriv(_imgObject);
				_myRankBands[4].RankBasedRatio(_imgObject);
				_myRankBands[5].RankBasedCorrelation(_imgObject);

				_fieldString = "Finished Computing Order";
				_fieldResults.setText(_fieldString);

			}
		});
		//_cboxOrderAll.setEnabled(false);


		_cboxShowOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//System.out.println("Test: ShowOrderBandsData");
				//String s = new String();
				// clear the field
				_fieldResults.setText(" ");
				int [] order = _myRankBands[0].GetOrderBands();
				if(order == null || order.length == 0){
					System.out.println("Error: No Ordered Data");
					return;
				}
				double [] measure = _myRankBands[0].GetScoreBands();
				if(measure == null || measure.length == 0){
					System.out.println("Error: No Ordered Data");
					return;
				}

				int i,j;
				int dispOrderBands;
				/*
        	    if(_bestBandNum <= order.sampPerPixel)
        	    dispOrderBands = _bestBandNum;
        	    else
        	    dispOrderBands = order.sampPerPixel;
				 */
				dispOrderBands = order.length;

				// create the text output
				if(_flagAllMethods){

					_fieldString = "Ordered Bands: Rank, Methods (Band Index, Measure) \n";
					_fieldString += "Methods:"+nameChoice1+","+nameChoice2+","+nameChoice3+","+nameChoice4+","+nameChoice5+","+nameChoice6+"\n";
					// make sure that the bands are ordered based on their score
					for(i=0;i<_numMethods;i++){
						_myRankBands[i].SortScore();
					}

					for(_band = 0; _band < dispOrderBands;_band++){
						_fieldString += (_band+1);
						for(i=0;i<_numMethods;i++){
							order = _myRankBands[i].GetOrderBands();
							if(order == null || order.length == 0){
								System.out.println("Error: No Ordered Data from method ="+i);
								return;
							}
							measure = _myRankBands[i].GetScoreBands();
							if(measure == null || measure.length == 0){
								System.out.println("Error: No Ordered Data from method="+i);
								return;
							}
							_fieldString += ", "+(order[_band]+1)+ ", "+ (float)measure[_band];
						}
						_fieldString +="\n";
					}
					_fieldResults.setText(_fieldString);

				}else{
					_fieldString = "Ordered Bands (Rank, Band Index, Measure) \n";

					for(_band = 0; _band < dispOrderBands;_band++){
						_fieldString += (_band+1) + ", "+ (order[_band]+1) + ", "+ (float)measure[_band]+"\n";
					}
					_fieldResults.setText(_fieldString);
				}

				if(measure.length <= 1){
					System.out.print("INFO: only one band");
					return;
				}


				// now show the results
				if(myPlot != null)
					frm.remove(myPlot);
				myPlot = new PlotComponent();
				if( _flagAllMethods ){
					for(i=0;i<_numMethods;i++){
						order = _myRankBands[i].GetOrderBands();
						measure = _myRankBands[i].GetScoreBands();
						int id = myPlot.addSeries("" + i);
						myPlot.setValue(id, order, measure);
					}
				}else{
					i = 0;
					order = _myRankBands[i].GetOrderBands();
					measure = _myRankBands[i].GetScoreBands();
					int id = myPlot.addSeries("" + i);
					myPlot.setValue(id, order, measure);

				}
				
				frm.add(myPlot);
				frm.pack();
				frm.setVisible(true);
				//myPlot.setVisible(true);

				/*        	    // create the plot output
        	    // init dataObject
        	    ImageObject dataObject = new ImageObject();
        	    dataObject.numcols = measure.sampPerPixel;//_histDialog[0].GetNumBins();
        	    dataObject.numrows = 1;
        	    if( !_flagAllMethods ){
        	      dataObject.sampPerPixel = 2;// data contains the index  and the values
        	    }else{
        	      dataObject.sampPerPixel = _numMethods+1;// data contains index plus all method scores
        	      //_numMethods<<1 ; // data contains the index  and the values
        	    }
        	    dataObject.size = dataObject.numcols * dataObject.numrows * dataObject.sampPerPixel;
        	    dataObject.sampType = "DOUBLE";//_typeDim[n];
        	    dataObject.imageDouble = new double[(int)dataObject.size];

        	    //test
        	    //System.out.println("Test: this is the object created from measuer data and passed to PlotDisplay");
        	    //dataObject.PrintImageObject();
        	    // assign values to dataObject
        	    // assumes that each band has the same number of hist bins
        	    int idx=0;
        	    PlotSettings mySettings = new PlotSettings();

        	    if(_flagAllMethods){

        	      // make sure that the bands are ordered based on their order so that they can be displayed
        	      for(i=0;i<_numMethods;i++){
        	        _myRankBands[i].SortOrder();
        	      }

        	      for(i=0;i< measure.sampPerPixel;i++){
        	        // idx of band
        	        dataObject.imageDouble[idx] = i+1;
        	        idx++;
        	        for(j=0;j<_numMethods;j++){


        	            order = _myRankBands[j].GetOrderBands();
        	            if(order == null || order.imageInt == null){
        	              System.out.println("Error: No Ordered Data from method ="+i);
        	              return;
        	            }

        	            measure = _myRankBands[j].GetScoreBands();
        	            if(measure == null || measure.imageDouble == null){
        	              System.out.println("Error: No Ordered Data from method="+i);
        	              return;
        	            }

        	            //dataObject.imageDouble[idx] = order.imageInt[i]+1;
        	            //idx++;

        	            dataObject.imageDouble[idx] = measure.imageDouble[ i ];
        	            idx++;
        	            if(idx > dataObject.size){
        	                System.out.println("Error: something went wrong in RankBandDialog to ImageObject idx="+idx+" size="+ dataObject.size);
        	            }
        	          }
        	      }// end of j

        	       mySettings.SetTitlePlot("All Measures = f(band)");
        	       mySettings.SetMaxNumComments(7);
        	       mySettings.SetComments("Entropy", 150,450, Color.red);
        	       mySettings.SetComments("Contrast", 165,450, Color.green);
        	       mySettings.SetComments("1st Der.", 180,450, Color.blue);
        	       mySettings.SetComments("2nd Der.", 195,450, Color.yellow);
        	       mySettings.SetComments("Ratio", 210,450, Color.magenta);
        	       mySettings.SetComments("Correl.", 225,450, Color.cyan);

        	    }else{
        	      for(i=0;i< measure.sampPerPixel;i++){
        	        dataObject.imageDouble[idx] = order.imageInt[i]+1;
        	        idx++;
        	        dataObject.imageDouble[idx] = measure.imageDouble[ i ];
        	        idx++;
        	        if(idx > dataObject.size){
        	            System.out.println("Error: something went wrong in RankBandDialog to ImageObject idx="+idx+" size="+ dataObject.size);
        	        }
        	      }

        	      if(measure.sampPerPixel >= 2){
        	        // set up all the variables for the PlotDisplay class
        	        switch(_rankOrderType){
        	          case 1:
        	            mySettings.SetTitlePlot("Entropy Measure = f(band)");
        	            break;
        	          case 2:
        	            mySettings.SetTitlePlot("Contrast Measure = f(band)");
        	           break;
        	          case 3:
        	            mySettings.SetTitlePlot("First Deriv. Measure = f(band)");
        	            break;
        	          case 4:
        	            mySettings.SetTitlePlot("Second Deriv. Measure = f(band)");
        	           break;
        	          case 5:
        	            mySettings.SetTitlePlot("Ratio Measure = f(band)");
        	           break;
        	          case 6:
        	            mySettings.SetTitlePlot("Correlation Measure = f(band)");
        	           break;
        	          default:
        	            mySettings.SetTitlePlot("Band Measure = f(band)");
        	            break;
        	        }
        	      }

        	    }

        	    // show
        	    //mySettings.SetTitlePlot("Band Measure = f(band)");
        	    mySettings.SetTitleAxisVert("Measure");
        	    mySettings.SetTitleAxisHoriz("Band Index");
        	    if(measure.sampPerPixel < 14){
        	      mySettings.SetTickNumHoriz(measure.sampPerPixel);
        	      mySettings.SetCrossSize(3);
        	    }else{
        	      mySettings.SetTickNumHoriz(13);
        	      //mySettings.SetUserHorizAxis(0.0,120.0);

        	      mySettings.SetCrossSize(2);
        	    }


        	    mySettings.SetTickNumVert(5);
        	    mySettings.SetTickHorizDecimal(1);

        	    try{
        	      myPlot.PlotShow("RankBand", dataObject,mySettings);
        	    }
        	    catch(Exception e){
        	      System.out.println("Error: in main of PlotDialog");
        	    }

				 */


			}
		});
		//_cboxShowOrder.setEnabled(false);

		// save the result to disk
		_cboxSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				FileChooser dialog = new FileChooser();
				dialog.setTitle("Save Rank Band Result");
				try {
					String filename = dialog.showSaveDialog();
					FileOutputStream fileOut = new FileOutputStream( filename );
					OutputStreamWriter out = new OutputStreamWriter( fileOut );
					out.write( "INDEX, ORDER (BAND INDEX), SCORE \n" );
					int i;
					int [] order = null;
					double [] measure = null;
					if( _flagAllMethods ){
						for(i=0;i<_numMethods;i++){
							order = _myRankBands[i].GetOrderBands();
							measure = _myRankBands[i].GetScoreBands();      	    
							for( i=0; i<order.length; i ++ ){
								out.write( i + ", "+ order[i] + ", " + measure[i] + "\n" );
							}
						}
					}else{
						i = 0;
						order = _myRankBands[i].GetOrderBands();
						measure = _myRankBands[i].GetScoreBands();      	    
						//myPlot.setValue(i, order, measure);
						for( i=0; i<order.length; i ++ ){
							out.write( i + ", "+ order[i] + ", " + measure[i] + "\n" );
						}
					}
					out.close();
					//ImageLoader.writeImage(filename, imgResult);
				} catch (IOException exc) {
					logger.error("Error saving file", exc);
				}
				setCursor(Cursor.getDefaultCursor());
			}
		});
		//_cboxSave.setEnabled(false);

		// save the result to disk
		_cboxDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				//if(myPlot != null)
				//	myPlot.removeAll();


				setCursor(Cursor.getDefaultCursor());
				RankBandDialog.this.setVisible(false);
			}
		});
		//_cboxDone.setEnabled(false);

		//myd.addWindowListener(new RankBandDialogWindowListener() );
		// combine all the pieces
		this.getContentPane().add(testPanel, BorderLayout.NORTH);
		this.getContentPane().add(resultsPanel, BorderLayout.CENTER);
		this.getContentPane().add(donePanel, BorderLayout.SOUTH);

		pack();
	}


	/**
	 * Hides the Dialog. This will also hide the preview and free all memory.
	 */
	public void closing() {
		_cboxShowOrder.setEnabled(false);
		_cboxOrder.setEnabled(false);
		_cboxOrderAll.setEnabled(false);      
		_cboxSave.setEnabled(false);
		_imgObject = null;
	}

	// ------------------------------------------------------------------------
	// Im2LearnMenu implementation
	// ------------------------------------------------------------------------
	public void setImagePanel(ImagePanel imagepanel) {
		this.imagepanel = imagepanel;
	}

	public JMenuItem[] getPanelMenuItems() {
		return null;
	}

	public JMenuItem[] getMainMenuItems() {
		JMenu tools = new JMenu("Tools");
		JMenu hs = new JMenu("HyperSpectral");
		tools.add(hs);
		JMenuItem rankBand = new JMenuItem(new AbstractAction("Rank Bands of Image") {
			public void actionPerformed(ActionEvent e) {
				setLocationRelativeTo(getOwner());
				setVisible(true);
			}
		});
		hs.add(rankBand);
		return new JMenuItem[]{tools};
	}

	public void imageUpdated(ImageUpdateEvent event) {
		if(event.getId() == ImageUpdateEvent.NEW_IMAGE) {
			if (isVisible()) {
				showing();
			}
		}
	}

	public URL getHelp(String menu) {
		return getClass().getResource("help/RankBandDialog.html");
	}      



}