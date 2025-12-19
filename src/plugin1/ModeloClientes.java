/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugin1;

import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ANTONIO
 */
public class ModeloClientes extends DefaultTableModel {

    java.awt.Frame parent;


    public ModeloClientes(java.awt.Frame frame) {
        parent = frame;

        this.addColumn("Client");
        this.addColumn("ID");
        
        
    }
    
    public static final int COL_CLIENTE = 0;
    public static final int COL_RIF = 1;
        
    
    public ModeloClientes() {
        this.addColumn("Client");
        this.addColumn("ID");
    }
            
    
    @Override
    public Class getColumnClass(int columna) {
        if (columna == COL_CLIENTE) {
            return Cliente.class;
        }

        if (columna == COL_RIF) {
            return String.class;
        }
        
        return Object.class;
    }
    
    
    public void llenarTabla(Factura oFactura) {
        vaciarTabla();
        Object[] fila = new Object[5];
        try {
            Cliente oDatos = new Cliente(parent);
           
            for (Cliente oAux : oDatos.getClientes(oFactura)){
                fila[COL_CLIENTE] = oAux;
                fila[COL_RIF]=oAux.getRif();
                //System.out.println("agregando");
                this.addRow(fila);
            }
            
        } catch (SQLException ex) {
            Util.guardarError(ex, true, parent);
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
