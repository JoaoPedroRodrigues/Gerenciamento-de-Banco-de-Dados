/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package externalSorting;

import control.Manager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import model.mem.Buffer;
import model.mem.BufferPair;
import model.mem.Page;
import java.lang.Math;


/**
 *
 * @author Guilherme
 */
public class Originator
{
    private String path;
    private String sourceFile;
    private Buffer buffer;
    
    public Originator(Buffer b)
    {
        this.buffer = b;
    }
    
    /**
     * Implementa o algoritmo de ordenação externa
     * 
     * @param path caminho da tabela no disco
     * @param nameSourceFile  nome da tabela a ser ordenada
     * @param indexAttribute indice correspondente a qual atributo deve ser chave da ordenação
     */
    public void externalSort(String path, String nameSourceFile, int indexAttribute, String newFile) throws FileNotFoundException, IOException
    {
        this.path = path;
        this.sourceFile = nameSourceFile;
               
        int subarq = internalSort(path, nameSourceFile, indexAttribute);

        int buffer = Manager.NUMBER_OF_FRAMES;
        
        Page output = this.buffer.newPage(newFile);
        
        //Numero de Paginas de cada subarquivo da etapa 0
        int pagSubarq = buffer;
       
  
        ArrayList<Integer> pags = new ArrayList(), 
                           limiteSubarq = new ArrayList(),
                           topo = new ArrayList();
        int next = 0;
        int count = 0, count2 = 0;
        boolean op = false;
        
        double etapas = Math.log(buffer - 1);
        if (etapas < 1)
            etapas = 1;
        
        if(Math.log(subarq)%etapas != 0)
            etapas = (Math.log(subarq)/etapas)+1;
        else
            etapas = (Math.log(subarq)/etapas);

        //Numero de Blocos que a Etapa vai ter
        int blocos = subarq/(buffer-1); //blocos -> numero de blocos que esta etapa vai construir
        if (subarq%(buffer-1)!=0) //TETO
            blocos++;
        
        //Etapa 1
        for(int j = 0; j < blocos; j++)
        {
            //Carrega a primeira pagina de cada subarquivo de um bloco j
            int pagBloco = pagSubarq*(buffer-1);
            for (int k = j*pagBloco; k < (j+1)*pagBloco; k += pagSubarq)
            {
                if(this.buffer.loadPage(nameSourceFile, k) == -1)   
                    pags.add(-1);
                else
                    pags.add(k);
                
                limiteSubarq.add(k+pagSubarq);
                topo.add(0);
            }

            byte[] menor = new byte[10], t1 = new byte[10];

            while(true){

                menor[0] = Byte.MAX_VALUE;
                op = false;
                count2 = 0; //Marca quantos subarquivos chegaram ao final
                for(int l = 0; l < buffer-1; l++)
                {
                    if(pags.get(l)==-1) //Final de Um Subarquivo
                    {
                        count2++;
                        if(count2 == buffer-1){ //Termina ordenação do bloco
                           op = true;
                           break;
                        }
                        continue;
                    }

                    t1 = this.buffer.getTuple(sourceFile, pags.get(l), topo.get(l));
                    if(t1 == null) //Significa que o subarquivo acabou;
                    {
                        pags.set(l, -1);
                        continue;
                    }
                    if(compare(t1, menor, indexAttribute)< 0)
                    {
                        menor = t1;
                        next = l; //Marca de qual pagina a tupla sera colocada no output
                    } 
                }

                if(op) //Termina Ordenação do bloco
                {
                    this.buffer.writePage(newFile, output.getNumberPage());
                    this.buffer.freePage(newFile, output.getNumberPage());
                    break;
                }

                output.put(count, menor.clone()); //Coloca menor no output
                count++;

                if(output.size() == Page.RECORDS_PER_PAGE) //Verifica se a pagina de output eta cheia
                {
                    int numberPageOut = output.getNumberPage();
                    this.buffer.writePage(newFile, numberPageOut);
                    this.buffer.freePage(newFile, numberPageOut);
                    output = this.buffer.newPage(newFile);
                    count = 0;
                }

                topo.set(next, topo.get(next)+1); //Incrementa a tupla que sera usada 

                if(topo.get(next) == Page.RECORDS_PER_PAGE) //Se a pagina tiver acabado
                {
                    this.buffer.freePage(nameSourceFile, pags.get(next)); //Libera a memoria
                    pags.set(next, pags.get(next)+1); //incrementa o numero da pagina
                    topo.set(next, 0); //volta para a primeira tupla
                    if (pags.get(next) == limiteSubarq.get(next)) //Se acabou o subarquivo
                        pags.set(next,-1); 
                    else
                        this.buffer.loadPage(nameSourceFile, pags.get(next));
                }
            } 
            
            pags = new ArrayList();
            topo = new ArrayList();
            limiteSubarq = new ArrayList();         
        }
        
        if(etapas < 2)
            return;
        
        String origem;
        String destino = new String();
               
        //Etapas 2 em diante
        for (int i=1; i<etapas; i++){
            
            subarq = blocos;
            blocos = subarq/(buffer-1);
            if (subarq%(buffer-1)!=0) //TETO
                blocos++;
            pagSubarq *= (buffer-1);
            int pagBloco = pagSubarq*(buffer-1);
                       
            if(i%2 == 0){
                origem = nameSourceFile;
                destino = newFile;
            }
            else{
                origem = newFile;
                destino = nameSourceFile;
            }
            
            int outNumber = 0;
                
            this.buffer.loadPage(destino, outNumber);   
                
            for(int j = 0; j < blocos; j++){
                
                pags = new ArrayList();
                topo = new ArrayList();
                limiteSubarq = new ArrayList();
                count = 0; //tupla corrente da output
                count2 = 0; // numero de subarquivos que chegou ao fim
                op = false; // termino da ordenação do bloco
                
                //Carrega a primeira pagina de cada subarquivo de um bloco j
                for (int k = j*pagBloco; k < (j+1)*pagBloco; k += pagSubarq)
                {
                    if(this.buffer.loadPage(origem, k) == -1)
                        pags.add(-1);
                    else
                        pags.add(k);

                    limiteSubarq.add(k+pagSubarq);
                    topo.add(0);
                }
                
                byte[] menor = new byte[10];
                byte [] t1;
                
                count2 = 0; //Marca quantos subarquivos chegaram ao final
                
                while(true){
                     
                     op = false;
                     menor[0] = Byte.MAX_VALUE;
                     for(int l = 0; l < buffer-1; l++){
                                              
                        if(count2 == (buffer-1)){
                           op = true; //Fim do bloco
                           break;
                        }
                        
                        if(pags.get(l) == -1){
                            continue;
                        }
                        
                        t1 = this.buffer.getTuple(origem, pags.get(l), topo.get(l));
                        
                        if(t1 == null){
                            pags.set(l,-1);
                            continue;
                        }
                        
                        if(Originator.compare(t1, menor, indexAttribute)< 0)
                        {
                            menor = t1;
                            next = l; //Marca de qual pagina a tupla sera colocada no output
                        } 
                    }
                    
                    if(op) //Termina Ordenação do bloco
                    {
                        this.buffer.writePage(destino, outNumber);
                        this.buffer.freePage(destino, outNumber);
                        break;
                    }
                    
                    this.buffer.get(destino, outNumber).set(count, menor.clone()); //Coloca menor no output
                    count++;
                    
                    if(count ==  this.buffer.get(destino, outNumber).size()) //Verifica se a pagina de output esta cheia
                    {              
                        this.buffer.writePage(destino, outNumber);
                        this.buffer.freePage(destino, outNumber);
                        outNumber++;
                        if(this.buffer.loadPage(destino, outNumber) == -1)
                            op = true;  
                        else
                            count = 0; 
                    }
                    
                    if(op){
                        this.buffer.freeBuffer();
                        break;
                    }
                    
                    
                    topo.set(next, topo.get(next)+1); //Incrementa a tupla que sera usada 
                    
                    if(count2 == buffer-1){
                        this.buffer.freeBuffer();
                        break;
                    }
                    
                    int tamanho = this.buffer.get(origem, pags.get(next)).size();
                                      
                    if(topo.get(next) == tamanho) //Se a pagina tiver acabado
                    {
                        if(pags.get(next)==-1)
                            break;
                        
                        this.buffer.freePage(origem, pags.get(next)); //Libera a memoria
                       
                        pags.set(next, pags.get(next)+1); //incrementa o numero da pagina
                        topo.set(next, 0); //volta para a primeira tupla
                        
                        if (pags.get(next) == limiteSubarq.get(next)){ //Se acabou o subarquivo
                            pags.set(next,-1);
                            count2++;
                        }
                        else
                            if(this.buffer.loadPage(origem, pags.get(next))== -1);
                                op = true;
                                
                        
                    }
                }
            }
            for(int p = 0 ; p< pags.size(); p++)
            {
                if(pags.get(p) != -1)
                    this.buffer.freePage(origem, pags.get(p));
            }
          }
        this.sourceFile = destino;
        
    }

