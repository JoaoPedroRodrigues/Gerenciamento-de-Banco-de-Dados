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
import java.util.HashMap;
import java.util.PriorityQueue;
import model.disk.Block;

/**
 * Representa o buffer pool.
 *
 * @author Alana
 * @author Guilherme Alves
 * @author Guilherme Nunes
 * @author João Pedro Galvão
 * 
 * @see HashMap
 */
public class Buffer extends HashMap<BufferPair, Page>
{
    private int pagesNum;
    private String currentDirectory;
    private PriorityQueue<Page> removepolicy; // heap onde a primeira posicao é a proxima pagina a ser removida do buffer

    public Buffer(String basePath, int numberOfPages, int sizeOfPage, boolean recordsType)
    {
        this.currentDirectory = basePath;
        this.pagesNum = numberOfPages;
        removepolicy = new PriorityQueue<>();
    }

    /* 
     * Nao esquecer de sempre que precisar de uma pagina deve pedir ela pelo allocate e sempre que parar de usar uma pagina
     * deve informar usando o free
     */
    
    /**
     *
     * @param nameFile nome da relação
     * @param numberPage número da página
     * @return 1 se a pagina já estiver no buffer, 0 se o espaco foi alocado ou
     * -1 se nao foi possivel alocar espaco (buffer cheio)
     * @throws FileNotFoundException
     * @throws IOException
     */
    public int allocatePage(String nameFile, int numberPage) throws FileNotFoundException, IOException
    {

        BufferPair key = new BufferPair(nameFile, numberPage);
        Page p = this.get(key);
        // testa se a pagina já esta no buffer, caso ela já esteja, caso o seu pin count fosse zero
        // tira ela da fila pra poder remover e incrementa seu pin count
        // além de atualizar o tempo em que ela foi usada por ultimo
        if (p != null)
        {
            if (p.getPinCount() == 0)
            {
                removepolicy.remove(p);
            }
            p.increasePinCount();
            p.refreshLastTimeUsed();
            return 1;
        }
        // caso ainda haja espaco no buffer, retorna 0
        if (this.size() < this.pagesNum)
        {
            return 0;
        }
        Page free2remove = removepolicy.poll();
        // se nao houver paginas para serem removidas retorna -1
        if (free2remove == null)
        {
            return -1;
        }
        // testa se a pagina que pode ser removida já foi escrita no HD, caso nao tenha, escreve
        if (free2remove.isDirty())
        {
            this.writePage(free2remove.getNameFile(), free2remove.getNumberPage());
        }
        // remove a pagina que poderia ser removida do buffer e retorna 0
        this.remove(new BufferPair(free2remove.getNameFile(), free2remove.getNumberPage()));
        return 0;
    }
    
    private boolean allocatePage() throws FileNotFoundException, IOException
    {
        if (this.size() < this.pagesNum)
        {
            return true;
        }
        Page free2remove = removepolicy.poll();
        // se nao houver paginas para serem removidas retorna -1
        if (free2remove == null)
        {
            return false;
        }
        // testa se a pagina que pode ser removida já foi escrita no HD, caso nao tenha, escreve
        if (free2remove.isDirty())
        {
            this.writePage(free2remove.getNameFile(), free2remove.getNumberPage());
        }
        // remove a pagina que poderia ser removida do buffer e retorna 0
        this.remove(new BufferPair(free2remove.getNameFile(), free2remove.getNumberPage()));
        return true;
    }

    /**
     * Serve pra quando o usuario deixa de usar a pagina, caso o pin count passe
     * a ser zero coloca na fila de paginas que podem ser removidas
     *
     * @param nameFile
     * @param numberPage
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void freePage(String nameFile, int numberPage) throws FileNotFoundException, IOException
    {
        BufferPair key = new BufferPair(nameFile, numberPage);
        Page p = this.get(key);
        p.reducePinCount();
        if (p.getPinCount() == 0)
        {
            removepolicy.add(p);
        }
    }

    /**
     * Lê uma nova pagina para o buffer
     *
     * @param nameFile
     * @param numberPage
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Page readPage(String nameFile, int numberPage) throws FileNotFoundException, IOException
    {
        Page p = new Page(this.currentDirectory, nameFile, numberPage);
        if (p.notFound){
            return p;
        }
        BufferPair key = new BufferPair(nameFile, numberPage);
        super.put(key, p);
        return p;
    }

    /**
     * Escreve uma página que está no buffer no disco.
     *
     *
     * @param nameFile
     * @param numberPage
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean writePage(String nameFile, int numberPage) throws FileNotFoundException, IOException
    {
        Page p = this.get(new BufferPair(nameFile, numberPage));
        // testa se a pagina esta no buffer
        if (p == null)
        {
            return false;
        }
        // escreve a pagina em disco setando seu dirtybit pra false
        p.writePage();
        return true;
    }
    
    /**
     * Grava uma nova página no arquivo.
     * 
     * @param p Página de output
     * @return <tt>true</tt> se a operção obtiver êxito ou <tt>false</tt> se 
     * ocorrer alguma falha.
     */
    public boolean writeNewPage(Page p)
    {
        try
        {
            p.writePage();
        } catch (Exception ex)
        {
            return false;
        }
        return true;
    }

    public int loadPage(BufferPair bp) throws FileNotFoundException, IOException
    {
        return this.loadPage(bp.getNameFile(), bp.getNumberPage());
    }
    
