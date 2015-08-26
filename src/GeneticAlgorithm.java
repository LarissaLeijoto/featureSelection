import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import jomp.compiler.*;
import jomp.runtime.*;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import jxl.Cell;
import jxl.CellType;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.PrincipalComponents;


/**
 * 
 *  Documentação do trabalho elaborado na Iniciação Cientifica: O Uso de Técnicas de Aprendizado de Máquina na Predição de Função de Proteínas
 *	A base de dados utilizada foi a mesma do artigo de Dobson e Doig: Predicting Enzyme Class From Protein Structure Without Alignments, Journal of Molecular Biology.
 *  Essa base de dados é utilizada para efeitos de comparação. Os tipo de proteínas utilizadas são as Enzimas,  que podem ser separadas em 6 superclasses: Hydrolase, Isomerase, Oxidoreductase, Ligase, Liase e Transferase. Cada classe catalisa diferentes reações, e essas reações estão diretamente relacionadas com suas funções.
 *	O banco de dados usado para a extração das caracteristicas foi o Sting_DB. O artigo relacionado a essa base é: Uma metodologia para seleção de parâmetros em modelos de classificação de proteínas.
 *	Existem dois scripts para extrair essa base do banco de dados mencionado. A execução do script é feita por linha de comando. O nome do script é: executa.sh. Para executá-lo é necessário um arquivo txt com os códigos pdb de cada proteína a ser extraída da base.
 *	Após a execução do script, ele retornará cada proteína como sendo uma tabela .xls. Essa tabela será dividida em dois abas, cada uma contendo N características.
 *	Esse código é a implementação de um algoritmo genético para a seleção das características do banco Sting_BD, com o objetivo de classificá-las com a melhor acurácia possível.
 * 
 * @author Larissa Fernades Leijôto
 */
public class GeneticAlgorithm
{
	static int	                  quantidadeTotalAminoacidos	= 0;
	static float[]	              MaioresPlan1;
	static float[]	              MenoresPlan1;
	static float[]	              MaioresPlan2;
	static float[]	              MenoresPlan2;
	static String[]	              NomeClasses	             = { "Hidrolases", "Isomerases", "Ligases", "Liases", "Oxidoredutases", "Transferases" };
	static int	                  menor	                     = 999999999;
	static int	                  maior	                     = -999999999;
	static int	                  tamanhoTransformada	     = 0;
	static float	              ProbabilidadeCruzamento	 = (float) 0.65;
	static float	              ProbabilidadeMutacao	     = (float) 0.01;
	static int	                  NumCaracteristicas		 = 20;
	static short	              NumPopulacao	             = 10;
	static int	                  NumGeracoes	             = 150;
	private static BufferedReader	maiores1;
	private static BufferedReader	menores1;
	private static BufferedReader	maiores2;
	private static BufferedReader	menores2;
	static float	MedidaDiversidadeGenetica	= 0;

/**
 * Função principal que executa a chamada do algoritmo genético
 * 
 * @param args 
 * @throws Exception
 */
	public static void main(String[] args) throws Exception
	{
		long start = System.currentTimeMillis();
		InicializaNormalizacao();
		AlgoritimoGenetico();
		long delay = System.currentTimeMillis() - start;
		System.out.println("# O main demorou :  " + delay + " milissegundos");
	}

	/**
	 * Função que inicializa os vetores com os maiores e menores valores de cada
	 * caracteristicas, para eles serem usados posteriormente na normalização.
	 */
	public static void InicializaNormalizacao()
	{

		try
		{
			maiores1 = new BufferedReader(new FileReader("MaioresPlan1.txt"));
			menores1 = new BufferedReader(new FileReader("MenoresPlan1.txt"));
			maiores2 = new BufferedReader(new FileReader("MaioresPlan2.txt"));
			menores2 = new BufferedReader(new FileReader("MenoresPlan2.txt"));
			String linha = "";
			String linha2 = "";

			while (maiores1.ready())
			{
				linha += maiores1.readLine() + " ";
				linha2 += menores1.readLine() + " ";
			}
			String[] M1 = linha.split(" ");
			String[] M2 = linha2.split(" ");
			MaioresPlan1 = new float[M1.length];
			MenoresPlan1 = new float[M2.length];

			//omp parallel for
			for (int i = 0; i < M1.length; i++)
			{
				MaioresPlan1[i] = Float.parseFloat(M1[i]);
			}

			//omp parallel for
			for (int i = 0; i < M2.length; i++)
			{
				MenoresPlan1[i] = Float.parseFloat(M2[i]);
			}

			linha = "";
			linha2 = "";

			while (maiores2.ready())
			{
				linha += maiores2.readLine() + " ";
				linha2 += menores2.readLine() + " ";
			}

			M1 = linha.split(" ");
			M2 = linha2.split(" ");

			MaioresPlan2 = new float[M1.length];
			MenoresPlan2 = new float[M2.length];

			//omp parallel for
			for (int i = 0; i < M1.length; i++)
			{
				MaioresPlan2[i] = Float.parseFloat(M1[i]);
			}

			//omp parallel for
			for (int i = 0; i < M2.length; i++)
			{
				MenoresPlan2[i] = Float.parseFloat(M2[i]);
			}
		} catch (Exception e)
		{
			System.out.print("ERRO na Normalizacao!");
			e.printStackTrace();
		}

	}