    public int internalSort(String path, String nameSourceFile, int indexAttribute) throws FileNotFoundException, IOException
    {
        this.path = path;
        this.sourceFile = nameSourceFile;
        int[] ini = new int[2];
        int[] fim = new int[2];
        BufferPair b = new BufferPair(nameSourceFile);
        int subarq = this.buffer.getNumberPagesOfFile(nameSourceFile)/Manager.NUMBER_OF_FRAMES;
        if(this.buffer.getNumberPagesOfFile(nameSourceFile)%Manager.NUMBER_OF_FRAMES != 0)
            subarq++;
        
        for(int i = 0; i<subarq; i++)
        {
            ini[0] = i*Manager.NUMBER_OF_FRAMES; //Primeira pagina
            ini[1] = 0; //Primeira tupla da primeira pagina
            
            for (int j = ini[0]; j<(ini[0]+Manager.NUMBER_OF_FRAMES); j++)
            {
                if(this.buffer.loadPage(nameSourceFile, j) == -1)
                {
                    fim [0] = j-1;
                    break;
                }
                else
                    fim[0] = j;
            }
            
            b.setNumberPage(fim[0]);
            
            fim[1] = this.buffer.get(b).size()-1;
            
            quick_sort(ini, fim, indexAttribute);
        
            for(int j = ini[0]; j <= fim[0]; j++)
            {
                this.buffer.writePage(nameSourceFile, j);
                this.buffer.freePage(nameSourceFile, j);
            }
            
        }
        return subarq;
    }
    
