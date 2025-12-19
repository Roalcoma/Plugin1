/*
 * Factura.java ACTUALIZADO
 */
package plugin1;

import conexionbd.Conector;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class Factura {
    
    public static final String XML = "fact.xml";
    
    protected static String SERIE;
    protected static int NUMERO;
    protected static String N;
    protected static String BDGESTION;
    
    private int codCliente;
    private String serie;
    private int numero;
    private String n;
    private double total;
    private int lineas;
    private int unidades;
    private int dias;
    private int ultInvitacion;
    private int codArticulo;
    private int duracion;
    private String fechaInv;
    private String nombre;     
    private int numImpresiones;
    private int idEstado;
    private int minimo;
    private int tipoCliente;
    private int invitadoPor;
    private int edad;
    private String promoPremio;
    private boolean entrada;
    private boolean activoEntrada;
    
    // --- NUEVO CAMPO AGREGADO ---
    private String nombreProducto; 

    static void cargarXML(java.awt.Component parent) throws Exception {
        File f = new File(XML);
        if (!f.exists()) {
            System.exit(0);
        }
        try {
            try (Scanner s = new Scanner(f)) {
                while (s.hasNextLine()) {
                    String linea = s.nextLine();
                    if (linea.contains("<database>")) {
                        BDGESTION= linea.replace("</database>", "").replace("<database>", "").trim();
                        linea = s.nextLine();
                    }
                    if (linea.contains("<tipodoc>") && !(
                            linea.replace("</tipodoc>", "").replace("<tipodoc>", "").trim().equalsIgnoreCase("FACVENTA")
                          ||linea.replace("</tipodoc>", "").replace("<tipodoc>", "").trim().equalsIgnoreCase("F")
                            ||linea.replace("</tipodoc>", "").replace("<tipodoc>", "").trim().equalsIgnoreCase("TIQUET"))
                            ){
                        s.close();
                        System.exit(0);
                    } else {
                        if (linea.contains("<tipodoc>")) {
                            linea = s.nextLine();
                        }
                    }
                    if (linea.contains("<serie>")) {
                        SERIE = linea.replace("</serie>", "").replace("<serie>", "").trim();
                        linea = s.nextLine();
                    }
                    if (linea.contains("<numero>")) {
                        NUMERO = Integer.valueOf(linea.replace("</numero>", "").replace("<numero>", "").trim());
                        linea = s.nextLine();
                    }
                    if (linea.contains("<n>")) {
                        N = linea.replace("</n>", "").replace("<n>", "").trim();
                        s.close();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Util.guardarError(e, true, parent);
        }
    }
    
    protected int cargarDiasFactura(java.awt.Component parent) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        Conector oConector =new Conector();
        int iReturn=0;
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "DECLARE @SERIE AS NVARCHAR(4)\n" +
                    "DECLARE @NUMERO AS INT\n" +
                    "DECLARE @N AS NVARCHAR(4)\n" +
                    "\n" +
                    "SET @SERIE=?\n" +
                    "SET @NUMERO=?\n" +
                    "SET @N=?\n" +
                    "\n" +
                    "SELECT \n" +
                    " 	 SUM(AVL.UNIDADESTOTAL*ART.DURACION) DURACION\n" +
                    "FROM ALBVENTACAB AVC\n" +
                    "	INNER JOIN ALBVENTALIN AVL ON AVC.NUMSERIE=AVL.NUMSERIE AND AVC.NUMALBARAN=AVL.NUMALBARAN AND AVC.N=AVL.N\n" +
                    "	INNER JOIN FACTURASVENTA FV ON AVC.NUMSERIEFAC=FV.NUMSERIE AND AVC.NUMFAC=FV.NUMFACTURA AND AVC.NFAC=FV.N\n" +
                    "	INNER JOIN CLIENTESCAMPOSLIBRES CCL ON AVC.CODCLIENTE=CCL.CODCLIENTE\n" +
                    "	INNER JOIN ARTICULOSCAMPOSLIBRES ART ON AVL.CODARTICULO=ART.CODARTICULO\n" +
                    "	LEFT JOIN CLIENTES C ON CCL.INVITADOPOR=C.CODCLIENTE \n" +
                    "	LEFT JOIN CLIENTES C2 ON FV.CODCLIENTE=C2.CODCLIENTE\n" +
                    "WHERE \n" +
                    "	AVC.NUMSERIEFAC=@SERIE \n" +
                    "	AND AVC.NUMFAC=@NUMERO \n" +
                    "	AND AVC.NFAC=@N \n" +
                    "	AND AVC.FACTURADO='T' GROUP BY FV.NUMSERIE \n";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            rs = pSQL.executeQuery();

            if (rs.next()) {
                iReturn=rs.getInt("DURACION");
            }
        } catch (SQLException ex) {
            Util.guardarError(ex, true,parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (rs != null) rs.close();
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
            return iReturn;
        }
    }
    
    protected boolean cargarDatosFactura(java.awt.Component parent) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        boolean bReturn = false;
        Conector oConector =new Conector();
        boolean bPrimeraLinea=true;
        try {
            dbConnLocal = oConector.getConexion();
            // SE AGREGO MAX(AVL.DESCRIPCION) PARA OBTENER EL NOMBRE DEL PLAN
            sSQL = "DECLARE @SERIE AS NVARCHAR(4)\n" +
                    "DECLARE @NUMERO AS INT\n" +
                    "DECLARE @N AS NVARCHAR(4)\n" +
                    "\n" +
                    "SET @SERIE=?\n" +
                    "SET @NUMERO=?\n" +
                    "SET @N=?\n" +
                    "\n" +
                    "SELECT \n" +
                    "	AVC.NUMSERIE\n" +
                    "	, AVC.NUMALBARAN\n" +
                    "	, AVC.NFAC N\n" +
                    "	, CCL.CODCLIENTE, CCL.PORENTRADAS, ART.CODARTICULO \n" +
                    "	, ISNULL(CCL.DIASRESTANTES,0) DIASRESTANTES\n" +
                    "	, DATEDIFF(DAY,ISNULL(CCL.ULTINVITACION,'20010101'),GETDATE()) ULTINVITACION\n" +
                    "	, SUM(AVL.UNIDADESTOTAL) UDSTOTAL\n" +
                    "	, COUNT(*) LINEAS\n" +
                    " 	, SUM(AVL.UNIDADESTOTAL)*ART.DURACION DURACION\n" +
                    "	, ART.CODARTICULO\n" +
                    "	, CONVERT(CHAR(10), CCL.ULTINVITACION, 103) FECHA_INV\n" +
                    "	, ISNULL(C.NOMBRECLIENTE,'N/A') INVITADOPOR, ISNULL(MAX(FV.NUMIMPRESIONES),0) NUMIMPRESIONES, ISNULL(MAX(FV.IDESTADO),0) IDESTADO \n" +
                    "	, ISNULL(ART.MINIMO,1) MINIMO\n" +
                    "	, ISNULL(C2.TIPO,0) TIPOCLIENTE, ISNULL(CCL.INVITADOPOR,0) INVITADOPOR2\n" +
                    "   , DATEDIFF(YEAR,ISNULL(MAX(C2.FECHANACIMIENTO),'19000101'),GETDATE()) EDAD \n"+
                    "   , MAX(AVL.DESCRIPCION) AS NOMBREPRODUCTO \n" + // <--- AQUI ESTA LA MAGIA
                    "FROM ALBVENTACAB AVC\n" +
                    "	INNER JOIN ALBVENTALIN AVL ON AVC.NUMSERIE=AVL.NUMSERIE AND AVC.NUMALBARAN=AVL.NUMALBARAN AND AVC.N=AVL.N\n" +
                    "	INNER JOIN FACTURASVENTA FV ON AVC.NUMSERIEFAC=FV.NUMSERIE AND AVC.NUMFAC=FV.NUMFACTURA AND AVC.NFAC=FV.N\n" +
                    "	INNER JOIN CLIENTESCAMPOSLIBRES CCL ON AVC.CODCLIENTE=CCL.CODCLIENTE\n" +
                    "	INNER JOIN ARTICULOSCAMPOSLIBRES ART ON AVL.CODARTICULO=ART.CODARTICULO\n" +
                    "	LEFT JOIN CLIENTES C ON CCL.INVITADOPOR=C.CODCLIENTE \n" +
                    "	LEFT JOIN CLIENTES C2 ON FV.CODCLIENTE=C2.CODCLIENTE\n" +
                    "WHERE \n" +
                    "	AVC.NUMSERIEFAC=@SERIE \n" +
                    "	AND AVC.NUMFAC=@NUMERO \n" +
                    "	AND AVC.NFAC=@N \n" +
                    "	AND AVC.FACTURADO='T'\n" +
                    "GROUP BY\n" +
                    "	AVC.NUMSERIE\n" +
                    "	, AVC.NUMALBARAN\n" +
                    "	, AVC.NFAC\n" +
                    "	, CCL.CODCLIENTE , CCL.PORENTRADAS\n" +
                    "	, ISNULL(CCL.DIASRESTANTES,0) \n" +
                    "	, DATEDIFF(DAY,ISNULL(CCL.ULTINVITACION,'20010101'),GETDATE())\n" +
                    "	, ART.DURACION\n" +
                    "	, ART.CODARTICULO\n" +
                    "	, CCL.ULTINVITACION\n" +
                    "	, C.NOMBRECLIENTE\n" +
                    "	, ART.MINIMO\n" +
                    "	, C2.TIPO, CCL.INVITADOPOR";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            rs = pSQL.executeQuery();

            while (rs.next()) {
                if (rs.getInt("CODARTICULO")==MainFrame.ARTICULO_ENTRADAS){
                     this.setEntrada(true);
                }
            
                if (bPrimeraLinea){
                    this.setCodCliente(rs.getInt("CODCLIENTE"));
                    this.setNumero(rs.getInt("NUMALBARAN"));
                    this.setSerie(rs.getString("NUMSERIE"));
                    this.setN(rs.getString("N"));
                    this.setLineas(rs.getInt("LINEAS"));
                    this.setUnidades(rs.getInt("UDSTOTAL"));
                    this.setCodArticulo(rs.getInt("CODARTICULO"));
                    this.setDuracion(this.cargarDiasFactura(parent));
                    this.setDias(rs.getInt("DIASRESTANTES"));
                    this.setFechaInv(rs.getString("FECHA_INV"));
                    this.setNombre(rs.getString("INVITADOPOR"));
                    this.setUltInvitacion(rs.getInt("ULTINVITACION"));
                    this.setNumImpresiones(rs.getInt("NUMIMPRESIONES"));
                    this.setIdEstado(rs.getInt("IDESTADO"));
                    this.setMinimo(rs.getInt("MINIMO"));
                    this.setTipoCliente(rs.getInt("TIPOCLIENTE"));
                    this.setInvitadoPor(rs.getInt("INVITADOPOR2"));
                    this.setEdad(rs.getInt("EDAD"));
                    
                    // GUARDAMOS EL NOMBRE DEL PRODUCTO
                    this.setNombreProducto(rs.getString("NOMBREPRODUCTO"));
                    
                    if(rs.getString("PORENTRADAS").equalsIgnoreCase("T")){
                        this.setActivoEntrada(true);
                    }
                    bPrimeraLinea=false;
                }else{
                    this.setLineas(2);
                }
            }
        } catch (SQLException ex) {
            Util.guardarError(ex, true,parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (rs != null) rs.close();
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
            return bReturn;
        }
    }
    
    protected boolean esClienteNuevo(java.awt.Component parent) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        boolean bReturn = true;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "SELECT CCL.CODCLIENTE FROM CLIENTESCAMPOSLIBRES CCL INNER JOIN RIP_V_MEMBRESIAS_CLIENTES MC ON CCL.CODCLIENTE=MC.CODCLIENTE AND MC.ORDEN=1 WHERE DATEDIFF(DAY,ISNULL(INACTIVODESDE,CAST(GETDATE() AS DATE)),CAST(GETDATE() AS DATE))<90 AND CCL.CODCLIENTE=? UNION ALL SELECT 1 WHERE NOT ? IN (SELECT CODARTICULO FROM VIEWGRUPO_3)";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setInt(1, this.getCodCliente());
            pSQL.setInt(2, this.getCodArticulo());
            rs = pSQL.executeQuery();
            if (rs.next()) { bReturn=false; }
        } catch (SQLException ex) { Util.guardarError(ex, true,parent); } 
        catch (Exception ex) { Util.guardarError(ex, true, parent); } 
        finally {
            if (rs != null) rs.close();
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
            return bReturn;
        }
    }
    
    protected int getCantidadClientesFactura(java.awt.Component parent) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        int iReturn = 0;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "SELECT COUNT(*) CANTIDAD FROM RIP_FACTURASVENTA_CLIENTES FV WHERE FV.NUMSERIE=? AND FV.NUMFACTURA=? AND FV.N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            rs = pSQL.executeQuery();
            if (rs.next()) { iReturn=rs.getInt("CANTIDAD"); }
        } catch (SQLException ex) { Util.guardarError(ex, true,parent); } 
        catch (Exception ex) { Util.guardarError(ex, true, parent); } 
        finally {
            if (rs != null) rs.close();
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
            return iReturn;
        }
    }

    protected void actualizarFechaInicio(java.awt.Component parent, String sFecha) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=?, INVITADOPOR=? WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setInt(2, this.getInvitadoPor());
            pSQL.setString(3, SERIE);
            pSQL.setInt(4, NUMERO);
            pSQL.setString(5, N);
            pSQL.execute();
            
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET FECHAINICIO=? , DIASRESTANTES=ISNULL(DIASRESTANTES,0)+? , ULTACTUALIZACION=CAST(GETDATE() AS DATE) WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setInt(2, this.getDuracion());
            pSQL.setInt(3, this.getCodCliente());
            pSQL.execute();
            
            if (this.getCodArticulo()==MainFrame.ARTICULO_ENTRADAS){
                sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET PORENTRADAS='T' WHERE CODCLIENTE=?";
                pSQL = dbConnLocal.prepareStatement(sSQL);
                pSQL.setInt(1, this.getCodCliente());
                pSQL.execute();
            }
        } catch (SQLException ex) { Util.guardarError(ex, true, parent); } 
        catch (Exception ex) { Util.guardarError(ex, true, parent); } 
        finally {
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
        }
    }
    
    protected void actualizarFechaInicioCongelado(java.awt.Component parent, String sFecha) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=? WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setString(2, SERIE);
            pSQL.setInt(3, NUMERO);
            pSQL.setString(4, N);
            pSQL.execute();
            
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET FECHACONGELADO=? , DIASCONGELADO=? , ULTACTUALIZACION=CAST(GETDATE() AS DATE) WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setInt(2, 60);
            pSQL.setInt(3, this.getCodCliente());
            pSQL.execute();
        } catch (SQLException ex) { Util.guardarError(ex, true, parent); } 
        catch (Exception ex) { Util.guardarError(ex, true, parent); } 
        finally {
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
        }
    }

    protected void actualizarFechaInicioGrupal(java.awt.Component parent, String sFecha) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=? WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setString(2, SERIE);
            pSQL.setInt(3, NUMERO);
            pSQL.setString(4, N);
            pSQL.execute();
            
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET FECHAINICIO=CASE WHEN ISNULL(DIASRESTANTES,0)=0 THEN ? ELSE FECHAINICIO END, DIASRESTANTES=ISNULL(DIASRESTANTES,0)+? WHERE CODCLIENTE IN (SELECT CODCLIENTE FROM RIP_FACTURASVENTA_CLIENTES FV WHERE FV.NUMSERIE=? AND FV.NUMFACTURA=? AND FV.N=?)";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setInt(2, this.getDuracion()/this.getUnidades());
            pSQL.setString(3, this.getSerie());
            pSQL.setInt(4, this.getNumero());
            pSQL.setString(5, this.getN());
            pSQL.execute();
        } catch (SQLException ex) { Util.guardarError(ex, true, parent); } 
        catch (Exception ex) { Util.guardarError(ex, true, parent); } 
        finally {
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
        }
    }
    
    // Este método ahora recibe TRES parámetros: parent, fechaInicio y fechaFin
    protected void actualizarFechaInicio(java.awt.Component parent, String sFechaInicio, String sFechaFin) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector = new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            
            // 1. Actualizamos la Factura para marcarla como usada (Esto se mantiene igual)
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=?, INVITADOPOR=? WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFechaInicio);
            pSQL.setInt(2, this.getInvitadoPor());
            pSQL.setString(3, SERIE);
            pSQL.setInt(4, NUMERO);
            pSQL.setString(5, N);
            pSQL.execute();
            
            // 2. Quitamos el bloqueo/estado de la factura
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            // 3. ACTUALIZACIÓN DEL CLIENTE (NUEVA LÓGICA)
            // Ya no sumamos días. Seteamos Inicio y Fin directamente.
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET FECHAINICIO=?, FECHAFIN=?, ULTACTUALIZACION=CAST(GETDATE() AS DATE) WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFechaInicio);
            pSQL.setString(2, sFechaFin); // Nueva fecha calculada
            pSQL.setInt(3, this.getCodCliente());
            pSQL.execute();
            
            // Eliminamos la lógica de PORENTRADAS si ya no la necesitas, 
            // o la dejamos si aún hay pases por visita.
            if (this.getCodArticulo() == MainFrame.ARTICULO_ENTRADAS){
                sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET PORENTRADAS='T' WHERE CODCLIENTE=?";
                pSQL = dbConnLocal.prepareStatement(sSQL);
                pSQL.setInt(1, this.getCodCliente());
                pSQL.execute();
            }
            
        } catch (SQLException ex) { 
            Util.guardarError(ex, true, parent); 
        } catch (Exception ex) { 
            Util.guardarError(ex, true, parent); 
        } finally {
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
        }
    }

    // Hacemos lo mismo para el método Grupal
    protected void actualizarFechaInicioGrupal(java.awt.Component parent, String sFechaInicio, String sFechaFin) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector = new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            
            // Actualizar Factura
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=? WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFechaInicio);
            pSQL.setString(2, SERIE);
            pSQL.setInt(3, NUMERO);
            pSQL.setString(4, N);
            pSQL.execute();
            
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            // Actualizar Todos los Clientes del Grupo con FECHAINICIO y FECHAFIN
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET FECHAINICIO=?, FECHAFIN=? WHERE CODCLIENTE IN (SELECT CODCLIENTE FROM RIP_FACTURASVENTA_CLIENTES FV WHERE FV.NUMSERIE=? AND FV.NUMFACTURA=? AND FV.N=?)";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFechaInicio);
            pSQL.setString(2, sFechaFin);
            pSQL.setString(3, this.getSerie());
            pSQL.setInt(4, this.getNumero());
            pSQL.setString(5, this.getN());
            pSQL.execute();
            
        } catch (SQLException ex) { 
            Util.guardarError(ex, true, parent); 
        } catch (Exception ex) { 
            Util.guardarError(ex, true, parent); 
        } finally {
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
        }
    }
    
    protected void removeClient(java.awt.Component parent, int iCodClient) throws SQLException {
         Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "DELETE FROM RIP_FACTURASVENTA_CLIENTES WHERE  NUMSERIE=? AND NUMFACTURA=? AND N=? AND CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.setInt(4, iCodClient);
            pSQL.execute();
        } catch (SQLException ex) { Util.guardarError(ex, true, parent); } 
        catch (Exception ex) { Util.guardarError(ex, true, parent); } 
        finally {
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
        }
    }
    
    protected void addClient(java.awt.Component parent, int iCodClient) throws SQLException {
         Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "INSERT INTO RIP_FACTURASVENTA_CLIENTES(NUMSERIE, NUMFACTURA, N, CODCLIENTE)VALUES(?,?,?,?)";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.setInt(4, iCodClient);
            pSQL.execute();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(parent, "The client already exists.", "ErrorMessage", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { Util.guardarError(ex, true, parent); } 
        finally {
            if (dbConnLocal != null) dbConnLocal.close();
            if (pSQL != null) pSQL.close();
        }
    }

    // --- GETTERS & SETTERS (Incluyendo el nuevo) ---
    // ESTOS MÉTODOS SON LOS QUE FALTABAN
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    
    public int getCodCliente() { return codCliente; }
    public void setCodCliente(int codCliente) { this.codCliente = codCliente; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getN() { return n; }
    public void setN(String n) { this.n = n; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public int getLineas() { return lineas; }
    public void setLineas(int lineas) { this.lineas = lineas; }
    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) { this.unidades = unidades; }
    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }
    public int getUltInvitacion() { return ultInvitacion; }
    public void setUltInvitacion(int ultInvitacion) { this.ultInvitacion = ultInvitacion; }
    public int getCodArticulo() { return codArticulo; }
    public void setCodArticulo(int codArticulo) { this.codArticulo = codArticulo; }
    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }
    public String getFechaInv() { return fechaInv; }
    public void setFechaInv(String fechaInv) { this.fechaInv = fechaInv; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getNumImpresiones() { return numImpresiones; }
    public void setNumImpresiones(int numImpresiones) { this.numImpresiones = numImpresiones; }
    public int getIdEstado() { return idEstado; }
    public void setIdEstado(int idEstado) { this.idEstado = idEstado; }
    public int getMinimo() { return minimo; }
    public void setMinimo(int minimo) { this.minimo = minimo; }
    public int getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(int tipoCliente) { this.tipoCliente = tipoCliente; }
    public int getInvitadoPor() { return invitadoPor; }
    public void setInvitadoPor(int invitadoPor) { this.invitadoPor = invitadoPor; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public String getPromoPremio() { return promoPremio; }
    public void setPromoPremio(String promoPremio) { this.promoPremio = promoPremio; }
    public boolean isEntrada() { return entrada; }
    public void setEntrada(boolean entrada) { this.entrada = entrada; }
    public boolean isActivoEntrada() { return activoEntrada; }
    public void setActivoEntrada(boolean activoEntrada) { this.activoEntrada = activoEntrada; }
}