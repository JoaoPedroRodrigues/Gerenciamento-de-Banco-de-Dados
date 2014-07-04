/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.pair;

import java.nio.ByteBuffer;
import java.util.Arrays;
import model.mem.Page;

/**
 *
 * @author Guilherme
 */
public class INodePair
{
    private byte[] entry;
    private int numberPage;
    
    /**
     * Constrói um objeto <tt>INodePair</tt> para uso no <tt>InternalNode</tt>
     * 
     * @param entry valor da entrada
     * @param pointer ponteiro para um nó da árvore
     */
    public INodePair(byte[] entry, int pointer)
    {
        this.entry = entry;
        this.numberPage = pointer;
    }
    
    public INodePair()
    {
        this.entry = new byte[Page.SIZE_OF_FIELD];
        this.numberPage = 0;
    }
    
    public byte[] toArray()
    {
        byte[] nPage = ByteBuffer.allocate(4).putInt(numberPage).array();
        byte[] res = new byte[nPage.length + 1 + entry.length + 2];
        int i;
        for (i = 0; i < 4; i++)
        {
            res[i] = nPage[i];
        }
        res[i] = ',';
        i++;
        for (int j = 0; j < entry.length; j++, i++)
        {
            res[i] = entry[j];
        }
        res[i] = '\r';
        res[i + 1] = '\n';
        
        return res;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + Arrays.hashCode(this.entry);
        hash = 67 * hash + this.numberPage;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final INodePair other = (INodePair) obj;
        if (!Arrays.equals(this.entry, other.entry))
        {
            return false;
        }
        if (this.numberPage != other.numberPage)
        {
            return false;
        }
        return true;
    }
    
    public int getPointer() {
        return numberPage;
    }
    public byte [] getEntry() {
        return entry;
    }
}
