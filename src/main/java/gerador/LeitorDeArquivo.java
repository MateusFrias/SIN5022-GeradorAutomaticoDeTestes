package gerador;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeitorDeArquivo {
    public List<String> leArquivoTexto(String path){
        List<String> linhas = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            while(br.ready()){
                String linha = br.readLine();
                linhas.add(linha);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
       return linhas;
    }
}
