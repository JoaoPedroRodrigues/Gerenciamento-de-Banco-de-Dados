/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import bulkLoading.Builder;
import externalSorting.Originator;
import java.io.FileNotFoundException;
import java.io.IOException;
import model.disk.Block;
import model.disk.Rid;
import model.mem.Buffer;
import model.mem.BufferPair;
import model.mem.Page;
import model.pair.INodePair;

/**
 *
 * @author Guilherme
 */
public class BTree
{
    private int minOccupation;
    private int maxOccupation;
    private int indexAttribute;
    private InternalNode root;
    private int numPageRoot;
    
    public static String NAME_DATA_FILE;
    public static String NAME_BTREE_FILE;
    public static String PATH;
    
    public BTree(Buffer b, int min, int max, String path, String nameSourceFile, String nameOutFile, int indexAttribute) throws IOException
    {
        this.minOccupation = min;
        this.maxOccupation = max;
        this.indexAttribute = indexAttribute;
        
        BTree.PATH = path;
        BTree.NAME_BTREE_FILE = nameOutFile;
        BTree.NAME_DATA_FILE = nameSourceFile;
        
        Builder builder = new Builder(b);
        this.numPageRoot = builder.bulkLoading(indexAttribute, this.minOccupation, this.maxOccupation);
    }
    
    public boolean containsEntry(byte[] b)
    {
        return false;
    }
    
    /**
     * Retorna o rid de um registro a partir da busca no índice B+Tree.
     * 
     * @param key
     * @return 
     */
    public static Rid search(Buffer b, BTree index, byte[] key) throws FileNotFoundException, IOException
    {
        return searchRec(b, index.indexAttribute , key, new BufferPair(BTree.NAME_BTREE_FILE, index.numPageRoot));
    }
    
    private static Rid searchRec(Buffer b, int indexAttribute, byte[] key, BufferPair pNode) throws FileNotFoundException, IOException
    {
        /*
         * Caso base da recursão.
         * Se o nó da árvore a ser busca não é nó interno, então chama-se o método que faz a busca dentro do nó folha.
         */
        if (!Block.isInternalNode(NAME_BTREE_FILE, pNode.getNameFile(), pNode.getNumberPage(), InternalNode.NUM_INIT_BYTES_NULL))
        {
            return searchInFinalNode(b, indexAttribute, key, pNode);
        }
        
        // quando não é possível carregar o nó no buffer.
        if (b.loadPage(pNode) != 1)
        {
            return null; 
        }
        
        /*
         * Neste momento temos a página do nó no buffer.
         * Agora vamos converter a página em um nó, de tal maneira que os métodos de busca possam varrer as entradas do nó.
         */
        InternalNode inode = InternalNode.toInternalNode(PATH, pNode);
        INodePair[] ps = inode.getElements();
        
        /*
         * Busca 
         */
        BufferPair nextNode = null;
        for (INodePair p : ps)
        {
            if (Originator.compare(key,p.getEntry(), 0) == -1) // key é menor que a entrada corrente
            {
                int numberPage = p.getPointer();
                nextNode = new BufferPair(BTree.NAME_BTREE_FILE, numberPage);
                break;
            }
        }
        if (nextNode == null)
        {
           nextNode = new BufferPair(BTree.NAME_BTREE_FILE, inode.lastPointer);
        }
        return searchRec(b, indexAttribute, key, nextNode);
    }

    /**
     * Busca o rid de um registro em um nó folha.
     * 
     * @param key chave da busca
     * @param fNode <tt>BufferPair</tt> do nó folha, ou seja, o nome do arquivo e o número da página.
     * @return 
     */
    private static Rid searchInFinalNode(Buffer b, int indexAttribute, byte[] key, BufferPair fNode)
    {
        Page p = b.get(fNode);
        for(int i = 0; i < p.size(); i++)
        {
            if (Originator.compare(key, p.getValue(i, indexAttribute), 0) == 0)
            {
                return new Rid(fNode.getNumberPage(), i);
            }
        }
        return null;
    }
    
    
}
