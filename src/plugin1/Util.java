/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugin1;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;


/**
 *
 * @author Antonio
 */
public class Util {
    public static String ARCHIVO_PARAMETROS = "parametrosIVA.config";
    
    public static boolean ERROR = false;
    public static String ID;
    public static int IVA12=1;
    public static int IVA10=2;
    
    public static int IVA_9=4;
    public static int IVA_7=5;
    
    
    public static double MONTOMAX=2000000;
    
    private static int INDEX_ROW_ERROR = 1;
    

    public static void cargarParametros() {

        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(ARCHIVO_PARAMETROS));
            IVA12= Integer.valueOf(properties.getProperty("IVA12"));
            IVA_9= Integer.valueOf(properties.getProperty("IVA9"));
            IVA_7= Integer.valueOf(properties.getProperty("IVA7"));
            MONTOMAX= Double.valueOf(properties.getProperty("MONTOMAX"));
            IVA10= Integer.valueOf(properties.getProperty("IVA10"));
            
        } catch (Exception e) {
            // e.printStackTrace();
            properties.clear();
            
            try {
                properties.store(new FileOutputStream(ARCHIVO_PARAMETROS), null);
            } catch (Exception ex) {
            }
        }

    }
    
    public static void guardarError(Exception sError, boolean esGrave) {
        Logger logger = Logger.getLogger("MyLog");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler("Errores.log", true);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages
            logger.log(Level.SEVERE, null, sError);
            logger.log(Level.INFO, "********************************************************************************");
            fh.close();

        } catch (SecurityException | IOException e) {
        }
        if (esGrave) {
            JOptionPane.showMessageDialog(null, sError, "Mensaje", JOptionPane.ERROR_MESSAGE);
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error grave. \n Por favor contacte al administrador del sistema.", "Mensaje", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    public static void guardarError(Exception sError, boolean esGrave, java.awt.Component parent) {
        Logger logger = Logger.getLogger("MyLog");
        try {
            FileHandler fh = new FileHandler("Errores.log", true);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.log(Level.SEVERE, null, sError);
            logger.log(Level.INFO, "********************************************************************************");
            fh.close();
        } catch (java.io.IOException e) {
        }
        if (esGrave) {
            JOptionPane.showMessageDialog(parent, sError, "Mensaje", 0);
            JOptionPane.showMessageDialog(parent, "Ha ocurrido un error grave. \n Por favor contacte al administrador del sistema.", "Mensaje", 0);
            System.exit(0);
        }
    }
    
   
}
