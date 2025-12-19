/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugin1;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import conexionbd.Conector;
import conexionbd.Datos;
import java.awt.Image;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import shared.util.DateTimeInt;

/**
 *
 * @author ANTONIO FALTANTE VALIDAR EDAD DE SENIOR MAYOR A 60 ANOS Y COLOCAR EL
 * CAMPO DE HORARIO REDUCIDO COLOCAR LOS DIAS DE RENOVACION COMO CONFIGURABLE
 * PERMITIR NOTA DE CREDITOS EVITANDO QUE QUEDEN DIAS EN NEGATIVO SI EL CLIENTE
 * ESTA ACTIVO ENTONCES NO SOLICITAR FECHA DE INICIO LISTO
 */
public class MainFrame extends javax.swing.JFrame {

    public static final int DIAS_INVITACION = 30;
    public static final int DIAS_RENOVACION = 100;
    public static final int DIAS_PARA_PODER_CONGELAR = 10;
    public static final int ARTICULO_INVITACION = 0;
    public static final int ARTICULO_ENTRENADOR = 5;
    public static final int ARTICULO_INFLUENCER = 0;
    public static final int ARTICULO_FREEZE = 13;
    public static final int ARTICULO_SENIOR = -1;
    public static final int ARTICULO_1DAY_1 = 2;
    public static final int ARTICULO_1DAY_2 = 15;
    public static final int ARTICULO_1WEEK = 3;
    public static final int ARTICULO_PROMO = 0;
    public static final int ARTICULO_ENTRADAS = 10;
    public static final int ARTICULO_SPINNING = 12;
    public static final int EDAD_MINIMA_SENIOR = 60;
    public static final int CLIENTE_ENTRENADOR = 3;
    public static final int CLIENTE_INFLUENCER = 4;
    public static boolean PEDIR_HUELLA = false;

    /**
     * Creates new form MainFrame
     *
     */
    public MainFrame() {
        bundle = java.util.ResourceBundle.getBundle("idiomas/espanol");
        initComponents();
        //this.setResizable(false);

        this.etiquetaSQLNombre.setText("");
        //this.setUndecorated(true);
        try {

            Datos.cargarConfig();
            this.setIconImage(new ImageIcon(getClass().getResource("resources/icono.png")).getImage());
            //Util.cargarParametros();
            Factura.cargarXML(this);
            Datos.BD = Factura.BDGESTION;
            oFactura = new Factura();
            oFactura.cargarDatosFactura(this);
            Calendar fecha = Calendar.getInstance();
            //PENDIENTE AQUI DE SI LA MEMBRESIA DEL ENTRENADOR ES POR 30 DIAS CONTINUOS O POR MES ENTERO
            //if(oFactura.getCodArticulo() != ARTICULO_ENTRENADOR){
            fecha.add(Calendar.DAY_OF_MONTH, -1);
            this.comboFecha.setMinDate(fecha);
            fecha = Calendar.getInstance();
            if (oFactura.getCodArticulo() != ARTICULO_INVITACION) {
                fecha.add(Calendar.DAY_OF_MONTH, 30);
            }
            this.comboFecha.setMaxDate(fecha);
            //}else{ // SI ES ENTRENADOR

            //}
            switch (ACCION) {
                case "FINISH":

                    //JOptionPane.showMessageDialog(this, "Estoy finalizando", "Message", JOptionPane.ERROR_MESSAGE);
                    if (oFactura.getNumImpresiones() > 1 || oFactura.getIdEstado() == 0) {//SI YA HA SIDO IMPRESO PREVIAMENTE
                        System.exit(0);
                    }
                    if (oFactura.getDias() > 0 && oFactura.getMinimo() == 1 && oFactura.getCodArticulo() != ARTICULO_FREEZE) {//pendiente con esto
                        oFactura.actualizarFechaInicio(this);
                        System.exit(0);
                    }
                    //this.activarDesactivar();
                    // oRetencion.hacerRetencionISLR(true);
                    //JOptionPane.showMessageDialog(this, "Finalizando factura", "Mensaje", JOptionPane.INFORMATION_MESSAGE);
                    break;
                default:
                    //JOptionPane.showMessageDialog(this, "Voy por defecto", "Message", JOptionPane.ERROR_MESSAGE);
                    this.evaluar();
                    //descomentar en caso de querer saber quien recomendo a un cliente nuevo
                    /*if (oFactura.getCodArticulo() != ARTICULO_INVITACION) {
                        //evaluamos si el cliente es nuevo
                        //un cliente nuevo es aquel que no tiene ninguna factura asociada
                        
                        if (oFactura.getDuracion()>=30&&oFactura.esClienteNuevo(this)){
                            int confirmado = JOptionPane.showConfirmDialog(this, "Se ha dectectado que es un nuevo cliente.\n¿Usted desea indicar quién lo recomendó?");
                            if (JOptionPane.OK_OPTION != confirmado) {
                                System.exit(0);
                            }else{
                                PEDIR_HUELLA=true;
                            }
                                
                        }else{
                            System.exit(0);
                        }
                    }*/
                    System.exit(0);
                    this.activarDesactivar();
                    break;
            }

            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            this.setLocationRelativeTo(null);
            this.setAlwaysOnTop(true);
            this.setIconImage(new ImageIcon(getClass().getResource("resources/icono.png")).getImage());
            //oFactura.procesar(this);

        } catch (Exception ex) {
            Util.guardarError(ex, true);
        }
    }

