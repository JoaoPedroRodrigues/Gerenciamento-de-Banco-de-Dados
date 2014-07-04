/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import model.pair.FNodePair;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import static model.Node.toInt;
import model.disk.Block;
import model.mem.Page;

/**
 *
 * @author Guilherme
 */
public class FinalNode extends Node
{
    
    private FNodePair[] entriesPointers;
    
    public static int NUM_LAST_BYTES_NULL;

    public FinalNode(FNodePair[] ps, int lP)
    {
        super(lP);
        this.entriesPointers = ps;
    }
    
    
    
    public byte[] getFirst() {
        
        return entriesPointers[0].getRecordKey();
        
    }
    
    /**
     * Converte este objeto para uma <tt>Page</tt>
     * 
     * @param path
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

        for (j = 0, i = 0; j < size; i++, j++)
        {
            map.put(i, entriesPointers[j].toArray());
        }

        int sizeOfRow = Page.SIZE_OF_FIELD + 9;
        
        int sizeOfNullBytes = (sizeOfRow + 2) * (Node.NUM_REG - map.size());
        
        if (sizeOfNullBytes > 0)
        {
            byte[] tempRow = new byte[sizeOfNullBytes];
            tempRow[sizeOfNullBytes - 2] = '\r';
            tempRow[sizeOfNullBytes - 1] = '\n';
            map.put(i, tempRow);
            i++;
        }
        
        byte[] lP = new byte[4 + FinalNode.NUM_LAST_BYTES_NULL + 2]; // 4 bytes para inteiro mais dois de quebra de linha
        byte[] temp = ByteBuffer.allocate(4).putInt(super.lastPointer).array();
        for (j = 0; j < 4; j++)
        {
            lP[j] = temp[j];
        }
        
        for (; j < lP.length - 2; j++)
        {
            lP[j] = 0;
        }
        lP[j] = '\r';
        lP[j + 1] = '\n';
        
        map.put(i, lP);
        return map;
    }
    
    /**
     * Converte uma <tt>Page</tt> em um nó intermediário <tt>FinalNode</tt>
     * 
     * @param path Caminho completo do diretório corrente
     * @param bTreeNameFile Nome do arquivo que contém a B+Tree
     * @param numberPage Número da página que identifica o nó
     * @return Um nó folha da B+Tree se a operação obtiver sucesso, 
     * ou <tt>null</tt> se a página <tt>p</tt> não for um nó folha,  
     * ou seja, se a operação falhar.
     */
    public static FinalNode toFinalNode(String path, String bTreeNameFile, int numberPage)
    {
        try
        {
            if (!Block.isInternalNode(path, bTreeNameFile, numberPage, InternalNode.NUM_INIT_BYTES_NULL))
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
    
    private static FinalNode parse(Block b)
    {
        int i = 0;
        int blockSize = b.getData().length - 4 - NUM_LAST_BYTES_NULL - 2;
        byte[] data = b.getData();
        ArrayList<ArrayList<byte[]>> rows = new ArrayList<>();
        
        for(int j; i < blockSize;)
        {
            ArrayList<byte[]> row = new ArrayList<>();
            byte[] pagePointer = new byte[4];
            byte[] slotPointer = new byte[4];
            byte[] entry = new byte[Page.SIZE_OF_FIELD];
            for (j = 0; j < 4; j++, i++)
            {
                pagePointer[j] = data[i];
            }
            row.add(pagePointer);
            for (j = 0; j < 4; j++, i++)
            {
                slotPointer[j] = data[i];
            }
            row.add(slotPointer);
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
        
        i = b.getData().length - 1 - NUM_LAST_BYTES_NULL - 2; // -2 é resultado da quebra de linha no final da página
        
        byte[] lPointer = new byte[4];
        for (int j = 3; j >= 0 ; i--, j--)
        {
            lPointer[j] = data[i];
        }
        
        int lastPointer = (lPointer[0]<<24)&0xff000000|(lPointer[1]<<16)&0xff0000|(lPointer[2]<<8)&0xff00|(lPointer[3]<<0)&0xff;
        
//        int lastPtr = toInt(lPointer);
        
        int numRow = rows.size();
        
        FNodePair[] pairs = new FNodePair[numRow];
        
        for (i = 0; i < numRow; i++) 
        {
            ArrayList<byte[]> row = rows.get(i);
            int page = (row.get(0)[0]<<24)&0xff000000|(row.get(0)[1]<<16)&0xff0000|(row.get(0)[2]<<8)&0xff00|(row.get(0)[3]<<0)&0xff;
            int slot = (row.get(1)[0]<<24)&0xff000000|(row.get(1)[1]<<16)&0xff0000|(row.get(1)[2]<<8)&0xff00|(row.get(1)[3]<<0)&0xff;
            FNodePair pair= new FNodePair(page, slot, row.get(2));
            pairs[i] = pair;
        }
        return new FinalNode(pairs, lastPointer);
    }
}