	/**
	 * Método que gera todos os arquivos necessários para a análise de
	 * dos dados
	 * 
	 * @param colunasPlanilha1
	 *            - Caracteristicas da planilha 1 que serão usadas para o
	 *            processamento
	 * @param colunasPlanilha2
	 *            - Caracteristicas da planilha 2 que serão usadas para o
	 *            processamento
	 * @param quantidadeCaracteristicas
	 *            - Soma da quantidade de caracteristicas selecionadas nas duas
	 *            planilhas
	 * @throws Exception
	 */

	public static void Principal(String colunasPlanilha1, String colunasPlanilha2, String nomeArquivo, int quantidadeCaracteristicas) throws Exception
	{

		try
		{
			int numeroTDC = 75;//MUDA AQUI A QUANTIDADE DE VALORES DA TDC 

			int i;

			for (i = 0; i < NomeClasses.length; i++)
			{
				BufferedReader ler = new BufferedReader(new FileReader("ArquivosXLS/" + NomeClasses[i] + "/" + "Nomes" + NomeClasses[i] + ".txt"));
				BufferedWriter proteinasProcessadas = new BufferedWriter(new FileWriter("saidaIntermediaria" + NomeClasses[i] + nomeArquivo + ".txt"));

				while (ler.ready()) //enquanto nao processar todas as proteinas
				{
					String nomeArq = ler.readLine();// pega o nome da proteina
					LePlanilha(proteinasProcessadas, "ArquivosXLS/" + NomeClasses[i] + "/" + nomeArq, colunasPlanilha1, colunasPlanilha2); // pega as caracteristicas da proteï¿½na dada e salva em 
				}
				ler.close(); // fecha o arquivo com os nomes das proteinas
				proteinasProcessadas.close(); // fecha o arquivo com as caracteristicas das proteinas
			}

			for (i = 0; i < NomeClasses.length; i++)
			{
				processaProteinas("saidaIntermediaria" + NomeClasses[i] + nomeArquivo + ".txt", numeroTDC, NomeClasses[i], nomeArquivo); // aplica a transformada do cosseno para todos os valores das caracterï¿½sticas, e pega os 20 valores mais significativos
			}

			tamanhoTransformada = numeroTDC;
			GerarArquivoArffSvm(nomeArquivo);

		} catch (Exception e)
		{
			System.out.print("ERRO no MAIN!");
			e.printStackTrace();
		}

	}

	
	/**
	 * 
	 * Função que processa os arquivos de cada proteína
	 * 
	 * @param nome Código Pdb da proteínas 
	 * @param num Número de coeficientes da transforma discreta do cosseno
	 * @param classe Classe de proteína que está sendo lida
	 * @param nomeArquivo Nome do arquivo xls que será lido
	 */
	public static void processaProteinas(String nome, int num, String classe, String nomeArquivo)
	{
		try
		{
			float[] vetorCaracteristicas;
			float[] vetorTransformado; //recebe o vetor de caracteriticas apos passar pela TDC

			BufferedReader proteinasProcessadas = new BufferedReader(new FileReader(nome));
			BufferedWriter saidaFinal = new BufferedWriter(new FileWriter("saidaFinal" + classe + nomeArquivo + ".txt"));
			String Classe = "";
			while (proteinasProcessadas.ready())
			{
				String linha = proteinasProcessadas.readLine(); // pega a linha com as caracteristicas de uma proteina   
				Classe += linha + "\n";
			}

			String[] proteinas = Classe.split("\n");
			float[][] saida = new float[proteinas.length][];

			//omp parallel for ordered
			for (int i = 0; i < proteinas.length; i++)
			{
				vetorCaracteristicas = ProcessaLinhaCaracteristicas(proteinas[i]); // manda fazer o split dessa linha
				vetorTransformado = TransformadaCosseno(vetorCaracteristicas, num);//aplica a transformada do cosseno em todas as caracteristicas da proteina
				vetorCaracteristicas = null;
				saida[i] = vetorTransformado;
				vetorTransformado = null;
			}

			for (int i = 0; i < saida.length; i++)
			{
				gravaDados(saida[i], saidaFinal);// grava vetor transformado
				saidaFinal.newLine();
			}

			proteinasProcessadas.close();

			saidaFinal.close();
		} catch (Exception e)
		{
			System.out.println("ERRO no PROCESSAMENTO DO ARQUIVO DOS VALORES DAS PROTEINAS!");
			e.printStackTrace();
		}

	}

