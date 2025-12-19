/*
 * MainFrame.java - Con detalles de factura y cliente
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
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import conexionbd.Datos;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import shared.util.DateTimeInt;

public class MainFrame extends javax.swing.JFrame {

    // --- CONSTANTES ---
    public static final int ARTICULO_ENTRADAS = 10;
    public static final int ARTICULO_INVITACION = 0; 

    // Huellas
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    // Lógica
    Factura oFactura;
    public static String ACCION = "";
    java.util.ResourceBundle bundle;
    DialogoClientes dialogoClientes;
    private File archivoFotoSeleccionada = null;

    public MainFrame() {
        bundle = java.util.ResourceBundle.getBundle("idiomas/espanol");
        
        // Inicializar componentes
        initComponents(); 

        this.etiquetaSQLNombre.setText("");
        
        try {
            Datos.cargarConfig(); 
            try {
                this.setIconImage(new ImageIcon(getClass().getResource("resources/icono.png")).getImage());
            } catch (Exception e) { }

            Factura.cargarXML(this);
            Datos.BD = Factura.BDGESTION;
            oFactura = new Factura();
            oFactura.cargarDatosFactura(this);
            
            // --- MOSTRAR DATOS DE FACTURA EN INTERFAZ ---
            String serieNum = oFactura.getSerie() + " - " + oFactura.getNumero();
            String producto = oFactura.getNombreProducto() != null ? oFactura.getNombreProducto() : "Membresía General";
            
            etiquetaFactura.setText(serieNum);
            etiquetaProducto.setText(producto);
            // --------------------------------------------

            // Configurar Cliente
            if (oFactura.getCodCliente() > 0) {
                 Cliente oCliente = new Cliente(this);
                 oCliente.cargarDatos(oFactura.getCodCliente(), this);
                 etiquetaSQLNombre.setText(oCliente.getNombre() != null ? oCliente.getNombre() : "Cliente Desconocido");
                 
                 // Cargar foto si existe
                 try {
                     ImageIcon fotoActual = oCliente.getFoto(this, etiquetaFoto.getWidth(), etiquetaFoto.getHeight());
                     if (fotoActual != null) {
                         etiquetaFoto.setIcon(fotoActual);
                     }
                 } catch(Exception e) {}
            }

            Calendar fecha = Calendar.getInstance();
            this.comboFecha.setSelectedDate(fecha);
            
            this.evaluar();

            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            this.setLocationRelativeTo(null);
            this.setAlwaysOnTop(true);
            
            iniciarDispositivo();
            start();

        } catch (Exception ex) {
            Util.guardarError(ex, true);
        }
    }

    public final void evaluar() {
        if (oFactura.getCodCliente() < 1) {
            JOptionPane.showMessageDialog(this, "Esta factura no tiene un cliente asociado.", "Error de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aceptar() {
        SimpleDateFormat sdfVisual = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfSQL = new SimpleDateFormat("yyyyMMdd"); 
        
        Date fechaInicioDate = comboFecha.getCurrent().getTime();
        int diasComprados = oFactura.getDuracion(); 
        
        // Calcular fecha fin
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaInicioDate);

        // --- LÓGICA DE MES CALENDARIO ---
        if (diasComprados == 30) {
            // Si el plan es de 30 días, sumamos 1 MES real (ej: 19 Dic -> 19 Ene)
            cal.add(Calendar.MONTH, 1);
        } else if (diasComprados == 365 || diasComprados == 360) {
             // Opcional: Si es anual, sumamos 1 AÑO (ej: 19 Dic 2025 -> 19 Dic 2026)
             cal.add(Calendar.YEAR, 1);
        } else {
            // Para cualquier otro caso (1 día, 7 días, etc.), sumamos los días exactos
            cal.add(Calendar.DAY_OF_YEAR, diasComprados);
        }
        // --------------------------------
        
        Date fechaFinDate = cal.getTime();

        // Crear Strings
        String fechaInicioStr = sdfVisual.format(fechaInicioDate);
        String fechaFinStr = sdfVisual.format(fechaFinDate);
        
        String fechaInicioBD = sdfSQL.format(fechaInicioDate);
        String fechaFinBD = sdfSQL.format(fechaFinDate);

        // Mensaje de confirmación previo
        String mensaje = String.format("<html><body style='width: 300px;'>" +
                "<h2>Resumen de Activación</h2>" +
                "<p><b>Cliente:</b> %s</p>" +
                "<p><b>Plan:</b> %s</p>" +
                "<hr>" +
                "<p><b>Inicio:</b> %s</p>" +
                "<p><b>Fin:</b> %s (Vence el mismo día)</p>" +
                "<br><p>¿Desea activar la membresía?</p></body></html>", 
                etiquetaSQLNombre.getText(),
                etiquetaProducto.getText(),
                fechaInicioStr, 
                fechaFinStr);

        int confirm = JOptionPane.showConfirmDialog(this, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 1. Guardar Huella
                if (getTemplate() != null) {
                    Cliente oCliente = new Cliente(this);
                    oCliente.setCodCliente(oFactura.getCodCliente());
                    oCliente.guardarHuella(this, getTemplate()); 
                }
                
                // 2. Activar Membresía (Actualizar BD)
                if (oFactura.getMinimo() == 1) {
                    oFactura.actualizarFechaInicio(this, fechaInicioBD, fechaFinBD);
                } else {
                    abrirDialogoClientes();
                    oFactura.actualizarFechaInicioGrupal(this, fechaInicioBD, fechaFinBD);
                }
                
                // Mensaje de éxito
                JOptionPane.showMessageDialog(this, 
                        "¡Proceso Exitoso!\n\nLa membresía ha sido activada y los datos actualizados.", 
                        "Operación Completada", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                actualizarXML(); // Cierra la aplicación
                
            } catch (SQLException ex) {
                Util.guardarError(ex, true, this);
            }
        }
    }
    
    private void seleccionarFoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Foto del Cliente");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "png", "jpeg"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            archivoFotoSeleccionada = fileChooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(archivoFotoSeleccionada);
                Image dimg = img.getScaledInstance(etiquetaFoto.getWidth(), etiquetaFoto.getHeight(), Image.SCALE_SMOOTH);
                etiquetaFoto.setIcon(new ImageIcon(dimg));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen: " + e.getMessage());
            }
        }
    }

    // --- HUELLAS ---
    protected void iniciarDispositivo() {
        Lector.addDataListener((DPFPDataEvent e) -> {
            SwingUtilities.invokeLater(() -> {
                enviarTexto("Huella capturada. Procesando...");
                procesarCaptura(e.getSample());
            });
        });
        Lector.addReaderStatusListener(new DPFPReaderStatusAdapter() {});
        Lector.addErrorListener(new DPFPErrorAdapter() {});
    }

    public void procesarCaptura(DPFPSample sample) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            DPFPFeatureSet features = extractor.createFeatureSet(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
            if (features != null) {
                try {
                    Reclutador.addFeatures(features); 
                    Image image = DPFPGlobal.getSampleConversionFactory().createImage(sample);
                    dibujarHuella(image);

                    switch(Reclutador.getTemplateStatus()){
                        case TEMPLATE_STATUS_READY:
                            stop();
                            setTemplate(Reclutador.getTemplate());
                            enviarTexto("Huella lista. Puede continuar.");
                            Reclutador.clear(); 
                            start();
                            break;
                        case TEMPLATE_STATUS_FAILED:
                            Reclutador.clear();
                            stop();
                            enviarTexto("Error. Intente de nuevo.");
                            start();
                            break;
                        case TEMPLATE_STATUS_INSUFFICIENT:
                            enviarTexto("Toque el lector nuevamente.");
                            break;
                    }
                } catch (Exception ex) { enviarTexto("Error: " + ex.getMessage()); }
            }
        } catch (DPFPImageQualityException e) { }
    }

    public void dibujarHuella(Image image) {
        if (image != null) {
            etiquetaHuella.setIcon(new ImageIcon(
                image.getScaledInstance(etiquetaHuella.getWidth(), etiquetaHuella.getHeight(), Image.SCALE_DEFAULT)));
            repaint();
        }
    }

    public void start() { Lector.startCapture(); enviarTexto("Escanee la huella..."); }
    public void stop() { Lector.stopCapture(); }
    public DPFPTemplate getTemplate() { return template; }
    public void setTemplate(DPFPTemplate template) { this.template = template; }
    public void enviarTexto(String string) { etiquetaStatus.setText(string); }
    private void actualizarXML() {
        XML oXML = new XML();
        oXML.modificarXML(Factura.XML, "correcta", "0");
        System.exit(0);
    }
    private void abrirDialogoClientes() {
        try {
            if (dialogoClientes == null) {
                dialogoClientes = new DialogoClientes(this, true, oFactura);
                dialogoClientes.setLocationRelativeTo(this);
            }
            dialogoClientes.setVisible(true);
            dialogoClientes = null;
        } catch (Exception ex) { Util.guardarError(ex, true, this); }
    }

    // --- DISEÑO VISUAL CON DATOS DE FACTURA ---
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        panelDetalles = new javax.swing.JPanel();
        lblTitFactura = new javax.swing.JLabel();
        etiquetaFactura = new javax.swing.JLabel();
        lblTitProducto = new javax.swing.JLabel();
        etiquetaProducto = new javax.swing.JLabel();
        
        comboFecha = new datechooser.beans.DateChooserCombo();
        etiquetaFecha = new javax.swing.JLabel();
        botonAceptar = new javax.swing.JButton();
        botonFoto = new javax.swing.JButton();
        etiquetaHuella = new javax.swing.JLabel();
        etiquetaStatus = new javax.swing.JLabel();
        etiquetaSQLNombre = new javax.swing.JLabel();
        etiquetaFoto = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Control de Acceso - Activación");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel Detalles Factura ---
        panelDetalles.setBackground(new java.awt.Color(245, 245, 245));
        panelDetalles.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Detalles de Compra", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); 

        lblTitFactura.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblTitFactura.setForeground(new java.awt.Color(100, 100, 100));
        lblTitFactura.setText("FACTURA Nº:");

        etiquetaFactura.setFont(new java.awt.Font("Segoe UI", 0, 14));
        etiquetaFactura.setText("----");

        lblTitProducto.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblTitProducto.setForeground(new java.awt.Color(100, 100, 100));
        lblTitProducto.setText("PLAN CONTRATADO:");

        etiquetaProducto.setFont(new java.awt.Font("Segoe UI", 1, 14));
        etiquetaProducto.setForeground(new java.awt.Color(0, 102, 204));
        etiquetaProducto.setText("----");

        // Layout del sub-panel
        javax.swing.GroupLayout panelDetallesLayout = new javax.swing.GroupLayout(panelDetalles);
        panelDetalles.setLayout(panelDetallesLayout);
        panelDetallesLayout.setHorizontalGroup(
            panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetallesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(etiquetaProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelDetallesLayout.createSequentialGroup()
                        .addGroup(panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTitProducto)
                            .addGroup(panelDetallesLayout.createSequentialGroup()
                                .addComponent(lblTitFactura)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(etiquetaFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelDetallesLayout.setVerticalGroup(
            panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDetallesLayout.createSequentialGroup()
                .addGroup(panelDetallesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitFactura)
                    .addComponent(etiquetaFactura))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTitProducto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(etiquetaProducto)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        // --- Resto de componentes ---
        comboFecha.setCalendarPreferredSize(new java.awt.Dimension(350, 250));
        comboFecha.setFieldFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 18));

        etiquetaFecha.setFont(new java.awt.Font("Segoe UI", 1, 18)); 
        etiquetaFecha.setForeground(new java.awt.Color(51, 51, 51));
        etiquetaFecha.setText("Fecha de Inicio:");

        etiquetaSQLNombre.setFont(new java.awt.Font("Segoe UI", 1, 28)); 
        etiquetaSQLNombre.setForeground(new java.awt.Color(51, 51, 51));
        etiquetaSQLNombre.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        etiquetaSQLNombre.setText("Cliente");

        etiquetaStatus.setFont(new java.awt.Font("Segoe UI", 2, 14)); 
        etiquetaStatus.setForeground(new java.awt.Color(102, 102, 102));
        etiquetaStatus.setText("Esperando lector...");

        botonAceptar.setBackground(new java.awt.Color(0, 153, 51));
        botonAceptar.setFont(new java.awt.Font("Segoe UI", 1, 18)); 
        botonAceptar.setForeground(new java.awt.Color(0, 0, 0));
        botonAceptar.setText("ACTIVAR AHORA");
        botonAceptar.addActionListener(evt -> aceptar());

        botonFoto.setText("Cargar Foto");
        botonFoto.addActionListener(evt -> seleccionarFoto());

        etiquetaFoto.setBackground(new java.awt.Color(240, 240, 240));
        etiquetaFoto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        etiquetaFoto.setOpaque(true);
        etiquetaFoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaFoto.setText("Sin Foto");

        etiquetaHuella.setBackground(new java.awt.Color(240, 240, 240));
        etiquetaHuella.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        etiquetaHuella.setOpaque(true);
        etiquetaHuella.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaHuella.setText("Huella");

        // Layout
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(botonFoto, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(etiquetaFoto, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(etiquetaSQLNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelDetalles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(etiquetaFecha)
                            .addComponent(comboFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(etiquetaStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                        .addComponent(etiquetaHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(botonAceptar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(etiquetaSQLNombre)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelDetalles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(etiquetaFecha)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(etiquetaStatus))
                            .addComponent(etiquetaHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(etiquetaFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botonFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botonAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1);
        pack();
    }

    public static void main(String args[]) {
        try { ACCION = args[0]; } catch (Exception ex) {}
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }

    // Variables
    private javax.swing.JButton botonAceptar;
    private javax.swing.JButton botonFoto;
    private datechooser.beans.DateChooserCombo comboFecha;
    private javax.swing.JLabel etiquetaFecha;
    public javax.swing.JLabel etiquetaFoto;
    private javax.swing.JLabel etiquetaHuella;
    public javax.swing.JLabel etiquetaSQLNombre;
    private javax.swing.JLabel etiquetaStatus;
    private javax.swing.JPanel jPanel1;
    // Nuevos componentes de detalle
    private javax.swing.JPanel panelDetalles;
    private javax.swing.JLabel lblTitFactura;
    private javax.swing.JLabel etiquetaFactura;
    private javax.swing.JLabel lblTitProducto;
    private javax.swing.JLabel etiquetaProducto;
}