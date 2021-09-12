import org.jeasy.states.api.*;
import org.jeasy.states.core.FiniteStateMachineBuilder;
import org.jeasy.states.core.TransitionBuilder;

import javax.print.DocFlavor;
import java.util.*;

public class main {

    public static void main(String[] args){

        String arquivoEventos = "eventos.csv";
        String arquivoTabela = "transicao.csv";

        gerador.LeitorDeArquivo leitor = new gerador.LeitorDeArquivo();

        List<String> linhasEventos = leitor.leArquivoTexto(arquivoEventos);
        List<String> linhasTabela = leitor.leArquivoTexto(arquivoTabela);

        int i = 0; int j = 0;

        String[][] listaEventos = new String[linhasEventos.size()][2];
        for (j=0;j<linhasEventos.size();j++){
            String linha = linhasEventos.get(j);
            String[] valores = linha.split(";");
            for (i=0;i< valores.length;i++){
                listaEventos[j][i] = valores[i];
            }
        }


        String[] eventos = new String[linhasEventos.size()-1];
        String[] transicoes = new String[linhasTabela.size()-1];

        i=0;j=0;

        for (String linha: linhasEventos){
            String[] valores = linha.split(";");
            if(!valores[0].equals("system proxy")) {
                eventos[i] = valores[0];
                i++;
            }
        }

        for (String linha: linhasTabela){
            String[] valores = linha.split(",");
            if(!valores[0].equals("states/events")) {
                transicoes[j] = valores[0];
                j++;
            }
        }

        System.out.println("\nEventos:");
        System.out.println(Arrays.toString(eventos) + "\n");
        System.out.println("Estados:");
        System.out.println(Arrays.toString(transicoes) + "\n");

        List<String> estadosUnicos = new ArrayList<>();
        List<String> arvore = new ArrayList<>();
        List<String> caminhos = new ArrayList<>();

        for (String linha: linhasTabela){
            String[] valores = linha.split(",");
            String primeira = "";

            if (!valores[0].contains("states/events")) {
                primeira = valores[0];
                if (!estadosUnicos.contains(valores[0]))
                    estadosUnicos.add(valores[0]);
                for (i = 1; i<valores.length; i++){
                    if (valores[i] != "") {
                        int sufixoValor = Collections.frequency(estadosUnicos, valores[i]);
                        arvore.add(primeira + "_0," + valores[i] + "_" + sufixoValor);
                        if (valores[0] != valores[i])
                            estadosUnicos.add(valores[i]);
                    }
                }
            }
        }

        System.out.println("Árvore de Transição:");
        for (String linha : arvore) {
            System.out.println(linha);
        }

        System.out.println("\nNós folha:");
        List<String> nosFolha = new ArrayList<>();
        for (String linha : arvore) {
            String[] item = linha.split(",");
            nosFolha.add(item[item.length-1]);
        }
        for (String linha : arvore) {
            String[] item = linha.split(",");
            if (nosFolha.contains(item[0]))
                nosFolha.remove(item[0]);
        }
        System.out.println(nosFolha);


        caminhos.add(arvore.get(0));
        List<String> caminhosAux = caminhos;

        System.out.println("\nCaminhos:");

        for (String linha : arvore) {
            caminhos = addCaminho(linha, caminhosAux);
            caminhosAux = caminhos;
        }

        List<String> removeCaminho = new ArrayList<>();
        for (String caminho : caminhos) {
            String[] item = caminho.split(",");
            if (!nosFolha.contains(item[item.length-1]))
                removeCaminho.add(caminho);
        }

        for (String caminho : removeCaminho){
            caminhos.remove(caminho);
        }

        List<String> caminhosLimpos = new ArrayList<>();
        for (String caminho : caminhos){
            String caminhoAux = "";
            String[] item = caminho.split(",");
            for (i=0; i < item.length; i++){
                String[] item2 = caminhoAux.split((","));
                if (!item[i].equals(item2[item2.length-1])) {
                    if (caminhoAux != ""){
                        caminhoAux = caminhoAux + "," + item[i];
                    }
                    else {
                        caminhoAux = item[0];
                    }
                }
            }
            caminhosLimpos.add(caminhoAux);
        }

        for (String linha : caminhosLimpos){
            System.out.println(linha);
        }

        List<String[]> tabelaTransicao = new ArrayList<>();

        for (String linha : linhasTabela){
            String[] valores = linha.split(",");
            tabelaTransicao.add(valores);
        }

        List<String> listaParaGerarTestes = new ArrayList<>();
        String[] primeiraLinha = tabelaTransicao.get(0);

        for (String[] linha : tabelaTransicao){
            if (!linha[0].equals("states/events")) {
                for (i = 1; i < linha.length; i++) {
                    if (linha[i] != "") {
                        listaParaGerarTestes.add(linha[0] + "," + primeiraLinha[i] + "," + linha[i]);
                    }
                }
            }
        }

        System.out.println("\nTransições:");

        String[][] listaTransicoes = new String[listaParaGerarTestes.size()][3];
        for (j=0;j<listaParaGerarTestes.size();j++){
            String linha = listaParaGerarTestes.get(j);
            System.out.println(linha);
            String[] valores = linha.split(",");
            for (i=0;i< valores.length;i++){
                listaTransicoes[j][i] = valores[i];
            }
        }

        System.out.println("\nTestes:");
        int x,y;
        for (x=0; x<caminhosLimpos.size(); x++){
            String linha = caminhosLimpos.get(x);
            linha = linha.replaceAll("[0-9]","");
            linha = linha.replaceAll("_", "");
            String item[] = linha.split(",");
            System.out.println("\n@Test" +
                    "\npublic void testaCaminho" + (x + 1) + "(){");
            for (i=0;i<item.length-1;i++){
                for (j=0;j<listaTransicoes.length;j++){
                    if(item[i].equals(listaTransicoes[j][0]) && item[i+1].equals(listaTransicoes[j][2])){
//                        System.out.println("estado: " + item[i] + ", evento: " + listaTransicoes[j][1] + ", estado: " + listaTransicoes[j][2]);
                        if (i==0)
                            System.out.println("assertEquals(\"" + item[i] + "\",SDevice.getState());");
                        for(y=0;y< listaEventos.length;y++){
                            if (listaTransicoes[j][1].equals(listaEventos[y][0]))
                                System.out.println(listaEventos[y][1] + ";");
                        }
                        System.out.println("assertEquals(\"" + listaTransicoes[j][2] + "\",SDevice.getState());");
                    }
                }
            }
            System.out.println("}");
        }
    }