    public void quick_sort(int ini[], int fim[], int indexAttribute) {
        int[] q;
        
        if (comparaIndice(ini,fim)) {
           
            q = partition(ini, fim, indexAttribute);
            
            if(q == null)
                return;
            
            quick_sort(ini, q, indexAttribute);
            
            incrementa(q);
            
            quick_sort(q, fim, indexAttribute);
        }
   }

    public int[] partition(int ini[], int fim[], int indexAttribute) {
        int[] topo = new int[2];
        byte[] pivo;
        BufferPair b = new BufferPair(this.sourceFile);
        BufferPair endPage = new BufferPair(this.sourceFile);
        byte[] t1, t2;
        int i,j;
        
        pivo = this.buffer.getTuple(this.sourceFile, ini[0], ini[1]);
        if(pivo == null)
            return null;
        
        topo[0] = ini[0];
        topo[1] = ini[1];
        
        incrementa(ini);
   
        for (i=ini[0]; i<=fim[0]; i++){
            
            endPage.setNumberPage(i);
            
            int size = this.buffer.get(endPage).size();
            
            for (j = ini[1]; j < size; j++) {
                              
                t1 = this.buffer.getTuple(this.sourceFile, i, j);
               
                if (Originator.compare(t1, pivo, indexAttribute) < 0) {
                                        
                    b.setNumberPage(topo[0]);
                    this.buffer.get(b).set(topo[1], t1);
                    
                    incrementa(topo);
                    
                    t2 = this.buffer.getTuple(this.sourceFile, topo[0], topo[1]);
                    b.setNumberPage(i);
                    this.buffer.get(b).set(j, t2);                 

                }
            }  
            ini[1]=0;
        }
        
        b.setNumberPage(topo[0]);
        this.buffer.get(b).set(topo[1], pivo);
        
        return topo;
    }
    
    public static int compare(byte[] byte1, byte[] byte2, int index)
    {
        
        for (int i = (index*11); i< (index*11 + 10);i++){
            if(Byte.compare(byte1[i],byte2[i])>0)
                return 1;
            if(Byte.compare(byte1[i],byte2[i])<0)
                return -1;
        }
        return 0;
    }
    
    public void incrementa(int[] a)
    {
        if(a[1] == Page.RECORDS_PER_PAGE-1) {
            a[0] +=1;
            a[1] = 0;
        }
        else 
            a[1] +=1;
    }
    
    public boolean comparaIndice(int[] i, int [] j)
    {
       if (i[0] < j[0])
           return true;

       if (i[0] == j[0] && (i[1] < j[1]))
           return true;
       
       return false;
    }
   
}
