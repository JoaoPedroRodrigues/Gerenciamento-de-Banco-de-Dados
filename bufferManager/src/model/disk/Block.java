/*
 * Federal University of Uberlândia
 * Computer Science Department
 * 
 * Management Database - 2013/1
 * Project: Index Nested Loop Join with BTree
 * 
 */
package model.disk;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import model.mem.Page;

/**
 * Representa um bloco de dados de um arquivo em disco. Note que, nesta abordagem
 * um bloco é, somente, um array de bytes e não se tem interesse e nem conhecimento 
 * da quantidade de registros, campos e muito menos o tamnho deles no referido bloco.
 * O tratamento dessas peculiaridades é feito na classe <tt>Page</tt> no pacote 
 * <tt>mem</tt>.
 * 
 * @author Alana
 * @author Guilherme Alves
 */
public class Block
{
    private byte[] data;
    public static int SIZE; // tamanho máximo em bytes
    boolean pageNotFound = false;
    /**
     * Constrói um objeto Block através da leitura dos bytes correspondentes ao
     * <tt>numberBlock</tt> do arquivo <tt>nameFile</tt>.
     * 
     * @param path caminho do arquivo
     * @param nameFile nome do arquivo
     * @param numberBlock número do bloco
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public Block(String path, String nameFile, int numberBlock) throws FileNotFoundException, IOException
    {
        this.data = new byte[SIZE];
        this.readBlock(numberBlock, path, nameFile);
        if(pageNotFound)
            throw new IOException();
    }

    public byte[] getData()
    {
        return data;
    }
  
    
    /**
     * Lê o bloco de número <tt>numBlock</tt> do arquivo de nome <tt>nameFile</tt>.
     * Este método preenche um array de bytes com os bytes correspondentes do bloco.
     * Faz-se uso de RandomAccessFile para movimentar o ponteiro de leitura 
     * <tt>seek(long)</tt> e ajustar o ínicio da leitura
     * 
     * @param numberBlock número do bloco do arquivo a ser lido
     * @param path caminho do arquivo (diretório base)
     * @param nameFile nome do arquivo
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private void readBlock(int numberBlock, String path, String nameFile) throws FileNotFoundException, IOException
    {
        /*
         *  init marca o início da leitura
         *  end marca o fim da leitura
         */
        long init = numberBlock * SIZE;
        long end = init + SIZE;
        
        RandomAccessFile fi = new RandomAccessFile(path + nameFile, "r");
        long i = init;
        fi.seek(i); // ajusta ponteiro de leitura para o primeiro byte do bloco solicitado
        for (int j = 0; i < end; i++, j++)
        {
            try
            {
                this.data[j] = fi.readByte();
            } catch(EOFException e)
            {
                if(j==0)
                    pageNotFound = true;
                break;
            }
        }
        
        fi.close();
        /*
        for (int j = 0; j < this.data.length; j++){
            char c = (char) this.data[j];
            System.out.print(c);
        }
        * */
    } 
    
    public static long getLength (String path, String nameFile) throws FileNotFoundException, IOException
    {
        RandomAccessFile fi = new RandomAccessFile(path + nameFile, "r");
        long len = fi.length();
        fi.close();
        return len;
    }
    
    public static boolean isInternalNode(String path, String nameFile, int numberBlock, int rowSize) throws FileNotFoundException, IOException
    {
        RandomAccessFile fi = new RandomAccessFile(path + nameFile, "r");
        long init = numberBlock * SIZE;
        long end = init + (rowSize - 2);
        long i = init;
        fi.seek(i); // ajusta ponteiro de leitura para o primeiro byte do bloco solicitado
        for (int j = 0; i < end; i++, j++)
        {
            try
            {
                byte read = fi.readByte();
                if (Byte.compare(read, (byte) 0) != 0)
                {
                    return false;
                }
            } catch(EOFException e)
            {
                return false;
            }
        }
        
        fi.close();
        return true;
    }
    
    public static int computeNewBlockNumber(String path, String nameFile) throws FileNotFoundException, IOException
    {
        long lastPointer = getLength(path, nameFile);
        int nPages = (int) (lastPointer / (long) Block.SIZE);
        if (!containsNumBlock(path, nameFile, nPages + 1))
        {
            return nPages + 1;
        }
        return nPages;
        
    }
    
    private static boolean containsNumBlock(String path, String nameFile, int numBlock) throws FileNotFoundException, IOException
    {
        long init = numBlock * SIZE;
        long end = init + SIZE;
        
        byte b;
        
        RandomAccessFile fi = new RandomAccessFile(path + nameFile, "r");
        long i = init;
        fi.seek(i); // ajusta ponteiro de leitura para o primeiro byte do bloco solicitado
        for (int j = 0; i < end; i++, j++)
        {
            try
            {
                b = fi.readByte();
            } catch(EOFException e)
            {
                if(j==0)
                    return true;
                break;
            }
        }
        return false;
    }
}
