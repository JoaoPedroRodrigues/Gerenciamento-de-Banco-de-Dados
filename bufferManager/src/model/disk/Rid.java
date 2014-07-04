/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.disk;

/**
 * Representa o rid, ou seja, uma dupla (página, slot) de um registro
 * @author Guilherme
 */
public class Rid
{
    private int numberPage;
    private int numberSlot;

    public Rid(int numberPage, int numberSlot)
    {
        this.numberPage = numberPage;
        this.numberSlot = numberSlot;
    }

    public int getNumberPage()
    {
        return numberPage;
    }

    public int getNumberSlot()
    {
        return numberSlot;
    }
}