	/**
	 * Grava o arquivo com as características definidas pelo algoritmo genético
	 * 
	 * @param saida Arquivo de saída
	 * @param nomeClasse Nome do arquivo com as características de cada classe
	 * @param classe Nome da classe que está sendo gravada no arquivo
	 */
	public static void gravaARFF(BufferedWriter saida, String nomeClasse, String classe)
	{
		try
		{
			BufferedReader ler = new BufferedReader(new FileReader(nomeClasse));

			while (ler.ready())
			{
				String[] s;
				String linha = ler.readLine();
				s = linha.split(" ");

				for (int i = 0; i < tamanhoTransformada; i++)
				{
					float valor = Float.parseFloat(s[i]);
					saida.write(valor + ",");

				}
				saida.write(classe);
				saida.write("\n");
			}
			ler.close();
		} catch (Exception e)
		{
			System.out.print("ERRO no GerarArquivos do Arquivo NORMALIZA!");
			e.printStackTrace();
		}
	}

	/**
	 * Função que junta os arquivos finais e grava no formato do weka, para que seja 
	 * feita a classificação
	 * 
	 * @param nomeArquivo Arquivo que sera gravado no formato do weka
	 */

	public static void GerarArquivoArffSvm(String nomeArquivo)
	{
		try
		{
			//--------------------------- CRIA O ARQUIVO NO FORMATO DO WEKA ----------------------------------------------------------------------------
			BufferedWriter saida = new BufferedWriter(new FileWriter(nomeArquivo + ".arff"));

			saida.write("@relation ClassesProteinas\n");
			for (int i = 1; i <= tamanhoTransformada; i++)
			{
				saida.write("@attribute 'c" + i + "' real\n");
			}
			saida.write("@attribute 'class' { Oxidoredutase, Transferase, Hidrolase, Liase, Isomerase, Ligase}\n");
			saida.write("@data\n");
			gravaARFF(saida, "saidaFinalOxidoredutases" + nomeArquivo + ".txt", "Oxidoredutase");
			gravaARFF(saida, "saidaFinalTransferases" + nomeArquivo + ".txt", "Transferase");
			gravaARFF(saida, "saidaFinalHidrolases" + nomeArquivo + ".txt", "Hidrolase");
			gravaARFF(saida, "saidaFinalLiases" + nomeArquivo + ".txt", "Liase");
			gravaARFF(saida, "saidaFinalIsomerases" + nomeArquivo + ".txt", "Isomerase");
			gravaARFF(saida, "saidaFinalLigases" + nomeArquivo + ".txt", "Ligase");
			saida.close();

		} catch (Exception e)
		{
			System.out.println("ERRO no MAIN do GeraARFF!");
			e.printStackTrace();
		}
	}

	/**
	 * Função que lê cada coluna de características 
	 * 
	 * @param saida Arquivo com as características processadas
	 * @param nomeArq Nome do arquivo .xls
	 * @param colunasPlanilha1 Planilha 1 do arquivo 
	 * @param colunasPlanilha2 Planilha 2 do arquivo
	 */
	public static void LePlanilha(BufferedWriter saida, String nomeArq, String colunasPlanilha1, String colunasPlanilha2)
	{

		Cell leitor; //calcula da planilha do excel
		Workbook workbook1; //cada arquivo .xls 
		WorkbookSettings ws = new WorkbookSettings();
		Sheet sheet; //cada planilha do arquivo .xls
		NumberCell nc;
		String nomeProteina, nomeCadeia;
		int numLinhas, numColunas;
		int posicaoVetor = 0;
		int tamanhoVetor;
		int numeroDePlanilhas;
		int[] vetor;
		int[] contaAminoacidos = new int[20];
		quantidadeTotalAminoacidos = 0;
		//*
		for (int i = 0; i < 20; i++)//iniciliza o vetor
		{
			contaAminoacidos[i] = 0;
		}
		boolean PodeNormalizar = true;

		try
		{
			// pega o arquivo do Excel  
			ws.setEncoding("ISO-8859-1");
			workbook1 = Workbook.getWorkbook(new File(nomeArq), ws);
			numeroDePlanilhas = workbook1.getNumberOfSheets();//pega o numero de planilhas do arquivo
			/**
			 * Numero de Colunas da Planilha 01 = 144 Numero de Colunas da
			 * Planilha 02 = 194
			 */
			for (int k = 0; k < 1; k++)
			{
				sheet = workbook1.getSheet(k);
				String nomeSheet = sheet.getName();
				numLinhas = sheet.getRows();
				numColunas = sheet.getColumns();
				if (k == 0)
				{
					// pega o nome da proteina 
					leitor = sheet.getCell(0, 0);
					nomeProteina = leitor.getContents();
					// pega a cadeia 
					leitor = sheet.getCell(0, 1);
					nomeCadeia = leitor.getContents();
					vetor = ProcessaValoresDasCaracteristicas(colunasPlanilha1);
				} else
				{

					vetor = ProcessaValoresDasCaracteristicas(colunasPlanilha2);
				}
				tamanhoVetor = vetor.length;
				//-------------------------------------------------------------------------------------------

				for (int j = 0; j < numColunas; j++)// percorre as colunas
				{
					if (posicaoVetor < tamanhoVetor)
						if (vetor[posicaoVetor] == j) // se for a coluna da caracteristica que quero os valores
						{
							for (int i = 4; i < numLinhas; i++)// percorre as linhas que variam de proteï¿½na para proteï¿½na
							{
								if (j != 0 && j != 1 && j != 2 && j != 3)// se for diferente dos nomes
								{
									leitor = sheet.getCell(j, i); // o primeiro valor da coluna e o segundo da linha
									double valor;
									if (leitor.getType() == CellType.NUMBER)
									{
										nc = (NumberCell) leitor;
										valor = nc.getValue();
									} else
									// testa se o valor da celula nao era int, e pra cada valor de string possivel grava um valor diferente como resposta
									{
										String valorCelula = leitor.getContents();
										if (valorCelula.equals("NONE"))
											valor = -1;
										else if (valorCelula.equals("VERDADEIRO"))
											valor = 1;
										else if (valorCelula.equals("FALSO"))
											valor = 0;
										else
											valor = 0;
									}
									double novoValor = valor;

									if (PodeNormalizar == true)
									{
										if (k == 0)
										{
											novoValor = ((valor - MenoresPlan1[j]) / (MaioresPlan1[j] - MenoresPlan1[j]));
										} else
										{
											novoValor = ((valor - MenoresPlan2[j]) / (MaioresPlan2[j] - MenoresPlan2[j]));
										}
									}
									saida.write(novoValor + " ");
								} else if (j == 0)
								{
									leitor = sheet.getCell(j, i); // o primeiro valor da coluna e o segundo da linha
									
								}
							}
							posicaoVetor++;
						}
				}
				posicaoVetor = 0;
			}
			saida.newLine();
			workbook1.close();

		} catch (Exception e)
		{
			System.out.println("ERRO na LEITURA DA PLANILHA!");
			e.printStackTrace();
		}

	}

