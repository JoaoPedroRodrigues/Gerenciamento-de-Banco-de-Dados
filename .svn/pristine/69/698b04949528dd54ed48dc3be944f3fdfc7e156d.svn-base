/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bulkLoading;

import externalSorting.Originator;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JOptionPane;
import model.BTree;
import model.pair.FNodePair;
import model.FinalNode;
import model.pair.INodePair;
import model.InternalNode;
import model.disk.Block;
import model.mem.Buffer;
import model.mem.Page;

/**
 *
 * @author Guilherme
 */
public class Builder
{
    private Buffer buffer;
    private int numfolhas;
    private int nivelBtree;
    private InternalNode raiz;
    private int numpagraiz;
    
    /**
     * Constrói um índice estrutura em B+Tree usando o algoritmo de bulkLoading.
     * 
     * @param b o buffer pool
     * @param PATH caminho do arquivo
     * @param nameFile nome do arquivo da relação a ser ordenada
     * @param indexAttribute índice do atributo chave a ser utilizado
     * @return o nó raiz da B+Tree
     */
    public int bulkLoading(int indexAttribute, int minOcc, int maxOcc) throws IOException
    {
//        Originator o = new Originator(this.buffer);
//        o.externalSort(BTree.PATH, BTree.NAME_DATA_FILE, indexAttribute); // faz a ordenação completa da relação
        
        criaFolhas(minOcc,maxOcc,indexAttribute);
        
        for(int i=1;i<numfolhas;i++) {
            
            FinalNode fin = FinalNode.toFinalNode(BTree.PATH, BTree.NAME_BTREE_FILE, i);
            
            byte [] c = fin.getFirst();
            
            insertNode(c,1,minOcc,i-1,i,maxOcc);
            
        }
        
        return numpagraiz;
    }

    public void insertNode(byte []c, int nivel,int minOcc, int pagEsq, int pgDir,int maxOcc) throws FileNotFoundException, IOException {
        if(nivel > nivelBtree) {
            Page z = buffer.newPage(BTree.NAME_BTREE_FILE);
            INodePair[] inp = new INodePair[maxOcc]; // INodePair[] inp = new INodePair[2 * minOcc];
            nivelBtree++;
            inp[0] = new INodePair(c,pagEsq);
            for (int i = 1; i < maxOcc; i++)
            {
                inp[i] = new INodePair();
            }
            InternalNode in = new InternalNode(inp,pgDir);
            raiz=in;
            numpagraiz = z.getNumberPage();
            Page n = in.toPage(BTree.NAME_BTREE_FILE, z.getNumberPage());
            z.putAll(n);
            buffer.writeNewPage(z);
            buffer.freePage(BTree.NAME_BTREE_FILE, z.getNumberPage());
            
        }
        else {
            InternalNode in = lastNodeAt(nivel);
            int numpgin = pageofLastNodeAt(nivel);
            INodePair[] inp = in.getElements();
            if(inp.length != maxOcc) {
                int i;
                INodePair[] novainp = new INodePair[maxOcc];
                for(i=0;i<inp.length;i++) {
                    novainp[i] = inp[i];
                }
                for(i=inp.length;i<maxOcc;i++) {
                    novainp[i] = new INodePair();
                }
                inp = novainp;
            }
            if(length(inp,maxOcc)<=minOcc) {
                inp[length(inp,maxOcc)] = new INodePair(c,in.getLastPointer());
                in.setLastPointer(pgDir);
                in.setElements(inp);
                
                
                Page z = in.toPage(BTree.NAME_BTREE_FILE, numpgin);
                buffer.loadPage(BTree.NAME_BTREE_FILE,numpgin);
                Page n = buffer.get(BTree.NAME_BTREE_FILE, numpgin);
                n.putAll(z);
                buffer.writeNewPage(n);
                buffer.freePage(BTree.NAME_BTREE_FILE, numpgin);
            }
            else {
                int lastp = in.getLastPointer();
                INodePair inult = inp[length(inp,maxOcc)-1];
                inp[length(inp,maxOcc)-1] = new INodePair();
                in.setLastPointer(inult.getPointer());
                in.setElements(inp);
                
                
                Page z = in.toPage(BTree.NAME_BTREE_FILE, numpgin);
                buffer.loadPage(BTree.NAME_BTREE_FILE,numpgin);
                Page n = buffer.get(BTree.NAME_BTREE_FILE, numpgin);
                n.putAll(z);
                buffer.writeNewPage(n);
                buffer.freePage(BTree.NAME_BTREE_FILE, numpgin);
                
                
                z = buffer.newPage(BTree.NAME_BTREE_FILE);
                inp = new INodePair[maxOcc];
                inp[0] = new INodePair(c,lastp);
                in = new InternalNode(inp,pgDir);
                
                for (int i = 1; i < maxOcc; i++)
                {
                    inp[i] = new INodePair();
                }
                
                int numpgnewno = z.getNumberPage();
                n = in.toPage(BTree.NAME_BTREE_FILE,numpgnewno);
                z.putAll(n);
                buffer.writeNewPage(z);
                buffer.freePage(BTree.NAME_BTREE_FILE,numpgnewno);
                
                insertNode(inult.getEntry(),nivel+1,minOcc,numpgin,numpgnewno,maxOcc);
            }
            
        }
        
        
    }
    
    
    public InternalNode lastNodeAt(int nivel) {
        int niv = nivelBtree;
        InternalNode in = raiz;
        while(nivel != niv) {
            int prox = in.getLastPointer();
            in = InternalNode.toInternalNode(BTree.PATH, BTree.NAME_BTREE_FILE, prox);
            niv--;
        }
        return in;
    }
    
