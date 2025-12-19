/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugin1;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ANTONIO
 */
public class ModeloAddClientes extends DefaultTableModel {

    java.awt.Frame parent;


    public ModeloAddClientes(java.awt.Frame frame) {
        parent = frame;
        this.addColumn("Cliente");
        this.addColumn("Id");
        this.addColumn("Teléfono");
        this.addColumn("Email");
        
    }
    
    public static final int COL_CLIENTE = 0;
    public static final int COL_RIF = 1;
    public static final int COL_TELEFONO = 2;
    public static final int COL_EMAIL = 3;

    
    
    public ModeloAddClientes() {
        this.addColumn("Cliente");
        this.addColumn("Id");
        this.addColumn("Teléfono");
        this.addColumn("Email");
    }
            
    
    @Override
    public Class getColumnClass(int columna) {
        if (columna == COL_CLIENTE) {
            return Cliente.class;
        }

        if (columna == COL_RIF) {
            return String.class;
        }
        
        if (columna == COL_TELEFONO) {
            return String.class;
        }
        
        if (columna == COL_EMAIL) {
            return String.class;
        }
        
        return Object.class;
    }
    
    
    public void llenarTabla(String sFiltro, boolean soloInactivos) {
        vaciarTabla();
        Object[] fila = new Object[5];
        try {
            Cliente oDatos = new Cliente(parent);
            //oPlano.setCodAlmacen(sCodAlmacen);
            //oPlano.setNumero(lNumero);
            for (Cliente oAux : oDatos.getClientes(sFiltro, soloInactivos)){
                fila[COL_CLIENTE] = oAux;
                fila[COL_RIF]=oAux.getRif();
                fila[COL_TELEFONO]=oAux.getTelefono();
                fila[COL_EMAIL]=oAux.getEmail();
                //System.out.println("agregando");
                this.addRow(fila);
            }
            
        } catch (Exception ex) {
            Util.guardarError(ex, true);
        }
        
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
       // Aquí devolvemos true o false según queramos que una celda
        // identificada por fila,columna (row,column), sea o no editable
        //if (column == 3)
        //return true;
        return false;
    }

    public void vaciarTabla() {
        while (this.getRowCount() > 0) {
            this.removeRow(0);
        }
    }
    
    

    public void eliminaRow(int iRow) {
        this.removeRow(iRow);
    }
}
