/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugin1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Antonio
 */
public class XML {
    public boolean modificarXML(String dir, String etiqueta, String value){
        try{
            SAXBuilder build = new SAXBuilder(); //Instanciamos la clase SAXBuilder para poder obtener la extructura del documento XML.
            Document doc = (Document)build.build(new File(dir)); //Leemos el XML para convertirlo en Document.
            Element root = doc.getRootElement(); //Obtenemos el elemento root del documento.
            List<Element> child = root.getChildren(); //Obtenemos a los hijos pertenecientes al root, el cual nos trae en forma de lista.
            /*
            Creamos un for tipo foreach el cual leeremos todos los Elements que tiene cada Element.
            De cada Element sacamos sus hijos y le mandamos el nuevo texto que tendra cada etiqueta
             */
            child.stream().map((i) -> i.getChildren()).forEachOrdered((ch) -> {
                ch.stream().filter((o) -> (o.getName().equalsIgnoreCase(etiqueta))).map((Element o) -> {
                    return o;
                }).forEachOrdered((o) -> {
                    o.setText(value);
                }); //System.out.println(o.getName());
            });
            XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
            try (FileWriter fw = new FileWriter(new File(dir))) {
                xmlOut.output(doc, fw);
            }
            return true;
        }catch(IOException | JDOMException ex){
            Util.guardarError(ex, false);
            return false;
        }
    }
}
