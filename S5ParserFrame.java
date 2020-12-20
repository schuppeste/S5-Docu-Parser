/*This file is part of S5Parser.

    S5Parser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    S5Parser is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with S5Parser.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von S5Parser.

    S5Parser ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder jeder neueren
    veröffentlichten Version, weiter verteilen und/oder modifizieren.

    S5Parser wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package S5parser;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class S5ParserFrame extends JFrame implements ActionListener, PropertyChangeListener {

    /**
     *
     * Initialisierungen
     */
    private static File docsfile;
    private static File zuordfile;
    private static int processbar = 0;
    private static int processmax = 0;

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel10;
    public static javax.swing.JLabel jLabel6;
    public static javax.swing.JTextField jTextField1;
    public static javax.swing.JTextField jTextField3;
    public static JButton zuord = new JButton("Datei Zuordnungen");
    public static JButton docs = new JButton("Datei FUP-Doku");
    public static JButton convert = new JButton("Konvertieren");
    public static JButton gen = new JButton("Generieren");
    public static javax.swing.JProgressBar jprogressBar = new JProgressBar();
    final JFileChooser fcdocs = new JFileChooser();
    final JFileChooser fczuord = new JFileChooser();

    private Task task;

    /**
     * Invoked when task's progress property changes.
     */
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();

        // jprogressBar = new JProgressBar(0, 100);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(600, 300));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        jPanel6.setPreferredSize(new java.awt.Dimension(400, 30));
        jPanel6.setLayout(new java.awt.GridLayout());

        jLabel1.setLabelFor(jButton1);
        jLabel1.setText("  Den Dateipfad zur PB Dokumentation angeben.");

        jPanel6.add(jLabel1);

        getContentPane().add(jPanel6);
        //jPanel2.setOpaque(true);
        jPanel2.setPreferredSize(new java.awt.Dimension(400, 30));
        jPanel2.setVerifyInputWhenFocusTarget(false);
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
        flowLayout1.setAlignOnBaseline(true);
        jPanel2.setLayout(flowLayout1);

        jTextField1.setColumns(40);
        jTextField1.setText("...");
        jTextField1.addActionListener(this);
        jPanel2.add(jTextField1);

        jButton1.setLabel("Suche");
        jButton1.setName("docs"); // NOI18N
        jPanel2.add(jButton1);
        jButton1.addActionListener(this);
        getContentPane().add(jPanel2);

        jPanel7.setPreferredSize(new java.awt.Dimension(400, 30));
        jPanel7.setLayout(new java.awt.GridLayout());
        //jPanel7.setOpaque(true);
        jLabel2.setText("  Den Dateipfad zur Zuordnungsliste angeben.");
        jPanel7.add(jLabel2);

        getContentPane().add(jPanel7);

        jPanel4.setPreferredSize(new java.awt.Dimension(400, 30));
        java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT);
        flowLayout2.setAlignOnBaseline(true);
        jPanel4.setLayout(flowLayout2);
        //jPanel4.setOpaque(true);
        jTextField3.setColumns(40);
        jTextField3.setText("...");
        jTextField3.addActionListener(this);
        jPanel4.add(jTextField3);

        jButton3.setLabel("Suche");
        jButton3.setName("docs"); // NOI18N
        jPanel4.add(jButton3);
        jButton3.addActionListener(this);
        getContentPane().add(jPanel4);

        jPanel5.setPreferredSize(new java.awt.Dimension(400, 30));

        jButton6.setText("Generieren");
        jButton6.setName("docs"); // NOI18N
        jButton6.addActionListener(this);
        jPanel5.add(jButton6);

        getContentPane().add(jPanel5);

        jPanel9.setPreferredSize(new java.awt.Dimension(400, 30));
        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Die Hilfs Datenbank wird angelegt oder überschrieben.");
        jLabel6.setAlignmentX(1.0F);
        jPanel9.add(jLabel6);
        getContentPane().add(jPanel9);

        // Wert für den Ladebalken wird gesetzt
        jprogressBar.setValue(0);
        jprogressBar.setStringPainted(true);
        jprogressBar.setMaximum(100);
        jprogressBar.setMinimum(0);
        // JProgressBar wird Panel hinzugefügt

        check = new JLabel();
        jPanel10.setPreferredSize(new java.awt.Dimension(400, 30));
        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.LINE_AXIS));
        jPanel10.add(jprogressBar);
        getContentPane().add(jPanel10);

        pack();

