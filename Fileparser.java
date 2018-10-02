/*This file is part of S5Parser.

    S5Parser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

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

import static S5parser.S5ParserFrame.jLabel6;
import static S5parser.S5ParserFrame.jTextField1;
import static S5parser.S5ParserFrame.jTextField3;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Fileparser extends Thread {

    /**
     *
     * Initialisierungen
     */
    private static final boolean MEM = true;
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:mem:test"; //"jdbc:h2:mem:test2";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";
    public static String[] pbs = new String[5000];
    public static boolean created = false;
    public static boolean withquer = false;
    public static int allpbs = 0;
    private static File docsfile;
    private static File zuordfile;
    private static int processbar = 0;
    private static final int SLEEPTIME = 2000;
    private static final boolean DEBUG = false;
    private static int countnws;

    @SuppressWarnings("CallToPrintStackTrace")
    private static void createHTML(String[] args) throws SQLException {

        //Datenbank erstellen/öffnen
        Connection connection = getDBConnection();
        @SuppressWarnings("UnusedAssignment")
        PreparedStatement selectPreparedStatement = null;
        Connection connectioncount = getDBConnection();
        PreparedStatement countStatement = null;
        //Datenbankabfrage Netzwerke natürlich soriert
        String SelectQuery = "select * FROM networks ORDER BY id,pb,nw";
        String countQuery = "select COUNT(id) FROM networks";
        try {
            processbar = 0;
            connection.setAutoCommit(false);

            selectPreparedStatement = connection.prepareStatement(SelectQuery);
            ResultSet rs = selectPreparedStatement.executeQuery();
            //System.out.println("Connect Database");
            //Hole jedes Netzwerk nacheinander aus der Datenbank
            int lastpb = 0;
            int tempallpbs = 0;
            while (rs.next()) {
                if (DEBUG) {
                    System.out.println(rs.getInt("pb"));
                }
                processbar++;
                int progress = Math.round((100f / countnws) * processbar);
                S5ParserFrame.jprogressBar.setValue(progress);
                tempallpbs++;
                String tempnw = rs.getString("code");
                //System.out.println(tempnw);
                //Suchmuster nach t.B. E/A/M 12.1

                //tempnw = getLinks("[ ]([SEAM][ ]{1,4}[0-9]{1,3}[.][0-9])[\\n\\r\\s]+", rs.getInt("pb"), rs.getInt("nw"), tempnw);
                //tempnw = getLinks("[ ]([T][ ]{1,3}[0-9]{1,3})[\\n\\r\\s]+", rs.getInt("pb"), rs.getInt("nw"), tempnw);
                //tempnw = getLinks("[ ]([Z][ ]{1,3}[0-9]{1,3})[\\n\\r\\s]+", rs.getInt("pb"), rs.getInt("nw"), tempnw);
                //tempnw = getLinks("[ ]([DF][BWX][ ]{1,3}[0-9]{1,3})([ ]|[^ ])", rs.getInt("pb"), rs.getInt("nw"), tempnw);
                tempnw = getLinks("([SEAM][ ]{1,4}[0-9]{1,3}\\.[0-9]{1})(?:[^\\d])", rs.getInt("pb"), rs.getInt("nw"), tempnw);//([ ]|\\r)
                tempnw = getLinks("([T][ ]{1,3}[0-9]{1,3})", rs.getInt("pb"), rs.getInt("nw"), tempnw);
                tempnw = getLinks("([Z][ ]{1,3}[0-9]{1,3})", rs.getInt("pb"), rs.getInt("nw"), tempnw);
                tempnw = getLinks("([-DFPSM|][-BWXDS][ ]{1,3}[0-9]{1,3})(?:[^\\d])", rs.getInt("pb"), rs.getInt("nw"), tempnw);
                String pblink = "";
                String nwlink = "";
                //tempnw = getLinks("[F][B][ ]{1,3}[0-9]{1,3}[ ]", rs.getInt("pb"), rs.getInt("nw"), tempnw);
                //füge vor jedes Netzwderk eine Sprungmarke PBXXNWXX
                if (rs.getInt("pb") < 256) {
                    nwlink = "<a href=\"#\" id=\"" + "pb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\"></a>";
                    //Suche Zeilen mit dem Begriff "Netzwerk" und Klammere diese in Dickschrift ein              
                    tempnw = tempnw.replaceAll("(.*Netzwerk.*)", "<h4>PB" + rs.getInt("pb") + " $1 </h4>");
                    //Hänge an laufenden Dateitext an.
                    pblink = "<a href=\"#\" id=\"" + "pb" + rs.getInt("pb") + "\"></a>";
                } else if (rs.getInt("pb") < 512) {
                    nwlink = "<a href=\"#\" id=\"" + "pb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\"></a>";
                    //Suche Zeilen mit dem Begriff "Netzwerk" und Klammere diese in Dickschrift ein              
                    tempnw = tempnw.replaceAll("(.*Netzwerk.*)", "<h4>FB" + (rs.getInt("pb") - 256) + " $1 </h4>");
                    //Hänge an laufenden Dateitext an.
                    pblink = "<a href=\"#\" id=\"" + "fb" + (rs.getInt("pb") - 256) + "\"></a>";
                } else if (rs.getInt("pb") < 768) {
                    nwlink = "<a href=\"#\" id=\"" + "pb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\"></a>";
                    //Suche Zeilen mit dem Begriff "Netzwerk" und Klammere diese in Dickschrift ein              
                    tempnw = tempnw.replaceAll("(.*Netzwerk.*)", "<h4>FX" + (rs.getInt("pb") - 512) + " $1 </h4>");
                    //Hänge an laufenden Dateitext an.
                    pblink = "<a href=\"#\" id=\"" + "fx" + (rs.getInt("pb") - 512) + "\"></a>";
                } else if (rs.getInt("pb") < 1024) {
                    nwlink = "<a href=\"#\" id=\"" + "pb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\"></a>";
                    //Suche Zeilen mit dem Begriff "Netzwerk" und Klammere diese in Dickschrift ein              
                    tempnw = tempnw.replaceAll("(.*Netzwerk.*)", "<h4>OB" + (rs.getInt("pb") - 768) + " $1 </h4>");
                    //Hänge an laufenden Dateitext an.
                    pblink = "<a href=\"#\" id=\"" + "ob" + (rs.getInt("pb") - 768) + "\"></a>";
                } else if (rs.getInt("pb") < 1280) {
                    nwlink = "<a href=\"#\" id=\"" + "sb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\"></a>";
                    //Suche Zeilen mit dem Begriff "Netzwerk" und Klammere diese in Dickschrift ein              
                    tempnw = tempnw.replaceAll("(.*Netzwerk.*)", "<h4>SB" + (rs.getInt("pb") - 1024) + " $1 </h4>");
                    //Hänge an laufenden Dateitext an.
                    pblink = "<a href=\"#\" id=\"" + "sb" + (rs.getInt("pb") - 1024) + "\"></a>";
                } else {
                }
                if (lastpb != rs.getInt("pb")) {
                    output += pblink;
                }
                lastpb = rs.getInt("pb");

                output += nwlink;
                output += tempnw;

            }
            selectPreparedStatement.close();

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!MEM) {
                connection.close();
            }
        }
    }

    //Hole das nächste PB/NW des übergebenen E/A/M zur Weitervelinkung
    private static String checkLinks(int pb, int nw, String eamname) throws SQLException {
        Connection connection = getDBConnection();
        String ret = "";
        //Entferne alle Leerzeichen zwischen1 und 5 aufeinanderfolgenden
        String eamtemp = eamname.replaceAll("[ ]{1,5}", "");
         eamname = eamname.replaceAll(" ", "&nbsp;");
        PreparedStatement selectPreparedStatement = null;
        PreparedStatement selectPreparedStatement2 = null;

        try {
            connection.setAutoCommit(false);
            //Suche in Tabelle nächstes E/A/M in dem das Netzwerk oder Baustein höher ist als das jetzige
            String titleQuery = "select eamname,pb,GROUP_CONCAT(nw ORDER BY ID SEPARATOR ' ') AS nw FROM refs WHERE eamname='" + eamtemp + "' GROUP BY pb ORDER BY pb";
            //  System.out.println(SelectQuery);
  PreparedStatement titlePreparedStatement = null;
                titlePreparedStatement = connection.prepareStatement(titleQuery);
                ResultSet rst = titlePreparedStatement.executeQuery();
                String title = "";//eamtemp + "&#13";//"<br>";

                while (rst.next()) {
                    if (rst.getInt("pb") < 256) {
                        title += "PB" + rst.getInt("pb") + " : " + rst.getString("nw") + "&#13";
                    } else if (rst.getInt("pb") < 512) {
                        title += "FB" + (rst.getInt("pb") - 256) + " : " + rst.getString("nw") + "&#13";
                    } else if (rst.getInt("pb") < 768) {
                        title += "FX" + (rst.getInt("pb") - 512) + " : " + rst.getString("nw") + "&#13";
                    } else if (rst.getInt("pb") < 1024) {
                        title += "OB" + (rst.getInt("pb") - 768) + " : " + rst.getString("nw") + "&#13";
                    } else if (rst.getInt("pb") < 1280) {
                        title += "SB" + (rst.getInt("pb") - 1024) + " : " + rst.getString("nw") + "&#13";
                    }
                }
                titlePreparedStatement.close();
            String SelectQuery = "select "
                    + "nw,pb,eamname FROM refs WHERE eamname='" + eamtemp + "' AND( (nw>" + nw + "  AND pb=" + pb + ") OR ( pb>" + pb + "))  ORDER BY pb,nw LIMIT 1";
            //  System.out.println(SelectQuery);
            selectPreparedStatement = connection.prepareStatement(SelectQuery);
            ResultSet rs = selectPreparedStatement.executeQuery();
            //Ergebnis verarbeiten
            //Wenn weiteres vorhanden austauschen, ansonsten Link zum ersten
            if (rs.next()) {
              
              
                //  ret = "<a href=\"#" + "pb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\" title=\""+title+" \" class=\"tooltip\">" + eamname + "<span class=\"tooltiptext\">"+title+"</span></a>";
                ret = "<a href=\"#" + "pb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\" title=\"" + title + " \" >" + eamname + "</a>";

            } else {
                // System.out.println(ret);
                //Hole erstes Vorkommen
                SelectQuery = "select "
                        + "nw,pb,eamname FROM refs WHERE eamname='" + eamtemp + "' ORDER BY pb,nw LIMIT 1";
                //  System.out.println(SelectQuery);
                selectPreparedStatement2 = connection.prepareStatement(SelectQuery);
                ResultSet rs2 = selectPreparedStatement2.executeQuery();
                //Tausche beim letzten Vorkommen einen Sprungpunkt zum ersten zurück
                if (rs2.next()) {
                    ret = "<a href=\"#" + "pb" + rs2.getInt("pb") + "nw" + rs2.getInt("nw") + "\"  title=\"" + title + " \">" + eamname + "</a>";

                } else {
                    ret = eamname;
                }
                selectPreparedStatement2.close();
            }
            //System.out.println(ret);
            selectPreparedStatement.close();

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
            //  System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!MEM) {
                connection.close();
            }
        }
        return ret;
    }

    public Fileparser(String[] args) throws SQLException, IOException {
        if (args[1] != null) {
            output += STYLEZUORD;
        } else {
            output += STYLESINGLE;
        }

        output += HEADER2;
        createHTML(args);
        output += CONTENT;
        if (args[1] != null) {
            zuordHtml(args[1]);
        }
        output += FOOTER;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File("."));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Html-Output", ".html");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setDialogTitle("Speichern.....");
        try {
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (fileToSave.getAbsolutePath() != "") {
                    saveFile(fileToSave.getAbsolutePath() + ".html");
                }
            }

        } catch (IOException ex) {

        }
        int eingabe = JOptionPane.showConfirmDialog(null,
                "Weitere Dateien", "Konvertieren?",
                JOptionPane.YES_NO_OPTION);
        if (eingabe != 0) {
            System.exit(0);
        }
        jTextField3.setText("...");
        jTextField1.setText("...");
        output = "";
    }

    private static void saveFile(String file) throws IOException {
        Path mypath;
        mypath = Paths.get(file);

        try (BufferedWriter writer = Files.newBufferedWriter(mypath)) {
            writer.write(output);
        }
    }

    private static void zuordHtml(String zuordPath) throws IOException, SQLException {
        // zuordPath = "C:\\Users\\philm\\OneDrive\\Dokumente\\NetBeansProjects\\S5ParserThreaded\\ZUORDNLS.INI";
        String zstring = new String(Files.readAllBytes(Paths.get(zuordPath)));
        zstring = zstring.replaceAll("(?m)^.*Datei.*$", "");
        zstring = zstring.replaceAll("(?m)^.*Operand.*$", "");
        String ztemp;
        ztemp = zstring.replaceFirst("([ ]+|>)([E][ ]{1,3}[0-9]{1,3}[.][0-7])", "<a href=\"\" id=\"ZLE\"><h4>Eingänge</h4></a><br><br>$0");
        ztemp = ztemp.replaceFirst("([ ]+|>)([A][ ]{1,3}[0-9]{1,3}[.][0-7])", "<a href=\"\" id=\"ZLA\"><h4>Ausgänge</h4></a><br><br>$0");
        ztemp = ztemp.replaceFirst("([ ]+|>)([S][ ]{1,3}[0-9]{1,3}[.][0-7])", "<a href=\"\" id=\"ZLS\"><h4>Ersatzmerker</h4></a><br><br>$0");
        ztemp = ztemp.replaceFirst("([ ]+|>)([M][ ]{1,3}[0-9]{1,3}[.][0-7])", "<a href=\"\" id=\"ZLM\"><h4>Merker</h4></a><br><br>$0");
        ztemp = ztemp.replaceFirst("([ ]+|>)([P][B][ ]{1,3}[0-9]{1,3})[ ]", "<a href=\"\" id=\"ZPB\"><h4>Bausteine</h4></a><br><br>$0");
        ztemp = ztemp.replaceFirst("([ ]+|>)([T][ ]{1,3}[0-9]{1,3})[ ]", "<a href=\"\" id=\"ZPT\"><h4>Timer</h4></a><br><br>$0");
        ztemp = ztemp.replaceFirst("([ ]+|>)([Z][ ]{1,3}[0-9]{1,3})[ ]", "<a href=\"\" id=\"ZPZ\"><h4>Zähler</h4></a><br><br>$0");
        zstring = ztemp.replaceAll("[P][B][ ]{1,3}([0-9]{1,3})[ ]", "<a href=\"#pb$1\" >PB$1</a>");
        zstring = zstring.replaceAll("[F][B][ ]{1,3}([0-9]{1,3})[ ]", "<a href=\"#fb$1\" >FB$1</a>");
        zstring = zstring.replaceAll("[O][B][ ]{1,3}([0-9]{1,3})[ ]", "<a href=\"#ob$1\" >OB$1</a>");
        zstring = zstring.replaceAll("[F][X][ ]{1,3}([0-9]{1,3})[ ]", "<a href=\"#fx$1\" >FX$1</a>");
        zstring = createZuordnungen(zstring, "([D][B|W][ ]{1,3}[0-9]{1,3}[ ]{1})");

        zstring = createZuordnungen(zstring, "([S|E|A|M][ ]{1,4}[0-9]*[.][0-7][ ])");

        zstring = createZuordnungen(zstring, "([T|Z][ ]{1,3}[0-9]{1,3}[ ]{1})");

        output += zstring;
        // System.out.println("Printout newhtml");
        // System.out.println(newHtml);

    }

    private static void insertNWWithPreparedStatement(int pb, int nw, String code) throws SQLException {
        Connection connection = getDBConnection();
        PreparedStatement insertPreparedStatement = null;
        PreparedStatement selectPreparedStatement = null;

        processbar++;
        int progress = Math.round((100f / countnws) * processbar);
        //System.out.println("Progress"+progress);
        S5ParserFrame.jprogressBar.setValue(progress);
        if (DEBUG) {
            System.out.println("Insert" + pb);
        }
        //S5ParserFrame.check.setText("Verarbeitete Netzwerke: "+String.valueOf(processbar)+"/"+String.valueOf(countnws));
        String InsertQuery = "INSERT INTO networks" + "( pb,nw,code) values" + "(?,?,?)";
        //String SelectQuery = "select * FROM networks WHERE pb=" + pb + " AND nw=" + nw + "";
        try {
            connection.setAutoCommit(false);

            insertPreparedStatement = connection.prepareStatement(InsertQuery);
            insertPreparedStatement.setInt(1, pb);
            insertPreparedStatement.setInt(2, nw);
            insertPreparedStatement.setString(3, code);

            insertPreparedStatement.executeUpdate();
            insertPreparedStatement.close();

            /*  selectPreparedStatement = connection.prepareStatement(SelectQuery);
            ResultSet rs = selectPreparedStatement.executeQuery();
           System.out.println("H2 Database inserted through PreparedStatement");
            while (rs.next()) {
                System.out.println("PB " + rs.getInt("pb") + " NW " + rs.getString("nw") + "" + rs.getString("code"));
            }
            selectPreparedStatement.close();
             */
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!MEM) {
                connection.close();
            }
        }
    }

    private static void insertREFWithPreparedStatement(String eamname, int pb, int nw) throws SQLException {
        Connection connection = getDBConnection();
        PreparedStatement insertPreparedStatement = null;
        PreparedStatement selectPreparedStatement = null;

        String InsertQuery = "INSERT INTO refs" + "( id,eamname,pb,nw) values" + "(?,?,?,?)";
        String SelectQuery = "select * FROM refs WHERE pb=" + pb + " AND nw=" + nw;
        try {
            insertPreparedStatement = connection.prepareStatement(InsertQuery);
            insertPreparedStatement.setInt(1, 0);
            insertPreparedStatement.setString(2, eamname.replaceAll(" ", ""));
            insertPreparedStatement.setInt(3, pb);
            insertPreparedStatement.setInt(4, nw);

            insertPreparedStatement.executeUpdate();
            insertPreparedStatement.close();

            /*selectPreparedStatement = connection.prepareStatement(SelectQuery);
            ResultSet rs = selectPreparedStatement.executeQuery();
            System.out.println("H2 Database inserted through PreparedStatement");
            while (rs.next()) {
                System.out.println("PB " + rs.getInt("pb") + " NW " + rs.getString("nw") + "" + rs.getString("eamname"));
            }
            selectPreparedStatement.close();
             */
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
            if (!MEM) {
                connection.close();
            }
        }
    }
    // H2 SQL Statement Example

    private static void createTables() throws SQLException {
        Connection connection = getDBConnection();
        PreparedStatement createPreparedStatement = null;

        PreparedStatement createPreparedStatement2 = null;

        String CreateQuery = "CREATE TABLE IF NOT EXISTS refs(id INTEGER auto_increment,eamname varchar(255),pb int, nw int)";
        String CreateQuery2 = "CREATE TABLE IF NOT EXISTS networks(id  INTEGER PRIMARY KEY AUTO_INCREMENT,pb int,nw int, code longvarchar(20000))";
        try {
            connection.setAutoCommit(false);

            createPreparedStatement = connection.prepareStatement(CreateQuery);
            createPreparedStatement.executeUpdate();
            createPreparedStatement.close();

            createPreparedStatement2 = connection.prepareStatement(CreateQuery2);
            createPreparedStatement2.executeUpdate();
            createPreparedStatement2.close();

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!MEM) {
                connection.close();
            }
        }
    }

    private static Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,
                    DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }

    private static String createZuordnungen(String zstring, String pattern) throws SQLException {
        Pattern nwp = Pattern.compile(pattern);
        String buffer = zstring;
        Matcher nwpmatcher = nwp.matcher(zstring);
        Connection connection = getDBConnection();
        PreparedStatement selectPreparedStatement = null;
        PreparedStatement titlePreparedStatement = null;
        connection.setAutoCommit(false);
        try {
            while (nwpmatcher.find()) {
                String eamtemp = nwpmatcher.group(1).replaceAll(" ", "");
                String SelectQuery = "select "
                        + "* FROM refs WHERE eamname='" + eamtemp + "' ORDER BY pb,nw LIMIT 1";
                selectPreparedStatement = connection.prepareStatement(SelectQuery);
                ResultSet rs = selectPreparedStatement.executeQuery();
                String titleQuery = "select eamname,pb,GROUP_CONCAT(nw ORDER BY ID SEPARATOR ' ') AS nw FROM refs WHERE eamname='" + eamtemp + "' GROUP BY pb ORDER BY pb";
                //System.out.println("H2 Database inserted through PreparedStatement");
                titlePreparedStatement = connection.prepareStatement(titleQuery);
                while (rs.next()) {
        
           
                ResultSet rst = titlePreparedStatement.executeQuery();
                String title = "";//eamtemp + "&#13";//"<br>";

                while (rst.next()) {
                    if (rst.getInt("pb") < 256) {
                        title += "PB" + rst.getInt("pb") + " : " + rst.getString("nw") + "&#13";
                    } else if (rst.getInt("pb") < 512) {
                        title += "FB" + (rst.getInt("pb") - 256) + " : " + rst.getString("nw") + "&#13";
                    } else if (rst.getInt("pb") < 768) {
                        title += "FX" + (rst.getInt("pb") - 512) + " : " + rst.getString("nw") + "&#13";
                    } else if (rst.getInt("pb") < 1024) {
                        title += "OB" + (rst.getInt("pb") - 768) + " : " + rst.getString("nw") + "&#13";
                    } else if (rst.getInt("pb") < 1280) {
                        title += "SB" + (rst.getInt("pb") - 1024) + " : " + rst.getString("nw") + "&#13";
                    }
                }
                titlePreparedStatement.close();
                    String ret = "<a href=\"#" + "pb" + rs.getInt("pb") + "nw" + rs.getInt("nw") + "\" title=\"" + title + "\">" + nwpmatcher.group(1) + "</a>";
                     System.out.println("EAM" + rs.getString("eamname") + " NW " + rs.getString("nw"));
                    buffer = buffer.replaceAll(nwpmatcher.group(1), ret);
                }
                selectPreparedStatement.close();

            }
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!MEM) {
                connection.close();
            }
        }
        //  insertCommWithPreparedStatement(String eamname, String comm)

        return buffer;
    }

    private static void getCommWithPreparedStatement(String eamname, String comm) throws SQLException {
        Connection connection = getDBConnection();
        PreparedStatement createPreparedStatement = null;
        PreparedStatement insertPreparedStatement = null;
        PreparedStatement selectPreparedStatement = null;

        String CreateQuery = "CREATE TABLE IF NOT EXISTS comms(id int auto_increment,eamname varchar(255),comm varchar(255))";
        String InsertQuery = "INSERT INTO comms" + "( id,eamname,comm) values" + "(?,?,?)";
        String SelectQuery = "select * FROM comms";
        try {
            connection.setAutoCommit(false);

            createPreparedStatement = connection.prepareStatement(CreateQuery);
            createPreparedStatement.executeUpdate();
            createPreparedStatement.close();

            insertPreparedStatement = connection.prepareStatement(InsertQuery);
            insertPreparedStatement.setInt(1, 0);
            insertPreparedStatement.setString(2, eamname.replaceAll("^ ", ""));
            insertPreparedStatement.setString(3, comm);

            insertPreparedStatement.executeUpdate();
            insertPreparedStatement.close();

            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!MEM) {
                connection.close();
            }
        }
    }
    // H2 SQL Statement Example

    private static void patterncheck(int pb, int nwi, String nw, String pattern) throws SQLException {
        Pattern nwp = Pattern.compile(pattern);
        Matcher nwpmatcher = nwp.matcher(nw);
        Connection connection = getDBConnection();
        PreparedStatement selectPreparedStatement = null;

        try {
            while (nwpmatcher.find()) {
                String eamtemp = nwpmatcher.group(1).replaceAll(" ", "");
                String SelectQuery = "select "
                        + "* FROM refs WHERE eamname='" + eamtemp + "' AND pb=" + pb + " AND nw=" + nwi + " ORDER BY pb,nw LIMIT 1";
                selectPreparedStatement = connection.prepareStatement(SelectQuery);
                ResultSet rs = selectPreparedStatement.executeQuery();
                //System.out.println("H2 Database inserted through PreparedStatement");
                if (!rs.next()) {
                    insertREFWithPreparedStatement(eamtemp, pb, nwi);
                }
                selectPreparedStatement.close();

            }
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!MEM) {
                connection.close();
            }
        }
    }

    private static String getLinks(String pattern, int pb, int nw, String tempnw) throws SQLException {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(tempnw);
        int oldposition = 0;
        int i = 0;
        // if(pb>4)System.exit(0);
        //Wende auf jedes gefundene Suchmuster an
        while (m.find()) {
            int position = m.start();
            int match = m.end();

            String temprep = m.group(1);
            //Hole das Netzwerk mit dem nächsten vorkommen
            String ersatz = checkLinks(pb, nw, temprep);

            tempnw = tempnw.replaceAll(temprep, ersatz);
        }
        return tempnw;

    }
    public static String[] myargs;

    public static void setArg(String[] args) {

        Fileparser.myargs = args;

    }

    public void startparsing(String[] args) {
        try {

            readFiles(args[0], args[1]);
            //System.out.println(docsfile.getAbsolutePath());
            createHTML(args);
            if (args[1] != null) {
                zuordHtml(zuordfile.getAbsolutePath());
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Html-Output", ".html");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setDialogTitle("Speichern.....");

        } catch (SQLException ex) {
            Logger.getLogger(S5ParserFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(S5ParserFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static JDialog dlg;

    @SuppressWarnings("empty-statement")
    static void readFiles(String docPath, String zuordPath) throws SQLException, IOException {
        if (!created) {
            jLabel6.setText("Lese Dateien..... Bitte warten!");
            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            createTables();
            String refs[] = new String[10000];
            String refsindex[] = new String[10000];
            refs[0] = "";
            int oldposition = 0;
            int i = 0;
            String refsindextmp = "";
            boolean firstt = true;

            String code = "";

            code = new String(Files.readAllBytes(Paths.get(docPath)));
            int count = 0;
            int counterror = 0;

            code = code.replaceAll(".*Blatt.*", "");
            code = code.replaceAll("<", "&lt;");
            code = code.replaceAll(">", "&gt;");
            //  if(source)jprogressBar.setValue(5);
            Pattern pattern = Pattern.compile(".*Netzwerk[ ]+[0-9]{1,3}[ ]+[0-9A-Z]{4}.*");
            Matcher countmatcher = pattern.matcher(code);
            while (countmatcher.find()) {
                countnws++;
            }
            Pattern pattern2 = Pattern.compile("\\bNetzwerk\\b[ ]+[0-9]{1,3}[ ]+[0-9A-Z]{4}.*[\\n]{1,2}.*[\\n]{1,2}\\bNetzwerk\\b[ ]+[0-9]{1,3}[ ]+[0-9A-Z]{4}.*");
            Matcher countmatcher2 = pattern2.matcher(code);
            //\bNetzwerk\b[ ]+[0-9]{1,3}[ ]+[0-9A-Z]{4}.*[\n]{1,2}.*[\n]{1,2}\bNetzwerk\b[ ]+[0-9]{1,3}[ ]+[0-9A-Z]{4}.*
            while (countmatcher2.find()) {
                countnws--;
            }

            jLabel6.setText("Lese Netzwerke..... Bitte warten! Gefunden: " + countnws + " Netzwerke");
            ;
            try {
                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            S5ParserFrame.jprogressBar.setIndeterminate(false);
            int pbsindex[] = new int[5000];
            refs[0] = "";
            //Finde Zeilen die folgend aufgebaut sind:
            //Beliebige Zeichen (.*), dann PB und 1 Leerzeichen [ ], beliebige Zeichen (.*)
            //dann LAE folgend 1-4 Zeichen Ziffern
            Pattern p = Pattern.compile(".*([P|F|O|S][B|X][ ]{1,2}.*LAE=[0-9]{1,4}).*");
            Matcher matcher = p.matcher(code);

            i = 0;

            String oldmatcher = "";
            int oldstart = 0;
            int lastindex = 0;
            boolean first = true;
            boolean found = false;

            while (matcher.find()) {

                if (first) {
                    oldmatcher = matcher.group(1);

                    first = false;
                }

                found = true;
                String tmp = matcher.group(1);
                if (!oldmatcher.equals(tmp)) {
                    try {
                        String pbindextmp = oldmatcher.replaceAll("(.*)[P|F|O|S][B|X][ ]{1,2}([0-9]{1,3})(.*LAE=[0-9]{1,4})", "$2");
                        String identify = oldmatcher.replaceAll("(.*)([P|F|O|S][B|X])[ ]{1,2}(.*LAE=[0-9]{1,4})", "$2");

                        pbsindex[i] = Integer.parseInt(pbindextmp);
                        switch (identify) {
                            case "PB":
                                if (DEBUG) {
                                    System.out.println("PB gefunden");
                                }
                                pbsindex[i] = Integer.parseInt(pbindextmp);
                                break;
                            case "FB":
                                pbsindex[i] = Integer.parseInt(pbindextmp) + 256;
                                if (DEBUG) {
                                    System.out.println("FB gefunden" + i + " " + pbsindex[i]);
                                }
                                break;
                            case "FX":
                                pbsindex[i] = Integer.parseInt(pbindextmp) + 512;
                                if (DEBUG) {
                                    System.out.println("FX gefunden" + i + " " + pbsindex[i]);
                                }
                                break;
                            case "OB":
                                pbsindex[i] = Integer.parseInt(pbindextmp) + 768;
                                if (DEBUG) {
                                    System.out.println("OB gefunden");
                                }
                                break;
                            case "SB":
                                pbsindex[i] = Integer.parseInt(pbindextmp) + 1024;
                                if (DEBUG) {
                                    System.out.println("SB gefunden");
                                }
                                break;
                            default:
                                pbsindex[i] = Integer.parseInt(pbindextmp);
                                break;
                        }
                        ;
                        pbs[i] = code.substring(oldstart, matcher.start());

                        pbs[i] = pbs[i].replaceAll("(.*[P|F|O|S][B|X][ ]{1,2})([0-9]{1,2})(.*LAE=[0-9]{1,4})", "");

                        oldstart = matcher.start();
                        oldmatcher = matcher.group(1);
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    i++;
                } else {

                    oldmatcher = matcher.group(1);
                }

            }

            String pbindextmp2 = oldmatcher.replaceAll("(.*)[P|F|O|S][B|X][ ]{1,2}([0-9]{1,3})(.*LAE=[0-9]{1,4})", "$2");
            String identify = oldmatcher.replaceAll("(.*)([P|F|O|S][B|X])[ ]{1,2}(.*LAE=[0-9]{1,4})", "$2");
            if ("PB".equals(identify)) {
                if (DEBUG) {
                    System.out.println("PB gefunden");
                }
                pbsindex[i] = Integer.parseInt(pbindextmp2);
            } else if ("FB".equals(identify)) {

                pbsindex[i] = Integer.parseInt(pbindextmp2) + 256;
                if (DEBUG) {
                    System.out.println("FB gefunden" + i + " " + pbsindex[i]);
                }
            } else if ("FX".equals(identify)) {

                pbsindex[i] = Integer.parseInt(pbindextmp2) + 512;
                if (DEBUG) {
                    System.out.println("FX gefunden" + i + " " + pbsindex[i]);
                }
            } else if ("OB".equals(identify)) {

                pbsindex[i] = Integer.parseInt(pbindextmp2) + 768;
                if (DEBUG) {
                    System.out.println("OB gefunden");
                }
            } else if ("SB".equals(identify)) {

                pbsindex[i] = Integer.parseInt(pbindextmp2) + 1024;
                if (DEBUG) {
                    System.out.println("SB gefunden");
                }
            } else {
                pbsindex[i] = Integer.parseInt(pbindextmp2);
            };

            pbs[i] = code.substring(oldstart, code.length());
            if (DEBUG) {
                System.out.println("Letzter Baustein");
            }
            if (DEBUG) {
                System.out.println(pbs[i]);
            }
            pbs[i] = pbs[i].replaceAll("(.*[P|F|O|S]B[ ]{1,2})([0-9{1,2}])(.*LAE=[0-9]{1,4})", "");
            i++;

            if (DEBUG) {
                if (!found) {
                    System.out.println("Sorry, no match!");
                }
            }

            allpbs = i;
            jLabel6.setText("Generiere Netzwerke..... Bitte warten!");
            for (int z = 0; z < i; z++) {

                String pbtmp = pbs[z];
                if (DEBUG) {
                    System.out.println(z);
                }
                int nwindex[] = new int[200];
                String nws[] = new String[200];
                oldmatcher = "";
                Pattern nwp = Pattern.compile(".*(Netzwerk[ ]{1,2})([0-9]{1,3}).*");
                Matcher nwpmatcher = nwp.matcher(pbtmp);
                int oldnwposition = 0;
                int x = 0;
                int oldnwstart = 0;
                int lastnwindex = 0;
                first = true;

                while (nwpmatcher.find()) {

                    if (first) {
                        oldmatcher = nwpmatcher.group();
                        first = false;
                    }

                    String tmp = nwpmatcher.group();

                    if (!oldmatcher.equals(tmp)) {
                        String nwindextmp = oldmatcher.replaceAll(".*(Netzwerk[ ]{1,2})([0-9]{1,3}).*", "$2");
                        oldmatcher = nwpmatcher.group();
                        nwindex[x] = Integer.parseInt(nwindextmp);
                        nws[x] = pbtmp.substring(oldnwstart, nwpmatcher.start());
                        oldnwstart = nwpmatcher.start();
                        x++;
                    } else {
                        oldmatcher = nwpmatcher.group();
                    }

                }
                first = true;
                String nwindextmp = oldmatcher.replaceAll(".*(Netzwerk[ ]{1,2})([0-9]{1,3}).*", "$2");
                nws[x] = pbtmp.substring(oldnwstart, pbtmp.length());
                nwindex[x] = Integer.parseInt(nwindextmp);
                networkcreator(x + 1, pbsindex[z], nwindex, z, nws);
            }
        }
        S5ParserFrame.jprogressBar.setValue(100);
        jLabel6.setText("Generiere Outputfile!");
        try {
            Thread.sleep(SLEEPTIME);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static void networkcreator(int fx, int pbindex, int[] fnwindex, int fz, String[] fnws) {
        if (DEBUG) {
            System.out.println();
        }
        for (int o = 0; o < fx; o++) {
            if (DEBUG) {
                System.out.println("networkcreator" + pbindex);
            }
            if (DEBUG) {
                System.out.println(fnws[o]);
            }
            if (DEBUG) {
                System.out.println();
            }
            if (DEBUG) {
                System.out.println(fnwindex[o]);
            }
            try {
                insertNWWithPreparedStatement(pbindex, fnwindex[o], fnws[o]);
                fnws[o] = fnws[o].replaceAll(".*[S|E|A|M][ ]{1,3}{0,4}[0-9]{1,3}[.][0-7][ ][=][ ].*", "");
                fnws[o] = fnws[o].replaceAll(".*[ ]+([T|Z][ ]{1,3}[0-9]{1,3})[ ][ ][=][ ].*", "");
                //fnws[o] = fnws[o].replaceAll(".*[ ]+[F|P|][D|B|W|X][ ]{1,5}[0-9]{1,3}[ ].*", "");
                patterncheck(pbindex, fnwindex[o], fnws[o], "([S|E|A|M][ ]{1,3}{0,4}[0-9]{1,3}[.][0-7])");
                patterncheck(pbindex, fnwindex[o], fnws[o], "[ ]+([T|Z][ ]{1,3}[0-9]{1,3})[ ]");
                patterncheck(pbindex, fnwindex[o], fnws[o], "([-DFPSM|][-BWXD][ ]{0,5}[0-9]{1,3})");
            } catch (SQLException ex) {
                Logger.getLogger(S5ParserFrame.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex);
            }

        }
    }

    private static final String HEADER1 = "<!DOCTYPE html><html lang=\"de\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><title>S5Parser</title><style>\n";
    private static final String STYLEZUORD = "pre{\nfont-size: 14px;\n\n} \na:link { background: lightgrey; text-decoration: none; color: black; }\na:visited { background: lightgrey; text-decoration: none; color: black; }\n\n/* The navigation bar */\n.navbar {\n    overflow: hidden;\n    background-color:  lightgrey;\n    position: fixed; /* Set the navbar to fixed position */\n    bottom: 0; /* Position the navbar at the top of the page */\n    width: 100%;\n    height:4%;\n    z-index:10;\n}\n\n/* Links inside the navbar */\n.navbar a {\n    float: left;\n    display: block;\n    color: #red;\n    text-align: center;\n    padding: 10px 16px;\n    text-decoration: none;\n}\nbody{\nmargin:0;\npadding:0;\n}\n/* Change background on mouse-over */\n.navbar a:hover {\n    background: #888888;\n    color: black;\n}\n.right{\n\nright: 0;\nwidth:50%;\nheight:100%;\noverflow-y: none;\nposition: fixed;\n\n}\npre{\nmargin-left:10px;\n}\n.Zuordnung {\n\noverflow-y: scroll;\nbackground: #EEEEEE;\nheight:96%;\n}\n/* Main content */\n.split{\nwidth:50%;\nheight:100%;\noverflow-y: scroll;\nposition: fixed;\n}\n.container{\nwidth:100%;\nheight:100%;\npadding:0;\n\n}\n\n.main {\nleft:0;\n\n   }";
    private static final String HEADER2 = "</style></head><body>\n\n<div class=\"container\">\n<div class=\"main split\"><pre><a href=\"#\" id=\"bausteine\"></a>";
    private static final String CONTENT = "</pre></div>\n<div class=\"right\">\n<div class=\"Zuordnung\" ><pre>";
    private static final String FOOTER = "</pre></div>\n<div tabindex=\"1\" class=\"navbar\"><a href=\"#ZLE\" tabindex=\"10\" onclick=\"\">Eingänge</a><a href=\"#ZLA\" tabindex=\"10\" onclick=\"\">Ausgänge</a><a href=\"#ZLM\" tabindex=\"10\" onclick=\"\">Merker</a><a href=\"#ZLS\" tabindex=\"10\" onclick=\"\">Ersatzmerker</a><a href=\"#ZPB\" tabindex=\"10\" >Bausteine</a><a href=\"#ZPT\" tabindex=\"10\" >Timer</a><a href=\"#ZPZ\" tabindex=\"10\" >Zähler</a></div>\n</div></div></body></html>";
    private static final String STYLESINGLE = "pre{\nfont-size: 14px;\nmargin-left:10px;\n}\n.tooltip {\n    position: relative;\n  display: inline-block;\n}\n\n.tooltip .tooltiptext {\n    visibility: hidden;\n    /*width: 60px;*/\n    background-color: #555;\n    color: #fff;\n   text-align: center;\n   padding: 5px 0;\n    border-radius: 6px;\n\n    /* Position the tooltip text */\n    position: absolute;\n    z-index: 1;\n    bottom: 125%;\n    left: 50%;\n    margin-left: 0px;\n\n    /* Fade in tooltip */\n    opacity: 0;\n    transition: opacity 0.3s;\n}\n\na:link { background: lightgrey; text-decoration: none; color: black; }\na:visited { background: lightgrey; text-decoration: none; color: black; }\n\n.navbar {\n    overflow: hidden;\n    background-color:  lightgrey;\n    position: fixed; /* Set the navbar to fixed position */\n    bottom: 0; /* Position the navbar at the top of the page */\n    width: 100%;\n    height:4%;\n    z-index:10;\n}\n\n.navbar a {\n    float: left;\n    display: block;\n    color: #red;\n    text-align: center;\n    padding: 10px 16px;\n    text-decoration: none;\n}\n\nbody{\nmargin:0;\npadding:0;\n}\n\n.navbar a:hover {\n    background: #888888;\n    color: black;\n}\n\n.right{\ndisplay:none;\nright: 0;\nwidth:0%;\nheight:100%;\noverflow-y: none;\nposition: fixed;\n}\n\n.Zuordnung {\noverflow-y: scroll;\nbackground: #EEEEEE;\nheight:96%;\n}\n\n.split{\n\nheight:100%;\noverflow-y: scroll;\nposition: fixed;\n}\n\n.container{\nwidth:100%;\nheight:100%;\npadding:0;\n}\n\n.main {\nleft:0;\nwidth: 100%;\n}\n";
    private static String output = HEADER1;
}