    public static List<String> addCaminho (String linha, List<String> caminhos) {
        String item[] = linha.split(",");
        List<String> caminhosAux = new ArrayList<>();

        for (String x : caminhos){
            caminhosAux.add(x);
        }

        for (String caminho : caminhos) {
            String item2[] = caminho.split(",");
            if (item[0].equals(item2[item2.length-1])){
                caminhosAux.add(caminho + "," + linha);
            }
        }

        for (String x : caminhosAux){
            caminhos.add(x);
        }
        return caminhosAux;
    }



        //            class PushEvent extends AbstractEvent { }
//            class CoinEvent extends AbstractEvent { }


//        State locked = new State("locked");
//        State unlocked = new State("unlocked");
//
//        Set<State> states = new HashSet<>();
//        states.add(locked);
//        states.add(unlocked);
//
//        class PushEvent extends AbstractEvent { }
//        class CoinEvent extends AbstractEvent { }
//
//        Transition pushLocked = new TransitionBuilder()
//                .name("pushLocked")
//                .sourceState(locked)
//                .eventType(PushEvent.class)
//                .targetState(locked)
//                .build();
//
//        Transition coinUnlocked = new TransitionBuilder()
//                .name("coinUnlocked")
//                .sourceState(unlocked)
//                .eventType(CoinEvent.class)
//                .targetState(unlocked)
//                .build();
//
//        FiniteStateMachine turnstileStateMachine = new FiniteStateMachineBuilder(states, locked)
//                .registerTransition(pushLocked)
//                .registerTransition(coinUnlocked)
//                .build();

}
