/*
 * Federal University of Uberlândia
 * Computer Science Department
 * 
 * Management Database - 2013/1
 * Project: Index Nested Loop Join with BTree
 * 
 */
package model.mem;

import control.Manager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.disk.Block;

/**
 * Representa uma página de um arquivo. Nesta implmentação, a página é um mapa
 * hash de (rid, record), ou seja, dado um rid obtem-se um registro do mapa (se
 * existir).
 *
 * @see HashMap
 * @see Block
 *
 * @author Alana
 * @author Guilherme Alves
 * @author Guilherme Nunes
 * @author João Pedro Galvão
 */
public class Page extends HashMap<Integer, byte[]> implements Comparable
{

    private boolean dirty = false;  // bit sujo -> 'true' se a página foi alterada e ainda não foi gravada em disco
    private int pinCount;           // numero de vezes que ela foi uma pagina foi solicitada mais ainda nao foi libera
    private long nextPage;          // ponteiro para o primeiro byte da próxima página do arquivo
    public static int NUMBER_OF_FIELDS;
    public static int SIZE_OF_FIELD;
    public static int SIZE_OF_RECORD;
    public static int RECORDS_PER_PAGE;
    public static int RECORDS_PER_NODE;
    private String pathFile;
    private String nameFile;
    private int numberPage;
    private long lastTimeUsed; // ultima vez em que a pagina foi pedida
    private boolean isFNode = false;
    private boolean isINode = false;
    public boolean notFound = false;

    /**
     * Constrói uma página a partir dos parâmetros de entrada: caminho e nome do
     * arquivo e número da página.
     *
     * @param path caminho do arquivo
     * @param nameFile nome do arquivo
     * @param numberPage número da página
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Page(String path, String nameFile, int numberPage)
    {
        Block b;      
        try {
            b = new Block(path, nameFile, numberPage);
            this.toPage(b);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            this.notFound = true;
        }
        this.pinCount = 1;
        this.pathFile = path;
        this.nameFile = nameFile;
        this.numberPage = numberPage;
        lastTimeUsed = System.nanoTime();
    }
    
    public Page(HashMap<Integer, byte[]> data, String path, String nameFile, int numberPage)
    {
        this.putAll(data);
    }
    
    public boolean isNode(HashMap<Integer, byte[]> m)
    {
        if (m.get(m.size() - 1).length != m.get(0).length)
        {
            return true;
        }
        return false;
    }
    
    
    public Page(String path, String nameFile) throws FileNotFoundException, IOException
    {
        this.pathFile = path;
        this.nameFile = nameFile;
        this.numberPage = -1;
    }
    
    /**
     * Converte o <tt>Block</tt> que é um array de bytes em um <tt>Page</tt>.
     *
     * @param b bloco do arquivo
     */
    private void toPage(Block b)
    {
        byte[] data = b.getData();
        int i, j, s, f, next;

        /*
         *  i controla o percorrimento de data
         *  j controla o percorrimento dentro do campo em 'record'
         *  f indica qual atributo/campo deverá ser preenchido em 'record'
         *  s indica o número do registro/slot corrente que será inserido na página
         *  next indica o byte que finaliza o campo que está sendo lido
         */
        for (s = 0, i = 0; i < data.length; s++)
        {
            byte[] record = new byte[SIZE_OF_RECORD];
            for (f = 0; i < data.length && f < SIZE_OF_RECORD; f++, i++)
            {
                record[f] = data[i];
            }
            if (!this.isNull(record))
            {
                this.put(s, record);
            }
        }
    }
    
    private boolean isNull(byte[] record)
    {
        for(int i = 0; i < record.length; i++)
        {
            if (Byte.compare(record[i], (byte) 0) != 0)
            {
                return false;
            }
        }
        return true;
    }
    
    public void putAll(HashMap<Integer, byte[]> m) 
    {
        if(m.size() == Page.RECORDS_PER_NODE)
        {
            this.isFNode = true;
            this.isINode = false;
        }
        else if(m.size() == Page.RECORDS_PER_NODE + 1) 
        {
            this.isFNode = false;
            this.isINode = true;
        }
        else {
            this.isFNode = false;
            this.isINode = false;
        }
        super.putAll(m);
    }
    
    
    /**
     * Insere um registro na página. Observe que, este método sobrescreve o
     * <tt>put(key, value)</tt> de <tt>HashMap</tt>, porque precisa fazer uma
     * verificação da quantidade de registros atualmente na página antes da
     * inserção.
     *
     * @param key o número do registro/slot
     * @param value registro
     * @return <tt>byte[][]</tt> inserido no mapa ou <tt>null</tt> se a página
     * estiver cheia.
     */
    @Override
    public byte[] put(Integer key, byte[] value)
    {
        if (this.containsKey(key))
        {
            return super.put(key, value);
        }
        if (this.isFNode)
        {
            return this.size() >= RECORDS_PER_NODE ? null : super.put(key, value);
        }
        else if (this.isINode) {
            return this.size() >= RECORDS_PER_NODE + 1 ? null : super.put(key, value);
        }
        return this.size() >= RECORDS_PER_PAGE ? null : super.put(key, value);
    }
    
