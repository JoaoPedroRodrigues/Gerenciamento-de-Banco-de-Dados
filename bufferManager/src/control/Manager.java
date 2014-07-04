/*
 * Federal University of Uberlândia
 * Computer Science Department
 * 
 * Management Database - 2013/1
 * Project: Index Nested Loop Join with BTree
 * 
 */
package control;

import java.util.HashMap;

/**
 * Representa a aplicação que faz requisições sobre o buffer pool.
 *
 * @author Alana
 * @author Guilherme Alves
 * @author Guilherme Nunes
 * @author João Pedro Galvão
 */
public class Manager
{
    public static String BASE_DIR = "D:\\Área de trabalho\\Pendentes\\ProjGBD\\Geradas-Numero\\";
    public static String R_RELATION = "Tabela3.txt";
    public static String S_RELATION = "OUTT.txt";
    public static boolean RECORDS_VARIABLE = false;  // se os campos sao do tipo variavel ou nao
    public static int NUMBER_OF_FRAMES = 5;         // numero de paginas
    public static HashMap<String, Integer[]> SCHEMA_MAP;

    
    
}
