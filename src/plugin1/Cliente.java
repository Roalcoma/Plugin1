/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugin1;

import com.digitalpersona.onetouch.DPFPTemplate;
import conexionbd.Conector;
import conexionbd.Datos;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import shared.util.DateTimeInt;

/**
 *
 * @author ANTONIO
 */
public class Cliente  {

    private int codCliente;
    private String nombre;
    private String rif;
    private String telefono;
    private String email;
    
    private String serie;
    private int numero;
    private String n;
    
    
    java.awt.Frame parent;

    public Cliente(java.awt.Frame parent) {
        this.parent = parent;
    }

    protected void guardarHuella(Component padre, DPFPTemplate template) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector = new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
            Integer tamanioHuella = template.serialize().length;
            System.out.println("Borrando huella anterior "+this.getCodCliente());
            sSQL = "DELETE FROM HUELLASCLIENTE WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setQueryTimeout(15);
            pSQL.setInt(1, this.getCodCliente());
            pSQL.execute();
            System.out.println("Huella anterior borrada");
            System.out.println("Guardando huella nueva");
            sSQL="INSERT INTO HUELLASCLIENTE(CODCLIENTE, HUELLAX64) VALUES(?,?)";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setInt(1, this.getCodCliente());
            pSQL.setBinaryStream(2,datosHuella,tamanioHuella);
            pSQL.execute();
            System.out.println("Huella nueva guardada");
            

        } catch (SQLException sqle) {
            Util.guardarError(sqle, true, padre);
        } catch (Exception ex) {
            Util.guardarError(ex, true, padre);
        } finally {

            if (dbConnLocal != null) {
                dbConnLocal.close();
            }
            if (pSQL != null) {
                pSQL.close();
            }

        }

    }
    
    protected ImageIcon getFoto(Component parent, int iWidth, int iHeight) throws SQLException{
        Connection dbConnLocal = null;
        Datos.cargarConfig();
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        ImageIcon iReturn = null;
        Conector oConector = new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            String sSQL = "SELECT FOTOCLIENTE FROM CLIENTES WHERE CODCLIENTE=? AND NOT FOTOCLIENTE IS NULL";

            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setInt(1, this.getCodCliente());

            rs = pSQL.executeQuery();
            if (rs.next()) {
		try{
                    Blob blob = rs.getBlob("FOTOCLIENTE");
                    BufferedImage image= ImageIO.read(blob.getBinaryStream());
                    
                    iReturn=new ImageIcon(image.getScaledInstance(iWidth, iHeight, Image.SCALE_DEFAULT));
                    
                }catch (Exception e){
                    Util.guardarError(e, false, parent);
                }
                
            }else{
                iReturn=new ImageIcon(getClass().getResource("/controlacceso/resources/silueta.jpg"));
            }
            parent.repaint();

        } catch (SQLException ex) {
           Util.guardarError(ex, false);
        } catch (Exception ex) {
           Util.guardarError(ex, false);
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
    
    public List<Cliente> getClientes(Factura oFactura) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        Cliente oCliente;
        List<Cliente> vClientes = new ArrayList();
        Conector oConector = new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "SELECT\n" +
                    "	C.NOMBRECLIENTE, C.CODCLIENTE \n" +
                    "	, C.NIF20\n" +
                    "FROM\n" +
                    "	RIP_FACTURASVENTA_CLIENTES FV\n" +
                    "	INNER JOIN CLIENTES C ON FV.CODCLIENTE=C.CODCLIENTE\n" +
                    "WHERE\n" +
                    "	FV.NUMSERIE=?\n" +
                    "	AND FV.NUMFACTURA=?\n" +
                    "	AND FV.N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, Factura.SERIE);
            pSQL.setInt(2, Factura.NUMERO);
            pSQL.setString(3, Factura.N);
      

            rs = pSQL.executeQuery();
            while (rs.next()) {
                oCliente = new Cliente(parent);
                oCliente.setCodCliente(rs.getInt("CODCLIENTE"));
                oCliente.setNombre(rs.getString("NOMBRECLIENTE"));
                oCliente.setRif(rs.getString("NIF20"));
             
                vClientes.add(oCliente);
            }
        } catch (SQLException sqle) {
            Util.guardarError(sqle, true, parent);
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
            return vClientes;
        }
    }
    
    public List<Cliente> getClientes(String sFiltro, boolean soloInactivos) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        Cliente oCliente;
        List<Cliente> vClientes = new ArrayList();
        Conector oConector = new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "DECLARE @FILTRO AS NVARCHAR(20)\n" +
                    "SET @FILTRO=?\n" +
                    "SELECT \n" +
                    "	C.CODCLIENTE\n" +
                    "	, C.NOMBRECLIENTE\n" +
                    "	, C.NIF20\n" +
                    "	, C.TELEFONO1\n" +
                    "	, C.E_MAIL \n" +
                    "FROM \n" +
                    "	CLIENTES C\n" +
                    "	INNER JOIN CLIENTESCAMPOSLIBRES CCL ON C.CODCLIENTE=CCL.CODCLIENTE\n" +
                    "WHERE \n" +
                    "	(C.NOMBRECLIENTE LIKE @FILTRO \n" +
                    "		OR C.NIF20 LIKE @FILTRO\n" +
                    "		OR C.TELEFONO1 LIKE @FILTRO \n" +
                    "		OR C.TELEFONO2 LIKE @FILTRO \n" +
                    "		OR C.E_MAIL LIKE @FILTRO) AND C.CODCLIENTE> 0 AND C.DESCATALOGADO='F' ";
            //if(soloInactivos){
                //sSQL+=" AND ISNULL(CCL.DIASRESTANTES,0) <=7 AND ISNULL(C.TIPO,0)<>3 ORDER BY NOMBRECLIENTE";
            //}
           
            System.out.println("filtro -->"+sFiltro);
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, "%" + sFiltro + "%");
            rs = pSQL.executeQuery();
            while (rs.next()) {
                oCliente = new Cliente(parent);
                oCliente.setCodCliente(rs.getInt("CODCLIENTE"));
                oCliente.setNombre(rs.getString("NOMBRECLIENTE"));
                oCliente.setRif(rs.getString("NIF20"));
                oCliente.setTelefono(rs.getString("TELEFONO1"));
                oCliente.setEmail(rs.getString("E_MAIL"));

                vClientes.add(oCliente);
            }
        } catch (SQLException sqle) {
            Util.guardarError(sqle, true, parent);
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
            return vClientes;
        }
    }
    
    protected boolean esGrupal(int iCodCliente) throws SQLException{
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        ResultSet rs = null;
       
        Conector oConector = new Conector();
        boolean bReturn =false;
        try {
            dbConnLocal = oConector.getConexion();
            sSQL = "SELECT\n" +
                    "	FVC.*\n" +
                    "FROM \n" +
                    "	RIP_FACTURASVENTA_CLIENTES FVC\n" +
                    "	INNER JOIN FACTURASVENTA FV ON FVC.NUMSERIE=FV.NUMSERIE AND FVC.NUMFACTURA=FV.NUMFACTURA AND FV.N=FVC.N\n" +
                    "WHERE\n" +
                    "	FVC.CODCLIENTE=?\n" +
                    "ORDER BY\n" +
                    "	FV.FECHA DESC";
            pSQL = dbConnLocal.prepareStatement(sSQL);
           
            pSQL.setInt(1, iCodCliente);
     
      

            rs = pSQL.executeQuery();
            if (rs.next()) {
                this.setNumero(rs.getInt("NUMFACTURA"));
                this.setSerie(rs.getString("NUMSERIE"));
                this.setN(rs.getString("N"));
                bReturn=true;
            }
        } catch (SQLException sqle) {
            Util.guardarError(sqle, true, parent);
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
    
    
    
    protected void cargarDatos(int iID, MainFrame parent) throws SQLException{
        Connection dbConnLocal = null;
        Datos.cargarConfig();
        PreparedStatement pSQL = null;
        ResultSet rs = null;
        Conector oConector = new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            String sSQL = "SELECT NOMBRECLIENTE, FOTOCLIENTE FROM CLIENTES WHERE CODCLIENTE=?";

            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setInt(1, iID);

            rs = pSQL.executeQuery();
            if (rs.next()) {
		//parent.etiquetaSQLNombre.setText(rs.getString("NOMBRECLIENTE"));
                 
                
                //InputStream in =rs.getBinaryStream("FOTO");
                //int n = in.read();
                try{
                    Blob blob = rs.getBlob("FOTOCLIENTE");
                    BufferedImage image= ImageIO.read(blob.getBinaryStream());
                    
                    
                    parent.etiquetaFoto.setIcon(new ImageIcon(
                    image.getScaledInstance(parent.etiquetaFoto.getWidth(), parent.etiquetaFoto.getHeight(), Image.SCALE_DEFAULT)));
                    
                }catch (Exception e){
                    System.out.println("No tiene foto");
                }
                
            }
            parent.repaint();

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
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
           
        }
        
    }
    
    protected void actualizarFechaInvitacion(java.awt.Component parent, int iCliente) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET ULTINVITACION=? , INVITADOPOR=? "
                    + " WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, new DateTimeInt().toStringDate());
            pSQL.setInt(2, this.getCodCliente());
            pSQL.setInt(3, iCliente);           
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
    
    protected void actualizarFechaRecomendado(java.awt.Component parent, int iCliente) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET FECHARECOMENDADO=? , RECOMENDADOPOR=? "
                    + " WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, new DateTimeInt().toStringDate());
            pSQL.setInt(2, this.getCodCliente());
            pSQL.setInt(3, iCliente);           
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
    
    
    protected void actualizarInvitacionesDiposnibles(int iCliente) throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            
            sSQL = "UPDATE CLIENTESCAMPOSLIBRES SET INVITACIONES=INVITACIONES-1  WHERE CODCLIENTE=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setInt(1, iCliente);           
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
    
    protected void copiarGrupo() throws SQLException {
        Connection dbConnLocal = null;
        String sSQL;
        PreparedStatement pSQL = null;
        Conector oConector =new Conector();
        try {
            dbConnLocal = oConector.getConexion();
            
            sSQL = "INSERT INTO RIP_FACTURASVENTA_CLIENTES(NUMSERIE, NUMFACTURA, N, CODCLIENTE) \n" +
                "SELECT \n" +
                "	?\n" +
                "	, ?\n" +
                "	, ?\n" +
                "	, FVC.CODCLIENTE\n" +
                "FROM\n" +
                "	RIP_FACTURASVENTA_CLIENTES FVC\n" +
                "WHERE\n" +
                "	FVC.NUMSERIE=?\n" +
                "	AND FVC.NUMFACTURA=?\n" +
                "	AND FVC.N=?";
            pSQL = dbConnLocal.prepareStatement(sSQL);
            pSQL.setString(1, Factura.SERIE);
            pSQL.setInt(2, Factura.NUMERO);
            pSQL.setString(3, Factura.N);      
            pSQL.setString(4, this.getSerie());
            pSQL.setInt(5, this.getNumero());
            pSQL.setString(6, this.getN());  
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
     * @return the rif
     */
    public String getRif() {
        return rif;
    }

    /**
     * @param rif the rif to set
     */
    public void setRif(String rif) {
        this.rif = rif;
    }

    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return this.nombre;
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

}