    public byte[] set(Integer key, byte[] value)
    {
        return super.put(key, value);
    }
    
    public int writePage() throws FileNotFoundException, IOException
    {
        RandomAccessFile fi = new RandomAccessFile(pathFile + nameFile, "rw");
        int num;
        if (this.numberPage != -1)
        {
            this.write(fi, this.numberPage * Block.SIZE);
            num = this.numberPage;
        }
        else
        {
            num = this.writeNew(fi);
        }
        this.setDirty(false);
        return num;
    }
    
    private int writeNew(RandomAccessFile fi) throws IOException
    {
        long eof = fi.length();
        this.write(fi, eof);
        return (int) (eof / (long) Block.SIZE);
    }
    
    private void write(RandomAccessFile fi, long init) throws FileNotFoundException, IOException
    {
        // seta o write para aonde ele deve comecar a escrever
        fi.seek(init);
        // pega tuplas enquanto a pagina ainda nao acabou
//        byte c[];
//        for (Map.Entry<Integer, byte[]> e : this.entrySet())
//        {
//            c = e.getValue();
////            System.out.print("slot #" + e.getKey() + " > ");
////            for (int j = 0; j < c.length; j++){
////            char d = (char) c[j];
////            System.out.print(d);
////            }
//            fi.write(c);
//        }
        int size = this.size();
        for(int i = 0; i < size; i++)
        {
            fi.write(this.get(i));
            
        }
        fi.close();
    }

    public static void writePage(HashMap<Integer, byte[]> p, String pathFile, String nameFile, int numberPage) throws FileNotFoundException, IOException
    {
        // acessa o arquivo tanto pra ler quanto pra escrever
        RandomAccessFile fi = new RandomAccessFile(pathFile + nameFile, "rw");
        // pega aonde ele deve comecar a escrever
        long init = numberPage * Block.SIZE;
        // pega quantas tuplas há para serem escritas
        Set perc = p.keySet();
        // seta o write para aonde ele deve comecar a escrever
        fi.seek(init);
        // pega tuplas enquanto a pagina ainda nao acabou
        byte c[];
        for (Map.Entry<Integer, byte[]> e : p.entrySet())
        {
            c = e.getValue();
            fi.write(c);
        }
        fi.close();
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        RandomAccessFile fi = new RandomAccessFile(Manager.BASE_DIR + "NoFolhaTeste.txt", "rw");
        fi.seek(0);
        
        fi.writeBytes("");
    }

    public void refreshLastTimeUsed()
    {
        lastTimeUsed = System.nanoTime();
    }

    public long getLastTimeUsed()
    {
        return lastTimeUsed;
    }

    @Override
    public int compareTo(Object o)
    {
        // compara duas paginas, caso esta pagina tenha sido usada mais recentemente retorna 1
        // caso esta paagina tenha sido usada a mais tempo atras retorna -1
        // caso elas tenham sido pedidas juntas retorna 0
        Page otherpage = (Page) o;
        long i = this.getLastTimeUsed() - otherpage.getLastTimeUsed();
        if (i > 0)
        {
            return 1;
        }
        if (i < 0)
        {
            return -1;
        }
        return 0;
    }

    public void increasePinCount()
    {
        this.pinCount++;
    }

    public void reducePinCount()
    {
        this.pinCount--;
    }
    
    public void setNumberPage(int number)
    {
        this.numberPage = number;
    }
    
    public byte[] getValue (int numberSlot, int indexAttribute)
    {
        byte[] tuple = this.get(numberSlot);
        byte[] out = new byte[Page.SIZE_OF_FIELD];
        for(int i = Manager.SCHEMA_MAP.get(this.nameFile)[indexAttribute], j = 0; i < Page.SIZE_OF_FIELD; i++, j++)
        {
            out[j] = tuple[i];
        }
        return out;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters e Setters">
    public int getPinCount()
    {
        return pinCount;
    }
    
    public void setDirty(boolean a)
    {
        dirty = a;
    }
    
    public boolean isDirty()
    {
        return dirty;
    }
    
    public String getNameFile()
    {
        return nameFile;
    }
    
    public int getNumberPage()
    {
        return numberPage;
    }
    //</editor-fold>
}
