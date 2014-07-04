/*
 * Federal University of Uberlândia
 * Computer Science Department
 * 
 * Management Database - 2013/1
 * Project: Index Nested Loop Join with BTree
 * 
 */
package model.mem;

import java.util.Objects;

/**
 * Representa genericamente um par de valores.
 * 
 * @author Guilherme
 */
public class BufferPair
{
    private String nameFile;
    private int numberPage;
    
    /**
     * Constrói um objeto <tt>BufferPair</tt> para uso em <tt>Buffer</tt>.
     * 
     * @param file nome do arquivo
     * @param number número da página/bloco
     */
    
    public BufferPair(String file)
    {
        this.nameFile = file;
    }
    
    public BufferPair(String file, int number)
    {
        this.nameFile = file;
        this.numberPage = number;
    }
    
    public String getNameFile()
    {
        return nameFile;
    }

    public int getNumberPage()
    {
        return numberPage;
    }
    
    public void setNumberPage(int number)
    {
        this.numberPage = number;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.nameFile);
        hash = 97 * hash + this.numberPage;
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
        final BufferPair other = (BufferPair) obj;
        if (!Objects.equals(this.nameFile, other.nameFile))
        {
            return false;
        }
        if (this.numberPage != other.numberPage)
        {
            return false;
        }
        return true;
    }
    
}