// do something 
// do more
    }// </editor-fold>                        

    /**
     *
     */
    public int progress = 0;
    public static JLabel check;

    public S5ParserFrame() throws IOException {
        this.setTitle("S5-Parser Html");
        this.setSize(600, 300);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height
                / 2 - this.getSize().height / 2);
        JPanel panel = new JPanel();
        zuord.addActionListener(this);
        docs.addActionListener(this);
        convert.addActionListener(this);
        gen.addActionListener(this);
        BufferedImage myImage;

        panel.add(zuord);
        panel.add(docs);
        panel.add(convert);
        panel.add(gen);
        initComponents();

        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    /**
     * Invoked when task's progress property changes.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            jprogressBar.setValue(progress);

        }
    }

    class Task extends SwingWorker<Void, Void> {

        /*
     * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() throws InterruptedException, SQLException, IOException {
            if( zuordfile== null)
                 Fileparser.readFiles(docsfile.getAbsolutePath(), null);
            else
            Fileparser.readFiles(docsfile.getAbsolutePath(), zuordfile.getAbsolutePath());
            new Thread(new Runnable() {
                @Override
                public void run() {
                     System.out.println("Teeeeesta");
                    String[] args = new String[2];
                    args[0] = docsfile.getAbsolutePath();
                    if( zuordfile!=null){
                    args[1] = zuordfile.getAbsolutePath();
                     System.out.println("zuord!!!");
                    }else{
                        args[1]=null;
                    System.out.println("Kein zuord!!");
                    }
                    try {
                        Fileparser parse = new Fileparser(args);
                        parse.run();
                    } catch (SQLException | IOException ex) {
                        Logger.getLogger(S5ParserFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).run();
            // Initialize progress property.
            return null;
        }

        /*
     * Executed in event dispatch thread
         */
        public void done() {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public static void main(final String[] args) throws IOException, SQLException {
        // TODO Auto-generated method stub
        try {
//      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
         try {
                
                   Thread.sleep(3000);
                } catch (InterruptedException e) {
                   System.err.println("Thread unterbrochen");
              }
        
        if (args.length == 0) {

            S5ParserFrame parser = new S5ParserFrame();
            parser.setVisible(true);
        } else {

            //myargs[0] = docsfile.getAbsolutePath();
            //myargs[1] = zuordfile.getAbsolutePath();
            try {
                String[] myargs = args;
                Fileparser parse = new Fileparser(args);
            } catch (SQLException | IOException ex) {
                Logger.getLogger(S5ParserFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == jButton1) {
            fcdocs.setCurrentDirectory(new File("."));
            int returnVal = fcdocs.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                docsfile = fcdocs.getSelectedFile();
                this.jTextField1.setText(docsfile.getAbsolutePath());
            } else {

            }
        } else if (e.getSource() == jButton3) {
            fcdocs.setCurrentDirectory(new File("."));
            int returnVal = fcdocs.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                zuordfile = fcdocs.getSelectedFile();
                this.jTextField3.setText(zuordfile.getAbsolutePath());
            } else {

            }
        } else if (e.getSource() == jButton6) {
            final String[] args = new String[2];

            if (docsfile != null) {
                if (docsfile.exists()) {
                    if (zuordfile != null) {
                        if (zuordfile.exists()) {
                            args[1] = zuordfile.getAbsolutePath();
                        } else {
                            args[1] = null;
                        }
                    }
                    args[0] = docsfile.getAbsolutePath();

                    S5ParserFrame.jprogressBar.setIndeterminate(true);
                        System.out.println("Create Task!!!");
                    task = new Task();
                    task.addPropertyChangeListener(this);
                    task.execute();

                }

            }
        }
    }

}

