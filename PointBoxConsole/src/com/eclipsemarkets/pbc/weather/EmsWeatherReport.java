/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.weather;

import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 *  GUI of Ems Weather Report
 */
class EmsWeatherReport extends JFrame  implements IUpdateWeatherReport, IEmsWeatherReport, ActionListener
{
    private static Logger logger = Logger.getLogger(EmsWeatherReport.class.getName());

    private static final long  DELAY_0_MINS = 0L;
    private static final long  REPEAT_EVERY_15_MINS = 30L * 60L * 1000L;
    private static final long  REPEAT_EVERY_30_MINS = 30L * 60L * 1000L;
    private static final long  REPEAT_EVERY_45_MINS = 45L * 60L * 1000L;
    private static final long  REPEAT_EVER_1_HOUR  = 60L * 60L * 1000L;
    private static final int   DEFAULT_DAY_COUNT = 6;
    private static final int   DEFAULT_WEATHER_REPORT_SOURCE_COUNT = 3;
    private static final String OVERALL_SUMMARY = "Overall Summary";


    private static final Dimension PREFERRED_TABLE_DIM = new Dimension ( 760, 580);
    private static final Dimension PREFERRED_FRAME_DIM = new Dimension ( 800, 600);
    private static final Dimension INTER_CELL_SPACING_DIM = new Dimension ( 0, 0);



    private boolean standAlone = false;
    private List<String> cityList;
    private StatusBar statusBar;
    private JPanel dropDownPanel;
    private JComboBox cmbBoxCities;
    private JTabbedPane baseTab;
    private Timer timer = null;
    private Object timerLock = new Object();
    private JMenuBar  menubar;
    private JMenu     menu;
    private JMenuItem miStartTimer;
    private JMenuItem miStopTimer;
    private JMenuItem miClose;
    private final JFrame    thisFrame;
    private JLabel    lblLastUpdatedAt;
    final private String listOfLatAndLon;


  EmsWeatherReport(Image frameIconImage)
  {
     // by default, not stand alone
     this (frameIconImage, false);
  }

  EmsWeatherReport(Image frameIconImage, boolean standAlone)
  {
    super("EMS Weather Report");
    this.standAlone = standAlone;
    this.thisFrame = this;
    if (frameIconImage != null )
    {
       this.setIconImage(frameIconImage);
    }
    
    cityList = loadCitiesToReportWeatherForecast();

    statusBar = new StatusBar();
    baseTab = createBaseTab();
    dropDownPanel = createDropDownPanel();

    setLayout(new BorderLayout());
    add(dropDownPanel, BorderLayout.NORTH);
    add(baseTab, BorderLayout.CENTER);
    add(statusBar, BorderLayout.SOUTH);
    listOfLatAndLon = CityGeoInfo.getListOfLatAndLonForWebService();
    
    createMenu();
    addWindowListener(
            new WindowListener()
            {
               public void windowOpened(WindowEvent e){}
               public void windowClosing(WindowEvent e)
               {
                  closeWeatherReport();
               }
               public void windowClosed(WindowEvent e){}
               public void windowIconified(WindowEvent e){}
               public void windowDeiconified(WindowEvent e){}
               public void windowActivated(WindowEvent e){}
               public void windowDeactivated(WindowEvent e){}
          }
     );
    setSize(PREFERRED_FRAME_DIM);
  }

  private void createMenu ()
  {
      //Create the menu bar.
     menubar = new JMenuBar();

     //Build the first menu.
     menu = new JMenu("Timer");
     menubar.add(menu);

     //a group of JMenuItems
     miStartTimer = new JMenuItem("Start timer");
     miStartTimer.addActionListener(this);
     menu.add(miStartTimer);

     miStopTimer = new JMenuItem("Stop timer");
     miStopTimer.addActionListener(this);
     menu.add(miStopTimer);

     menu.addSeparator();
     miClose = new JMenuItem("Close");
     miClose.addActionListener(this);
     menu.add(miClose);
     this.setJMenuBar(menubar);
  }

  public void actionPerformed (ActionEvent ae)
  {
     Object src = ae.getSource();
     if ( src == miStartTimer)
     {
        this.startTimer();
     }
     else
     if ( src == miStopTimer)
     {
        this.stopTimer();
     }
     else
     if ( src == miClose)
     {
        this.closeWeatherReport();
     }

  }


  private List<String> loadCitiesToReportWeatherForecast()
  {
    Set<String> citySet = CityGeoInfo.getCitySet();
    List<String> cityList = new ArrayList<String> ();    
    cityList.addAll(citySet);
    return cityList;

  }