	/**
	 * Escreve os dados no buffer de saída
	 * @param vetorTransformado Vetor de float com os valores transformados
	 * @param saidaFinal  Arquivo para se gravar os valores transformados
	 */
	public static void gravaDados(float[] vetorTransformado, BufferedWriter saidaFinal)
	{
		try
		{
			for (int i = 0; i < vetorTransformado.length; i++)
			{
				saidaFinal.write(vetorTransformado[i] + " ");
			}

			vetorTransformado = null;
		} catch (Exception e)
		{
			System.out.print("ERRO na gravacao dos dados no arquivo final!");
			e.printStackTrace();
		}
	}

	/**
	 * Função que recebe uma string e faz o split dela, retornando um vetor de
	 * float com os valores da string.
	 * 
	 * @param valores String que contém os valores
	 * @return vetor Vetor de float com as mesmas características para serem manipuladas
	 */
	public static float[] ProcessaLinhaCaracteristicas(String valores)
	{
		String[] s;
		s = valores.split(" ");
		float[] vetor = new float[s.length];

		//*
		for (int i = 0; i < s.length; i++)
		{
			vetor[i] = Float.parseFloat(s[i]);
		}

		return vetor;
	}


	/**
	 * Calcula a DCT de um arranjo unidimensional de float.
	 * @param x [Parametro de entrada] Sinal para ser calculada a DCT
	 * @param num [Parametro de entrada] Tamanho do vetor (número de elementos)
	 * @return Resultado da transformada
	 */
	public static float[] TransformadaCosseno(float[] x, int num)
	{
		float[] X = new float[x.length];

		int tamanho = x.length;
		
		for (int k = 0; k < tamanho; k++)
		{
			for (int n = 0; n < tamanho; n++)
			{
				if (k != 0)
				{
					X[k] += Math.sqrt(2.0 / tamanho) * Math.cos(k * Math.PI / (2.0 * tamanho) * (2.0 * n + 1)) * x[n];

				} else
				{
					X[k] += 1 / Math.sqrt(tamanho) * x[n];

				}
			}
		}

		float resposta[] = new float[num];

		for (int k = 0; k < num; k++)
		{
			resposta[k] = X[k];
		}
		X = null;
		return resposta;
	}

	/**
	 * Função que recebe uma string e faz o split dela, retornando um vetor de
	 * int com os valores da string. 
	 * 
	 * @param valores Recebe uma string com os números das características que a serem gravadas
	 * @return vetor Um vetor de inteiros com os números das caracteristicas
	 */
	public static int[] ProcessaValoresDasCaracteristicas(String valores)
	{
		String[] s;
		s = valores.split(",");
		int[] vetor = new int[s.length];

		//*
		for (int i = 0; i < s.length; i++)
		{
			vetor[i] = (Integer.parseInt(s[i]) - 1);
		}
		return vetor;
	}
	
	/**
	 * Contabiliza a frequência de aminácidos de cada proteína
	 * 
	 * @param nomeAminoacido Aminoácido que será contabilizado
	 * @param contaAminoacidos Vetor de aminácidos
	 * @throws IOException
	 */

