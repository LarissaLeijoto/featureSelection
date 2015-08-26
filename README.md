# Um algoritmo genético para a seleção de características utilizadas na predição de função de proteínas
Documentação do trabalho elaborado na Iniciação Cientifica: O Uso de Técnicas de Aprendizado de Máquina na Predição de Função de Proteínas

Orientadora: Cristiane Neri Nobre
Orientada: Larissa Fernandes Leijôto


Artigos que possuem relação com o projeto: 
A Genetic Algorithm for the Selection of Features Used in the Prediction of Protein Function. 2014 IEEE International Conference on Bioinformatics and Bioengineering
Predição de Função de Proteínas Através da Extração de Características Físico-Químicas. Revista de Informática Teórica e Aplicada: RITA  

A base de dados utilizada foi a mesma do artigo de Dobson e Doig: Predicting Enzyme Class From Protein Structure Without Alignments, Journal of Molecular Biology.
Essa base de dados é utilizada para efeitos de comparação. Os tipo de proteínas utilizadas são as Enzimas,  que podem ser separadas em 6 superclasses: Hydrolase, Isomerase, Oxidoreductase, Ligase, Liase e Transferase. Cada classe catalisa diferentes reações, e essas reações estão diretamente relacionadas com suas funções.
O banco de dados usado para a extração das caracteristicas foi o Sting_DB. O artigo relacionado a essa base é: Uma metodologia para seleção de parâmetros em modelos de classificação de proteínas.
Existem dois scripts para extrair essa base do banco de dados mencionado. A execução do script é feita por linha de comando. O nome do script é: executa.sh. Para executá-lo é necessário um arquivo txt com o código pdb de cada proteína a ser extraída da base.
Após a execução do script, ele retornará cada proteína como sendo uma tabela .xls. Essa tabela será dividida em dois sheets, cada um contendo N características.

O algoritmo genético implementado e disponibilizado nessa pasta é utilizado para a seleção das características contidas na base de dados Sting_DB. Ao final da execução ele retorna as características que melhor preveram as funções das proteínas contidas na base de dados.
