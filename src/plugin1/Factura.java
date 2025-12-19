/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author ANTONIO
 */


public class Factura{
    
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
    private boolean entrada;//PARA SABER SI HAY ALGUNA MEMBRESIA POR ENTRADA
    private boolean activoEntrada;
    
    static void cargarXML(java.awt.Component parent) throws Exception {
        File f = new File(XML);
        if (!f.exists()) {
            System.exit(0);

            //throw new Exception("Archivo "+XML+" no existe.");
        }
        try {
            try (Scanner s = new Scanner(f)) {

                while (s.hasNextLine()) {
                    String linea = s.nextLine();
                    //System.out.println(linea);
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
                        //f.delete();
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
                        //f.delete();
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
            if (rs != null) {
                rs.close();
            }
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
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
                    //this.setPromoPremio(rs.getString("PROMO"));
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
            if (rs != null) {
                rs.close();
            }
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
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
            sSQL = "SELECT\n" +
                    "	CCL.CODCLIENTE\n" +
                    "FROM \n" +
                    "	CLIENTESCAMPOSLIBRES CCL \n" +
                    "	INNER JOIN RIP_V_MEMBRESIAS_CLIENTES MC ON CCL.CODCLIENTE=MC.CODCLIENTE AND MC.ORDEN=1\n" +
                    "WHERE\n" +
                    "	DATEDIFF(DAY,ISNULL(INACTIVODESDE,CAST(GETDATE() AS DATE)),CAST(GETDATE() AS DATE))<90\n" +
                    "	AND CCL.CODCLIENTE=?\n" +
                    "UNION ALL\n" +
                    "SELECT 1 WHERE NOT ? IN (SELECT CODARTICULO FROM VIEWGRUPO_3)";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            
            pSQL.setInt(1, this.getCodCliente());
            pSQL.setInt(2, this.getCodArticulo());
            rs = pSQL.executeQuery();

            if (rs.next()) {
               bReturn=false;
            }
        } catch (SQLException ex) {
            Util.guardarError(ex, true,parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
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
            sSQL = "SELECT\n" +
                    "	COUNT(*) CANTIDAD\n" +
                    "FROM\n" +
                    "	RIP_FACTURASVENTA_CLIENTES FV\n" +
                    "WHERE\n" +
                    "	FV.NUMSERIE=?\n" +
                    "	AND FV.NUMFACTURA=?\n" +
                    "	AND FV.N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            rs = pSQL.executeQuery();

            if (rs.next()) {
                iReturn=rs.getInt("CANTIDAD");
            }
        } catch (SQLException ex) {
            Util.guardarError(ex, true,parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
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
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=?, INVITADOPOR=? "
                    + " WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setInt(2, this.getInvitadoPor());
            pSQL.setString(3, SERIE);
            pSQL.setInt(4, NUMERO);
            pSQL.setString(5, N);
            pSQL.execute();
            
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 "
                    + " WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET FECHAINICIO=? , DIASRESTANTES=ISNULL(DIASRESTANTES,0)+? , ULTACTUALIZACION=CAST(GETDATE() AS DATE)"
                    + " WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setInt(2, this.getDuracion());
            pSQL.setInt(3, this.getCodCliente());
            pSQL.execute();
            
            if (this.getCodArticulo()==MainFrame.ARTICULO_ENTRADAS){
                sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET PORENTRADAS='T'"
                        + " WHERE CODCLIENTE=?";
                pSQL = dbConnLocal.prepareStatement(sSQL);
                pSQL.setInt(1, this.getCodCliente());
                pSQL.execute();
            }

        } catch (SQLException ex) {
            Util.guardarError(ex, true, parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
        }
    }
    
    
    protected void actualizarFechaInicioCongelado(java.awt.Component parent, String sFecha) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=? "
                    + " WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setString(2, SERIE);
            pSQL.setInt(3, NUMERO);
            pSQL.setString(4, N);
            pSQL.execute();
            
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 "
                    + " WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET FECHACONGELADO=? , DIASCONGELADO=? , ULTACTUALIZACION=CAST(GETDATE() AS DATE)"
                    + " WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setInt(2, 60);
            pSQL.setInt(3, this.getCodCliente());
            pSQL.execute();

        } catch (SQLException ex) {
            Util.guardarError(ex, true, parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
        }
    }
    protected void actualizarFechaInicioGrupal(java.awt.Component parent, String sFecha) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=?"
                    + " WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setString(2, SERIE);
            pSQL.setInt(3, NUMERO);
            pSQL.setString(4, N);
            pSQL.execute();
            
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 "
                    + " WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET \n" +
                "FECHAINICIO=CASE WHEN ISNULL(DIASRESTANTES,0)=0 THEN ? ELSE FECHAINICIO END\n" +
                ", DIASRESTANTES=ISNULL(DIASRESTANTES,0)+? \n" +
                " WHERE CODCLIENTE IN (\n" +
                "SELECT CODCLIENTE FROM RIP_FACTURASVENTA_CLIENTES FV WHERE FV.NUMSERIE=? AND FV.NUMFACTURA=? AND FV.N=?)";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, sFecha);
            pSQL.setInt(2, this.getDuracion()/this.getUnidades());
            pSQL.setString(3, this.getSerie());
            pSQL.setInt(4, this.getNumero());
            pSQL.setString(5, this.getN());
            pSQL.execute();

        } catch (SQLException ex) {
            Util.guardarError(ex, true, parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
        }
    }
    
    protected void actualizarFechaInicio(java.awt.Component parent) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "UPDATE FACTURASVENTACAMPOSLIBRES SET FECHAINICIO=GETDATE()"
                    + " WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
          
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            sSQL = "UPDATE FACTURASVENTA SET IDESTADO=0 "
                    + " WHERE NUMSERIE=? AND NUMFACTURA=? AND N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, SERIE);
            pSQL.setInt(2, NUMERO);
            pSQL.setString(3, N);
            pSQL.execute();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET DIASRESTANTES=ISNULL(DIASRESTANTES,0)+?"
                    + " WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setInt(1, this.getDuracion());
            pSQL.setInt(2, this.getCodCliente());
            pSQL.execute();
            
            if (this.getCodArticulo()==MainFrame.ARTICULO_ENTRADAS){
                sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET PORENTRADAS='T'"
                        + " WHERE CODCLIENTE=?";
                pSQL = dbConnLocal.prepareStatement(sSQL);
                pSQL.setInt(1, this.getCodCliente());
                pSQL.execute();
            }

        } catch (SQLException ex) {
            Util.guardarError(ex, true, parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
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
    
        } catch (SQLException ex) {
            Util.guardarError(ex, true, parent);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
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
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "The client already exists.",
                    "ErrorMessage",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Util.guardarError(ex, true, parent);
        } finally {
            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    

    /**
     * @return the codCliente
     */
    public int getCodCliente() {
        return codCliente;
    }

    /**
     * @param codCliente the codCliente to set
     */
    public void setCodCliente(int codCliente) {
        this.codCliente = codCliente;
    }

    /**
     * @return the serie
     */
    public String getSerie() {
        return serie;
    }

    /**
     * @param serie the serie to set
     */
    public void setSerie(String serie) {
        this.serie = serie;
    }

    /**
     * @return the numero
     */
    public int getNumero() {
        return numero;
    }

    /**
     * @param numero the numero to set
     */
    public void setNumero(int numero) {
        this.numero = numero;
    }

    /**
     * @return the n
     */
    public String getN() {
        return n;
    }

    /**
     * @param n the n to set
     */
    public void setN(String n) {
        this.n = n;
    }

    /**
     * @return the total
     */
    public double getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(double total) {
        this.total = total;
    }

    /**
     * @return the lineas
     */
    public int getLineas() {
        return lineas;
    }

    /**
     * @param lineas the lineas to set
     */
    public void setLineas(int lineas) {
        this.lineas = lineas;
    }

    /**
     * @return the unidades
     */
    public int getUnidades() {
        return unidades;
    }

    /**
     * @param unidades the unidades to set
     */
    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }

    /**
     * @return the dias
     */
    public int getDias() {
        return dias;
    }

    /**
     * @param dias the dias to set
     */
    public void setDias(int dias) {
        this.dias = dias;
    }

    /**
     * @return the ultInvitacion
     */
    public int getUltInvitacion() {
        return ultInvitacion;
    }

    /**
     * @param ultInvitacion the ultInvitacion to set
     */
    public void setUltInvitacion(int ultInvitacion) {
        this.ultInvitacion = ultInvitacion;
    }

    /**
     * @return the codArticulo
     */
    public int getCodArticulo() {
        return codArticulo;
    }

    /**
     * @param codArticulo the codArticulo to set
     */
    public void setCodArticulo(int codArticulo) {
        this.codArticulo = codArticulo;
    }

    /**
     * @return the duracion
     */
    public int getDuracion() {
        return duracion;
    }

    /**
     * @param duracion the duracion to set
     */
    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    /**
     * @return the fechaInv
     */
    public String getFechaInv() {
        return fechaInv;
    }

    /**
     * @param fechaInv the fechaInv to set
     */
    public void setFechaInv(String fechaInv) {
        this.fechaInv = fechaInv;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the numImpresiones
     */
    public int getNumImpresiones() {
        return numImpresiones;
    }

    /**
     * @param numImpresiones the numImpresiones to set
     */
    public void setNumImpresiones(int numImpresiones) {
        this.numImpresiones = numImpresiones;
    }

    /**
     * @return the idEstado
     */
    public int getIdEstado() {
        return idEstado;
    }

    /**
     * @param idEstado the idEstado to set
     */
    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    /**
     * @return the minimo
     */
    public int getMinimo() {
        return minimo;
    }

    /**
     * @param minimo the minimo to set
     */
    public void setMinimo(int minimo) {
        this.minimo = minimo;
    }

    /**
     * @return the tipoCliente
     */
    public int getTipoCliente() {
        return tipoCliente;
    }

    /**
     * @param tipoCliente the tipoCliente to set
     */
    public void setTipoCliente(int tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    /**
     * @return the invitadoPor
     */
    public int getInvitadoPor() {
        return invitadoPor;
    }

    /**
     * @param invitadoPor the invitadoPor to set
     */
    public void setInvitadoPor(int invitadoPor) {
        this.invitadoPor = invitadoPor;
    }

    /**
     * @return the edad
     */
    public int getEdad() {
        return edad;
    }

    /**
     * @param edad the edad to set
     */
    public void setEdad(int edad) {
        this.edad = edad;
    }

    /**
     * @return the promoPremio
     */
    public String getPromoPremio() {
        return promoPremio;
    }

    /**
     * @param promoPremio the promoPremio to set
     */
    public void setPromoPremio(String promoPremio) {
        this.promoPremio = promoPremio;
    }

    /**
     * @return the entrada
     */
    public boolean isEntrada() {
        return entrada;
    }

    /**
     * @param entrada the entrada to set
     */
    public void setEntrada(boolean entrada) {
        this.entrada = entrada;
    }

    /**
     * @return the activoEntrada
     */
    public boolean isActivoEntrada() {
        return activoEntrada;
    }

    /**
     * @param activoEntrada the activoEntrada to set
     */
    public void setActivoEntrada(boolean activoEntrada) {
        this.activoEntrada = activoEntrada;
    }
    
}