	public static void contaAminoacidos(String nomeAminoacido, int[] contaAminoacidos) throws IOException
	{

		if (nomeAminoacido.equals("I"))
		{
			contaAminoacidos[12]++;

		} else if (nomeAminoacido.equals("V"))
		{
			contaAminoacidos[18]++;

		} else if (nomeAminoacido.equals("L"))
		{
			contaAminoacidos[13]++;

		} else if (nomeAminoacido.equals("F"))
		{
			contaAminoacidos[11]++;

		} else if (nomeAminoacido.equals("C"))
		{
			contaAminoacidos[1]++;

		} else if (nomeAminoacido.equals("M"))
		{
			contaAminoacidos[15]++;

		} else if (nomeAminoacido.equals("A"))
		{
			contaAminoacidos[6]++;

		} else if (nomeAminoacido.equals("G"))
		{
			contaAminoacidos[2]++;

		} else if (nomeAminoacido.equals("T"))
		{
			contaAminoacidos[16]++;

		} else if (nomeAminoacido.equals("S"))
		{
			contaAminoacidos[7]++;

		} else if (nomeAminoacido.equals("W"))
		{
			contaAminoacidos[17]++;

		} else if (nomeAminoacido.equals("Y"))
		{
			contaAminoacidos[5]++;

		} else if (nomeAminoacido.equals("P"))
		{
			contaAminoacidos[4]++;

		} else if (nomeAminoacido.equals("H"))
		{
			contaAminoacidos[9]++;

		} else if (nomeAminoacido.equals("Q"))
		{
			contaAminoacidos[3]++;

		} else if (nomeAminoacido.equals("N"))
		{
			contaAminoacidos[8]++;

		} else if (nomeAminoacido.equals("E"))
		{
			contaAminoacidos[19]++;
		}

		else if (nomeAminoacido.equals("D"))
		{
			contaAminoacidos[10]++;

		} else if (nomeAminoacido.equals("K"))
		{
			contaAminoacidos[14]++;

		} else if (nomeAminoacido.equals("R"))
		{
			contaAminoacidos[0]++;
		}
	}

	/**
	 * Parte do programa que seleciona as características por meio de um Algoritmo genético simples.
	 * 
	 * @throws Exception
	 */
	public static void AlgoritimoGenetico() throws Exception
	{
		int[][] Populacao = null;
		int[][] PopulacaoAux = null;
		float[] fitness = null;
		float[] fitnessAux = null;
		int[] top = new int[NumCaracteristicas];// melhor da população. Escolhido por meio de elitismo
		int pior, melhor;
		double [] melhores = new double [NumGeracoes];
		double [] media = new double [NumGeracoes];

		//Inicio do Algoritmo Genético
		Populacao = InicializaPopulacao(Populacao);
		PopulacaoAux = InicializaPopulacao(PopulacaoAux);
		fitness = Avaliacao(Populacao);

		//Manter melhor
		melhor = MelhorIndividuo(fitness);
		for (int k = 0; k < NumCaracteristicas; k++)
		{
			top[k] = Populacao[melhor][k];
		}

		for (int i = 0; i < NumGeracoes; i++)
		{
			Selecao(Populacao, PopulacaoAux, fitness);
			Cruzamento(PopulacaoAux);
			Mutacao(PopulacaoAux);
			fitnessAux = Avaliacao(PopulacaoAux);

			//Inserir individuo mais forte			
			pior = PiorIndividuo(fitnessAux);

			for (int k = 0; k < NumCaracteristicas; k++)
			{
				PopulacaoAux[pior][k] = top[k];
			}
			fitnessAux[pior] = fitness[melhor];
			
			AtualizaPopulacao(Populacao, PopulacaoAux);
			AtualizaFitness(fitness, fitnessAux);

			//Manter individuo mais forte
			melhor = MelhorIndividuo(fitnessAux);
			for (int k = 0; k < NumCaracteristicas; k++)
			{
				top[k] = Populacao[melhor][k];
			}
			
			melhores[i] = fitnessAux[MelhorIndividuo(fitnessAux)];
			media[i] = media(fitnessAux);

			MedidaDiversidadeGenetica = media(fitness) / fitness[MelhorIndividuo(fitness)];

			if (MedidaDiversidadeGenetica > 0.90)
			{
				ProbabilidadeMutacao = (float) 0.9;
				ProbabilidadeCruzamento = (float) 0.01;
			} else
			{
				ProbabilidadeMutacao = (float) 0.01;
				ProbabilidadeCruzamento = (float) 0.65;
			}

		}

		System.out.println("******************************************************************");
		for (int i = 0; i < NumPopulacao; i++)
		{
			for (int j = 0; j < NumCaracteristicas; j++)
			{
				System.out.print(Populacao[i][j] + " ");

			}
			System.out.println(fitness[i] + " ");

		}

		System.out.println("******************************************************************");
		
		
		System.out.println("Médias");
		for (int i = 0; i < NumGeracoes; i++)
		{
			System.out.print((float)media[i] + " ");

		}
		
		System.out.println();
		System.out.println("Melhores");
		for (int i = 0; i < NumGeracoes; i++)
		{
			System.out.print(melhores[i] + " ");

		}
	}
	
	/**
	 * Função que calcula a média do fitness dos individuos na população
	 * 
	 * @param fitness2 Vetor de fitness dos individuos
	 * @return Retorna o fitness medio d população
	 */
	