    public DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }
    }

    public void procesarCaptura(DPFPSample sample) {
        // Procesar la muestra de la huella y crear un conjunto de características con el propósito de verificacion.
        featuresverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        // Comprobar la calidad de la muestra de la huella y lo añade a su reclutador si es bueno
        if (featuresverificacion != null) {
            try {

                //System.out.println("Las Caracteristicas de la Huella han sido creada");
                //Reclutador.addFeatures(featuresinscripcion);// Agregar las caracteristicas de la huella a la plantilla a crear
                // Dibuja la huella dactilar capturada.
                Image image = crearImagenHuella(sample);
                dibujarHuella(image);

            } catch (Exception ex) {
                System.err.println("Error: " + ex.getMessage());
            } finally {

            }
        }
    }

    public void dibujarHuella(Image image) {
        etiquetaHuella.setIcon(new ImageIcon(
                image.getScaledInstance(etiquetaHuella.getWidth(), etiquetaHuella.getHeight(), Image.SCALE_DEFAULT)));
        repaint();
    }

    public Image crearImagenHuella(DPFPSample sample) {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }

    protected void iniciarDispositivo() {
        Lector.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(final DPFPDataEvent e) {
                SwingUtilities.invokeLater(() -> {
                    enviarTexto(bundle.getString("Mensaje_01"));
                    procesarCaptura(e.getSample());
                });
            }
        });

        Lector.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            @Override
            public void readerConnected(final DPFPReaderStatusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    enviarTexto(bundle.getString("Mensaje_02"));
                });
            }

            @Override
            public void readerDisconnected(final DPFPReaderStatusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    enviarTexto(bundle.getString("Mensaje_03"));
                });
            }
        });

        Lector.addSensorListener(new DPFPSensorAdapter() {
            @Override
            public void fingerTouched(final DPFPSensorEvent e) {
                SwingUtilities.invokeLater(() -> {
                    enviarTexto(bundle.getString("Mensaje_04"));
                });
            }

            @Override
            public void fingerGone(final DPFPSensorEvent e) {
                SwingUtilities.invokeLater(() -> {
                    enviarTexto(bundle.getString("Mensaje_05"));
                    try {
                        stop();
                        if (oFactura.getCodArticulo() == ARTICULO_INVITACION) {
                            identificarHuella();
                        } else {
                            identificarHuella2();
                        }
                        start();

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });

        Lector.addErrorListener(new DPFPErrorAdapter() {
            public void errorReader(final DPFPErrorEvent e) {
                SwingUtilities.invokeLater(() -> {
                    enviarTexto("Error: " + e.getError());
                });
            }
        });
    }

    public void enviarTexto(String string) {
        etiquetaStatus.setText(string);
    }

    private void activarDesactivar() {
        if (oFactura.getCodArticulo() == ARTICULO_INVITACION || PEDIR_HUELLA) {
            etiquetaInvitado.setEnabled(true);
            etiquetaFoto.setEnabled(true);
            etiquetaHuella.setEnabled(true);
            etiquetaSQLNombre.setEnabled(true);
            etiquetaFecha.setEnabled(false);
            comboFecha.setEnabled(false);
            botonAceptar.setEnabled(false);
            iniciarDispositivo();
            start();
        } else {
            etiquetaInvitado.setEnabled(false);
            etiquetaFoto.setEnabled(false);
            etiquetaHuella.setEnabled(false);
            etiquetaSQLNombre.setEnabled(false);
            etiquetaFecha.setEnabled(true);
            comboFecha.setEnabled(true);
            botonAceptar.setEnabled(true);

        }
    }

    public final void evaluar() {
        //VERIFICAMOS QUE TENGA ASOCIADO UN CLIENTE
        boolean bError = false;
        if (oFactura.getCodCliente() < 1) {
            JOptionPane.showMessageDialog(this, "Se debe asociar un cliente para continuar con la facturación", "Error", JOptionPane.ERROR_MESSAGE);
            bError = true;
        }

        //verificar que si tiene entradas disponibles entonces no pueda comprar membresias.
        if (!bError && oFactura.isActivoEntrada() && oFactura.getDias() > 0) {
            JOptionPane.showMessageDialog(this, "Este cliente tiene " + oFactura.getDias() + " entradas disponibles", "Error", JOptionPane.ERROR_MESSAGE);
            bError = true;
        }

        //VALIDAMOS QUE SI DESEA CONGELAR UNA MEMBRESIA DISPONGA DE AL MENOS DISPONGA DE 10 DIAS
        if (!bError && oFactura.getCodArticulo() == ARTICULO_FREEZE && oFactura.getDias() < DIAS_PARA_PODER_CONGELAR && !(oFactura.getCodArticulo() == ARTICULO_ENTRADAS || oFactura.getCodArticulo() == ARTICULO_SPINNING)) {
            JOptionPane.showMessageDialog(this, "El cliente debe tener por lo menos " + DIAS_PARA_PODER_CONGELAR + " días disponibles para poder pausar sus días restantes.", "Error", JOptionPane.ERROR_MESSAGE);
            bError = true;
        }

        //VALIDAMOS QUE SI ES UNA MEMBRESIA SENIOR, EL CLIENTE CUMPLA CON LA EDAD MINIMA REQUERIDA
        /*if(!bError && oFactura.getCodArticulo() == ARTICULO_SENIOR && (oFactura.getEdad()<EDAD_MINIMA_SENIOR || oFactura.getEdad()>100)){
            JOptionPane.showMessageDialog(this, "The minimum age for this type of membership is "+EDAD_MINIMA_SENIOR+" years", "Message "+oFactura.getEdad(), JOptionPane.ERROR_MESSAGE);
            bError=true;
        }
        
        //VALIDAMOS QUE SI ES UNA PERSONA MAYOR ENTONCES LE VALIDEN SI ES SENIOR LA MEMBESIA
        if(!bError && oFactura.getCodArticulo() != ARTICULO_SENIOR && oFactura.getDuracion()>15 && (oFactura.getEdad()>=EDAD_MINIMA_SENIOR && oFactura.getEdad()<=100)){
            JOptionPane.showMessageDialog(this, "Must select senior membership. "+EDAD_MINIMA_SENIOR+" years", "Message "+oFactura.getEdad(), JOptionPane.ERROR_MESSAGE);
            bError=true;
        }*/
        //verificamos que pueda facturar el producto de entrenador
        if (!bError && oFactura.getCodArticulo() == ARTICULO_ENTRENADOR && oFactura.getTipoCliente() != CLIENTE_ENTRENADOR) {
            JOptionPane.showMessageDialog(this, "Este tipo de pase está restringido solo para Entrenadores", "Error", JOptionPane.ERROR_MESSAGE);
            bError = true;
        }

        //verificamos que pueda facturar el producto de influencer
        /*if(!bError && oFactura.getCodArticulo() == ARTICULO_INFLUENCER && oFactura.getTipoCliente()!=CLIENTE_INFLUENCER){
            JOptionPane.showMessageDialog(this, "This type of membership is restricted to influencers only.", "Message", JOptionPane.ERROR_MESSAGE);
            bError=true;
        }*/
        //verificamos que si es un entrenador solo pueda facturar dicho producto
        if (!bError && oFactura.getCodArticulo() != ARTICULO_ENTRENADOR && oFactura.getTipoCliente() == CLIENTE_ENTRENADOR) {
            JOptionPane.showMessageDialog(this, "Tipo de pase no válido para entrenadores", "Message", JOptionPane.ERROR_MESSAGE);
            bError = true;
        }

        //verificamos que si es un influencer solo pueda facturar dicho producto
        /*if(!bError && oFactura.getCodArticulo() != ARTICULO_INFLUENCER && oFactura.getTipoCliente()==CLIENTE_INFLUENCER ){
            JOptionPane.showMessageDialog(this, "Membership is not valid for the type of customer.", "Message", JOptionPane.ERROR_MESSAGE);
            bError=true;
        }*/
 /* if ((oFactura.getLineas() != 1 || oFactura.getUnidades() != Math.abs(1))&&oFactura.getMinimo()==1) {
            JOptionPane.showMessageDialog(this, "Remember that only one membership should be billed.", "Message", JOptionPane.ERROR_MESSAGE);
            bError=true;
        }*/
        //VERIFICAMOS QUE SI ES UNA INVITACION NO TENGA UNA EN UN PERIODO MENOS A 90 DIAS
        /*if (!bError && oFactura.getCodArticulo() == ARTICULO_INVITACION && oFactura.getUltInvitacion() < DIAS_INVITACION) {
            JOptionPane.showMessageDialog(this, "This customer has already been invited in the last " + DIAS_INVITACION + " days.\nCAN NOT BE INVITED AGAIN", "Message", JOptionPane.ERROR_MESSAGE);
            JOptionPane.showMessageDialog(this, "He was invited on " + oFactura.getFechaInv() + " by "+oFactura.getNombre()+".\nCAN NOT BE INVITED AGAIN", "Message", JOptionPane.ERROR_MESSAGE);
            bError=true;
        }*/
        //DIAS FALTANTES PARA PODER RENOVAR
        if (!bError && oFactura.getDias() > DIAS_RENOVACION && oFactura.getCodArticulo() != ARTICULO_FREEZE && oFactura.getCodArticulo() != ARTICULO_PROMO && oFactura.getCodArticulo() != ARTICULO_ENTRADAS && oFactura.getCodArticulo() != ARTICULO_SPINNING) {
            JOptionPane.showMessageDialog(this, "El cliente tiene " + oFactura.getDias() + " días disponibles. Imposible Renovar", "Mensaje", JOptionPane.ERROR_MESSAGE);
            bError = true;
            //AQUI PUEDO HACER UNA PREGUNTA

        }

        //SI ES UN ARTICULO POR ENTRADAS ENTONCES VALIDAR QUE ESTE INACTIVO
        if (!bError && (oFactura.getCodArticulo() == ARTICULO_ENTRADAS || oFactura.getCodArticulo() == ARTICULO_SPINNING) && oFactura.getDias() > 0) {
            JOptionPane.showMessageDialog(this, "Para poder comprar este tipo de pases el cliente se debe encontrar inactivo.", "Error", JOptionPane.ERROR_MESSAGE);
            bError = true;
        }

        //VERIFICAR QUE SOLO TENGA UNA MEMBRESIA
        if (!bError && (oFactura.getCodArticulo() == ARTICULO_ENTRADAS || oFactura.getCodArticulo() == ARTICULO_SPINNING) && oFactura.getUnidades() > 1) {
            JOptionPane.showMessageDialog(this, "El máximo de pases de este tipo por factura es de 1.", "Message", JOptionPane.ERROR_MESSAGE);
            bError = true;
        }

        //verificar que la membresia de entradas no se venda junto con otra membresia
        if (!bError && oFactura.isEntrada() && oFactura.getLineas() > 1) {
            JOptionPane.showMessageDialog(this, "Tipos de pases incompatibles", "Error", JOptionPane.ERROR_MESSAGE);
            bError = true;
        }

        //VALIDAMOS QUE NO PUEDA REGALAR 5 DIAS SI NO APLICA. ESTO ES POR QUE SE PASAN DE GALLOS
        /*if(!bError && oFactura.getCodArticulo() == ARTICULO_PROMO && oFactura.getPromoPremio().equalsIgnoreCase("F")){
            JOptionPane.showMessageDialog(this, "Promo is not valid.", "Message", JOptionPane.ERROR_MESSAGE);
            bError=true;
        }*/
        //VERIFICAMOS QUE LA CANTIDAD DE MIEMBROS SEA LA MINIMA PARA EL TIPO DE MEMBRESIA
        if (!bError && Math.abs(oFactura.getUnidades()) < oFactura.getMinimo()) {
            JOptionPane.showMessageDialog(this, "Este tipo de plan requiere un mínimo de " + oFactura.getMinimo() + " personas", "Error", JOptionPane.ERROR_MESSAGE);
            bError = true;
            //AQUI PUEDO HACER UNA PREGUNTA

        }

        if (bError) {
            actualizarXML();
        }

    }

    private void actualizarXML() {
        XML oXML = new XML();
        oXML.modificarXML(Factura.XML, "correcta", "0");
        System.exit(0);
    }

    public void start() {
        Lector.startCapture();
        enviarTexto(bundle.getString("Mensaje_06"));
    }

    public void stop() {
        Lector.stopCapture();
        enviarTexto(bundle.getString("Mensaje_07"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        comboFecha = new datechooser.beans.DateChooserCombo();
        etiquetaFecha = new javax.swing.JLabel();
        etiquetaInvitado = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        etiquetaHuella = new javax.swing.JLabel();
        etiquetaStatus = new javax.swing.JLabel();
        etiquetaSQLNombre = new javax.swing.JLabel();
        etiquetaFoto = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Palestra Medical Gym, C.A.");
        setAlwaysOnTop(true);
        getContentPane().setLayout(new java.awt.CardLayout());

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 16), new java.awt.Color(255, 255, 255))); // NOI18N

        comboFecha.setCurrentView(new datechooser.view.appearance.AppearancesList("Swing",
            new datechooser.view.appearance.ViewAppearance("custom",
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    true,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15),
                    new java.awt.Color(0, 0, 255),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15),
                    new java.awt.Color(128, 128, 128),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.LabelPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.LabelPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(255, 0, 0),
                    false,
                    false,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                (datechooser.view.BackRenderer)null,
                false,
                true)));
    comboFecha.setCalendarPreferredSize(new java.awt.Dimension(700, 540));
    comboFecha.setNothingAllowed(false);
    comboFecha.setWeekStyle(datechooser.view.WeekDaysStyle.FULL);
    comboFecha.setFieldFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 24));
    comboFecha.setLocale(new java.util.Locale("en", "GB", ""));
    comboFecha.setNavigateFont(new java.awt.Font("Serif", java.awt.Font.PLAIN, 16));
    comboFecha.setBehavior(datechooser.model.multiple.MultyModelBehavior.SELECT_SINGLE);

    etiquetaFecha.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
    etiquetaFecha.setForeground(new java.awt.Color(255, 255, 255));
    etiquetaFecha.setText("Fecha de Inicio:");

    etiquetaInvitado.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
    etiquetaInvitado.setForeground(new java.awt.Color(255, 255, 255));
    etiquetaInvitado.setText("Por favor indiquele a la persona que realizó la invitación colocar su huella.");
    etiquetaInvitado.setEnabled(false);

    botonAceptar.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
    botonAceptar.setText("ACEPTAR");
    botonAceptar.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonAceptarActionPerformed(evt);
        }
    });

    etiquetaHuella.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    etiquetaHuella.setEnabled(false);
    etiquetaHuella.setOpaque(true);

    etiquetaStatus.setForeground(new java.awt.Color(51, 102, 255));

    etiquetaSQLNombre.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
    etiquetaSQLNombre.setForeground(new java.awt.Color(255, 255, 255));
    etiquetaSQLNombre.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    etiquetaSQLNombre.setText("jLabel1");
    etiquetaSQLNombre.setEnabled(false);

    etiquetaFoto.setBackground(new java.awt.Color(204, 204, 204));
    etiquetaFoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    etiquetaFoto.setEnabled(false);
    etiquetaFoto.setOpaque(true);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(etiquetaFecha)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(comboFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addComponent(etiquetaFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(etiquetaSQLNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(etiquetaHuella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(etiquetaInvitado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botonAceptar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(209, Short.MAX_VALUE))
        .addComponent(etiquetaStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(18, 18, 18)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(etiquetaFecha)
                .addComponent(comboFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addComponent(etiquetaInvitado)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(etiquetaSQLNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                        .addComponent(etiquetaHuella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(12, 12, 12)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
                    .addComponent(etiquetaStatus))
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(etiquetaFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))))
    );

    getContentPane().add(jPanel1, "card2");

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarActionPerformed
        // TODO add your handling code here:
        this.aceptar();
        //System.exit(0);
    }//GEN-LAST:event_botonAceptarActionPerformed
    private void aceptar() {
        //aqui toca evaluar que no se una membresia grupal
        DateTimeInt fecha = new DateTimeInt(comboFecha.getCurrent().getTime());
        try {
            if (oFactura.getMinimo() == 1) {

                System.out.println(fecha.toStringDate());

                if (oFactura.getCodArticulo() != ARTICULO_FREEZE) {
                    oFactura.actualizarFechaInicio(this, fecha.toStringDate());
                } else {
                    oFactura.actualizarFechaInicioCongelado(this, fecha.toStringDate());
                }

                if (oFactura.getCodArticulo() == ARTICULO_INVITACION) {
                    Cliente oCliente = new Cliente(this);
                    oCliente.actualizarInvitacionesDiposnibles(oFactura.getInvitadoPor());
                }
            } else {
                System.out.println("SE DEBEN ASOCIAR LAS PERSONAS");
                abrirDialogoClientes();
                oFactura.actualizarFechaInicioGrupal(this, fecha.toStringDate());
            }
            System.exit(0);
        } catch (SQLException ex) {
            Util.guardarError(ex, true, this);
        }
    }

    private void abrirDialogoClientes() {

        try {

            if (dialogoClientes == null) {

                dialogoClientes = new DialogoClientes(this, true, oFactura);
                dialogoClientes.setLocationRelativeTo(this);
            }
            dialogoClientes.setVisible(true);
            dialogoClientes = null;

        } catch (Exception ex) {
            Util.guardarError(ex, true, this);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            ACCION = args[0];
        } catch (Exception ex) {

        }
        //ACCION="FINISH";

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    public DPFPTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DPFPTemplate template) {
        DPFPTemplate old = this.template;
        this.template = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }

    //Este es para el control de invitaciones
    public void identificarHuella() throws IOException {
        Connection dbConnLocal = null;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        conexionbd.Conector con = new Conector();
        try {

            Datos.cargarConfig();
            //Establece los valores para la sentencia SQL
            dbConnLocal = con.getConexion();
            String sSQL = "SELECT\n"
                    + "	A.*\n"
                    + "	,CCL.DIASRESTANTES \n"
                    + "	, CCL.INVITACIONES\n"
                    + "FROM \n"
                    + "	RIP_GET_HUELLAS A \n"
                    + "	INNER JOIN CLIENTESCAMPOSLIBRES CCL ON A.CODIGO=CCL.CODCLIENTE\n"
                    + "	INNER JOIN RIP_V_MEMBRESIAS_CLIENTES MC ON CCL.CODCLIENTE=MC.CODCLIENTE AND MC.ORDEN=1\n"
                    + "WHERE\n"
                    + "	CCL.DIASRESTANTES>0\n"
                    + "	AND CCL.INVITACIONES>0\n"
                    + "	AND A.TIPO=1\n"
                    + "UNION ALL\n"
                    + "SELECT\n"
                    + "	A.*\n"
                    + "	, 1 DIAS\n"
                    + "	, ISNULL(CCL.NUMCUENTA,0) INVITACIONES\n"
                    + "FROM \n"
                    + "	RIP_GET_HUELLAS A \n"
                    + "	INNER JOIN VENDEDORES CCL ON A.CODIGO=CCL.CODVENDEDOR\n"
                    + "\n"
                    + "WHERE\n"
                    + "	ISNULL(NUMCUENTA,0)>0\n"
                    + "	AND A.TIPO=2\n"
                    + "	AND CCL.DESCATALOGADO='F'";
            pSQL = dbConnLocal.prepareStatement(sSQL);

            rs = pSQL.executeQuery();

            //Si se encuentra el nombre en la base de datos
            while (rs.next()) {
                //Lee la plantilla de la base de datos
                byte templateBuffer[] = rs.getBytes("HUELLAX64");
                //String nombre = rs.getString("CODVENDEDOR");
                //Crea una nueva plantilla a partir de la guardada en la base de datos
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                //Envia la plantilla creada al objeto contendor de Template del componente de huella digital
                setTemplate(referenceTemplate);

                // Compara las caracteriticas de la huella recientemente capturda con la
                // alguna plantilla guardada en la base de datos que coincide con ese tipo
                DPFPVerificationResult result = verificador.verify(featuresverificacion, getTemplate());

                //compara las plantilas (actual vs bd)
                //Si encuentra correspondencia dibuja el mapa
                //e indica el nombre de la persona que coincidió.
                if (result.isVerified()) {
                    //crea la imagen de los datos guardado de las huellas guardadas en la base de datos
                    System.out.println("Codigo " + rs.getInt("CODIGO"));
                    //etiquetaFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/controlacceso/resources/silueta.jpg")));
                    switch (rs.getInt("TIPO")) {
                        case SOCIO:
                            if (rs.getInt("INVITACIONES") > 0) {
                                Cliente oCliente = new Cliente(this);
                                etiquetaSQLNombre.setText(bundle.getString("etiquetaEstado_03"));
                                //JOptionPane.showMessageDialog(this, bundle.getString("etiquetaEstado_03"), "Message", JOptionPane.ERROR_MESSAGE);
                                oCliente.cargarDatos(rs.getInt("CODIGO"), this);
                                oCliente.setCodCliente(rs.getInt("CODIGO") * -1);
                                oCliente.actualizarFechaInvitacion(this, oFactura.getCodCliente());

                                try {
                                    con = null;
                                    rs = null;
                                } catch (Exception x) {

                                }
                                System.exit(0);
                            } else {
                                JOptionPane.showMessageDialog(this, "The user does not have invitations available.", "Message", JOptionPane.ERROR_MESSAGE);
                            }

                            break;
                        case MIEMBRO:
                            if (rs.getInt("DIASRESTANTES") > 0) {
                                if (rs.getInt("INVITACIONES") > 0) {
                                    Cliente oCliente = new Cliente(this);
                                    etiquetaSQLNombre.setText(bundle.getString("etiquetaEstado_03"));
                                    //JOptionPane.showMessageDialog(this, bundle.getString("etiquetaEstado_03"), "Message", JOptionPane.ERROR_MESSAGE);
                                    oCliente.cargarDatos(rs.getInt("CODIGO"), this);
                                    oCliente.setCodCliente(rs.getInt("CODIGO"));
                                    oCliente.actualizarFechaInvitacion(this, oFactura.getCodCliente());

                                    try {
                                        con = null;
                                        rs = null;
                                    } catch (Exception x) {

                                    }
                                    System.exit(0);
                                } else {
                                    JOptionPane.showMessageDialog(this, "The client does not have invitations available.", "Message", JOptionPane.ERROR_MESSAGE);
                                }
                                //etiquetaSQLTipo.setText(bundle.getString("etiquetaSQLTipoUsuario_02"));
                                //this.Aceptar();
                            } else {
                                JOptionPane.showMessageDialog(this, "The client is inactive.", "Message", JOptionPane.ERROR_MESSAGE);
                            }
                            break;
                        default:

                            break;
                    }
                    etiquetaStatus.setText(bundle.getString("etiquetaEstado_02"));
                    this.repaint();
                    //JOptionPane.showMessageDialog(null, "Las huella capturada es de " + nombre, "Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
                    //return;
                }/*else{
                    //etiquetaSQLTipo.setText("");
                    etiquetaSQLNombre.setText("");
                    //etiquetaFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/controlacceso/resources/silueta.jpg")));
                    
                }*/
            }
            //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje
            etiquetaSQLNombre.setText(bundle.getString("etiquetaEstado_02"));
            JOptionPane.showMessageDialog(this, bundle.getString("etiquetaEstado_02"), "Message", JOptionPane.ERROR_MESSAGE);
            setTemplate(null);
            this.actualizarXML();
        } catch (IllegalArgumentException | SQLException e) {
            //Si ocurre un error lo indica en la consola
            etiquetaStatus.setText(bundle.getString("Mensaje_08"));
            System.err.println("Error al identificar huella dactilar." + e.getMessage());
        } finally {
            etiquetaHuella.setIcon(null);
            //System.err.println("Cerrando Conexiones.");
            con = null;
            pSQL = null;
            rs = null;
            //System.err.println("Conexiones Cerradas.");
        }

    }

    //Este es para el control de invitaciones(recomendaciones) para las promociones
    public void identificarHuella2() throws IOException {
        Connection dbConnLocal = null;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        conexionbd.Conector con = new Conector();
        try {

            Datos.cargarConfig();
            //Establece los valores para la sentencia SQL
            dbConnLocal = con.getConexion();
            String sSQL = "SELECT\n"
                    + "	A.*\n"
                    + "FROM \n"
                    + "	RIP_GET_HUELLAS A \n"
                    + "	INNER JOIN CLIENTESCAMPOSLIBRES CCL ON A.CODIGO=CCL.CODCLIENTE\n"
                    + "	INNER JOIN RIP_V_MEMBRESIAS_CLIENTES MC ON CCL.CODCLIENTE=MC.CODCLIENTE AND MC.ORDEN=1\n"
                    + "WHERE\n"
                    + "	A.TIPO=1\n"
                    + "	AND MC.CODARTICULO IN (SELECT CODARTICULO FROM VIEWGRUPO_3)";

            pSQL = dbConnLocal.prepareStatement(sSQL);

            rs = pSQL.executeQuery();

            //Si se encuentra el nombre en la base de datos
            while (rs.next()) {
                //Lee la plantilla de la base de datos
                byte templateBuffer[] = rs.getBytes("HUELLAX64");
                //String nombre = rs.getString("CODVENDEDOR");
                //Crea una nueva plantilla a partir de la guardada en la base de datos
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                //Envia la plantilla creada al objeto contendor de Template del componente de huella digital
                setTemplate(referenceTemplate);

                // Compara las caracteriticas de la huella recientemente capturda con la
                // alguna plantilla guardada en la base de datos que coincide con ese tipo
                DPFPVerificationResult result = verificador.verify(featuresverificacion, getTemplate());

                //compara las plantilas (actual vs bd)
                //Si encuentra correspondencia dibuja el mapa
                //e indica el nombre de la persona que coincidió.
                if (result.isVerified()) {
                    //crea la imagen de los datos guardado de las huellas guardadas en la base de datos
                    System.out.println("Codigo " + rs.getInt("CODIGO"));
                    //etiquetaFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/controlacceso/resources/silueta.jpg")));
                    switch (rs.getInt("TIPO")) {

                        case MIEMBRO:
                            if (rs.getInt("DIASRESTANTES") > 0) {
                                Cliente oCliente = new Cliente(this);
                                etiquetaSQLNombre.setText(bundle.getString("etiquetaEstado_03"));
                                //JOptionPane.showMessageDialog(this, bundle.getString("etiquetaEstado_03"), "Message", JOptionPane.ERROR_MESSAGE);
                                oCliente.cargarDatos(rs.getInt("CODIGO"), this);
                                oCliente.setCodCliente(rs.getInt("CODIGO"));
                                oCliente.actualizarFechaRecomendado(this, oFactura.getCodCliente());

                                try {
                                    con = null;
                                    rs = null;
                                } catch (Exception x) {

                                }
                                System.exit(0);
                                //etiquetaSQLTipo.setText(bundle.getString("etiquetaSQLTipoUsuario_02"));
                                //this.Aceptar();
                            } else {
                                JOptionPane.showMessageDialog(this, "The client is inactive.", "Message", JOptionPane.ERROR_MESSAGE);
                            }
                            break;
                        default:

                            break;
                    }
                    etiquetaStatus.setText(bundle.getString("etiquetaEstado_02"));
                    this.repaint();
                    //JOptionPane.showMessageDialog(null, "Las huella capturada es de " + nombre, "Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
                    //return;
                }/*else{
                    //etiquetaSQLTipo.setText("");
                    etiquetaSQLNombre.setText("");
                    //etiquetaFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/controlacceso/resources/silueta.jpg")));
                    
                }*/
            }
            //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje
            etiquetaSQLNombre.setText(bundle.getString("etiquetaEstado_02"));
            JOptionPane.showMessageDialog(this, bundle.getString("etiquetaEstado_02"), "Message", JOptionPane.ERROR_MESSAGE);
            setTemplate(null);
            this.actualizarXML();
        } catch (IllegalArgumentException | SQLException e) {
            //Si ocurre un error lo indica en la consola
            etiquetaStatus.setText(bundle.getString("Mensaje_08"));
            System.err.println("Error al identificar huella dactilar." + e.getMessage());
        } finally {
            etiquetaHuella.setIcon(null);
            //System.err.println("Cerrando Conexiones.");
            con = null;
            pSQL = null;
            rs = null;
            //System.err.println("Conexiones Cerradas.");
        }

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAceptar;
    private datechooser.beans.DateChooserCombo comboFecha;
    private javax.swing.JLabel etiquetaFecha;
    public javax.swing.JLabel etiquetaFoto;
    private javax.swing.JLabel etiquetaHuella;
    private javax.swing.JLabel etiquetaInvitado;
    public javax.swing.JLabel etiquetaSQLNombre;
    private javax.swing.JLabel etiquetaStatus;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    Factura oFactura;
    public static String ACCION = "";
    private final DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    public DPFPFeatureSet featuresverificacion;
    java.util.ResourceBundle bundle;
    private DPFPTemplate template;

    private final static int SOCIO = 2;
    private final static int MIEMBRO = 1;
    public static String TEMPLATE_PROPERTY = "template";
    private final DPFPVerification verificador = DPFPGlobal.getVerificationFactory().createVerification();

    DialogoClientes dialogoClientes;
}
