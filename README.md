# O Uso de Técnicas de Aprendizado de Máquina na Predição de Função de Proteínas 

**Larissa Fernandes Leijôto, Luis Enrique Zárate (Colaborador), Cristiane Neri Nobre (Orientadora)**

## Resumo 
Realizar a previsão da função de uma proteína a partir da sua sequência é um dos problemas fundamentais da bioinformática, uma vez que os métodos de determinação experimental não conseguem  acompanhar  o  ritmo  a  que  novos  genomas  são  sequenciados.  O  objetivo  desse projeto foi fornecer uma nova metodologia para a predição de função de proteínas através da análise das estruturas das proteínas utilizando-se  Support  Vector Machine  e  Algoritmos  Genéticos.  Para  isso  foi considerado um  conjunto  de  enzimas  analisadas  por  Dobson  e  Doig (2005).



## Artigos gerados a partir do projeto:
> LEIJOTO, Larissa Fernandes ; RODRIGUES, Thiago Assis de Oliveira ; Zárate, Luis ; NOBRE, Cristiane Neri . A Genetic algorithm for the selection of features used in the prediction of protein function. In: Conference on BioInformatics and BioEngineering, 2014, Boca Raton, Florida. BIBE 2014, 2014.

> RODRIGUES, Thiago Assis de Oliveira ; LEIJOTO, Larissa Fernandes ; BRANDAO, P. C. O. ; NOBRE, Cristiane Neri . Predição de Função de Proteínas Através da Extração de Características Físico-Químicas. Revista de Informática Teórica e Aplicada: RITA, v. 22, p. 29-51, 2015. 

## Dissertação desenvolvida a partir do projeto:
1. Gabriela Teodoro de Oliveira Santos. Predição de função de proteínas. Início: 2013. Dissertação (Mestrado em Programa de Pós Graduação em Informática) - Pontifícia Universidade Católica de Minas Gerais. (Coorientador). Em andamento.

## Monografias desenvolvidas a partir do projeto:

1. Marcos Felipe. Algoritmos Genéticos para ajuste de parâmetros da SVM. 2015. Trabalho de Conclusão de Curso. (Graduação em Ciência da Computação) - Pontifícia Universidade Católica de Minas Gerais. Orientador: Cristiane Neri Nobre.

2. Guilherme Padilha. Codificação para Predição de Função de Proteínas. 2015. Trabalho de Conclusão de Curso. (Graduação em Sistemas de Informação) - Pontifícia Universidade Católica de Minas Gerais. Orientador: Cristiane Neri Nobre.

3. Larissa Fernandes Leijôto. Um algoritmo genético para a seleção de características utilizadas na predição de função de proteínas. 2014. Trabalho de Conclusão de Curso. (Graduação em Ciência da Computação) - Pontifícia Universidade Católica de Minas Gerais, Fundação de Amparo à Pesquisa do Estado de Minas Gerais. Orientador: Cristiane Neri Nobre.

4. Thiago Assis de Oliveira Rodrigues. Predição de função de proteínas através da extração de características da estrutura primária da proteína. 2013. Trabalho de Conclusão de Curso. (Graduação em Ciência da Computação) - Pontifícia Universidade Católica de Minas Gerais. Orientador: Cristiane Neri Nobre. 

5. Guilherme Pereira Gasparini Kingma. Previsão de Função de enzimas a partir da análise das estruturas de aminoácidos. 2013. Trabalho de Conclusão de Curso. (Graduação em Ciência da Computação) - Pontifícia Universidade Católica de Minas Gerais. Orientador: Cristiane Neri Nobre. 

6. Pedro Ribeiro Bastos Soares. Predição de Função de Enzimas utilizando Máquina de Vetores de Suporte. 2012. Trabalho de Conclusão de Curso. (Graduação em Ciência da Computação) - Pontifícia Universidade Católica de Minas Gerais. Orientador: Cristiane Neri Nobre. 

***Os autores agradecem o apoio financeiro recebido da Fundação de Amparo à Pesquisa do Estado de Minas Gerais-FAPEMIG (Projeto APQ-01565-12) e ao Centro Nacional de Supercomputação (CESUP) da Universidade Federal do Rio Grande do Sul (UFRGS).***

___________________________________________________________________________________________________________________

# Breve drescrição dobre o funcionamento da ferramenta

A execução do script é feita por linha de comando. O nome do script é: executa.sh. Para executá-lo é necessário um arquivo txt com o código pdb de cada proteína a ser extraída da base.
Após a execução do script, ele retornará cada proteína como sendo uma tabela .xls. Essa tabela será dividida em dois sheets, cada um contendo N características.

O algoritmo genético implementado e disponibilizado nessa pasta é utilizado para a seleção das características contidas na base de dados Sting_DB. Ao final da execução ele retorna as características que melhor preveram as funções das proteínas contidas na base de dados.