	private static float media(float[] fitness2)
	{
		float resposta = 0;

		for (int i = 0; i < fitness2.length; i++)
		{
			resposta += fitness2[i];
		}
		return resposta / fitness2.length;
	}
	/**
	 * Avalia a população inicial de acordo com as métricas de avaliação do
	 * individuos impostas pelo programa.
	 * 
	 * @return Retorna o fitness do individuo
	 * @throws Exception
	 */
	public static float[] Avaliacao(int[][] populacao) throws Exception
	{
		float[] notas = new float[NumPopulacao];
		String Caracteristicas[] = new String[NumCaracteristicas];
		String nomeArquivo[] = new String[NumCaracteristicas];
		int[] quantidadeCaracteristicas = new int[NumCaracteristicas];

		for (int i = 0; i < populacao.length; i++)
		{
			Caracteristicas[i] = "";
			nomeArquivo[i] = "";
			quantidadeCaracteristicas[i] = 0;
			Caracteristicas[i] = "1";

			for (int j = 0; j < populacao[0].length; j++)
			{
				Caracteristicas[i] += "," + populacao[i][j];
				quantidadeCaracteristicas[i]++;

			}
			nomeArquivo[i] = "Individuo" + i;
			String[] a = Caracteristicas[i].split(",");
			quicksort(a, 0, a.length - 1);
			String vetorOrdenado ="";
			vetorOrdenado+= Integer.parseInt(a[0]);
			
			for(int k = 1 ; k < a.length;k++)
				vetorOrdenado+=","+Integer.parseInt(a[k]);
			
			Principal(vetorOrdenado, "", nomeArquivo[i], quantidadeCaracteristicas[i]);

		}
	
		for (int i = 0; i < populacao.length; i++)
			notas[i] = Classificacao(nomeArquivo[i]); //max

		return notas;
	}

	/**
	 * Atualiza o valor do fitness da população para a proxima geração de
	 * individuos.
	 * 
	 * @param fitness Vetor de fitness atual
	 * @param fitnessAux Vetor de fitness antigo
	 */
	public static void AtualizaFitness(float[] fitness, float[] fitnessAux)
	{
		for (int i = 0; i < NumPopulacao; i++)
			fitness[i] = fitnessAux[i];
	}
	/**
	 * Atualiza a população antiga pela nova população
	 * 
	 * @param populacao População atual
	 * @param populacaoAux População antiga
	 */

	public static void AtualizaPopulacao(int[][] populacao, int[][] populacaoAux)
	{
		for (int i = 0; i < NumPopulacao; i++)
		{
			for (int j = 0; j < NumCaracteristicas; j++)
			{
				populacao[i][j] = populacaoAux[i][j];
			}
		}

	}
	/**
	 * Método que realiza a mutação nos indivíduos da população
	 * 
	 * @param populacaoAux População que será mutada
	 */

	public static void Mutacao(int[][] populacaoAux)
	{
		Random gerador = new Random();

		int NumeroMutacao = gerador.nextInt((int) Math.ceil((NumPopulacao * ProbabilidadeMutacao) * NumCaracteristicas));
		int r1 = 0, r2 = 0;

		for (int i = 0; i < NumeroMutacao; i++)
		{
			r1 = gerador.nextInt(NumPopulacao);
			r2 = gerador.nextInt(NumCaracteristicas);

			int k = gerador.nextInt(144) + 1;

			while (ja_existe(populacaoAux[r1], k) || k == 0 || k == 1 || k == 2 || k == 3 || k == 4)
			{
				k = gerador.nextInt(144) + 1;
			}

			populacaoAux[r1][r2] = k;

		}

	}
	/**
	 * Método que realiza o cruzamento entre indivíduos de uma população
	 * 
	 * @param populacaoAux População em que os cruzamentos serão realizados
	 */