    /**
     * Carrega uma página no buffer.
     *
     * @param nameFile
     * @param numberPage
     * @return <tt>1</tt> se a operação for bem sucedida ou
     * <tt>0</tt> se o buffer estiver cheio.
     * <tt>-1</tt> se for uma pagina inexistente ou erro
     * @throws FileNotFoundException
     * @throws IOException
     */
    public int loadPage(String nameFile, int numberPage) throws FileNotFoundException, IOException
    {
        int res = this.allocatePage(nameFile, numberPage);
        if (res == 1)
        {
            return 1;
        } else if (res == 0)
        {
            try {
                Page p = this.readPage(nameFile, numberPage);
                if (p.notFound){
                    return -1;
                }
                return 1;
            } catch (IOException iOException) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * Grava todas as páginas alteradas e que não foram salvas do buffer em disco.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void freeBuffer() throws FileNotFoundException, IOException
    {
        for (BufferPair p : this.keySet())
        {
            Page page = this.get(p);
            if (page.isDirty())
            {
                page.writePage();
                this.freePage(currentDirectory, pagesNum);
            }
        }
    }

    /**
     * Obtém o valor de um atributo de uma tupla específica.
     * 
     * @param p
     * @param record
     * @param indexAttribute
     * @return um <tt>byte[]</tt> que representa o valor do atributo de interesse da tupla alvo
     */
    public byte[] getValue(BufferPair p, int record, int indexAttribute)
    {
        Page pg = this.get(p);
        if (pg != null)
        {
            byte[] tuple = pg.get(record);
            byte[] value = new byte[Page.SIZE_OF_FIELD];
            for (int i = Manager.SCHEMA_MAP.get(p.getNameFile())[indexAttribute], j = 0; j < Page.SIZE_OF_FIELD; i++, j++)
            {
                value[j] = tuple[i];
            }
            return value;
        }
        return null;
    }
    
    /**
     * Obtém o valor de um atributo de uma tupla específica.
     * 
     * @param nameFile nome da relação
     * @param numberPage número da página
     * @param record número da tupla/record da página
     * @param indexAttribute índice do atributo de interesse
     * @return um <tt>byte[]</tt> que representa o valor do atributo de interesse da tupla alvo
     */
    public byte[] getValue(String nameFile, int numberPage, int record, int indexAttribute)
    {
        BufferPair p = new BufferPair(nameFile, numberPage);
        return this.getValue(p, record, indexAttribute);
    }

    /**
     * Retorna uma tupla de uma determinada página.
     *
     * @param nameFile nome da relação
     * @param numberPage número da página
     * @param record número do registro dentro da página
     * @return um tupla da relação solicitada.
     */
    public byte[] getTuple(String nameFile, int numberPage, int record)
    {
        BufferPair p = new BufferPair(nameFile, numberPage);
        return this.getTuple(p, record);
    }

    /**
     * Retorna uma tupla de
     *
     * @param p
     * @param record
     * @return
     */
    public byte[] getTuple(BufferPair p, int record)
    {
        Page pg = this.get(p);
        if (pg != null)
        {
            return pg.get(record);
        }
        return null;
    }
    
    public Page newPage(String nameFile) throws FileNotFoundException, IOException
    {
        if (!this.allocatePage())
        {
            return null;
        }
        Page p;
        int numP = Block.computeNewBlockNumber(currentDirectory, nameFile);
        p = new Page(currentDirectory, nameFile, numP);

        super.put(new BufferPair(nameFile, numP), p);
        
        return p;
    }
    
    
    
    /**
     * Obtém uma página que está no buffer a partir do nome da tabela/relação
     * e número da página;
     * 
     * @param nameRelation
     * @param nP
     * @return 
     */
    public Page get(String nameRelation, int nP)
    {
        BufferPair b = new BufferPair(nameRelation, nP);
        return this.get(b);
    }
    
    /**
     * Calcula a quantidade páginas totalmente prenchidas de um
     * determinado arquivo
     * 
     * @param nameRelation nome do arquivo
     * @return o número de páginas totais do arquivo ou -1 se houver 
     * falha na operação.
     */
    public int getNumberPagesOfFile(String nameRelation) throws FileNotFoundException, IOException
    {
        long len;
        len = Block.getLength(this.currentDirectory, nameRelation);
        
        long n = len / (long) Page.SIZE_OF_RECORD;
        
        int r = ((int) n) / Page.RECORDS_PER_PAGE;
        
        int z = ((int)n) % Page.RECORDS_PER_PAGE;
        
        // antigo codigo
        //n /= (long) Page.RECORDS_PER_PAGE;
        
        if(z == 0)
            return r;
        r++;
        return r;
        
        
        
        //return (int) n;
    }

    //<editor-fold defaultstate="collapsed" desc="Método(s) obsoleto(s)">
    /**
     * Insere uma página em um frame livre do buffer.
     *
     * @param key um objeto <tt>BufferPair</tt> que contenha o nome do arquivo e
     * o número da página (lembre-se que o buffer armazenará páginas de arquivos
     * diferentes, por isso a necessidade de um objeto BufferPair)
     * @param value página
     * @return <tt>Page</tt> inserido no mapa ou <tt>null</tt> se a página
     * estiver cheia.
     *
     * @deprecated 
     */
    @Override
    public Page put(BufferPair key, Page value)
    {
        return this.size() >= this.pagesNum ? null : super.put(key, value);
    }
    //</editor-fold>
}