    public int pageofLastNodeAt(int nivel) {
        int niv = nivelBtree;
        InternalNode in = raiz;
        int prox=numpagraiz;
        while(nivel != niv) {
            prox = in.getLastPointer();
            in = InternalNode.toInternalNode(BTree.PATH, BTree.NAME_BTREE_FILE, prox);
            niv--;
        }
        return prox;
    }
    
    
    public Builder(Buffer b)
    {
        this.buffer = b;
        numfolhas =0;
        nivelBtree=0;
        raiz=null;
    }
    
    private void criaFolhas(int minOcc, int maxOcc, int indexAttribute) throws FileNotFoundException, IOException {
        int k = 0;
        FNodePair[] fnod = new FNodePair[maxOcc];
        Page z = buffer.newPage(BTree.NAME_BTREE_FILE);
        for(int i=0;true;i++) {
            // testo se o arquivo de dados chegou ao fim
            if(-1==buffer.loadPage(BTree.NAME_DATA_FILE,i))
                break;
            
            //pega 1 pagina do arquivo de dados
            Page p = buffer.get(BTree.NAME_DATA_FILE,i);
            int size = p.size();
            // pra cada registro da pagina
            for(int j=0;j< size;j++) {
                // se a ocupacao do fnod tiver chegado a ocupacao minima, entao deve ser guardado esse nó no arquivo da btre
                // e deve ser criada uma nova pagina da btree
                if(k==minOcc) {
                    for(;k<maxOcc;k++) {
                        fnod[k] = new FNodePair();
                    }
                    FinalNode folha = new FinalNode(fnod,z.getNumberPage()+1);
                    Page n = folha.toPage(BTree.NAME_BTREE_FILE,z.getNumberPage());
                    z.putAll(n);
                    buffer.writeNewPage(z);
                    buffer.freePage(BTree.NAME_BTREE_FILE, z.getNumberPage());
                    z = buffer.newPage(BTree.NAME_BTREE_FILE);
                    
                    numfolhas++;
                    
                    fnod = new FNodePair[maxOcc];
                    k=0;
                }
                byte []key = buffer.getValue(BTree.NAME_DATA_FILE, i, j, indexAttribute);
                fnod[k] = new FNodePair(i,j,key);
                k++;
                
            }
            buffer.freePage(BTree.NAME_DATA_FILE,i);
        }
        for(;k<maxOcc;k++) {
            fnod[k] = new FNodePair();
        }
        
        FinalNode folha = new FinalNode(fnod,-1);
        Page n = folha.toPage(BTree.NAME_BTREE_FILE,z.getNumberPage());
        z.putAll(n);
        buffer.writeNewPage(z);
        
        numfolhas++;
        
    }

    private int length(INodePair[] inp, int maxOcc) {
        boolean quebrei;
        int i;
        for(i=0;i<maxOcc;i++) {
            quebrei=false;
            byte[] c = inp[i].getEntry();
            for(int j=0;j<Page.SIZE_OF_FIELD;j++) {
                if(Byte.compare(c[j], (byte) 0)!=0) {
                    quebrei = true;
                    break;
                }
            }
            if(!quebrei)
                break;
        }
        return i;
        
        
    }
    
    
    
    
}