	public static void Cruzamento(int[][] populacaoAux)
	{
		// Cruzamento de 2 cortes parcialmente mapeado
		Random gerador = new Random();
		int crossPoint1, crossPoint2 = 0;
		int variavel;

		for (int i = 0; i < NumPopulacao - 1; i = i + 2)
		{
			if (Math.random() <= ProbabilidadeCruzamento)
			{
				crossPoint1 = gerador.nextInt(NumCaracteristicas);
				crossPoint2 = gerador.nextInt(NumCaracteristicas);

				if (crossPoint2 < crossPoint1)
				{
					int aux = crossPoint1;
					crossPoint1 = crossPoint2;
					crossPoint2 = aux;
				}
				int[][] map = new int[2][];

				map[0] = copia(populacaoAux[i]);
				map[1] = copia(populacaoAux[i + 1]);

				for (int j = crossPoint1; j < crossPoint2; j++)
				{
					variavel = populacaoAux[i][j];
					populacaoAux[i][j] = populacaoAux[i + 1][j];
					populacaoAux[i + 1][j] = variavel;
				}

				for (int k = 0; k < crossPoint1; k++)
				{
					for (int j = crossPoint1; j < crossPoint2; j++)
					{
						if (populacaoAux[i][k] == populacaoAux[i][j])
						{
							populacaoAux[i][k] = populacaoAux[i + 1][j];

						}
						if (populacaoAux[i + 1][k] == populacaoAux[i + 1][j])
						{
							populacaoAux[i + 1][k] = populacaoAux[i][j];

						}
					}
				}

				for (int k = crossPoint2; k < NumCaracteristicas; k++)
				{
					for (int j = crossPoint1; j < crossPoint2; j++)
					{
						if (populacaoAux[i][k] == populacaoAux[i][j])
						{
							populacaoAux[i][k] = populacaoAux[i + 1][j];
						}
						if (populacaoAux[i + 1][k] == populacaoAux[i + 1][j])
						{
							populacaoAux[i + 1][k] = populacaoAux[i][j];

						}
					}
				}
				while (repetido(populacaoAux[i]) != -1)
				{
					for (int k = 0; k < populacaoAux[i].length; k++)
					{
						if (repetido(populacaoAux[i]) == map[1][k])
						{
							populacaoAux[i][k] = map[0][k];
						}
					}

				}
				while (repetido(populacaoAux[i + 1]) != -1)
				{
					for (int k = 0; k < populacaoAux[i + 1].length; k++)
					{
						if (repetido(populacaoAux[i + 1]) == map[0][k])
						{
							populacaoAux[i + 1][k] = map[1][k];
						}
					}

				}
			}

		}
	}
/**
 * Repara o vetor de características não deixando elas repetirem
 * 
 * @param vetor Vetor de características
 */
	public static void reparo(int vetor[])
	{
		for (int i = 0; i < vetor.length; i++)
		{
			int aux = vetor[i];
			for (int k = i + 1; k < vetor.length; k++)
			{
				if (vetor[k] == aux)
				{
					Random gerador = new Random();
					vetor[k] = gerador.nextInt(144) + 1;
					k = i + 1;
				}
			}
		}
	}
/**
 * Método que copia um vetor e o retorna
 * 
 * @param vetor Vetor que será copiado
 * @return Retorna o vetor copiado
 */
	public static int[] copia(int[] vetor)
	{
		int[] copia = new int[vetor.length];
		for (int i = 0; i < vetor.length; i++)
		{
			copia[i] = vetor[i];
		}
		return copia;
	}
	/**
	 * Método que acha o melhor indivíduo da população
	 * 
	 * @param Notas Fitness dos individuos 
	 * @param populacao Conjunto de individuos 
	 * @return Retorna o melhor indivíduo da população
	 */

	public static int[] Elite(float[] Notas, int[][] populacao)
	{
		int[] Melhor = new int[NumCaracteristicas];

		for (int i = 0; i < NumCaracteristicas; i++)
			Melhor[i] = populacao[MelhorIndividuo(Notas)][i];

		return Melhor;
	}
	
	/**
	 * Método que procura o melhor indivíduo da população
	 * @param notas Fitness de cada indivíduo
	 * @return Índice do melhor indivíduo
	 */

	public static int MelhorIndividuo(float[] notas)
	{

		float maximo;
		int indice = 0;

		maximo = notas[0];

		for (int q = 0; q < notas.length - 1; q++)
		{

			if (maximo < notas[q + 1])
			{
				maximo = notas[q + 1];
				indice = q + 1;
			}
		}
		return indice;
	}
	/**
	 * Método que seleciona os indivíduos mais adaptados
	 * 
	 * @param populacao População nova
	 * @param populacaoAux População antiga
	 * @param fitness Fitness de cada indivíduo
	 */

	public static void Selecao(int[][] populacao, int[][] populacaoAux, float[] fitness)
	{
		double r1, r2;
		int a = 0;
		for (int i = 0; i < NumPopulacao; i++)
		{
			r1 = (int) Math.round(a + (NumPopulacao - 1) * Math.random());
			r2 = (int) Math.round(a + (NumPopulacao - 1) * Math.random());

			if (fitness[(int) r1] >= fitness[(int) r2])
			{
				//*
				for (int j = 0; j < NumCaracteristicas; j++)
				{
					populacaoAux[i][j] = populacao[(int) r1][j];
				}
			} else
			{
				//*
				for (int j = 0; j < NumCaracteristicas; j++)
				{
					populacaoAux[i][j] = populacao[(int) r2][j];
				}
			}
		}

	}
	
	/**
	 * Realiza o elitismo mantendo o melhor indivíduo da população
	 * 
	 * @param Notas Fitness de cada indivíduo
	 * @param populacaoAux População que será feito o elitismo
	 * @param top Melhor indivíduo 
	 */

