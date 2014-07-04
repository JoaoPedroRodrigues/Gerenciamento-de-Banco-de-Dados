/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import model.mem.Page;

/**
 *
 * @author Guilherme
 */
public abstract class Node
{
    protected int lastPointer;
    
    public static String NAME_FILE;
    public static int NUM_REG;
    
    public Node(int lPointer)
    {
        this.lastPointer = lPointer;
    }
    
    protected static int toInt(byte[] bs)
    {
        int res = bs[0] - 48;
        for (int i = 1; i < 4; i++)
        {
            res += (bs[i] - 48) * Math.pow(10, i);
        }
        return res;
    }
    
    public abstract Page toPage(String path, int numberPage);
    
}
