/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.pair;

import java.nio.ByteBuffer;
import model.mem.Page;

/**
 *
 * @author Guilherme
 */
public class FNodePair
{
    private byte[] recordKey;
    private int numberPage;
    private int numberRecord;
    
    /**
     * Constrói um objeto <tt>FNodePair</tt> para uso no <tt>FinalNode</tt>
     * 
     * @param entry valor da entrada
     * @param idSlot ponteiro para um nó da árvore
     */
    public FNodePair(int page, int record, byte[] key)
    {
        this.numberPage = page;
        this.numberRecord = record;
        this.recordKey = key;
    }

    public FNodePair() {
        this.recordKey = new byte[Page.SIZE_OF_FIELD];
        this.numberPage = 0;
        this.numberRecord = 0;
    }
    
    public byte[] toArray()
    {
        byte[] nPage = ByteBuffer.allocate(4).putInt(numberPage).array();
        byte[] nSlot = ByteBuffer.allocate(4).putInt(numberRecord).array();
        byte[] res = new byte[nPage.length + nSlot.length + 1 + recordKey.length + 2];
        int i, j;
        for (i = 0; i < 4; i++)
        {
            res[i] = nPage[i];
        }
        for (j = 0; j < 4; i++, j++)
        {
            res[i] = nSlot[j];
        }
        res[i] = ',';
        i++;
        for (j = 0; j < recordKey.length; j++, i++)
        {
            res[i] = recordKey[j];
        }
        res[i] = '\r';
        res[i + 1] = '\n';
        
        return res;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Getters, setters, equals e hashCode">
    public byte[] getRecordKey()
    {
        return recordKey;
    }
    
    public void setRecordKey(byte[] recordKey)
    {
        this.recordKey = recordKey;
    }
    
    public int getNumberPage()
    {
        return numberPage;
    }
    
    public void setNumberPage(int numberPage)
    {
        this.numberPage = numberPage;
    }
    
    public int getNumberRecord()
    {
        return numberRecord;
    }
    
    public void setNumberRecord(int numberRecord)
    {
        this.numberRecord = numberRecord;
    }
    
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + this.numberPage;
        hash = 53 * hash + this.numberRecord;
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
        final FNodePair other = (FNodePair) obj;
        if (this.numberPage != other.numberPage)
        {
            return false;
        }
        if (this.numberRecord != other.numberRecord)
        {
            return false;
        }
        return true;
    }
    //</editor-fold>
}
