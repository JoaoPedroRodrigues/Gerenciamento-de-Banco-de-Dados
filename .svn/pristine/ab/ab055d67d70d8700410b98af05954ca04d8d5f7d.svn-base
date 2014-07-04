/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import static control.Manager.*;
import java.util.HashMap;
import model.BTree;
import model.FinalNode;
import model.InternalNode;
import model.Node;
import model.disk.Block;
import model.mem.Buffer;
import model.mem.Page;

/**
 *
 * @author Guilherme
 */
public class Start
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        prepareEnvironment();
        try
        {
            Buffer b = new Buffer(Manager.BASE_DIR, Manager.NUMBER_OF_FRAMES, Block.SIZE, Manager.RECORDS_VARIABLE);
            
            //<editor-fold defaultstate="collapsed" desc="Testa classes extends Node">
            
            //            InternalNode i = InternalNode.toInternalNode(Manager.BASE_DIR, "NoInternoTeste.txt", 0);
            //            Page p = b.newPage("NEW.txt");
            //            p.putAll(i.toPage(BASE_DIR, -1));
            //            p.writePage();
            //
            //            i = InternalNode.toInternalNode(Manager.BASE_DIR, "NoInternoTeste.txt", 1);
            //            p = b.newPage("NEW.txt");
            //            p.putAll(i.toPage(BASE_DIR, -1));
            //            p.writePage();
            //
            //            i = InternalNode.toInternalNode(Manager.BASE_DIR, "NoInternoTeste.txt", 2);
            //            p = b.newPage("NEW.txt");
            //            p.putAll(i.toPage(BASE_DIR, -1));
            //            p.writePage();
            
            //            FinalNode i = FinalNode.toFinalNode(Manager.BASE_DIR, "NoFolhaTeste.txt", 0);
            //            Page p = b.newPage("NEW2.txt");
            //            p.putAll(i.toPage(BASE_DIR, -1));
            //            p.writePage();
            //
            //            i = FinalNode.toFinalNode(Manager.BASE_DIR, "NoFolhaTeste.txt", 1);
            //            p = b.newPage("NEW2.txt");
            //            p.putAll(i.toPage(BASE_DIR, -1));
            //            p.writePage();
            //
            //            i = FinalNode.toFinalNode(Manager.BASE_DIR, "NoFolhaTeste.txt", 2);
            //            p = b.newPage("NEW2.txt");
            //            p.putAll(i.toPage(BASE_DIR, -1));
            //            p.writePage();
            //</editor-fold>
            
        //   BTree bt = new BTree(b, 194, 389, Manager.BASE_DIR ,"TESTE.txt", "btree.data" ,0);
            BTree bt = new BTree(b, 2, 4, Manager.BASE_DIR ,"TESTE.txt", "btree.data" ,0);
//            b.newPage("TESTE.txt");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Seta alguns parâmetros utilizados durante todo o sistema.
     */
    @SuppressWarnings("empty-statement")
    public static void prepareEnvironment()
    {
        Manager.BASE_DIR = "C:\\Users\\joao Pedro\\Documents\\gbd\\trampo\\";
        Manager.R_RELATION = "TESTE.txt";
        Manager.S_RELATION = "OUTT.txt";
        Manager.RECORDS_VARIABLE = false;  // se os campos sao do tipo variavel ou nao
        Manager.NUMBER_OF_FRAMES = 5; 
        
        /*
         * Para a tabela do arquivo 'Tabela1.txt' temos:
         * Uma linha tem 23 bytes. Para um bloco de tamanho 8KB = 8192 bytes então
         * cada bloco deve ter 356 linhas  = 356 * 23 = 8188 bytes
         */
        
        Block.SIZE = 92; // 8188; //92;
        Page.NUMBER_OF_FIELDS = 2; // número de atributos (campos) na tupla
        Page.SIZE_OF_FIELD = 10; // número de bytes de cada campo da tupla (somente para registros de tamanho fixo)
        Page.SIZE_OF_RECORD = 23; // número de bytes por tupla (linha do arquivo)
        Page.RECORDS_PER_PAGE = 4; //356;// 4; // quantidade máxima de registros por página
        Page.RECORDS_PER_NODE = 4 + 1 ; //389 + 1; // 6;
        
        Manager.SCHEMA_MAP = new HashMap<>();
        
        Integer[] table1 = new Integer[2];
        table1[0] = 0; // inicio do primeiro atributo
        table1[1] = 11; // inicio do segundo atributo
        
        Manager.SCHEMA_MAP.put("TESTE.txt", table1);
        
        Node.NUM_REG = 4;//389; // 4;
        InternalNode.NUM_INIT_BYTES_NULL = 18;//1564; // 18;
        FinalNode.NUM_LAST_BYTES_NULL = 2;//13; // 2;
        
    }
}
