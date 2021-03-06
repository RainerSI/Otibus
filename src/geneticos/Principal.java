package geneticos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import api_interface.PassageiroResposta;
import api_interface.Resposta;
import graficos.GraficoDupla;
import graficos.GraficoTreino;
import itens.OnibusUtilizacao;
import itens.Pessoa;
import itens.TemposMedios;

public class Principal {
	
	
public static Cromossomo[] VidaCruel(Cromossomo[] cromossomos,int Maxgeracoes,
		double mutacaoTx,boolean[] alfabeto, Resposta resposta, String versao, File file){
		
		
		Fitness f=  new Fitness();
		//temos que ter uma populaçã aleatoria;
		ArrayList<Double>fitMedio=new ArrayList<>();
		ArrayList<Double>minimos=new ArrayList<>();
		ArrayList<Double>maximos=new ArrayList<>();
		
		double fitness[]=new double[cromossomos.length];// para armazenar os fitness;
		Cromossomo[]geracaoAtual=cromossomos;
		Cromossomo cAux=new Cromossomo(cromossomos[0].conteudo.length);
		cAux.setRotaPadrao();
		int qtdPontos = BaseInfo.getInstance().getQtdPonto();
		int qtdOnibus = BaseInfo.getInstance().getOnibusListados().size();
		resposta.setBaseline(new ArrayList<PassageiroResposta>());
		double daux=f.calculaFitness(cAux, resposta.getBaseline());
		
		int correcao = 0;
		int []auxint= new int[5000];
		GraficoTreino gt= new GraficoTreino(geracaoAtual,auxint);
		
		for(int geracao=0;geracao<Maxgeracoes;geracao++){
			// para cada uma das geracoes 
			
			System.out.println("Geração G"+geracao);
			
			
			double totalFitness=0;
			double minfit= Double.MAX_VALUE;
			double verominfit= Double.MAX_VALUE;
//			System.out.println("Fitness por cromossomo:");
			for(int aux=0; aux<fitness.length;aux++){
				double tempFit = f.calculaFitness(geracaoAtual[aux], null);
				fitness[aux]=tempFit;
				if(tempFit<minfit)minfit=tempFit;
				if((1/tempFit)<verominfit)verominfit=(1/tempFit);
//				System.out.println(tempFit);
				totalFitness+=1/tempFit;///FUNCAO DE MEDIR O FITNESS DO CROMOSSOMO
			}
			maximos.add((1/minfit));
			minimos.add((verominfit));
			//normalizando fitness
			for(int aux=0; aux<fitness.length;aux++){
				fitness[aux]-=minfit;
			}
			
			
//			System.out.println("/------------------/");
		
			
			// para compor a nova geracao
			
			Roleta roleta = new Roleta(fitness);//roleta que escolhe individuos
			
			Cromossomo[] novaGeracao=new Cromossomo[cromossomos.length];
			
			if(Math.random() > 0.95 && geracao > 10000) {
				correcao = 75;
			} else {
				if(correcao >0) correcao -=3;
			}
			
			double clonagem = 1+geracao*0.001;
			double mutacao = correcao+(300/(geracao*0.0001+1));
			double crossover = 1+geracao*0.002;
			double soma = clonagem+mutacao+crossover;
			DecimalFormat df = new DecimalFormat("0.0##");
			
			System.out.println(df.format(clonagem)+"("+df.format(clonagem/soma)+")" +"\t"+ 
			df.format(mutacao)+"("+df.format(mutacao/soma)+")" +"\t"+df.format(crossover)+"("+df.format(crossover/soma)+")");
			double[]probabilidade ={clonagem, mutacao, crossover};//chances de cada operação genetica
			Roleta roletaOperacao = new Roleta(probabilidade);// roleta pra escolher a operação;
			
			for(int i=0;i<cromossomos.length;i++){
				//hora de escolher a operaçao genetica
				
				int sorteio=roletaOperacao.sortear(); 
				
				switch(sorteio){
					case 0:
						novaGeracao[i]=OperadorGenetico.clonagem(geracaoAtual[roleta.sortear()]);
						//System.out.println("clonagem");
						auxint[i]=100;
						break;
					case 1:
//						System.out.println("aqui:"+sorteio+" "+i+" "+roleta.sortear());
						if(Math.random() >0.6)
							novaGeracao[i]=OperadorGenetico.mutacaoViagem(geracaoAtual[roleta.sortear()], alfabeto, qtdOnibus, qtdPontos);
						else
							novaGeracao[i]=OperadorGenetico.mutacao(geracaoAtual[roleta.sortear()], alfabeto);
						
						auxint[i]=221;
						//System.out.println("mutação");
						break;
						
					case 2:
						if(i==cromossomos.length-1){
							i--;
							continue;
						}
						//System.out.println("crossover");
						Cromossomo[] aux = OperadorGenetico.CrossoverPorOnibus(geracaoAtual[roleta.sortear()],geracaoAtual[roleta.sortear()], qtdOnibus, qtdPontos);
						novaGeracao[i] = aux[0];
						novaGeracao[i+1] = aux[1];
//						novaGeracao[i]=OperadorGenetico.Crossover1(geracaoAtual[roleta.sortear()],geracaoAtual[roleta.sortear()]);
//						novaGeracao[i+1]=OperadorGenetico.Crossover2(geracaoAtual[roleta.sortear()],geracaoAtual[roleta.sortear()]);
						i++;
						
						auxint[i]=331;
						break;
				}
				
				
				
			}
			gt.SetCromosso(geracaoAtual);
			System.out.println("F:"+totalFitness/geracaoAtual.length);
			System.out.println("");
			geracaoAtual=novaGeracao;
			fitMedio.add(totalFitness/geracaoAtual.length);
			
			
		}
		
//		double ftest = 0;
//		ArrayList<PassageiroResposta> pr = new ArrayList<>();
//		int h = -1;
//		for(int i = 0; i < geracaoAtual.length; i++) {
//			ArrayList<PassageiroResposta> temp = new ArrayList<>();
//			double ft = f.calculaFitness(geracaoAtual[i], temp);
//			if(ft > ftest) {
//				pr = temp;
//				h = i;
//			}
//		}
//		resposta.setMelhorGeracao(pr);
//		resposta.setUltimaGeracao(geracaoAtual[h]);
		String saida = String.valueOf(1/daux)+"\n";
		for(Double d:fitMedio) {
			saida += String.valueOf(d)+"\n";
		}
		try {
            FileWriter writer = new FileWriter("SaidaTreinamento/V"+versao+"/mediaPorGeracaoV"+versao+".txt", false);
            writer.write(saida);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		
		JOptionPane.showMessageDialog(null, 1/daux);
		new Grafico1(fitMedio,1/daux,minimos,maximos).salvarImagem("graficoEvolucaoV"+versao,file);;
		
		// graficos de inicio e termino
		
		{
		int auxcont=0;
		
		ArrayList<Double>X= new ArrayList<>();
		ArrayList<Double>Y= new ArrayList<>();
		for(PassageiroResposta p: resposta.getBaseline()){
//			System.out.println("i"+auxcont+"-> "+p.getInicioEspera()+" : "+p.getHorarioTermino());
			//if(Math.random()>0.05)continue;
			auxcont++;
			X.add(p.getInicioEspera());
			Y.add(p.getHorarioTermino());
		}
		
		double[] xx= new double[X.size()];
		double[] yy= new double[X.size()];
		for(int i=0;i<X.size();i++){
			xx[i]=X.get(i)*4;
			yy[i]=(Y.get(i)*4)/3600;
			
		}
		
		new GraficoDupla(xx,yy, null, 60).salvarImagem("GinicioFimBaselineV"+versao, file);;
		}
		int minIndex=-1;
		double minFitaux= Double.MAX_VALUE;
		for(int i=0;i<geracaoAtual.length;i++){
			double aux=f.calculaFitness(geracaoAtual[i],null);
			if(aux<minFitaux){
				aux=minFitaux;
				minIndex=i;	
			}
		}
		f.calculaFitness(geracaoAtual[minIndex],resposta.getMelhorGeracao() );
		{
			int auxcont=0;
			
			ArrayList<Double>X= new ArrayList<>();
			ArrayList<Double>Y= new ArrayList<>();
			for(PassageiroResposta p: resposta.getMelhorGeracao()){
//				System.out.println("i"+auxcont+"-> "+p.getInicioEspera()+" : "+p.getHorarioTermino());
				//if(Math.random()>1)continue;
				auxcont++;
				X.add(p.getInicioEspera());
				Y.add(p.getHorarioTermino());
			}
			
			double[] xx= new double[X.size()];
			double[] yy= new double[X.size()];
			for(int i=0;i<X.size();i++){
				xx[i]=X.get(i)*4;
				yy[i]=(Y.get(i)*4)/3600;
				
			}
			
			new GraficoDupla(xx,yy, null, 60).salvarImagem("GinicioFImTreinadoV"+versao, file);
			}
		
		
		
		
		return geracaoAtual;
	}



//
//	public static void main(String[] args) {
//		 //TODO Auto-generated method stub
//		
//		testeOnibus();
//		
////		
//		Cromossomo[]cromossomos= new Cromossomo[10];
//		for(int i=0;i<cromossomos.length;i++){
//			cromossomos[i]= new Cromossomo(20);
//		}
//		boolean[]alfabeto={true,false};
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Cromossomo[] geracao = VidaCruel(cromossomos, 2, 3, alfabeto);
//		
//		for(Cromossomo b:geracao) {
//			System.out.println(Arrays.toString(b.getConteudo()));
//		}
//	}
//	
//	public static void testeOnibus() {
//		int nOnibus = 5;
//		int nPontos = 4;
//		double tempoOnibus[] = {10, 15, 71, 80, 100};
//		double tempoParadas[] = {3, 2, 4, 2};
////		
//		TemposMedios tm = TemposMedios.getInstance();
//		tm.setTempoOnibus(tempoOnibus);
//		tm.setTempoParada(tempoParadas);
//		Cromossomo cromossomo = new Cromossomo(nPontos*nOnibus);
////		
////		boolean b[] = {
////				true, false, true, true,
////				false, true, true, false,
////				true, true, true, true,
////				true, false, false, true,
////				false, true, true, true
////		};
//		boolean b[] = {
//				true,true,true,true,
//				true,true,true,true,
//				true,true,true,true,
//				true,true,true,true,
//				true,true,true,true
//		};
//		cromossomo.setConteudo(b);
//		
//		tm.setTempoTrajetoEntrePontos(temposTrajetos);
//		
//		ArrayList<OnibusUtilizacao> oni = todosOnibus(nOnibus);
//		ArrayList<Pessoa> pass = todosOsPassageiros();
//		Fitness f = new Fitness();
////		System.out.println(pass.size());
////		for (Pessoa p: pass) {
////			System.out.println(p.getDestino() + " "+p.getPartida()+ " "+p.getHorarioChegada()+" "+p.getInicioEspera());
////		}
////		for (Onibus p: oni) {
////			System.out.println(p.getCapacidade() + " "+p.getParada()+ " "+p.getHorarioChegada()+" "+p.getInicioEspera());
////		}
////		
//		long tempoInicial = System.currentTimeMillis();
//		double t = f.calculaFitness(cromossomo);
//		long tempoFinal = System.currentTimeMillis();
//		System.out.println(tempoFinal - tempoInicial);
//		System.out.println(t);
////		System.out.println(pass.size());
////		for (Pessoa p: pass) {
////			System.out.println(p.getDestino() + " "+p.getPartida()+ " "+p.getHorarioChegada()+" "+p.getInicioEspera());
////		}
//		tempoInicial = System.currentTimeMillis();
//		t = f.calculaFitness(cromossomo);
//		tempoFinal = System.currentTimeMillis();
//		System.out.println(tempoFinal - tempoInicial);
//		System.out.println(t);
//	
//	}
	

//	public static ArrayList<OnibusUtilizacao> todosOnibus (int nOnibus){
//		ArrayList<OnibusUtilizacao> onibusd= new ArrayList<>();
//		for(int i = 0; i<nOnibus;i++) {
//			OnibusUtilizacao novo = new OnibusUtilizacao();
//			novo.setId(i);
//			onibusd.add(novo);
//		}
//		return onibusd;
//	}
//	
//	public static ArrayList<Pessoa> todosOsPassageiros() {
//		
//		ArrayList<Pessoa> passageiros = new ArrayList<>();
//		Pessoa p1 = new Pessoa(2, 0, 9);
//		passageiros.add(p1);
//		Pessoa p2 = new Pessoa(3, 0, 78);
//		passageiros.add(p2);
//		Pessoa p3 = new Pessoa(3, 1, 35);
//		passageiros.add(p3);
//		Pessoa p4 = new Pessoa(3, 2, 81);
//		passageiros.add(p4);
//		Pessoa p5 = new Pessoa(3, 2, 40);
//		passageiros.add(p5);
//		return passageiros;
//	}
	
}
