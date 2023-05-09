1-Fazer o sinc fork com o projeto da comunidade java_nfe, sempre selecionar o branch master e clicar em sinc fork

![image](https://github.com/asaasdev/Java_NFe/assets/81778352/90ffb830-2fda-4201-8845-f08351f16495)

2-Criar um branch a partir do branch release

3-Fazer o merge da branch criadA com a master(comunidade)

4-Fazer as alterações necessários para implementar as novas regras ou em caso de quebra de código.

5-Igualar a versão do arquivo pom-base.xml com o pom.xml que é gerenciado pela comunidade.

  -Verificar se alguma alteração no pom.xml afeta ao pom-base.xml, somente verificar somente inserção/Alterção das dependencias
  
  -Precisamos manter esse pom-base customizado para fazemos a publicação do jar no nexus.

![image](https://github.com/asaasdev/Java_NFe/assets/81778352/1b63365c-0dda-4a31-8a88-359030036bfc)

5-Abrir um Pull Request para o branch release

Para fazer o merge para produção deve-se ser entre a branch release e master_base