	public static void Elitismo(float[] Notas, int[][] populacaoAux, int[] top)
	{
		for (int i = 0; i < NumCaracteristicas; i++)
			populacaoAux[PiorIndividuo(Notas)][i] = top[i];
	}
	/**
	 * Encontra o pior indivíduo da população 
	 * 
	 * @param Notas Fitness dos indivíduos
	 * @return Índice do pior indivíduo
	 */
	public static int PiorIndividuo(float Notas[])
	{

		float menor;

		int indice = 0;
		menor = Notas[0];
		for (int j = 0; j < Notas.length - 1; j++)
		{

			if (menor > Notas[j + 1])
			{
				menor = Notas[j + 1];
				indice = j + 1;
			}
		}
		return indice;
	}
/**
 * Inicializa a população do algoritmo genético
 * 
 * @param populacao População do algoritmo genético
 * @return População inicializada
 */
	public static int[][] InicializaPopulacao(int[][] populacao)
	{

		populacao = new int[NumPopulacao][NumCaracteristicas];

		for (int i = 0; i < NumPopulacao; i++)
		{
			for (int j = 0; j < NumCaracteristicas; j++)
			{
				Random gerador = new Random();
				int k = gerador.nextInt(144) + 1;

				while (ja_existe(populacao[i], k) || k == 0 || k == 1 || k == 2 || k == 3)
				{
					k = gerador.nextInt(144) + 1;
				}
				populacao[i][j] = k;
			}
		}
		return populacao;

	}
/**
 * Verifica se um número já está contido no vetor
 * @param valores Vetor com os números da características
 * @param ultimo_valor Valor que será procurado
 * @return Retorna se o elemento está contido ou não
 */
	public static boolean ja_existe(int valores[], int ultimo_valor)
	{
		boolean jaTem = false;

		for (int i = 0; i < valores.length; i++)
		{
			if (valores[i] == ultimo_valor)
			{
				jaTem = true;
				break;
			}
		}

		return jaTem;
	}
/**
 * Verifica se um número já existe no vetor
 * 
 * @param populacao Conjunto de indivíduos do algoritmo genético
 * @return Retorna a posição do elemento repetido
 */
	public static int repetido(int[] populacao)
	{
		int resp = -1;
		for (int i = 0; i < populacao.length; i++)
		{
			int num = populacao[i];
			for (int j = i + 1; j < populacao.length; j++)
			{
				if (populacao[j] == num)
				{
					resp = j;
					break;
				}
			}
		}
		return resp;
	}
	/**
	 * Algoritmo de ordenação 
	 * 
	 * @param a String que será ordenada
	 * @param ini Início da string
	 * @param fim Final da String
	 */
	public static void quicksort(String[] a, int ini, int fim)
	{
		int meio;

		if (ini < fim)
		{
			meio = partition(a, ini, fim);
			quicksort(a, ini, meio);
			quicksort(a, meio + 1, fim);
		}
	}
	/**
	 * @param a String particionada
	 * @param ini Ínicio do vetor
	 * @param fim  Final do vetor
	 * @return
	 */

	public static int partition(String[] a, int ini, int fim)
	{
		int pivo, topo, i;
		pivo = Integer.parseInt(a[ini]);
		topo = ini;

		for (i = ini + 1; i <= fim; i++)
		{
			if (Integer.parseInt(a[i]) < pivo)
			{
				a[topo] = a[i];
				a[i] = a[topo + 1];
				topo++;
			}
		}
		a[topo] = "" + pivo;
		return topo;
	}

	/**
	 * Retorna o valor da acurácia da classificação das proteínas
	 * 
	 * @param nomeArquivo - Nome do arquivo arff que contem os vetores de
	 *            caracteristicas para a classificação
	 * @return Retorna a acurácia do individuo
	 * @throws Exception
	 */

	public static float Classificacao(String nomeArquivo) throws Exception
	{
		String fileName = nomeArquivo + ".arff";
		DataSource source = null;
		Instances dataInstances = null;
		Evaluation evaluation = null;
		double best = 0, bestG = 0, bestC = 0, correct = 0;

		for (double c = 3; c < 4; c = c + 2)
		{
			for (double g = -1; g > -6; g = g - 2)
			{
				double cost = Math.pow(2, c);//look at libsvm guide, change if you want
				double gamma = Math.pow(2, g);

				Classifier classifier = new LibSVM();
				((LibSVM) classifier).setCost(cost);
				((LibSVM) classifier).setGamma(gamma);
				source = new DataSource(fileName);
				dataInstances = source.getDataSet();
				dataInstances.setClassIndex(dataInstances.numAttributes() - 1);
				classifier.buildClassifier(dataInstances);
				evaluation = new Evaluation(dataInstances);
				evaluation.crossValidateModel(classifier, dataInstances, 10, new Random(1));

				correct = evaluation.pctCorrect();

				if (correct > best)
				{
					best = correct;
					bestC = cost;
					bestG = gamma;
				}

				classifier = null;
				source = null;
				dataInstances = null;
				evaluation = null;
			}

		}
		return (float) best;
	}
	/**
	 * Método de arredondamento de números decimais
	 * 
	 * @param d Número decimal
	 * @param c Quantidade de casas decimais desejadas
	 * @return arredonda um número com a quantidade de casa decimais desejadas
	 */
	public static double roundToDecimals(double d, int c)
	{
		int temp = (int) ((d * Math.pow(10, c)));
		return (((double) temp) / Math.pow(10, c));
	}
}