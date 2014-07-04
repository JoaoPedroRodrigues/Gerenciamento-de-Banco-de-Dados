/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import model.pair.INodePair;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import model.disk.Block;
import model.mem.BufferPair;
import model.mem.Page;

/**
 *
 * @author Guilherme
 */
public class InternalNode extends Node
{
    private INodePair[] entriesPointers;
    
    
    public static int NUM_INIT_BYTES_NULL; // quantidade de bytes nulos no início da página do nó intermediário
    
    public InternalNode (INodePair[] ps, int lP)
    {
        super(lP);
        this.entriesPointers = ps;
    }
    
    /**
     * Converte este objeto para uma <tt>Page</tt>
     * 
     * @param PATH
     * @param numberPage
     * @return 
     */
    @Override
    public Page toPage(String path, int numberPage)
    {
        HashMap<Integer, byte[]> data = this.toHashMap();
        
        return new Page(data, path, BTree.NAME_BTREE_FILE, numberPage);
    }
    
    public HashMap<Integer, byte[]> toHashMap()
    {
        HashMap<Integer,byte[]> map = new HashMap<>();
        int size = entriesPointers.length; 
        int i, j;
        byte[] regNull = new byte[InternalNode.NUM_INIT_BYTES_NULL];
        
        regNull[InternalNode.NUM_INIT_BYTES_NULL - 2] = '\r';
        regNull[InternalNode.NUM_INIT_BYTES_NULL - 1] = '\n';
        map.put(0, regNull); // bytes iniciais nulos
        
        for (i = 1, j = 0; j < size; i++, j++)
        {
            map.put(i, entriesPointers[j].toArray());
        }
        int sizeOfRow = Page.SIZE_OF_FIELD + 7;
        
        int sizeOfNullBytes = sizeOfRow * (Node.NUM_REG + 1 - map.size());
        
        if (sizeOfNullBytes > 0)
        {
            byte[] tempRow = new byte[sizeOfNullBytes];            
            map.put(i, tempRow);
            i++;
        }
        
        byte[] lP = new byte[6]; // 4 bytes para inteiro mais dois de quebra de linha
        byte[] temp = ByteBuffer.allocate(4).putInt(super.lastPointer).array();
        for (j = 0; j < 4; j++)
        {
            lP[j] = temp[j];
        }

        lP[j] = '\r';
        lP[j + 1] = '\n';
        map.put(i, lP);
        return map;
    }
    
    
    public static InternalNode toInternalNode(String path, BufferPair bp)
    {
        return toInternalNode(path, bp.getNameFile(), bp.getNumberPage());
    }
    /**
     * Converte uma <tt>Page</tt> em um nó intermediário <tt>InternalNode</tt>
     * 
     * @param PATH Caminho completo do diretório corrente
     * @param bTreeNameFile Nome do arquivo que contém a B+Tree
     * @return Um nó interno da B+Tree se a operação obtiver sucesso, 
     * ou <tt>null</tt> se a página <tt>p</tt> não for um nó intermediário,  
     * ou seja, se a operação falhar.
     */
    public static InternalNode toInternalNode(String path, String bTreeNameFile, int numberPage)
    {
        try
        {
            if (Block.isInternalNode(path, bTreeNameFile, numberPage, InternalNode.NUM_INIT_BYTES_NULL))
            {
                Block b = new Block(path, bTreeNameFile, numberPage);
                return parse(b);
            }
        } catch (Exception ex)
        {
            return null;
        } 
        return null;
    }
    
    private static InternalNode parse(Block b)
    {
        int i = InternalNode.NUM_INIT_BYTES_NULL;
        int blockSize = b.getData().length - 6;
        byte[] data = b.getData();
        ArrayList<ArrayList<byte[]>> rows = new ArrayList<>();
        
        for(int j; i < blockSize;)
        {
            ArrayList<byte[]> row = new ArrayList<>();
            byte[] pointer = new byte[4];
            byte[] entry = new byte[Page.SIZE_OF_FIELD];
            for (j = 0; j < 4; j++, i++)
            {
                pointer[j] = data[i];
            }
            row.add(pointer);
            i++; // pula a vírgula
            
            if ((i == blockSize) || (Byte.compare(data[i], (byte) 0) == 0))
            {
                break;  // se a entrada for nula, o programa já leu os dados deste nó, só falta 
                        // o último ponteiro da direita.
            }
            
            for (j = 0; j < Page.SIZE_OF_FIELD; j++, i++)
            {
                entry[j] = data[i];
            }
            row.add(entry);
            i += 2; // pula os caracteres "\r\n"
            
            rows.add(row);
        }
        
        i = b.getData().length - 1 - 2; // -2 é resultado da quebra de linha no final da página
        
        byte[] lPointer = new byte[4];
        for (int j = 3; j >= 0 ; i--, j--)
        {
            lPointer[j] = data[i];
        }
        
        int lastPointer = (lPointer[0]<<24)&0xff000000|(lPointer[1]<<16)&0xff0000|(lPointer[2]<<8)&0xff00|(lPointer[3]<<0)&0xff;
        
//        int lastPtr = toInt(lPointer);
        
        int numRow = rows.size();
        
        INodePair[] pairs = new INodePair[numRow];
        
        for (i = 0; i < numRow; i++) 
        {
            ArrayList<byte[]> row = rows.get(i);
            int page = (row.get(0)[0]<<24)&0xff000000|(row.get(0)[1]<<16)&0xff0000|(row.get(0)[2]<<8)&0xff00|(row.get(0)[3]<<0)&0xff;
            INodePair pair= new INodePair(row.get(1), page);
            pairs[i] = pair;
        }
        return new InternalNode(pairs, lastPointer);
    }
    
    public int getLastPointer() {
        return lastPointer;
    }
    
    public INodePair[] getElements() {
        return entriesPointers;
    }
    public void setLastPointer(int lp) {
        lastPointer = lp;
    }
    public void setElements(INodePair[] entries) {
        entriesPointers = entries;
    }
}