  private JPanel createDropDownPanel()
  {
     JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,0));
     JLabel selectLabel = new JLabel();
     selectLabel.setText("  Select City:  ");

     cmbBoxCities = new JComboBox();
     cmbBoxCities.addItem (OVERALL_SUMMARY);
     for(int i = 0; i < cityList.size(); i++)
     {
        cmbBoxCities.addItem(cityList.get(i));
     }

     panel.add(selectLabel);
     panel.add(cmbBoxCities);

     JLabel lblLastEditedAtTitle = new JLabel("   Last updated at :  ");
     panel.add(lblLastEditedAtTitle);
     this.lblLastUpdatedAt = new JLabel("                                  ");
     panel.add(lblLastUpdatedAt);





     cmbBoxCities.addActionListener(
          new ActionListener()
          {
             public void actionPerformed(ActionEvent e)
             {
                baseTab.setSelectedIndex(cmbBoxCities.getSelectedIndex());
             }
          }
        );
        return panel;
    }


  private JTabbedPane createBaseTab ()
  {
    JTabbedPane tabbedPane = new JTabbedPane();

    JComponent panel = makeWeatherPanel(OVERALL_SUMMARY);
    tabbedPane.addTab(OVERALL_SUMMARY, null, panel, "");

    for ( String city : cityList)
    {
       panel = makeWeatherPanel(city);
       tabbedPane.addTab(city, null, panel, "");
    }
    return tabbedPane;

  }


  JComponent makeWeatherPanel(String target)
  {
    JScrollPane panel = new JScrollPane();
    JTable table = new JTable();
    table.setPreferredSize(PREFERRED_TABLE_DIM );
    panel.setViewportView(table);
    table.setShowGrid(false);
    //table.setAutoCreateColumnsFromModel(false);
    //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setIntercellSpacing(INTER_CELL_SPACING_DIM);

    if ( target.equalsIgnoreCase(OVERALL_SUMMARY))
    {
       table.setDefaultRenderer(String.class,  new OverallSummeryTableCellRenderer() );
       table.setModel(OverallSummaryTableModel.getEmptyOverallSummaryCityTableModel(cityList, DEFAULT_DAY_COUNT));
    }
    else
    {
       table.setDefaultRenderer(String.class,  new WeatherReportTableCellRenderer() );
       table.setModel(CityWeatherTableModel.getEmptyCityTableModel(target, DEFAULT_WEATHER_REPORT_SOURCE_COUNT, DEFAULT_DAY_COUNT));
    }
    return panel;
  }


  public void udpateCityWeatherReport ( String target, final CityWeatherTableModel tableModel)
  {
     int index = this.cityList.indexOf(target);
     if ( index != -1)
     {
        int indexToUse = index + 1;
        if (baseTab.getTabCount() <= indexToUse){
            return;
        }
        Component comp = baseTab.getComponentAt(indexToUse);
        if ( comp instanceof JScrollPane)
        {
             JScrollPane spane = (JScrollPane)comp;
             Component viewComp = spane.getViewport().getView();
             if ( viewComp instanceof JTable)
             {
                final JTable table = (JTable)viewComp;
                SwingUtilities.invokeLater
                    (
                        new Runnable ()
                        {
                            public void run ()
                           {
                              table.setModel(tableModel);
                           }
                        }
                    );
             }
        }
     }
  }

   public void updateOverallSummaryReport (final OverallSummaryTableModel model)
   {
      Component comp = baseTab.getComponentAt(0);
      if ( comp instanceof JScrollPane)
      {
         JScrollPane spane = (JScrollPane)comp;
         Component viewComp = spane.getViewport().getView();
         if ( viewComp instanceof JTable)
         {
            final JTable table = (JTable)viewComp;
            SwingUtilities.invokeLater(
                  new Runnable ()
                  {
                     public void run ()
                     {
                       table.setModel(model);
                     }
                  }
               );
         }
      }
   }



  public  void reportWebServiceException (final Exception e)
  {
      SwingUtilities.invokeLater(
              new Runnable ()
              {
                 public void run ()
                 {
                    JOptionPane.showMessageDialog(
                                        thisFrame
                                      , "Exception thrown : " + e.toString()
                                      ,  "Weather web service Error"
                                      , JOptionPane.ERROR_MESSAGE
                                     );
                 }
              }
        );
  }

  private void closeWeatherReport ()
  {
     this.stopTimer();
     this.setVisible(false);
  }


  private void updateTimerStatus (final boolean isOn)
  {
      SwingUtilities.invokeLater(
              new Runnable ()
              {
                  public void run ()
                  {
                     if ( isOn)
                     {
                        statusBar.toggleTimerStatusOn();
                     }
                     else
                     {
                        statusBar.toggleTimerStatusOff();
                     }
                  }
              }
        );
  }



  public void startTimer()
  {
     synchronized ( timerLock)
     {     
        if ( isTimerOn())
        {
           return;
        }
        timer = new Timer("WeatherReportUpdateTimer", true);

        WeatherReportUpdateTask reportUpdateTask = new WeatherReportUpdateTask(this,listOfLatAndLon);
        timer.schedule(reportUpdateTask, DELAY_0_MINS, REPEAT_EVER_1_HOUR );
        logger.log(Level.INFO, "weather service timer started, REPEAT_EVER_1_HOUR");
        updateTimerStatus (true);
     }
  }



  void stopTimer ()
  {
     synchronized ( timerLock)
     {
        if ( isTimerOn() == false)
        {
           return;
        }

        timer.cancel();
        timer = null;
        logger.log(Level.INFO, "weather service timer stopped");
        updateTimerStatus (false);
     }
  }



  boolean isTimerOn ()
  {
     synchronized ( timerLock)
     {
        return timer != null;
     }
  }


  public void setLastUpdatedAt (final String timeStamp)
  {
     SwingUtilities.invokeLater(
         new Runnable()
         {
            public void run ()
            {
               lblLastUpdatedAt.setText(timeStamp);
            }
         }

     );
  }


  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void createAndShowGUI(boolean standAlone) {

    final EmsWeatherReport frame = new EmsWeatherReport(null, standAlone);
    //frame.pack();
    frame.setVisible(true);
    //SwingUtilities.invokeLater(new Runnable(){ public void run(){frame.startTimer(); }});
  }




  public static void main(String[] args)
  {
    final boolean standAlone = true;
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        createAndShowGUI(standAlone);
      }
    });
  }
}
